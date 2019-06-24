package com.myorg.balancecalculator.domain;

import java.math.BigDecimal;

public class RelativeBalance {
    long numberOfTransaction;
    BigDecimal relativeBalance;

    public RelativeBalance(long numberOfTransaction, BigDecimal relativeBalance) {
        this.numberOfTransaction = numberOfTransaction;
        this.relativeBalance = relativeBalance == null ? BigDecimal.ZERO : relativeBalance;
    }

    public long getNumberOfTransaction() {
        return numberOfTransaction;
    }

    public BigDecimal getRelativeBalance() {
        return relativeBalance;
    }

    public void incrementNumberOfTransaction() {
        this.numberOfTransaction = this.numberOfTransaction + 1;
    }

    public void decrementNumberOfTransaction() {
        this.numberOfTransaction = this.numberOfTransaction - 1;
    }


    public void addBalance(BigDecimal amount) {
        this.relativeBalance = this.relativeBalance.add(amount);
    }

    public void substractBalance(BigDecimal amount) {
        this.relativeBalance = this.relativeBalance.subtract(amount);
    }


}
