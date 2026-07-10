package com.taskflow.controller;

import com.taskflow.model.Tarefa;
import com.taskflow.service.ImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/importar")
@Tag(name = "Importação", description = "Importação de dados")
public class ImportController {

    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/tarefas")
    @Operation(summary = "Importar tarefas", description = "Importa tarefas a partir de arquivo CSV ou JSON")
    public ResponseEntity<?> importarTarefas(@RequestParam("file") MultipartFile file) {
        try {
            String nome = file.getOriginalFilename();
            List<Tarefa> importadas;
            if (nome != null && nome.toLowerCase().endsWith(".csv")) {
                importadas = importService.importarCSV(file.getInputStream());
            } else {
                importadas = importService.importarJSON(file.getInputStream());
            }
            return ResponseEntity.ok(Map.of(
                    "mensagem", importadas.size() + " tarefas importadas com sucesso",
                    "total", importadas.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Falha na importação: " + e.getMessage()));
        }
    }
}
