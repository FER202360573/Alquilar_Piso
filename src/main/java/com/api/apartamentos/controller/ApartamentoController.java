package com.api.apartamentos.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.apartamentos.models.ApartamentoModel;
import com.api.apartamentos.services.ApartamentoService;

@RestController
@RequestMapping("/apartamentos")
public class ApartamentoController {

    private final ApartamentoService apartamentoService;

    public ApartamentoController(ApartamentoService apartamentoService) {
        this.apartamentoService = apartamentoService;
    }

    @GetMapping
    public List<ApartamentoModel> listar() {
        return apartamentoService.listar();
    }

    // Ejemplo: /apartamentos/disponibles?inicio=2026-12-20&fin=2026-12-23
    @GetMapping("/disponibles")
    public List<ApartamentoModel> disponibles(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return apartamentoService.disponibles(inicio, fin);
    }

    @GetMapping("/{id}")
    public ApartamentoModel obtener(@PathVariable Long id) {
        return apartamentoService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<ApartamentoModel> crear(@RequestBody ApartamentoModel apartamento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(apartamentoService.crear(apartamento));
    }

    @PutMapping("/{id}")
    public ApartamentoModel actualizar(@PathVariable Long id, @RequestBody ApartamentoModel apartamento) {
        return apartamentoService.actualizar(id, apartamento);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        apartamentoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
