package com.curso.restaurante.api.cozinha;

import com.curso.restaurante.domain.cardapio.CategoriaCardapio;
import com.curso.restaurante.domain.cardapio.ItemCardapio;
import com.curso.restaurante.domain.cardapio.SecaoPreparo;
import com.curso.restaurante.domain.comanda.Comanda;
import com.curso.restaurante.domain.comanda.TipoAtendimento;
import com.curso.restaurante.domain.cozinha.PreparoItem;
import com.curso.restaurante.domain.pedido.Pedido;
import com.curso.restaurante.domain.usuario.PerfilUsuario;
import com.curso.restaurante.repository.cardapio.CategoriaCardapioRepository;
import com.curso.restaurante.repository.cardapio.ItemCardapioRepository;
import com.curso.restaurante.service.comanda.ComandaService;
import com.curso.restaurante.service.cozinha.FilaCozinhaService;
import com.curso.restaurante.service.mesa.MesaService;
import com.curso.restaurante.service.pedido.PedidoService;
import com.curso.restaurante.service.usuario.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FilaCozinhaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ComandaService comandaService;

    @Autowired
    private MesaService mesaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private FilaCozinhaService filaCozinhaService;

    @Autowired
    private CategoriaCardapioRepository categoriaCardapioRepository;

    @Autowired
    private ItemCardapioRepository itemCardapioRepository;

    @Test
    void cozinhaDeveListarEBuscarFila() throws Exception {
        usuarioService.criar("Cozinha Lista Fila Ctrl", "cozinha.lista.fila.ctrl", "senha", PerfilUsuario.COZINHA);
        PreparoItem preparoItem = umPreparoItemEnfileirado();

        mockMvc.perform(get("/api/cozinha/fila").with(httpBasic("cozinha.lista.fila.ctrl", "senha")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/cozinha/fila/" + preparoItem.getId())
                        .with(httpBasic("cozinha.lista.fila.ctrl", "senha")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AGUARDANDO"));
    }

    @Test
    void garcomNaoDeveAcessarFilaDaCozinha() throws Exception {
        usuarioService.criar("Garçom Fila Ctrl", "garcom.fila.ctrl", "senha", PerfilUsuario.GARCOM);

        mockMvc.perform(get("/api/cozinha/fila").with(httpBasic("garcom.fila.ctrl", "senha")))
                .andExpect(status().isForbidden());
    }

    @Test
    void cozinhaDeveIniciarEConcluirItemDePreparo() throws Exception {
        usuarioService.criar("Cozinha Fluxo Fila Ctrl", "cozinha.fluxo.fila.ctrl", "senha", PerfilUsuario.COZINHA);
        PreparoItem preparoItem = umPreparoItemEnfileirado();

        mockMvc.perform(post("/api/cozinha/fila/" + preparoItem.getId() + "/iniciar")
                        .with(httpBasic("cozinha.fluxo.fila.ctrl", "senha")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_PREPARO"));

        mockMvc.perform(post("/api/cozinha/fila/" + preparoItem.getId() + "/concluir")
                        .with(httpBasic("cozinha.fluxo.fila.ctrl", "senha")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDO"));
    }

    @Test
    void cozinhaDeveCancelarItemDePreparo() throws Exception {
        usuarioService.criar("Cozinha Cancela Fila Ctrl", "cozinha.cancela.fila.ctrl", "senha", PerfilUsuario.COZINHA);
        PreparoItem preparoItem = umPreparoItemEnfileirado();

        mockMvc.perform(post("/api/cozinha/fila/" + preparoItem.getId() + "/cancelar")
                        .with(httpBasic("cozinha.cancela.fila.ctrl", "senha")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    @Test
    void cozinhaDeveAlterarPrioridade() throws Exception {
        usuarioService.criar("Cozinha Prioridade Fila Ctrl", "cozinha.prioridade.fila.ctrl", "senha", PerfilUsuario.COZINHA);
        PreparoItem preparoItem = umPreparoItemEnfileirado();

        mockMvc.perform(post("/api/cozinha/fila/" + preparoItem.getId() + "/alterar-prioridade")
                        .with(httpBasic("cozinha.prioridade.fila.ctrl", "senha"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"prioridade":"URGENTE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prioridade").value("URGENTE"));
    }

    private PreparoItem umPreparoItemEnfileirado() {
        var responsavel = usuarioService.criar(
                "Garçom Fila Ctrl " + Math.random(), "garcom.fila.ctrl." + Math.random(), "senha", PerfilUsuario.GARCOM);
        var mesa = mesaService.criar((int) (Math.random() * 1000000), 4, "Salão Fila Ctrl");
        Comanda comanda = comandaService.abrir(
                TipoAtendimento.SALAO, mesa.getId(), null, responsavel.getUsername(), 2, BigDecimal.ZERO, null);
        Pedido pedido = pedidoService.criarPedido(comanda.getId(), responsavel.getUsername(), null);

        CategoriaCardapio categoria = categoriaCardapioRepository.save(
                new CategoriaCardapio("Categoria Fila Ctrl " + Math.random(), null, 1));
        ItemCardapio item = new ItemCardapio(
                "ITEM-FILA-CTRL-" + Math.random(), "Item Fila Ctrl", null, new BigDecimal("9.00"), 5,
                SecaoPreparo.COZINHA, true, false, BigDecimal.ZERO, LocalDate.of(2026, 8, 20));
        categoria.adicionarItem(item);
        item = itemCardapioRepository.save(item);

        pedidoService.adicionarItem(pedido.getId(), item.getId(), 1, null);
        Pedido enviado = pedidoService.enviarParaPreparo(pedido.getId());
        List<PreparoItem> fila = filaCozinhaService.enfileirarItensDoPedido(enviado);
        return fila.getFirst();
    }
}
