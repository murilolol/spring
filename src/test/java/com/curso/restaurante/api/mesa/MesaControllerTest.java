package com.curso.restaurante.api.mesa;

import com.curso.restaurante.domain.mesa.Mesa;
import com.curso.restaurante.domain.usuario.PerfilUsuario;
import com.curso.restaurante.service.mesa.MesaService;
import com.curso.restaurante.service.usuario.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
class MesaControllerTest {

    private static final String SENHA_ADMIN_BOOTSTRAP = "admin123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MesaService mesaService;

    @Autowired
    private UsuarioService usuarioService;

    @Test
    void adminDeveCriarMesa() throws Exception {
        mockMvc.perform(post("/api/mesas")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numero":601,"capacidade":4,"setor":"Salão Ctrl"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").value(601));
    }

    @Test
    void garcomNaoDeveCriarMesa() throws Exception {
        usuarioService.criar("Garçom Mesa Ctrl", "garcom.mesa.ctrl", "senha", PerfilUsuario.GARCOM);

        mockMvc.perform(post("/api/mesas")
                        .with(httpBasic("garcom.mesa.ctrl", "senha"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numero":602,"capacidade":4,"setor":"Salão Ctrl"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void qualquerAutenticadoDeveListarMesas() throws Exception {
        usuarioService.criar("Cozinha Mesa Ctrl", "cozinha.mesa.ctrl", "senha", PerfilUsuario.COZINHA);

        mockMvc.perform(get("/api/mesas").with(httpBasic("cozinha.mesa.ctrl", "senha")))
                .andExpect(status().isOk());
    }

    @Test
    void adminDeveAtualizarMesa() throws Exception {
        Mesa criada = mesaService.criar(603, 4, "Salão Ctrl");

        mockMvc.perform(put("/api/mesas/" + criada.getId())
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"capacidade":8,"setor":"Varanda Ctrl"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.setor").value("Varanda Ctrl"));
    }

    @Test
    void garcomDeveReservarECancelarReserva() throws Exception {
        usuarioService.criar("Garçom Reserva Ctrl", "garcom.reserva.ctrl", "senha", PerfilUsuario.GARCOM);
        Mesa criada = mesaService.criar(604, 4, "Salão Ctrl");

        mockMvc.perform(post("/api/mesas/" + criada.getId() + "/reservar")
                        .with(httpBasic("garcom.reserva.ctrl", "senha")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESERVADA"));

        mockMvc.perform(post("/api/mesas/" + criada.getId() + "/cancelar-reserva")
                        .with(httpBasic("garcom.reserva.ctrl", "senha")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LIVRE"));
    }

    @Test
    void reservarMesaJaOcupadaDeveRetornar409() throws Exception {
        Mesa criada = mesaService.criar(605, 4, "Salão Ctrl");
        mesaService.reservar(criada.getId());
        mesaService.cancelarReserva(criada.getId());

        mockMvc.perform(post("/api/mesas/" + criada.getId() + "/cancelar-reserva")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP)))
                .andExpect(status().isConflict());
    }

    @Test
    void adminDeveInterditarELiberarInterdicao() throws Exception {
        Mesa criada = mesaService.criar(606, 4, "Salão Ctrl");

        mockMvc.perform(post("/api/mesas/" + criada.getId() + "/interditar")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INTERDITADA"));

        mockMvc.perform(post("/api/mesas/" + criada.getId() + "/liberar-interdicao")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LIVRE"));
    }

    @Test
    void garcomNaoDeveInterditarMesa() throws Exception {
        usuarioService.criar("Garçom Interdita Ctrl", "garcom.interdita.ctrl", "senha", PerfilUsuario.GARCOM);
        Mesa criada = mesaService.criar(607, 4, "Salão Ctrl");

        mockMvc.perform(post("/api/mesas/" + criada.getId() + "/interditar")
                        .with(httpBasic("garcom.interdita.ctrl", "senha")))
                .andExpect(status().isForbidden());
    }
}
