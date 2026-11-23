# Qbanking

A desktop banking application for **CSE202 – Object Oriented Analysis & Design with Java**
(student: Masego Wame, fcse25-035).

Built with **Java 26**, **Maven**, **JavaFX 26** (screens), **JDBC + H2** (database) and
**JUnit 5** (tests), using the **MVC** pattern.

---

## 1. What the app does

| Who | What they can do |
|-----|------------------|
| **Customer** | Register, log in, open Savings / Investment / Cheque accounts (several at once), deposit, withdraw (where allowed), view transaction history, update their details |
| **Bank staff** | Log in, see every account, run the **monthly interest payment** |

Banking rules (from the assignment):

| Account | Deposits | Withdrawals | Monthly interest | Opening rule |
|---------|----------|-------------|------------------|--------------|
| Savings | yes | **no** | 0.05 % | none |
| Investment | yes | yes (not more than the balance) | 5 % | at least **BWP 500.00** |
| Cheque | yes | yes (not more than the balance) | none | customer must give **employer name + address** |

Extra safety rules: an account always belongs to a customer, amounts must be positive with at most
2 decimals, a customer can only touch their **own** accounts, passwords are stored as salted
PBKDF2 hashes, and interest can only be paid **once per month** per account.

### Sample logins (created automatically the first time the app runs)

The database is filled with **10 sample customers** (as required for the presentation).

| Username | Password | Role |
|----------|----------|------|
| `masego` (has all 3 account types) | `Password@1` | Customer |
| `kabelo`, `neo`, `lesedi`, `tshepo`, `boitumelo`, `thabo`, `naledi`, `kagiso`, `lorato` | `Password@1` | Customer |
| `staff` | `Staff@123` | Bank staff |

---

## 2. How to run it in IntelliJ IDEA

### Step 1 – Install Java 26 inside IntelliJ
1. Use a current version of IntelliJ IDEA (it must know Java 26 – use **Help → Check for Updates** if
   JDK 26 is not offered below).
2. Open **File → Project Structure…** (`Ctrl+Alt+Shift+S`) → **Project**.
3. Click the **SDK** drop-down → **Download JDK…** → choose **Version 26** → **Download**.
4. Set **Language level** to **26**, then click **OK**.

### Step 2 – Open the project
1. **File → Open…** and select the **`qbanking`** folder (the one that contains `pom.xml`).
2. Choose **Open as Project** and **Trust Project**.
3. IntelliJ now downloads the libraries (JavaFX, H2, JUnit). **The first time, you need internet.**
   Wait for the progress bar at the bottom to finish.
   If nothing happens: open the **Maven** tool window (right side) and click **Reload All Maven Projects**.

> Make sure Maven also uses JDK 26:
> **Settings → Build, Execution, Deployment → Build Tools → Maven → Runner → JRE = Project JDK**
> and **… → Maven → Importing → JDK for importer = Project JDK**.

### Step 3 – Run the app
1. In the Project view open `src/main/java/com/qbanking/Launcher.java`.
2. Click the green ▶ arrow next to `public static void main` → **Run 'Launcher.main()'**.
3. The login window opens. Log in with one of the sample accounts above.

> **Always start from `Launcher`**, not from `QBankingApp`. Starting a class that extends
> `Application` directly gives *"JavaFX runtime components are missing"*.

**Another way (Maven):** Maven tool window → **qbanking → Plugins → javafx → javafx:run**.

### Step 4 – Run the tests
Right-click the folder `src/test/java` → **Run 'All Tests'**
(or Maven tool window → **Lifecycle → test**).

* `AccountTest` – unit tests for the rules inside the model classes.
* `BankingIntegrationTest` – full flows through controllers → model → DAOs → a temporary in-memory database.

---

## 3. Where is the data? How do I reset it?

H2 is a small database that runs inside the program, so **nothing has to be installed**.
Its files are in the folder **`data/`** inside the project (`qbanking.mv.db`).

* To start again from scratch: **close the app**, delete the `data` folder, run again.
* Only **one copy** of the app can use the database at a time.
* To use MySQL instead, change `URL`, `USER` and `PASSWORD` in `dao/Database.java` and add the MySQL
  driver dependency to `pom.xml`. The DAO classes use standard JDBC.

---

## 4. Project structure (MVC)

```
qbanking
├── pom.xml
└── src
    ├── main
    │   ├── java/com/qbanking
    │   │   ├── Launcher.java            <- start here in IntelliJ
    │   │   ├── QBankingApp.java         <- JavaFX application
    │   │   ├── model/        (M)  business rules: Account, SavingsAccount, InvestmentAccount,
    │   │   │                      ChequeAccount, InterestBearing, Customer, Transaction, User ...
    │   │   ├── view/         (V)  JavaFX screens: LoginView, RegisterView, CustomerDashboardView,
    │   │   │                      StaffDashboardView, OpenAccountDialog ... (no business logic)
    │   │   ├── controller/   (C)  AuthController, AccountController, CustomerController,
    │   │   │                      StaffController, Session  (sit between views and model)
    │   │   ├── dao/               JDBC code: Database, CustomerDAO, UserDAO, AccountDAO,
    │   │   │                      TransactionDAO, DatabaseInitializer (schema + sample data)
    │   │   └── util/              PasswordHasher
    │   └── resources/qbanking.css
    └── test/java/com/qbanking     AccountTest, BankingIntegrationTest
```

**Flow of one action** (e.g. *Deposit*):

```
CustomerDashboardView  ->  AccountController.deposit()  ->  Account.deposit()   (rules)
      (screen)               (checks input & owner)            (model)
                                      |
                                      +->  AccountDAO / TransactionDAO  ->  H2 database
```

The rule "views only talk to controllers, controllers talk to model + DAOs" is followed everywhere:
no class in `view` imports anything from `dao`, and no class in `model` knows about JavaFX or SQL.

---

## 5. OOP principles – where to find them

| Principle | Where |
|-----------|-------|
| **Abstraction** | `Account` is `abstract` (`withdraw()` and `getAccountType()` are abstract) |
| **Inheritance** | `SavingsAccount`, `InvestmentAccount`, `ChequeAccount` extend `Account` |
| **Interface** | `InterestBearing` (implemented by Savings and Investment; has `default` methods) |
| **Polymorphism** | `Account` references call the right `withdraw()`; `AccountDAO.toAccount()` builds the right subclass; `StaffController` pays interest to anything that is `InterestBearing` |
| **Overriding** | `withdraw()`, `getAccountType()`, `payMonthlyInterest()` in the subclasses |
| **Overloading** | `Account` constructors (new vs. loaded-from-database), `deposit(amount)` / `deposit(amount, description)`, `Customer` constructors |
| **Encapsulation** | Private fields; the balance changes only through `deposit`, `withdraw` and interest |

---

## 6. Troubleshooting

| Problem | Fix |
|---------|-----|
| `JavaFX runtime components are missing` | Run **`Launcher`**, not `QBankingApp` |
| `release version 26 not supported` / `invalid source release: 26` | Project SDK is not 26 – repeat Step 1 and the Maven JDK settings in Step 2 |
| Red imports like `javafx...` or `org.h2...` | Maven has not finished – **Reload All Maven Projects** (needs internet the first time) |
| `Qbanking could not open its database` | Another copy is running – close it. Or delete the `data` folder |
| Console warnings about *restricted methods* / *native access* | Harmless JavaFX warnings on newer JDKs |
