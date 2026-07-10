package com.taskflow.controller;

import com.taskflow.dto.TarefaRecorrenteDTO;
import com.taskflow.service.TarefaRecorrenteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TarefaRecorrenteController {

    private final TarefaRecorrenteService service;

    public TarefaRecorrenteController(TarefaRecorrenteService service) {
        this.service = service;
    }

    @GetMapping("/tarefas-recorrentes")
    public ResponseEntity<List<TarefaRecorrenteDTO>> listar() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @PostMapping("/tarefas-recorrentes")
    public ResponseEntity<TarefaRecorrenteDTO> criar(@RequestBody TarefaRecorrenteDTO dto,
                                                      @RequestHeader("X-User-Id") Long usuarioId) {
        TarefaRecorrenteDTO criada = service.criar(dto, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @PutMapping("/tarefas-recorrentes/{id}")
    public ResponseEntity<TarefaRecorrenteDTO> atualizar(@PathVariable Long id,
                                                          @RequestBody TarefaRecorrenteDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/tarefas-recorrentes/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/tarefas-recorrentes/{id}/ativa")
    public ResponseEntity<TarefaRecorrenteDTO> toggleAtiva(@PathVariable Long id, @RequestBody Boolean ativa) {
        return ResponseEntity.ok(service.atualizar(id, new TarefaRecorrenteDTO() {{ setAtiva(ativa); }}));
    }

    @PatchMapping("/tarefas-recorrentes/{id}/pular")
    public ResponseEntity<TarefaRecorrenteDTO> pular(@PathVariable Long id) {
        return ResponseEntity.ok(service.pular(id));
    }

    @PatchMapping("/tarefas-recorrentes/{id}/adiar")
    public ResponseEntity<TarefaRecorrenteDTO> adiar(@PathVariable Long id, @RequestParam int dias) {
        return ResponseEntity.ok(service.adiar(id, dias));
    }

    @GetMapping("/tarefas-recorrentes/{id}/proximas")
    public ResponseEntity<List<java.time.LocalDate>> proximasExecucoes(@PathVariable Long id,
                                                                       @RequestParam(defaultValue = "6") int quantidade) {
        return ResponseEntity.ok(service.calcularProximasExecucoes(id, quantidade));
    }

    @PostMapping("/tarefas-recorrentes/executar")
    public ResponseEntity<List<TarefaRecorrenteDTO>> executarPendentes() {
        return ResponseEntity.ok(service.executarPendentes().stream().map(t -> {
            TarefaRecorrenteDTO dto = new TarefaRecorrenteDTO();
            dto.setId(t.getId());
            dto.setTarefaModeloTitulo(t.getTitulo());
            return dto;
        }).toList());
    }
}
