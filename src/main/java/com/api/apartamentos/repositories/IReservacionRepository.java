package com.api.apartamentos.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.api.apartamentos.models.ReservacionModel;

@Repository
public interface IReservacionRepository extends JpaRepository<ReservacionModel, Long> {

    boolean existsByCliente_Id(Long clienteId);

    boolean existsByApartamento_Id(Long apartamentoId);

    List<ReservacionModel> findByEstado(String estado);

    List<ReservacionModel> findByCliente_Id(Long clienteId);

    List<ReservacionModel> findByApartamento_IdAndEstado(Long apartamentoId, String estado);

    // true si ya hay una reservación confirmada que se traslapa con las fechas pedidas
    @Query("SELECT COUNT(r) > 0 FROM ReservacionModel r WHERE r.apartamento.id = :apartamentoId "
            + "AND r.estado = 'CONFIRMADA' AND r.fechaInicio < :fin AND r.fechaFin > :inicio")
    boolean hayTraslape(@Param("apartamentoId") Long apartamentoId,
                        @Param("inicio") LocalDate inicio,
                        @Param("fin") LocalDate fin);
}
