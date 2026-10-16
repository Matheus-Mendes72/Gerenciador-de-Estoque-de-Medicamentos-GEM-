-- Adiciona campos de especificações exigidos pelos critérios de aceitação
ALTER TABLE medicamentos ADD COLUMN IF NOT EXISTS laboratorio VARCHAR(100);
ALTER TABLE estoque ADD COLUMN IF NOT EXISTS lote VARCHAR(50);
ALTER TABLE estoque ADD COLUMN IF NOT EXISTS data_validade DATE;

-- Garante que medicamentos existentes no estoque estejam presentes no catálogo de medicamentos
INSERT INTO medicamentos (principio_ativo, dose, tipo_med, ativo, laboratorio)
SELECT DISTINCT e.principio_ativo, e.dose, e.tipo_med, TRUE, 'LABORATÓRIO PADRÃO'
FROM estoque e
WHERE NOT EXISTS (
    SELECT 1 FROM medicamentos m 
    WHERE m.principio_ativo = e.principio_ativo 
      AND m.dose = e.dose 
      AND m.tipo_med = e.tipo_med
);

-- Vincula a FK id_medicamento no estoque caso ainda não esteja associada
UPDATE estoque e
SET id_medicamento = m.id
FROM medicamentos m
WHERE e.id_medicamento IS NULL
  AND e.principio_ativo = m.principio_ativo
  AND e.dose = m.dose
  AND e.tipo_med = m.tipo_med;
