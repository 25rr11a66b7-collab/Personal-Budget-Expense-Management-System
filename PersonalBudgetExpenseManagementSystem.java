import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Personal Budget & Expense Management System
 * A desktop GUI application built with Java Swing.
 *
 * Data is saved automatically in budget_transactions.txt
 * in the same folder from which the program is run.
 */
public class PersonalBudgetExpenseManagementSystem extends JFrame {

    private static final String DATA_FILE = "budget_transactions.txt";

    private final List<Transaction> transactions = new ArrayList<>();

    private JLabel totalIncomeLabel;
    private JLabel totalExpenseLabel;
    private JLabel balanceLabel;
    private DefaultTableModel tableModel;
    private JTable historyTable;

    private JTextField incomeDateField;
    private JTextField incomeAmountField;
    private JTextField incomeSourceField;

    private JTextField expenseDateField;
    private JTextField expenseAmountField;
    private JComboBox<String> expenseCategoryBox;
    private JTextField expenseDescriptionField;

    private static final Color DARK_GREEN = new Color(20, 83, 45);
    private static final Color GREEN = new Color(34, 139, 84);
    private static final Color RED = new Color(220, 65, 65);
    private static final Color BLUE = new Color(45, 112, 210);
    private static final Color BACKGROUND = new Color(245, 248, 246);

    public PersonalBudgetExpenseManagementSystem() {
        setTitle("Personal Budget & Expense Management System");
        setSize(1000, 680);
        setMinimumSize(new Dimension(850, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        loadTransactions();
        buildInterface();
        refreshAllViews();
    }

    private void buildInterface() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND);

        JLabel heading = new JLabel(
                "  PERSONAL BUDGET & EXPENSE MANAGEMENT SYSTEM",
                SwingConstants.CENTER
        );
        heading.setOpaque(true);
        heading.setBackground(DARK_GREEN);
        heading.setForeground(Color.WHITE);
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        heading.setBorder(BorderFactory.createEmptyBorder(18, 10, 18, 10));
        mainPanel.add(heading, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 14));
        tabs.addTab("Dashboard", createDashboardPanel());
        tabs.addTab("Income", createIncomePanel());
        tabs.addTab("Expense", createExpensePanel());
        tabs.addTab("History", createHistoryPanel());
        tabs.addTab("Budget Tips", createTipsPanel());

        mainPanel.add(tabs, BorderLayout.CENTER);

        JLabel footer = new JLabel(
                "Your money, your plan. Track every rupee!",
                SwingConstants.CENTER
        );
        footer.setFont(new Font("Arial", Font.ITALIC, 12));
        footer.setForeground(new Color(90, 100, 90));
        footer.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
        mainPanel.add(footer, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel welcome = new JLabel("Welcome to your Budget Dashboard");
        welcome.setFont(new Font("Arial", Font.BOLD, 23));
        welcome.setForeground(DARK_GREEN);
        panel.add(welcome, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 3, 18, 18));
        cards.setOpaque(false);

        totalIncomeLabel = new JLabel("₹0.00", SwingConstants.CENTER);
        totalExpenseLabel = new JLabel("₹0.00", SwingConstants.CENTER);
        balanceLabel = new JLabel("₹0.00", SwingConstants.CENTER);

        cards.add(createSummaryCard("TOTAL INCOME", totalIncomeLabel, GREEN));
        cards.add(createSummaryCard("TOTAL EXPENSE", totalExpenseLabel, RED));
        cards.add(createSummaryCard("CURRENT BALANCE", balanceLabel, BLUE));

        JLabel note = new JLabel(
                "<html><div style='text-align:center;'>"
                        + "Use the Income tab to record money received.<br><br>"
                        + "Use the Expense tab to record your spending.<br><br>"
                        + "Open History to view or delete saved transactions."
                        + "</div></html>",
                SwingConstants.CENTER
        );
        note.setFont(new Font("Arial", Font.PLAIN, 16));
        note.setForeground(new Color(65, 75, 70));

        JPanel center = new JPanel(new BorderLayout(15, 35));
        center.setOpaque(false);
        center.add(cards, BorderLayout.NORTH);
        center.add(note, BorderLayout.CENTER);

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSummaryCard(String title, JLabel amountLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout(8, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 228, 222)),
                BorderFactory.createEmptyBorder(25, 12, 25, 12)
        ));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(color);

        amountLabel.setFont(new Font("Arial", Font.BOLD, 25));
        amountLabel.setForeground(color);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(amountLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createIncomePanel() {
        JPanel panel = createFormPanel();
        incomeDateField = new JTextField(LocalDate.now().toString(), 20);
        incomeAmountField = new JTextField(20);
        incomeSourceField = new JTextField(20);

        addFormRow(panel, "Date (YYYY-MM-DD):", incomeDateField, 0);
        addFormRow(panel, "Amount (₹):", incomeAmountField, 1);
        addFormRow(panel, "Income Source:", incomeSourceField, 2);

        JButton addButton = createButton("Add Income", GREEN);
        addButton.addActionListener(e -> addIncome());
        addButton.setBounds(330, 270, 180, 42);
        panel.add(addButton);

        return panel;
    }

    private JPanel createExpensePanel() {
        JPanel panel = createFormPanel();
        expenseDateField = new JTextField(LocalDate.now().toString(), 20);
        expenseAmountField = new JTextField(20);
        expenseCategoryBox = new JComboBox<>(new String[]{
                "Food", "Travel", "Education", "Shopping",
                "Bills", "Health", "Entertainment", "Other"
        });
        expenseDescriptionField = new JTextField(20);

        addFormRow(panel, "Date (YYYY-MM-DD):", expenseDateField, 0);
        addFormRow(panel, "Amount (₹):", expenseAmountField, 1);
        addFormRow(panel, "Category:", expenseCategoryBox, 2);
        addFormRow(panel, "Description:", expenseDescriptionField, 3);

        JButton addButton = createButton("Add Expense", RED);
        addButton.addActionListener(e -> addExpense());
        addButton.setBounds(330, 315, 180, 42);
        panel.add(addButton);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BACKGROUND);

        JPanel form = new JPanel(null);
        form.setPreferredSize(new Dimension(650, 410));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 228, 222)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        outer.add(form);
        return form;
    }

    private void addFormRow(JPanel panel, String labelText, JComponent field, int row) {
        int y = 65 + row * 65;

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setBounds(75, y, 190, 30);

        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBounds(270, y, 290, 34);

        panel.add(label);
        panel.add(field);
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        String[] columns = {"Type", "Date", "Amount (₹)", "Category / Source", "Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        historyTable.setFont(new Font("Arial", Font.PLAIN, 13));
        historyTable.setRowHeight(28);
        historyTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);

        JButton deleteButton = createButton("Delete Selected Transaction", RED);
        deleteButton.addActionListener(e -> deleteSelectedTransaction());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(deleteButton);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTipsPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 15, 15));
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        String[][] tips = {
                {"Track Your Expenses", "Record your daily spending to understand where your money goes."},
                {"Set a Budget Goal", "Decide how much you want to spend in each month."},
                {"Save First", "Keep a portion of your available money for savings."},
                {"Avoid Impulse Purchases", "Think carefully before buying non-essential items."},
                {"Use Expense Categories", "Group spending into food, travel, bills and other categories."},
                {"Review Regularly", "Check your transaction history and current balance often."}
        };

        for (String[] tip : tips) {
            JPanel card = new JPanel(new BorderLayout(5, 8));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 228, 222)),
                    BorderFactory.createEmptyBorder(18, 18, 18, 18)
            ));

            JLabel title = new JLabel(tip[0], SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 16));
            title.setForeground(DARK_GREEN);

            JTextArea description = new JTextArea(tip[1]);
            description.setFont(new Font("Arial", Font.PLAIN, 14));
            description.setLineWrap(true);
            description.setWrapStyleWord(true);
            description.setEditable(false);
            description.setOpaque(false);

            card.add(title, BorderLayout.NORTH);
            card.add(description, BorderLayout.CENTER);
            panel.add(card);
        }

        return panel;
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void addIncome() {
        try {
            LocalDate date = LocalDate.parse(incomeDateField.getText().trim());
            double amount = parseAmount(incomeAmountField.getText());
            String source = incomeSourceField.getText().trim();

            if (source.isEmpty()) {
                showError("Please enter the income source.");
                return;
            }

            transactions.add(new Transaction("Income", date.toString(), amount, source, ""));
            saveTransactions();
            refreshAllViews();

            incomeDateField.setText(LocalDate.now().toString());
            incomeAmountField.setText("");
            incomeSourceField.setText("");
            JOptionPane.showMessageDialog(this, "Income added successfully!");
        } catch (DateTimeParseException ex) {
            showError("Enter the date in YYYY-MM-DD format.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void addExpense() {
        try {
            LocalDate date = LocalDate.parse(expenseDateField.getText().trim());
            double amount = parseAmount(expenseAmountField.getText());
            String category = (String) expenseCategoryBox.getSelectedItem();
            String description = expenseDescriptionField.getText().trim();

            transactions.add(new Transaction(
                    "Expense", date.toString(), amount, category, description
            ));
            saveTransactions();
            refreshAllViews();

            expenseDateField.setText(LocalDate.now().toString());
            expenseAmountField.setText("");
            expenseDescriptionField.setText("");
            expenseCategoryBox.setSelectedIndex(0);
            JOptionPane.showMessageDialog(this, "Expense added successfully!");
        } catch (DateTimeParseException ex) {
            showError("Enter the date in YYYY-MM-DD format.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private double parseAmount(String text) {
        try {
            double amount = Double.parseDouble(text.trim());
            if (!Double.isFinite(amount) || amount <= 0) {
                throw new NumberFormatException();
            }
            return amount;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Enter a valid amount greater than zero.");
        }
    }

    private void deleteSelectedTransaction() {
        int selectedRow = historyTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a transaction to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this transaction?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            transactions.remove(selectedRow);
            saveTransactions();
            refreshAllViews();
        }
    }

    private void refreshAllViews() {
        double income = 0;
        double expense = 0;

        if (tableModel != null) {
            tableModel.setRowCount(0);
        }

        for (Transaction transaction : transactions) {
            if ("Income".equals(transaction.type)) {
                income += transaction.amount;
            } else {
                expense += transaction.amount;
            }

            if (tableModel != null) {
                tableModel.addRow(new Object[]{
                        transaction.type,
                        transaction.date,
                        String.format("%.2f", transaction.amount),
                        transaction.category,
                        transaction.description
                });
            }
        }

        if (totalIncomeLabel != null) {
            totalIncomeLabel.setText(formatRupees(income));
            totalExpenseLabel.setText(formatRupees(expense));
            balanceLabel.setText(formatRupees(income - expense));
        }
    }

    private String formatRupees(double amount) {
        return String.format("₹%.2f", amount);
    }

    private void saveTransactions() {
        try (BufferedWriter writer = Files.newBufferedWriter(
                Paths.get(DATA_FILE), StandardCharsets.UTF_8)) {

            for (Transaction t : transactions) {
                writer.write(encode(t.type) + "|"
                        + encode(t.date) + "|"
                        + t.amount + "|"
                        + encode(t.category) + "|"
                        + encode(t.description));
                writer.newLine();
            }
        } catch (IOException ex) {
            showError("Could not save data: " + ex.getMessage());
        }
    }

    private void loadTransactions() {
        Path path = Paths.get(DATA_FILE);
        if (!Files.exists(path)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length == 5) {
                    try {
                        transactions.add(new Transaction(
                                decode(parts[0]),
                                decode(parts[1]),
                                Double.parseDouble(parts[2]),
                                decode(parts[3]),
                                decode(parts[4])
                        ));
                    } catch (RuntimeException ignored) {
                        // Skip a damaged record and continue loading the remaining data.
                    }
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "Could not load saved data: " + ex.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Input Error", JOptionPane.ERROR_MESSAGE);
    }

    private static class Transaction {
        String type;
        String date;
        double amount;
        String category;
        String description;

        Transaction(String type, String date, double amount, String category, String description) {
            this.type = type;
            this.date = date;
            this.amount = amount;
            this.category = category;
            this.description = description;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PersonalBudgetExpenseManagementSystem app =
                    new PersonalBudgetExpenseManagementSystem();
            app.setVisible(true);
        });
    }
}