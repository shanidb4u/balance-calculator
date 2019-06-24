package com.myorg.balancecalculator.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.myorg.balancecalculator.constant.BalanceCalculatorConstant.DDMMYYYY_FORMATTER;

public class Transaction {
    String transactionId;
    String fromAccountId;
    String toAccountId;
    LocalDateTime createdAt;
    BigDecimal amount;
    TransactionType transactionType;
    String relatedTransaction;

    public Transaction(String transactionId, String fromAccountId, String toAccountId, LocalDateTime createdAt,
                       BigDecimal amount, TransactionType transactionType, String relatedTransaction) {
        this.transactionId = transactionId;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.createdAt = createdAt;
        this.amount = amount;
        this.transactionType = transactionType;
        this.relatedTransaction = relatedTransaction;
    }

    public Transaction(String transactionId, String fromAccountId, String toAccountId, String createdAt,
                       String amount, String transactionType, String relatedTransaction) {
        this.transactionId = transactionId;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.createdAt = LocalDateTime.parse(createdAt, DDMMYYYY_FORMATTER);
        this.amount = new BigDecimal(amount);
        this.transactionType = TransactionType.valueOf(transactionType);
        this.relatedTransaction = relatedTransaction;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getFromAccountId() {
        return fromAccountId;
    }

    public String getToAccountId() {
        return toAccountId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public String getRelatedTransaction() {
        return relatedTransaction;
    }
}
