import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { GanttService, GanttData, GanttTarefa, Dependencia, Marco } from '../../services/gantt.service';

@Component({
  selector: 'app-gantt-chart',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './gantt-chart.html',
  styleUrl: './gantt-chart.css'
})
export class GanttChart implements OnInit {
  data: GanttData = { tarefas: [], dependencias: [], marcos: [] };
  loading = true;

  dias: Date[] = [];
  inicioPeriodo: Date;
  fimPeriodo: Date;
  diaPixel = 28;
  mesAtual = '';

  mostrarFormMarco = false;
  nomeMarco = '';
  dataMarco = '';

  mostrarFormDep = false;
  depTarefaId: number | null = null;
  depDependenteId: number | null = null;
  depTipo = 'BLOQUEIA';

  meses = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];
  mesesNomes = ['Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho', 'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'];

  statusColors: Record<string, string> = {
    PENDENTE: '#f59e0b',
    EM_EXECUCAO: '#3b82f6',
    CONCLUIDA: '#22c55e',
    ARQUIVADA: '#9ca3af'
  };

  constructor(private ganttService: GanttService) {
    this.inicioPeriodo = new Date();
    this.inicioPeriodo.setDate(this.inicioPeriodo.getDate() - 7);
    this.fimPeriodo = new Date();
    this.fimPeriodo.setDate(this.fimPeriodo.getDate() + 30);
  }

  ngOnInit() {
    this.carregar();
  }

  carregar() {
    this.loading = true;
    const ini = this.formatarData(this.inicioPeriodo);
    const fim = this.formatarData(this.fimPeriodo);
    this.ganttService.buscar(ini, fim).subscribe({
      next: (data) => { this.data = data; this.gerarDias(); this.loading = false; },
      error: () => this.loading = false
    });
  }

  gerarDias() {
    this.dias = [];
    const atual = new Date(this.inicioPeriodo);
    while (atual <= this.fimPeriodo) {
      this.dias.push(new Date(atual));
      atual.setDate(atual.getDate() + 1);
    }
    if (this.dias.length > 0) {
      const primeiro = this.dias[0];
      this.mesAtual = `${this.mesesNomes[primeiro.getMonth()]} ${primeiro.getFullYear()}`;
    }
  }

  get larguraGantt(): number {
    return this.dias.length * this.diaPixel;
  }

  isHoje(dia: Date): boolean {
    const hoje = new Date();
    return dia.getDate() === hoje.getDate() && dia.getMonth() === hoje.getMonth() && dia.getFullYear() === hoje.getFullYear();
  }

  isFimDeSemana(dia: Date): boolean {
    const d = dia.getDay();
    return d === 0 || d === 6;
  }

  barraLeft(tarefa: GanttTarefa): number {
    const prazo = new Date(tarefa.prazo);
    prazo.setDate(prazo.getDate() - tarefa.duracaoDias);
    const diff = (prazo.getTime() - this.inicioPeriodo.getTime()) / (1000 * 60 * 60 * 24);
    return Math.max(0, diff) * this.diaPixel;
  }

  barraWidth(tarefa: GanttTarefa): number {
    return Math.max(tarefa.duracaoDias * this.diaPixel, this.diaPixel);
  }

  linhaDependencia(dep: Dependencia): { x1: number; y1: number; x2: number; y2: number } | null {
    const deIdx = this.data.tarefas.findIndex(t => t.id === dep.tarefaId);
    const paraIdx = this.data.tarefas.findIndex(t => t.id === dep.tarefaDependenteId);
    if (deIdx < 0 || paraIdx < 0) return null;

    const de = this.data.tarefas[deIdx];
    const para = this.data.tarefas[paraIdx];

    const x1 = this.barraLeft(de) + this.barraWidth(de);
    const y1 = deIdx * 38 + 18;
    const x2 = this.barraLeft(para);
    const y2 = paraIdx * 38 + 18;

    return { x1, y1, x2, y2 };
  }

  formatarDataBr(dia: Date): string {
    return `${dia.getDate().toString().padStart(2, '0')}/${(dia.getMonth() + 1).toString().padStart(2, '0')}`;
  }

  formatarDataISO(d: Date): string {
    return d.toISOString().split('T')[0];
  }

  private formatarData(d: Date): string {
    return d.toISOString().split('T')[0];
  }

  anteriorMes() {
    this.inicioPeriodo.setMonth(this.inicioPeriodo.getMonth() - 1);
    this.fimPeriodo.setMonth(this.fimPeriodo.getMonth() - 1);
    this.carregar();
  }

  proximoMes() {
    this.inicioPeriodo.setMonth(this.inicioPeriodo.getMonth() + 1);
    this.fimPeriodo.setMonth(this.fimPeriodo.getMonth() + 1);
    this.carregar();
  }

  voltarHoje() {
    this.inicioPeriodo = new Date();
    this.inicioPeriodo.setDate(this.inicioPeriodo.getDate() - 7);
    this.fimPeriodo = new Date();
    this.fimPeriodo.setDate(this.fimPeriodo.getDate() + 30);
    this.carregar();
  }

  abrirFormMarco() {
    this.mostrarFormMarco = true;
    this.nomeMarco = '';
    this.dataMarco = new Date().toISOString().split('T')[0];
  }

  fecharFormMarco() { this.mostrarFormMarco = false; }

  salvarMarco() {
    if (!this.nomeMarco) return;
    this.ganttService.criarMarco({
      nome: this.nomeMarco,
      data: this.dataMarco + 'T12:00:00'
    }).subscribe({ next: () => { this.fecharFormMarco(); this.carregar(); } });
  }

  excluirMarco(id: number) {
    this.ganttService.excluirMarco(id).subscribe({ next: () => this.carregar() });
  }

  abrirFormDep() {
    this.mostrarFormDep = true;
    this.depTarefaId = null;
    this.depDependenteId = null;
    this.depTipo = 'BLOQUEIA';
  }

  fecharFormDep() { this.mostrarFormDep = false; }

  salvarDep() {
    if (!this.depTarefaId || !this.depDependenteId) return;
    this.ganttService.criarDependencia(this.depTarefaId, this.depDependenteId, this.depTipo)
      .subscribe({ next: () => { this.fecharFormDep(); this.carregar(); } });
  }

  excluirDep(id: number) {
    this.ganttService.excluirDependencia(id).subscribe({ next: () => this.carregar() });
  }
}
