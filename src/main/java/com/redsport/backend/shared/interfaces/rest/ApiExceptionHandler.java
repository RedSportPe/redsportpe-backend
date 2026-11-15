package com.redsport.backend.shared.interfaces.rest;

import com.redsport.backend.shared.domain.model.DomainException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {
  private ResponseEntity<ProblemDetail> problem(int status, String detail) {
    return ResponseEntity.status(status)
        .body(ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(status), detail));
  }

  @ExceptionHandler(DomainException.class)
  ResponseEntity<ProblemDetail> domain(DomainException e) {
    return problem(e.status(), e.getMessage());
  }

  @ExceptionHandler({
    IllegalArgumentException.class,
    MethodArgumentTypeMismatchException.class,
    HttpMessageNotReadableException.class
  })
  ResponseEntity<ProblemDetail> invalid(Exception e) {
    return problem(
        400, e instanceof IllegalArgumentException ? e.getMessage() : "Solicitud inválida");
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ProblemDetail> validation(MethodArgumentNotValidException e) {
    return problem(
        400,
        e.getBindingResult().getFieldErrors().stream()
            .map(x -> x.getField() + ": " + x.getDefaultMessage())
            .distinct()
            .collect(java.util.stream.Collectors.joining("; ")));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<ProblemDetail> duplicate(Exception e) {
    return problem(409, "La operación entra en conflicto con datos existentes");
  }

  @ExceptionHandler(BadCredentialsException.class)
  ResponseEntity<ProblemDetail> credentials(Exception e) {
    return problem(401, "Credenciales inválidas");
  }

  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<ProblemDetail> forbidden(Exception e) {
    return problem(403, "No tienes permiso para esta operación");
  }
}
