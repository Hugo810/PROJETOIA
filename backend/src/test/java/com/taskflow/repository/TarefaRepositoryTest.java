package com.taskflow.repository;

import com.taskflow.model.Categoria;
import com.taskflow.model.Tarefa;
import com.taskflow.model.TarefaStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class TarefaRepositoryTest {

    @Autowired
    private TarefaRepository tarefaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Categoria categoria;
    private Tarefa tarefa1;
    private Tarefa tarefa2;
    private Tarefa tarefa3;

    @BeforeEach
    void setUp() {
        categoria = categoriaRepository.save(new Categoria(null, "TRABALHO"));
        Categoria pessoal = categoriaRepository.save(new Categoria(null, "PESSOAL"));

        tarefa1 = criarTarefa("Tarefa A", categoria, LocalDate.now().plusDays(3), TarefaStatus.PENDENTE);
        tarefa2 = criarTarefa("Tarefa B", categoria, LocalDate.now().plusDays(1), TarefaStatus.PENDENTE);
        tarefa3 = criarTarefa("Tarefa C", pessoal, LocalDate.now().plusDays(2), TarefaStatus.CONCLUIDA);

        tarefa1 = tarefaRepository.save(tarefa1);
        tarefa2 = tarefaRepository.save(tarefa2);
        tarefa3 = tarefaRepository.save(tarefa3);
    }

    @Test
    void deveListarTodasOrdenadasPorPrazo() {
        Page<Tarefa> page = tarefaRepository.findAllByOrderByPrazoAsc(PageRequest.of(0, 10));
        assertEquals(3, page.getContent().size());
        assertTrue(page.getContent().get(0).getPrazo().isBefore(page.getContent().get(1).getPrazo()));
    }

    @Test
    void deveFiltrarPorStatus() {
        Page<Tarefa> page = tarefaRepository.findByStatusOrderByPrazoAsc(TarefaStatus.PENDENTE, PageRequest.of(0, 10));
        assertEquals(2, page.getContent().size());
    }

    @Test
    void deveFiltrarPorCategoria() {
        Page<Tarefa> page = tarefaRepository.findByCategoriaIdOrderByPrazoAsc(categoria.getId(), PageRequest.of(0, 10));
        assertEquals(2, page.getContent().size());
    }

    @Test
    void deveFiltrarPorStatusECategoria() {
        Page<Tarefa> page = tarefaRepository.findByStatusAndCategoriaIdOrderByPrazoAsc(
                TarefaStatus.CONCLUIDA, categoria.getId(), PageRequest.of(0, 10));
        assertEquals(0, page.getContent().size());
    }

    @Test
    void deveCriarELerTarefa() {
        Tarefa nova = criarTarefa("Nova Tarefa", categoria, LocalDate.now().plusDays(5), TarefaStatus.PENDENTE);
        nova = tarefaRepository.save(nova);

        assertTrue(tarefaRepository.findById(nova.getId()).isPresent());
        assertEquals("Nova Tarefa", tarefaRepository.findById(nova.getId()).get().getTitulo());
    }

    @Test
    void deveAtualizarTarefa() {
        tarefa1.setTitulo("Tarefa Atualizada");
        tarefa1.setStatus(TarefaStatus.CONCLUIDA);
        tarefaRepository.save(tarefa1);

        Tarefa atualizada = tarefaRepository.findById(tarefa1.getId()).get();
        assertEquals("Tarefa Atualizada", atualizada.getTitulo());
        assertEquals(TarefaStatus.CONCLUIDA, atualizada.getStatus());
    }

    @Test
    void deveExcluirTarefa() {
        tarefaRepository.deleteById(tarefa1.getId());
        assertTrue(tarefaRepository.findById(tarefa1.getId()).isEmpty());
    }

    private Tarefa criarTarefa(String titulo, Categoria cat, LocalDate prazo, TarefaStatus status) {
        Tarefa t = new Tarefa();
        t.setTitulo(titulo);
        t.setDescricao("Descricao");
        t.setCategoria(cat);
        t.setPrazo(prazo);
        t.setStatus(status);
        return t;
    }
}
