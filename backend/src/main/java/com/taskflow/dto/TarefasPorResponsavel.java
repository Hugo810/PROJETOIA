package com.taskflow.dto;

public class TarefasPorResponsavel {
    private Long responsavelId;
    private String responsavel;
    private long total;
    private long concluidas;
    private double taxaConclusao;

    public TarefasPorResponsavel() {}

    public TarefasPorResponsavel(Long responsavelId, String responsavel, long total, long concluidas) {
        this.responsavelId = responsavelId;
        this.responsavel = responsavel;
        this.total = total;
        this.concluidas = concluidas;
        this.taxaConclusao = total > 0 ? Math.round(concluidas * 100.0 / total) : 0;
    }

    public Long getResponsavelId() { return responsavelId; }
    public void setResponsavelId(Long responsavelId) { this.responsavelId = responsavelId; }
    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public long getConcluidas() { return concluidas; }
    public void setConcluidas(long concluidas) { this.concluidas = concluidas; }
    public double getTaxaConclusao() { return taxaConclusao; }
    public void setTaxaConclusao(double taxaConclusao) { this.taxaConclusao = taxaConclusao; }
}
