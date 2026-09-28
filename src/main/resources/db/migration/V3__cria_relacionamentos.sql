ALTER TABLE historico
ADD CONSTRAINT fk_historico_estoque
FOREIGN KEY (id_estoque)
REFERENCES estoque(id);

ALTER TABLE historico
ADD CONSTRAINT fk_historico_usuario
FOREIGN KEY (id_usuario)
REFERENCES usuarios(id);