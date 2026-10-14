package com.api.apartamentos.exceptions;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    // 404: el recurso no existe
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> noEncontrado(RecursoNoEncontradoException ex) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 400: datos faltantes o inválidos
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> datosInvalidos(IllegalArgumentException ex) {
        return responder(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 409: la operación choca con el estado actual (fechas ocupadas, duplicados, saldo, etc.)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> conflicto(IllegalStateException ex) {
        return responder(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> integridad(DataIntegrityViolationException ex) {
        return responder(HttpStatus.CONFLICT,
                "La operación viola una restricción de la base de datos (registro duplicado o con datos relacionados).");
    }

    // 400: JSON mal formado o fechas con formato incorrecto
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> jsonInvalido(HttpMessageNotReadableException ex) {
        return responder(HttpStatus.BAD_REQUEST,
                "El cuerpo de la petición no es un JSON válido o tiene fechas con formato incorrecto (usa yyyy-MM-dd).");
    }

    // 400: parámetro de la URL con formato incorrecto (por ejemplo una fecha)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> parametroInvalido(MethodArgumentTypeMismatchException ex) {
        return responder(HttpStatus.BAD_REQUEST, "El parámetro '" + ex.getName() + "' tiene un formato incorrecto.");
    }

    private ResponseEntity<Map<String, String>> responder(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status).body(Map.of("error", mensaje));
    }
}
