export interface TarefaRecorrente {
  id?: number;
  tarefaModeloId: number;
  tarefaModeloTitulo?: string;
  categoriaId?: number;
  categoriaNome?: string;
  descricao?: string;
  recorrencia: string;
  configuracao?: string;
  proximaExecucao: string;
  ativa: boolean;
  dataCriacao?: string;
  criadorId?: number;
  criadorNome?: string;
  proximasExecucoes?: string[];
}
