package com.taskflow.controller;

import com.taskflow.dto.TarefaDTO;
import com.taskflow.service.TarefaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tarefas")
@CrossOrigin(origins = "*")
public class TarefaController {

    private final TarefaService service;

    public TarefaController(TarefaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<TarefaDTO>> listar(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long responsavelId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho) {
        return ResponseEntity.ok(service.listar(status, categoriaId, responsavelId, pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TarefaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<TarefaDTO> criar(@Valid @RequestBody TarefaDTO dto, @RequestHeader("X-User-Id") Long usuarioId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto, usuarioId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TarefaDTO> atualizar(@PathVariable Long id, @Valid @RequestBody TarefaDTO dto, @RequestHeader("X-User-Id") Long usuarioId) {
        return ResponseEntity.ok(service.atualizar(id, dto, usuarioId));
    }

    @PatchMapping("/{id}/iniciar")
    public ResponseEntity<TarefaDTO> iniciar(@PathVariable Long id, @RequestHeader("X-User-Id") Long usuarioId) {
        return ResponseEntity.ok(service.iniciar(id, usuarioId));
    }

    @PatchMapping("/{id}/concluir")
    public ResponseEntity<TarefaDTO> concluir(@PathVariable Long id, @RequestHeader("X-User-Id") Long usuarioId) {
        return ResponseEntity.ok(service.concluir(id, usuarioId));
    }

    @PatchMapping("/{id}/distribuir")
    public ResponseEntity<TarefaDTO> distribuir(@PathVariable Long id, @RequestParam Long responsavelId, @RequestHeader("X-User-Id") Long usuarioId) {
        return ResponseEntity.ok(service.distribuir(id, responsavelId, usuarioId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id, @RequestHeader("X-User-Id") Long usuarioId) {
        service.excluir(id, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
