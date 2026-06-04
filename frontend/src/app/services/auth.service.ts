import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Usuario, LoginRequest } from '../models/user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private baseUrl = '/api/auth';
  private storageKey = 'taskflow_user';
  private authSubject = new BehaviorSubject<Usuario | null>(this.carregarUsuario());

  authState$ = this.authSubject.asObservable();

  constructor(private http: HttpClient) {}

  private carregarUsuario(): Usuario | null {
    const data = localStorage.getItem(this.storageKey);
    return data ? JSON.parse(data) : null;
  }

  login(req: LoginRequest): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.baseUrl}/login`, req).pipe(
      tap(user => {
        localStorage.setItem(this.storageKey, JSON.stringify(user));
        this.authSubject.next(user);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.storageKey);
    this.authSubject.next(null);
  }

  getUsuarioLogado(): Usuario | null {
    return this.authSubject.getValue();
  }

  isLogado(): boolean {
    return this.authSubject.getValue() !== null;
  }

  temRole(...roles: string[]): boolean {
    const user = this.authSubject.getValue();
    return user ? roles.includes(user.role) : false;
  }
}
