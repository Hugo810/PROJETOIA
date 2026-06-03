import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Usuario, LoginRequest } from '../models/user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private baseUrl = '/api/auth';
  private storageKey = 'taskflow_user';

  constructor(private http: HttpClient) {}

  login(req: LoginRequest): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.baseUrl}/login`, req).pipe(
      tap(user => localStorage.setItem(this.storageKey, JSON.stringify(user)))
    );
  }

  logout(): void {
    localStorage.removeItem(this.storageKey);
  }

  getUsuarioLogado(): Usuario | null {
    const data = localStorage.getItem(this.storageKey);
    return data ? JSON.parse(data) : null;
  }

  isLogado(): boolean {
    return this.getUsuarioLogado() !== null;
  }

  temRole(...roles: string[]): boolean {
    const user = this.getUsuarioLogado();
    return user ? roles.includes(user.role) : false;
  }
}
