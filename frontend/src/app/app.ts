import { Component, OnInit } from '@angular/core';
import { Router, RouterModule, RouterOutlet } from '@angular/router';
import { AuthService } from './services/auth.service';
import { Notifications } from './components/notifications/notifications';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterModule, Notifications],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  isLogado = false;
  nome = '';
  role = '';
  roleLabel = '';

  constructor(private auth: AuthService, private router: Router) {}

  ngOnInit() {
    const user = this.auth.getUsuarioLogado();
    this.isLogado = !!user;
    if (user) {
      this.nome = user.nome;
      this.role = user.role;
      this.roleLabel = user.role === 'ADMIN' ? 'Administrador'
        : user.role === 'DISTRIBUIDOR' ? 'Distribuidor'
        : 'Executor';
    }

    const isLoginPage = this.router.url === '/login';
    if (!this.isLogado && !isLoginPage) {
      this.auth.logout();
      this.router.navigate(['/login']);
    }
    if (this.isLogado && isLoginPage) {
      this.router.navigate(['/']);
    }
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
