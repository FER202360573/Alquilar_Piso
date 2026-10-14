package com.api.apartamentos.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.apartamentos.models.PagoModel;

@Repository
public interface IPagoRepository extends JpaRepository<PagoModel, Long> {

    List<PagoModel> findByReservacion_Id(Long reservacionId);

    List<PagoModel> findByFechaPagoBetween(LocalDate desde, LocalDate hasta);
}
