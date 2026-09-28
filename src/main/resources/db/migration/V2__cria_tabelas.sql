CREATE TABLE estoque (
    id SERIAL PRIMARY KEY,
    principio_ativo VARCHAR(50) NOT NULL,
    dose VARCHAR(50) NOT NULL,
    tipo_med tipo_med NOT NULL,
    quantidade INT NOT NULL
);

CREATE TABLE usuarios (
    id SERIAL PRIMARY KEY,
    cargo cargo NOT NULL,
    senha_hash VARCHAR(255) NOT NULL
);

CREATE TABLE historico (
    id SERIAL PRIMARY KEY,
    tipo_mov tipo_mov NOT NULL,
    id_estoque INT NOT NULL,
    quantidade INT NOT NULL,
    data_mov DATE NOT NULL,
    id_usuario INT NOT NULL,
    setor_hosp setor_hosp NOT NULL,
    nome_func VARCHAR(50) NOT NULL,
    motivo_obs VARCHAR(100),
    quantidade_pre INT NOT NULL,
    quantidade_pos INT NOT NULL
);