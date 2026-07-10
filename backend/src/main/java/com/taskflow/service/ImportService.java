package com.taskflow.service;

import com.taskflow.dto.TarefaDTO;
import com.taskflow.model.Tarefa;
import com.taskflow.model.TarefaStatus;
import com.taskflow.repository.CategoriaRepository;
import com.taskflow.repository.TarefaRepository;
import com.taskflow.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImportService {

    private static final Logger log = LoggerFactory.getLogger(ImportService.class);

    private final TarefaRepository tarefaRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper;

    public ImportService(TarefaRepository tarefaRepository, CategoriaRepository categoriaRepository,
                         UsuarioRepository usuarioRepository, ObjectMapper objectMapper) {
        this.tarefaRepository = tarefaRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public List<Tarefa> importarJSON(InputStream inputStream) throws Exception {
        log.info("Importando tarefas via JSON");
        List<TarefaDTO> dtos = objectMapper.readValue(inputStream,
                objectMapper.getTypeFactory().constructCollectionType(List.class, TarefaDTO.class));
        List<Tarefa> importadas = new ArrayList<>();
        for (TarefaDTO dto : dtos) {
            Tarefa tarefa = converterDTO(dto);
            importadas.add(tarefaRepository.save(tarefa));
        }
        log.info("Importadas {} tarefas via JSON", importadas.size());
        return importadas;
    }

    @Transactional
    public List<Tarefa> importarCSV(InputStream inputStream) throws Exception {
        log.info("Importando tarefas via CSV");
        List<Tarefa> importadas = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String header = reader.readLine();
            String linha;
            while ((linha = reader.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] campos = linha.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                Tarefa tarefa = new Tarefa();
                tarefa.setTitulo(campos.length > 1 ? campos[1].replace("\"", "").trim() : "Sem título");
                tarefa.setDescricao(campos.length > 2 ? campos[2].replace("\"", "").trim() : null);
                if (campos.length > 3 && !campos[3].trim().isEmpty()) {
                    try {
                        tarefa.setStatus(TarefaStatus.valueOf(campos[3].replace("\"", "").trim()));
                    } catch (IllegalArgumentException ignored) {
                        tarefa.setStatus(TarefaStatus.PENDENTE);
                    }
                } else {
                    tarefa.setStatus(TarefaStatus.PENDENTE);
                }
                if (campos.length > 4 && !campos[4].trim().isEmpty()) {
                    try {
                        tarefa.setPrazo(LocalDate.parse(campos[4].replace("\"", "").trim()));
                    } catch (Exception ignored) {}
                }
                importadas.add(tarefaRepository.save(tarefa));
            }
        }
        log.info("Importadas {} tarefas via CSV", importadas.size());
        return importadas;
    }

    private Tarefa converterDTO(TarefaDTO dto) {
        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo(dto.getTitulo());
        tarefa.setDescricao(dto.getDescricao());
        if (dto.getStatus() != null) {
            try {
                tarefa.setStatus(TarefaStatus.valueOf(dto.getStatus()));
            } catch (IllegalArgumentException e) {
                tarefa.setStatus(TarefaStatus.PENDENTE);
            }
        } else {
            tarefa.setStatus(TarefaStatus.PENDENTE);
        }
        tarefa.setPrazo(dto.getPrazo() != null ? dto.getPrazo() : LocalDate.now().plusDays(7));
        tarefa.setDataCriacao(LocalDateTime.now());
        if (dto.getCategoriaId() != null) {
            categoriaRepository.findById(dto.getCategoriaId()).ifPresent(tarefa::setCategoria);
        }
        if (dto.getResponsavelId() != null) {
            usuarioRepository.findById(dto.getResponsavelId()).ifPresent(tarefa::setResponsavel);
        }
        return tarefa;
    }
}
