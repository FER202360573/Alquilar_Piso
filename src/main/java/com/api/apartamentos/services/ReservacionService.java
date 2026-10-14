package com.api.apartamentos.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.apartamentos.exceptions.RecursoNoEncontradoException;
import com.api.apartamentos.models.ApartamentoModel;
import com.api.apartamentos.models.ClienteModel;
import com.api.apartamentos.models.ReservacionModel;
import com.api.apartamentos.repositories.IApartamentoRepository;
import com.api.apartamentos.repositories.IClienteRepository;
import com.api.apartamentos.repositories.IReservacionRepository;

@Service
public class ReservacionService {

    private final IReservacionRepository reservacionRepository;
    private final IApartamentoRepository apartamentoRepository;
    private final IClienteRepository clienteRepository;

    public ReservacionService(IReservacionRepository reservacionRepository,
                              IApartamentoRepository apartamentoRepository,
                              IClienteRepository clienteRepository) {
        this.reservacionRepository = reservacionRepository;
        this.apartamentoRepository = apartamentoRepository;
        this.clienteRepository = clienteRepository;
    }

    public List<ReservacionModel> listar() {
        return reservacionRepository.findAll();
    }

    public ReservacionModel obtener(Long id) {
        return reservacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una reservación con id " + id));
    }

    public List<ReservacionModel> listarPorCliente(Long clienteId) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new RecursoNoEncontradoException("No existe un cliente con id " + clienteId);
        }
        return reservacionRepository.findByCliente_Id(clienteId);
    }

    // Crea la reservación CONFIRMADA y calcula el total (días x precioPorDia)
    @Transactional
    public ReservacionModel crear(ReservacionModel request) {
        if (request == null || request.getApartamento() == null || request.getApartamento().getId() == null
                || request.getCliente() == null || request.getCliente().getId() == null) {
            throw new IllegalArgumentException("Se requieren 'apartamento.id' y 'cliente.id'.");
        }
        LocalDate inicio = request.getFechaInicio();
        LocalDate fin = request.getFechaFin();
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Se requieren 'fechaInicio' y 'fechaFin' (yyyy-MM-dd).");
        }
        if (!fin.isAfter(inicio)) {
            throw new IllegalArgumentException("La 'fechaFin' debe ser posterior a la 'fechaInicio'.");
        }

        Long apartamentoId = request.getApartamento().getId();
        Long clienteId = request.getCliente().getId();
        ApartamentoModel apartamento = apartamentoRepository.findById(apartamentoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un apartamento con id " + apartamentoId));
        ClienteModel cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un cliente con id " + clienteId));

        if (!"ARRENDATARIO".equals(cliente.getTipo())) {
            throw new IllegalArgumentException("El cliente debe ser de tipo ARRENDATARIO.");
        }
        if (!"ACTIVO".equals(apartamento.getEstado())) {
            throw new IllegalStateException("El apartamento no está activo.");
        }
        if (reservacionRepository.hayTraslape(apartamento.getId(), inicio, fin)) {
            throw new IllegalStateException("El apartamento no está disponible en esas fechas.");
        }

        long dias = ChronoUnit.DAYS.between(inicio, fin);

        ReservacionModel reservacion = new ReservacionModel();
        reservacion.setApartamento(apartamento);
        reservacion.setCliente(cliente);
        reservacion.setFechaInicio(inicio);
        reservacion.setFechaFin(fin);
        reservacion.setTotal(apartamento.getPrecioPorDia().multiply(BigDecimal.valueOf(dias)));
        reservacion.setEstado("CONFIRMADA");
        return reservacionRepository.save(reservacion);
    }

    public ReservacionModel cancelar(Long id) {
        ReservacionModel reservacion = obtener(id);
        if ("CANCELADA".equals(reservacion.getEstado())) {
            throw new IllegalStateException("La reservación ya está cancelada.");
        }
        reservacion.setEstado("CANCELADA");
        return reservacionRepository.save(reservacion);
    }
}
