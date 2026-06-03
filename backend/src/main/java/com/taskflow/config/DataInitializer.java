package com.taskflow.config;

import com.taskflow.model.Categoria;
import com.taskflow.model.Tarefa;
import com.taskflow.model.TarefaStatus;
import com.taskflow.model.Usuario;
import com.taskflow.model.UsuarioRole;
import com.taskflow.repository.CategoriaRepository;
import com.taskflow.repository.TarefaRepository;
import com.taskflow.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TarefaRepository tarefaRepository;

    public DataInitializer(CategoriaRepository categoriaRepository, UsuarioRepository usuarioRepository, TarefaRepository tarefaRepository) {
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.tarefaRepository = tarefaRepository;
    }

    @Override
    public void run(String... args) {
        if (categoriaRepository.count() == 0) {
            categoriaRepository.save(new Categoria(null, "TRABALHO"));
            categoriaRepository.save(new Categoria(null, "ESTUDOS"));
            categoriaRepository.save(new Categoria(null, "PESSOAL"));
            categoriaRepository.save(new Categoria(null, "URGENTE"));
            categoriaRepository.save(new Categoria(null, "SAUDE"));
            categoriaRepository.save(new Categoria(null, "FINANCEIRO"));
        }

        if (usuarioRepository.count() == 0) {
            usuarioRepository.save(new Usuario(null, "Admin", "admin@taskflow.com", "admin", UsuarioRole.ADMIN));
            usuarioRepository.save(new Usuario(null, "Distribuidor", "dist@taskflow.com", "123456", UsuarioRole.DISTRIBUIDOR));
            usuarioRepository.save(new Usuario(null, "Executor", "executor@taskflow.com", "123456", UsuarioRole.EXECUTOR));
        }

        if (tarefaRepository.count() == 0) {
            List<Categoria> cats = categoriaRepository.findAll();
            List<Usuario> users = usuarioRepository.findAll();

            Categoria trabalho = cats.stream().filter(c -> c.getNome().equals("TRABALHO")).findFirst().get();
            Categoria estudos = cats.stream().filter(c -> c.getNome().equals("ESTUDOS")).findFirst().get();
            Categoria pessoal = cats.stream().filter(c -> c.getNome().equals("PESSOAL")).findFirst().get();
            Categoria urgente = cats.stream().filter(c -> c.getNome().equals("URGENTE")).findFirst().get();
            Categoria saude = cats.stream().filter(c -> c.getNome().equals("SAUDE")).findFirst().get();
            Categoria financeiro = cats.stream().filter(c -> c.getNome().equals("FINANCEIRO")).findFirst().get();

            Usuario executor = users.stream().filter(u -> u.getRole() == UsuarioRole.EXECUTOR).findFirst().get();
            Usuario dist = users.stream().filter(u -> u.getRole() == UsuarioRole.DISTRIBUIDOR).findFirst().get();
            Usuario admin = users.stream().filter(u -> u.getRole() == UsuarioRole.ADMIN).findFirst().get();

            LocalDate hoje = LocalDate.now();

            tarefaRepository.saveAll(List.of(
                criarTarefa("Relatorio mensal", trabalho, hoje.plusDays(5), TarefaStatus.PENDENTE, null, admin.getId()),
                criarTarefa("Reuniao com equipe", trabalho, hoje.plusDays(2), TarefaStatus.EM_EXECUCAO, executor, dist.getId()),
                criarTarefa("Curso de Angular", estudos, hoje.plusDays(15), TarefaStatus.PENDENTE, null, admin.getId()),
                criarTarefa("Revisao de codigo", trabalho, hoje.plusDays(1), TarefaStatus.PENDENTE, executor, dist.getId()),
                criarTarefa("Compras do mes", pessoal, hoje.plusDays(7), TarefaStatus.CONCLUIDA, admin, admin.getId()),
                criarTarefa("Pagamento de contas", financeiro, hoje.plusDays(3), TarefaStatus.PENDENTE, null, dist.getId()),
                criarTarefa("Exame medico anual", saude, hoje.plusDays(20), TarefaStatus.PENDENTE, null, admin.getId()),
                criarTarefa("Vazamento no banheiro", urgente, hoje.plusDays(0), TarefaStatus.EM_EXECUCAO, executor, dist.getId()),
                criarTarefa("Projeto apresentacao", trabalho, hoje.plusDays(10), TarefaStatus.PENDENTE, executor, admin.getId()),
                criarTarefa("Leitura tecnica", estudos, hoje.plusDays(30), TarefaStatus.PENDENTE, null, dist.getId()),
                criarTarefa("Academia semanal", saude, hoje.plusDays(2), TarefaStatus.CONCLUIDA, admin, admin.getId()),
                criarTarefa("Declaracao imposto de renda", financeiro, hoje.plusDays(60), TarefaStatus.PENDENTE, null, admin.getId())
            ));
        }
    }

    private Tarefa criarTarefa(String titulo, Categoria categoria, LocalDate prazo, TarefaStatus status, Usuario responsavel, Long distribuidorId) {
        Tarefa t = new Tarefa();
        t.setTitulo(titulo);
        t.setDescricao("Descricao da tarefa: " + titulo.toLowerCase());
        t.setCategoria(categoria);
        t.setPrazo(prazo);
        t.setStatus(status);
        t.setResponsavel(responsavel);
        t.setDistribuidorId(distribuidorId);
        t.setDataCriacao(LocalDateTime.now().minusDays((long) (Math.random() * 10)));
        if (status == TarefaStatus.CONCLUIDA) {
            t.setDataConclusao(LocalDateTime.now());
        }
        return t;
    }
}
