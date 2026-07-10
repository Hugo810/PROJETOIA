package com.taskflow.dto;

import java.time.LocalDate;

public class GanttTarefa {
    private Long id;
    private String titulo;
    private LocalDate prazo;
    private long duracaoDias;
    private String status;
    private String prioridade;
    private String responsavel;
    private String categoria;
    private Long responsavelId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public LocalDate getPrazo() { return prazo; }
    public void setPrazo(LocalDate prazo) { this.prazo = prazo; }
    public long getDuracaoDias() { return duracaoDias; }
    public void setDuracaoDias(long duracaoDias) { this.duracaoDias = duracaoDias; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPrioridade() { return prioridade; }
    public void setPrioridade(String prioridade) { this.prioridade = prioridade; }
    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public Long getResponsavelId() { return responsavelId; }
    public void setResponsavelId(Long responsavelId) { this.responsavelId = responsavelId; }
}
