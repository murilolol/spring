package com.curso.restaurante.api.fornecedor.dto;

import com.curso.restaurante.domain.Status;
import com.curso.restaurante.domain.fornecedor.Fornecedor;

public record FornecedorResponse(Long id, String razaoSocial, String cnpj, Status status) {

    public static FornecedorResponse de(Fornecedor fornecedor) {
        return new FornecedorResponse(
                fornecedor.getId(), fornecedor.getRazaoSocial(), fornecedor.getCnpj(), fornecedor.getStatus());
    }
}
