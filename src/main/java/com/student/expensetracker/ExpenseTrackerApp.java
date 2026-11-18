package com.student.expensetracker;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Main JavaFX Application class for DHANLEKHA (Personal Expense Tracker).
 *
 * Demonstrates:
 * 1. Inheritance: Extends javafx.application.Application.
 * 2. Method Overriding: Overrides the start(Stage stage) lifecycle method.
 * 3. Composition: Owns an instance of ExpenseTracker (has-a relationship).
 * 4. Screen Navigation: Clean transition from Welcome Screen to Main Tracker within the same Stage.
 * 5. Event-Driven Programming: Handles button clicks, keyboard shortcuts, and table interactions.
 * 6. Modular UI Design: UI creation is decomposed into readable, dedicated helper methods.
 */
public class ExpenseTrackerApp extends Application {

    // Date & Currency formatters
    private static final DateTimeFormatter MONTH_HEADER_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final DateTimeFormatter DATE_COL_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("₹#,##0.00");

    // The backend business logic and persistence controller (Composition)
    private ExpenseTracker tracker;

    // Primary JavaFX Stage & Scene references for in-place screen navigation
    private Stage primaryStage;
    private Scene scene;
    private Parent trackerRoot;

    // Current month being viewed and managed
    private YearMonth currentMonth;

    // Track if we are editing an existing expense (null if in 'Add' mode)
    private Integer editingExpenseId = null;

    // --- UI Controls referenced across methods ---
    private Label lblCurrentMonth;
    private Label lblBudget;
    private Label lblTotalSpent;
    private Label lblRemainingBalance;

    private DatePicker datePicker;
    private ComboBox<String> categoryComboBox;
    private TextField amountField;
    private TextField descriptionField;
    private Button btnSubmitExpense;
    private Button btnCancelEdit;

    private ComboBox<String> categoryFilterCombo;
    private TableView<Expense> expenseTable;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.tracker = new ExpenseTracker();
        this.currentMonth = YearMonth.now();

        // 1. Set window icon using the standalone antique-gold D
        Image appIcon = loadResourceImage("app-icon.png");
        if (appIcon != null) {
            primaryStage.getIcons().add(appIcon);
        }

        // 2. Set initial window title
        primaryStage.setTitle("DHANLEKHA");

        // 3. Create the Welcome screen as the initial view
        Parent welcomeRoot = createWelcomeView();
        scene = new Scene(welcomeRoot, 940, 680);

        // Attach external CSS styling
        String cssPath = getClass().getResource("style.css") != null
                ? getClass().getResource("style.css").toExternalForm()
                : null;
        if (cssPath != null) {
            scene.getStylesheets().add(cssPath);
        }

        primaryStage.setMinWidth(840);
        primaryStage.setMinHeight(600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // =========================================================================
    // Welcome Screen & Navigation
    // =========================================================================

    /**
     * Builds the Welcome Screen featuring the antique-gold D + champagne DHANLEKHA logo
     * on a solid deep espresso background with a single Enter button.
     */
    private Parent createWelcomeView() {
        StackPane root = new StackPane();
        root.getStyleClass().add("welcome-pane");

        VBox centerBox = new VBox(28); // 28 px space between logo and button
        centerBox.setAlignment(Pos.CENTER);

        // Welcome Logo: D (antique gold #A9874F) + DHANLEKHA (champagne #E5D2A9)
        Image welcomeLogoImg = loadResourceImage("logo-welcome.png");
        ImageView logoView = new ImageView(welcomeLogoImg);
        logoView.setPreserveRatio(true);
        logoView.setSmooth(true);
        logoView.setFitWidth(420); // standard ~420px width on laptop

        // Allow logo to scale down proportionally if window is resized smaller
        root.widthProperty().addListener((obs, oldVal, newVal) -> {
            double w = newVal.doubleValue();
            double targetWidth = Math.min(420.0, Math.max(220.0, w * 0.65));
            logoView.setFitWidth(targetWidth);
        });

        // Enter Button: Champagne fill, espresso text, rounded corners
        Button btnEnter = new Button("Enter");
        btnEnter.getStyleClass().add("btn-enter");
        btnEnter.setDefaultButton(true); // Triggers action on Enter key press
        btnEnter.setOnAction(e -> showTracker());

        centerBox.getChildren().addAll(logoView, btnEnter);
        root.getChildren().add(centerBox);

        return root;
    }

    /**
     * Transitions from Welcome Screen to Main Tracker in the same Stage,
     * preserving window dimensions, position, and maximized state.
     */
    private void showTracker() {
        primaryStage.setTitle("DHANLEKHA — Personal Expense Tracker");

        if (trackerRoot == null) {
            trackerRoot = createTrackerView();
        }

        scene.setRoot(trackerRoot);
        refreshView();
    }

    // =========================================================================
    // Main Tracker Screen Builders
    // =========================================================================

    /**
     * Builds the main expense tracker view hierarchy.
     */
    private Parent createTrackerView() {
        VBox root = new VBox();
        root.setSpacing(10);
        root.getStyleClass().addAll("root", "tracker-root");

        HBox header = createHeader();
        HBox summary = createSummarySection();
        VBox form = createExpenseForm();
        VBox tableSection = createExpenseTable();
        HBox actions = createTableActions();

        // Give the expense table most of the available vertical space
        VBox.setVgrow(tableSection, Priority.ALWAYS);

        root.getChildren().addAll(header, summary, form, tableSection, actions);
        return root;
    }

    /**
     * Builds Section A: Branded Header with compact DHANLEKHA logo and month navigation.
     */
    private HBox createHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("header-box");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(14);

        // Compact brand lockup (antique gold D + deep espresso DHANLEKHA)
        Image headerLogoImg = loadResourceImage("logo-header.png");
        ImageView headerLogo = new ImageView(headerLogoImg);
        headerLogo.setPreserveRatio(true);
        headerLogo.setSmooth(true);
        headerLogo.setFitHeight(30);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnPrevMonth = new Button("< Previous");
        btnPrevMonth.getStyleClass().add("nav-button");
        btnPrevMonth.setOnAction(e -> {
            currentMonth = currentMonth.minusMonths(1);
            if (editingExpenseId != null) {
                cancelEdit();
            }
            refreshView();
        });

        lblCurrentMonth = new Label(currentMonth.format(MONTH_HEADER_FORMAT));
        lblCurrentMonth.getStyleClass().add("month-display");

        Button btnNextMonth = new Button("Next >");
        btnNextMonth.getStyleClass().add("nav-button");
        btnNextMonth.setOnAction(e -> {
            currentMonth = currentMonth.plusMonths(1);
            if (editingExpenseId != null) {
                cancelEdit();
            }
            refreshView();
        });

        HBox navBox = new HBox(8, btnPrevMonth, lblCurrentMonth, btnNextMonth);
        navBox.setAlignment(Pos.CENTER_RIGHT);

        header.getChildren().addAll(headerLogo, spacer, navBox);
        return header;
    }

    /**
     * Builds Section B: Monthly Summary cards for Budget, Total Spent, and Remaining Balance.
     */
    private HBox createSummarySection() {
        HBox summary = new HBox();
        summary.getStyleClass().add("summary-container");
        summary.setAlignment(Pos.CENTER);

        // 1. Monthly Budget Card
        VBox budgetCard = new VBox(6);
        budgetCard.getStyleClass().add("summary-card");
        HBox.setHgrow(budgetCard, Priority.ALWAYS);

        Label lblBudgetHeading = new Label("MONTHLY BUDGET");
        lblBudgetHeading.getStyleClass().add("summary-label");

        lblBudget = new Label("Not set");
        lblBudget.getStyleClass().add("summary-value");

        Button btnSetBudget = new Button("Set Budget");
        btnSetBudget.getStyleClass().add("btn-small");
        btnSetBudget.setOnAction(e -> handleSetBudget());

        HBox budgetRow = new HBox(12, lblBudget, btnSetBudget);
        budgetRow.setAlignment(Pos.CENTER_LEFT);
        budgetCard.getChildren().addAll(lblBudgetHeading, budgetRow);

        // 2. Total Spent Card
        VBox spentCard = new VBox(6);
        spentCard.getStyleClass().add("summary-card");
        HBox.setHgrow(spentCard, Priority.ALWAYS);

        Label lblSpentHeading = new Label("TOTAL SPENT THIS MONTH");
        lblSpentHeading.getStyleClass().add("summary-label");

        lblTotalSpent = new Label("₹0.00");
        lblTotalSpent.getStyleClass().add("summary-value");
        spentCard.getChildren().addAll(lblSpentHeading, lblTotalSpent);

        // 3. Remaining Balance Card
        VBox balanceCard = new VBox(6);
        balanceCard.getStyleClass().add("summary-card");
        HBox.setHgrow(balanceCard, Priority.ALWAYS);

        Label lblBalanceHeading = new Label("REMAINING BALANCE");
        lblBalanceHeading.getStyleClass().add("summary-label");

        lblRemainingBalance = new Label("-");
        lblRemainingBalance.getStyleClass().add("summary-value");
        balanceCard.getChildren().addAll(lblBalanceHeading, lblRemainingBalance);

        summary.getChildren().addAll(budgetCard, spentCard, balanceCard);
        return summary;
    }

    /**
     * Builds Section C: Expense Entry Form.
     */
    private VBox createExpenseForm() {
        VBox formContainer = new VBox(10);
        formContainer.getStyleClass().add("form-card");
        VBox.setMargin(formContainer, new Insets(0, 22, 0, 22));

        Label formTitle = new Label("Record Expense");
        formTitle.getStyleClass().add("form-label");

        HBox fieldsRow = new HBox(10);
        fieldsRow.setAlignment(Pos.CENTER_LEFT);

        // Date Picker (Defaults to today)
        datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefWidth(135);
        datePicker.setPromptText("Date");

        // Category ComboBox (Hardcoded categories)
        categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().addAll(ExpenseTracker.CATEGORIES);
        categoryComboBox.setValue("Food");
        categoryComboBox.setPrefWidth(125);

        // Amount Field
        amountField = new TextField();
        amountField.setPromptText("Amount (₹)");
        amountField.setPrefWidth(110);

        // Description Field
        descriptionField = new TextField();
        descriptionField.setPromptText("Description (e.g. Canteen lunch)");
        HBox.setHgrow(descriptionField, Priority.ALWAYS);

        // Submit Button
        btnSubmitExpense = new Button("Add Expense");
        btnSubmitExpense.getStyleClass().add("btn-primary");
        btnSubmitExpense.setOnAction(e -> handleAddOrUpdateExpense());

        // Cancel Edit Button (Hidden by default)
        btnCancelEdit = new Button("Cancel");
        btnCancelEdit.getStyleClass().add("btn-secondary");
        btnCancelEdit.setVisible(false);
        btnCancelEdit.setManaged(false);
        btnCancelEdit.setOnAction(e -> cancelEdit());

        fieldsRow.getChildren().addAll(
                datePicker, categoryComboBox, amountField, descriptionField,
                btnSubmitExpense, btnCancelEdit
        );

        formContainer.getChildren().addAll(formTitle, fieldsRow);
        return formContainer;
    }

    /**
     * Builds Section D: Category filter and TableView displaying expenses.
     */
    private VBox createExpenseTable() {
        VBox tableBox = new VBox(8);
        tableBox.getStyleClass().add("table-container");

        // Filter Bar above table
        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        Label filterLabel = new Label("Filter by Category:");
        filterLabel.getStyleClass().add("form-label");

        categoryFilterCombo = new ComboBox<>();
        categoryFilterCombo.getItems().add("All Categories");
        categoryFilterCombo.getItems().addAll(ExpenseTracker.CATEGORIES);
        categoryFilterCombo.setValue("All Categories");
        categoryFilterCombo.setOnAction(e -> populateTable());

        filterBar.getChildren().addAll(filterLabel, categoryFilterCombo);

        // TableView setup
        expenseTable = new TableView<>();
        expenseTable.setPlaceholder(new Label("No expenses found for this month."));

        // Column 1: Date
        TableColumn<Expense, String> colDate = new TableColumn<>("Date");
        colDate.setPrefWidth(120);
        colDate.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDate().format(DATE_COL_FORMAT)));

        // Column 2: Category
        TableColumn<Expense, String> colCategory = new TableColumn<>("Category");
        colCategory.setPrefWidth(130);
        colCategory.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getCategory()));

        // Column 3: Description
        TableColumn<Expense, String> colDescription = new TableColumn<>("Description");
        colDescription.setPrefWidth(340);
        colDescription.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDescription()));

        // Column 4: Amount (₹) - Right Aligned
        TableColumn<Expense, String> colAmount = new TableColumn<>("Amount (₹)");
        colAmount.setPrefWidth(140);
        colAmount.setStyle("-fx-alignment: CENTER-RIGHT;");
        colAmount.setCellValueFactory(cell ->
                new SimpleStringProperty(formatCurrency(cell.getValue().getAmount())));

        expenseTable.getColumns().addAll(colDate, colCategory, colDescription, colAmount);
        expenseTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox.setVgrow(expenseTable, Priority.ALWAYS);
        tableBox.getChildren().addAll(filterBar, expenseTable);
        return tableBox;
    }

    /**
     * Builds Section E: Table action buttons (Edit Selected and Delete Selected).
     */
    private HBox createTableActions() {
        HBox actions = new HBox(12);
        actions.getStyleClass().add("action-bar");

        Button btnEdit = new Button("Edit Selected");
        btnEdit.getStyleClass().add("btn-secondary");
        btnEdit.setOnAction(e -> handleEditSelected());

        Button btnDelete = new Button("Delete Selected");
        btnDelete.getStyleClass().add("btn-danger");
        btnDelete.setOnAction(e -> handleDeleteSelected());

        actions.getChildren().addAll(btnEdit, btnDelete);
        return actions;
    }

    // =========================================================================
    // Controller Action Handlers
    // =========================================================================

    /**
     * Handles adding a new expense or saving modifications to an existing expense.
     */
    private void handleAddOrUpdateExpense() {
        LocalDate date = datePicker.getValue();
        if (date == null) {
            showError("Please select a valid date.");
            return;
        }

        String category = categoryComboBox.getValue();
        if (category == null || category.trim().isEmpty()) {
            showError("Please select a category.");
            return;
        }

        BigDecimal amount;
        try {
            amount = ExpenseTracker.validateAmount(amountField.getText());
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            return;
        }

        String description = descriptionField.getText();

        try {
            if (editingExpenseId == null) {
                // ADD MODE: Insert new expense
                tracker.addExpense(date, category, amount, description);

                // If entered date belongs to a different month, switch to that month
                YearMonth expenseMonth = YearMonth.from(date);
                if (!expenseMonth.equals(currentMonth)) {
                    currentMonth = expenseMonth;
                    categoryFilterCombo.setValue("All Categories");
                }
            } else {
                // EDIT MODE: Update existing expense by ID
                tracker.updateExpense(editingExpenseId, date, category, amount, description);

                // Switch month if needed so the updated record remains visible
                YearMonth expenseMonth = YearMonth.from(date);
                if (!expenseMonth.equals(currentMonth)) {
                    currentMonth = expenseMonth;
                    categoryFilterCombo.setValue("All Categories");
                } else {
                    // Reset filter if category no longer matches active filter
                    String currentFilter = categoryFilterCombo.getValue();
                    if (currentFilter != null && !currentFilter.equals("All Categories")
                            && !currentFilter.equals(category)) {
                        categoryFilterCombo.setValue("All Categories");
                    }
                }
                cancelEdit();
            }

            // Clear inputs for easy subsequent entries
            amountField.clear();
            descriptionField.clear();

            refreshView();
        } catch (IOException ex) {
            showError("Failed to save expense data: " + ex.getMessage());
        }
    }

    /**
     * Loads selected expense details into the form for editing.
     */
    private void handleEditSelected() {
        Expense selected = expenseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select an expense from the table to edit.");
            return;
        }

        editingExpenseId = selected.getId();
        datePicker.setValue(selected.getDate());
        categoryComboBox.setValue(selected.getCategory());
        amountField.setText(selected.getAmount().toPlainString());
        descriptionField.setText(selected.getDescription());

        btnSubmitExpense.setText("Save Changes");
        btnCancelEdit.setVisible(true);
        btnCancelEdit.setManaged(true);
    }

    /**
     * Cancels the edit operation and restores the form to Add mode.
     */
    private void cancelEdit() {
        editingExpenseId = null;
        btnSubmitExpense.setText("Add Expense");
        btnCancelEdit.setVisible(false);
        btnCancelEdit.setManaged(false);

        datePicker.setValue(LocalDate.now());
        categoryComboBox.setValue("Food");
        amountField.clear();
        descriptionField.clear();
    }

    /**
     * Deletes the selected expense after asking for user confirmation.
     */
    private void handleDeleteSelected() {
        Expense selected = expenseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select an expense from the table to delete.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to delete this "
                + selected.getCategory() + " expense of " + formatCurrency(selected.getAmount()) + "?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                tracker.deleteExpense(selected.getId());

                // If the deleted expense was currently being edited, reset form
                if (editingExpenseId != null && editingExpenseId == selected.getId()) {
                    cancelEdit();
                }

                refreshView();
            } catch (IOException ex) {
                showError("Failed to delete expense: " + ex.getMessage());
            }
        }
    }

    /**
     * Prompts the user with a dialog to enter or update the budget for the current month.
     */
    private void handleSetBudget() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Set Monthly Budget");
        dialog.setHeaderText("Set budget for " + currentMonth.format(MONTH_HEADER_FORMAT));
        dialog.setContentText("Enter budget amount (₹):");

        BigDecimal existing = tracker.getBudget(currentMonth);
        if (existing != null) {
            dialog.getEditor().setText(existing.toPlainString());
        }

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                BigDecimal budget = ExpenseTracker.validateAmount(result.get());
                tracker.setBudget(currentMonth, budget);
                refreshView();
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            } catch (IOException ex) {
                showError("Failed to save budget: " + ex.getMessage());
            }
        }
    }

    // =========================================================================
    // View Synchronization & Formatting Helpers
    // =========================================================================

    /**
     * Refreshes the header, summary statistics, and table rows for the current view.
     */
    private void refreshView() {
        // 1. Update Month Header
        lblCurrentMonth.setText(currentMonth.format(MONTH_HEADER_FORMAT));

        // 2. Update Monthly Budget Display
        BigDecimal budget = tracker.getBudget(currentMonth);
        if (budget == null) {
            lblBudget.setText("Not set");
        } else {
            lblBudget.setText(formatCurrency(budget));
        }

        // 3. Update Total Spent (Always reflects ALL categories for the current month)
        BigDecimal totalSpent = tracker.calculateTotalForMonth(currentMonth);
        lblTotalSpent.setText(formatCurrency(totalSpent));

        // 4. Update Remaining Balance
        BigDecimal balance = tracker.calculateRemainingBalance(currentMonth);
        lblRemainingBalance.getStyleClass().removeAll("summary-value-negative", "summary-value");
        if (balance == null) {
            lblRemainingBalance.setText("-");
            lblRemainingBalance.getStyleClass().add("summary-value");
        } else {
            lblRemainingBalance.setText(formatCurrency(balance));
            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                lblRemainingBalance.getStyleClass().add("summary-value-negative");
            } else {
                lblRemainingBalance.getStyleClass().add("summary-value");
            }
        }

        // 5. Populate Table
        populateTable();
    }

    /**
     * Populates the TableView based on current month and selected category filter.
     */
    private void populateTable() {
        String filter = (categoryFilterCombo != null) ? categoryFilterCombo.getValue() : "All Categories";
        List<Expense> list = tracker.getFilteredExpenses(currentMonth, filter);
        expenseTable.getItems().setAll(list);
    }

    /**
     * Formats a BigDecimal into a standard Indian Rupee string with 2 decimal places.
     */
    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "₹0.00";
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            return "-₹" + CURRENCY_FORMAT.format(amount.abs()).substring(1);
        }
        return CURRENCY_FORMAT.format(amount);
    }

    /**
     * Loads an image asset from classpath resources.
     */
    private Image loadResourceImage(String filename) {
        try (InputStream stream = getClass().getResourceAsStream(filename)) {
            if (stream != null) {
                return new Image(stream);
            }
        } catch (IOException e) {
            System.err.println("Could not load image resource " + filename + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Displays an Alert dialog with an error message.
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Input Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
