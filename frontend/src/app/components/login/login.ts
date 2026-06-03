import { Component } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterModule],
  template: `
    <div class="login-wrapper">
      <div class="login-card">
        <h1>TaskFlow</h1>
        <p class="subtitle">Faça login para continuar</p>

        <form (ngSubmit)="entrar()">
          <div class="form-group">
            <label for="email">Email</label>
            <input id="email" type="email" [(ngModel)]="email" name="email" placeholder="admin@taskflow.com" required>
          </div>

          <div class="form-group">
            <label for="senha">Senha</label>
            <input id="senha" type="password" [(ngModel)]="senha" name="senha" placeholder="••••••" required>
          </div>

          <button type="submit" class="btn btn-primary btn-full">Entrar</button>

          @if (mensagemErro) {
            <div class="erro">{{ mensagemErro }}</div>
          }
        </form>

        <div class="login-hint">
          <p><strong>Admin:</strong> admin@taskflow.com / admin</p>
          <p><strong>Distribuidor:</strong> dist@taskflow.com / 123456</p>
          <p><strong>Usuário:</strong> user@taskflow.com / 123456</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .login-wrapper {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      padding: 20px;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    }
    .login-card {
      background: white;
      padding: 40px;
      border-radius: 16px;
      box-shadow: 0 20px 60px rgba(0,0,0,0.15);
      width: 100%;
      max-width: 400px;
    }
    .login-card h1 {
      margin: 0 0 4px;
      font-size: 28px;
      color: #1a1a2e;
      text-align: center;
    }
    .subtitle {
      text-align: center;
      color: #6c757d;
      margin: 0 0 24px;
      font-size: 14px;
    }
    .erro {
      background: #fce4ec;
      color: #c62828;
      padding: 10px 14px;
      border-radius: 8px;
      margin-bottom: 16px;
      font-size: 14px;
    }
    .form-group {
      margin-bottom: 16px;
    }
    .form-group label {
      display: block;
      margin-bottom: 6px;
      font-size: 14px;
      font-weight: 500;
      color: #333;
    }
    .form-group input {
      width: 100%;
      padding: 10px 12px;
      border: 1px solid #ddd;
      border-radius: 8px;
      font-size: 14px;
      box-sizing: border-box;
      text-transform: uppercase;
    }
    .form-group input:focus {
      outline: none;
      border-color: #667eea;
      box-shadow: 0 0 0 3px rgba(102,126,234,0.15);
    }
    .btn {
      padding: 10px 20px;
      border: none;
      border-radius: 8px;
      cursor: pointer;
      font-size: 15px;
      font-weight: 600;
      transition: opacity 0.2s;
    }
    .btn-primary { background: #667eea; color: white; }
    .btn-full { width: 100%; margin-top: 8px; }
    .btn:hover { opacity: 0.85; }
    .login-hint {
      margin-top: 24px;
      padding-top: 16px;
      border-top: 1px solid #eee;
      font-size: 12px;
      color: #999;
      text-align: center;
    }
    .login-hint p { margin: 2px 0; }
    .login-hint strong { color: #666; }
  `]
})
export class Login {
  email = '';
  senha = '';
  mensagemErro = '';

  constructor(private auth: AuthService, private router: Router) {}

  entrar() {
    this.mensagemErro = '';
    this.auth.login({ email: this.email.toUpperCase(), senha: this.senha }).subscribe({
      next: () => this.router.navigate(['/']),
      error: (err) => this.mensagemErro = err.error?.erro || 'Falha no login'
    });
  }
}
