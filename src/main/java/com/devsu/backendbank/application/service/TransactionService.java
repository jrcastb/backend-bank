package com.devsu.backendbank.application.service;

import com.devsu.backendbank.application.input.port.TransactionApi;
import com.devsu.backendbank.application.output.port.AccountLockPort;
import com.devsu.backendbank.application.output.port.TransactionCommandPort;
import com.devsu.backendbank.application.output.port.TransactionQueryPort;
import com.devsu.backendbank.application.service.transaction.TransactionRuleContext;
import com.devsu.backendbank.application.service.transaction.TransactionRuleStrategyResolver;
import com.devsu.backendbank.domain.model.AccountDomain;
import com.devsu.backendbank.domain.model.TransactionDomain;
import com.devsu.backendbank.infrastructure.exception.BusinessException;
import com.devsu.backendbank.infrastructure.exception.message.BusinessErrorMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class TransactionService implements TransactionApi {

    private final AccountLockPort accountLockPort;
    private final TransactionQueryPort transactionQueryPort;
    private final TransactionCommandPort transactionCommandPort;
    private final TransactionRuleStrategyResolver transactionRuleStrategyResolver;

    @Override
    public Page<TransactionDomain> findTransactions(Pageable pageable) {
        return transactionQueryPort.findTransactions(pageable);
    }

    @Override
    public TransactionDomain findTransactionById(Long id) {
        return transactionQueryPort.findTransactionById(id)
                .orElseThrow(() -> new BusinessException(BusinessErrorMessage.NO_MOVEMENTS_FOUND));
    }

    @Override
    @Transactional
    public TransactionDomain createTransaction(TransactionDomain transaction) {
        AccountDomain account = accountLockPort.findAccountByIdForUpdate(transaction.accountId())
                .orElseThrow(() -> new BusinessException(BusinessErrorMessage.ACCOUNT_NOT_FOUND));

        if (Boolean.FALSE.equals(account.estado())) {
            throw new BusinessException(BusinessErrorMessage.ACCOUNT_INACTIVE);
        }

        if (transaction.valor() == null || transaction.valor().compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException(BusinessErrorMessage.INVALID_TRANSACTION_AMOUNT);
        }

        BigDecimal currentBalance = transactionQueryPort.findLatestTransactionByAccountId(account.id())
                .map(TransactionDomain::saldo)
                .orElse(account.saldoInicial());

        transactionRuleStrategyResolver.resolve(transaction.tipoMovimiento())
                .validate(new TransactionRuleContext(transaction, account, currentBalance));

        BigDecimal newBalance = currentBalance.add(transaction.valor());
        TransactionDomain toSave = new TransactionDomain(
                null,
                transaction.accountId(),
                LocalDateTime.now(ZoneOffset.UTC),
                transaction.tipoMovimiento(),
                transaction.valor(),
                newBalance,
                null,
                account.numeroCuenta()
        );

        return transactionCommandPort.saveTransaction(toSave);
    }

    @Override
    public void deleteTransaction(Long id) {
        findTransactionById(id);
        transactionCommandPort.deleteTransactionById(id);
    }
}
