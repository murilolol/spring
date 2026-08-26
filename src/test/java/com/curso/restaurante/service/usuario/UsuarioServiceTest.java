package com.curso.restaurante.service.usuario;

import com.curso.restaurante.domain.Status;
import com.curso.restaurante.domain.comum.ConflitoDeEstadoException;
import com.curso.restaurante.domain.comum.RecursoNaoEncontradoException;
import com.curso.restaurante.domain.comum.RegraDeNegocioException;
import com.curso.restaurante.domain.usuario.PerfilUsuario;
import com.curso.restaurante.domain.usuario.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UsuarioServiceTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void deveCriarUsuarioComSenhaCriptografada() {
        Usuario usuario = usuarioService.criar("Ana Caixa", "ana.svc.criar", "senha-secreta", PerfilUsuario.CAIXA);

        assertNotEquals("senha-secreta", usuario.getSenhaHash());
        assertTrue(passwordEncoder.matches("senha-secreta", usuario.getSenhaHash()));
    }

    @Test
    void deveRejeitarUsernameDuplicado() {
        usuarioService.criar("Primeiro", "duplicado.svc", "senha-1", PerfilUsuario.GARCOM);

        assertThrows(
                ConflitoDeEstadoException.class,
                () -> usuarioService.criar("Segundo", "duplicado.svc", "senha-2", PerfilUsuario.CAIXA));
    }

    @Test
    void buscarPorIdDeveLancarQuandoNaoExiste() {
        assertThrows(RecursoNaoEncontradoException.class, () -> usuarioService.buscarPorId(-1L));
    }

    @Test
    void buscarPorIdDeveRetornarUsuarioExistente() {
        Usuario criado = usuarioService.criar("Buscar Por Id", "buscar.svc", "senha", PerfilUsuario.ADMIN);

        Usuario encontrado = usuarioService.buscarPorId(criado.getId());

        assertEquals(criado.getId(), encontrado.getId());
    }

    @Test
    void buscarPorUsernameDeveRetornarUsuarioExistente() {
        usuarioService.criar("Buscar Por Username", "buscar.username.svc", "senha", PerfilUsuario.ADMIN);

        Usuario encontrado = usuarioService.buscarPorUsername("buscar.username.svc");

        assertEquals("buscar.username.svc", encontrado.getUsername());
    }

    @Test
    void buscarPorUsernameDeveLancarQuandoNaoExiste() {
        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> usuarioService.buscarPorUsername("nao.existe.username.svc"));
    }

    @Test
    void deveListarComFiltroDePerfil() {
        usuarioService.criar("Cozinha Um", "cozinha.um.svc", "senha", PerfilUsuario.COZINHA);
        usuarioService.criar("Caixa Um", "caixa.um.svc", "senha", PerfilUsuario.CAIXA);

        var pagina = usuarioService.listar(PerfilUsuario.COZINHA, null, PageRequest.of(0, 50));

        assertTrue(pagina.getContent().stream().allMatch(u -> u.getPerfil() == PerfilUsuario.COZINHA));
        assertTrue(pagina.getContent().stream().anyMatch(u -> u.getUsername().equals("cozinha.um.svc")));
    }

    @Test
    void deveAtualizarNomeEPerfil() {
        Usuario criado = usuarioService.criar("Nome Antigo", "atualizar.svc", "senha", PerfilUsuario.GARCOM);

        Usuario atualizado = usuarioService.atualizar(criado.getId(), "Nome Novo", PerfilUsuario.ADMIN);

        assertEquals("Nome Novo", atualizado.getNome());
        assertEquals(PerfilUsuario.ADMIN, atualizado.getPerfil());
    }

    @Test
    void alterarSenhaDevePermitirQuandoEhODonoESenhaAtualConfere() {
        Usuario criado = usuarioService.criar("Dono Senha", "dono.senha.svc", "senha-atual", PerfilUsuario.GARCOM);

        usuarioService.alterarSenha(criado.getId(), "senha-atual", "senha-nova", "dono.senha.svc", false);

        Usuario recarregado = usuarioService.buscarPorId(criado.getId());
        assertTrue(passwordEncoder.matches("senha-nova", recarregado.getSenhaHash()));
    }

    @Test
    void alterarSenhaDeveRejeitarQuandoSenhaAtualNaoConfere() {
        Usuario criado = usuarioService.criar("Senha Errada", "senha.errada.svc", "senha-atual", PerfilUsuario.GARCOM);

        assertThrows(
                RegraDeNegocioException.class,
                () -> usuarioService.alterarSenha(
                        criado.getId(), "senha-incorreta", "senha-nova", "senha.errada.svc", false));
    }

    @Test
    void alterarSenhaDevePermitirAdminResetarSenhaDeOutroUsuarioSemSenhaAtual() {
        Usuario criado = usuarioService.criar("Resetado", "resetado.svc", "senha-atual", PerfilUsuario.GARCOM);

        usuarioService.alterarSenha(criado.getId(), null, "senha-nova-admin", "admin.logado", true);

        Usuario recarregado = usuarioService.buscarPorId(criado.getId());
        assertTrue(passwordEncoder.matches("senha-nova-admin", recarregado.getSenhaHash()));
    }

    @Test
    void alterarSenhaDeveRejeitarQuandoNaoEhDonoNemAdmin() {
        Usuario criado = usuarioService.criar("Vitima", "vitima.svc", "senha-atual", PerfilUsuario.GARCOM);

        assertThrows(
                AccessDeniedException.class,
                () -> usuarioService.alterarSenha(
                        criado.getId(), "senha-atual", "senha-nova", "outro.usuario", false));
    }

    @Test
    void deveInativarEAtivar() {
        Usuario criado = usuarioService.criar("Toggle Status", "toggle.svc", "senha", PerfilUsuario.GARCOM);

        Usuario inativado = usuarioService.inativar(criado.getId());
        assertEquals(Status.INATIVO, inativado.getStatus());

        Usuario ativado = usuarioService.ativar(criado.getId());
        assertEquals(Status.ATIVO, ativado.getStatus());
    }
}
