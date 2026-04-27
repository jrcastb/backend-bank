package com.devsu.backendbank.application.service;

import com.devsu.backendbank.application.input.port.AccountApi;
import com.devsu.backendbank.application.output.port.BankDb;
import com.devsu.backendbank.domain.model.AccountDomain;
import com.devsu.backendbank.infrastructure.exception.BusinessException;
import com.devsu.backendbank.infrastructure.exception.message.BusinessErrorMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService implements AccountApi {

    private final BankDb bankDb;

    @Override
    public Page<AccountDomain> findAccounts(Pageable pageable) {
        return bankDb.findAccounts(pageable);
    }

    @Override
    public AccountDomain findAccountById(Long id) {
        return bankDb.findAccountById(id)
                .orElseThrow(() -> new BusinessException(BusinessErrorMessage.ACCOUNT_NOT_FOUND));
    }

    @Override
    public AccountDomain createAccount(AccountDomain account) {
        if (bankDb.findClientById(account.clientId()).isEmpty()) {
            throw new BusinessException(BusinessErrorMessage.CLIENT_NOT_FOUND);
        }
        if (bankDb.accountExistsByNumeroCuenta(account.numeroCuenta())) {
            throw new BusinessException(BusinessErrorMessage.ACCOUNT_ALREADY_EXISTS);
        }
        return bankDb.saveOrUpdateAccount(account);
    }

    @Override
    public AccountDomain updateAccount(Long id, AccountDomain account) {
        AccountDomain current = findAccountById(id);
        if (bankDb.findClientById(account.clientId()).isEmpty()) {
            throw new BusinessException(BusinessErrorMessage.CLIENT_NOT_FOUND);
        }
        if (bankDb.accountExistsByNumeroCuentaExcludingId(account.numeroCuenta(), id)) {
            throw new BusinessException(BusinessErrorMessage.ACCOUNT_ALREADY_EXISTS);
        }
        return bankDb.saveOrUpdateAccount(preserveAudit(current, account));
    }

    @Override
    public AccountDomain patchAccount(Long id, AccountDomain account) {
        AccountDomain current = findAccountById(id);
        if (!current.numeroCuenta().equals(account.numeroCuenta())
                && bankDb.accountExistsByNumeroCuentaExcludingId(account.numeroCuenta(), id)) {
            throw new BusinessException(BusinessErrorMessage.ACCOUNT_ALREADY_EXISTS);
        }
        return bankDb.saveOrUpdateAccount(preserveAudit(current, account));
    }

    @Override
    public void deleteAccount(Long id) {
        findAccountById(id);
        bankDb.deleteAccountById(id);
    }

    private AccountDomain preserveAudit(AccountDomain current, AccountDomain replacement) {
        return new AccountDomain(
                current.id(),
                replacement.clientId(),
                replacement.numeroCuenta(),
                replacement.tipoCuenta(),
                replacement.saldoInicial(),
                replacement.estado(),
                current.createdAt(),
                current.updatedAt(),
                current.nombreCliente()
        );
    }
}
