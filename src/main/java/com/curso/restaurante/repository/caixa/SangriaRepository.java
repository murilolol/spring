package com.curso.restaurante.repository.caixa;

import com.curso.restaurante.domain.caixa.Sangria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SangriaRepository extends JpaRepository<Sangria, Long> {

    List<Sangria> findBySessaoCaixaId(Long sessaoCaixaId);

    Page<Sangria> findBySessaoCaixaId(Long sessaoCaixaId, Pageable pageable);
}
