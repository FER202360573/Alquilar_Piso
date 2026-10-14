package com.api.apartamentos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.api.apartamentos.exceptions.RecursoNoEncontradoException;
import com.api.apartamentos.models.ClienteModel;

@Controller
public class ClienteVistaController {

    private final ClienteController clienteController;

    public ClienteVistaController(ClienteController clienteController) {
        this.clienteController = clienteController;
    }

    @GetMapping("/vista/clientes")
    public String listar(Model model) {
        model.addAttribute("clientes", clienteController.listar(null));
        return "clientes";
    }

    @PostMapping("/vista/clientes/guardar")
    public String guardar(@ModelAttribute ClienteModel cliente, RedirectAttributes redirect) {
        try {
            clienteController.crear(cliente);
            redirect.addFlashAttribute("mensaje", "Cliente guardado correctamente.");
        } catch (RecursoNoEncontradoException | IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vista/clientes";
    }

    @PostMapping("/vista/clientes/{id}/actualizar")
    public String actualizar(@PathVariable Long id, @ModelAttribute ClienteModel cliente,
                             RedirectAttributes redirect) {
        try {
            clienteController.actualizar(id, cliente);
            redirect.addFlashAttribute("mensaje", "Cliente actualizado correctamente.");
        } catch (RecursoNoEncontradoException | IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vista/clientes";
    }

    @PostMapping("/vista/clientes/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            clienteController.eliminar(id);
            redirect.addFlashAttribute("mensaje", "Cliente eliminado.");
        } catch (RecursoNoEncontradoException | IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vista/clientes";
    }
}
