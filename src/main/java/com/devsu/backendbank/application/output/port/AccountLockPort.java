package com.devsu.backendbank.application.output.port;

import com.devsu.backendbank.domain.model.AccountDomain;

import java.util.Optional;

public interface AccountLockPort {
    Optional<AccountDomain> findAccountByIdForUpdate(Long accountId);
}

