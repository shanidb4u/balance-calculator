**Balance Calculator:** 
Application to print the relative account balance (positive or negative) in a given time frame from list of transaction records. 
The relative account balance is the sum of funds that were transferred to / from an account in a given time frame, 
it does not account for funds that were in that account prior to the time frame.


**Technologies Used:**
Java8, Gradle, Spock and Groovy

**How to run:**

Step 1: Generate jar     
   Option 1 : Use the jar balance-calculator-1.0-SNAPSHOT.jar provided under the project repo.        
   Option 2: Manually Run below command and this will generate jar under build/libs folder.        
             `./gradlew clean build`
                 
Step 2: Run the jar       

   Open terminal, go to the folder where balance-calculator-1.0-SNAPSHOT.jar is present and run the below command       
    `java -jar balance-calculator-1.0-SNAPSHOT.jar --transaction-record-file-path=csvfilepath` 
   or        
    `java -jar balance-calculator-1.0-SNAPSHOT.jar --transaction-record-file-path=csvfilepath --transaction-reversal-days=90`

Balance Calculator accepts following arguments while running        

--transaction-record-file-path - Path of the csv file where transactions details are present.   
--transaction-reversal-days   - This is optional argument,  and specify number of days if there is days limit for a transaction to be reversed.      

eg:- 
`java -jar balance-calculator-1.0-SNAPSHOT.jar --transaction-record-file-path=/Users/shanid/dev/mylab/Java/code-challenge/src/test/resources/transactions.csv`     

`java -jar balance-calculator-1.0-SNAPSHOT.jar --transaction-record-file-path=/Users/shanid/dev/mylab/Java/code-challenge/src/test/resources/transactions.csv --transaction-reversal-days=90`      



**Design:**

Transactions details are stored in Map , and map has key as account id and value as all the debit and credit transactions of the account.
Two separate maps are maintained for payment transaction and reversal transactions.

Main aim of the design was to avoid searching entire list for finding the relative balance.

keeping separate list for reversal transaction since reversal transaction can happen outside the given time frame and needs to scan  list for all transactions after from date, provide --transaction-reversal-days if there is days limit for a transaction to be reversed to optimize this search.

Application is using binary search in many places to find transaction from the list given a time range and is optimized to make sure that entire transactions list are not traversed unless necessary.
 
 
**Enhancements or Pending Items:**

1. Use third party tool like open csv to read csv files.
2. Add validation while reading transaction list like No two transaction with same id, no fields values are missing, etc. ( Currently it is assumed that the data is good and in valid format).
3. Use logger for logging.
4. More test cases to cover every classes and lines.



