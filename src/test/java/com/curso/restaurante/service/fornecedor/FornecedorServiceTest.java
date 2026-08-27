package com.curso.restaurante.service.fornecedor;

import com.curso.restaurante.domain.Status;
import com.curso.restaurante.domain.comum.ConflitoDeEstadoException;
import com.curso.restaurante.domain.comum.RecursoNaoEncontradoException;
import com.curso.restaurante.domain.fornecedor.Fornecedor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FornecedorServiceTest {

    @Autowired
    private FornecedorService fornecedorService;

    @Test
    void deveCriarFornecedor() {
        Fornecedor fornecedor = fornecedorService.cadastrar("Distribuidora Bom Sabor Ltda", "11122233344455");

        assertEquals("Distribuidora Bom Sabor Ltda", fornecedor.getRazaoSocial());
        assertEquals(Status.ATIVO, fornecedor.getStatus());
    }

    @Test
    void deveRejeitarCnpjDuplicado() {
        fornecedorService.cadastrar("Primeiro Fornecedor Svc", "22233344455566");

        assertThrows(
                ConflitoDeEstadoException.class,
                () -> fornecedorService.cadastrar("Segundo Fornecedor Svc", "22233344455566"));
    }

    @Test
    void buscarPorIdDeveLancarQuandoNaoExiste() {
        assertThrows(RecursoNaoEncontradoException.class, () -> fornecedorService.buscarPorId(-1L));
    }

    @Test
    void deveListarComFiltroDeStatus() {
        fornecedorService.cadastrar("Listagem Fornecedor Svc", "33344455566677");

        var pagina = fornecedorService.listar(Status.ATIVO, PageRequest.of(0, 50));

        assertTrue(pagina.getContent().stream().anyMatch(f -> f.getRazaoSocial().equals("Listagem Fornecedor Svc")));
    }

    @Test
    void deveInativarEAtivar() {
        Fornecedor criado = fornecedorService.cadastrar("Toggle Fornecedor Svc", "44455566677788");

        Fornecedor inativado = fornecedorService.inativar(criado.getId());
        assertEquals(Status.INATIVO, inativado.getStatus());

        Fornecedor ativado = fornecedorService.ativar(criado.getId());
        assertEquals(Status.ATIVO, ativado.getStatus());
    }
}
