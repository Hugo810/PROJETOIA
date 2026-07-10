package com.taskflow.service;

import com.taskflow.dto.*;
import com.taskflow.model.TarefaStatus;
import com.taskflow.repository.TarefaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private TarefaRepository tarefaRepository;
    private DashboardService service;

    @BeforeEach
    void setUp() {
        service = new DashboardService(tarefaRepository);
    }

    @Test
    void deveRetornarResumo() {
        when(tarefaRepository.count()).thenReturn(20L);
        when(tarefaRepository.countByStatus(TarefaStatus.PENDENTE)).thenReturn(8L);
        when(tarefaRepository.countByStatus(TarefaStatus.EM_EXECUCAO)).thenReturn(5L);
        when(tarefaRepository.countByStatus(TarefaStatus.CONCLUIDA)).thenReturn(7L);
        when(tarefaRepository.countAtrasadas(LocalDate.now())).thenReturn(3L);
        when(tarefaRepository.countCriadasDesde(any())).thenReturn(4L);
        when(tarefaRepository.countConcluidasDesde(any())).thenReturn(2L);

        DashboardResumo resumo = service.resumo();

        assertEquals(20, resumo.getTotalTarefas());
        assertEquals(8, resumo.getPendentes());
        assertEquals(5, resumo.getEmExecucao());
        assertEquals(7, resumo.getConcluidas());
        assertEquals(3, resumo.getAtrasadas());
        assertEquals(4, resumo.getCriadasUltimos7Dias());
        assertEquals(2, resumo.getConcluidasUltimos7Dias());
    }

    @Test
    void deveRetornarPorStatus() {
        when(tarefaRepository.countByStatus(any())).thenReturn(0L);
        when(tarefaRepository.countByStatus(TarefaStatus.PENDENTE)).thenReturn(5L);
        when(tarefaRepository.countByStatus(TarefaStatus.EM_EXECUCAO)).thenReturn(3L);

        List<TarefasPorStatus> resultado = service.porStatus();

        assertEquals(4, resultado.size());
        assertEquals(5L, resultado.stream().filter(r -> r.getStatus().equals("PENDENTE")).findFirst().get().getContagem());
    }

    @Test
    void deveRetornarPorCategoria() {
        Object[] row1 = {1L, "Trabalho", 10L};
        Object[] row2 = {2L, "Pessoal", 5L};
        when(tarefaRepository.countGroupByCategoria()).thenReturn(List.of(row1, row2));

        List<TarefasPorCategoria> resultado = service.porCategoria();

        assertEquals(2, resultado.size());
        assertEquals("Trabalho", resultado.get(0).getCategoria());
        assertEquals(10, resultado.get(0).getContagem());
    }

    @Test
    void deveRetornarPorResponsavel() {
        Object[] row1 = {1L, "Joao", 12L, 8L};
        Object[] row2 = {2L, "Maria", 7L, 5L};
        when(tarefaRepository.countGroupByResponsavel()).thenReturn(List.of(row1, row2));

        List<TarefasPorResponsavel> resultado = service.porResponsavel();

        assertEquals(2, resultado.size());
        assertEquals("Joao", resultado.get(0).getResponsavel());
        assertEquals(8, resultado.get(0).getConcluidas());
        assertTrue(resultado.get(0).getTaxaConclusao() > 0);
    }

    @Test
    void deveRetornarTendencia() {
        Object[] row1 = {LocalDate.now().minusDays(2), 3L};
        Object[] row2 = {LocalDate.now(), 2L};
        when(tarefaRepository.countCriadasPorDia(any())).thenReturn(List.of(row1, row2));
        when(tarefaRepository.countConcluidasPorDia(any())).thenReturn(List.of());

        List<Tendencia> resultado = service.tendencia();

        assertTrue(resultado.size() >= 1);
    }

    @Test
    void deveOrdenarPorCategoriaDesc() {
        Object[] row1 = {1L, "Pessoal", 3L};
        Object[] row2 = {2L, "Trabalho", 15L};
        when(tarefaRepository.countGroupByCategoria()).thenReturn(List.of(row1, row2));

        List<TarefasPorCategoria> resultado = service.porCategoria();

        assertEquals("Trabalho", resultado.get(0).getCategoria());
    }
}
