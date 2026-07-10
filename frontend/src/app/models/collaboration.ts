export interface Comentario {
  id?: number;
  texto: string;
  tarefaId: number;
  autorId?: number;
  autorNome?: string;
  dataCriacao?: string;
  dataAtualizacao?: string;
}

export interface Notificacao {
  id?: number;
  mensagem: string;
  usuarioId: number;
  tarefaId?: number;
  tarefaTitulo?: string;
  lida: boolean;
  dataCriacao?: string;
  tipo: string;
}

export interface Historico {
  id?: number;
  tarefaId: number;
  tarefaTitulo?: string;
  usuarioId: number;
  usuarioNome?: string;
  campo: string;
  valorAnterior?: string;
  valorNovo?: string;
  dataAlteracao?: string;
}
