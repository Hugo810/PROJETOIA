package com.taskflow.config;

public class TarefaNotFoundException extends RuntimeException {
    public TarefaNotFoundException(Long id) {
        super("Tarefa não encontrada: " + id);
    }
}
