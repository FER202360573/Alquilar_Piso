package com.api.apartamentos.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.apartamentos.exceptions.RecursoNoEncontradoException;
import com.api.apartamentos.models.PagoModel;
import com.api.apartamentos.models.ReservacionModel;
import com.api.apartamentos.repositories.IPagoRepository;
import com.api.apartamentos.repositories.IReservacionRepository;

@Service
public class PagoService {

    private static final Set<String> METODOS = Set.of("EFECTIVO", "TARJETA", "TRANSFERENCIA");

    private final IPagoRepository pagoRepository;
    private final IReservacionRepository reservacionRepository;

    public PagoService(IPagoRepository pagoRepository, IReservacionRepository reservacionRepository) {
        this.pagoRepository = pagoRepository;
        this.reservacionRepository = reservacionRepository;
    }

    public List<PagoModel> listar() {
        return pagoRepository.findAll();
    }

    public PagoModel obtener(Long id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un pago con id " + id));
    }

    public List<PagoModel> listarPorReservacion(Long reservacionId) {
        buscarReservacion(reservacionId);
        return pagoRepository.findByReservacion_Id(reservacionId);
    }

    @Transactional
    public PagoModel crear(PagoModel request) {
        if (request == null || request.getReservacion() == null || request.getReservacion().getId() == null) {
            throw new IllegalArgumentException("Se requiere 'reservacion.id'.");
        }
        if (request.getMonto() == null || request.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El 'monto' es obligatorio y debe ser mayor a 0.");
        }

        ReservacionModel reservacion = buscarReservacion(request.getReservacion().getId());
        if (!"CONFIRMADA".equals(reservacion.getEstado())) {
            throw new IllegalStateException("Solo se pueden registrar pagos de reservaciones confirmadas.");
        }

        BigDecimal saldo = reservacion.getTotal().subtract(totalPagado(reservacion.getId()));
        if (request.getMonto().compareTo(saldo) > 0) {
            throw new IllegalStateException("El monto excede el saldo pendiente (" + saldo + ").");
        }

        String metodo = (request.getMetodo() == null || request.getMetodo().isBlank())
                ? "EFECTIVO" : request.getMetodo().trim().toUpperCase();
        if (!METODOS.contains(metodo)) {
            throw new IllegalArgumentException("Valor inválido para 'metodo'. Permitidos: " + METODOS);
        }

        PagoModel pago = new PagoModel();
        pago.setReservacion(reservacion);
        pago.setMonto(request.getMonto());
        pago.setMetodo(metodo);
        pago.setFechaPago(request.getFechaPago() != null ? request.getFechaPago() : LocalDate.now());
        return pagoRepository.save(pago);
    }

    // Estado de cuenta de una reservación: total, pagado, saldo y lista de pagos
    public Map<String, Object> estadoCuenta(Long reservacionId) {
        ReservacionModel r = buscarReservacion(reservacionId);
        BigDecimal pagado = totalPagado(reservacionId);
        BigDecimal saldo = r.getTotal().subtract(pagado);

        Map<String, Object> cuenta = new LinkedHashMap<>();
        cuenta.put("reservacionId", r.getId());
        cuenta.put("cliente", r.getCliente().getNombre() + " " + r.getCliente().getApellidos());
        cuenta.put("apartamento", r.getApartamento().getTitulo());
        cuenta.put("estadoReservacion", r.getEstado());
        cuenta.put("total", r.getTotal());
        cuenta.put("totalPagado", pagado);
        cuenta.put("saldo", saldo);
        cuenta.put("estadoCuenta", saldo.compareTo(BigDecimal.ZERO) <= 0 ? "PAGADO" : "PENDIENTE");
        cuenta.put("pagos", pagoRepository.findByReservacion_Id(reservacionId));
        return cuenta;
    }

    public BigDecimal totalPagado(Long reservacionId) {
        BigDecimal suma = BigDecimal.ZERO;
        for (PagoModel p : pagoRepository.findByReservacion_Id(reservacionId)) {
            suma = suma.add(p.getMonto());
        }
        return suma;
    }

    private ReservacionModel buscarReservacion(Long id) {
        return reservacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una reservación con id " + id));
    }
}
