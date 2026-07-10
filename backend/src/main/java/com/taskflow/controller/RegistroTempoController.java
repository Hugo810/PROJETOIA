package com.taskflow.controller;

import com.taskflow.dto.RegistroTempoDTO;
import com.taskflow.service.RegistroTempoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RegistroTempoController {

    private final RegistroTempoService service;

    public RegistroTempoController(RegistroTempoService service) {
        this.service = service;
    }

    @GetMapping("/tarefas/{tarefaId}/tempo")
    public ResponseEntity<Map<String, Object>> listar(@PathVariable Long tarefaId) {
        List<RegistroTempoDTO> registros = service.listarPorTarefa(tarefaId);
        long total = service.totalMinutosTarefa(tarefaId);
        return ResponseEntity.ok(Map.of("registros", registros, "totalMinutos", total));
    }

    @PostMapping("/tarefas/{tarefaId}/tempo")
    public ResponseEntity<RegistroTempoDTO> iniciarTimer(@PathVariable Long tarefaId,
                                                         @RequestHeader("X-User-Id") Long usuarioId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.iniciarTimer(tarefaId, usuarioId));
    }

    @PatchMapping("/registros-tempo/{id}/parar")
    public ResponseEntity<RegistroTempoDTO> pararTimer(@PathVariable Long id,
                                                       @RequestHeader("X-User-Id") Long usuarioId) {
        return ResponseEntity.ok(service.pararTimer(id, usuarioId));
    }

    @PatchMapping("/tarefas/{tarefaId}/tempo/parar")
    public ResponseEntity<RegistroTempoDTO> pararTimerPorTarefa(@PathVariable Long tarefaId,
                                                                 @RequestHeader("X-User-Id") Long usuarioId) {
        return ResponseEntity.ok(service.pararTimerPorTarefa(tarefaId, usuarioId));
    }

    @PostMapping("/tarefas/{tarefaId}/tempo/manual")
    public ResponseEntity<RegistroTempoDTO> registrarManual(@PathVariable Long tarefaId,
                                                            @RequestHeader("X-User-Id") Long usuarioId,
                                                            @RequestBody Map<String, Object> body) {
        Long duracaoMinutos = body.get("duracaoMinutos") != null ? Long.valueOf(body.get("duracaoMinutos").toString()) : null;
        String descricao = body.get("descricao") != null ? body.get("descricao").toString() : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarManual(tarefaId, usuarioId, duracaoMinutos, descricao));
    }

    @GetMapping("/usuarios/{usuarioId}/tempo")
    public ResponseEntity<Map<String, Object>> listarPorUsuario(
            @PathVariable Long usuarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        List<RegistroTempoDTO> registros = service.listarPorUsuarioPeriodo(usuarioId, inicio, fim);
        long total = service.totalMinutosUsuarioPeriodo(usuarioId, inicio, fim);
        return ResponseEntity.ok(Map.of("registros", registros, "totalMinutos", total));
    }

    @DeleteMapping("/registros-tempo/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id, @RequestHeader("X-User-Id") Long usuarioId) {
        service.excluir(id, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
