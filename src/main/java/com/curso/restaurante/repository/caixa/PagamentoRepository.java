package com.curso.restaurante.repository.caixa;

import com.curso.restaurante.domain.caixa.FormaPagamento;
import com.curso.restaurante.domain.caixa.Pagamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    List<Pagamento> findByComandaId(Long comandaId);

    List<Pagamento> findBySessaoCaixaId(Long sessaoCaixaId);

    Page<Pagamento> findBySessaoCaixaId(Long sessaoCaixaId, Pageable pageable);

    Page<Pagamento> findBySessaoCaixaIdAndFormaPagamento(Long sessaoCaixaId, FormaPagamento formaPagamento, Pageable pageable);
}
