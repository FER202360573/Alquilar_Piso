package com.api.apartamentos.controller;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.apartamentos.services.ReporteService;

@RestController
@RequestMapping("/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/ocupacion")
public Map<String, Object> ocupacion(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
    
    // Asignar fechas por defecto si no las envían (ej. el mes actual)
    if (inicio == null) inicio = LocalDate.now().withDayOfMonth(1);
    if (fin == null) fin = LocalDate.now();

    return reporteService.ocupacion(inicio, fin);
}

    @GetMapping("/ingresos")
    public Map<String, Object> ingresos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return reporteService.ingresos(inicio, fin);
    }

    @GetMapping("/morosidad")
    public Map<String, Object> morosidad() {
        return reporteService.morosidad();
    }
}
