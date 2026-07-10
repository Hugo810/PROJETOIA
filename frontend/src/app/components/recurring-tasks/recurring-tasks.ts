import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RecorrenteService } from '../../services/recorrente.service';
import { CacheService } from '../../services/cache.service';
import { TaskService } from '../../services/task.service';
import { AuthService } from '../../services/auth.service';
import { TarefaRecorrente } from '../../models/recorrente';
import { Tarefa } from '../../models/task';
import { Categoria } from '../../models/category';

@Component({
  selector: 'app-recurring-tasks',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './recurring-tasks.html',
  styleUrl: './recurring-tasks.css'
})
export class RecurringTasks implements OnInit {
  recorrentes: TarefaRecorrente[] = [];
  tarefas: Tarefa[] = [];
  categorias: Categoria[] = [];
  mostrarForm = false;
  editandoId: number | null = null;
  loading = true;
  expandirId: number | null = null;

  modeloSelecionado = '';
  recorrenciaSelecionada = 'SEMANAL';
  proximaExecucao = '';
  configuracao = '';

  meses = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];

  recorrenciaLabels: Record<string, string> = {
    DIARIA: 'Diária',
    SEMANAL: 'Semanal',
    QUINZENAL: 'Quinzenal',
    MENSAL: 'Mensal',
    BIMESTRAL: 'Bimestral',
    PERSONALIZADA: 'Personalizada'
  };

  constructor(
    private recorrenteService: RecorrenteService,
    private taskService: TaskService,
    private cache: CacheService,
    private auth: AuthService
  ) {}

  ngOnInit() {
    this.loading = true;
    this.cache.getCategorias().subscribe(c => this.categorias = c);
    this.taskService.listarTarefas().subscribe(t => this.tarefas = t);
    this.recorrenteService.listar().subscribe({
      next: (data) => { this.recorrentes = data; this.loading = false; },
      error: () => this.loading = false
    });
  }

  get usuarioId(): number | null {
    const user = this.auth.getUsuarioLogado();
    return user ? user.id : null;
  }

  abrirForm() {
    this.mostrarForm = true;
    this.editandoId = null;
    this.modeloSelecionado = '';
    this.recorrenciaSelecionada = 'SEMANAL';
    this.proximaExecucao = this.proximoDomingo();
    this.configuracao = '';
  }

  editar(rec: TarefaRecorrente) {
    this.mostrarForm = true;
    this.editandoId = rec.id!;
    this.modeloSelecionado = rec.tarefaModeloId.toString();
    this.recorrenciaSelecionada = rec.recorrencia;
    this.proximaExecucao = rec.proximaExecucao;
    this.configuracao = rec.configuracao || '';
  }

  fecharForm() {
    this.mostrarForm = false;
    this.editandoId = null;
  }

  salvar() {
    if (!this.modeloSelecionado || !this.proximaExecucao) return;

    const dados: Partial<TarefaRecorrente> = {
      tarefaModeloId: +this.modeloSelecionado,
      recorrencia: this.recorrenciaSelecionada,
      proximaExecucao: this.proximaExecucao,
      configuracao: this.configuracao || undefined
    };

    const obs = this.editandoId
      ? this.recorrenteService.atualizar(this.editandoId, dados)
      : this.recorrenteService.criar(dados);

    obs.subscribe({
      next: () => { this.fecharForm(); this.ngOnInit(); }
    });
  }

  excluir(id: number) {
    this.recorrenteService.excluir(id).subscribe({ next: () => this.ngOnInit() });
  }

  toggleAtiva(rec: TarefaRecorrente) {
    this.recorrenteService.toggleAtiva(rec.id!, !rec.ativa).subscribe({ next: () => this.ngOnInit() });
  }

  pular(id: number) {
    this.recorrenteService.pular(id).subscribe({ next: () => this.ngOnInit() });
  }

  adiar(id: number, dias: number) {
    this.recorrenteService.adiar(id, dias).subscribe({ next: () => this.ngOnInit() });
  }

  toggleExpandir(id: number) {
    if (this.expandirId === id) {
      this.expandirId = null;
      return;
    }
    this.expandirId = id;
    const rec = this.recorrentes.find(r => r.id === id);
    if (rec) {
      this.recorrenteService.proximasExecucoes(id, 6).subscribe({
        next: (datas) => rec.proximasExecucoes = datas
      });
    }
  }

  formatarData(data: string): string {
    if (!data) return '';
    const d = new Date(data + 'T00:00:00');
    return `${d.getDate()} ${this.meses[d.getMonth()]}`;
  }

  private proximoDomingo(): string {
    const hoje = new Date();
    const diasAteDomingo = (7 - hoje.getDay()) % 7 || 7;
    const proximo = new Date(hoje);
    proximo.setDate(hoje.getDate() + diasAteDomingo);
    return proximo.toISOString().split('T')[0];
  }
}
