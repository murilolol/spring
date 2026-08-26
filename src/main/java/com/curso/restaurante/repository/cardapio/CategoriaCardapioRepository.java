package com.curso.restaurante.repository.cardapio;

import com.curso.restaurante.domain.cardapio.CategoriaCardapio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CategoriaCardapioRepository
        extends JpaRepository<CategoriaCardapio, Long>, JpaSpecificationExecutor<CategoriaCardapio> {

    boolean existsByNome(String nome);
}
