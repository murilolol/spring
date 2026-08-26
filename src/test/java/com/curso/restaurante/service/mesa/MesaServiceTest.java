package com.curso.restaurante.service.mesa;

import com.curso.restaurante.domain.comum.ConflitoDeEstadoException;
import com.curso.restaurante.domain.comum.RecursoNaoEncontradoException;
import com.curso.restaurante.domain.mesa.Mesa;
import com.curso.restaurante.domain.mesa.StatusMesa;
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
class MesaServiceTest {

    @Autowired
    private MesaService mesaService;

    @Test
    void deveCriarMesa() {
        Mesa mesa = mesaService.criar(501, 4, "Salão Svc");

        assertEquals(501, mesa.getNumero());
        assertEquals(StatusMesa.LIVRE, mesa.getStatus());
    }

    @Test
    void deveRejeitarNumeroDuplicado() {
        mesaService.criar(502, 4, "Salão Svc");

        assertThrows(ConflitoDeEstadoException.class, () -> mesaService.criar(502, 6, "Outro Salão Svc"));
    }

    @Test
    void buscarPorIdDeveLancarQuandoNaoExiste() {
        assertThrows(RecursoNaoEncontradoException.class, () -> mesaService.buscarPorId(-1L));
    }

    @Test
    void deveListarComFiltroDeStatus() {
        mesaService.criar(503, 4, "Salão Svc Filtro");
        Mesa reservada = mesaService.criar(504, 4, "Salão Svc Filtro");
        mesaService.reservar(reservada.getId());

        var pagina = mesaService.listar(StatusMesa.RESERVADA, null, PageRequest.of(0, 50));

        assertTrue(pagina.getContent().stream().allMatch(m -> m.getStatus() == StatusMesa.RESERVADA));
    }

    @Test
    void deveAtualizarCapacidadeESetor() {
        Mesa criada = mesaService.criar(505, 4, "Salão Svc");

        Mesa atualizada = mesaService.atualizar(criada.getId(), 8, "Varanda Svc");

        assertEquals(8, atualizada.getCapacidade());
        assertEquals("Varanda Svc", atualizada.getSetor());
    }

    @Test
    void deveReservarECancelarReserva() {
        Mesa criada = mesaService.criar(506, 4, "Salão Svc");

        Mesa reservada = mesaService.reservar(criada.getId());
        assertEquals(StatusMesa.RESERVADA, reservada.getStatus());

        Mesa liberada = mesaService.cancelarReserva(criada.getId());
        assertEquals(StatusMesa.LIVRE, liberada.getStatus());
    }

    @Test
    void deveInterditarELiberarInterdicao() {
        Mesa criada = mesaService.criar(507, 4, "Salão Svc");

        Mesa interditada = mesaService.interditar(criada.getId());
        assertEquals(StatusMesa.INTERDITADA, interditada.getStatus());

        Mesa liberada = mesaService.liberarInterdicao(criada.getId());
        assertEquals(StatusMesa.LIVRE, liberada.getStatus());
    }
}
