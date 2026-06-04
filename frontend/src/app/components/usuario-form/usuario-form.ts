import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { UsuarioService } from '../../services/usuario.service';
import { AuthService } from '../../services/auth.service';
import { Usuario } from '../../models/user';

@Component({
  selector: 'app-usuario-form',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterModule],
  template: `
    <div class="container">
      <header>
        <h1>{{ editando ? 'Editar Usuário' : 'Novo Usuário' }}</h1>
        <a routerLink="/usuarios" class="btn btn-secondary">Voltar</a>
      </header>

      @if (erro) {
        <div class="erro">{{ erro }}</div>
      }

      <form (ngSubmit)="salvar()">
        <div class="form-group">
          <label for="nome">Nome *</label>
          <input id="nome" [(ngModel)]="dto.nome" name="nome" required placeholder="Nome completo">
        </div>

        <div class="form-group">
          <label for="email">Email *</label>
          <input id="email" type="email" [(ngModel)]="dto.email" name="email" required placeholder="email@exemplo.com">
        </div>

        <div class="form-group">
          <label for="role">Função *</label>
          <select id="role" [(ngModel)]="dto.role" name="role" required>
            <option value="ADMIN">Administrador</option>
            <option value="DISTRIBUIDOR">Distribuidor</option>
            <option value="EXECUTOR">Executor</option>
          </select>
        </div>

        <div class="form-actions">
          <button type="submit" class="btn btn-primary">{{ editando ? 'Atualizar' : 'Criar' }}</button>
          <a routerLink="/usuarios" class="btn btn-secondary">Cancelar</a>
        </div>
      </form>
    </div>
  `,
  styles: [`
    .container { max-width: 500px; margin: 0 auto; padding: 20px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
    header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
    header h1 { margin: 0; font-size: 28px; color: #1a1a2e; }
    .erro { background: #fce4ec; color: #c62828; padding: 10px 14px; border-radius: 8px; margin-bottom: 16px; font-size: 14px; }
    .form-group { margin-bottom: 16px; }
    .form-group label { display: block; margin-bottom: 6px; font-size: 14px; font-weight: 500; color: #333; }
    .form-group input, .form-group select {
      width: 100%; padding: 10px 12px; border: 1px solid #ddd; border-radius: 8px; font-size: 14px; box-sizing: border-box;
    }
    .form-group input { text-transform: uppercase; }
    .form-group input:focus, .form-group select:focus {
      outline: none; border-color: #4361ee; box-shadow: 0 0 0 3px rgba(67,97,238,0.15);
    }
    .form-actions { display: flex; gap: 8px; margin-top: 24px; }
    .btn {
      padding: 10px 20px; border: none; border-radius: 8px; cursor: pointer; font-size: 14px;
      text-decoration: none; display: inline-block; transition: opacity 0.2s;
    }
    .btn-primary { background: #4361ee; color: white; }
    .btn-secondary { background: #6c757d; color: white; }
    .btn:hover { opacity: 0.85; }
  `]
})
export class UsuarioForm implements OnInit {
  dto: Partial<Usuario> = { nome: '', email: '', role: 'EXECUTOR' };
  editando = false;
  erro = '';
  private usuarioId?: number;

  constructor(
    private service: UsuarioService,
    private auth: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    const user = this.auth.getUsuarioLogado();
    if (!user || user.role !== 'ADMIN') {
      window.location.href = '/usuarios';
      return;
    }
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.editando = true;
      this.usuarioId = +id;
      this.service.listar().subscribe({
        next: users => {
          const u = users.find(x => x.id === this.usuarioId);
          if (u) this.dto = { ...u };
          else this.erro = 'Usuário não encontrado.';
        },
        error: () => this.erro = 'Erro ao carregar dados do usuário.'
      });
    }
  }

  salvar() {
    if (this.editando && !confirm('Deseja realmente salvar as alterações?')) return;
    this.erro = '';
    this.dto.nome = this.dto.nome?.toUpperCase();
    this.dto.email = this.dto.email?.toUpperCase();
    const obs = this.editando
      ? this.service.atualizar(this.usuarioId!, this.dto)
      : this.service.criar(this.dto);

    obs.subscribe({
      next: () => this.router.navigate(['/usuarios']),
      error: (err) => this.erro = err.error?.erro || 'Erro ao salvar.'
    });
  }
}
