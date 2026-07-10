package com.taskflow.service;

import com.taskflow.dto.MetaDTO;
import com.taskflow.dto.ProjetoDTO;
import com.taskflow.model.*;
import com.taskflow.repository.MetaRepository;
import com.taskflow.repository.ProjetoRepository;
import com.taskflow.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjetoService {

    private static final Logger log = LoggerFactory.getLogger(ProjetoService.class);

    private final ProjetoRepository projetoRepository;
    private final MetaRepository metaRepository;
    private final UsuarioRepository usuarioRepository;

    public ProjetoService(ProjetoRepository projetoRepository, MetaRepository metaRepository,
                          UsuarioRepository usuarioRepository) {
        this.projetoRepository = projetoRepository;
        this.metaRepository = metaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjetoDTO> listarTodos() {
        return projetoRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjetoDTO buscarPorId(Long id) {
        Projeto p = projetoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado com id: " + id));
        return toDTO(p);
    }

    @Transactional
    public ProjetoDTO criar(ProjetoDTO dto, Long criadorId) {
        Projeto projeto = new Projeto();
        projeto.setNome(dto.getNome());
        projeto.setDescricao(dto.getDescricao());
        projeto.setDataInicio(dto.getDataInicio());
        projeto.setDataFim(dto.getDataFim());
        projeto.setStatus(ProjetoStatus.PLANEJADO);

        if (dto.getResponsavelId() != null) {
            Usuario resp = usuarioRepository.findById(dto.getResponsavelId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            projeto.setResponsavel(resp);
        }

        Projeto salvo = projetoRepository.save(projeto);
        log.info("Projeto criado: id={}, nome={}", salvo.getId(), dto.getNome());
        return toDTO(salvo);
    }

    @Transactional
    public ProjetoDTO atualizar(Long id, ProjetoDTO dto) {
        Projeto projeto = projetoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado com id: " + id));

        if (dto.getNome() != null) projeto.setNome(dto.getNome());
        if (dto.getDescricao() != null) projeto.setDescricao(dto.getDescricao());
        if (dto.getDataInicio() != null) projeto.setDataInicio(dto.getDataInicio());
        if (dto.getDataFim() != null) projeto.setDataFim(dto.getDataFim());
        if (dto.getStatus() != null) projeto.setStatus(ProjetoStatus.valueOf(dto.getStatus()));

        if (dto.getResponsavelId() != null) {
            Usuario resp = usuarioRepository.findById(dto.getResponsavelId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            projeto.setResponsavel(resp);
        }

        Projeto atualizado = projetoRepository.save(projeto);
        return toDTO(atualizado);
    }

    @Transactional
    public void excluir(Long id) {
        projetoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<MetaDTO> listarMetas(Long projetoId) {
        return metaRepository.findByProjetoIdOrderByDataInicioAsc(projetoId).stream()
                .map(this::toMetaDTO).collect(Collectors.toList());
    }

    @Transactional
    public MetaDTO criarMeta(Long projetoId, MetaDTO dto) {
        Projeto projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado com id: " + projetoId));

        Meta meta = new Meta();
        meta.setTitulo(dto.getTitulo());
        meta.setDescricao(dto.getDescricao());
        meta.setDataInicio(dto.getDataInicio());
        meta.setDataFim(dto.getDataFim());
        meta.setProgresso(dto.getProgresso() != null ? dto.getProgresso() : 0.0);
        meta.setStatus(MetaStatus.PENDENTE);
        meta.setProjeto(projeto);

        Meta salva = metaRepository.save(meta);
        log.info("Meta criada: id={}, projeto={}", salva.getId(), projetoId);
        return toMetaDTO(salva);
    }

    @Transactional
    public MetaDTO atualizarMeta(Long metaId, MetaDTO dto) {
        Meta meta = metaRepository.findById(metaId)
                .orElseThrow(() -> new RuntimeException("Meta não encontrada com id: " + metaId));

        if (dto.getTitulo() != null) meta.setTitulo(dto.getTitulo());
        if (dto.getDescricao() != null) meta.setDescricao(dto.getDescricao());
        if (dto.getDataInicio() != null) meta.setDataInicio(dto.getDataInicio());
        if (dto.getDataFim() != null) meta.setDataFim(dto.getDataFim());
        if (dto.getStatus() != null) meta.setStatus(MetaStatus.valueOf(dto.getStatus()));
        if (dto.getProgresso() != null) meta.setProgresso(dto.getProgresso());

        Meta atualizada = metaRepository.save(meta);
        return toMetaDTO(atualizada);
    }

    @Transactional
    public void excluirMeta(Long metaId) {
        metaRepository.deleteById(metaId);
    }

    private ProjetoDTO toDTO(Projeto p) {
        ProjetoDTO dto = new ProjetoDTO();
        dto.setId(p.getId());
        dto.setNome(p.getNome());
        dto.setDescricao(p.getDescricao());
        dto.setDataInicio(p.getDataInicio());
        dto.setDataFim(p.getDataFim());
        dto.setStatus(p.getStatus().name());
        dto.setDataCriacao(p.getDataCriacao());

        if (p.getResponsavel() != null) {
            dto.setResponsavelId(p.getResponsavel().getId());
            dto.setResponsavelNome(p.getResponsavel().getNome());
        }

        List<Meta> metas = metaRepository.findByProjetoIdOrderByDataInicioAsc(p.getId());
        dto.setTotalMetas((long) metas.size());
        dto.setMetasConcluidas(metas.stream().filter(m -> m.getStatus() == MetaStatus.CONCLUIDA).count());
        double progresso = metas.isEmpty() ? 0 :
                metas.stream().mapToDouble(m -> m.getProgresso() != null ? m.getProgresso() : 0).average().orElse(0);
        dto.setProgressoGeral(Math.round(progresso * 100.0) / 100.0);

        return dto;
    }

    private MetaDTO toMetaDTO(Meta m) {
        MetaDTO dto = new MetaDTO();
        dto.setId(m.getId());
        dto.setTitulo(m.getTitulo());
        dto.setDescricao(m.getDescricao());
        dto.setDataInicio(m.getDataInicio());
        dto.setDataFim(m.getDataFim());
        dto.setStatus(m.getStatus().name());
        dto.setProgresso(m.getProgresso());
        if (m.getProjeto() != null) {
            dto.setProjetoId(m.getProjeto().getId());
            dto.setProjetoNome(m.getProjeto().getNome());
        }
        return dto;
    }
}
