package com.devsu.backendbank.application.service;

import com.devsu.backendbank.application.input.port.TransactionApi;
import com.devsu.backendbank.application.output.port.BankDb;
import com.devsu.backendbank.domain.model.AccountDomain;
import com.devsu.backendbank.domain.model.TransactionDomain;
import com.devsu.backendbank.domain.model.TransactionTypeDomain;
import com.devsu.backendbank.infrastructure.exception.BusinessException;
import com.devsu.backendbank.infrastructure.exception.message.BusinessErrorMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class TransactionService implements TransactionApi {

    private static final BigDecimal DAILY_DEBIT_LIMIT = BigDecimal.valueOf(1000);

    private final BankDb bankDb;

    @Override
    public Page<TransactionDomain> findTransactions(Pageable pageable) {
        return bankDb.findTransactions(pageable);
    }

    @Override
    public TransactionDomain findTransactionById(Long id) {
        return bankDb.findTransactionById(id)
                .orElseThrow(() -> new BusinessException(BusinessErrorMessage.NO_MOVEMENTS_FOUND));
    }

    @Override
    @Transactional
    public TransactionDomain createTransaction(TransactionDomain transaction) {
        AccountDomain account = bankDb.findAccountByIdForUpdate(transaction.accountId())
                .orElseThrow(() -> new BusinessException(BusinessErrorMessage.ACCOUNT_NOT_FOUND));

        if (Boolean.FALSE.equals(account.estado())) {
            throw new BusinessException(BusinessErrorMessage.ACCOUNT_INACTIVE);
        }

        if (transaction.valor() == null || transaction.valor().compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException(BusinessErrorMessage.INVALID_TRANSACTION_AMOUNT);
        }

        BigDecimal currentBalance = bankDb.findLatestTransactionByAccountId(account.id())
                .map(TransactionDomain::saldo)
                .orElse(account.saldoInicial());

        validateBusinessRules(transaction, currentBalance, account.id());

        BigDecimal newBalance = currentBalance.add(transaction.valor());
        TransactionDomain toSave = new TransactionDomain(
                null,
                transaction.accountId(),
                LocalDateTime.now(ZoneOffset.UTC),
                transaction.tipoMovimiento(),
                transaction.valor(),
                newBalance,
                null
        );

        return bankDb.saveTransaction(toSave);
    }

    @Override
    public void deleteTransaction(Long id) {
        findTransactionById(id);
        bankDb.deleteTransactionById(id);
    }

    private void validateBusinessRules(TransactionDomain input, BigDecimal currentBalance, Long accountId) {
        if (input.tipoMovimiento() == TransactionTypeDomain.DEBITO) {
            BigDecimal debitAmount = input.valor().abs();
            if (currentBalance.compareTo(debitAmount) < 0) {
                throw new BusinessException(BusinessErrorMessage.INSUFFICIENT_FUNDS);
            }

            LocalDate today = LocalDate.now(ZoneOffset.UTC);
            LocalDateTime from = today.atStartOfDay();
            LocalDateTime to = today.plusDays(1).atStartOfDay().minusNanos(1);
            BigDecimal currentDailyDebits = bankDb.sumDailyDebitsByAccount(accountId, from, to);
            BigDecimal projected = currentDailyDebits.add(debitAmount);
            if (projected.compareTo(DAILY_DEBIT_LIMIT) > 0) {
                throw new BusinessException(BusinessErrorMessage.DAILY_LIMIT_EXCEEDED);
            }
        }
    }
}
