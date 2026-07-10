package com.taskflow.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ProjetoDTO {
    private Long id;
    private String nome;
    private String descricao;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String status;
    private Long responsavelId;
    private String responsavelNome;
    private LocalDateTime dataCriacao;
    private Long totalMetas;
    private Long metasConcluidas;
    private Double progressoGeral;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getResponsavelId() { return responsavelId; }
    public void setResponsavelId(Long responsavelId) { this.responsavelId = responsavelId; }
    public String getResponsavelNome() { return responsavelNome; }
    public void setResponsavelNome(String responsavelNome) { this.responsavelNome = responsavelNome; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public Long getTotalMetas() { return totalMetas; }
    public void setTotalMetas(Long totalMetas) { this.totalMetas = totalMetas; }
    public Long getMetasConcluidas() { return metasConcluidas; }
    public void setMetasConcluidas(Long metasConcluidas) { this.metasConcluidas = metasConcluidas; }
    public Double getProgressoGeral() { return progressoGeral; }
    public void setProgressoGeral(Double progressoGeral) { this.progressoGeral = progressoGeral; }
}
