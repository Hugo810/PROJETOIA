import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TempoService } from '../../services/tempo.service';
import { AuthService } from '../../services/auth.service';
import { RegistroTempo } from '../../models/tempo';

@Component({
  selector: 'app-task-timer',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './task-timer.html',
  styleUrl: './task-timer.css'
})
export class TaskTimer implements OnInit, OnDestroy {
  @Input() tarefaId!: number;

  registros: RegistroTempo[] = [];
  totalMinutos = 0;
  timerAtivo: RegistroTempo | null = null;
  elapsedSeconds = 0;
  private intervalId: any;

  mostrarModal = false;
  duracaoManual: number | null = null;
  descricaoManual = '';

  meses = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];

  constructor(
    private tempoService: TempoService,
    private auth: AuthService
  ) {}

  ngOnInit() {
    this.carregar();
  }

  ngOnDestroy() {
    this.pararCronometro();
  }

  get usuarioId(): number | null {
    const user = this.auth.getUsuarioLogado();
    return user ? user.id : null;
  }

  get timerRodando(): boolean {
    return this.timerAtivo !== null;
  }

  get cronometroFormatado(): string {
    const h = Math.floor(this.elapsedSeconds / 3600);
    const m = Math.floor((this.elapsedSeconds % 3600) / 60);
    const s = this.elapsedSeconds % 60;
    return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  }

  carregar() {
    this.tempoService.listarPorTarefa(this.tarefaId).subscribe({
      next: (data) => {
        this.registros = data.registros;
        this.totalMinutos = data.totalMinutos;
        const ativo = this.registros.find(r => r.emAndamento);
        if (ativo) {
          this.timerAtivo = ativo;
          this.iniciarCronometro();
        }
      }
    });
  }

  iniciar() {
    this.tempoService.iniciarTimer(this.tarefaId).subscribe({
      next: (registro) => {
        this.timerAtivo = registro;
        this.elapsedSeconds = 0;
        this.iniciarCronometro();
      }
    });
  }

  parar() {
    if (!this.timerAtivo) return;
    this.tempoService.pararTimer(this.timerAtivo.id!).subscribe({
      next: (registro) => {
        this.pararCronometro();
        this.timerAtivo = null;
        this.elapsedSeconds = 0;
        this.carregar();
      }
    });
  }

  private iniciarCronometro() {
    this.pararCronometro();
    if (this.timerAtivo?.inicio) {
      const inicio = new Date(this.timerAtivo.inicio).getTime();
      this.elapsedSeconds = Math.floor((Date.now() - inicio) / 1000);
    }
    this.intervalId = setInterval(() => {
      this.elapsedSeconds++;
    }, 1000);
  }

  private pararCronometro() {
    if (this.intervalId) {
      clearInterval(this.intervalId);
      this.intervalId = null;
    }
  }

  abrirModalManual() {
    this.mostrarModal = true;
    this.duracaoManual = null;
    this.descricaoManual = '';
  }

  fecharModal() {
    this.mostrarModal = false;
  }

  registrarManual() {
    if (!this.duracaoManual || this.duracaoManual <= 0) return;
    this.tempoService.registrarManual(this.tarefaId, this.duracaoManual, this.descricaoManual || undefined).subscribe({
      next: () => {
        this.fecharModal();
        this.carregar();
      }
    });
  }

  excluir(registro: RegistroTempo) {
    this.tempoService.excluir(registro.id!).subscribe({
      next: () => this.carregar()
    });
  }

  formatarDuracao(minutos: number): string {
    const h = Math.floor(minutos / 60);
    const m = minutos % 60;
    if (h > 0) return `${h}h ${m}min`;
    return `${m}min`;
  }

  formatarData(data?: string): string {
    if (!data) return '';
    const d = new Date(data);
    return `${d.getDate()} ${this.meses[d.getMonth()]} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`;
  }
}
