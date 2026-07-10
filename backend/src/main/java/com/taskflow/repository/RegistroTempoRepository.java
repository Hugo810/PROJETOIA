package com.taskflow.repository;

import com.taskflow.model.RegistroTempo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistroTempoRepository extends JpaRepository<RegistroTempo, Long> {

    List<RegistroTempo> findByTarefaIdOrderByInicioDesc(Long tarefaId);

    Optional<RegistroTempo> findByTarefaIdAndEmAndamentoTrue(Long tarefaId);

    Optional<RegistroTempo> findByUsuarioIdAndEmAndamentoTrue(Long usuarioId);

    @Query("SELECT COALESCE(SUM(r.duracaoMinutos), 0) FROM RegistroTempo r WHERE r.tarefa.id = :tarefaId AND r.duracaoMinutos IS NOT NULL")
    long sumDuracaoByTarefaId(@Param("tarefaId") Long tarefaId);

    @Query("SELECT COALESCE(SUM(r.duracaoMinutos), 0) FROM RegistroTempo r WHERE r.usuario.id = :usuarioId AND r.duracaoMinutos IS NOT NULL AND r.inicio BETWEEN :inicio AND :fim")
    long sumDuracaoByUsuarioAndPeriodo(@Param("usuarioId") Long usuarioId, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT r FROM RegistroTempo r WHERE r.usuario.id = :usuarioId AND r.inicio BETWEEN :inicio AND :fim ORDER BY r.inicio DESC")
    List<RegistroTempo> findByUsuarioAndPeriodo(@Param("usuarioId") Long usuarioId, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    List<RegistroTempo> findByUsuarioIdOrderByInicioDesc(Long usuarioId);
}
