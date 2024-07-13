
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EmployeeInfoGUI {

    private static final Logger LOGGER = Logger.getLogger(EmployeeInfoGUI.class.getName());

    private static ArrayList<String[]> employeeData = new ArrayList<>();
    private static DefaultTableModel tableModel;
    private static JFrame frame;

    public static void main(String[] args) {
        String tsvFile = "employees.tsv"; // Path to your TSV file
        String line;
        String tsvSplitBy = "\t"; // Use tab as the delimiter for TSV

        try (BufferedReader br = new BufferedReader(new FileReader(tsvFile))) {
            // Skip the first row (header)
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] employee = line.split(tsvSplitBy);
                if (employee.length >= 8) { // Ensure there are at least 8 elements
                    employeeData.add(employee);
                } else {
                    LOGGER.warning("Employee data in TSV file is incomplete or malformed: " + line);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error reading employee data from TSV file", e);
        }

        // Sort the employee data based on Employee Number
        employeeData.sort(Comparator.comparingInt(employee -> Integer.parseInt(employee[0].trim())));

        // Create the GUI
        SwingUtilities.invokeLater(EmployeeInfoGUI::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        frame = new JFrame("Employee Info");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Input Panel
JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Input"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
       

        JLabel labelEmployeeNumber = new JLabel("Employee Number:");
        JTextField textEmployeeNumber = new JTextField(10);
        JButton buttonCalculate = new JButton("Calculate Salary");

gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(labelEmployeeNumber, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        inputPanel.add(textEmployeeNumber, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        inputPanel.add(buttonCalculate, gbc);

       
        // Result Area
        JTextArea textAreaResult = new JTextArea();
        textAreaResult.setEditable(false);
        textAreaResult.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textAreaResult);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Result"));

        // Employee Data Table
        String[] columnNames = {"Employee Number", "Last Name", "First Name", "SSS No.", "PhilHealth No.", "TIN", "Pagibig No."};
        tableModel = new DefaultTableModel(columnNames, 0);
        JTable employeeTable = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Disable editing of all cells
            }
        };
        JScrollPane tableScrollPane = new JScrollPane(employeeTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Employee Data"));

        // Populate the table with employee data
        for (String[] employee : employeeData) {
            // Ensure each employee array has at least 8 elements before adding to tableModel
            if (employee.length >= 18) {
                tableModel.addRow(new Object[]{employee[0], employee[1], employee[2], employee[6], employee[7], employee[8], employee[9]});
            } else {
                LOGGER.warning("Employee data in internal storage is incomplete or malformed: " + String.join(",", employee));
            }
        }

        // Add input panel, result area, and employee table to main panel
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(tableScrollPane, BorderLayout.SOUTH);

  // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton buttonViewEmployee = new JButton("View Employee");
        JButton buttonUpdateEmployee = new JButton("Update Employee");
        JButton buttonDeleteEmployee = new JButton("Delete Employee");

        buttonPanel.add(buttonViewEmployee);
        buttonPanel.add(buttonUpdateEmployee);
        buttonPanel.add(buttonDeleteEmployee);

        mainPanel.add(buttonPanel, BorderLayout.EAST);

    
        // Add main panel to frame
        frame.getContentPane().add(mainPanel);
        frame.setVisible(true);

        // Action listener for calculate button
        buttonCalculate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String employeeNumber = textEmployeeNumber.getText().trim();
                    if (employeeNumber.isEmpty()) {
                        throw new IllegalArgumentException("Employee number cannot be empty.");
                    }

                    String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
                    String selectedMonth = (String) JOptionPane.showInputDialog(frame, "Select month:", "Month Selection", JOptionPane.QUESTION_MESSAGE, null, months, months[0]);
                    if (selectedMonth == null || selectedMonth.isEmpty()) {
                        throw new IllegalArgumentException("Month cannot be empty.");
                    }

                    String hoursInput = JOptionPane.showInputDialog(frame, "Enter the number of hours worked in " + selectedMonth + ":", "Enter Hours Worked", JOptionPane.QUESTION_MESSAGE);
                    if (hoursInput == null || hoursInput.isEmpty()) {
                        throw new IllegalArgumentException("Number of hours worked cannot be empty.");
                    }

                    int hoursWorked = Integer.parseInt(hoursInput.trim());
                    if (hoursWorked <= 0) {
                        throw new IllegalArgumentException("Hours worked must be greater than zero.");
                    }

                    String result = calculateAndDisplaySalary(employeeNumber, hoursWorked);
                    textAreaResult.setText(result);

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid input for hours worked. Please enter a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Action listener for view employee button
        buttonViewEmployee.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = employeeTable.getSelectedRow();
                if (selectedRow != -1) {
                    String[] employee = new String[employeeTable.getColumnCount()];
                    for (int i = 0; i < employeeTable.getColumnCount(); i++) {
                        employee[i] = employeeTable.getValueAt(selectedRow, i).toString();
                    }
                    showEmployeeDetails(employee);
                } else {
                    JOptionPane.showMessageDialog(frame, "Please select an employee from the table.", "No Selection", JOptionPane.WARNING_MESSAGE);
                }
            }
        });


        // Action listener for update employee button
        buttonUpdateEmployee.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = employeeTable.getSelectedRow();
                if (selectedRow != -1) {
                    // Assuming you have an update method to modify employee data
                    String[] employee = new String[employeeTable.getColumnCount()];
                    for (int i = 0; i < employeeTable.getColumnCount(); i++) {
                        employee[i] = employeeTable.getValueAt(selectedRow, i).toString();
                    }
                    createUpdateEmployeeFrame(employee); // Open update frame with selected employee details
                } else {
                    JOptionPane.showMessageDialog(frame, "Please select an employee from the table.", "No Selection", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

buttonDeleteEmployee.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = employeeTable.getSelectedRow();
                if (selectedRow != -1) {
                    // Remove the selected row from the table model and employeeData list
                    tableModel.removeRow(selectedRow);
                    employeeData.remove(selectedRow);
                } else {
                    JOptionPane.showMessageDialog(frame, "Please select an employee from the table.", "No Selection", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
}

    private static String calculateAndDisplaySalary(String employeeNumber, int hoursWorked) {
        for (String[] employee : employeeData) {
            if (employee[0].equals(employeeNumber)) {
                try {
                    double hourlyRate = Double.parseDouble(employee[18]); // Assuming hourly rate is at index 6
                    double grossSalary = hoursWorked * hourlyRate;
                    double sssContribution = grossSalary * 0.045; // Example contribution rates
                    double philhealthContribution = grossSalary * 0.035;
                    double pagibigContribution = grossSalary * 0.02;
                    double netSalary = grossSalary - (sssContribution + philhealthContribution + pagibigContribution);

                    return "Employee Number: " + employeeNumber + "\n" +
                            "Hours Worked  : " + hoursWorked + "\n" +
                            "Hourly Rate   : P" + String.format("%.2f", hourlyRate) + "\n" +
                            "Gross Salary  : P" + String.format("%.2f", grossSalary) + "\n" +
                            "SSS           : P" + String.format("%.2f", sssContribution) + "\n" +
                            "PhilHeath     : P" + String.format("%.2f", philhealthContribution) + "\n" +
                            "Pag-IBIG      : P" + String.format("%.2f", pagibigContribution) + "\n" +
                            "Net Salary    : P" + String.format("%.2f", netSalary);

                } catch (NumberFormatException e) {
                    LOGGER.log(Level.SEVERE, "Invalid hourly rate format for employee", e);
                    return "Invalid hourly rate format for employee.";
                }
            }
        }
        return "Employee not found.";
    }

    private static void showEmployeeDetails(String[] employee) {
        JFrame detailsFrame = new JFrame("Employee Details");
        detailsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        detailsFrame.setSize(400, 300);
        detailsFrame.setLocationRelativeTo(null); // Center the frame on the screen

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Employee Info Panel
        JPanel employeeInfoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 10, 5, 10);

        String[] labels = {"Employee Number:", "Last Name:", "First Name:", "SSS No.:", "PhilHealth No.:", "TIN:", "Pagibig No."};

        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i]);
            String value = (i < employee.length) ? employee[i] : "";
            JTextField textField = new JTextField(value);
            textField.setEditable(false);

            employeeInfoPanel.add(label, gbc);
            gbc.gridx++;
            employeeInfoPanel.add(textField, gbc);
            gbc.gridx = 0;
            gbc.gridy++;
        }

        mainPanel.add(employeeInfoPanel, BorderLayout.CENTER);

        // Month selection
        JPanel monthPanel = new JPanel(new BorderLayout());
        JLabel labelMonth = new JLabel("Select Month:");
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        JComboBox<String> comboBoxMonth = new JComboBox<>(months);
        monthPanel.add(labelMonth, BorderLayout.WEST);
        monthPanel.add(comboBoxMonth, BorderLayout.CENTER);

        mainPanel.add(monthPanel, BorderLayout.NORTH);

        // Calculate button
        JButton buttonCalculate = new JButton("Calculate Salary");
        buttonCalculate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedMonth = (String) comboBoxMonth.getSelectedItem();

                String hoursInput = JOptionPane.showInputDialog(detailsFrame, "Enter the number of hours worked in " + selectedMonth + ":", "Enter Hours Worked", JOptionPane.QUESTION_MESSAGE);
                if (hoursInput == null || hoursInput.isEmpty()) {
                    JOptionPane.showMessageDialog(detailsFrame, "Number of hours worked cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    int hoursWorked = Integer.parseInt(hoursInput.trim());
                    if (hoursWorked <= 0) {
                        JOptionPane.showMessageDialog(detailsFrame, "Hours worked must be greater than zero.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String result = calculateAndDisplaySalary(employee[0], hoursWorked);
                    JOptionPane.showMessageDialog(detailsFrame, result, "Salary Calculation", JOptionPane.INFORMATION_MESSAGE);

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(detailsFrame, "Invalid input for hours worked. Please enter a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        mainPanel.add(buttonCalculate, BorderLayout.SOUTH);

        detailsFrame.getContentPane().add(mainPanel);
        detailsFrame.setVisible(true);
    }

    private static void createUpdateEmployeeFrame(String[] employee) {
        JFrame updateFrame = new JFrame("Update Employee Details");
        updateFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        updateFrame.setSize(400, 300);
        updateFrame.setLocationRelativeTo(null); // Center the frame on the screen

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Employee Info Panel
        JPanel employeeInfoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 10, 5, 10);

        String[] labels = {"Employee Number:", "Last Name:", "First Name:", "SSS No.:", "PhilHealth No.:", "TIN:", "Pagibig No."};
        JTextField[] textFields = new JTextField[labels.length];

        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i]);
            String value = (i < employee.length) ? employee[i] : "";
            JTextField textField = new JTextField(value);
            textFields[i] = textField;

            gbc.gridx = 0;
            employeeInfoPanel.add(label, gbc);
            gbc.gridx++;
            employeeInfoPanel.add(textField, gbc);
            gbc.gridy++;
        }

        mainPanel.add(employeeInfoPanel, BorderLayout.CENTER);

        // Save button
        JButton buttonSave = new JButton("Save");
        buttonSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Update employee details in the main data structure
                for (int i = 0; i < textFields.length; i++) {
                    employee[i] = textFields[i].getText();
                }
                updateEmployeeDetails(employee); // Update the employee details
                refreshTable(); // Refresh the table after updating
                updateFrame.dispose(); // Close the update frame
            }
        });

        mainPanel.add(buttonSave, BorderLayout.SOUTH);

        updateFrame.getContentPane().add(mainPanel);
        updateFrame.setVisible(true);
    }

    // Method to update employee details (to be implemented)
    private static void updateEmployeeDetails(String[] employee) {
        // Implement your update logic here
        // For example, you can modify the employee data in `employeeData` ArayList
        for (String[] emp : employeeData) {
            if (emp[0].equals(employee[0])) { // Assuming employee[0] is Employee Number
                // Update the employee details as needed
                emp[1] = employee[1]; // Update Last Name
                emp[2] = employee[2]; // Update First Name
                emp[6] = employee[3]; // Update SSS No.
                emp[7] = employee[4]; // Update PhilHealth No.
                emp[8] = employee[5]; // Update TIN
                emp[9] = employee[6]; // Update Pagibig No.
                break; // Exit loop once updated
            }
        }
    }

    // Method to refresh the table after updating employee details
    private static void refreshTable() {
        tableModel.setRowCount(0); // Clear existing rows from table
        for (String[] employee : employeeData) {
            tableModel.addRow(new Object[]{employee[0], employee[1], employee[2], employee[6], employee[7], employee[8], employee[9]});
        }
    }
}
