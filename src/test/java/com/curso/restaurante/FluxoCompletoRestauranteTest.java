package com.curso.restaurante;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Reproduz, via HTTP real, o fluxo completo de um atendimento de restaurante:
 * abrir caixa, cadastrar cliente e mesa, abrir comanda, lançar pedido com um
 * item de cozinha e um de bar, avançar a fila de preparo, entregar, fechar a
 * comanda, pagar em duas formas diferentes e fechar o caixa com conferência.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.MethodName.class)
class FluxoCompletoRestauranteTest {

    private static final String SENHA_ADMIN_BOOTSTRAP = "admin123";
    private static final String SENHA = "senha-fluxo-2026";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveReproduzirOFluxoCompletoDeUmAtendimento() throws Exception {
        String sufixo = String.valueOf(System.nanoTime());

        criarUsuario("garcom.fluxo." + sufixo, "GARCOM", "Garçom Fluxo");
        criarUsuario("cozinha.fluxo." + sufixo, "COZINHA", "Cozinheiro Fluxo");
        criarUsuario("caixa.fluxo." + sufixo, "CAIXA", "Caixa Fluxo");

        Long sessaoCaixaId = abrirSessaoDeCaixa("caixa.fluxo." + sufixo);

        Long mesaId = criarMesa(9000 + (int) (System.nanoTime() % 1000));
        Long categoriaId = criarCategoriaCardapio("Categoria Fluxo " + sufixo);
        Long itemCozinhaId = criarItemCardapio(categoriaId, "ITEM-FLUXO-COZINHA-" + sufixo, true, "COZINHA");
        Long itemBarId = criarItemCardapio(categoriaId, "ITEM-FLUXO-BAR-" + sufixo, false, "BAR");

        Long comandaId = abrirComanda("garcom.fluxo." + sufixo, mesaId);
        Long pedidoId = abrirPedido("garcom.fluxo." + sufixo, comandaId);

        adicionarItemAoPedido("garcom.fluxo." + sufixo, pedidoId, itemCozinhaId, 1);
        adicionarItemAoPedido("garcom.fluxo." + sufixo, pedidoId, itemBarId, 2);

        String respostaEnvio = mockMvc.perform(post("/api/pedidos/" + pedidoId + "/enviar-para-preparo")
                        .with(httpBasic("garcom.fluxo." + sufixo, SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_PREPARO"))
                .andReturn().getResponse().getContentAsString();
        assertEquals("EM_PREPARO", JsonPath.read(respostaEnvio, "$.status"));

        String respostaFila = mockMvc.perform(get("/api/cozinha/fila")
                        .param("secao", "COZINHA")
                        .param("status", "AGUARDANDO")
                        .with(httpBasic("cozinha.fluxo." + sufixo, SENHA)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Number preparoItemIdBruto = JsonPath.read(respostaFila, "$.conteudo[0].id");
        Long preparoItemId = preparoItemIdBruto.longValue();

        mockMvc.perform(post("/api/cozinha/fila/" + preparoItemId + "/iniciar")
                        .with(httpBasic("cozinha.fluxo." + sufixo, SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_PREPARO"));

        mockMvc.perform(post("/api/cozinha/fila/" + preparoItemId + "/concluir")
                        .with(httpBasic("cozinha.fluxo." + sufixo, SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDO"));

        mockMvc.perform(get("/api/pedidos/" + pedidoId)
                        .with(httpBasic("cozinha.fluxo." + sufixo, SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PRONTO"));

        mockMvc.perform(post("/api/pedidos/" + pedidoId + "/marcar-entregue")
                        .with(httpBasic("garcom.fluxo." + sufixo, SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENTREGUE"));

        mockMvc.perform(post("/api/comandas/" + comandaId + "/fechar")
                        .with(httpBasic("garcom.fluxo." + sufixo, SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FECHADA"));

        mockMvc.perform(post("/api/comandas/" + comandaId + "/pagamentos")
                        .with(httpBasic("caixa.fluxo." + sufixo, SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"formaPagamento":"DINHEIRO","valor":1.00,"valorRecebido":5.00}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/comandas/" + comandaId + "/pagamentos")
                        .with(httpBasic("caixa.fluxo." + sufixo, SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"formaPagamento":"PIX","valor":9999.00,"valorRecebido":null}
                                """))
                .andExpect(status().is4xxClientError());

        String conta = mockMvc.perform(get("/api/comandas/" + comandaId + "/conta")
                        .with(httpBasic("caixa.fluxo." + sufixo, SENHA)))
                .andReturn().getResponse().getContentAsString();
        double total = ((Number) JsonPath.read(conta, "$.total")).doubleValue();
        double restante = total - 1.00;

        mockMvc.perform(post("/api/comandas/" + comandaId + "/pagamentos")
                        .with(httpBasic("caixa.fluxo." + sufixo, SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"formaPagamento":"PIX","valor":%s,"valorRecebido":null}
                                """.formatted(restante)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/comandas/" + comandaId)
                        .with(httpBasic("caixa.fluxo." + sufixo, SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAGA"));

        mockMvc.perform(get("/api/mesas/" + mesaId)
                        .with(httpBasic("caixa.fluxo." + sufixo, SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LIVRE"));

        mockMvc.perform(post("/api/caixa/sessoes/" + sessaoCaixaId + "/sangrias")
                        .with(httpBasic("caixa.fluxo." + sufixo, SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"valor":1.00,"motivo":"Reforço de troco no bar"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/caixa/sessoes/" + sessaoCaixaId + "/fechar")
                        .with(httpBasic("caixa.fluxo." + sufixo, SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"valorContado":%s,"observacao":"Fechamento do fluxo de teste"}
                                """.formatted(total - 1.00)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FECHADA"));
    }

    private void criarUsuario(String username, String perfil, String nome) throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"%s","username":"%s","senha":"%s","perfil":"%s"}
                                """.formatted(nome, username, SENHA, perfil)))
                .andExpect(status().isCreated());
    }

    private Long abrirSessaoDeCaixa(String usernameCaixa) throws Exception {
        String resposta = mockMvc.perform(post("/api/caixa/sessoes")
                        .with(httpBasic(usernameCaixa, SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"valorAbertura":100.00}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return idDe(resposta);
    }

    private Long criarMesa(int numero) throws Exception {
        String resposta = mockMvc.perform(post("/api/mesas")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numero":%d,"capacidade":4,"setor":"Salão Fluxo"}
                                """.formatted(numero)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return idDe(resposta);
    }

    private Long criarCategoriaCardapio(String nome) throws Exception {
        String resposta = mockMvc.perform(post("/api/categorias-cardapio")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"%s","descricao":null,"ordemExibicao":1}
                                """.formatted(nome)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return idDe(resposta);
    }

    private Long criarItemCardapio(Long categoriaId, String codigo, boolean exigePreparo, String secao) throws Exception {
        String resposta = mockMvc.perform(post("/api/itens-cardapio")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoriaId":%d,"codigo":"%s","nome":"Item %s","descricao":null,
                                "precoVenda":9.90,"tempoPreparoMinutos":5,"secaoPreparo":"%s","exigePreparo":%s,
                                "controlaEstoque":false,"saldoEstoque":0,"dataCadastro":"2026-08-20",
                                "estoqueMinimo":0}
                                """.formatted(categoriaId, codigo, codigo, secao, exigePreparo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return idDe(resposta);
    }

    private Long abrirComanda(String usernameGarcom, Long mesaId) throws Exception {
        String resposta = mockMvc.perform(post("/api/comandas")
                        .with(httpBasic(usernameGarcom, SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipoAtendimento":"SALAO","mesaId":%d,"clienteId":null,"numeroPessoas":2,
                                "percentualTaxaServico":0,"observacao":null}
                                """.formatted(mesaId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return idDe(resposta);
    }

    private Long abrirPedido(String usernameGarcom, Long comandaId) throws Exception {
        String resposta = mockMvc.perform(post("/api/comandas/" + comandaId + "/pedidos")
                        .with(httpBasic(usernameGarcom, SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"observacao":"Fluxo de teste completo"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return idDe(resposta);
    }

    private void adicionarItemAoPedido(String usernameGarcom, Long pedidoId, Long itemCardapioId, int quantidade)
            throws Exception {
        mockMvc.perform(post("/api/pedidos/" + pedidoId + "/itens")
                        .with(httpBasic(usernameGarcom, SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"itemCardapioId":%d,"quantidade":%d,"observacao":null}
                                """.formatted(itemCardapioId, quantidade)))
                .andExpect(status().isCreated());
    }

    private Long idDe(String jsonDaResposta) {
        Number id = JsonPath.read(jsonDaResposta, "$.id");
        return id.longValue();
    }
}
