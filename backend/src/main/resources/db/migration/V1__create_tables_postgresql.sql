-- ============================================
-- TaskFlow - Migração H2 (Oracle) -> PostgreSQL 17
-- Arquivo: V1__create_tables_postgresql.sql
-- Data: 2026-07-10
-- ============================================

-- Criar schema se necessário
-- CREATE SCHEMA IF NOT EXISTS taskflow;

-- ============================================
-- TABELA: CATEGORIA
-- ============================================
CREATE TABLE IF NOT EXISTS categoria (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE
);

-- Comentário na tabela
COMMENT ON TABLE categoria IS 'Tabela de categorias de tarefas';
COMMENT ON COLUMN categoria.id IS 'Identificador único da categoria';
COMMENT ON COLUMN categoria.nome IS 'Nome da categoria (único)';

-- ============================================
-- TABELA: USUARIO
-- ============================================
CREATE TABLE IF NOT EXISTS usuario (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL
);

-- Comentário na tabela
COMMENT ON TABLE usuario IS 'Tabela de usuários do sistema';
COMMENT ON COLUMN usuario.id IS 'Identificador único do usuário';
COMMENT ON COLUMN usuario.nome IS 'Nome completo do usuário';
COMMENT ON COLUMN usuario.email IS 'Email do usuário (único)';
COMMENT ON COLUMN usuario.senha IS 'Senha do usuário (deve ser hasheada)';
COMMENT ON COLUMN usuario.role IS 'Papel do usuário: ADMIN, DISTRIBUIDOR, EXECUTOR';

-- ============================================
-- TABELA: TAREFA
-- ============================================
CREATE TABLE IF NOT EXISTS tarefa (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    descricao VARCHAR(1000),
    categoria_id BIGINT NOT NULL,
    prazo DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_conclusao TIMESTAMP,
    responsavel_id BIGINT,
    distribuidor_id BIGINT,
    
    -- Constraints de chaves estrangeiras
    CONSTRAINT fk_tarefa_categoria 
        FOREIGN KEY (categoria_id) 
        REFERENCES categoria(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    
    CONSTRAINT fk_tarefa_responsavel 
        FOREIGN KEY (responsavel_id) 
        REFERENCES usuario(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

-- Comentário na tabela
COMMENT ON TABLE tarefa IS 'Tabela principal de tarefas';
COMMENT ON COLUMN tarefa.id IS 'Identificador único da tarefa';
COMMENT ON COLUMN tarefa.titulo IS 'Título da tarefa';
COMMENT ON COLUMN tarefa.descricao IS 'Descrição detalhada da tarefa';
COMMENT ON COLUMN tarefa.categoria_id IS 'FK para categoria';
COMMENT ON COLUMN tarefa.prazo IS 'Data limite para conclusão';
COMMENT ON COLUMN tarefa.status IS 'Status: PENDENTE, EM_EXECUCAO, CONCLUIDA';
COMMENT ON COLUMN tarefa.data_criacao IS 'Data/hora de criação (automático)';
COMMENT ON COLUMN tarefa.data_conclusao IS 'Data/hora de conclusão';
COMMENT ON COLUMN tarefa.responsavel_id IS 'FK para usuário responsável';
COMMENT ON COLUMN tarefa.distribuidor_id IS 'ID do usuário distribuidor (sem FK)';

-- ============================================
-- ÍNDICES PARA PERFORMANCE
-- ============================================

-- Índice para busca por status (filtro frequente)
CREATE INDEX idx_tarefa_status ON tarefa(status);

-- Índice para busca por categoria (filtro frequente)
CREATE INDEX idx_tarefa_categoria ON tarefa(categoria_id);

-- Índice para busca por prazo (ordenção e filtros)
CREATE INDEX idx_tarefa_prazo ON tarefa(prazo);

-- Índice para busca por responsável
CREATE INDEX idx_tarefa_responsavel ON tarefa(responsavel_id);

-- Índice para busca por data de criação
CREATE INDEX idx_tarefa_data_criacao ON tarefa(data_criacao);

-- ============================================
-- VALIDAÇÕES (CHECK CONSTRAINTS)
-- ============================================

-- Validar valores de status
ALTER TABLE tarefa 
    ADD CONSTRAINT chk_tarefa_status 
    CHECK (status IN ('PENDENTE', 'EM_EXECUCAO', 'CONCLUIDA'));

-- Validar valores de role
ALTER TABLE usuario 
    ADD CONSTRAINT chk_usuario_role 
    CHECK (role IN ('ADMIN', 'DISTRIBUIDOR', 'EXECUTOR'));

-- ============================================
-- DADOS INICIAIS (SEED)
-- ============================================

-- Categorias iniciais
INSERT INTO categoria (nome) VALUES 
    ('TRABALHO'),
    ('ESTUDOS'),
    ('PESSOAL'),
    ('URGENTE'),
    ('SAUDE'),
    ('FINANCEIRO')
ON CONFLICT (nome) DO NOTHING;

-- Usuários iniciais (senhas devem ser hasheadas em produção)
INSERT INTO usuario (nome, email, senha, role) VALUES 
    ('Admin', 'admin@taskflow.com', 'admin123', 'ADMIN'),
    ('Distribuidor', 'distribuidor@taskflow.com', 'dist123', 'DISTRIBUIDOR'),
    ('Executor', 'executor@taskflow.com', 'exec123', 'EXECUTOR')
ON CONFLICT (email) DO NOTHING;

-- ============================================
-- GRANTS (opcional - ajustar conforme necessidade)
-- ============================================

-- Se precisar de um usuário dedicado para a aplicação:
-- CREATE USER taskflow_user WITH PASSWORD 'senha_segura';
-- GRANT USAGE ON SCHEMA public TO taskflow_user;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO taskflow_user;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO taskflow_user;
