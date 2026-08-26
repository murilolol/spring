package com.curso.restaurante.domain.usuario;

import com.curso.restaurante.domain.Status;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioTest {

    @Test
    void deveCriarUsuarioAtivoComOsDadosInformados() {
        Usuario usuario = new Usuario("Maria Garçonete", "maria.garconete", "hash-bcrypt", PerfilUsuario.GARCOM);

        assertEquals("Maria Garçonete", usuario.getNome());
        assertEquals("maria.garconete", usuario.getUsername());
        assertEquals("hash-bcrypt", usuario.getSenhaHash());
        assertEquals(PerfilUsuario.GARCOM, usuario.getPerfil());
        assertEquals(Status.ATIVO, usuario.getStatus());
        assertTrue(usuario.estaAtivo());
        assertNotNull(usuario.getCriadoEm());
    }

    @Test
    void deveRejeitarNomeEmBranco() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Usuario("   ", "maria.garconete", "hash-bcrypt", PerfilUsuario.GARCOM));
    }

    @Test
    void deveRejeitarUsernameEmBranco() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Usuario("Maria Garçonete", "  ", "hash-bcrypt", PerfilUsuario.GARCOM));
    }

    @Test
    void deveRejeitarSenhaHashNula() {
        assertThrows(
                NullPointerException.class,
                () -> new Usuario("Maria Garçonete", "maria.garconete", null, PerfilUsuario.GARCOM));
    }

    @Test
    void deveRejeitarPerfilNulo() {
        assertThrows(
                NullPointerException.class,
                () -> new Usuario("Maria Garçonete", "maria.garconete", "hash-bcrypt", null));
    }

    @Test
    void deveAlterarSenha() {
        Usuario usuario = new Usuario("Maria Garçonete", "maria.garconete", "hash-antigo", PerfilUsuario.GARCOM);

        usuario.alterarSenha("hash-novo");

        assertEquals("hash-novo", usuario.getSenhaHash());
    }

    @Test
    void deveRejeitarAlterarSenhaParaValorNulo() {
        Usuario usuario = new Usuario("Maria Garçonete", "maria.garconete", "hash-antigo", PerfilUsuario.GARCOM);

        assertThrows(NullPointerException.class, () -> usuario.alterarSenha(null));
    }

    @Test
    void deveAlterarNome() {
        Usuario usuario = new Usuario("Nome Antigo", "nome.antigo", "hash", PerfilUsuario.GARCOM);

        usuario.alterarNome("Nome Novo");

        assertEquals("Nome Novo", usuario.getNome());
    }

    @Test
    void deveRejeitarAlterarNomeParaValorEmBranco() {
        Usuario usuario = new Usuario("Nome Antigo", "nome.antigo", "hash", PerfilUsuario.GARCOM);

        assertThrows(IllegalArgumentException.class, () -> usuario.alterarNome("   "));
    }

    @Test
    void deveAlterarPerfil() {
        Usuario usuario = new Usuario("Maria Garçonete", "maria.garconete", "hash", PerfilUsuario.GARCOM);

        usuario.alterarPerfil(PerfilUsuario.ADMIN);

        assertEquals(PerfilUsuario.ADMIN, usuario.getPerfil());
    }

    @Test
    void deveInativarEAtivar() {
        Usuario usuario = new Usuario("Maria Garçonete", "maria.garconete", "hash", PerfilUsuario.GARCOM);

        usuario.inativar();
        assertEquals(Status.INATIVO, usuario.getStatus());
        assertTrue(!usuario.estaAtivo());

        usuario.ativar();
        assertEquals(Status.ATIVO, usuario.getStatus());
        assertTrue(usuario.estaAtivo());
    }
}
