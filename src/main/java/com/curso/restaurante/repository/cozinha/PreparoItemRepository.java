package com.curso.restaurante.repository.cozinha;

import com.curso.restaurante.domain.cozinha.PreparoItem;
import com.curso.restaurante.domain.cozinha.StatusPreparo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PreparoItemRepository extends JpaRepository<PreparoItem, Long>, JpaSpecificationExecutor<PreparoItem> {

    List<PreparoItem> findByItemPedido_Pedido_IdAndStatusNot(Long pedidoId, StatusPreparo status);
}
