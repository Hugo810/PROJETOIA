export interface RegraAutomacao {
  id?: number;
  nome: string;
  condicao: string;
  acao: string;
  ativa: boolean;
  dataCriacao?: string;
  criadorId?: number;
  criadorNome?: string;
}
