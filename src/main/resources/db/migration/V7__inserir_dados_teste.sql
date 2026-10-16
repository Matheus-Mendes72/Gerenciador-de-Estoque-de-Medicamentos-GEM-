-- 1. Inserir alguns medicamentos no estoque base
INSERT INTO estoque (principio_ativo, dose, tipo_med, quantidade) VALUES
('Dipirona', '500mg', 'ANTIBIOTICO', 100),
('Paracetamol', '750mg', 'ANTIBIOTICO', 150),
('Clonazepam', '2mg', 'CONTROLADO', 50),
('Amoxicilina', '500mg', 'ANTIBIOTICO', 80);

-- 2. Inserir histórico de movimentações simulando datas diferentes
DO $$ 
DECLARE 
    id_usr INT;
    id_dipirona INT;
    id_paracetamol INT;
    id_clonazepam INT;
BEGIN 
    SELECT id INTO id_usr FROM usuarios WHERE cargo = 'COORDENADOR' LIMIT 1;
    SELECT id INTO id_dipirona FROM estoque WHERE principio_ativo = 'Dipirona' LIMIT 1;
    SELECT id INTO id_paracetamol FROM estoque WHERE principio_ativo = 'Paracetamol' LIMIT 1;
    SELECT id INTO id_clonazepam FROM estoque WHERE principio_ativo = 'Clonazepam' LIMIT 1;

    -- Saída de Dipirona (Ontem)
    INSERT INTO historico (tipo_mov, id_estoque, quantidade, data_mov, id_usuario, setor_hosp, nome_func, motivo_obs, quantidade_pre, quantidade_pos)
    VALUES ('SAIDA', id_dipirona, 10, NOW() - INTERVAL '1 day', id_usr, 'EMERGENCIA', 'COORDENADOR', 'Paciente com febre', 110, 100);

    -- Saída de Clonazepam (Semana passada)
    INSERT INTO historico (tipo_mov, id_estoque, quantidade, data_mov, id_usuario, setor_hosp, nome_func, motivo_obs, quantidade_pre, quantidade_pos)
    VALUES ('SAIDA', id_clonazepam, 5, NOW() - INTERVAL '7 days', id_usr, 'CLINICA_MEDICA', 'COORDENADOR', 'Prescrição psiquiátrica', 55, 50);

    -- Retorno de Paracetamol (Hoje)
    INSERT INTO historico (tipo_mov, id_estoque, quantidade, data_mov, id_usuario, setor_hosp, nome_func, motivo_obs, quantidade_pre, quantidade_pos)
    VALUES ('RETORNO', id_paracetamol, 2, NOW(), id_usr, 'PEDIATRIA', 'COORDENADOR', 'Sobra de plantão', 148, 150);
    
    -- Saída de Paracetamol (Hoje, algumas horas atrás)
    INSERT INTO historico (tipo_mov, id_estoque, quantidade, data_mov, id_usuario, setor_hosp, nome_func, motivo_obs, quantidade_pre, quantidade_pos)
    VALUES ('SAIDA', id_paracetamol, 20, NOW() - INTERVAL '5 hours', id_usr, 'PEDIATRIA', 'COORDENADOR', 'Administração geral', 168, 148);

END $$;
