package com.taskflow.repository;

import com.taskflow.model.RegraAutomacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegraAutomacaoRepository extends JpaRepository<RegraAutomacao, Long> {
    List<RegraAutomacao> findByAtivaTrue();
    List<RegraAutomacao> findByCriadorId(Long criadorId);
}
