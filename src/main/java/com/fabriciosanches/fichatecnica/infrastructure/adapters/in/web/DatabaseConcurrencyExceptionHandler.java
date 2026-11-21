package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.HandlerMapping;

@RestControllerAdvice
public class DatabaseConcurrencyExceptionHandler {

    private static final Logger logger = LogManager.getLogger(DatabaseConcurrencyExceptionHandler.class);

    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<ProblemDetail> handleConflict(ConcurrencyFailureException exception,
                                                       HttpServletRequest request) {
        logger.warn("Conflito de banco em {} {}: {}", request.getMethod(),
                request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE),
                exception.getClass().getSimpleName());
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Os dados estão sendo alterados por outra transação. Aguarde e tente novamente.");
        problem.setTitle("Conflito de concorrência");
        problem.setProperty("code", "DATABASE_CONFLICT");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }
}
