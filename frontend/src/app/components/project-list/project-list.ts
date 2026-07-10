import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ProjetoService } from '../../services/projeto.service';
import { Projeto } from '../../models/projeto';

@Component({
  selector: 'app-project-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './project-list.html',
  styleUrl: './project-list.css'
})
export class ProjectList implements OnInit {
  projetos: Projeto[] = [];
  loading = true;
  mostrarForm = false;
  editandoId: number | null = null;

  nome = '';
  descricao = '';
  dataInicio = '';
  dataFim = '';
  status = 'PLANEJADO';

  meses = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];
  statusLabels: Record<string, string> = {
    PLANEJADO: 'Planejado', EM_ANDAMENTO: 'Em Andamento', CONCLUIDO: 'Concluído', CANCELADO: 'Cancelado'
  };
  statusColors: Record<string, string> = {
    PLANEJADO: '#f59e0b', EM_ANDAMENTO: '#3b82f6', CONCLUIDO: '#22c55e', CANCELADO: '#9ca3af'
  };

  constructor(private projetoService: ProjetoService) {}

  ngOnInit() { this.carregar(); }

  carregar() {
    this.loading = true;
    this.projetoService.listar().subscribe({
      next: (data) => { this.projetos = data; this.loading = false; },
      error: () => this.loading = false
    });
  }

  abrirForm() {
    this.mostrarForm = true;
    this.editandoId = null;
    this.nome = '';
    this.descricao = '';
    this.dataInicio = new Date().toISOString().split('T')[0];
    this.dataFim = '';
    this.status = 'PLANEJADO';
  }

  fecharForm() { this.mostrarForm = false; }

  salvar() {
    if (!this.nome) return;
    const dados: Partial<Projeto> = {
      nome: this.nome, descricao: this.descricao,
      dataInicio: this.dataInicio || undefined,
      dataFim: this.dataFim || undefined,
      status: this.status
    };
    const obs = this.editandoId
      ? this.projetoService.atualizar(this.editandoId, dados)
      : this.projetoService.criar(dados);
    obs.subscribe({ next: () => { this.fecharForm(); this.carregar(); } });
  }

  excluir(id: number) {
    this.projetoService.excluir(id).subscribe({ next: () => this.carregar() });
  }

  formatarData(d?: string): string {
    if (!d) return '—';
    const dt = new Date(d + 'T00:00:00');
    return `${dt.getDate()} ${this.meses[dt.getMonth()]} ${dt.getFullYear()}`;
  }
}
