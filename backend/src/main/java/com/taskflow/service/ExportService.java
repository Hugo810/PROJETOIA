package com.taskflow.service;

import com.taskflow.dto.TarefaDTO;
import com.taskflow.model.Categoria;
import com.taskflow.model.Tarefa;
import com.taskflow.repository.TarefaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExportService {

    private static final Logger log = LoggerFactory.getLogger(ExportService.class);

    private final TarefaRepository tarefaRepository;

    public ExportService(TarefaRepository tarefaRepository) {
        this.tarefaRepository = tarefaRepository;
    }

    public List<TarefaDTO> exportarJSON() {
        log.info("Exportando tarefas em formato JSON");
        return tarefaRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public String exportarCSV() {
        log.info("Exportando tarefas em formato CSV");
        List<Tarefa> tarefas = tarefaRepository.findAll();

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Título,Descrição,Status,Prazo,Categoria,Responsável,Data Criação,Data Conclusão\n");

        for (Tarefa t : tarefas) {
            csv.append(escaparCSV(String.valueOf(t.getId()))).append(",");
            csv.append(escaparCSV(t.getTitulo())).append(",");
            csv.append(escaparCSV(t.getDescricao() != null ? t.getDescricao() : "")).append(",");
            csv.append(escaparCSV(t.getStatus() != null ? t.getStatus().name() : "")).append(",");
            csv.append(escaparCSV(t.getPrazo() != null ? t.getPrazo().toString() : "")).append(",");
            csv.append(escaparCSV(t.getCategoria() != null ? t.getCategoria().getNome() : "")).append(",");
            csv.append(escaparCSV(t.getResponsavel() != null ? t.getResponsavel().getNome() : "")).append(",");
            csv.append(escaparCSV(t.getDataCriacao() != null ? t.getDataCriacao().toString() : "")).append(",");
            csv.append(escaparCSV(t.getDataConclusao() != null ? t.getDataConclusao().toString() : ""));
            csv.append("\n");
        }
        return csv.toString();
    }

    private TarefaDTO toDTO(Tarefa t) {
        TarefaDTO dto = new TarefaDTO();
        dto.setId(t.getId());
        dto.setTitulo(t.getTitulo());
        dto.setDescricao(t.getDescricao());
        dto.setPrazo(t.getPrazo());
        dto.setStatus(t.getStatus() != null ? t.getStatus().name() : "PENDENTE");
        dto.setPrioridade(t.getPrioridade() != null ? t.getPrioridade().name() : "MEDIA");
        dto.setDataCriacao(t.getDataCriacao());
        dto.setDataConclusao(t.getDataConclusao());
        dto.setDistribuidorId(t.getDistribuidorId());
        if (t.getCategoria() != null) {
            dto.setCategoriaId(t.getCategoria().getId());
            dto.setCategoriaNome(t.getCategoria().getNome());
        }
        if (t.getResponsavel() != null) {
            dto.setResponsavelId(t.getResponsavel().getId());
            dto.setResponsavelNome(t.getResponsavel().getNome());
        }
        return dto;
    }

    private String escaparCSV(String valor) {
        if (valor.contains(",") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}
