# DHANLEKHA — Personal Expense Tracker

A clean, offline desktop application developed for college students to track daily personal expenses (canteen food, bus fare, stationery, mobile recharge, etc.) against a monthly budget. Built with **Java 17**, **JavaFX**, and **Maven** adhering strictly to core Object-Oriented Programming (OOP) principles.

Branded as **DHANLEKHA**, featuring a rich brand identity: an antique-gold stepped **D** monogram, deep espresso backgrounds, champagne accents, and an ivory desktop tracker.

---

## 1. Project Objective

The objective of this project is to provide students with a lightweight, reliable desktop tool to monitor monthly spending habits, visualize where their money goes, and avoid budget deficits. The application introduces a calm, uncluttered **Welcome Screen** upon launch and transitions smoothly into the main tracker interface upon activating **Enter**, demonstrating clean OOP design, in-place JavaFX scene graph navigation, standard Java file I/O persistence without external database dependencies, explicit UI layouts, and defensive input validation.

---

## 2. Technology Stack & Prerequisites

- **Java Development Kit (JDK)**: JDK 17 or higher (tested with Microsoft OpenJDK 21 LTS).
- **Build Tool**: Apache Maven 3.8+ (A standalone portable Maven wrapper `mvn.cmd` / `mvn` is bundled directly in the project root).
- **GUI Framework**: JavaFX 21 (managed via Maven dependencies and `javafx-maven-plugin`).
- **Data Persistence**: Local TSV (Tab-Separated Values) flat files (`data/expenses.tsv` and `data/budgets.tsv`).
- **Brand Assets**: Custom transparent PNG lockups and square desktop app icon under `src/main/resources/com/student/expensetracker/`.

---

## 3. How to Run the Project

### Option A: From Command Line (Quickest)

Open a terminal (PowerShell, Command Prompt, or Bash) in the project directory and run:

```bash
# On Windows (Command Prompt or PowerShell)
.\mvn.cmd javafx:run

# Or if Maven is installed globally on your system PATH:
mvn javafx:run
```

To run all automated unit tests and verify the logic:
```bash
.\mvn.cmd test
```

### Option B: In IntelliJ IDEA

1. Open **IntelliJ IDEA**.
2. Select **File -> Open...** and browse to this directory (`Java-mini-project`).
3. IntelliJ will detect `pom.xml` and automatically import dependencies.
4. Ensure your Project SDK is set to **Java 17 or higher** under **File -> Project Structure -> Project**.
5. In the right-hand sidebar, open the **Maven** tool window:
   - Navigate to `personal-expense-tracker` -> `Plugins` -> `javafx` -> double-click `javafx:run`.
   - Alternatively, navigate to `src/main/java/com/student/expensetracker/ExpenseTrackerApp.java`, right-click, and select **Run 'ExpenseTrackerApp.main()'**.

---

## 4. Class Architecture & Responsibilities

The application separates concerns across three core classes in the `com.student.expensetracker` package:

| Class | Responsibility | Key Methods / Fields |
| :--- | :--- | :--- |
| **`Expense.java`** | Entity Model representing one expense record. Enforces encapsulation and stable identity. | `getId()`, `getDate()`, `getCategory()`, `getAmount()`, `getDescription()`, setters for mutable fields. |
| **`ExpenseTracker.java`** | Business Logic and Data Controller. Manages in-memory collections, file persistence, validation, and calculations. | `addExpense()`, `updateExpense()`, `deleteExpense()`, `getFilteredExpenses()`, `calculateTotalForMonth()`, `setBudget()`, `validateAmount()`, `saveExpenses()`, `loadExpenses()`. |
| **`ExpenseTrackerApp.java`** | Presentation layer (JavaFX Application). Manages screen flow (Welcome Screen -> Tracker), constructs UI controls, binds event handlers, and refreshes views. | `start(Stage)`, `createWelcomeView()`, `showTracker()`, `createHeader()`, `createSummarySection()`, `createExpenseForm()`, `createExpenseTable()`, `refreshView()`, `showError()`. |

---

## 5. Mapping of Object-Oriented Programming (OOP) Concepts

You can point directly to these locations in your viva to explain each OOP pillar:

### 1. Encapsulation & Data Hiding
- **Where to point**: `Expense.java` lines 18–31 and `ExpenseTracker.java` lines 39–43.
- **Explanation**: All fields (`id`, `date`, `category`, `amount`, `description`) are declared `private`. Internal data cannot be corrupted from outside. Access is provided strictly through public getter and setter methods. In `ExpenseTracker`, `expenses` and `budgets` collections are private so external classes cannot bypass validation or persistence routines.

### 2. Immutability of Identity
- **Where to point**: `Expense.java` line 19 (`private final int id;`).
- **Explanation**: The `id` field is marked `final` with no setter method. When an expense is edited, its date, amount, or description may change, but its unique identifier remains stable.

### 3. Inheritance
- **Where to point**: `ExpenseTrackerApp.java` line 38 (`public class ExpenseTrackerApp extends Application`).
- **Explanation**: `ExpenseTrackerApp` inherits window lifecycle management, scene graphs, and stage handling directly from the JavaFX `Application` base class.

### 4. Method Overriding (Polymorphism)
- **Where to point**: `ExpenseTrackerApp.java` line 71 (`@Override public void start(Stage primaryStage)`) and `Expense.java` line 80 (`@Override public String toString()`).
- **Explanation**: The app overrides JavaFX's abstract `start(Stage)` method to customize the window icon, window title, and initial scene setup.

### 5. Composition ("Has-A" Relationship)
- **Where to point**: `ExpenseTrackerApp.java` line 48 (`private ExpenseTracker tracker;`).
- **Explanation**: Instead of bloating the UI with file I/O or calculation code, the application *has-a* `ExpenseTracker` instance. Similarly, `ExpenseTracker` *has-a* `List<Expense>` and a `Map<YearMonth, BigDecimal>`. This decouples business logic from presentation.

### 6. Clean Screen Navigation & State Preservation
- **Where to point**: `ExpenseTrackerApp.java` lines 106–162 (`createWelcomeView()` and `showTracker()`).
- **Explanation**: The application launches with `createWelcomeView()`. When the user clicks **Enter** or presses the **Enter** key (`btnEnter.setDefaultButton(true)`), `showTracker()` calls `scene.setRoot(trackerRoot)` in-place. This preserves the Stage size, position, and maximized state seamlessly without spawning separate windows.

### 7. Defensive Programming & Validation
- **Where to point**: `ExpenseTracker.java` lines 180–223 (`validateAmount(String input)`).
- **Explanation**: Centralized validation method rejecting blank inputs, alphabetic characters, negative numbers, zero, and inputs with more than two decimal places.

---

## 6. Data Storage & File Format

All records persist locally inside the `data/` folder created automatically on first run:

1. **`data/expenses.tsv`**: Stores expense records in Tab-Separated Values (TSV) format:
   ```tsv
   1	2026-11-18	Food	60.50	Canteen lunch
   2	2026-11-18	Travel	25.00	Bus fare
   ```
   Tabs and newlines inside descriptions are sanitized to maintain a strict one-line-per-record format.
2. **`data/budgets.tsv`**: Stores per-month budget limits in TSV format:
   ```tsv
   2026-11	3000.00
   ```
   Each month maintains an independent budget. Setting November's budget leaves December's budget untouched.

---

## 7. One-Minute Demonstration Script for Viva

Follow these exact steps during your demonstration to show the examiner all required behaviors:

1. **Launch App**: Run `.\mvn.cmd javafx:run`. 
   - Point out the **Welcome Screen** with solid deep espresso background (`#251D16`), the antique-gold D and champagne **DHANLEKHA** brand lockup, and the square window icon.
   - Point out the initial window title: `DHANLEKHA`.
2. **Enter Navigation**:
   - Press the **Enter** key or click the **Enter** button.
   - The screen transitions in-place to the main tracker view (preserving window size and maximized state).
   - Point out the updated window title: `DHANLEKHA — Personal Expense Tracker`, the compact branded header, and the ivory background (`#F7F3EA`).
3. **Set Monthly Budget**: Click **Set Budget**, enter `3000`, and click **OK**. The budget displays `₹3,000.00` and remaining balance displays `₹3,000.00`.
4. **Add First Expense**: Set category to **Food**, amount to `60.50`, description to `Canteen lunch`, and click **Add Expense**. The table shows the row, total spent updates to `₹60.50`, and balance becomes `₹2,939.50`.
5. **Add Second Expense**: Set category to **Travel**, amount to `25.00`, description to `Bus fare`, and click **Add Expense**. Total spent updates to `₹85.50` and remaining balance becomes `₹2,914.50`.
6. **Demonstrate Filtering**: In the "Filter by Category" dropdown, select **Food**. The table filters to only the lunch row, but notice **Total Spent This Month remains ₹85.50** (examiner check). Reset filter to **All Categories**.
7. **Demonstrate Edit**: Select the lunch row and click **Edit Selected**. The form loads its fields and the button changes to **Save Changes**. Change `60.50` to `70.50` and click **Save Changes**. The table updates and total spent recalculates to `₹95.50`.
8. **Demonstrate Delete**: Select the bus fare row and click **Delete Selected**. A confirmation dialog appears. Click **OK**. Bus fare disappears, total spent updates to `₹70.50`, and balance adjusts to `₹2,929.50`.
9. **Demonstrate Persistence**: Close the application window completely and run `.\mvn.cmd javafx:run` again. Press **Enter**. The budget `₹3,000.00` and the lunch expense `₹70.50` are automatically restored from disk.
10. **Month Isolation**: Click **Next >** to navigate to the next month. The table is empty and budget shows "Not set", proving each month is isolated. Click **< Previous** to return to your records.
11. **Defensive Validation**: In the amount field, type `abc` or `-50` or `12.345` and click **Add Expense**. A clean alert dialog explains the error without crashing the application.

---

## 8. Five Likely Viva Questions & Model Answers

### Q1: Why did you choose `BigDecimal` instead of `double` or `float` for money?
> **Answer**: `double` and `float` are binary floating-point numbers conforming to IEEE 754. They cannot accurately represent decimal fractions like 0.1 or 0.7, which introduces subtle rounding inaccuracies (e.g. `0.1 + 0.2 = 0.30000000000000004`). In financial applications, currency amounts must be exact to two decimal places, so `BigDecimal` is the standard industry choice in Java.

### Q2: How does your application achieve screen navigation without spawning multiple windows?
> **Answer**: In `ExpenseTrackerApp.java`, we maintain a single `Stage` and `Scene`. The `start()` method initializes the scene with `createWelcomeView()`. When the user activates the Enter button, `showTracker()` calls `scene.setRoot(trackerRoot)` and updates `primaryStage.setTitle(...)`. This technique swaps the root node of the scene graph in-place, keeping the exact window size, coordinates, and maximized state without any window flickering.

### Q3: How do you prevent ID collisions when new expenses are added after restarting the application?
> **Answer**: In `ExpenseTracker.loadExpenses()`, as existing records are read from `expenses.tsv`, we track the highest ID (`maxId`). Once the file read is complete, `nextId` is initialized to `maxId + 1`. This guarantees that every newly added expense receives a strictly incremental, unique ID regardless of how many times the application is restarted or whether rows were deleted.

### Q4: Why did you use TSV (Tab-Separated Values) instead of CSV (Comma-Separated Values)?
> **Answer**: Expense descriptions entered by users often contain commas (for example, `"Notebook, pen, and eraser"`). Parsing standard CSV would require quotes and escape handling. By using tabs (`\t`) as delimiters and sanitizing tabs from descriptions prior to writing, every single record is guaranteed to stay clean on a single line without complex CSV parser libraries.

### Q5: How does the application handle keyboard accessibility on the welcome screen?
> **Answer**: In `createWelcomeView()`, we call `btnEnter.setDefaultButton(true)`. In JavaFX, setting a button as the default button automatically links the keyboard `ENTER` key accelerator to that button's action handler. This allows the user to press Enter or click the button with identical behavior.
