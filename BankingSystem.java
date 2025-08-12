import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class Bank {
    private Map<String, Account> accounts;
    private int accountCounter = 103; // Starting after predefined accounts

    public Bank() {
        this.accounts = new HashMap<>();
    }

    public void addAccount(String accountNumber, String customerName, double balance, String pin) {
        Account account = new Account(accountNumber, customerName, balance, pin);
        accounts.put(accountNumber, account);
    }

    public Account getAccount(String accountNumber, String pin) {
        Account account = accounts.get(accountNumber);
        if (account != null && account.authenticate(pin)) {
            return account;
        }
        return null;
    }

    public boolean transfer(String fromAccNum, String pin, String toAccNum, double amount) {
        Account fromAccount = getAccount(fromAccNum, pin);
        Account toAccount = accounts.get(toAccNum);

        if (fromAccount == null) {
            System.out.println("Authentication failed or account not found!");
            return false;
        }
        if (toAccount == null) {
            System.out.println("Target account not found!");
            return false;
        }
        if (fromAccount.withdraw(amount)) {
            toAccount.deposit(amount);
            return true;
        }
        return false;
    }

    public String createNewAccount(String name, String pin, double initialDeposit) {
        String accountNumber = String.valueOf(accountCounter++);
        addAccount(accountNumber, name, initialDeposit, pin);
        return accountNumber;
    }
}

class Account {
    private String accountNumber;
    private String customerName;
    private double balance;
    private String pin;

    public Account(String accountNumber, String customerName, double balance, String pin) {
        this.accountNumber = accountNumber;
        this.customerName = customerName;
        this.balance = balance;
        this.pin = pin;
    }

    public boolean authenticate(String enteredPin) {
        return this.pin.equals(enteredPin);
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            System.out.println("Deposited $" + amount + " successfully.");
        } else {
            System.out.println("Invalid deposit amount!");
        }
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            System.out.println("Withdrew $" + amount + " successfully.");
            return true;
        } else {
            System.out.println("Insufficient balance or invalid amount!");
            return false;
        }
    }

    public void displayInfo() {
        System.out.println("Account Number: " + accountNumber);
        System.out.println("Customer Name: " + customerName);
        System.out.println("Balance: $" + balance);
    }
}

public class BankingSystem {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Bank bank = new Bank();

        // Predefined accounts
        bank.addAccount("101", "John Doe", 1000.0, "1234");
        bank.addAccount("102", "Jane Smith", 1500.0, "5678");

        while (true) {
            System.out.println("\n===== Banking System Menu =====");
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. Transfer");
            System.out.println("4. Balance Inquiry");
            System.out.println("5. Exit");
            System.out.println("6. Create New Account");
            System.out.print("Enter choice: ");
            int choice = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (choice) {
                case 1: // Deposit
                    System.out.print("Enter account number: ");
                    String accNumD = sc.nextLine();
                    System.out.print("Enter PIN: ");
                    String pinD = sc.nextLine();
                    Account accD = bank.getAccount(accNumD, pinD);
                    if (accD != null) {
                        System.out.print("Enter deposit amount: ");
                        double depositAmount = sc.nextDouble();
                        accD.deposit(depositAmount);
                    } else {
                        System.out.println("Invalid account or PIN!");
                    }
                    break;

                case 2: // Withdraw
                    System.out.print("Enter account number: ");
                    String accNumW = sc.nextLine();
                    System.out.print("Enter PIN: ");
                    String pinW = sc.nextLine();
                    Account accW = bank.getAccount(accNumW, pinW);
                    if (accW != null) {
                        System.out.print("Enter withdraw amount: ");
                        double withdrawAmount = sc.nextDouble();
                        accW.withdraw(withdrawAmount);
                    } else {
                        System.out.println("Invalid account or PIN!");
                    }
                    break;

                case 3: // Transfer
                    System.out.print("Enter your account number: ");
                    String fromAcc = sc.nextLine();
                    System.out.print("Enter your PIN: ");
                    String fromPin = sc.nextLine();
                    System.out.print("Enter target account number: ");
                    String toAcc = sc.nextLine();
                    System.out.print("Enter transfer amount: ");
                    double transferAmount = sc.nextDouble();
                    if (bank.transfer(fromAcc, fromPin, toAcc, transferAmount)) {
                        System.out.println("Transfer successful!");
                    }
                    break;

                case 4: // Balance Inquiry
                    System.out.print("Enter account number: ");
                    String accNumB = sc.nextLine();
                    System.out.print("Enter PIN: ");
                    String pinB = sc.nextLine();
                    Account accB = bank.getAccount(accNumB, pinB);
                    if (accB != null) {
                        accB.displayInfo();
                    } else {
                        System.out.println("Invalid account or PIN!");
                    }
                    break;

                case 5:
                    System.out.println("Thank you for using our banking system!");
                    sc.close();
                    return;

                case 6: // Create New Account
                    System.out.print("Enter your name: ");
                    String name = sc.nextLine();
                    System.out.print("Set a 4-digit PIN: ");
                    String newPin = sc.nextLine();
                    System.out.print("Enter initial deposit amount: ");
                    double initialDeposit = sc.nextDouble();
                    sc.nextLine(); // consume newline
                    String newAccountNumber = bank.createNewAccount(name, newPin, initialDeposit);
                    System.out.println("Account created successfully! Your account number is: " + newAccountNumber);
                    break;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
}
