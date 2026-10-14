package com.api.apartamentos.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReporteVistaController {

    private final ReporteController reporteController;

    public ReporteVistaController(ReporteController reporteController) {
        this.reporteController = reporteController;
    }

    // Por defecto muestra el mes actual
    @GetMapping("/vista/reportes")
    public String reportes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            Model model) {
        LocalDate hoy = LocalDate.now();
        if (inicio == null) {
            inicio = hoy.withDayOfMonth(1);
        }
        if (fin == null) {
            fin = inicio.withDayOfMonth(inicio.lengthOfMonth());
        }
        model.addAttribute("inicio", inicio);
        model.addAttribute("fin", fin);

        try {
            model.addAttribute("ocupacion", reporteController.ocupacion(inicio, fin));
            model.addAttribute("ingresos", reporteController.ingresos(inicio, fin));
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("morosidad", reporteController.morosidad());
        return "reportes";
    }
}
