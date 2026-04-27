package com.devsu.backendbank.application.output.port;

import com.devsu.backendbank.domain.model.AccountDomain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface AccountQueryPort {
    Page<AccountDomain> findAccounts(Pageable pageable);
    Optional<AccountDomain> findAccountById(Long id);
}

