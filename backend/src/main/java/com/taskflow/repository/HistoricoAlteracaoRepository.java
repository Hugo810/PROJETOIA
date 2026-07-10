package com.taskflow.repository;

import com.taskflow.model.HistoricoAlteracao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoAlteracaoRepository extends JpaRepository<HistoricoAlteracao, Long> {
    List<HistoricoAlteracao> findByTarefaIdOrderByDataAlteracaoDesc(Long tarefaId);
}
