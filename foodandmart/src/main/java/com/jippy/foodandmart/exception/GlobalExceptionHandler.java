package com.jippy.foodandmart.exception;

import com.jippy.foodandmart.dto.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {
    //        Handling Resource Bad-request in postman
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            WebRequest webRequest) {

        ErrorResponseDto error = new ErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    //        handling if already exists Duplicate Data
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicateResourceException(
            DuplicateResourceException ex,
            WebRequest webRequest) {

        ErrorResponseDto error = new ErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.CONFLICT,
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidUserTypeException.class)
    public ResponseEntity<String> handleInvalidUserType(InvalidUserTypeException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    //        General Exceptional Handler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGlobalException(
            Exception ex,
            WebRequest webRequest) {

        ErrorResponseDto error = new ErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(PricingException.class)
    public ResponseEntity<ErrorResponseDto> handlePricingException(
            PricingException ex, HttpServletRequest req) {

        return ResponseEntity.badRequest().body(
                new ErrorResponseDto(
                        req.getRequestURI(),
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage(),
                        LocalDateTime.now()
                )
        );
    }
}
