export interface Tarefa {
  id?: number;
  titulo: string;
  descricao?: string;
  categoriaId: number;
  categoriaNome?: string;
  prazo: string;
  status: string;
  dataCriacao?: string;
  dataConclusao?: string;
  responsavelId?: number;
  responsavelNome?: string;
  distribuidorId?: number;
}
