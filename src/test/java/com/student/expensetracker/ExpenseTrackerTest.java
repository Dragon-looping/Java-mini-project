package com.student.expensetracker;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Automated test suite covering the 10 verification steps specified in the project prompt,
 * including CRUD operations, monthly filtering, calculations, file persistence, and validation.
 */
public class ExpenseTrackerTest {

    private File testExpensesFile;
    private File testBudgetsFile;
    private ExpenseTracker tracker;

    @BeforeEach
    void setUp() throws IOException {
        testExpensesFile = File.createTempFile("test_expenses", ".tsv");
        testBudgetsFile = File.createTempFile("test_budgets", ".tsv");
        testExpensesFile.deleteOnExit();
        testBudgetsFile.deleteOnExit();

        tracker = new ExpenseTracker(testExpensesFile, testBudgetsFile);
    }

    @AfterEach
    void tearDown() {
        if (testExpensesFile != null && testExpensesFile.exists()) {
            testExpensesFile.delete();
        }
        if (testBudgetsFile != null && testBudgetsFile.exists()) {
            testBudgetsFile.delete();
        }
    }

    @Test
    void testExactVerificationSequence() throws IOException {
        YearMonth currentMonth = YearMonth.now();
        LocalDate today = LocalDate.now();

        // 1. Set current month's budget to ₹3,000.00
        BigDecimal budget3000 = new BigDecimal("3000.00");
        tracker.setBudget(currentMonth, budget3000);
        assertEquals(budget3000, tracker.getBudget(currentMonth));

        // 2. Add Food / Canteen lunch / ₹60.50
        Expense lunch = tracker.addExpense(today, "Food", new BigDecimal("60.50"), "Canteen lunch");
        assertNotNull(lunch);
        assertTrue(lunch.getId() > 0);

        // 3. Add Travel / Bus fare / ₹25.00
        Expense bus = tracker.addExpense(today, "Travel", new BigDecimal("25.00"), "Bus fare");
        assertNotNull(bus);

        // 4. Confirm total spent is ₹85.50 and remaining balance is ₹2,914.50
        BigDecimal totalSpent = tracker.calculateTotalForMonth(currentMonth);
        assertEquals(new BigDecimal("85.50"), totalSpent);
        BigDecimal remaining = tracker.calculateRemainingBalance(currentMonth);
        assertEquals(new BigDecimal("2914.50"), remaining);

        // 5. Filter table to Food: only lunch should appear, monthly total still ₹85.50
        List<Expense> foodExpenses = tracker.getFilteredExpenses(currentMonth, "Food");
        assertEquals(1, foodExpenses.size());
        assertEquals("Canteen lunch", foodExpenses.get(0).getDescription());
        assertEquals(new BigDecimal("85.50"), tracker.calculateTotalForMonth(currentMonth));

        // 6. Edit lunch from ₹60.50 to ₹70.50: confirm monthly total becomes ₹95.50
        boolean updated = tracker.updateExpense(lunch.getId(), today, "Food", new BigDecimal("70.50"), "Canteen lunch");
        assertTrue(updated);
        assertEquals(new BigDecimal("95.50"), tracker.calculateTotalForMonth(currentMonth));
        assertEquals(new BigDecimal("2904.50"), tracker.calculateRemainingBalance(currentMonth));

        // 7. Return to All Categories and delete bus fare: confirm total becomes ₹70.50 and remaining becomes ₹2,929.50
        boolean deleted = tracker.deleteExpense(bus.getId());
        assertTrue(deleted);
        assertEquals(new BigDecimal("70.50"), tracker.calculateTotalForMonth(currentMonth));
        assertEquals(new BigDecimal("2929.50"), tracker.calculateRemainingBalance(currentMonth));
        List<Expense> allExpenses = tracker.getFilteredExpenses(currentMonth, "All Categories");
        assertEquals(1, allExpenses.size());
        assertEquals(lunch.getId(), allExpenses.get(0).getId());

        // 8. Close and reopen the application: confirm lunch entry and monthly budget are restored
        ExpenseTracker reloadedTracker = new ExpenseTracker(testExpensesFile, testBudgetsFile);
        assertEquals(budget3000, reloadedTracker.getBudget(currentMonth));
        List<Expense> reloadedList = reloadedTracker.getFilteredExpenses(currentMonth, "All Categories");
        assertEquals(1, reloadedList.size());
        assertEquals("Canteen lunch", reloadedList.get(0).getDescription());
        assertEquals(new BigDecimal("70.50"), reloadedList.get(0).getAmount());
        assertEquals(lunch.getId(), reloadedList.get(0).getId());

        // 9. Switch to another month and confirm it has its own records and budget
        YearMonth nextMonth = currentMonth.plusMonths(1);
        assertNull(reloadedTracker.getBudget(nextMonth));
        assertEquals(BigDecimal.ZERO, reloadedTracker.calculateTotalForMonth(nextMonth));
        assertTrue(reloadedTracker.getFilteredExpenses(nextMonth, "All Categories").isEmpty());

        // Set budget and expense in another month
        reloadedTracker.setBudget(nextMonth, new BigDecimal("4500.00"));
        reloadedTracker.addExpense(nextMonth.atDay(5), "Education", new BigDecimal("500.00"), "Textbook");

        assertEquals(new BigDecimal("4500.00"), reloadedTracker.getBudget(nextMonth));
        assertEquals(new BigDecimal("500.00"), reloadedTracker.calculateTotalForMonth(nextMonth));
        // Verify current month remains completely unaffected
        assertEquals(budget3000, reloadedTracker.getBudget(currentMonth));
        assertEquals(new BigDecimal("70.50"), reloadedTracker.calculateTotalForMonth(currentMonth));

        // 10. Try invalid amounts and confirm the app explains the problem without crashing
        assertThrows(IllegalArgumentException.class, () -> ExpenseTracker.validateAmount(""));
        assertThrows(IllegalArgumentException.class, () -> ExpenseTracker.validateAmount("   "));
        assertThrows(IllegalArgumentException.class, () -> ExpenseTracker.validateAmount("abc"));
        assertThrows(IllegalArgumentException.class, () -> ExpenseTracker.validateAmount("0"));
        assertThrows(IllegalArgumentException.class, () -> ExpenseTracker.validateAmount("-50"));
        assertThrows(IllegalArgumentException.class, () -> ExpenseTracker.validateAmount("50.123")); // >2 decimal places

        // Valid amounts
        assertEquals(new BigDecimal("50"), ExpenseTracker.validateAmount("50"));
        assertEquals(new BigDecimal("50.5"), ExpenseTracker.validateAmount("50.5"));
        assertEquals(new BigDecimal("50.50"), ExpenseTracker.validateAmount("50.50"));
    }

    @Test
    void testFormatCurrency() {
        assertEquals("₹60.50", ExpenseTrackerApp.formatCurrency(new BigDecimal("60.50")));
        assertEquals("₹3,000.00", ExpenseTrackerApp.formatCurrency(new BigDecimal("3000.00")));
        assertEquals("₹0.00", ExpenseTrackerApp.formatCurrency(BigDecimal.ZERO));
        assertEquals("-₹50.00", ExpenseTrackerApp.formatCurrency(new BigDecimal("-50.00")));
    }
}
