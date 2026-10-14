package com.api.apartamentos.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.api.apartamentos.exceptions.RecursoNoEncontradoException;
import com.api.apartamentos.models.ApartamentoModel;
import com.api.apartamentos.models.ClienteModel;
import com.api.apartamentos.models.ReservacionModel;

@Controller
public class ReservacionVistaController {

    private final ReservacionController reservacionController;
    private final ApartamentoController apartamentoController;
    private final ClienteController clienteController;

    public ReservacionVistaController(ReservacionController reservacionController,
                                      ApartamentoController apartamentoController,
                                      ClienteController clienteController) {
        this.reservacionController = reservacionController;
        this.apartamentoController = apartamentoController;
        this.clienteController = clienteController;
    }

    @GetMapping("/vista/reservaciones")
    public String listar(Model model) {
        List<ReservacionModel> reservaciones = reservacionController.listar();

        // Estado de cuenta (total, pagado, saldo) de cada reservación, por id
        Map<Long, Map<String, Object>> cuentas = new HashMap<>();
        for (ReservacionModel r : reservaciones) {
            cuentas.put(r.getId(), reservacionController.estadoCuenta(r.getId()));
        }

        model.addAttribute("reservaciones", reservaciones);
        model.addAttribute("cuentas", cuentas);
        model.addAttribute("apartamentos", apartamentoController.listar());
        model.addAttribute("arrendatarios", clienteController.listar("ARRENDATARIO"));
        return "reservaciones";
    }

    @PostMapping("/vista/reservaciones/guardar")
    public String guardar(@RequestParam Long apartamentoId,
                          @RequestParam Long clienteId,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                          RedirectAttributes redirect) {
        ApartamentoModel apartamento = new ApartamentoModel();
        apartamento.setId(apartamentoId);
        ClienteModel cliente = new ClienteModel();
        cliente.setId(clienteId);

        ReservacionModel reservacion = new ReservacionModel();
        reservacion.setApartamento(apartamento);
        reservacion.setCliente(cliente);
        reservacion.setFechaInicio(fechaInicio);
        reservacion.setFechaFin(fechaFin);
        try {
            reservacionController.crear(reservacion);
            redirect.addFlashAttribute("mensaje", "Reservación creada correctamente.");
        } catch (RecursoNoEncontradoException | IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vista/reservaciones";
    }

    @PostMapping("/vista/reservaciones/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            reservacionController.cancelar(id);
            redirect.addFlashAttribute("mensaje", "Reservación cancelada.");
        } catch (RecursoNoEncontradoException | IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vista/reservaciones";
    }
}
