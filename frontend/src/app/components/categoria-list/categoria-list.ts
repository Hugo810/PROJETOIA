import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CategoriaService } from '../../services/categoria.service';
import { Categoria } from '../../models/category';

@Component({
  selector: 'app-categoria-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="container">
      <header>
        <h1>Categorias</h1>
        <div class="header-actions">
          <button class="btn btn-primary" (click)="mostrarForm = true" [disabled]="mostrarForm">Nova Categoria</button>
          <button class="btn btn-secondary" routerLink="/tarefas">Voltar</button>
        </div>
      </header>

      @if (erro) {
        <div class="erro">{{ erro }}</div>
      }

      @if (mostrarForm) {
        <div class="form-card">
          <h2>Nova Categoria</h2>
          <div class="form-group">
            <input
              #nomeInput
              type="text"
              [(ngModel)]="novoNome"
              placeholder="Nome da categoria"
              (keyup.enter)="salvar()"
              (keyup.escape)="cancelar()"
              autofocus
            />
          </div>
          @if (erroForm) {
            <div class="erro-form">{{ erroForm }}</div>
          }
          <div class="form-actions">
            <button class="btn btn-primary" (click)="salvar()" [disabled]="!novoNome.trim()">Salvar</button>
            <button class="btn btn-secondary" (click)="cancelar()">Cancelar</button>
          </div>
        </div>
      }

      @if (categorias.length === 0) {
        <div class="empty">Nenhuma categoria cadastrada.</div>
      }

      <div class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Nome</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            @for (cat of categorias; track cat.id) {
              <tr>
                <td>{{ cat.id }}</td>
                <td>{{ cat.nome }}</td>
                <td class="actions">
                  <button class="btn btn-sm btn-danger" (click)="excluir(cat.id!)">Excluir</button>
                </td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .container { max-width: 600px; margin: 0 auto; padding: 20px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
    header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
    header h1 { margin: 0; font-size: 28px; color: #1a1a2e; }
    .header-actions { display: flex; gap: 8px; }
    .btn {
      padding: 8px 16px; border: none; border-radius: 6px; cursor: pointer; font-size: 14px;
      text-decoration: none; display: inline-block; transition: opacity 0.2s;
    }
    .btn-sm { padding: 6px 12px; font-size: 13px; }
    .btn-primary { background: #4361ee; color: white; }
    .btn-secondary { background: #6c757d; color: white; }
    .btn-danger { background: #e63946; color: white; }
    .btn:hover { opacity: 0.85; }
    .btn:disabled { opacity: 0.4; cursor: default; }
    .form-card { background: #f8f9fa; border-radius: 10px; padding: 20px; margin-bottom: 24px; border: 1px solid #e8e8e8; }
    .form-card h2 { margin: 0 0 16px; font-size: 18px; color: #1a1a2e; }
    .form-group { margin-bottom: 12px; }
    .form-group input { width: 100%; padding: 10px 12px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; box-sizing: border-box; text-transform: uppercase; }
    .form-actions { display: flex; gap: 8px; }
    .erro { text-align: center; padding: 20px; color: #e63946; font-size: 16px; margin-bottom: 16px; }
    .erro-form { color: #e63946; font-size: 13px; margin-bottom: 8px; }
    .table-wrapper { overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
    th, td { padding: 12px 16px; text-align: left; font-size: 14px; }
    th { background: #f8f9fa; color: #333; font-weight: 600; border-bottom: 2px solid #e8e8e8; }
    td { border-bottom: 1px solid #f0f0f0; }
    tr:last-child td { border-bottom: none; }
    .actions { display: flex; gap: 6px; }
    .empty { text-align: center; padding: 40px; color: #6c757d; font-size: 16px; }
  `]
})
export class CategoriaList implements OnInit {
  categorias: Categoria[] = [];
  novoNome = '';
  mostrarForm = false;
  erro?: string;
  erroForm?: string;

  constructor(private service: CategoriaService) {}

  ngOnInit() {
    this.carregar();
  }

  carregar() {
    this.erro = undefined;
    this.service.listar().subscribe({
      next: data => this.categorias = data,
      error: err => this.erro = err.status ? 'Erro ao carregar categorias' : err.message
    });
  }

  salvar() {
    const nome = this.novoNome.trim().toUpperCase();
    if (!nome) return;
    this.erroForm = undefined;
    this.service.criar(nome).subscribe({
      next: () => {
        this.novoNome = '';
        this.mostrarForm = false;
        this.carregar();
      },
      error: err => {
        this.erroForm = err.error?.erro || err.error?.message || 'Erro ao criar categoria';
      }
    });
  }

  cancelar() {
    this.novoNome = '';
    this.mostrarForm = false;
    this.erroForm = undefined;
  }

  excluir(id: number) {
    if (confirm('Excluir categoria?')) {
      this.service.excluir(id).subscribe({
        next: () => this.categorias = this.categorias.filter(c => c.id !== id),
        error: err => this.erro = err.error?.erro || 'Erro ao excluir categoria'
      });
    }
  }
}
