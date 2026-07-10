export interface RegistroTempo {
  id?: number;
  tarefaId: number;
  tarefaTitulo?: string;
  usuarioId?: number;
  usuarioNome?: string;
  inicio?: string;
  fim?: string;
  duracaoMinutos?: number;
  descricao?: string;
  manual: boolean;
  emAndamento: boolean;
}
