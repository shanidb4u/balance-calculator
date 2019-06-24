package com.myorg.balancecalculator.constant;

import java.time.format.DateTimeFormatter;

public class BalanceCalculatorConstant {

    private BalanceCalculatorConstant() {
    }

    public static final String CSV_FILE_PATH_OPTION = "--transaction-record-file-path";
    public static final String TRANSACTION_REVERSAL_MAX_DAYS = "--transaction-reversal-days";
    public static final String COMMA_SYMBOL = ",";
    public static final String EQUAL_SYMBOL = "=";
    public static final String EXIT = "exit";
    public static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static final DateTimeFormatter DDMMYYYY_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);


}
