package com.api.apartamentos.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.api.apartamentos.models.ApartamentoModel;

@Repository
public interface IApartamentoRepository extends JpaRepository<ApartamentoModel, Long> {

    boolean existsByPropietario_Id(Long propietarioId);

    // Apartamentos activos sin reservaciones confirmadas que se traslapen con el rango
    @Query("SELECT a FROM ApartamentoModel a WHERE a.estado = 'ACTIVO' AND NOT EXISTS ("
            + "SELECT r FROM ReservacionModel r WHERE r.apartamento = a AND r.estado = 'CONFIRMADA' "
            + "AND r.fechaInicio < :fin AND r.fechaFin > :inicio)")
    List<ApartamentoModel> buscarDisponibles(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);
}
