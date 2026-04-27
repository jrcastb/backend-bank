-- =============================================================================
-- BaseDatos.sql
-- Proyecto: Backend Bank - API REST de Gestión Bancaria
-- Autor:    Devsu
-- Fecha:    2026-04-27
-- DBMS:     MySQL 8.x
--
-- Descripción:
--   Script completo de base de datos para el sistema bancario Backend Bank.
--   Incluye: creación de esquema, tablas, restricciones, índices y datos
--   iniciales de prueba compatibles con el enunciado del ejercicio técnico.
--
-- Uso:
--   mysql -u<usuario> -p<contraseña> < BaseDatos.sql
--   mysql -uroot -p < BaseDatos.sql
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 0. CONFIGURACIÓN INICIAL DE SESIÓN
-- -----------------------------------------------------------------------------
SET @OLD_UNIQUE_CHECKS    = @@UNIQUE_CHECKS,    UNIQUE_CHECKS    = 0;
SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0;
SET @OLD_SQL_MODE         = @@SQL_MODE,         SQL_MODE         = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------------------------------
-- 1. CREACIÓN / SELECCIÓN DE ESQUEMA
-- -----------------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS `backend_bank`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `backend_bank`;

-- -----------------------------------------------------------------------------
-- 2. TABLAS PRINCIPALES
-- -----------------------------------------------------------------------------

-- 2.1 persona
--   Representa los datos personales de cualquier individuo en el sistema.
--   Es la entidad base de la que hereda cliente (patrón table-per-type).
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `persona` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT   COMMENT 'Identificador interno autoincremental',
    `nombre`         VARCHAR(120) NOT NULL                  COMMENT 'Nombre completo de la persona',
    `genero`         VARCHAR(20)  NOT NULL                  COMMENT 'Género: MASCULINO | FEMENINO | OTRO',
    `edad`           INT          NOT NULL                  COMMENT 'Edad en años (0-130)',
    `identificacion` VARCHAR(50)  NOT NULL                  COMMENT 'Número de cédula o pasaporte (único)',
    `direccion`      VARCHAR(180) NOT NULL                  COMMENT 'Dirección domiciliaria',
    `telefono`       VARCHAR(30)  NOT NULL                  COMMENT 'Número de contacto',
    `created_at`     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP                    COMMENT 'Fecha de creación del registro',
    `updated_at`     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Fecha de última modificación',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Datos personales base de cada individuo registrado en el sistema';

-- 2.2 cliente
--   Extiende persona con credenciales y estado de acceso al sistema.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `cliente` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT   COMMENT 'Identificador interno autoincremental',
    `persona_id` BIGINT       NOT NULL                  COMMENT 'FK → persona.id (relación 1:1)',
    `contrasena` VARCHAR(255) NOT NULL                  COMMENT 'Contraseña de acceso (plaintext en demo; usar hash en producción)',
    `estado`     BOOLEAN      NOT NULL DEFAULT TRUE     COMMENT 'TRUE = activo, FALSE = inactivo',
    `created_at` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP                    COMMENT 'Fecha de creación del registro',
    `updated_at` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Fecha de última modificación',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Datos de acceso y estado del cliente bancario';

-- 2.3 cuenta
--   Cuenta bancaria (Ahorros o Corriente) asociada a un cliente.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `cuenta` (
    `id`            BIGINT         NOT NULL AUTO_INCREMENT   COMMENT 'Identificador interno autoincremental',
    `cliente_id`    BIGINT         NOT NULL                  COMMENT 'FK → cliente.id',
    `numero_cuenta` VARCHAR(30)    NOT NULL                  COMMENT 'Número de cuenta (único en el sistema)',
    `tipo_cuenta`   VARCHAR(20)    NOT NULL                  COMMENT 'Tipo: AHORROS | CORRIENTE',
    `saldo_inicial` DECIMAL(19,2)  NOT NULL                  COMMENT 'Saldo de apertura (≥ 0)',
    `estado`        BOOLEAN        NOT NULL DEFAULT TRUE     COMMENT 'TRUE = activa, FALSE = inactiva',
    `created_at`    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP                    COMMENT 'Fecha de creación del registro',
    `updated_at`    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Fecha de última modificación',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Cuentas bancarias registradas en el sistema';

-- 2.4 movimiento
--   Registra cada débito o crédito realizado sobre una cuenta.
--   El saldo refleja el balance DESPUÉS de aplicar el movimiento.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `movimiento` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT   COMMENT 'Identificador interno autoincremental',
    `cuenta_id`       BIGINT        NOT NULL                  COMMENT 'FK → cuenta.id',
    `fecha`           TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha y hora del movimiento',
    `tipo_movimiento` VARCHAR(20)   NOT NULL                  COMMENT 'Tipo: CREDITO | DEBITO',
    `valor`           DECIMAL(19,2) NOT NULL                  COMMENT 'Monto: positivo para CREDITO, negativo para DEBITO',
    `saldo`           DECIMAL(19,2) NOT NULL                  COMMENT 'Saldo resultante después del movimiento (≥ 0)',
    `created_at`      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de inserción del registro',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Historial de movimientos (transacciones) por cuenta';

-- -----------------------------------------------------------------------------
-- 3. RESTRICCIONES DE INTEGRIDAD (CONSTRAINTS)
-- -----------------------------------------------------------------------------

-- persona: identificación debe ser única en todo el sistema
ALTER TABLE `persona`
    ADD CONSTRAINT `uk_persona_identificacion`
        UNIQUE (`identificacion`),
    ADD CONSTRAINT `chk_persona_edad`
        CHECK (`edad` >= 0 AND `edad` <= 130);

-- cliente: un cliente está vinculado a exactamente una persona (1:1)
ALTER TABLE `cliente`
    ADD CONSTRAINT `uk_cliente_persona`
        UNIQUE (`persona_id`),
    ADD CONSTRAINT `fk_cliente_persona`
        FOREIGN KEY (`persona_id`) REFERENCES `persona` (`id`)
        ON DELETE RESTRICT
        ON UPDATE CASCADE;

-- cuenta: número de cuenta único; tipo y saldo inicial con validación
ALTER TABLE `cuenta`
    ADD CONSTRAINT `uk_cuenta_numero`
        UNIQUE (`numero_cuenta`),
    ADD CONSTRAINT `fk_cuenta_cliente`
        FOREIGN KEY (`cliente_id`) REFERENCES `cliente` (`id`)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    ADD CONSTRAINT `chk_cuenta_tipo`
        CHECK (`tipo_cuenta` IN ('AHORROS', 'CORRIENTE')),
    ADD CONSTRAINT `chk_cuenta_saldo_inicial`
        CHECK (`saldo_inicial` >= 0);

-- movimiento: integridad referencial, tipo y validación de signo vs. tipo
ALTER TABLE `movimiento`
    ADD CONSTRAINT `fk_movimiento_cuenta`
        FOREIGN KEY (`cuenta_id`) REFERENCES `cuenta` (`id`)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    ADD CONSTRAINT `chk_movimiento_tipo`
        CHECK (`tipo_movimiento` IN ('CREDITO', 'DEBITO')),
    ADD CONSTRAINT `chk_movimiento_valor_signo`
        CHECK (
            (`tipo_movimiento` = 'CREDITO' AND `valor` > 0)
            OR
            (`tipo_movimiento` = 'DEBITO'  AND `valor` < 0)
        ),
    ADD CONSTRAINT `chk_movimiento_saldo`
        CHECK (`saldo` >= 0);

-- -----------------------------------------------------------------------------
-- 4. ÍNDICES DE RENDIMIENTO
-- -----------------------------------------------------------------------------

-- Consultas de cuentas por cliente (listado, reportes)
CREATE INDEX `idx_cuenta_cliente_id`      ON `cuenta`     (`cliente_id`);

-- Consultas de movimientos por rango de fechas (reportes)
CREATE INDEX `idx_movimiento_fecha`       ON `movimiento` (`fecha`);

-- Consultas combinadas cuenta + fecha (reporte estado de cuenta)
CREATE INDEX `idx_movimiento_cuenta_fecha` ON `movimiento` (`cuenta_id`, `fecha`);

-- -----------------------------------------------------------------------------
-- 5. DATOS INICIALES DE PRUEBA
--    Corresponden al enunciado del ejercicio técnico Devsu.
-- -----------------------------------------------------------------------------

-- 5.1 Personas
INSERT INTO `persona` (`nombre`, `genero`, `edad`, `identificacion`, `direccion`, `telefono`)
VALUES
    ('Jose Lema',           'MASCULINO', 35, '0102030405', 'Otavalo sn y principal', '0991111111'),
    ('Marianela Montalvo',  'FEMENINO',  30, '1717171717', 'Amazonas y nnuu',        '0992222222');

-- 5.2 Clientes (vinculados a las personas anteriores en mismo orden)
INSERT INTO `cliente` (`persona_id`, `contrasena`, `estado`)
VALUES
    (1, '1234', TRUE),   -- Jose Lema
    (2, '5678', TRUE);   -- Marianela Montalvo

-- 5.3 Cuentas bancarias
INSERT INTO `cuenta` (`cliente_id`, `numero_cuenta`, `tipo_cuenta`, `saldo_inicial`, `estado`)
VALUES
    (1, '478758', 'AHORROS',   2000.00, TRUE),  -- Cuenta de Jose Lema
    (2, '225487', 'CORRIENTE',  100.00, TRUE);  -- Cuenta de Marianela Montalvo

-- 5.4 Movimientos iniciales
INSERT INTO `movimiento` (`cuenta_id`, `fecha`, `tipo_movimiento`, `valor`, `saldo`)
VALUES
    (2, '2022-02-10 09:00:00', 'CREDITO', 600.00, 700.00);  -- Crédito en cuenta de Marianela

-- -----------------------------------------------------------------------------
-- 6. RESTAURAR CONFIGURACIÓN DE SESIÓN
-- -----------------------------------------------------------------------------
SET UNIQUE_CHECKS     = @OLD_UNIQUE_CHECKS;
SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;
SET SQL_MODE          = @OLD_SQL_MODE;

-- -----------------------------------------------------------------------------
-- FIN DEL SCRIPT
-- =============================================================================

