package com.taskflow.service;

import com.taskflow.dto.*;
import com.taskflow.model.TarefaStatus;
import com.taskflow.repository.TarefaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final TarefaRepository tarefaRepository;

    public DashboardService(TarefaRepository tarefaRepository) {
        this.tarefaRepository = tarefaRepository;
    }

    public DashboardResumo resumo() {
        DashboardResumo dto = new DashboardResumo();
        dto.setTotalTarefas(tarefaRepository.count());
        dto.setPendentes(tarefaRepository.countByStatus(TarefaStatus.PENDENTE));
        dto.setEmExecucao(tarefaRepository.countByStatus(TarefaStatus.EM_EXECUCAO));
        dto.setConcluidas(tarefaRepository.countByStatus(TarefaStatus.CONCLUIDA));
        dto.setAtrasadas(tarefaRepository.countAtrasadas(LocalDate.now()));
        dto.setCriadasUltimos7Dias(tarefaRepository.countCriadasDesde(LocalDateTime.now().minusDays(7)));
        dto.setConcluidasUltimos7Dias(tarefaRepository.countConcluidasDesde(LocalDateTime.now().minusDays(7)));
        return dto;
    }

    public List<TarefasPorStatus> porStatus() {
        return Arrays.stream(TarefaStatus.values())
                .map(s -> new TarefasPorStatus(s.name(), tarefaRepository.countByStatus(s)))
                .collect(Collectors.toList());
    }

    public List<TarefasPorCategoria> porCategoria() {
        return tarefaRepository.countGroupByCategoria().stream()
                .map(row -> new TarefasPorCategoria(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue()))
                .sorted(Comparator.comparingLong(TarefasPorCategoria::getContagem).reversed())
                .collect(Collectors.toList());
    }

    public List<TarefasPorResponsavel> porResponsavel() {
        return tarefaRepository.countGroupByResponsavel().stream()
                .map(row -> new TarefasPorResponsavel(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).longValue()))
                .sorted(Comparator.comparingLong(TarefasPorResponsavel::getTotal).reversed())
                .collect(Collectors.toList());
    }

    public List<Tendencia> tendencia() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(30);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");

        Map<LocalDate, Long> criadasMap = tarefaRepository.countCriadasPorDia(inicio).stream()
                .collect(Collectors.toMap(
                        row -> (LocalDate) row[0],
                        row -> ((Number) row[1]).longValue()));

        Map<LocalDate, Long> concluidasMap = tarefaRepository.countConcluidasPorDia(inicio).stream()
                .collect(Collectors.toMap(
                        row -> (LocalDate) row[0],
                        row -> ((Number) row[1]).longValue()));

        List<Tendencia> tendencia = new ArrayList<>();
        for (int i = 30; i >= 0; i--) {
            LocalDate dia = LocalDate.now().minusDays(i);
            long criadas = criadasMap.getOrDefault(dia, 0L);
            long concluidas = concluidasMap.getOrDefault(dia, 0L);
            if (criadas > 0 || concluidas > 0) {
                tendencia.add(new Tendencia(dia.format(fmt), criadas, concluidas));
            }
        }
        return tendencia;
    }
}
