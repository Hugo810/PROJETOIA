package com.taskflow.controller;

import com.taskflow.model.Tarefa;
import com.taskflow.repository.TarefaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/calendario")
@Tag(name = "Calendário", description = "Integração com calendário iCal")
public class CalendarioController {

    private final TarefaRepository tarefaRepository;

    public CalendarioController(TarefaRepository tarefaRepository) {
        this.tarefaRepository = tarefaRepository;
    }

    @GetMapping("/tarefas/ics")
    @Operation(summary = "Exportar tarefas como iCalendar", description = "Gera arquivo .ics com as tarefas do usuário")
    public ResponseEntity<String> exportarICS(
            @RequestParam(required = false) Long usuarioId) {
        List<Tarefa> tarefas;
        if (usuarioId != null) {
            Page<Tarefa> page = tarefaRepository.findByResponsavelIdOrderByPrazoAsc(
                    usuarioId, PageRequest.of(0, 1000));
            tarefas = page.getContent();
        } else {
            tarefas = tarefaRepository.findAll();
        }

        StringBuilder ics = new StringBuilder();
        ics.append("BEGIN:VCALENDAR\n");
        ics.append("VERSION:2.0\n");
        ics.append("PRODID:-//TaskFlow//PT\n");
        ics.append("CALSCALE:GREGORIAN\n");
        ics.append("METHOD:PUBLISH\n");

        for (Tarefa t : tarefas) {
            ics.append("BEGIN:VEVENT\n");
            ics.append("UID:").append(UUID.randomUUID()).append("@taskflow\n");
            ics.append("SUMMARY:").append(escapeICS(t.getTitulo())).append("\n");
            if (t.getDescricao() != null) {
                ics.append("DESCRIPTION:").append(escapeICS(t.getDescricao())).append("\n");
            }
            if (t.getPrazo() != null) {
                ics.append("DTSTART;VALUE=DATE:").append(t.getPrazo().format(DateTimeFormatter.BASIC_ISO_DATE)).append("\n");
                ics.append("DTEND;VALUE=DATE:").append(t.getPrazo().plusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE)).append("\n");
            }
            if (t.getDataCriacao() != null) {
                ics.append("DTSTAMP:").append(t.getDataCriacao().format(
                        DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"))).append("\n");
            }
            ics.append("END:VEVENT\n");
        }

        ics.append("END:VCALENDAR\n");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tarefas.ics")
                .contentType(MediaType.parseMediaType("text/calendar"))
                .body(ics.toString());
    }

    private String escapeICS(String texto) {
        return texto.replace("\\", "\\\\").replace(",", "\\,").replace(";", "\\;").replace("\n", "\\n");
    }
}
