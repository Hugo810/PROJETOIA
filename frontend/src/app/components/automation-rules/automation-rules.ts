import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AutomacaoService } from '../../services/automacao.service';
import { RegraAutomacao } from '../../models/automacao';

@Component({
  selector: 'app-automation-rules',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './automation-rules.html',
  styleUrl: './automation-rules.css'
})
export class AutomationRules implements OnInit {
  regras: RegraAutomacao[] = [];
  mostrarForm = false;
  editandoId: number | null = null;
  loading = true;

  nome = '';
  condicaoTipo = 'STATUS_MUDOU';
  condicaoValor = 'CONCLUIDA';
  acaoTipo = 'ENVIAR_NOTIFICACAO';
  acaoDados = '{}';

  condicoes = [
    { valor: 'STATUS_MUDOU', label: 'Status mudou para' },
    { valor: 'PRAZO_CHEGOU', label: 'Prazo chegou' },
    { valor: 'PRAZO_ATRASADO', label: 'Tarefa atrasada' },
    { valor: 'TAREFA_CRIADA', label: 'Tarefa criada' },
    { valor: 'PRIORIDADE_ALTA', label: 'Prioridade alta' },
    { valor: 'CATEGORIA_IGUAL', label: 'Categoria específica' }
  ];

  acoes = [
    { valor: 'CRIAR_TAREFA', label: 'Criar nova tarefa' },
    { valor: 'ENVIAR_NOTIFICACAO', label: 'Enviar notificação' },
    { valor: 'ATRIBUIR_RESPONSAVEL', label: 'Atribuir responsável' },
    { valor: 'ALTERAR_PRIORIDADE', label: 'Alterar prioridade' },
    { valor: 'ENVIAR_NOTIFICACAO_CRIADOR', label: 'Notificar criador' }
  ];

  statusOpcoes = ['PENDENTE', 'EM_EXECUCAO', 'CONCLUIDA', 'ARQUIVADA'];
  prioridadeOpcoes = ['BAIXA', 'MEDIA', 'ALTA'];

  novaTarefaTitulo = '';
  novaTarefaDias = 7;
  novaTarefaPrioridade = 'MEDIA';
  notificacaoMensagem = '';
  notificacaoUsuarioId = 1;
  prioridadeNova = 'ALTA';

  constructor(private automacaoService: AutomacaoService) {}

  ngOnInit() {
    this.automacaoService.listar().subscribe({
      next: (data) => { this.regras = data; this.loading = false; },
      error: () => this.loading = false
    });
  }

  abrirForm() {
    this.mostrarForm = true;
    this.editandoId = null;
    this.nome = '';
    this.condicaoTipo = 'STATUS_MUDOU';
    this.condicaoValor = 'CONCLUIDA';
    this.acaoTipo = 'ENVIAR_NOTIFICACAO';
    this.novaTarefaTitulo = '';
    this.novaTarefaDias = 7;
    this.novaTarefaPrioridade = 'MEDIA';
    this.notificacaoMensagem = '';
    this.notificacaoUsuarioId = 1;
    this.prioridadeNova = 'ALTA';
  }

  fecharForm() {
    this.mostrarForm = false;
    this.editandoId = null;
  }

  getCondicaoJson(): string {
    switch (this.condicaoTipo) {
      case 'STATUS_MUDOU': return JSON.stringify({ tipo: 'STATUS_MUDOU', valor: this.condicaoValor });
      case 'PRAZO_CHEGOU': return JSON.stringify({ tipo: 'PRAZO_CHEGOU', valor: 'HOJE' });
      case 'PRAZO_ATRASADO': return JSON.stringify({ tipo: 'PRAZO_ATRASADO' });
      case 'TAREFA_CRIADA': return JSON.stringify({ tipo: 'TAREFA_CRIADA' });
      case 'PRIORIDADE_ALTA': return JSON.stringify({ tipo: 'PRIORIDADE_ALTA' });
      case 'CATEGORIA_IGUAL': return JSON.stringify({ tipo: 'CATEGORIA_IGUAL', valor: +this.condicaoValor });
      default: return JSON.stringify({ tipo: this.condicaoTipo });
    }
  }

  getAcaoJson(): string {
    switch (this.acaoTipo) {
      case 'CRIAR_TAREFA':
        return JSON.stringify({
          tipo: 'CRIAR_TAREFA',
          dados: { titulo: this.novaTarefaTitulo, diasPrazo: this.novaTarefaDias, prioridade: this.novaTarefaPrioridade }
        });
      case 'ENVIAR_NOTIFICACAO':
        return JSON.stringify({
          tipo: 'ENVIAR_NOTIFICACAO',
          dados: { usuarioId: this.notificacaoUsuarioId, mensagem: this.notificacaoMensagem || 'Automação executada' }
        });
      case 'ATRIBUIR_RESPONSAVEL':
        return JSON.stringify({
          tipo: 'ATRIBUIR_RESPONSAVEL',
          dados: { usuarioId: this.notificacaoUsuarioId }
        });
      case 'ALTERAR_PRIORIDADE':
        return JSON.stringify({
          tipo: 'ALTERAR_PRIORIDADE',
          dados: { prioridade: this.prioridadeNova }
        });
      case 'ENVIAR_NOTIFICACAO_CRIADOR':
        return JSON.stringify({
          tipo: 'ENVIAR_NOTIFICACAO_CRIADOR',
          dados: { mensagem: this.notificacaoMensagem || 'Automação executada na sua tarefa' }
        });
      default: return '{}';
    }
  }

  salvar() {
    if (!this.nome) return;
    const dados: Partial<RegraAutomacao> = {
      nome: this.nome,
      condicao: this.getCondicaoJson(),
      acao: this.getAcaoJson()
    };

    const obs = this.editandoId
      ? this.automacaoService.atualizar(this.editandoId, dados)
      : this.automacaoService.criar(dados);

    obs.subscribe({ next: () => { this.fecharForm(); this.ngOnInit(); } });
  }

  toggleAtiva(regra: RegraAutomacao) {
    this.automacaoService.toggleAtiva(regra.id!, !regra.ativa).subscribe({ next: () => this.ngOnInit() });
  }

  excluir(id: number) {
    this.automacaoService.excluir(id).subscribe({ next: () => this.ngOnInit() });
  }

  getLabelCondicao(tipo: string): string {
    return this.condicoes.find(c => c.valor === tipo)?.label || tipo;
  }

  getLabelAcao(tipo: string): string {
    return this.acoes.find(a => a.valor === tipo)?.label || tipo;
  }

  parseCondicao(json: string): { tipo: string; valor?: any } {
    try { return JSON.parse(json); } catch { return { tipo: '?' }; }
  }

  parseAcao(json: string): { tipo: string; dados?: any } {
    try { return JSON.parse(json); } catch { return { tipo: '?' }; }
  }
}
