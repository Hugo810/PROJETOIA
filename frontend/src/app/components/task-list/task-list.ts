import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TaskService } from '../../services/task.service';
import { AuthService } from '../../services/auth.service';
import { UsuarioService } from '../../services/usuario.service';
import { Tarefa } from '../../models/task';
import { Categoria } from '../../models/category';
import { Usuario } from '../../models/user';
import { ConfirmModal } from '../confirm-modal/confirm-modal';

type FiltroRapido = 'todas' | 'minhas' | 'atrasadas' | 'semana';

@Component({
  selector: 'app-task-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ConfirmModal],
  templateUrl: './task-list.html',
  styleUrl: './task-list.css'
})
export class TaskList implements OnInit {
  tarefas: Tarefa[] = [];
  tarefasExibidas: Tarefa[] = [];
  categorias: Categoria[] = [];
  usuarios: Usuario[] = [];
  totalPages = 0;
  currentPage = 0;
  pageSize = 12;

  filtroStatus = '';
  filtroCategoria: number | null = null;
  filtroRapido: FiltroRapido = 'todas';
  searchTerm = '';

  selectedIds = new Set<number>();
  selectAll = false;

  showDeleteModal = false;
  tarefaParaExcluir?: number;
  deleteMultiple = false;

  showDistribuirModal = false;
  tarefaParaDistribuir?: Tarefa;
  distribuirUsuarioId: number | null = null;

  concluindo = new Set<number>();
  bulkLoading = false;

  constructor(
    private taskService: TaskService,
    private auth: AuthService,
    private usuarioService: UsuarioService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.carregarCategorias();
    this.carregarTarefas();
    if (this.podeDistribuir) {
      this.usuarioService.listar().subscribe(data => this.usuarios = data);
    }
  }

  get podeDistribuir(): boolean {
    return this.auth.temRole('ADMIN', 'DISTRIBUIDOR');
  }

  get podeCriar(): boolean {
    return this.auth.temRole('ADMIN', 'DISTRIBUIDOR');
  }

  get podeExcluir(): boolean {
    return this.auth.temRole('ADMIN');
  }

  get podeConcluirQualquer(): boolean {
    return this.auth.temRole('ADMIN', 'DISTRIBUIDOR');
  }

  get podeGerenciar(): boolean {
    return this.auth.temRole('ADMIN', 'DISTRIBUIDOR');
  }

  get usuarioLogado() {
    return this.auth.getUsuarioLogado();
  }

  get hasSelection(): boolean {
    return this.selectedIds.size > 0;
  }

  get podeExcluirSelecao(): boolean {
    return this.podeExcluir && this.hasSelection;
  }

  get podeConcluirSelecao(): boolean {
    return this.hasSelection && this.podeGerenciar;
  }

  get tarefasVisiveis(): Tarefa[] {
    return this.tarefasExibidas;
  }

  carregarCategorias() {
    this.taskService.listarCategorias().subscribe(data => this.categorias = data);
  }

  carregarTarefas() {
    let responsavelId: number | undefined;
    if (this.filtroRapido === 'minhas') {
      responsavelId = this.usuarioLogado?.id;
    }

    this.taskService.listarTarefas(
      this.filtroStatus || undefined,
      this.filtroCategoria || undefined,
      responsavelId,
      this.currentPage,
      this.pageSize
    ).subscribe(page => {
      this.tarefas = this.ordenarPorUrgencia(page.content);
      this.totalPages = page.totalPages;
      this.aplicarFiltrosLocais();
      this.cdr.detectChanges();
    });
  }

  private aplicarFiltrosLocais() {
    let lista = this.tarefas;

    if (this.filtroRapido === 'atrasadas') {
      const hoje = new Date();
      hoje.setHours(0, 0, 0, 0);
      lista = lista.filter(t => {
        if (t.status === 'CONCLUIDA') return false;
        const prazo = new Date(t.prazo);
        prazo.setHours(0, 0, 0, 0);
        return prazo < hoje;
      });
    } else if (this.filtroRapido === 'semana') {
      const hoje = new Date();
      hoje.setHours(0, 0, 0, 0);
      const fimSemana = new Date(hoje);
      fimSemana.setDate(fimSemana.getDate() + 7);
      lista = lista.filter(t => {
        if (t.status === 'CONCLUIDA') return false;
        const prazo = new Date(t.prazo);
        prazo.setHours(0, 0, 0, 0);
        return prazo >= hoje && prazo <= fimSemana;
      });
    }

    if (this.searchTerm.trim()) {
      const term = this.searchTerm.trim().toLowerCase();
      lista = lista.filter(t => t.titulo.toLowerCase().includes(term));
    }

    this.tarefasExibidas = lista;
  }

  onSearchInput() {
    this.aplicarFiltrosLocais();
  }

  setFiltroRapido(f: FiltroRapido) {
    this.filtroRapido = f;
    this.currentPage = 0;
    this.selectedIds.clear();
    this.selectAll = false;
    this.carregarTarefas();
  }

  getBadgeColor(nome: string): string {
    const cores = ['#e0e7ff', '#fce4ec', '#e8f5e9', '#fff3e0', '#f3e5f5', '#e0f7fa'];
    const colors = ['#4361ee', '#c62828', '#2e7d32', '#e65100', '#7b1fa2', '#00838f'];
    let hash = 0;
    for (let i = 0; i < nome.length; i++) {
      hash = nome.charCodeAt(i) + ((hash << 5) - hash);
    }
    const idx = Math.abs(hash) % cores.length;
    return cores[idx];
  }

  private ordenarPorUrgencia(tarefas: Tarefa[]): Tarefa[] {
    return [...tarefas].sort((a, b) => {
      const aUrg = this.getDiasPrazo(a);
      const bUrg = this.getDiasPrazo(b);
      const aNivel = aUrg !== null && aUrg <= 3 ? 0 : 1;
      const bNivel = bUrg !== null && bUrg <= 3 ? 0 : 1;
      if (aNivel !== bNivel) return aNivel - bNivel;
      if (a.status === 'CONCLUIDA' && b.status !== 'CONCLUIDA') return 1;
      if (a.status !== 'CONCLUIDA' && b.status === 'CONCLUIDA') return -1;
      return a.prazo.localeCompare(b.prazo);
    });
  }

  getDiasPrazo(t: Tarefa): number | null {
    if (!t.prazo) return null;
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    const prazo = new Date(t.prazo);
    prazo.setHours(0, 0, 0, 0);
    const diff = prazo.getTime() - hoje.getTime();
    return Math.ceil(diff / (1000 * 60 * 60 * 24));
  }

  getUrgenciaClass(t: Tarefa): string {
    if (t.status === 'CONCLUIDA') return '';
    const dias = this.getDiasPrazo(t);
    if (dias === null) return '';
    if (dias <= 3) return 'urgencia-alta';
    if (dias <= 7) return 'urgencia-media';
    return '';
  }

  filtrar() {
    this.currentPage = 0;
    this.selectedIds.clear();
    this.selectAll = false;
    this.carregarTarefas();
  }

  limparFiltros() {
    this.filtroStatus = '';
    this.filtroCategoria = null;
    this.filtroRapido = 'todas';
    this.searchTerm = '';
    this.filtrar();
  }

  toggleSelect(id: number) {
    if (this.selectedIds.has(id)) {
      this.selectedIds.delete(id);
    } else {
      this.selectedIds.add(id);
    }
  }

  toggleSelectAll() {
    this.selectAll = !this.selectAll;
    if (this.selectAll) {
      this.tarefasExibidas.forEach(t => t.id && this.selectedIds.add(t.id));
    } else {
      this.selectedIds.clear();
    }
  }

  isSelected(id: number): boolean {
    return this.selectedIds.has(id);
  }

  concluir(id: number) {
    if (this.concluindo.has(id)) return;
    this.concluindo.add(id);
    this.taskService.concluirTarefa(id).subscribe({
      next: (tarefaAtualizada) => {
        this.concluindo.delete(id);
        const idx = this.tarefas.findIndex(t => t.id === id);
        if (idx !== -1) {
          this.tarefas[idx] = { ...this.tarefas[idx], ...tarefaAtualizada };
        }
        this.selectedIds.delete(id);
        this.aplicarFiltrosLocais();
        this.cdr.detectChanges();
      },
      error: () => {
        this.concluindo.delete(id);
      }
    });
  }

  toggleConcluir(t: Tarefa) {
    if (t.status === 'CONCLUIDA') return;
    if (!this.podeConcluirTarefa(t)) return;
    this.concluir(t.id!);
  }

  podeConcluirTarefa(t: Tarefa): boolean {
    if (t.status === 'CONCLUIDA') return false;
    if (this.podeConcluirQualquer) return true;
    return t.responsavelId === this.usuarioLogado?.id;
  }

  podeEditarTarefa(t: Tarefa): boolean {
    if (t.status === 'CONCLUIDA') return false;
    return this.auth.temRole('ADMIN', 'DISTRIBUIDOR');
  }

  abrirDistribuir(t: Tarefa) {
    this.tarefaParaDistribuir = t;
    this.distribuirUsuarioId = t.responsavelId || null;
    this.showDistribuirModal = true;
  }

  distribuir() {
    if (this.tarefaParaDistribuir?.id && this.distribuirUsuarioId) {
      this.taskService.distribuirTarefa(this.tarefaParaDistribuir.id, this.distribuirUsuarioId)
        .subscribe(() => {
          this.showDistribuirModal = false;
          this.tarefaParaDistribuir = undefined;
          this.distribuirUsuarioId = null;
          this.carregarTarefas();
        });
    }
  }

  cancelarDistribuir() {
    this.showDistribuirModal = false;
    this.tarefaParaDistribuir = undefined;
    this.distribuirUsuarioId = null;
  }

  confirmarExclusao(id: number) {
    this.tarefaParaExcluir = id;
    this.deleteMultiple = false;
    this.showDeleteModal = true;
  }

  confirmarExclusaoSelecionadas() {
    this.deleteMultiple = true;
    this.showDeleteModal = true;
  }

  excluir() {
    if (this.deleteMultiple) {
      const ids = Array.from(this.selectedIds);
      this.bulkLoading = true;
      let completed = 0;
      ids.forEach(id => {
        this.taskService.excluirTarefa(id).subscribe({
          next: () => {
            this.tarefas = this.tarefas.filter(t => t.id !== id);
            this.selectedIds.delete(id);
            completed++;
            if (completed === ids.length) {
              this.bulkLoading = false;
              this.showDeleteModal = false;
              this.selectAll = false;
              this.aplicarFiltrosLocais();
              this.cdr.detectChanges();
            }
          },
          error: () => {
            completed++;
            if (completed === ids.length) {
              this.bulkLoading = false;
            }
          }
        });
      });
    } else if (this.tarefaParaExcluir) {
      const id = this.tarefaParaExcluir;
      this.taskService.excluirTarefa(id).subscribe({
        next: () => {
          this.showDeleteModal = false;
          this.tarefas = this.tarefas.filter(t => t.id !== id);
          this.selectedIds.delete(id);
          this.tarefaParaExcluir = undefined;
          this.aplicarFiltrosLocais();
          this.cdr.detectChanges();
        },
        error: () => {
          this.showDeleteModal = false;
          this.tarefaParaExcluir = undefined;
        }
      });
    }
  }

  cancelarExclusao() {
    this.showDeleteModal = false;
    this.tarefaParaExcluir = undefined;
    this.deleteMultiple = false;
  }

  concluirSelecionadas() {
    const ids = Array.from(this.selectedIds).filter(id => !this.concluindo.has(id));
    if (ids.length === 0) return;
    this.bulkLoading = true;
    let completed = 0;
    ids.forEach(id => {
      this.concluindo.add(id);
      this.taskService.concluirTarefa(id).subscribe({
        next: (tarefaAtualizada) => {
          this.concluindo.delete(id);
          const idx = this.tarefas.findIndex(t => t.id === id);
          if (idx !== -1) {
            this.tarefas[idx] = { ...this.tarefas[idx], ...tarefaAtualizada };
          }
          this.selectedIds.delete(id);
          completed++;
          if (completed === ids.length) {
            this.bulkLoading = false;
            this.selectAll = false;
            this.aplicarFiltrosLocais();
            this.cdr.detectChanges();
          }
        },
        error: () => {
          this.concluindo.delete(id);
          completed++;
          if (completed === ids.length) {
            this.bulkLoading = false;
          }
        }
      });
    });
  }

  mudarPagina(pagina: number) {
    this.currentPage = pagina;
    this.selectedIds.clear();
    this.selectAll = false;
    this.carregarTarefas();
  }

  getStatusClass(status: string): string {
    if (status === 'CONCLUIDA') return 'status-concluida';
    if (status === 'EM_EXECUCAO') return 'status-execucao';
    return 'status-pendente';
  }

  podeIniciarTarefa(t: Tarefa): boolean {
    if (t.status !== 'PENDENTE') return false;
    if (t.responsavelId === this.usuarioLogado?.id) return true;
    return this.podeConcluirQualquer;
  }

  iniciar(id: number) {
    this.taskService.iniciarTarefa(id).subscribe(() => this.carregarTarefas());
  }
}
