package com.taskflow.controller;

import com.taskflow.dto.ComentarioDTO;
import com.taskflow.service.ComentarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ComentarioController {

    private final ComentarioService service;

    public ComentarioController(ComentarioService service) {
        this.service = service;
    }

    @GetMapping("/tarefas/{tarefaId}/comentarios")
    public ResponseEntity<List<ComentarioDTO>> listar(@PathVariable Long tarefaId) {
        return ResponseEntity.ok(service.listarPorTarefa(tarefaId));
    }

    @PostMapping("/tarefas/{tarefaId}/comentarios")
    public ResponseEntity<ComentarioDTO> criar(@PathVariable Long tarefaId, @Valid @RequestBody ComentarioDTO dto,
                                               @RequestHeader("X-User-Id") Long usuarioId) {
        dto.setTarefaId(tarefaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto, usuarioId));
    }

    @PutMapping("/comentarios/{id}")
    public ResponseEntity<ComentarioDTO> atualizar(@PathVariable Long id, @Valid @RequestBody ComentarioDTO dto,
                                                   @RequestHeader("X-User-Id") Long usuarioId) {
        return ResponseEntity.ok(service.atualizar(id, dto, usuarioId));
    }

    @DeleteMapping("/comentarios/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id, @RequestHeader("X-User-Id") Long usuarioId) {
        service.excluir(id, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
