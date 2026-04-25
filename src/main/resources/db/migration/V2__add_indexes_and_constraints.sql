ALTER TABLE persona
    ADD CONSTRAINT uk_persona_identificacion UNIQUE (identificacion),
    ADD CONSTRAINT chk_persona_edad CHECK (edad >= 0 AND edad <= 130);

ALTER TABLE cliente
    ADD CONSTRAINT uk_cliente_persona UNIQUE (persona_id),
    ADD CONSTRAINT fk_cliente_persona FOREIGN KEY (persona_id) REFERENCES persona (id) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE cuenta
    ADD CONSTRAINT uk_cuenta_numero UNIQUE (numero_cuenta),
    ADD CONSTRAINT fk_cuenta_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    ADD CONSTRAINT chk_cuenta_tipo CHECK (tipo_cuenta IN ('AHORROS', 'CORRIENTE')),
    ADD CONSTRAINT chk_cuenta_saldo_inicial CHECK (saldo_inicial >= 0);

ALTER TABLE movimiento
    ADD CONSTRAINT fk_movimiento_cuenta FOREIGN KEY (cuenta_id) REFERENCES cuenta (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    ADD CONSTRAINT chk_movimiento_tipo CHECK (tipo_movimiento IN ('CREDITO', 'DEBITO')),
    ADD CONSTRAINT chk_movimiento_valor_signo CHECK (
        (tipo_movimiento = 'CREDITO' AND valor > 0)
        OR
        (tipo_movimiento = 'DEBITO' AND valor < 0)
    ),
    ADD CONSTRAINT chk_movimiento_saldo CHECK (saldo >= 0);

CREATE INDEX idx_cuenta_cliente_id ON cuenta (cliente_id);
CREATE INDEX idx_movimiento_fecha ON movimiento (fecha);
CREATE INDEX idx_movimiento_cuenta_fecha ON movimiento (cuenta_id, fecha);

