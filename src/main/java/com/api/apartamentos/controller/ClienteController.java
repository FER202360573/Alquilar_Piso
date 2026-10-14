package com.api.apartamentos.controller;

import java.util.List;

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

import com.api.apartamentos.models.ClienteModel;
import com.api.apartamentos.services.ClienteService;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // Ejemplos: /clientes  |  /clientes?tipo=PROPIETARIO  |  /clientes?tipo=ARRENDATARIO
    @GetMapping
    public List<ClienteModel> listar(@RequestParam(required = false) String tipo) {
        return clienteService.listar(tipo);
    }

    @GetMapping("/{id}")
    public ClienteModel obtener(@PathVariable Long id) {
        return clienteService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<ClienteModel> crear(@RequestBody ClienteModel cliente) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crear(cliente));
    }

    @PutMapping("/{id}")
    public ClienteModel actualizar(@PathVariable Long id, @RequestBody ClienteModel cliente) {
        return clienteService.actualizar(id, cliente);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
