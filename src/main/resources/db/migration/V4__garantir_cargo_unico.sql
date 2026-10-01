ALTER TABLE usuarios
ADD CONSTRAINT uk_usuarios_cargo UNIQUE (cargo);