# Java Banking System 💳

A simple **Java-based Banking Management System** that allows users to create accounts, deposit money, withdraw money, transfer funds, check balances, and view transaction history.  
This repository contains a single-file, easy-to-run implementation (`BankingSystem.java`) that uses Java serialization for persistence.

---

## 📌 Features (implemented)
- Create a new bank account (account number chosen by user)
- PIN-based authentication for account-sensitive operations
- Deposit money into an account
- Withdraw money with:
  - Minimum balance enforcement (₹500)
  - Daily withdrawal limit (₹10,000)
- Transfer money between accounts
- View transaction history (per account)
- Data persistence to `bank_data.ser` (accounts saved between runs)
- Currency displayed in **Indian Rupees (₹)** using `NumberFormat` for India

---

## 🛠 Tech Stack
- **Language:** Java (JDK 8+ recommended)
- **Key APIs:** `java.io.Serializable`, `ObjectInputStream`/`ObjectOutputStream`, `NumberFormat`, `HashMap`
- **Persistence:** Java object serialization to `bank_data.ser`

---

## 📂 Project Structure
--BankingSystem.java # Main entry point
--Bank.java # Bank management logic
--Account.java # Account details & operations


---

## 🚀 How to Run
1. Clone the repository:
   ```bash
   git clone https://github.com/surendrary123/Java-Banking-System.git
2. Navigate to the project folder:
bash
Copy code
cd Java-Banking-System
3. Compile the Java files:

bash
Copy code
javac BankingSystem.java

4. Run the program:

bash
Copy code
java BankingSystem

Example Usage:

Welcome to the Banking System
1. Create Account
2. Deposit
3. Withdraw
4. Display Account Info
5. Exit
Enter your choice: 1
Enter Account Number: 101
Enter Customer Name: John Doe
Enter Initial Balance: 5000
Account created successfully!

🔮 Future Improvements
Add PIN authentication for each account

Add transaction history feature

Implement interest calculation

Connect to a database (MySQL) for persistent storage

Create a simple GUI using JavaFX or Swing.

📜 License
This project is licensed under the MIT License – you are free to use, modify, and distribute it.

Author: S. Surendra Reddy
📅 Year: 2025


