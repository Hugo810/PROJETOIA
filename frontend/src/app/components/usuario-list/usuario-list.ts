import { Component, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { UsuarioService } from '../../services/usuario.service';
import { AuthService } from '../../services/auth.service';
import { Usuario } from '../../models/user';

@Component({
  selector: 'app-usuario-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="container">
      <header>
        <h1>Usuários</h1>
        <div class="header-actions">
          @if (isAdmin) {
            <button class="btn btn-primary" routerLink="/usuarios/novo">Novo Usuário</button>
          }
          <button class="btn btn-secondary" (click)="recarregar()">Recarregar</button>
          <button class="btn btn-secondary" routerLink="/">Voltar</button>
        </div>
      </header>

      @if (erro) {
        <div class="erro">{{ erro }}</div>
      } @else if (usuarios.length === 0) {
        <div class="empty">Nenhum usuário encontrado.</div>
      }

      <div class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Nome</th>
              <th>Email</th>
              <th>Função</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            @for (u of usuarios; track u.id) {
              <tr>
                <td>{{ u.id }}</td>
                <td>{{ u.nome }} @if (u.id === meuId) { <span class="voce">(você)</span> }</td>
                <td>{{ u.email }}</td>
                <td>
                  <span class="role-badge" [class.admin]="u.role === 'ADMIN'" [class.dist]="u.role === 'DISTRIBUIDOR'" [class.executor]="u.role === 'EXECUTOR'">
                    {{ u.role === 'ADMIN' ? 'Administrador' : u.role === 'DISTRIBUIDOR' ? 'Distribuidor' : 'Executor' }}
                  </span>
                </td>
                <td class="actions">
                  @if (isAdmin) {
                    <button class="btn btn-sm btn-secondary" [routerLink]="['/usuarios/editar', u.id]">Editar</button>
                    <button class="btn btn-sm btn-danger" (click)="excluir(u.id!)" [disabled]="u.id === meuId">Excluir</button>
                  }
                </td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .container { max-width: 800px; margin: 0 auto; padding: 20px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
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
    .table-wrapper { overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
    th, td { padding: 12px 16px; text-align: left; font-size: 14px; }
    th { background: #f8f9fa; color: #333; font-weight: 600; border-bottom: 2px solid #e8e8e8; }
    td { border-bottom: 1px solid #f0f0f0; }
    tr:last-child td { border-bottom: none; }
    .actions { display: flex; gap: 6px; }
    .role-badge { padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; }
    .role-badge.admin { background: #e0e7ff; color: #4361ee; }
    .role-badge.dist { background: #fff3cd; color: #856404; }
    .role-badge.executor { background: #e8f5e9; color: #2e7d32; }
    .empty { text-align: center; padding: 40px; color: #6c757d; font-size: 16px; }
    .erro { text-align: center; padding: 40px; color: #e63946; font-size: 16px; }
    .voce { color: #4361ee; font-weight: 600; font-size: 12px; }
  `]
})
export class UsuarioList implements OnInit {
  usuarios: Usuario[] = [];
  meuId?: number;
  isAdmin = false;
  erro?: string;
  carregando = true;

  constructor(
    private service: UsuarioService,
    private auth: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    const user = this.auth.getUsuarioLogado();
    if (!user) {
      window.location.href = '/login';
      return;
    }
    this.meuId = user.id;
    this.isAdmin = user.role === 'ADMIN';
    this.carregar();
  }

  private carregar() {
    this.carregando = true;
    this.erro = undefined;
    this.service.listar().subscribe({
      next: data => { this.usuarios = data; this.carregando = false; },
      error: (err) => {
        this.carregando = false;
        this.erro = err.message || err.status || 'Erro ao carregar usuarios';
        if (err.status === 403 || err.status === 400) {
          this.auth.logout();
        }
      }
    });
  }

  recarregar() {
    this.carregar();
  }

  excluir(id: number) {
    if (confirm('Excluir usuário?')) {
      this.service.excluir(id).subscribe(() => {
        this.usuarios = this.usuarios.filter(u => u.id !== id);
      });
    }
  }
}
