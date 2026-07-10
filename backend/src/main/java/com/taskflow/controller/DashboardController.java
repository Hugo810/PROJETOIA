package com.taskflow.controller;

import com.taskflow.dto.*;
import com.taskflow.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> dashboard() {
        return ResponseEntity.ok(Map.of(
                "resumo", service.resumo(),
                "porStatus", service.porStatus(),
                "porCategoria", service.porCategoria(),
                "porResponsavel", service.porResponsavel(),
                "tendencia", service.tendencia()
        ));
    }

    @GetMapping("/resumo")
    public ResponseEntity<DashboardResumo> resumo() {
        return ResponseEntity.ok(service.resumo());
    }

    @GetMapping("/por-status")
    public ResponseEntity<List<TarefasPorStatus>> porStatus() {
        return ResponseEntity.ok(service.porStatus());
    }

    @GetMapping("/por-categoria")
    public ResponseEntity<List<TarefasPorCategoria>> porCategoria() {
        return ResponseEntity.ok(service.porCategoria());
    }

    @GetMapping("/por-responsavel")
    public ResponseEntity<List<TarefasPorResponsavel>> porResponsavel() {
        return ResponseEntity.ok(service.porResponsavel());
    }

    @GetMapping("/tendencia")
    public ResponseEntity<List<Tendencia>> tendencia() {
        return ResponseEntity.ok(service.tendencia());
    }
}
