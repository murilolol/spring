package com.curso.restaurante.repository.cardapio;

import com.curso.restaurante.domain.cardapio.ItemCardapio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ItemCardapioRepository
        extends JpaRepository<ItemCardapio, Long>, JpaSpecificationExecutor<ItemCardapio> {

    boolean existsByCodigo(String codigo);
}
