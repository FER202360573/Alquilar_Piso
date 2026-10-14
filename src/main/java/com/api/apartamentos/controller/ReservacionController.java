package com.api.apartamentos.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.apartamentos.models.ReservacionModel;
import com.api.apartamentos.services.PagoService;
import com.api.apartamentos.services.ReservacionService;

@RestController
@RequestMapping("/reservaciones")
public class ReservacionController {

    private final ReservacionService reservacionService;
    private final PagoService pagoService;

    public ReservacionController(ReservacionService reservacionService, PagoService pagoService) {
        this.reservacionService = reservacionService;
        this.pagoService = pagoService;
    }

    @GetMapping
    public List<ReservacionModel> listar() {
        return reservacionService.listar();
    }

    @GetMapping("/{id}")
    public ReservacionModel obtener(@PathVariable Long id) {
        return reservacionService.obtener(id);
    }

    @GetMapping("/cliente/{clienteId}")
    public List<ReservacionModel> listarPorCliente(@PathVariable Long clienteId) {
        return reservacionService.listarPorCliente(clienteId);
    }

    @GetMapping("/{id}/estado-cuenta")
    public Map<String, Object> estadoCuenta(@PathVariable Long id) {
        return pagoService.estadoCuenta(id);
    }

    @PostMapping
    public ResponseEntity<ReservacionModel> crear(@RequestBody ReservacionModel reservacion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservacionService.crear(reservacion));
    }

    @PutMapping("/{id}/cancelar")
    public ReservacionModel cancelar(@PathVariable Long id) {
        return reservacionService.cancelar(id);
    }
}
