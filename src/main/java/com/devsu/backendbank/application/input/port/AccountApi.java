package com.devsu.backendbank.application.input.port;

import com.devsu.backendbank.domain.model.AccountDomain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountApi {

    Page<AccountDomain> findAccounts(Pageable pageable);

    AccountDomain findAccountById(Long id);

    AccountDomain createAccount(AccountDomain account);

    AccountDomain updateAccount(Long id, AccountDomain account);

    AccountDomain patchAccount(Long id, AccountDomain account);

    void deleteAccount(Long id);
}
