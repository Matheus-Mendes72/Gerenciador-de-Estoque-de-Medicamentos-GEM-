ALTER TABLE historico
ADD COLUMN medico_prescritor VARCHAR(100);

-- Preenche registros existentes com valor padrão
UPDATE historico
SET medico_prescritor = 'NÃO INFORMADO'
WHERE medico_prescritor IS NULL;

ALTER TABLE historico
ALTER COLUMN medico_prescritor SET NOT NULL;
