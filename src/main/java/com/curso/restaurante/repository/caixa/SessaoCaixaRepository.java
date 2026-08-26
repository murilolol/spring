package com.curso.restaurante.repository.caixa;

import com.curso.restaurante.domain.caixa.SessaoCaixa;
import com.curso.restaurante.domain.caixa.StatusSessaoCaixa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SessaoCaixaRepository extends JpaRepository<SessaoCaixa, Long>, JpaSpecificationExecutor<SessaoCaixa> {

    Optional<SessaoCaixa> findByStatus(StatusSessaoCaixa status);
}
