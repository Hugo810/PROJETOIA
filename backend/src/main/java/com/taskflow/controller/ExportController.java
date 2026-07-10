package com.taskflow.controller;

import com.taskflow.dto.TarefaDTO;
import com.taskflow.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/exportar")
@Tag(name = "Exportação", description = "Exportação de dados")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/tarefas")
    @Operation(summary = "Exportar tarefas", description = "Exporta tarefas em formato CSV ou JSON")
    public ResponseEntity<?> exportarTarefas(
            @RequestParam(defaultValue = "json") String formato) {
        if ("csv".equalsIgnoreCase(formato)) {
            String csv = exportService.exportarCSV();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tarefas.csv")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(csv);
        }
        List<TarefaDTO> json = exportService.exportarJSON();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tarefas.json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }
}
