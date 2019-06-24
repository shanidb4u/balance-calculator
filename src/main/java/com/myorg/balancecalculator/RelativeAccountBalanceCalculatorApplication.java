package com.myorg.balancecalculator;

import com.myorg.balancecalculator.domain.RelativeBalance;
import com.myorg.balancecalculator.exception.FilePathMissingException;
import org.apache.commons.lang3.StringUtils;

import java.io.Console;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static com.myorg.balancecalculator.constant.BalanceCalculatorConstant.CSV_FILE_PATH_OPTION;
import static com.myorg.balancecalculator.constant.BalanceCalculatorConstant.DDMMYYYY_FORMATTER;
import static com.myorg.balancecalculator.constant.BalanceCalculatorConstant.EQUAL_SYMBOL;
import static com.myorg.balancecalculator.constant.BalanceCalculatorConstant.EXIT;
import static com.myorg.balancecalculator.constant.BalanceCalculatorConstant.TRANSACTION_REVERSAL_MAX_DAYS;

public class RelativeAccountBalanceCalculatorApplication {

    public static void main(String[] args) {

        RelativeAccountBalanceCalculator relativeAccountBalanceCalculator = initializeTransactionsData(args);

        getUserInputsAndPrintBalance(relativeAccountBalanceCalculator);
    }

    private static RelativeAccountBalanceCalculator initializeTransactionsData(String[] args) {
        RelativeAccountBalanceCalculator relativeAccountBalanceCalculator;
        String transactionsFilePath = Arrays.stream(args)
                .filter(arg -> arg.contains(CSV_FILE_PATH_OPTION + EQUAL_SYMBOL))
                .findFirst()
                .orElseThrow(() -> new FilePathMissingException("Missing Transaction file path option " + CSV_FILE_PATH_OPTION))
                .split(EQUAL_SYMBOL)[1].trim();

        Optional<String> revMaxDaysOptional = Arrays.stream(args)
                .filter(arg -> arg.contains(TRANSACTION_REVERSAL_MAX_DAYS + EQUAL_SYMBOL))
                .findFirst();

        if (revMaxDaysOptional.isPresent()) {
            relativeAccountBalanceCalculator = new RelativeAccountBalanceCalculator(transactionsFilePath,
                    revMaxDaysOptional.get().split(EQUAL_SYMBOL)[1].trim());
        } else {
            relativeAccountBalanceCalculator = new RelativeAccountBalanceCalculator(transactionsFilePath);
        }
        return relativeAccountBalanceCalculator;
    }

    private static void getUserInputsAndPrintBalance(RelativeAccountBalanceCalculator relativeAccountBalanceCalculator) {
        Console console = System.console();
        if (console == null) {
            System.err.println("Console not present, exiting...");
            System.exit(1);
        }

        while (true) {
            System.out.println("Enter Account Id to calculate relative balance or enter Exit to exit the system");
            String accountId = console.readLine();
            if (StringUtils.isBlank(accountId)) {
                System.err.println("Account Id cannot be empty or null");
                continue;
            }
            if (EXIT.equalsIgnoreCase(accountId)) {
                break;
            } else {
                System.out.println("Enter the From Date in dd/MM/yyyy HH:mm:ss format");
                String fromDate = console.readLine();
                if (StringUtils.isBlank(fromDate)) {
                    System.err.println("From Date cannot be empty or null");
                    continue;
                }
                System.out.println("Enter To Date in dd/MM/yyyy HH:mm:ss format");
                String toDate = console.readLine();
                if (StringUtils.isBlank(toDate)) {
                    System.err.println("To Date cannot be empty or null");
                    continue;
                }
                RelativeBalance balance = relativeAccountBalanceCalculator.getRelativeBalance(accountId, LocalDateTime.parse(fromDate, DDMMYYYY_FORMATTER),
                        LocalDateTime.parse(toDate, DDMMYYYY_FORMATTER));

                //Default formatter print negative amount as ($amount) so tweaking to print in -$amount format
                DecimalFormat formatter = (DecimalFormat)NumberFormat.getCurrencyInstance();
                String symbol = formatter.getCurrency().getSymbol();
                formatter.setNegativePrefix("-"+symbol);
                formatter.setNegativeSuffix("");

                System.out.println("Relative balance for the period is: " + formatter.format(balance.getRelativeBalance()));
                System.out.println("Number of transactions included is: " + balance.getNumberOfTransaction());
            }
        }
    }

}
