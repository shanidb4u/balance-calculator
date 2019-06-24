package com.myorg.balancecalculator


import com.myorg.balancecalculator.domain.Transaction
import spock.lang.Specification

import java.time.LocalDateTime

import static com.myorg.balancecalculator.constant.BalanceCalculatorConstant.DDMMYYYY_FORMATTER

class RelativeAccountBalanceCalculatorSpec extends Specification {

    def "Basic test with Only payment Transactions"() {
        given:

        def relativeAccountBalanceCalculator = new RelativeAccountBalanceCalculator()
        relativeAccountBalanceCalculator.addTransaction(new Transaction("TX10001", "ACC334455", "ACC778899", "20/10/2018 12:47:55", "25.00", "PAYMENT", null))
        relativeAccountBalanceCalculator.addTransaction(new Transaction("TX10002", "ACC334455", "ACC998877", "20/10/2018 17:33:43", "10.50", "PAYMENT", null))
        relativeAccountBalanceCalculator.addTransaction(new Transaction("TX10003", "ACC998877", "ACC778899", "20/10/2018 18:00:00", "5.00", "PAYMENT", null))
        relativeAccountBalanceCalculator.addTransaction(new Transaction("TX10005", "ACC334455", "ACC778899", "21/10/2018 09:30:00", "7.25", "PAYMENT", null))

        when:

        def balance = relativeAccountBalanceCalculator.getRelativeBalance("ACC334455",
                LocalDateTime.parse("20/10/2018 12:00:00", DDMMYYYY_FORMATTER),
                LocalDateTime.parse("20/10/2018 19:00:00", DDMMYYYY_FORMATTER))
        then:
        balance.numberOfTransaction == 2
        balance.relativeBalance == -35.5

    }

    def "Basic test with Reversal Transactions"() {
        given:

        def relativeAccountBalanceCalculator = new RelativeAccountBalanceCalculator()
        relativeAccountBalanceCalculator.addTransaction(new Transaction("TX10001", "ACC334455", "ACC778899", "20/10/2018 12:47:55", "25.00", "PAYMENT", null))
        relativeAccountBalanceCalculator.addTransaction(new Transaction("TX10002", "ACC334455", "ACC998877", "20/10/2018 17:33:43", "10.50", "PAYMENT", null))
        relativeAccountBalanceCalculator.addTransaction(new Transaction("TX10003", "ACC998877", "ACC778899", "20/10/2018 18:00:00", "5.00", "PAYMENT", null))
        relativeAccountBalanceCalculator.addTransaction(new Transaction("TX10004", "ACC334455", "ACC998877", "20/10/2018 19:45:00", "10.50", "REVERSAL", "TX10002"))
        relativeAccountBalanceCalculator.addTransaction(new Transaction("TX10005", "ACC334455", "ACC778899", "21/10/2018 09:30:00", "7.25", "PAYMENT", null))

        when:

        def balance = relativeAccountBalanceCalculator.getRelativeBalance("ACC334455",
                LocalDateTime.parse("20/10/2018 12:00:00", DDMMYYYY_FORMATTER),
                LocalDateTime.parse("20/10/2018 19:00:00", DDMMYYYY_FORMATTER))
        then:
        balance.numberOfTransaction == 1
        balance.relativeBalance == -25.0

    }

    def "When there are transactions exist exactly at from time of given time frame"() {
        given:

        def relativeAccountBalanceCalculator = new RelativeAccountBalanceCalculator()
        relativeAccountBalanceCalculator.addTransaction(new Transaction("TX10001", "ACC778899", "ACC334455", "20/10/2018 12:00:00", "25.00", "PAYMENT", null))
        relativeAccountBalanceCalculator.addTransaction(new Transaction("TX100123", "ACC998877", "ACC334455", "20/10/2018 12:00:00", "100.00", "PAYMENT", null))
        relativeAccountBalanceCalculator.addTransaction(new Transaction("TX10002", "ACC334455", "ACC998877", "20/10/2018 12:00:00", "10.50", "PAYMENT", null))
        relativeAccountBalanceCalculator.addTransaction(new Transaction("TX10003", "ACC998877", "ACC778899", "20/10/2018 18:00:00", "5.00", "PAYMENT", null))
        relativeAccountBalanceCalculator.addTransaction(new Transaction("TX10004", "ACC334455", "ACC998877", "20/10/2018 19:45:00", "10.50", "REVERSAL", "TX10002"))
        relativeAccountBalanceCalculator.addTransaction(new Transaction("TX10005", "ACC334455", "ACC778899", "21/10/2018 09:30:00", "7.25", "PAYMENT", null))

        when:

        def balance = relativeAccountBalanceCalculator.getRelativeBalance("ACC334455", LocalDateTime.parse("20/10/2018 12:00:00", DDMMYYYY_FORMATTER),
                LocalDateTime.parse("20/10/2018 19:00:00", DDMMYYYY_FORMATTER))
        then:
        balance.numberOfTransaction == 2
        balance.relativeBalance == 125.0

    }

    def "Basic test with  Transactions when transactionReversalMaxDays is set"() {
        given:

        def relativeAccountBalanceCalculator = new RelativeAccountBalanceCalculator()
        relativeAccountBalanceCalculator.setTransactionReversalMaxDays(90)
        relativeAccountBalanceCalculator.addTransaction(new Transaction("TX10001", "ACC334455", "ACC778899", "20/10/2018 12:47:55", "25.00", "PAYMENT", null))
        relativeAccountBalanceCalculator.addTransaction(new Transaction("TX10002", "ACC334455", "ACC998877", "20/10/2018 17:33:43", "10.50", "PAYMENT", null))
        relativeAccountBalanceCalculator.addTransaction(new Transaction("TX10003", "ACC998877", "ACC778899", "20/10/2018 18:00:00", "5.00", "PAYMENT", null))
        //below reversal transaction will not be considered as this reversal happened after 90 days (TransactionReversalMaxDays)  of original transaction
        relativeAccountBalanceCalculator.addTransaction(new Transaction("TX10004", "ACC334455", "ACC998877", "01/07/2019 19:45:00", "10.50", "REVERSAL", "TX10002"))
        relativeAccountBalanceCalculator.addTransaction(new Transaction("TX10005", "ACC334455", "ACC778899", "21/10/2018 09:30:00", "7.25", "PAYMENT", null))

        when:

        def balance = relativeAccountBalanceCalculator.getRelativeBalance("ACC334455", LocalDateTime.parse("20/10/2018 12:00:00", DDMMYYYY_FORMATTER),
                LocalDateTime.parse("20/10/2018 19:00:00", DDMMYYYY_FORMATTER))
        then:
        balance.numberOfTransaction == 2
        balance.relativeBalance == -35.5

    }

}
