package com.taskmanager.exception.handler;
import com.taskmanager.exception.common.BadRequestException;
import com.taskmanager.exception.domain.BadStatusProjectException;
import com.taskmanager.exception.common.DuplicateResourceException;
import com.taskmanager.exception.common.ResourceNotFoundException;
import com.taskmanager.exception.domain.UserByEmailNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ========== НЕ КОРЕКТНЫЙ REQUEST ==========
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handlerBadRequest(BadRequestException ex){
        log.error("Ошибка в Request: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    // ========== НЕ КОРЕКТНЫЙ СТАТУС ПРОЕКТА ==========
    @ExceptionHandler(BadStatusProjectException.class)
    public ResponseEntity<ErrorResponse> handlerBadStatusProject(BadStatusProjectException ex){
        log.error("Статус должен быть ACTIVE, COMPLETED, ARCHIVED: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    // ========== 1. ОШИБКА ВАЛИДАЦИИ @Valid ==========
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handlerMismatchException(MethodArgumentTypeMismatchException ex){
        log.error("Параметр не является корректным типом: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handlerViolationException(ConstraintViolationException ex){
        log.error("Информация об ошибке: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }


    // ОШИБКА ВАЛИДАЦИИ, СТРОКА ВМЕСТО ID
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handlerValidationException(MethodArgumentNotValidException ex){
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField()
                        + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        ErrorResponse error = new ErrorResponse(
            message, HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    // ========== 1. ПОЛЬЗОВАТЕЛЯ С ДАННЫМ EMAIL НЕ СУЩЕСТВУЕТ ==========
    @ExceptionHandler(UserByEmailNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlerUserByEmailNotFound(UserByEmailNotFoundException ex){
        log.error("Пользователь с данным email: {} не найден", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // ========== 2. ДУБЛИКАТ EMAIL ==========
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handlerEmailDuplicate(DuplicateResourceException ex){
        log.error("Пользователь с данным email: {} существует", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.CONFLICT.value()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // ========== 3. РЕСУРС НЕ НАЙДЕН ==========
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlerNotFound(ResourceNotFoundException ex){
        log.error("Данные не были найдены: {}", ex.getMessage());
        //Возвращаем клиенту
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    // ========== 4. ВСЕ ОСТАЛЬНЫЕ ОШИБКИ ==========
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex){
        log.error("Неизвестная ошибка: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Ошибка сервера", 500));
    }


}

