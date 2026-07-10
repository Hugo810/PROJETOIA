package com.taskflow.service;

import com.taskflow.config.CategoriaNotFoundException;
import com.taskflow.config.RegraNegocioException;
import com.taskflow.dto.CategoriaDTO;
import com.taskflow.model.Categoria;
import com.taskflow.repository.CategoriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    private static final Logger log = LoggerFactory.getLogger(CategoriaService.class);

    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    public List<CategoriaDTO> listarTodas() {
        return repository.findAll().stream().map(this::toDTO).toList();
    }

    public CategoriaDTO buscarPorId(Long id) {
        return toDTO(repository.findById(id)
                .orElseThrow(() -> new CategoriaNotFoundException(id)));
    }

    public CategoriaDTO criar(CategoriaDTO dto) {
        if (repository.existsByNomeIgnoreCase(dto.getNome())) {
            throw new RegraNegocioException("Categoria já existe: " + dto.getNome());
        }
        Categoria c = new Categoria(null, dto.getNome());
        CategoriaDTO saved = toDTO(repository.save(c));
        log.info("Categoria criada: id={}, nome={}", saved.getId(), saved.getNome());
        return saved;
    }

    public CategoriaDTO atualizar(Long id, CategoriaDTO dto) {
        Categoria c = repository.findById(id)
                .orElseThrow(() -> new CategoriaNotFoundException(id));
        c.setNome(dto.getNome());
        CategoriaDTO saved = toDTO(repository.save(c));
        log.info("Categoria atualizada: id={}, nome={}", id, dto.getNome());
        return saved;
    }

    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new CategoriaNotFoundException(id);
        }
        try {
            repository.deleteById(id);
            log.info("Categoria excluida: id={}", id);
        } catch (DataIntegrityViolationException ex) {
            throw new RegraNegocioException("Não é possível excluir categoria com tarefas vinculadas.");
        }
    }

    Categoria findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new CategoriaNotFoundException(id));
    }

    private CategoriaDTO toDTO(Categoria c) {
        CategoriaDTO dto = new CategoriaDTO();
        dto.setId(c.getId());
        dto.setNome(c.getNome());
        return dto;
    }
}
