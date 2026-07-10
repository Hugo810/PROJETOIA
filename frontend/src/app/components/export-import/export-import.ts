import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-export-import',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './export-import.html',
  styleUrl: './export-import.css'
})
export class ExportImport {
  importando = false;
  mensagem = '';
  erro = '';

  constructor(private http: HttpClient) {}

  exportarCSV() {
    window.open('/api/exportar/tarefas?formato=csv', '_blank');
  }

  exportarJSON() {
    window.open('/api/exportar/tarefas?formato=json', '_blank');
  }

  exportarICS() {
    window.open('/api/calendario/tarefas/ics', '_blank');
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const file = input.files[0];
    const formData = new FormData();
    formData.append('file', file);

    this.importando = true;
    this.mensagem = '';
    this.erro = '';

    this.http.post<{ mensagem: string; total: number }>('/api/importar/tarefas', formData)
      .subscribe({
        next: (res) => {
          this.mensagem = res.mensagem;
          this.importando = false;
          input.value = '';
        },
        error: (err) => {
          this.erro = err.error?.erro || 'Erro ao importar arquivo';
          this.importando = false;
          input.value = '';
        }
      });
  }
}
