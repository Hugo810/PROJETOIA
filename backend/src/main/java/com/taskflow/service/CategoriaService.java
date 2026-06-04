package com.taskflow.service;

import com.taskflow.dto.CategoriaDTO;
import com.taskflow.model.Categoria;
import com.taskflow.repository.CategoriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
                .orElseThrow(() -> new RuntimeException("Categoria nao encontrada: " + id)));
    }

    public CategoriaDTO criar(CategoriaDTO dto) {
        if (repository.existsByNomeIgnoreCase(dto.getNome())) {
            throw new RuntimeException("Categoria ja existe: " + dto.getNome());
        }
        Categoria c = new Categoria(null, dto.getNome());
        CategoriaDTO saved = toDTO(repository.save(c));
        log.info("Categoria criada: id={}, nome={}", saved.getId(), saved.getNome());
        return saved;
    }

    public CategoriaDTO atualizar(Long id, CategoriaDTO dto) {
        Categoria c = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria nao encontrada: " + id));
        c.setNome(dto.getNome());
        CategoriaDTO saved = toDTO(repository.save(c));
        log.info("Categoria atualizada: id={}, nome={}", id, dto.getNome());
        return saved;
    }

    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Categoria nao encontrada: " + id);
        }
        repository.deleteById(id);
        log.info("Categoria excluida: id={}", id);
    }

    Categoria findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria nao encontrada: " + id));
    }

    private CategoriaDTO toDTO(Categoria c) {
        CategoriaDTO dto = new CategoriaDTO();
        dto.setId(c.getId());
        dto.setNome(c.getNome());
        return dto;
    }
}
