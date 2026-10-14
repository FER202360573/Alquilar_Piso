package com.api.apartamentos.exceptions;

// Se lanza cuando un apartamento, cliente, reservación o pago no existe (responde 404)
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
