package com.taskflow.controller;

import com.taskflow.dto.HistoricoDTO;
import com.taskflow.service.HistoricoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tarefas/{tarefaId}/historico")
@CrossOrigin(origins = "*")
public class HistoricoController {

    private final HistoricoService service;

    public HistoricoController(HistoricoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<HistoricoDTO>> listar(@PathVariable Long tarefaId) {
        return ResponseEntity.ok(service.listarPorTarefa(tarefaId));
    }
}
