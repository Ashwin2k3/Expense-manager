import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

class Expense {
    private double amount;
    private String category;
    private String date;

    public Expense(double amount, String category, String date) {
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public String getDate() {
        return date;
    }
}

class ExpenseFileManager {
    private static final String FILE_PATH = "expenses.txt";

    public void saveExpenses(List<Expense> expenses) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (Expense expense : expenses) {
                writer.println(expense.getAmount() + "," + expense.getCategory() + "," + expense.getDate());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Expense> loadExpenses() {
        List<Expense> expenses = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    double amount = Double.parseDouble(parts[0]);
                    String category = parts[1];
                    String date = parts[2];
                    Expense expense = new Expense(amount, category, date);
                    expenses.add(expense);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return expenses;
    }
}

class ExpenseDAO {
    private List<Expense> expenses;
    private ExpenseFileManager fileManager;

    public ExpenseDAO() {
        expenses = new ArrayList<>();
        fileManager = new ExpenseFileManager();
        expenses = fileManager.loadExpenses();
    }

    public void addExpense(Expense expense) {
        expenses.add(expense);
        fileManager.saveExpenses(expenses);
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public void deleteExpense(int index) {
        if (index >= 0 && index < expenses.size()) {
            expenses.remove(index);
            fileManager.saveExpenses(expenses);
        }
    }

    public void updateExpense(int index, Expense expense) {
        if (index >= 0 && index < expenses.size()) {
            expenses.set(index, expense);
            fileManager.saveExpenses(expenses);
        }
    }
}

class ExpenseView {
    private ExpenseController controller;
    private JFrame frame;
    private JTextField amountTextField;
    private JTextField categoryTextField;
    private JTextField dateTextField;
    private JTable expensesTable;
    private DefaultTableModel tableModel;
    private JLabel totalSpendingLabel;

    public void setController(ExpenseController controller) {
        this.controller = controller;
    }

    public void createAndShowGUI() {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        frame = new JFrame("Expense Tracker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5);

        JLabel amountLabel = new JLabel("Amount:");
        constraints.gridx = 0;
        constraints.gridy = 0;
        inputPanel.add(amountLabel, constraints);

        amountTextField = new JTextField(10);
        constraints.gridx = 1;
        inputPanel.add(amountTextField, constraints);

        JLabel categoryLabel = new JLabel("Category:");
        constraints.gridx = 0;
        constraints.gridy = 1;
        inputPanel.add(categoryLabel, constraints);

        categoryTextField = new JTextField(10);
        constraints.gridx = 1;
        inputPanel.add(categoryTextField, constraints);

        JLabel dateLabel = new JLabel("Date:");
        constraints.gridx = 0;
        constraints.gridy = 2;
        inputPanel.add(dateLabel, constraints);

        dateTextField = new JTextField(10);
        constraints.gridx = 1;
        inputPanel.add(dateTextField, constraints);

        JButton addButton = new JButton("Add Expense");
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        inputPanel.add(addButton, constraints);

        contentPane.add(inputPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel();
        tableModel.addColumn("Amount");
        tableModel.addColumn("Category");
        tableModel.addColumn("Date");
        expensesTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(expensesTable);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        totalSpendingLabel = new JLabel("Total Spending: ₹ 0.00");
        bottomPanel.add(totalSpendingLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel();
        JButton deleteButton = new JButton("Delete");
        JButton editButton = new JButton("Edit");
        buttonPanel.add(deleteButton);
        buttonPanel.add(editButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        contentPane.add(bottomPanel, BorderLayout.SOUTH);

        frame.setContentPane(contentPane);
        frame.setVisible(true);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double amount = Double.parseDouble(amountTextField.getText());
                String category = categoryTextField.getText();
                String date = dateTextField.getText();
                controller.addExpense(amount, category, date);
                amountTextField.setText("");
                categoryTextField.setText("");
                dateTextField.setText("");
                updateExpensesTable();
                updateTotalSpendingLabel();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = expensesTable.getSelectedRow();
                if (selectedRow >= 0) {
                    controller.deleteExpense(selectedRow);
                    updateExpensesTable();
                    updateTotalSpendingLabel();
                }
            }
        });

        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = expensesTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String amount = JOptionPane.showInputDialog(frame, "Enter Amount:", tableModel.getValueAt(selectedRow, 0));
                    String category = JOptionPane.showInputDialog(frame, "Enter Category:", tableModel.getValueAt(selectedRow, 1));
                    String date = JOptionPane.showInputDialog(frame, "Enter Date:", tableModel.getValueAt(selectedRow, 2));
                    controller.updateExpense(selectedRow, new Expense(Double.parseDouble(amount), category, date));
                    updateExpensesTable();
                    updateTotalSpendingLabel();
                }
            }
        });
    }

    public void updateExpensesTable() {
        tableModel.setRowCount(0);
        List<Expense> expenses = controller.getExpenses();
        for (Expense expense : expenses) {
            Object[] rowData = {formatAmount(expense.getAmount()), expense.getCategory(), expense.getDate()};
            tableModel.addRow(rowData);
        }
    }

    public void updateTotalSpendingLabel() {
        double totalSpending = controller.getTotalSpending();
        totalSpendingLabel.setText("Total Spending: ₹" + formatAmount(totalSpending));
    }

    private String formatAmount(double amount) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return decimalFormat.format(amount);
    }
}

class ExpenseController {
    private ExpenseDAO expenseDAO;
    private ExpenseView expenseView;

    public ExpenseController(ExpenseDAO expenseDAO, ExpenseView expenseView) {
        this.expenseDAO = expenseDAO;
        this.expenseView = expenseView;
        this.expenseView.setController(this);
    }

    public void addExpense(double amount, String category, String date) {
        Expense expense = new Expense(amount, category, date);
        expenseDAO.addExpense(expense);
    }

    public List<Expense> getExpenses() {
        return expenseDAO.getExpenses();
    }

    public double getTotalSpending() {
        double total = 0;
        List<Expense> expenses = expenseDAO.getExpenses();
        for (Expense expense : expenses) {
            total += expense.getAmount();
        }
        return total;
    }

    public void deleteExpense(int index) {
        expenseDAO.deleteExpense(index);
    }

    public void updateExpense(int index, Expense expense) {
        expenseDAO.updateExpense(index, expense);
    }
    
}

public class ExpenseTrackerApp {
    public static void main(String[] args) {
        ExpenseDAO expenseDAO = new ExpenseDAO();
        ExpenseView expenseView = new ExpenseView();
        ExpenseController expenseController = new ExpenseController(expenseDAO, expenseView);
        expenseView.createAndShowGUI();
    }
}

