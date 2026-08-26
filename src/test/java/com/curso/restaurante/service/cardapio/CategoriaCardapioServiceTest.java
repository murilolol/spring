package com.curso.restaurante.service.cardapio;

import com.curso.restaurante.domain.Status;
import com.curso.restaurante.domain.cardapio.CategoriaCardapio;
import com.curso.restaurante.domain.comum.ConflitoDeEstadoException;
import com.curso.restaurante.domain.comum.RecursoNaoEncontradoException;
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
class CategoriaCardapioServiceTest {

    @Autowired
    private CategoriaCardapioService categoriaCardapioService;

    @Test
    void deveCriarCategoria() {
        CategoriaCardapio categoria = categoriaCardapioService.criar("Entradas Svc", "descrição", 1);

        assertEquals("Entradas Svc", categoria.getNome());
        assertEquals(Status.ATIVO, categoria.getStatus());
    }

    @Test
    void deveRejeitarNomeDuplicado() {
        categoriaCardapioService.criar("Bebidas Svc", null, 1);

        assertThrows(
                ConflitoDeEstadoException.class,
                () -> categoriaCardapioService.criar("Bebidas Svc", null, 2));
    }

    @Test
    void buscarPorIdDeveLancarQuandoNaoExiste() {
        assertThrows(RecursoNaoEncontradoException.class, () -> categoriaCardapioService.buscarPorId(-1L));
    }

    @Test
    void deveListarComFiltroDeStatus() {
        CategoriaCardapio ativa = categoriaCardapioService.criar("Sobremesas Svc", null, 1);
        CategoriaCardapio inativa = categoriaCardapioService.criar("Descontinuada Svc", null, 2);
        categoriaCardapioService.inativar(inativa.getId());

        var pagina = categoriaCardapioService.listar(Status.ATIVO, PageRequest.of(0, 50));

        assertTrue(pagina.getContent().stream().anyMatch(c -> c.getId().equals(ativa.getId())));
        assertTrue(pagina.getContent().stream().noneMatch(c -> c.getId().equals(inativa.getId())));
    }

    @Test
    void deveAtualizarNomeDescricaoEOrdem() {
        CategoriaCardapio criada = categoriaCardapioService.criar("Nome Antigo Svc", "descrição antiga", 1);

        CategoriaCardapio atualizada = categoriaCardapioService.atualizar(
                criada.getId(), "Nome Novo Svc", "descrição nova", 9);

        assertEquals("Nome Novo Svc", atualizada.getNome());
        assertEquals("descrição nova", atualizada.getDescricao());
        assertEquals(9, atualizada.getOrdemExibicao());
    }

    @Test
    void deveInativarEAtivar() {
        CategoriaCardapio criada = categoriaCardapioService.criar("Toggle Svc", null, 1);

        CategoriaCardapio inativada = categoriaCardapioService.inativar(criada.getId());
        assertEquals(Status.INATIVO, inativada.getStatus());

        CategoriaCardapio ativada = categoriaCardapioService.ativar(criada.getId());
        assertEquals(Status.ATIVO, ativada.getStatus());
    }
}
