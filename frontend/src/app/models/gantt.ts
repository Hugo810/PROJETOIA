export interface GanttTarefa {
  id: number;
  titulo: string;
  prazo: string;
  duracaoDias: number;
  status: string;
  prioridade: string;
  responsavel?: string;
  responsavelId?: number;
  categoria?: string;
}

export interface Dependencia {
  id: number;
  tarefaId: number;
  tarefaTitulo: string;
  tarefaDependenteId: number;
  tarefaDependenteTitulo: string;
  tipo: string;
}

export interface Marco {
  id?: number;
  nome: string;
  data: string;
  criadorId?: number;
  criadorNome?: string;
}

export interface GanttData {
  tarefas: GanttTarefa[];
  dependencias: Dependencia[];
  marcos: Marco[];
}
