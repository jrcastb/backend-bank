package com.devsu.backendbank.application.output.port;

import com.devsu.backendbank.domain.model.AccountDomain;

public interface AccountCommandPort {
    boolean accountExistsByNumeroCuenta(String numeroCuenta);
    boolean accountExistsByNumeroCuentaExcludingId(String numeroCuenta, Long id);
    AccountDomain saveOrUpdateAccount(AccountDomain accountDomain);
    void deleteAccountById(Long id);
}

