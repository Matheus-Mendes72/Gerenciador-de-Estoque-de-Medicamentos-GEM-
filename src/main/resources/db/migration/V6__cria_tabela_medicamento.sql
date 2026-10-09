CREATE TABLE medicamentos (
    id SERIAL PRIMARY KEY,
    principio_ativo VARCHAR(50) NOT NULL,
    dose VARCHAR(50) NOT NULL,
    tipo_med tipo_med NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Migra dados existentes de estoque para a tabela medicamentos (se houver)
INSERT INTO medicamentos (principio_ativo, dose, tipo_med, ativo)
SELECT DISTINCT principio_ativo, dose, tipo_med, TRUE
FROM estoque;

-- Adiciona a chave estrangeira na tabela de estoque
ALTER TABLE estoque
ADD COLUMN id_medicamento INT;

-- Vincula os estoques existentes aos seus respectivos medicamentos
UPDATE estoque e
SET id_medicamento = m.id
FROM medicamentos m
WHERE e.principio_ativo = m.principio_ativo
  AND e.dose = m.dose
  AND e.tipo_med = m.tipo_med;

-- Cria a constraint de chave estrangeira entre estoque e medicamentos
ALTER TABLE estoque
ADD CONSTRAINT fk_estoque_medicamento
FOREIGN KEY (id_medicamento)
REFERENCES medicamentos(id);
