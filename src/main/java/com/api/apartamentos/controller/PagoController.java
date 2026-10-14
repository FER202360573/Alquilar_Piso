package com.api.apartamentos.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.apartamentos.models.PagoModel;
import com.api.apartamentos.services.PagoService;

@RestController
@RequestMapping("/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @GetMapping
    public List<PagoModel> listar() {
        return pagoService.listar();
    }

    @GetMapping("/{id}")
    public PagoModel obtener(@PathVariable Long id) {
        return pagoService.obtener(id);
    }

    @GetMapping("/reservacion/{reservacionId}")
    public List<PagoModel> listarPorReservacion(@PathVariable Long reservacionId) {
        return pagoService.listarPorReservacion(reservacionId);
    }

    @PostMapping
    public ResponseEntity<PagoModel> crear(@RequestBody PagoModel pago) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.crear(pago));
    }
}
