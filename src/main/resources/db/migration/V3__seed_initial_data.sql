INSERT INTO persona (nombre, genero, edad, identificacion, direccion, telefono)
VALUES
    ('Jose Lema', 'MASCULINO', 35, '0102030405', 'Otavalo sn y principal', '0991111111'),
    ('Marianela Montalvo', 'FEMENINO', 30, '1717171717', 'Amazonas y nnuu', '0992222222');

INSERT INTO cliente (persona_id, contrasena, estado)
VALUES
    (1, '1234', TRUE),
    (2, '5678', TRUE);

INSERT INTO cuenta (cliente_id, numero_cuenta, tipo_cuenta, saldo_inicial, estado)
VALUES
    (1, '478758', 'AHORROS', 2000.00, TRUE),
    (2, '225487', 'CORRIENTE', 100.00, TRUE);

INSERT INTO movimiento (cuenta_id, fecha, tipo_movimiento, valor, saldo)
VALUES
    (2, '2022-02-10 09:00:00', 'CREDITO', 600.00, 700.00);

