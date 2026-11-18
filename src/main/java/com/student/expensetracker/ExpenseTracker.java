package com.student.expensetracker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller/Manager class responsible for business logic, calculations,
 * and data persistence.
 *
 * Demonstrates:
 * 1. Collections: ArrayList for dynamic list of expenses, HashMap for monthly budgets.
 * 2. File I/O: Reading and writing TSV (Tab Separated Values) files using BufferedReader/BufferedWriter.
 * 3. Separation of Concerns: Keeps business logic and calculations decoupled from UI logic.
 * 4. Exception Handling: Handles IOExceptions gracefully and validates user input.
 */
public class ExpenseTracker {

    // Hardcoded fixed categories as required by the specification
    public static final List<String> CATEGORIES = List.of(
            "Food", "Travel", "Education", "Shopping", "Recharge", "Other"
    );

    // Default paths for local text file storage
    private final File expensesFile;
    private final File budgetsFile;

    // In-memory data collections
    private final List<Expense> expenses;
    private final Map<YearMonth, BigDecimal> budgets;

    // Counter to generate unique IDs for each expense
    private int nextId = 1;

    /**
     * Default constructor using standard 'data' folder files.
     */
    public ExpenseTracker() {
        this(new File("data", "expenses.tsv"), new File("data", "budgets.tsv"));
    }

    /**
     * Parameterized constructor allowing custom file locations (helpful for automated unit tests).
     */
    public ExpenseTracker(File expensesFile, File budgetsFile) {
        this.expensesFile = expensesFile;
        this.budgetsFile = budgetsFile;
        this.expenses = new ArrayList<>();
        this.budgets = new HashMap<>();

        // Ensure parent directories exist automatically
        ensureDataDirectoryExists();

        // Load existing data from files on startup
        loadData();
    }

    /**
     * Ensures the parent directory for the storage files exists.
     */
    private void ensureDataDirectoryExists() {
        File parentDir = expensesFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
    }

    // =========================================================================
    // Core Expense Operations (Add, Update, Delete)
    // =========================================================================

    /**
     * Creates and adds a new expense, assigns a unique ID, and saves to file.
     */
    public Expense addExpense(LocalDate date, String category, BigDecimal amount, String description) throws IOException {
        Expense expense = new Expense(nextId++, date, category, amount, description);
        expenses.add(expense);
        saveExpenses();
        return expense;
    }

    /**
     * Updates an existing expense by its unique ID, preserving the original ID.
     */
    public boolean updateExpense(int id, LocalDate date, String category, BigDecimal amount, String description) throws IOException {
        for (Expense expense : expenses) {
            if (expense.getId() == id) {
                expense.setDate(date);
                expense.setCategory(category);
                expense.setAmount(amount);
                expense.setDescription(description);
                saveExpenses();
                return true;
            }
        }
        return false;
    }

    /**
     * Deletes an expense matching the given ID and saves to file.
     */
    public boolean deleteExpense(int id) throws IOException {
        for (int i = 0; i < expenses.size(); i++) {
            if (expenses.get(i).getId() == id) {
                expenses.remove(i);
                saveExpenses();
                return true;
            }
        }
        return false;
    }

    // =========================================================================
    // Filtering and Calculations (Using standard loops for student clarity)
    // =========================================================================

    /**
     * Returns a list of expenses filtered by month and category.
     * If category is "All Categories" or null, all expenses for that month are returned.
     */
    public List<Expense> getFilteredExpenses(YearMonth month, String category) {
        List<Expense> result = new ArrayList<>();
        for (Expense expense : expenses) {
            YearMonth expenseMonth = YearMonth.from(expense.getDate());
            if (expenseMonth.equals(month)) {
                if (category == null || category.equalsIgnoreCase("All Categories")
                        || category.equalsIgnoreCase(expense.getCategory())) {
                    result.add(expense);
                }
            }
        }
        return result;
    }

    /**
     * Calculates the total money spent in a specific month across ALL categories.
     * This total remains independent of table category filters.
     */
    public BigDecimal calculateTotalForMonth(YearMonth month) {
        BigDecimal total = BigDecimal.ZERO;
        for (Expense expense : expenses) {
            YearMonth expenseMonth = YearMonth.from(expense.getDate());
            if (expenseMonth.equals(month)) {
                total = total.add(expense.getAmount());
            }
        }
        return total;
    }

    // =========================================================================
    // Budget Management
    // =========================================================================

    /**
     * Sets the budget for a specific month and persists it to file.
     */
    public void setBudget(YearMonth month, BigDecimal budget) throws IOException {
        budgets.put(month, budget);
        saveBudgets();
    }

    /**
     * Retrieves the budget for a specific month, or null if not set.
     */
    public BigDecimal getBudget(YearMonth month) {
        return budgets.get(month);
    }

    /**
     * Calculates the remaining balance for a month: (Monthly Budget - Total Spent).
     * Returns null if no budget has been set for that month.
     */
    public BigDecimal calculateRemainingBalance(YearMonth month) {
        BigDecimal budget = getBudget(month);
        if (budget == null) {
            return null;
        }
        BigDecimal totalSpent = calculateTotalForMonth(month);
        return budget.subtract(totalSpent);
    }

    // =========================================================================
    // Input Validation
    // =========================================================================

    /**
     * Validates and parses currency input.
     * Accepts: "50", "50.5", "50.50".
     * Rejects: empty/blank, letters, negative numbers, zero, and more than 2 decimal places.
     */
    public static BigDecimal validateAmount(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Amount cannot be blank.");
        }

        String cleaned = input.trim();

        // Check if string contains negative sign
        if (cleaned.startsWith("-")) {
            throw new IllegalArgumentException("Enter an amount greater than ₹0.00.");
        }

        // Check decimal places
        if (cleaned.contains(".")) {
            String[] parts = cleaned.split("\\.", -1);
            if (parts.length > 2 || parts[1].length() > 2) {
                throw new IllegalArgumentException("Amount cannot have more than 2 decimal places.");
            }
        }

        // Validate regular expression for valid positive decimal number
        if (!cleaned.matches("^\\d+(\\.\\d{1,2})?$")) {
            throw new IllegalArgumentException("Enter a valid numeric amount (e.g. 50 or 50.50).");
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enter a valid numeric amount.");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Enter an amount greater than ₹0.00.");
        }

        return amount;
    }

    // =========================================================================
    // File Persistence (TSV format)
    // =========================================================================

    /**
     * Loads both expenses and budgets from their respective TSV files.
     */
    public void loadData() {
        loadExpenses();
        loadBudgets();
    }

    /**
     * Loads expenses from data/expenses.tsv.
     * Automatically adjusts nextId to be strictly greater than any existing ID.
     */
    public void loadExpenses() {
        expenses.clear();
        int maxId = 0;

        if (!expensesFile.exists()) {
            nextId = 1;
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(expensesFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                String[] tokens = line.split("\t", -1);
                if (tokens.length >= 4) {
                    try {
                        int id = Integer.parseInt(tokens[0]);
                        LocalDate date = LocalDate.parse(tokens[1]);
                        String category = tokens[2];
                        BigDecimal amount = new BigDecimal(tokens[3]);
                        String description = (tokens.length > 4) ? tokens[4] : "";

                        Expense expense = new Expense(id, date, category, amount, description);
                        expenses.add(expense);

                        if (id > maxId) {
                            maxId = id;
                        }
                    } catch (Exception e) {
                        System.err.println("Skipping malformed expense line: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading expenses file: " + e.getMessage());
        }

        nextId = maxId + 1;
    }

    /**
     * Saves all in-memory expenses to data/expenses.tsv in TSV format.
     */
    public void saveExpenses() throws IOException {
        ensureDataDirectoryExists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(expensesFile))) {
            for (Expense expense : expenses) {
                // Sanitize description: replace tabs and newlines with spaces to maintain single-line TSV format
                String cleanDesc = expense.getDescription()
                        .replace('\t', ' ')
                        .replace('\r', ' ')
                        .replace('\n', ' ')
                        .trim();

                String line = expense.getId() + "\t"
                        + expense.getDate() + "\t"
                        + expense.getCategory() + "\t"
                        + expense.getAmount().toPlainString() + "\t"
                        + cleanDesc;

                writer.write(line);
                writer.newLine();
            }
        }
    }

    /**
     * Loads monthly budgets from data/budgets.tsv.
     */
    public void loadBudgets() {
        budgets.clear();
        if (!budgetsFile.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(budgetsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                String[] tokens = line.split("\t", -1);
                if (tokens.length >= 2) {
                    try {
                        YearMonth month = YearMonth.parse(tokens[0]);
                        BigDecimal budget = new BigDecimal(tokens[1]);
                        budgets.put(month, budget);
                    } catch (Exception e) {
                        System.err.println("Skipping malformed budget line: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading budgets file: " + e.getMessage());
        }
    }

    /**
     * Saves all monthly budgets to data/budgets.tsv in TSV format.
     */
    public void saveBudgets() throws IOException {
        ensureDataDirectoryExists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(budgetsFile))) {
            for (Map.Entry<YearMonth, BigDecimal> entry : budgets.entrySet()) {
                String line = entry.getKey().toString() + "\t" + entry.getValue().toPlainString();
                writer.write(line);
                writer.newLine();
            }
        }
    }

    // =========================================================================
    // Getters for inspection and testing
    // =========================================================================

    public List<Expense> getAllExpenses() {
        return new ArrayList<>(expenses);
    }

    public int getNextId() {
        return nextId;
    }
}
