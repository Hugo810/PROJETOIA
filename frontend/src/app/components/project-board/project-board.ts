import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ProjetoService } from '../../services/projeto.service';
import { Projeto, Meta } from '../../models/projeto';

@Component({
  selector: 'app-project-board',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './project-board.html',
  styleUrl: './project-board.css'
})
export class ProjectBoard implements OnInit {
  projeto: Projeto | null = null;
  metas: Meta[] = [];
  loading = true;
  mostrarFormMeta = false;
  editandoMetaId: number | null = null;

  metaTitulo = '';
  metaDescricao = '';
  metaInicio = '';
  metaFim = '';
  metaStatus = 'PENDENTE';
  metaProgresso = 0;

  meses = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];
  statusLabels: Record<string, string> = {
    PLANEJADO: 'Planejado', EM_ANDAMENTO: 'Em Andamento', CONCLUIDO: 'Concluído', CANCELADO: 'Cancelado',
    PENDENTE: 'Pendente', CONCLUIDA: 'Concluída'
  };
  statusColors: Record<string, string> = {
    PLANEJADO: '#f59e0b', EM_ANDAMENTO: '#3b82f6', CONCLUIDO: '#22c55e', CANCELADO: '#9ca3af',
    PENDENTE: '#f59e0b', CONCLUIDA: '#22c55e'
  };
  statusOptions = ['PENDENTE', 'EM_ANDAMENTO', 'CONCLUIDA', 'CANCELADA'];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private projetoService: ProjetoService
  ) {}

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) this.carregar(id);
  }

  carregar(id: number) {
    this.loading = true;
    this.projetoService.buscar(id).subscribe({
      next: (p) => { this.projeto = p; this.carregarMetas(id); },
      error: () => this.router.navigate(['/projetos'])
    });
  }

  carregarMetas(id: number) {
    this.projetoService.listarMetas(id).subscribe({
      next: (m) => { this.metas = m; this.loading = false; },
      error: () => this.loading = false
    });
  }

  voltar() { this.router.navigate(['/projetos']); }

  abrirFormMeta() {
    this.mostrarFormMeta = true;
    this.editandoMetaId = null;
    this.metaTitulo = '';
    this.metaDescricao = '';
    this.metaInicio = new Date().toISOString().split('T')[0];
    this.metaFim = '';
    this.metaStatus = 'PENDENTE';
    this.metaProgresso = 0;
  }

  editarMeta(m: Meta) {
    this.mostrarFormMeta = true;
    this.editandoMetaId = m.id!;
    this.metaTitulo = m.titulo;
    this.metaDescricao = m.descricao || '';
    this.metaInicio = m.dataInicio || '';
    this.metaFim = m.dataFim || '';
    this.metaStatus = m.status;
    this.metaProgresso = m.progresso;
  }

  fecharFormMeta() { this.mostrarFormMeta = false; }

  salvarMeta() {
    if (!this.metaTitulo || !this.projeto) return;
    const dados: Partial<Meta> = {
      titulo: this.metaTitulo, descricao: this.metaDescricao,
      dataInicio: this.metaInicio || undefined,
      dataFim: this.metaFim || undefined,
      status: this.metaStatus, progresso: this.metaProgresso
    };
    const obs = this.editandoMetaId
      ? this.projetoService.atualizarMeta(this.editandoMetaId, dados)
      : this.projetoService.criarMeta(this.projeto.id!, dados);
    obs.subscribe({ next: () => { this.fecharFormMeta(); this.carregarMetas(this.projeto!.id!); } });
  }

  excluirMeta(id: number) {
    this.projetoService.excluirMeta(id).subscribe({ next: () => this.carregarMetas(this.projeto!.id!) });
  }

  formatarData(d?: string): string {
    if (!d) return '—';
    const dt = new Date(d + 'T00:00:00');
    return `${dt.getDate()} ${this.meses[dt.getMonth()]} ${dt.getFullYear()}`;
  }
}
