package com.api.apartamentos.models;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "apartamentos")
public class ApartamentoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // HABITACION, APARTAMENTO o PISO (piso completo)
    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String direccion;

    @Column(length = 500)
    private String descripcion;

    private Integer habitaciones;

    private Integer banos;

    @Column(name = "metros_cuadrados")
    private Integer metrosCuadrados;

    @Column(name = "precio_por_dia", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioPorDia;

    // ACTIVO o INACTIVO
    @Column(nullable = false, length = 20)
    private String estado;

    @ManyToOne(optional = false)
    @JoinColumn(name = "propietario_id", nullable = false)
    private ClienteModel propietario;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getHabitaciones() { return habitaciones; }
    public void setHabitaciones(Integer habitaciones) { this.habitaciones = habitaciones; }

    public Integer getBanos() { return banos; }
    public void setBanos(Integer banos) { this.banos = banos; }

    public Integer getMetrosCuadrados() { return metrosCuadrados; }
    public void setMetrosCuadrados(Integer metrosCuadrados) { this.metrosCuadrados = metrosCuadrados; }

    public BigDecimal getPrecioPorDia() { return precioPorDia; }
    public void setPrecioPorDia(BigDecimal precioPorDia) { this.precioPorDia = precioPorDia; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public ClienteModel getPropietario() { return propietario; }
    public void setPropietario(ClienteModel propietario) { this.propietario = propietario; }
}
