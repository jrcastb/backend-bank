package com.devsu.backendbank.infrastructure.output.repository.projection;

import com.devsu.backendbank.infrastructure.output.repository.entity.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TransactionReportProjection {

    LocalDateTime getFecha();

    String getCliente();

    String getNumeroCuenta();

    AccountType getTipoCuenta();

    BigDecimal getSaldoInicial();

    Boolean getEstadoCuenta();

    BigDecimal getMovimiento();

    BigDecimal getSaldoDisponible();
}

