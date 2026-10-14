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
@Table(name = "pagos")
public class PagoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reservacion_id", nullable = false)
    private ReservacionModel reservacion;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDate fechaPago;

    // EFECTIVO, TARJETA o TRANSFERENCIA
    @Column(nullable = false, length = 20)
    private String metodo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ReservacionModel getReservacion() { return reservacion; }
    public void setReservacion(ReservacionModel reservacion) { this.reservacion = reservacion; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public LocalDate getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDate fechaPago) { this.fechaPago = fechaPago; }

    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
}
