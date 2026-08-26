package com.curso.restaurante.repository.pedido;

import com.curso.restaurante.domain.pedido.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;

public interface PedidoRepository extends JpaRepository<Pedido, Long>, JpaSpecificationExecutor<Pedido> {

    long countByAbertoEmBetween(LocalDateTime inicio, LocalDateTime fim);
}
