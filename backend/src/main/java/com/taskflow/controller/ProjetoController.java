package com.taskflow.controller;

import com.taskflow.dto.MetaDTO;
import com.taskflow.dto.ProjetoDTO;
import com.taskflow.service.ProjetoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projetos")
public class ProjetoController {

    private final ProjetoService service;

    public ProjetoController(ProjetoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ProjetoDTO>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjetoDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ProjetoDTO> criar(@RequestBody ProjetoDTO dto,
                                             @RequestHeader("X-User-Id") Long usuarioId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto, usuarioId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjetoDTO> atualizar(@PathVariable Long id, @RequestBody ProjetoDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/metas")
    public ResponseEntity<List<MetaDTO>> listarMetas(@PathVariable Long id) {
        return ResponseEntity.ok(service.listarMetas(id));
    }

    @PostMapping("/{id}/metas")
    public ResponseEntity<MetaDTO> criarMeta(@PathVariable Long id, @RequestBody MetaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarMeta(id, dto));
    }

    @PutMapping("/metas/{metaId}")
    public ResponseEntity<MetaDTO> atualizarMeta(@PathVariable Long metaId, @RequestBody MetaDTO dto) {
        return ResponseEntity.ok(service.atualizarMeta(metaId, dto));
    }

    @DeleteMapping("/metas/{metaId}")
    public ResponseEntity<Void> excluirMeta(@PathVariable Long metaId) {
        service.excluirMeta(metaId);
        return ResponseEntity.noContent().build();
    }
}
