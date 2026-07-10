package com.taskflow.dto;

import com.taskflow.model.Recorrencia;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TarefaRecorrenteDTO {

    private Long id;
    private Long tarefaModeloId;
    private String tarefaModeloTitulo;
    private Long categoriaId;
    private String categoriaNome;
    private String descricao;
    private Recorrencia recorrencia;
    private String configuracao;
    private LocalDate proximaExecucao;
    private Boolean ativa;
    private LocalDateTime dataCriacao;
    private Long criadorId;
    private String criadorNome;
    private List<LocalDate> proximasExecucoes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTarefaModeloId() { return tarefaModeloId; }
    public void setTarefaModeloId(Long tarefaModeloId) { this.tarefaModeloId = tarefaModeloId; }
    public String getTarefaModeloTitulo() { return tarefaModeloTitulo; }
    public void setTarefaModeloTitulo(String tarefaModeloTitulo) { this.tarefaModeloTitulo = tarefaModeloTitulo; }
    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }
    public String getCategoriaNome() { return categoriaNome; }
    public void setCategoriaNome(String categoriaNome) { this.categoriaNome = categoriaNome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public Recorrencia getRecorrencia() { return recorrencia; }
    public void setRecorrencia(Recorrencia recorrencia) { this.recorrencia = recorrencia; }
    public String getConfiguracao() { return configuracao; }
    public void setConfiguracao(String configuracao) { this.configuracao = configuracao; }
    public LocalDate getProximaExecucao() { return proximaExecucao; }
    public void setProximaExecucao(LocalDate proximaExecucao) { this.proximaExecucao = proximaExecucao; }
    public Boolean getAtiva() { return ativa; }
    public void setAtiva(Boolean ativa) { this.ativa = ativa; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public Long getCriadorId() { return criadorId; }
    public void setCriadorId(Long criadorId) { this.criadorId = criadorId; }
    public String getCriadorNome() { return criadorNome; }
    public void setCriadorNome(String criadorNome) { this.criadorNome = criadorNome; }
    public List<LocalDate> getProximasExecucoes() { return proximasExecucoes; }
    public void setProximasExecucoes(List<LocalDate> proximasExecucoes) { this.proximasExecucoes = proximasExecucoes; }
}
