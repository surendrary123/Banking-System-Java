import java.io.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Advanced Banking System
 * - Persistence via serialization (bank_data.ser)
 * - INR formatting
 * - PIN masking (Console) with fallback
 * - Minimum balance and daily withdrawal limit
 * - Transaction history per account
 *
 * Single-file implementation for ease of testing.
 */

class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    enum Type { DEPOSIT, WITHDRAW, TRANSFER_IN, TRANSFER_OUT }
    private Type type;
    private double amount;
    private LocalDateTime timestamp;
    private double balanceAfter;
    private String note;

    public Transaction(Type type, double amount, double balanceAfter, String note) {
        this.type = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.balanceAfter = balanceAfter;
        this.note = note;
    }

    @Override
    public String toString() {
        NumberFormat nf = BankingUtils.getINRFormatter();
        return String.format("%s | %s | %s | Balance: %s | %s",
                timestamp.toString(),
                type,
                nf.format(amount),
                nf.format(balanceAfter),
                note == null ? "" : note);
    }
}

class Account implements Serializable {
    private static final long serialVersionUID = 1L;

    private String accountNumber;
    private String customerName;
    private double balance;
    private String pin;

    // Transaction history
    private List<Transaction> transactions = new ArrayList<>();

    // For daily withdrawal limit tracking
    private double dailyWithdrawn = 0.0;
    private LocalDate lastWithdrawDate = LocalDate.MIN;

    // Configurable per-account limits (could be made dynamic)
    private static final double MINIMUM_BALANCE = 500.0;   // ₹500 minimum balance
    private static final double DAILY_WITHDRAW_LIMIT = 10000.0; // ₹10,000 per day

    public Account(String accountNumber, String customerName, double balance, String pin) {
        this.accountNumber = accountNumber;
        this.customerName = customerName;
        this.balance = balance;
        this.pin = pin;
        addTransaction(new Transaction(Transaction.Type.DEPOSIT, balance, this.balance, "Initial deposit"));
    }

    public boolean authenticate(String enteredPin) {
        return this.pin.equals(enteredPin);
    }

    public String getAccountNumber() { return accountNumber; }
    public String getCustomerName() { return customerName; }

    public synchronized double getBalance() { return balance; }

    public synchronized boolean deposit(double amount) {
        if (amount <= 0) return false;
        balance += amount;
        addTransaction(new Transaction(Transaction.Type.DEPOSIT, amount, balance, null));
        return true;
    }

    public synchronized boolean withdraw(double amount) {
        if (amount <= 0) return false;

        LocalDate today = LocalDate.now();
        if (!today.equals(lastWithdrawDate)) {
            // reset daily counter
            dailyWithdrawn = 0.0;
            lastWithdrawDate = today;
        }

        if (dailyWithdrawn + amount > DAILY_WITHDRAW_LIMIT) {
            System.out.println("Daily withdrawal limit exceeded! Remaining limit: " +
                    BankingUtils.getINRFormatter().format(DAILY_WITHDRAW_LIMIT - dailyWithdrawn));
            return false;
        }

        if (amount > balance - MINIMUM_BALANCE) {
            System.out.println("Cannot withdraw. Minimum balance requirement of " +
                    BankingUtils.getINRFormatter().format(MINIMUM_BALANCE) + " must be maintained.");
            return false;
        }

        balance -= amount;
        dailyWithdrawn += amount;
        addTransaction(new Transaction(Transaction.Type.WITHDRAW, amount, balance, null));
        return true;
    }

    public synchronized void addTransaction(Transaction t) {
        transactions.add(0, t); // add at front so recent appear first
        // Keep history trimmed if desired (e.g., keep only last 100). For now, keep all.
    }

    public synchronized List<Transaction> getRecentTransactions(int n) {
        return transactions.subList(0, Math.min(n, transactions.size()));
    }

    public void displayInfo() {
        NumberFormat nf = BankingUtils.getINRFormatter();
        System.out.println("Account Number: " + accountNumber);
        System.out.println("Customer Name:  " + customerName);
        System.out.println("Balance:        " + nf.format(balance));
        System.out.println("Recent Transactions:");
        List<Transaction> recent = getRecentTransactions(5);
        if (recent.isEmpty()) {
            System.out.println("  No transactions yet.");
        } else {
            for (Transaction t : recent) {
                System.out.println("  " + t.toString());
            }
        }
    }
}

class Bank implements Serializable {
    private static final long serialVersionUID = 1L;
    private Map<String, Account> accounts = new HashMap<>();
    private int accountCounter = 103; // start after sample accounts

    // persistence file
    private static final String DATA_FILE = "bank_data.ser";

    public Bank() {}

    public synchronized void addAccount(String accountNumber, String customerName, double balance, String pin) {
        accounts.put(accountNumber, new Account(accountNumber, customerName, balance, pin));
        // keep counter consistent if accountNumber numeric
        try {
            int num = Integer.parseInt(accountNumber);
            if (num >= accountCounter) accountCounter = num + 1;
        } catch (NumberFormatException ignored) {}
    }

    public synchronized String createNewAccount(String name, String pin, double initialDeposit) {
        String accountNumber = String.valueOf(accountCounter++);
        addAccount(accountNumber, name, initialDeposit, pin);
        return accountNumber;
    }

    public synchronized Account getAccountIfAuthenticated(String accNum, String pin) {
        Account a = accounts.get(accNum);
        if (a != null && a.authenticate(pin)) return a;
        return null;
    }

    public synchronized boolean transfer(String fromAccNum, String pin, String toAccNum, double amount) {
        Account from = getAccountIfAuthenticated(fromAccNum, pin);
        Account to = accounts.get(toAccNum);

        if (from == null) {
            System.out.println("Authentication failed or 'from' account not found.");
            return false;
        }
        if (to == null) {
            System.out.println("Target account not found.");
            return false;
        }
        // withdraw from source (this will check min balance and daily limit)
        if (from.withdraw(amount)) {
            to.deposit(amount);
            from.addTransaction(new Transaction(Transaction.Type.TRANSFER_OUT, amount, from.getBalance(), "To: " + toAccNum));
            to.addTransaction(new Transaction(Transaction.Type.TRANSFER_IN, amount, to.getBalance(), "From: " + fromAccNum));
            return true;
        }
        return false;
    }

    public synchronized void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(this);
            System.out.println("Bank data saved to " + DATA_FILE);
        } catch (IOException e) {
            System.out.println("Failed to save bank data: " + e.getMessage());
        }
    }

    public static Bank loadDataOrCreate() {
        File f = new File(DATA_FILE);
        if (f.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                Object obj = ois.readObject();
                if (obj instanceof Bank) {
                    System.out.println("Loaded bank data from " + DATA_FILE);
                    return (Bank) obj;
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Failed to load saved data, starting fresh. (" + e.getMessage() + ")");
            }
        }
        // create sample bank with two accounts
        Bank bank = new Bank();
        bank.addAccount("101", "John Doe", 1000.0, "1234");
        bank.addAccount("102", "Jane Smith", 1500.0, "5678");
        System.out.println("Started with sample accounts (101, 102).");
        return bank;
    }

    public synchronized Set<String> listAllAccountNumbers() {
        return new HashSet<>(accounts.keySet());
    }
}

class BankingUtils {
    public static NumberFormat getINRFormatter() {
        return NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    }

    public static String readPinMasked(Scanner sc) {
        Console console = System.console();
        if (console != null) {
            char[] pwd = console.readPassword();
            return new String(pwd);
        } else {
            // fallback
            System.out.print("(input hidden not available here) Enter PIN: ");
            return sc.nextLine();
        }
    }

    public static String promptForPin(Scanner sc, String prompt) {
        System.out.print(prompt);
        return readPinMasked(sc);
    }

    public static double readPositiveDouble(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double v = Double.parseDouble(sc.nextLine().trim());
                if (v < 0) {
                    System.out.println("Enter a positive amount.");
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }
}

public class BankingSystemAdvanced {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Bank bank = Bank.loadDataOrCreate();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // save on normal exit / ctrl+c
            bank.saveData();
        }));

        while (true) {
            System.out.println("\n===== Advanced Banking System Menu =====");
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. Transfer");
            System.out.println("4. Balance Inquiry & Recent Transactions");
            System.out.println("5. Create New Account");
            System.out.println("6. List All Account Numbers (for testing)");
            System.out.println("7. Save & Exit");
            System.out.print("Enter choice: ");

            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1": {
                    System.out.print("Enter account number: ");
                    String acc = sc.nextLine().trim();
                    String pin = BankingUtils.promptForPin(sc, "Enter PIN: ");
                    Account a = bank.getAccountIfAuthenticated(acc, pin);
                    if (a != null) {
                        double amt = BankingUtils.readPositiveDouble(sc, "Enter deposit amount (₹): ");
                        if (a.deposit(amt)) {
                            System.out.println("Deposited " + BankingUtils.getINRFormatter().format(amt) + " successfully.");
                        } else {
                            System.out.println("Deposit failed.");
                        }
                    } else {
                        System.out.println("Invalid account or PIN.");
                    }
                    break;
                }
                case "2": {
                    System.out.print("Enter account number: ");
                    String acc = sc.nextLine().trim();
                    String pin = BankingUtils.promptForPin(sc, "Enter PIN: ");
                    Account a = bank.getAccountIfAuthenticated(acc, pin);
                    if (a != null) {
                        double amt = BankingUtils.readPositiveDouble(sc, "Enter withdraw amount (₹): ");
                        if (a.withdraw(amt)) {
                            System.out.println("Withdrew " + BankingUtils.getINRFormatter().format(amt) + " successfully.");
                        } else {
                            System.out.println("Withdrawal failed.");
                        }
                    } else {
                        System.out.println("Invalid account or PIN.");
                    }
                    break;
                }
                case "3": {
                    System.out.print("Enter your account number: ");
                    String from = sc.nextLine().trim();
                    String pin = BankingUtils.promptForPin(sc, "Enter your PIN: ");
                    System.out.print("Enter target account number: ");
                    String to = sc.nextLine().trim();
                    double amt = BankingUtils.readPositiveDouble(sc, "Enter transfer amount (₹): ");
                    if (bank.transfer(from, pin, to, amt)) {
                        System.out.println("Transfer successful.");
                    } else {
                        System.out.println("Transfer failed.");
                    }
                    break;
                }
                case "4": {
                    System.out.print("Enter account number: ");
                    String acc = sc.nextLine().trim();
                    String pin = BankingUtils.promptForPin(sc, "Enter PIN: ");
                    Account a = bank.getAccountIfAuthenticated(acc, pin);
                    if (a != null) {
                        a.displayInfo();
                    } else {
                        System.out.println("Invalid account or PIN.");
                    }
                    break;
                }
                case "5": {
                    System.out.print("Enter your full name: ");
                    String name = sc.nextLine().trim();
                    String pin;
                    while (true) {
                        pin = BankingUtils.promptForPin(sc, "Set a 4-digit PIN: ");
                        if (pin.matches("\\d{4}")) break;
                        System.out.println("PIN must be exactly 4 digits.");
                    }
                    double initial = BankingUtils.readPositiveDouble(sc, "Enter initial deposit amount (₹): ");
                    String accNum = bank.createNewAccount(name, pin, initial);
                    System.out.println("Account created successfully! Your account number: " + accNum);
                    break;
                }
                case "6": {
                    System.out.println("Accounts: " + bank.listAllAccountNumbers());
                    break;
                }
                case "7": {
                    System.out.println("Saving data and exiting...");
                    bank.saveData();
                    sc.close();
                    return;
                }
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
}
