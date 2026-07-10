package com.taskflow.service;

import com.taskflow.dto.HistoricoDTO;
import com.taskflow.model.HistoricoAlteracao;
import com.taskflow.model.Tarefa;
import com.taskflow.model.Usuario;
import com.taskflow.repository.HistoricoAlteracaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoricoService {

    private static final Logger log = LoggerFactory.getLogger(HistoricoService.class);

    private final HistoricoAlteracaoRepository historicoRepository;

    public HistoricoService(HistoricoAlteracaoRepository historicoRepository) {
        this.historicoRepository = historicoRepository;
    }

    public List<HistoricoDTO> listarPorTarefa(Long tarefaId) {
        return historicoRepository.findByTarefaIdOrderByDataAlteracaoDesc(tarefaId)
                .stream().map(this::toDTO).toList();
    }

    public void registrar(String campo, String valorAnterior, String valorNovo, Tarefa tarefa, Usuario usuario) {
        HistoricoAlteracao h = new HistoricoAlteracao();
        h.setCampo(campo);
        h.setValorAnterior(valorAnterior);
        h.setValorNovo(valorNovo);
        h.setTarefa(tarefa);
        h.setUsuario(usuario);
        historicoRepository.save(h);
        log.info("Historico registrado para tarefa {}: {} de '{}' para '{}'", tarefa.getId(), campo, valorAnterior, valorNovo);
    }

    private HistoricoDTO toDTO(HistoricoAlteracao h) {
        HistoricoDTO dto = new HistoricoDTO();
        dto.setId(h.getId());
        dto.setTarefaId(h.getTarefa().getId());
        dto.setTarefaTitulo(h.getTarefa().getTitulo());
        dto.setUsuarioId(h.getUsuario().getId());
        dto.setUsuarioNome(h.getUsuario().getNome());
        dto.setCampo(h.getCampo());
        dto.setValorAnterior(h.getValorAnterior());
        dto.setValorNovo(h.getValorNovo());
        dto.setDataAlteracao(h.getDataAlteracao());
        return dto;
    }
}
