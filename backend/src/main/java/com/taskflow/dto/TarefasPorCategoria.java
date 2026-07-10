package com.taskflow.dto;

public class TarefasPorCategoria {
    private Long categoriaId;
    private String categoria;
    private long contagem;

    public TarefasPorCategoria() {}

    public TarefasPorCategoria(Long categoriaId, String categoria, long contagem) {
        this.categoriaId = categoriaId;
        this.categoria = categoria;
        this.contagem = contagem;
    }

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public long getContagem() { return contagem; }
    public void setContagem(long contagem) { this.contagem = contagem; }
}
