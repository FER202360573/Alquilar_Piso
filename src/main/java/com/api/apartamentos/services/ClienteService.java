package com.api.apartamentos.services;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.api.apartamentos.exceptions.RecursoNoEncontradoException;
import com.api.apartamentos.models.ClienteModel;
import com.api.apartamentos.repositories.IApartamentoRepository;
import com.api.apartamentos.repositories.IClienteRepository;
import com.api.apartamentos.repositories.IReservacionRepository;

@Service
public class ClienteService {

    private static final Set<String> TIPOS = Set.of("ARRENDATARIO", "PROPIETARIO");

    private final IClienteRepository clienteRepository;
    private final IApartamentoRepository apartamentoRepository;
    private final IReservacionRepository reservacionRepository;

    public ClienteService(IClienteRepository clienteRepository,
                          IApartamentoRepository apartamentoRepository,
                          IReservacionRepository reservacionRepository) {
        this.clienteRepository = clienteRepository;
        this.apartamentoRepository = apartamentoRepository;
        this.reservacionRepository = reservacionRepository;
    }

    public List<ClienteModel> listar(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return clienteRepository.findAll();
        }
        return clienteRepository.findByTipo(tipo.trim().toUpperCase());
    }

    public ClienteModel obtener(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un cliente con id " + id));
    }

    public ClienteModel crear(ClienteModel cliente) {
        cliente.setId(null);
        validar(cliente);
        if (clienteRepository.existsByCorreo(cliente.getCorreo())) {
            throw new IllegalStateException("Ya existe un cliente con el correo " + cliente.getCorreo());
        }
        return clienteRepository.save(cliente);
    }

    public ClienteModel actualizar(Long id, ClienteModel datos) {
        ClienteModel existente = obtener(id);
        validar(datos);
        if (!existente.getCorreo().equalsIgnoreCase(datos.getCorreo())
                && clienteRepository.existsByCorreo(datos.getCorreo())) {
            throw new IllegalStateException("Ya existe un cliente con el correo " + datos.getCorreo());
        }
        existente.setNombre(datos.getNombre());
        existente.setApellidos(datos.getApellidos());
        existente.setCorreo(datos.getCorreo());
        existente.setTelefono(datos.getTelefono());
        existente.setTipo(datos.getTipo());
        return clienteRepository.save(existente);
    }

    public void eliminar(Long id) {
        obtener(id);
        if (apartamentoRepository.existsByPropietario_Id(id) || reservacionRepository.existsByCliente_Id(id)) {
            throw new IllegalStateException(
                    "No se puede eliminar el cliente porque tiene apartamentos o reservaciones registrados.");
        }
        clienteRepository.deleteById(id);
    }

    private void validar(ClienteModel c) {
        requerido(c.getNombre(), "nombre");
        requerido(c.getApellidos(), "apellidos");
        requerido(c.getCorreo(), "correo");
        requerido(c.getTipo(), "tipo");
        String tipo = c.getTipo().trim().toUpperCase();
        if (!TIPOS.contains(tipo)) {
            throw new IllegalArgumentException("Valor inválido para 'tipo'. Permitidos: " + TIPOS);
        }
        c.setTipo(tipo);
        c.setCorreo(c.getCorreo().trim());
    }

    private void requerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo '" + campo + "' es obligatorio.");
        }
    }
}
