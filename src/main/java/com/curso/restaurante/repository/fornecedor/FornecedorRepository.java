package com.curso.restaurante.repository.fornecedor;

import com.curso.restaurante.domain.fornecedor.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long>, JpaSpecificationExecutor<Fornecedor> {

    boolean existsByCnpj(String cnpj);
}
