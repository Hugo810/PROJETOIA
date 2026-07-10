package com.taskflow.repository;

import com.taskflow.model.Tarefa;
import com.taskflow.model.TarefaStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TarefaRepository extends JpaRepository<Tarefa, Long>, JpaSpecificationExecutor<Tarefa> {

    Page<Tarefa> findByStatusOrderByPrazoAsc(TarefaStatus status, Pageable pageable);

    Page<Tarefa> findByCategoriaIdOrderByPrazoAsc(Long categoriaId, Pageable pageable);

    Page<Tarefa> findByStatusAndCategoriaIdOrderByPrazoAsc(TarefaStatus status, Long categoriaId, Pageable pageable);

    Page<Tarefa> findByResponsavelIdOrderByPrazoAsc(Long responsavelId, Pageable pageable);

    Page<Tarefa> findByStatusAndResponsavelIdOrderByPrazoAsc(TarefaStatus status, Long responsavelId, Pageable pageable);

    Page<Tarefa> findByCategoriaIdAndResponsavelIdOrderByPrazoAsc(Long categoriaId, Long responsavelId, Pageable pageable);

    Page<Tarefa> findByStatusAndCategoriaIdAndResponsavelIdOrderByPrazoAsc(TarefaStatus status, Long categoriaId, Long responsavelId, Pageable pageable);

    Page<Tarefa> findAllByOrderByPrazoAsc(Pageable pageable);

    @Query("SELECT t FROM Tarefa t WHERE t.prazo = :data AND t.status <> com.taskflow.model.TarefaStatus.CONCLUIDA ORDER BY t.prioridade DESC, t.prazo ASC")
    List<Tarefa> findByPrazo(@Param("data") LocalDate data);

    @Query("SELECT t FROM Tarefa t WHERE t.prazo BETWEEN :inicio AND :fim AND t.status <> com.taskflow.model.TarefaStatus.CONCLUIDA ORDER BY t.prazo ASC, t.prioridade DESC")
    List<Tarefa> findByPrazoBetween(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT t FROM Tarefa t WHERE t.prazo < :data AND t.status <> com.taskflow.model.TarefaStatus.CONCLUIDA AND t.status <> com.taskflow.model.TarefaStatus.ARQUIVADA ORDER BY t.prazo ASC, t.prioridade DESC")
    List<Tarefa> findAtrasadas(@Param("data") LocalDate data);

    @Query("SELECT t FROM Tarefa t WHERE t.prazo >= :hoje AND t.prazo <= :limite AND t.status <> com.taskflow.model.TarefaStatus.CONCLUIDA ORDER BY t.prazo ASC, t.prioridade DESC")
    List<Tarefa> findProximas(@Param("hoje") LocalDate hoje, @Param("limite") LocalDate limite);

    @Query("SELECT COUNT(t) FROM Tarefa t WHERE t.prazo = :data AND t.status <> com.taskflow.model.TarefaStatus.CONCLUIDA")
    long countByPrazo(@Param("data") LocalDate data);

    @Query("SELECT COUNT(t) FROM Tarefa t WHERE t.prazo < :data AND t.status <> com.taskflow.model.TarefaStatus.CONCLUIDA AND t.status <> com.taskflow.model.TarefaStatus.ARQUIVADA")
    long countAtrasadas(@Param("data") LocalDate data);

    @Query("SELECT COUNT(t) FROM Tarefa t WHERE t.prazo >= :hoje AND t.prazo <= :limite AND t.status <> com.taskflow.model.TarefaStatus.CONCLUIDA")
    long countProximas(@Param("hoje") LocalDate hoje, @Param("limite") LocalDate limite);

    @Query("SELECT COUNT(t) FROM Tarefa t WHERE t.status = :status")
    long countByStatus(@Param("status") com.taskflow.model.TarefaStatus status);

    @Query("SELECT COUNT(t) FROM Tarefa t WHERE t.dataCriacao >= :data")
    long countCriadasDesde(@Param("data") java.time.LocalDateTime data);

    @Query("SELECT COUNT(t) FROM Tarefa t WHERE t.dataConclusao >= :data")
    long countConcluidasDesde(@Param("data") java.time.LocalDateTime data);

    @Query("SELECT t.categoria.id, t.categoria.nome, COUNT(t) FROM Tarefa t GROUP BY t.categoria.id, t.categoria.nome")
    java.util.List<Object[]> countGroupByCategoria();

    @Query("SELECT t.responsavel.id, t.responsavel.nome, COUNT(t), " +
           "SUM(CASE WHEN t.status = com.taskflow.model.TarefaStatus.CONCLUIDA THEN 1 ELSE 0 END) " +
           "FROM Tarefa t WHERE t.responsavel IS NOT NULL GROUP BY t.responsavel.id, t.responsavel.nome")
    java.util.List<Object[]> countGroupByResponsavel();

    @Query("SELECT CAST(t.dataCriacao AS LocalDate), COUNT(t) FROM Tarefa t WHERE t.dataCriacao >= :inicio GROUP BY CAST(t.dataCriacao AS LocalDate) ORDER BY CAST(t.dataCriacao AS LocalDate)")
    java.util.List<Object[]> countCriadasPorDia(@Param("inicio") java.time.LocalDateTime inicio);

    @Query("SELECT CAST(t.dataConclusao AS LocalDate), COUNT(t) FROM Tarefa t WHERE t.dataConclusao >= :inicio GROUP BY CAST(t.dataConclusao AS LocalDate) ORDER BY CAST(t.dataConclusao AS LocalDate)")
    java.util.List<Object[]> countConcluidasPorDia(@Param("inicio") java.time.LocalDateTime inicio);
}
