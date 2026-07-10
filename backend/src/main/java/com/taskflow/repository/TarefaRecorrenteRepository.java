package com.taskflow.repository;

import com.taskflow.model.TarefaRecorrente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TarefaRecorrenteRepository extends JpaRepository<TarefaRecorrente, Long> {

    List<TarefaRecorrente> findByAtivaTrue();

    List<TarefaRecorrente> findByCriadorId(Long criadorId);

    List<TarefaRecorrente> findByProximaExecucaoLessThanEqualAndAtivaTrue(LocalDate data);

    @Query("SELECT tr FROM TarefaRecorrente tr WHERE tr.ativa = true AND tr.proximaExecucao <= :dataLimite ORDER BY tr.proximaExecucao ASC")
    List<TarefaRecorrente> findPendentesAte(LocalDate dataLimite);
}
