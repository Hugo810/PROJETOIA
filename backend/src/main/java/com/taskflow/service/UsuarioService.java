package com.taskflow.service;

import com.taskflow.config.AccessDeniedException;
import com.taskflow.dto.LoginRequest;
import com.taskflow.dto.UsuarioDTO;
import com.taskflow.model.Usuario;
import com.taskflow.model.UsuarioRole;
import com.taskflow.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public UsuarioDTO login(LoginRequest req) {
        Usuario user = repository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Email ou senha invalidos"));
        if (!user.getSenha().equals(req.getSenha())) {
            throw new RuntimeException("Email ou senha invalidos");
        }
        log.info("Usuario logado: id={}, email={}", user.getId(), user.getEmail());
        return toDTO(user);
    }

    public List<UsuarioDTO> listar() {
        return repository.findAll().stream().map(this::toDTO).toList();
    }

    public UsuarioDTO buscarPorId(Long id) {
        return toDTO(repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado: " + id)));
    }

    public UsuarioDTO criar(UsuarioDTO dto) {
        Usuario u = new Usuario(null, dto.getNome(), dto.getEmail(), "123456", dto.getRole() != null ? dto.getRole() : UsuarioRole.EXECUTOR);
        UsuarioDTO saved = toDTO(repository.save(u));
        log.info("Usuario criado: id={}, email={}, role={}", saved.getId(), saved.getEmail(), saved.getRole());
        return saved;
    }

    public UsuarioDTO atualizar(Long id, UsuarioDTO dto) {
        Usuario u = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado: " + id));
        u.setNome(dto.getNome());
        u.setEmail(dto.getEmail());
        if (dto.getRole() != null) u.setRole(dto.getRole());
        UsuarioDTO saved = toDTO(repository.save(u));
        log.info("Usuario atualizado: id={}, email={}", id, dto.getEmail());
        return saved;
    }

    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Usuario nao encontrado: " + id);
        }
        repository.deleteById(id);
        log.info("Usuario excluido: id={}", id);
    }

    public Usuario findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado: " + id));
    }

    public void validarRole(Long usuarioId, UsuarioRole roleMinima) {
        Usuario u = findEntityById(usuarioId);
        if (u.getRole() == UsuarioRole.ADMIN) return;
        if (u.getRole() == UsuarioRole.DISTRIBUIDOR && roleMinima == UsuarioRole.DISTRIBUIDOR) return;
        if (u.getRole() == roleMinima) return;
        throw new AccessDeniedException("Acesso negado: permissao insuficiente");
    }

    private UsuarioDTO toDTO(Usuario u) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(u.getId());
        dto.setNome(u.getNome());
        dto.setEmail(u.getEmail());
        dto.setRole(u.getRole());
        return dto;
    }
}
