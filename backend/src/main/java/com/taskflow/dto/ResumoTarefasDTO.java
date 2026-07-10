package com.taskflow.dto;

import java.util.List;

public class ResumoTarefasDTO {

    private List<TarefaDTO> hoje;
    private List<TarefaDTO> semana;
    private List<TarefaDTO> atrasadas;
    private List<TarefaDTO> proximas;
    private long contagemHoje;
    private long contagemSemana;
    private long contagemAtrasadas;
    private long contagemProximas;

    public ResumoTarefasDTO() {}

    public List<TarefaDTO> getHoje() { return hoje; }
    public void setHoje(List<TarefaDTO> hoje) { this.hoje = hoje; }
    public List<TarefaDTO> getSemana() { return semana; }
    public void setSemana(List<TarefaDTO> semana) { this.semana = semana; }
    public List<TarefaDTO> getAtrasadas() { return atrasadas; }
    public void setAtrasadas(List<TarefaDTO> atrasadas) { this.atrasadas = atrasadas; }
    public List<TarefaDTO> getProximas() { return proximas; }
    public void setProximas(List<TarefaDTO> proximas) { this.proximas = proximas; }
    public long getContagemHoje() { return contagemHoje; }
    public void setContagemHoje(long contagemHoje) { this.contagemHoje = contagemHoje; }
    public long getContagemSemana() { return contagemSemana; }
    public void setContagemSemana(long contagemSemana) { this.contagemSemana = contagemSemana; }
    public long getContagemAtrasadas() { return contagemAtrasadas; }
    public void setContagemAtrasadas(long contagemAtrasadas) { this.contagemAtrasadas = contagemAtrasadas; }
    public long getContagemProximas() { return contagemProximas; }
    public void setContagemProximas(long contagemProximas) { this.contagemProximas = contagemProximas; }
}
