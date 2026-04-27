package com.devsu.backendbank.application.service.transaction;

import com.devsu.backendbank.application.output.port.TransactionQueryPort;
import com.devsu.backendbank.domain.enums.TransactionTypeDomain;
import com.devsu.backendbank.infrastructure.exception.BusinessException;
import com.devsu.backendbank.infrastructure.exception.message.BusinessErrorMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
@RequiredArgsConstructor
public class DebitTransactionRuleStrategy implements TransactionRuleStrategy {

    private static final BigDecimal DAILY_DEBIT_LIMIT = BigDecimal.valueOf(1000);

    private final TransactionQueryPort transactionQueryPort;

    @Override
    public TransactionTypeDomain supports() {
        return TransactionTypeDomain.DEBITO;
    }

    @Override
    public void validate(TransactionRuleContext context) {
        BigDecimal debitAmount = context.transaction().valor().abs();
        if (context.currentBalance().compareTo(debitAmount) < 0) {
            throw new BusinessException(BusinessErrorMessage.INSUFFICIENT_FUNDS);
        }

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDateTime from = today.atStartOfDay();
        LocalDateTime to = today.plusDays(1).atStartOfDay().minusNanos(1);
        BigDecimal currentDailyDebits = transactionQueryPort.sumDailyDebitsByAccount(context.account().id(), from, to);
        BigDecimal projected = currentDailyDebits.add(debitAmount);
        if (projected.compareTo(DAILY_DEBIT_LIMIT) > 0) {
            throw new BusinessException(BusinessErrorMessage.DAILY_LIMIT_EXCEEDED);
        }
    }
}

