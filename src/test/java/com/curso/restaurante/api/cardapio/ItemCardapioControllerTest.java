package com.curso.restaurante.api.cardapio;

import com.curso.restaurante.domain.cardapio.CategoriaCardapio;
import com.curso.restaurante.domain.cardapio.ItemCardapio;
import com.curso.restaurante.domain.cardapio.SecaoPreparo;
import com.curso.restaurante.domain.fornecedor.Fornecedor;
import com.curso.restaurante.domain.usuario.PerfilUsuario;
import com.curso.restaurante.service.cardapio.CategoriaCardapioService;
import com.curso.restaurante.service.cardapio.ItemCardapioService;
import com.curso.restaurante.service.fornecedor.FornecedorService;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ItemCardapioControllerTest {

    private static final String SENHA_ADMIN_BOOTSTRAP = "admin123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoriaCardapioService categoriaCardapioService;

    @Autowired
    private ItemCardapioService itemCardapioService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private FornecedorService fornecedorService;

    @Test
    void adminDeveAssociarERemoverFornecedor() throws Exception {
        CategoriaCardapio categoria = categoriaCardapioService.criar("Categoria Fornecedor Item Ctrl", null, 1);
        ItemCardapio criado = itemCardapioService.criar(
                categoria.getId(), "ITEM-FORN-CTRL", "Item Fornecedor Ctrl", null, new BigDecimal("9.00"), 5,
                SecaoPreparo.BAR, true, true, BigDecimal.ZERO, LocalDate.of(2026, 8, 20));
        Fornecedor fornecedor = fornecedorService.cadastrar("Fornecedor Item Ctrl", "66677788899900");

        mockMvc.perform(post("/api/itens-cardapio/" + criado.getId() + "/fornecedor")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fornecedorId":%d}
                                """.formatted(fornecedor.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fornecedorRazaoSocial").value("Fornecedor Item Ctrl"));

        mockMvc.perform(post("/api/itens-cardapio/" + criado.getId() + "/fornecedor/remover")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fornecedorId").isEmpty());
    }

    @Test
    void adminDeveAlterarEstoqueMinimo() throws Exception {
        CategoriaCardapio categoria = categoriaCardapioService.criar("Categoria Estoque Minimo Item Ctrl", null, 1);
        ItemCardapio criado = itemCardapioService.criar(
                categoria.getId(), "ITEM-ESTOQUE-MINIMO-CTRL", "Item Estoque Mínimo Ctrl", null,
                new BigDecimal("9.00"), 5, SecaoPreparo.BAR, true, true, BigDecimal.ZERO, LocalDate.of(2026, 8, 20));

        mockMvc.perform(post("/api/itens-cardapio/" + criado.getId() + "/estoque-minimo")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"estoqueMinimo":3.000}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estoqueMinimo").value(3.0));
    }

    @Test
    void adminDeveCriarItem() throws Exception {
        CategoriaCardapio categoria = categoriaCardapioService.criar("Categoria Item Ctrl", null, 1);

        mockMvc.perform(post("/api/itens-cardapio")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoriaId":%d,"codigo":"ITEM-CTRL-0001","nome":"Suco Ctrl","descricao":null,
                                "precoVenda":9.00,"tempoPreparoMinutos":5,"secaoPreparo":"BAR","exigePreparo":true,
                                "controlaEstoque":true,"saldoEstoque":10.000,"dataCadastro":"2026-08-20",
                                "estoqueMinimo":2.000}
                                """.formatted(categoria.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Suco Ctrl"));
    }

    @Test
    void garcomNaoDeveCriarItem() throws Exception {
        usuarioService.criar("Garçom Item Ctrl", "garcom.item.ctrl", "senha", PerfilUsuario.GARCOM);
        CategoriaCardapio categoria = categoriaCardapioService.criar("Categoria Item Sem Permissao Ctrl", null, 1);

        mockMvc.perform(post("/api/itens-cardapio")
                        .with(httpBasic("garcom.item.ctrl", "senha"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoriaId":%d,"codigo":"ITEM-CTRL-0002","nome":"Suco Ctrl","descricao":null,
                                "precoVenda":9.00,"tempoPreparoMinutos":5,"secaoPreparo":"BAR","exigePreparo":true,
                                "controlaEstoque":true,"saldoEstoque":10.000,"dataCadastro":"2026-08-20",
                                "estoqueMinimo":2.000}
                                """.formatted(categoria.getId())))
                .andExpect(status().isForbidden());
    }

    @Test
    void qualquerAutenticadoDeveListarEBuscarItem() throws Exception {
        usuarioService.criar("Cozinha Item Ctrl", "cozinha.item.ctrl", "senha", PerfilUsuario.COZINHA);
        CategoriaCardapio categoria = categoriaCardapioService.criar("Categoria Listar Item Ctrl", null, 1);
        ItemCardapio criado = itemCardapioService.criar(
                categoria.getId(), "ITEM-LISTAR-CTRL", "Item Listar Ctrl", null, new BigDecimal("9.00"), 5,
                SecaoPreparo.BAR, true, true, BigDecimal.ZERO, LocalDate.of(2026, 8, 20));

        mockMvc.perform(get("/api/itens-cardapio").with(httpBasic("cozinha.item.ctrl", "senha")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/itens-cardapio/" + criado.getId())
                        .with(httpBasic("cozinha.item.ctrl", "senha")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Item Listar Ctrl"));
    }

    @Test
    void adminDeveAtualizarItem() throws Exception {
        CategoriaCardapio categoria = categoriaCardapioService.criar("Categoria Atualizar Item Ctrl", null, 1);
        ItemCardapio criado = itemCardapioService.criar(
                categoria.getId(), "ITEM-ATUALIZAR-CTRL", "Nome Antigo Ctrl", null, new BigDecimal("9.00"), 5,
                SecaoPreparo.BAR, true, true, BigDecimal.ZERO, LocalDate.of(2026, 8, 20));

        mockMvc.perform(put("/api/itens-cardapio/" + criado.getId())
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Nome Novo Ctrl","descricao":"nova descrição","precoVenda":12.00,
                                "tempoPreparoMinutos":8,"secaoPreparo":"SOBREMESA","exigePreparo":false,
                                "controlaEstoque":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Novo Ctrl"));
    }

    @Test
    void adminDeveRegistrarEntradaESaidaDeEstoque() throws Exception {
        CategoriaCardapio categoria = categoriaCardapioService.criar("Categoria Estoque Item Ctrl", null, 1);
        ItemCardapio criado = itemCardapioService.criar(
                categoria.getId(), "ITEM-ESTOQUE-CTRL", "Item Estoque Ctrl", null, new BigDecimal("9.00"), 5,
                SecaoPreparo.BAR, true, true, new BigDecimal("10.000"), LocalDate.of(2026, 8, 20));

        mockMvc.perform(post("/api/itens-cardapio/" + criado.getId() + "/entradas-estoque")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantidade":5.000}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoEstoque").value(15.0));

        mockMvc.perform(post("/api/itens-cardapio/" + criado.getId() + "/saidas-estoque")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantidade":3.000}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoEstoque").value(12.0));
    }

    @Test
    void adminDeveAtivarEInativarItem() throws Exception {
        CategoriaCardapio categoria = categoriaCardapioService.criar("Categoria Toggle Item Ctrl", null, 1);
        ItemCardapio criado = itemCardapioService.criar(
                categoria.getId(), "ITEM-TOGGLE-CTRL", "Item Toggle Ctrl", null, new BigDecimal("9.00"), 5,
                SecaoPreparo.BAR, true, true, BigDecimal.ZERO, LocalDate.of(2026, 8, 20));

        mockMvc.perform(post("/api/itens-cardapio/" + criado.getId() + "/inativar")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INATIVO"));

        mockMvc.perform(post("/api/itens-cardapio/" + criado.getId() + "/ativar")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ATIVO"));
    }
}
