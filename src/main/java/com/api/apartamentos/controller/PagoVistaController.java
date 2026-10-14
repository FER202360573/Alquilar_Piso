package com.api.apartamentos.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.api.apartamentos.exceptions.RecursoNoEncontradoException;
import com.api.apartamentos.models.PagoModel;
import com.api.apartamentos.models.ReservacionModel;

@Controller
public class PagoVistaController {

    private final PagoController pagoController;
    private final ReservacionController reservacionController;

    public PagoVistaController(PagoController pagoController, ReservacionController reservacionController) {
        this.pagoController = pagoController;
        this.reservacionController = reservacionController;
    }

    @GetMapping("/vista/pagos")
    public String listar(Model model) {
        // Solo las reservaciones confirmadas pueden recibir pagos
        List<ReservacionModel> confirmadas = reservacionController.listar().stream()
                .filter(r -> "CONFIRMADA".equals(r.getEstado()))
                .toList();

        Map<Long, Map<String, Object>> cuentas = new HashMap<>();
        for (ReservacionModel r : confirmadas) {
            cuentas.put(r.getId(), reservacionController.estadoCuenta(r.getId()));
        }

        model.addAttribute("pagos", pagoController.listar());
        model.addAttribute("reservaciones", confirmadas);
        model.addAttribute("cuentas", cuentas);
        return "pagos";
    }

    @PostMapping("/vista/pagos/guardar")
    public String guardar(@RequestParam Long reservacionId,
                          @RequestParam BigDecimal monto,
                          @RequestParam String metodo,
                          RedirectAttributes redirect) {
        ReservacionModel reservacion = new ReservacionModel();
        reservacion.setId(reservacionId);

        PagoModel pago = new PagoModel();
        pago.setReservacion(reservacion);
        pago.setMonto(monto);
        pago.setMetodo(metodo);
        try {
            pagoController.crear(pago);
            redirect.addFlashAttribute("mensaje", "Pago registrado correctamente.");
        } catch (RecursoNoEncontradoException | IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vista/pagos";
    }
}
