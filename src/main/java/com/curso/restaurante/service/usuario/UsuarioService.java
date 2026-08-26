package com.curso.restaurante.service.usuario;

import com.curso.restaurante.domain.Status;
import com.curso.restaurante.domain.comum.ConflitoDeEstadoException;
import com.curso.restaurante.domain.comum.RecursoNaoEncontradoException;
import com.curso.restaurante.domain.comum.RegraDeNegocioException;
import com.curso.restaurante.domain.usuario.PerfilUsuario;
import com.curso.restaurante.domain.usuario.Usuario;
import com.curso.restaurante.repository.usuario.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario criar(String nome, String username, String senha, PerfilUsuario perfil) {
        if (usuarioRepository.existsByUsername(username)) {
            throw new ConflitoDeEstadoException("Nome de usuário já está em uso");
        }

        Usuario usuario = new Usuario(nome, username, passwordEncoder.encode(senha), perfil);
        return usuarioRepository.save(usuario);
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }

    public Usuario buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }

    public Page<Usuario> listar(PerfilUsuario perfil, Status status, Pageable pageable) {
        Specification<Usuario> especificacao = Specification.allOf();

        if (perfil != null) {
            especificacao = especificacao.and((raiz, consulta, cb) -> cb.equal(raiz.get("perfil"), perfil));
        }
        if (status != null) {
            especificacao = especificacao.and((raiz, consulta, cb) -> cb.equal(raiz.get("status"), status));
        }

        return usuarioRepository.findAll(especificacao, pageable);
    }

    public Usuario atualizar(Long id, String nome, PerfilUsuario perfil) {
        Usuario usuario = buscarPorId(id);
        usuario.alterarNome(nome);
        usuario.alterarPerfil(perfil);
        return usuario;
    }

    public void alterarSenha(
            Long id,
            String senhaAtualInformada,
            String novaSenha,
            String usernameAutenticado,
            boolean autenticadoEhAdmin) {
        Usuario usuario = buscarPorId(id);
        boolean ehDono = usuario.getUsername().equals(usernameAutenticado);

        if (!ehDono && !autenticadoEhAdmin) {
            throw new AccessDeniedException("Você só pode alterar a própria senha");
        }

        if (ehDono && !passwordEncoder.matches(senhaAtualInformada, usuario.getSenhaHash())) {
            throw new RegraDeNegocioException("Senha atual incorreta");
        }

        usuario.alterarSenha(passwordEncoder.encode(novaSenha));
    }

    public Usuario ativar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.ativar();
        return usuario;
    }

    public Usuario inativar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.inativar();
        return usuario;
    }
}
