export interface Projeto {
  id?: number;
  nome: string;
  descricao?: string;
  dataInicio?: string;
  dataFim?: string;
  status: string;
  responsavelId?: number;
  responsavelNome?: string;
  dataCriacao?: string;
  totalMetas?: number;
  metasConcluidas?: number;
  progressoGeral?: number;
}

export interface Meta {
  id?: number;
  titulo: string;
  descricao?: string;
  dataInicio?: string;
  dataFim?: string;
  status: string;
  progresso: number;
  projetoId?: number;
  projetoNome?: string;
}
