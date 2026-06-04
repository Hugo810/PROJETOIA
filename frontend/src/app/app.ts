import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, RouterModule, RouterOutlet } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit, OnDestroy {
  isLogado = false;
  nome = '';
  role = '';
  roleLabel = '';
  private sub: Subscription = new Subscription();

  constructor(private auth: AuthService, private router: Router) {}

  ngOnInit() {
    this.sub = this.auth.authState$.subscribe(user => {
      this.isLogado = !!user;
      if (user) {
        this.nome = user.nome;
        this.role = user.role;
        this.roleLabel = user.role === 'ADMIN' ? 'Administrador'
          : user.role === 'DISTRIBUIDOR' ? 'Distribuidor'
          : 'Executor';
      } else {
        this.nome = '';
        this.role = '';
        this.roleLabel = '';
      }
    });

    const isLoginPage = window.location.pathname === '/login';
    if (!this.isLogado && !isLoginPage) {
      this.auth.logout();
      window.location.href = '/login';
    }
    if (this.isLogado && isLoginPage) {
      window.location.href = '/tarefas';
    }
  }

  ngOnDestroy() {
    this.sub.unsubscribe();
  }

  logout() {
    this.auth.logout();
    window.location.href = '/login';
  }
}
