package com.curso.restaurante.repository.comanda;

import com.curso.restaurante.domain.comanda.Comanda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;

public interface ComandaRepository extends JpaRepository<Comanda, Long>, JpaSpecificationExecutor<Comanda> {

    long countByAbertaEmBetween(LocalDateTime inicio, LocalDateTime fim);

    Page<Comanda> findByClienteId(Long clienteId, Pageable pageable);
}
