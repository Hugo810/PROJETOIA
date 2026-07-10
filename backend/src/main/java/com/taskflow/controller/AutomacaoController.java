package com.taskflow.controller;

import com.taskflow.dto.RegraAutomacaoDTO;
import com.taskflow.service.AutomacaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/automacoes")
public class AutomacaoController {

    private final AutomacaoService service;

    public AutomacaoController(AutomacaoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<RegraAutomacaoDTO>> listar() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @PostMapping
    public ResponseEntity<RegraAutomacaoDTO> criar(@RequestBody RegraAutomacaoDTO dto,
                                                    @RequestHeader("X-User-Id") Long usuarioId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto, usuarioId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegraAutomacaoDTO> atualizar(@PathVariable Long id, @RequestBody RegraAutomacaoDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/ativa")
    public ResponseEntity<Void> toggleAtiva(@PathVariable Long id, @RequestBody Boolean ativa) {
        service.toggleAtiva(id, ativa);
        return ResponseEntity.ok().build();
    }
}
