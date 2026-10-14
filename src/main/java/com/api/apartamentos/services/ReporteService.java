package com.api.apartamentos.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.api.apartamentos.models.ApartamentoModel;
import com.api.apartamentos.models.PagoModel;
import com.api.apartamentos.models.ReservacionModel;
import com.api.apartamentos.repositories.IApartamentoRepository;
import com.api.apartamentos.repositories.IPagoRepository;
import com.api.apartamentos.repositories.IReservacionRepository;

@Service
public class ReporteService {

    private final IApartamentoRepository apartamentoRepository;
    private final IReservacionRepository reservacionRepository;
    private final IPagoRepository pagoRepository;
    private final PagoService pagoService;

    public ReporteService(IApartamentoRepository apartamentoRepository,
                          IReservacionRepository reservacionRepository,
                          IPagoRepository pagoRepository,
                          PagoService pagoService) {
        this.apartamentoRepository = apartamentoRepository;
        this.reservacionRepository = reservacionRepository;
        this.pagoRepository = pagoRepository;
        this.pagoService = pagoService;
    }

    // Ocupación de cada apartamento activo entre inicio y fin (ambos días incluidos)
    public Map<String, Object> ocupacion(LocalDate inicio, LocalDate fin) {
        validarRango(inicio, fin);
        LocalDate finExclusivo = fin.plusDays(1);
        long diasRango = ChronoUnit.DAYS.between(inicio, finExclusivo);

        List<Map<String, Object>> detalle = new ArrayList<>();
        long sumaOcupados = 0;
        long sumaDisponibles = 0;

        for (ApartamentoModel a : apartamentoRepository.findAll()) {
            if (!"ACTIVO".equals(a.getEstado())) {
                continue;
            }
            long ocupados = 0;
            for (ReservacionModel r : reservacionRepository.findByApartamento_IdAndEstado(a.getId(), "CONFIRMADA")) {
                LocalDate desde = r.getFechaInicio().isAfter(inicio) ? r.getFechaInicio() : inicio;
                LocalDate hasta = r.getFechaFin().isBefore(finExclusivo) ? r.getFechaFin() : finExclusivo;
                if (hasta.isAfter(desde)) {
                    ocupados += ChronoUnit.DAYS.between(desde, hasta);
                }
            }
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("apartamentoId", a.getId());
            fila.put("titulo", a.getTitulo());
            fila.put("diasOcupados", ocupados);
            fila.put("diasEnRango", diasRango);
            fila.put("ocupacionPorcentaje", porcentaje(ocupados, diasRango));
            detalle.add(fila);

            sumaOcupados += ocupados;
            sumaDisponibles += diasRango;
        }

        Map<String, Object> reporte = new LinkedHashMap<>();
        reporte.put("inicio", inicio);
        reporte.put("fin", fin);
        reporte.put("ocupacionGeneralPorcentaje", porcentaje(sumaOcupados, sumaDisponibles));
        reporte.put("apartamentos", detalle);
        return reporte;
    }

    // Pagos recibidos entre inicio y fin (ambos días incluidos)
    public Map<String, Object> ingresos(LocalDate inicio, LocalDate fin) {
        validarRango(inicio, fin);
        BigDecimal total = BigDecimal.ZERO;
        Map<String, BigDecimal> porMetodo = new LinkedHashMap<>();
        List<PagoModel> pagos = pagoRepository.findByFechaPagoBetween(inicio, fin);
        for (PagoModel p : pagos) {
            total = total.add(p.getMonto());
            porMetodo.merge(p.getMetodo(), p.getMonto(), BigDecimal::add);
        }

        Map<String, Object> reporte = new LinkedHashMap<>();
        reporte.put("inicio", inicio);
        reporte.put("fin", fin);
        reporte.put("numeroPagos", pagos.size());
        reporte.put("totalIngresos", total);
        reporte.put("ingresosPorMetodo", porMetodo);
        return reporte;
    }

    // Morosidad: reservaciones confirmadas cuya estancia ya inició y aún tienen saldo pendiente
    public Map<String, Object> morosidad() {
        LocalDate hoy = LocalDate.now();
        BigDecimal totalAdeudado = BigDecimal.ZERO;
        List<Map<String, Object>> deudores = new ArrayList<>();

        for (ReservacionModel r : reservacionRepository.findByEstado("CONFIRMADA")) {
            if (r.getFechaInicio().isAfter(hoy)) {
                continue;
            }
            BigDecimal pagado = pagoService.totalPagado(r.getId());
            BigDecimal saldo = r.getTotal().subtract(pagado);
            if (saldo.compareTo(BigDecimal.ZERO) > 0) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("reservacionId", r.getId());
                fila.put("clienteId", r.getCliente().getId());
                fila.put("cliente", r.getCliente().getNombre() + " " + r.getCliente().getApellidos());
                fila.put("correo", r.getCliente().getCorreo());
                fila.put("telefono", r.getCliente().getTelefono());
                fila.put("apartamento", r.getApartamento().getTitulo());
                fila.put("fechaInicio", r.getFechaInicio());
                fila.put("total", r.getTotal());
                fila.put("totalPagado", pagado);
                fila.put("saldo", saldo);
                deudores.add(fila);
                totalAdeudado = totalAdeudado.add(saldo);
            }
        }

        Map<String, Object> reporte = new LinkedHashMap<>();
        reporte.put("fechaConsulta", hoy);
        reporte.put("totalAdeudado", totalAdeudado);
        reporte.put("deudores", deudores);
        return reporte;
    }

    private void validarRango(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null || fin.isBefore(inicio)) {
            throw new IllegalArgumentException(
                    "Indica 'inicio' y 'fin' (yyyy-MM-dd) y que 'fin' no sea anterior a 'inicio'.");
        }
    }

    private double porcentaje(long parte, long total) {
        if (total == 0) {
            return 0;
        }
        return Math.round(parte * 1000.0 / total) / 10.0;
    }
}
