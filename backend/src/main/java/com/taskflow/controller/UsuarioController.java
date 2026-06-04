package com.taskflow.controller;

import com.taskflow.dto.UsuarioDTO;
import com.taskflow.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listar(@RequestHeader("X-User-Id") Long usuarioId) {
        service.findEntityById(usuarioId);
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> criar(@RequestHeader("X-User-Id") Long usuarioId, @RequestBody UsuarioDTO dto) {
        service.validarRole(usuarioId, com.taskflow.model.UsuarioRole.ADMIN);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> atualizar(@RequestHeader("X-User-Id") Long usuarioId, @PathVariable Long id, @RequestBody UsuarioDTO dto) {
        service.validarRole(usuarioId, com.taskflow.model.UsuarioRole.ADMIN);
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@RequestHeader("X-User-Id") Long usuarioId, @PathVariable Long id) {
        service.validarRole(usuarioId, com.taskflow.model.UsuarioRole.ADMIN);
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
