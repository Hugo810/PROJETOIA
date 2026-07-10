package com.taskflow.service;

import com.taskflow.dto.MetaDTO;
import com.taskflow.dto.ProjetoDTO;
import com.taskflow.model.*;
import com.taskflow.repository.MetaRepository;
import com.taskflow.repository.ProjetoRepository;
import com.taskflow.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjetoServiceTest {

    @Mock private ProjetoRepository projetoRepository;
    @Mock private MetaRepository metaRepository;
    @Mock private UsuarioRepository usuarioRepository;

    private ProjetoService service;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        service = new ProjetoService(projetoRepository, metaRepository, usuarioRepository);
        usuario = new Usuario(1L, "Joao", "joao@test.com", "123", UsuarioRole.EXECUTOR);
    }

    @Test
    void deveCriarProjeto() {
        ProjetoDTO dto = new ProjetoDTO();
        dto.setNome("Projeto Alpha");
        dto.setDescricao("Descricao");
        dto.setDataInicio(LocalDate.now());
        dto.setDataFim(LocalDate.now().plusDays(30));

        when(projetoRepository.save(any())).thenAnswer(i -> {
            Projeto p = i.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(metaRepository.findByProjetoIdOrderByDataInicioAsc(1L)).thenReturn(List.of());

        ProjetoDTO resultado = service.criar(dto, 1L);

        assertEquals("Projeto Alpha", resultado.getNome());
        assertEquals("PLANEJADO", resultado.getStatus());
        assertEquals(0L, resultado.getTotalMetas());
    }

    @Test
    void deveCriarProjetoComResponsavel() {
        ProjetoDTO dto = new ProjetoDTO();
        dto.setNome("Projeto Beta");
        dto.setResponsavelId(1L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(projetoRepository.save(any())).thenAnswer(i -> {
            Projeto p = i.getArgument(0);
            p.setId(2L);
            return p;
        });
        when(metaRepository.findByProjetoIdOrderByDataInicioAsc(2L)).thenReturn(List.of());

        ProjetoDTO resultado = service.criar(dto, 1L);

        assertEquals(1L, resultado.getResponsavelId());
        assertEquals("Joao", resultado.getResponsavelNome());
    }

    @Test
    void deveListarTodos() {
        Projeto p = new Projeto();
        p.setId(1L);
        p.setNome("Projeto");
        p.setStatus(ProjetoStatus.EM_ANDAMENTO);
        when(projetoRepository.findAll()).thenReturn(List.of(p));
        when(metaRepository.findByProjetoIdOrderByDataInicioAsc(1L)).thenReturn(List.of());

        List<ProjetoDTO> resultado = service.listarTodos();

        assertEquals(1, resultado.size());
    }

    @Test
    void deveAtualizarStatus() {
        Projeto p = new Projeto();
        p.setId(1L);
        p.setNome("Projeto");
        p.setStatus(ProjetoStatus.PLANEJADO);
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(projetoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(metaRepository.findByProjetoIdOrderByDataInicioAsc(1L)).thenReturn(List.of());

        ProjetoDTO dto = new ProjetoDTO();
        dto.setStatus("EM_ANDAMENTO");

        ProjetoDTO resultado = service.atualizar(1L, dto);

        assertEquals("EM_ANDAMENTO", resultado.getStatus());
    }

    @Test
    void deveExcluir() {
        service.excluir(1L);
        verify(projetoRepository).deleteById(1L);
    }

    @Test
    void deveCriarMeta() {
        Projeto p = new Projeto();
        p.setId(1L);
        p.setNome("Projeto");
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(p));

        MetaDTO dto = new MetaDTO();
        dto.setTitulo("Meta 1");
        dto.setProgresso(0.5);

        when(metaRepository.save(any())).thenAnswer(i -> {
            Meta m = i.getArgument(0);
            m.setId(1L);
            return m;
        });

        MetaDTO resultado = service.criarMeta(1L, dto);

        assertEquals("Meta 1", resultado.getTitulo());
        assertEquals(0.5, resultado.getProgresso());
        assertEquals(1L, resultado.getProjetoId());
    }

    @Test
    void deveAtualizarMeta() {
        Meta m = new Meta();
        m.setId(1L);
        m.setTitulo("Meta antiga");
        m.setStatus(MetaStatus.PENDENTE);
        when(metaRepository.findById(1L)).thenReturn(Optional.of(m));
        when(metaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        MetaDTO dto = new MetaDTO();
        dto.setTitulo("Meta nova");
        dto.setStatus("EM_ANDAMENTO");
        dto.setProgresso(0.3);

        MetaDTO resultado = service.atualizarMeta(1L, dto);

        assertEquals("Meta nova", resultado.getTitulo());
        assertEquals("EM_ANDAMENTO", resultado.getStatus());
        assertEquals(0.3, resultado.getProgresso());
    }

    @Test
    void deveExcluirMeta() {
        service.excluirMeta(1L);
        verify(metaRepository).deleteById(1L);
    }
}
