package com.api.apartamentos.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.apartamentos.models.ClienteModel;

@Repository
public interface IClienteRepository extends JpaRepository<ClienteModel, Long> {

    boolean existsByCorreo(String correo);

    List<ClienteModel> findByTipo(String tipo);
}
