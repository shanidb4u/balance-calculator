package com.myorg.balancecalculator;

import com.myorg.balancecalculator.domain.Transaction;
import com.myorg.balancecalculator.domain.RelativeBalance;
import org.apache.commons.collections4.CollectionUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.myorg.balancecalculator.constant.BalanceCalculatorConstant.COMMA_SYMBOL;
import static com.myorg.balancecalculator.constant.BalanceCalculatorConstant.TRANSACTION_REVERSAL_MAX_DAYS;

public class RelativeAccountBalanceCalculator {

    Map<String, List<Transaction>> accountIdTransactionMap;

    Map<String, List<Transaction>> accountIdReversedTransactionMap;

    /**
     * This is optional field , specify n if transaction cannot be reversed after nth day
     * This will optimize the search so that entire list need not be traversed to check for reversed transaction.
     */
    int transactionReversalMaxDays = 0;

    public RelativeAccountBalanceCalculator() {
    }

    public RelativeAccountBalanceCalculator(String csvFilePath) {
        initializeTransactions(csvFilePath);
    }

    public RelativeAccountBalanceCalculator(String csvFilePath, String transactionCancellationDays) {
        try {
            this.setTransactionReversalMaxDays(Integer.valueOf(transactionCancellationDays));
        } catch (NumberFormatException exception) {
            System.err.println(TRANSACTION_REVERSAL_MAX_DAYS + " argument value should be a number");
        }
        initializeTransactions(csvFilePath);
    }


    public RelativeAccountBalanceCalculator(Map<String, List<Transaction>> accountIdTransactionMap,
                                            Map<String, List<Transaction>> accountIdReversedTransactionMap) {
        this.accountIdTransactionMap = accountIdTransactionMap;
        this.accountIdReversedTransactionMap = accountIdReversedTransactionMap;
    }


    public RelativeAccountBalanceCalculator(Map<String, List<Transaction>> accountIdTransactionMap,
                                            Map<String, List<Transaction>> accountIdReversedTransactionMap, int transactionCancellationDays) {
        this.accountIdTransactionMap = accountIdTransactionMap;
        this.accountIdReversedTransactionMap = accountIdReversedTransactionMap;
        this.transactionReversalMaxDays = transactionCancellationDays;
    }

    public void setAccountIdTransactionMap(Map<String, List<Transaction>> accountIdTransactionMap) {
        this.accountIdTransactionMap = accountIdTransactionMap;
    }

    public void setAccountIdReversedTransactionMap(Map<String, List<Transaction>> accountIdReversedTransactionMap) {
        this.accountIdReversedTransactionMap = accountIdReversedTransactionMap;
    }

    public void setTransactionReversalMaxDays(int transactionReversalMaxDays) {
        this.transactionReversalMaxDays = transactionReversalMaxDays;
    }


    public void addTransaction(Transaction transaction) {
        switch (transaction.getTransactionType()) {
            case PAYMENT:
                if (accountIdTransactionMap == null) {
                    accountIdTransactionMap = new HashMap<>();
                }
                accountIdTransactionMap.computeIfAbsent(transaction.getFromAccountId(), s -> new ArrayList<>()).add(transaction);
                accountIdTransactionMap.computeIfAbsent(transaction.getToAccountId(), s -> new ArrayList<>()).add(transaction);
                break;
            case REVERSAL:
                if (accountIdReversedTransactionMap == null) {
                    accountIdReversedTransactionMap = new HashMap<>();
                }
                accountIdReversedTransactionMap.computeIfAbsent(transaction.getFromAccountId(), s -> new ArrayList<>()).add(transaction);
                accountIdReversedTransactionMap.computeIfAbsent(transaction.getToAccountId(), s -> new ArrayList<>()).add(transaction);
                break;
            default:
                throw new IllegalArgumentException(String.format("Transaction Type %s is not Supported", transaction.getTransactionType()));
        }
    }


    public RelativeBalance getRelativeBalance(String accountId, LocalDateTime fromTime, LocalDateTime toTime) {

        RelativeBalance relativeBalance = new RelativeBalance(0, BigDecimal.ZERO);

        if (accountIdTransactionMap != null) {
            List<Transaction> transactions = accountIdTransactionMap.get(accountId);
            if (CollectionUtils.isNotEmpty(transactions)) {
                Set<String> transactionIds = new HashSet<>();
                int index = Collections.binarySearch(transactions, new Transaction(null, null, null, fromTime, null, null, null)
                        , Comparator.comparing(Transaction::getCreatedAt));
                //if there is transactions exactly at fromTime
                if (index >= 0) {
                    int backwardIndex = index;
                    int forwardIndex = index + 1;
                    //traverse back and calculate balance since there can be more than one transactions exactly at fromTime
                    calculateBalanceByMovingBackwardFromIndex(accountId, fromTime, relativeBalance, transactions, transactionIds, backwardIndex);
                    //traverse forward and calculate balance for transactions before toTime.
                    calculateBalanceByMovingForwardFromIndex(accountId, toTime, relativeBalance, transactions, transactionIds, forwardIndex);
                }
                /**
                 * if no transaction at fromTime then index returned is  (-(insertion point) - 1). The insertion point is defined
                 * as the point at which the key would be inserted into the list.
                 */
                else {
                    int forwardIndex = -index - 1;
                    calculateBalanceByMovingForwardFromIndex(accountId, toTime, relativeBalance, transactions, transactionIds, forwardIndex);
                }

                /**
                 * if  {@transactionReversalMaxDays} is specified then iterates transaction created from startDate until EndDate + {@transactionReversalMaxDays}
                 * if {@transactionReversalMaxDays} not specified then iterates list starting from the fromTime till the end of list.
                 */

                if (accountIdReversedTransactionMap != null) {
                    calculateBalanceForReversalTransactions(accountId, fromTime, toTime, relativeBalance, transactionIds);
                }
            }
        }
        return relativeBalance;
    }

    private void calculateBalanceByMovingBackwardFromIndex(String accountId, LocalDateTime fromTime, RelativeBalance relativeBalance, List<Transaction> transactions, Set<String> transactionIds, int backwardIndex) {
        while (backwardIndex >= 0) {
            Transaction transaction = transactions.get(backwardIndex);
            if (transaction.getCreatedAt().isBefore(fromTime)) {
                break;
            }
            if (accountId.equals(transaction.getFromAccountId())) {
                relativeBalance.substractBalance(transaction.getAmount());
            } else if (accountId.equals(transaction.getToAccountId())) {
                relativeBalance.addBalance(transaction.getAmount());
            }
            transactionIds.add(transaction.getTransactionId());
            relativeBalance.incrementNumberOfTransaction();
            backwardIndex--;
        }
    }

    private void calculateBalanceByMovingForwardFromIndex(String accountId, LocalDateTime toTime, RelativeBalance relativeBalance, List<Transaction> transactions, Set<String> transactionIds, int forwardIndex) {
        while (forwardIndex < transactions.size()) {
            Transaction transaction = transactions.get(forwardIndex);
            if (transaction.getCreatedAt().isAfter(toTime)) {
                break;
            }
            if (accountId.equals(transaction.getFromAccountId())) {
                relativeBalance.substractBalance(transaction.getAmount());
            } else if (accountId.equals(transaction.getToAccountId())) {
                relativeBalance.addBalance(transaction.getAmount());
            }
            transactionIds.add(transaction.getTransactionId());
            relativeBalance.incrementNumberOfTransaction();
            forwardIndex++;
        }
    }

    private void calculateBalanceForReversalTransactions(String accountId, LocalDateTime fromTime, LocalDateTime toTime, RelativeBalance relativeBalance, Set<String> transactionIds) {
        List<Transaction> reversedTransactions = accountIdReversedTransactionMap.get(accountId);
        if (CollectionUtils.isNotEmpty(reversedTransactions)) {
            int revTxnIndex = Collections.binarySearch(reversedTransactions, new Transaction(null, null, null,
                    fromTime, null, null, null), Comparator.comparing(Transaction::getCreatedAt));

            if (revTxnIndex < 0) {
                revTxnIndex = -revTxnIndex - 1;
            }

            while (revTxnIndex < reversedTransactions.size() && !transactionIds.isEmpty()) {
                Transaction reversedTransaction = reversedTransactions.get(revTxnIndex);
                if (transactionReversalMaxDays > 0 && reversedTransaction.getCreatedAt().isAfter(toTime.plusDays(transactionReversalMaxDays + 1L))) {
                    break;
                }
                if (transactionIds.contains(reversedTransaction.getRelatedTransaction())) {
                    if (accountId.equals(reversedTransaction.getFromAccountId())) {
                        relativeBalance.addBalance(reversedTransaction.getAmount());
                    } else if (accountId.equals(reversedTransaction.getToAccountId())) {
                        relativeBalance.substractBalance(reversedTransaction.getAmount());
                    }
                    transactionIds.remove(reversedTransaction.getTransactionId());
                    relativeBalance.decrementNumberOfTransaction();
                }
                revTxnIndex++;
            }
        }
    }

    private void initializeTransactions(String transactionsFilePath) {

        try (BufferedReader br = new BufferedReader(new FileReader(transactionsFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(COMMA_SYMBOL);
                Transaction transaction;
                if (values.length >= 7) {
                    transaction = new Transaction(values[0].trim(), values[1].trim(), values[2].trim(), values[3].trim(),
                            values[4].trim(), values[5].trim(), values[6].trim());
                } else if (values.length == 6) {
                    transaction = new Transaction(values[0].trim(), values[1].trim(), values[2].trim(), values[3].trim(),
                            values[4].trim(), values[5].trim(), null);
                } else {
                    throw new IllegalStateException("Input Error: Transaction data record is not in the correct format");
                }
                this.addTransaction(transaction);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
