package com.api.apartamentos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.api.apartamentos.exceptions.RecursoNoEncontradoException;
import com.api.apartamentos.models.ApartamentoModel;

@Controller
public class ApartamentoVistaController {

    private final ApartamentoController apartamentoController;
    private final ClienteController clienteController;

    public ApartamentoVistaController(ApartamentoController apartamentoController,
                                      ClienteController clienteController) {
        this.apartamentoController = apartamentoController;
        this.clienteController = clienteController;
    }

    // La raíz abre la lista de apartamentos
    @GetMapping("/")
    public String inicio() {
        return "redirect:/vista/apartamentos";
    }

    @GetMapping("/vista/apartamentos")
    public String listar(Model model) {
        model.addAttribute("apartamentos", apartamentoController.listar());
        model.addAttribute("propietarios", clienteController.listar("PROPIETARIO"));
        return "apartamentos";
    }

    @PostMapping("/vista/apartamentos/guardar")
    public String guardar(@ModelAttribute ApartamentoModel apartamento, RedirectAttributes redirect) {
        try {
            apartamentoController.crear(apartamento);
            redirect.addFlashAttribute("mensaje", "Apartamento guardado correctamente.");
        } catch (RecursoNoEncontradoException | IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vista/apartamentos";
    }

    @PostMapping("/vista/apartamentos/{id}/actualizar")
    public String actualizar(@PathVariable Long id, @ModelAttribute ApartamentoModel apartamento,
                             RedirectAttributes redirect) {
        try {
            apartamentoController.actualizar(id, apartamento);
            redirect.addFlashAttribute("mensaje", "Apartamento actualizado correctamente.");
        } catch (RecursoNoEncontradoException | IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vista/apartamentos";
    }

    @PostMapping("/vista/apartamentos/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            apartamentoController.eliminar(id);
            redirect.addFlashAttribute("mensaje", "Apartamento eliminado.");
        } catch (RecursoNoEncontradoException | IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vista/apartamentos";
    }
}
