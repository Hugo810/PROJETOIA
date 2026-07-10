package com.taskflow.dto;

public class DashboardResumo {
    private long totalTarefas;
    private long pendentes;
    private long emExecucao;
    private long concluidas;
    private long atrasadas;
    private long criadasUltimos7Dias;
    private long concluidasUltimos7Dias;
    private long totalUsuarios;
    private long totalCategorias;

    public long getTotalTarefas() { return totalTarefas; }
    public void setTotalTarefas(long totalTarefas) { this.totalTarefas = totalTarefas; }
    public long getPendentes() { return pendentes; }
    public void setPendentes(long pendentes) { this.pendentes = pendentes; }
    public long getEmExecucao() { return emExecucao; }
    public void setEmExecucao(long emExecucao) { this.emExecucao = emExecucao; }
    public long getConcluidas() { return concluidas; }
    public void setConcluidas(long concluidas) { this.concluidas = concluidas; }
    public long getAtrasadas() { return atrasadas; }
    public void setAtrasadas(long atrasadas) { this.atrasadas = atrasadas; }
    public long getCriadasUltimos7Dias() { return criadasUltimos7Dias; }
    public void setCriadasUltimos7Dias(long criadasUltimos7Dias) { this.criadasUltimos7Dias = criadasUltimos7Dias; }
    public long getConcluidasUltimos7Dias() { return concluidasUltimos7Dias; }
    public void setConcluidasUltimos7Dias(long concluidasUltimos7Dias) { this.concluidasUltimos7Dias = concluidasUltimos7Dias; }
    public long getTotalUsuarios() { return totalUsuarios; }
    public void setTotalUsuarios(long totalUsuarios) { this.totalUsuarios = totalUsuarios; }
    public long getTotalCategorias() { return totalCategorias; }
    public void setTotalCategorias(long totalCategorias) { this.totalCategorias = totalCategorias; }
}
