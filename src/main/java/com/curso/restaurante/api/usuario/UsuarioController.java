package com.curso.restaurante.api.usuario;

import com.curso.restaurante.api.comum.PaginaResponse;
import com.curso.restaurante.api.usuario.dto.AlterarSenhaRequest;
import com.curso.restaurante.api.usuario.dto.AtualizarUsuarioRequest;
import com.curso.restaurante.api.usuario.dto.CriarUsuarioRequest;
import com.curso.restaurante.api.usuario.dto.UsuarioResponse;
import com.curso.restaurante.domain.Status;
import com.curso.restaurante.domain.usuario.PerfilUsuario;
import com.curso.restaurante.domain.usuario.Usuario;
import com.curso.restaurante.service.usuario.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody CriarUsuarioRequest request) {
        Usuario usuario = usuarioService.criar(request.nome(), request.username(), request.senha(), request.perfil());
        return ResponseEntity
                .created(URI.create("/api/usuarios/" + usuario.getId()))
                .body(UsuarioResponse.de(usuario));
    }

    @GetMapping
    public PaginaResponse<UsuarioResponse> listar(
            @RequestParam(required = false) PerfilUsuario perfil,
            @RequestParam(required = false) Status status,
            Pageable pageable) {
        return PaginaResponse.de(usuarioService.listar(perfil, status, pageable).map(UsuarioResponse::de));
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscarPorId(@PathVariable Long id) {
        return UsuarioResponse.de(usuarioService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public UsuarioResponse atualizar(@PathVariable Long id, @Valid @RequestBody AtualizarUsuarioRequest request) {
        return UsuarioResponse.de(usuarioService.atualizar(id, request.nome(), request.perfil()));
    }

    @PatchMapping("/{id}/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void alterarSenha(
            @PathVariable Long id,
            @Valid @RequestBody AlterarSenhaRequest request,
            Authentication authentication) {
        boolean autenticadoEhAdmin = authentication.getAuthorities().stream()
                .anyMatch(autoridade -> autoridade.getAuthority().equals("ROLE_ADMIN"));
        usuarioService.alterarSenha(
                id, request.senhaAtual(), request.novaSenha(), authentication.getName(), autenticadoEhAdmin);
    }

    @PostMapping("/{id}/ativar")
    public UsuarioResponse ativar(@PathVariable Long id) {
        return UsuarioResponse.de(usuarioService.ativar(id));
    }

    @PostMapping("/{id}/inativar")
    public UsuarioResponse inativar(@PathVariable Long id) {
        return UsuarioResponse.de(usuarioService.inativar(id));
    }
}
