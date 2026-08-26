package com.curso.restaurante.repository.mesa;

import com.curso.restaurante.domain.mesa.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MesaRepository extends JpaRepository<Mesa, Long>, JpaSpecificationExecutor<Mesa> {

    boolean existsByNumero(int numero);
}
