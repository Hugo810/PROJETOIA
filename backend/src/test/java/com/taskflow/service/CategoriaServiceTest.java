package com.taskflow.service;

import com.taskflow.dto.CategoriaDTO;
import com.taskflow.model.Categoria;
import com.taskflow.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository repository;

    private CategoriaService service;

    @BeforeEach
    void setUp() {
        service = new CategoriaService(repository);
    }

    @Test
    void deveListarTodasCategorias() {
        when(repository.findAll()).thenReturn(List.of(
                new Categoria(1L, "TRABALHO"),
                new Categoria(2L, "ESTUDOS")
        ));

        List<CategoriaDTO> resultado = service.listarTodas();

        assertEquals(2, resultado.size());
        assertEquals("TRABALHO", resultado.get(0).getNome());
    }

    @Test
    void deveBuscarCategoriaPorId() {
        when(repository.findById(1L)).thenReturn(Optional.of(new Categoria(1L, "URGENTE")));

        CategoriaDTO dto = service.buscarPorId(1L);

        assertEquals("URGENTE", dto.getNome());
    }

    @Test
    void deveLancarExcecaoQuandoCategoriaNaoEncontrada() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.buscarPorId(99L));
    }

    @Test
    void deveCriarCategoria() {
        when(repository.existsByNomeIgnoreCase("NOVA")).thenReturn(false);
        when(repository.save(any())).thenReturn(new Categoria(1L, "NOVA"));

        CategoriaDTO dto = new CategoriaDTO(null, "NOVA");
        CategoriaDTO resultado = service.criar(dto);

        assertEquals("NOVA", resultado.getNome());
        assertEquals(1L, resultado.getId());
    }

    @Test
    void deveLancarExcecaoAoCriarCategoriaDuplicada() {
        when(repository.existsByNomeIgnoreCase("TRABALHO")).thenReturn(true);

        CategoriaDTO dto = new CategoriaDTO(null, "TRABALHO");
        assertThrows(RuntimeException.class, () -> service.criar(dto));
    }

    @Test
    void deveAtualizarCategoria() {
        when(repository.findById(1L)).thenReturn(Optional.of(new Categoria(1L, "TRABALHO")));
        when(repository.save(any())).thenReturn(new Categoria(1L, "PROFISSIONAL"));

        CategoriaDTO dto = new CategoriaDTO(null, "PROFISSIONAL");
        CategoriaDTO resultado = service.atualizar(1L, dto);

        assertEquals("PROFISSIONAL", resultado.getNome());
    }

    @Test
    void deveExcluirCategoria() {
        when(repository.existsById(1L)).thenReturn(true);

        service.excluir(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void deveLancarExcecaoAoExcluirCategoriaInexistente() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> service.excluir(99L));
    }
}
