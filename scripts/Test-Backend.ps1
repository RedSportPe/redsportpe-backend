param([string]$PgBin = 'C:\Program Files\PostgreSQL\17\bin')
$ErrorActionPreference = 'Stop'
$taskRepo = Split-Path -Parent $PSScriptRoot
$taskTarget = Join-Path $taskRepo 'target'
$taskData = Join-Path $taskTarget ('postgres-test-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $taskData -Force | Out-Null
$taskResolved = [IO.Path]::GetFullPath($taskData)
if (-not $taskResolved.StartsWith([IO.Path]::GetFullPath($taskTarget) + [IO.Path]::DirectorySeparatorChar)) { throw 'Unsafe test database directory' }
$taskListener = [Net.Sockets.TcpListener]::new([Net.IPAddress]::Loopback, 0)
$taskListener.Start()
$taskPort = $taskListener.LocalEndpoint.Port
$taskListener.Stop()
$taskStarted = $false
$taskOldUrl=$env:TEST_DATABASE_URL
$taskOldUser=$env:TEST_DATABASE_USERNAME
$taskOldPassword=$env:TEST_DATABASE_PASSWORD
try {
    & (Join-Path $PgBin 'initdb.exe') -D $taskData -U redsport_test -A trust --encoding=UTF8 --locale=C > (Join-Path $taskTarget 'postgres-init.log') 2>&1
    if ($LASTEXITCODE -ne 0) { throw 'initdb failed; see target/postgres-init.log' }
    & (Join-Path $PgBin 'pg_ctl.exe') -D $taskData -l (Join-Path $taskData 'server.log') -o "-p $taskPort -h 127.0.0.1" -w start
    if ($LASTEXITCODE -ne 0) { throw 'PostgreSQL test instance failed to start' }
    $taskStarted = $true
    & (Join-Path $PgBin 'createdb.exe') -h 127.0.0.1 -p $taskPort -U redsport_test redsport_test
    if ($LASTEXITCODE -ne 0) { throw 'createdb failed' }
    foreach ($taskSql in @('db\baseline\000_existing_schema.sql','db\test\legacy_fixture.sql','db\migrations\001_complete_backend.sql','db\test\verify_migration.sql')) {
        & (Join-Path $PgBin 'psql.exe') -X -v ON_ERROR_STOP=1 -h 127.0.0.1 -p $taskPort -U redsport_test -d redsport_test -f (Join-Path $taskRepo $taskSql) > (Join-Path $taskTarget 'migration-test.log') 2>&1
        if ($LASTEXITCODE -ne 0) { throw "SQL failed: $taskSql. See target/migration-test.log" }
    }
    $env:TEST_DATABASE_URL="jdbc:postgresql://127.0.0.1:$taskPort/redsport_test"
    $env:TEST_DATABASE_USERNAME='redsport_test'
    $env:TEST_DATABASE_PASSWORD=''
    Push-Location $taskRepo
    try {
        & .\mvnw.cmd -B -ntp test
        if ($LASTEXITCODE -ne 0) { throw 'Backend tests failed' }
    } finally { Pop-Location }
} finally {
    $env:TEST_DATABASE_URL=$taskOldUrl
    $env:TEST_DATABASE_USERNAME=$taskOldUser
    $env:TEST_DATABASE_PASSWORD=$taskOldPassword
    if ($taskStarted) { & (Join-Path $PgBin 'pg_ctl.exe') -D $taskData -m fast -w stop }
    # Retain only this stopped, isolated cluster under target/ for diagnosis.
    # The application's configured PostgreSQL server is never modified.
}
