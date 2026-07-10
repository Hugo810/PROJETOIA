package com.taskflow.controller;

import com.taskflow.dto.*;
import com.taskflow.model.TipoDependencia;
import com.taskflow.service.GanttService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GanttController {

    private final GanttService service;

    public GanttController(GanttService service) {
        this.service = service;
    }

    @GetMapping("/gantt")
    public ResponseEntity<Map<String, Object>> gantt(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(Map.of(
                "tarefas", service.buscarTarefas(inicio, fim),
                "dependencias", service.listarDependencias(null),
                "marcos", service.listarMarcos()
        ));
    }

    @GetMapping("/tarefas/{id}/dependencias")
    public ResponseEntity<List<DependenciaTarefaDTO>> dependencias(@PathVariable Long id) {
        return ResponseEntity.ok(service.listarDependencias(id));
    }

    @PostMapping("/tarefas/{id}/dependencias")
    public ResponseEntity<DependenciaTarefaDTO> criarDependencia(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long dependenteId = ((Number) body.get("tarefaDependenteId")).longValue();
        TipoDependencia tipo = TipoDependencia.valueOf((String) body.get("tipo"));
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarDependencia(id, dependenteId, tipo));
    }

    @DeleteMapping("/dependencias/{id}")
    public ResponseEntity<Void> excluirDependencia(@PathVariable Long id) {
        service.excluirDependencia(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/marcos")
    public ResponseEntity<List<MarcoDTO>> marcos() {
        return ResponseEntity.ok(service.listarMarcos());
    }

    @PostMapping("/marcos")
    public ResponseEntity<MarcoDTO> criarMarco(@RequestBody MarcoDTO dto,
                                                @RequestHeader("X-User-Id") Long usuarioId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarMarco(dto, usuarioId));
    }

    @DeleteMapping("/marcos/{id}")
    public ResponseEntity<Void> excluirMarco(@PathVariable Long id) {
        service.excluirMarco(id);
        return ResponseEntity.noContent().build();
    }
}
