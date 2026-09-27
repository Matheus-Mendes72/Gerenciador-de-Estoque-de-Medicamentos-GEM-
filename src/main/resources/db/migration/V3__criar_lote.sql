CREATE TABLE lote (
    id BIGSERIAL PRIMARY KEY,
    medicamento_id BIGINT NOT NULL,
    numero_lote VARCHAR(100) NOT NULL,
    data_validade DATE NOT NULL,

    CONSTRAINT fk_lote_medicamento
        FOREIGN KEY (medicamento_id)
        REFERENCES medicamentos(id)
);