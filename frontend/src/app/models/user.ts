export interface Usuario {
  id: number;
  nome: string;
  email: string;
  role: 'ADMIN' | 'DISTRIBUIDOR' | 'EXECUTOR';
}

export interface LoginRequest {
  email: string;
  senha: string;
}
