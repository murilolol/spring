package com.curso.restaurante.repository.pedido;

import com.curso.restaurante.domain.pedido.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {
}
