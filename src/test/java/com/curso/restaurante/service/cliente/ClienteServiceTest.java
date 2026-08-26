package com.curso.restaurante.service.cliente;

import com.curso.restaurante.domain.Status;
import com.curso.restaurante.domain.cliente.Cliente;
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
class ClienteServiceTest {

    @Autowired
    private ClienteService clienteService;

    @Test
    void deveCriarCliente() {
        Cliente cliente = clienteService.criar(
                "Ana Cliente Svc", "22233344455", "(45) 91111-0001", "ana.svc@example.com", null, null);

        assertEquals("Ana Cliente Svc", cliente.getNome());
        assertEquals(Status.ATIVO, cliente.getStatus());
    }

    @Test
    void deveRejeitarDocumentoDuplicado() {
        clienteService.criar("Primeiro Svc", "33344455566", "(45) 91111-0002", null, null, null);

        assertThrows(
                ConflitoDeEstadoException.class,
                () -> clienteService.criar("Segundo Svc", "33344455566", "(45) 91111-0003", null, null, null));
    }

    @Test
    void devePermitirDoisClientesSemDocumento() {
        clienteService.criar("Sem Doc 1 Svc", null, "(45) 91111-0004", null, null, null);
        clienteService.criar("Sem Doc 2 Svc", null, "(45) 91111-0005", null, null, null);
    }

    @Test
    void buscarPorIdDeveLancarQuandoNaoExiste() {
        assertThrows(RecursoNaoEncontradoException.class, () -> clienteService.buscarPorId(-1L));
    }

    @Test
    void deveListarComFiltroDeNome() {
        clienteService.criar("Fulano Filtro Svc", null, "(45) 91111-0006", null, null, null);
        clienteService.criar("Beltrano Filtro Svc", null, "(45) 91111-0007", null, null, null);

        var pagina = clienteService.listar("Fulano Filtro", null, null, PageRequest.of(0, 50));

        assertTrue(pagina.getContent().stream().anyMatch(c -> c.getNome().equals("Fulano Filtro Svc")));
        assertTrue(pagina.getContent().stream().noneMatch(c -> c.getNome().equals("Beltrano Filtro Svc")));
    }

    @Test
    void deveAtualizarDados() {
        Cliente criado = clienteService.criar("Nome Antigo Svc", null, "(45) 91111-0008", null, null, null);

        Cliente atualizado = clienteService.atualizar(
                criado.getId(), "Nome Novo Svc", "(45) 92222-0008", "novo@example.com", "Rua Nova, 1");

        assertEquals("Nome Novo Svc", atualizado.getNome());
        assertEquals("(45) 92222-0008", atualizado.getTelefone());
    }

    @Test
    void deveInativarEAtivar() {
        Cliente criado = clienteService.criar("Toggle Svc", null, "(45) 91111-0009", null, null, null);

        Cliente inativado = clienteService.inativar(criado.getId());
        assertEquals(Status.INATIVO, inativado.getStatus());

        Cliente ativado = clienteService.ativar(criado.getId());
        assertEquals(Status.ATIVO, ativado.getStatus());
    }
}
