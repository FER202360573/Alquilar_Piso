package com.api.apartamentos.models;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "reservaciones")
public class ReservacionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "apartamento_id", nullable = false)
    private ApartamentoModel apartamento;

    // Cliente de tipo ARRENDATARIO
    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private ClienteModel cliente;

    // Día de entrada
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    // Día de salida (esa noche ya no se cobra)
    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    // Se calcula automáticamente: días x precioPorDia
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    // CONFIRMADA o CANCELADA
    @Column(nullable = false, length = 20)
    private String estado;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ApartamentoModel getApartamento() { return apartamento; }
    public void setApartamento(ApartamentoModel apartamento) { this.apartamento = apartamento; }

    public ClienteModel getCliente() { return cliente; }
    public void setCliente(ClienteModel cliente) { this.cliente = cliente; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
