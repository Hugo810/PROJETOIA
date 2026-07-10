package com.taskflow.config;

public class CategoriaNotFoundException extends RuntimeException {
    public CategoriaNotFoundException(Long id) {
        super("Categoria não encontrada: " + id);
    }

    public CategoriaNotFoundException(String nome) {
        super("Categoria não encontrada: " + nome);
    }
}
