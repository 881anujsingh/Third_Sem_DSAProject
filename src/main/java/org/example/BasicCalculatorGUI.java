package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;

public class BasicCalculatorGUI extends JFrame {

    private JTextField inputField;
    private JButton[] buttons;
    private JLabel resultLabel;

    public BasicCalculatorGUI() {
        // Set up the frame
        setTitle("Colorful Calculator");
        setSize(350, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Input field
        inputField = new JTextField();
        inputField.setFont(new Font("Arial", Font.PLAIN, 24));
        inputField.setHorizontalAlignment(JTextField.RIGHT);
        inputField.setEditable(false);
        inputField.setBackground(new Color(224, 224, 224)); // Light gray background
        inputField.setForeground(Color.BLACK); // Black text
        add(inputField, BorderLayout.NORTH);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(5, 4, 5, 5));
        buttonPanel.setBackground(new Color(240, 240, 240)); // Slightly darker gray background
        buttons = new JButton[20];

        // Button labels
        String[] buttonLabels = {
                "7", "8", "9", "/",
                "4", "5", "6", "*",
                "1", "2", "3", "-",
                "0", "(", ")", "+",
                "C", "=", ".", " "
        };

        // Add buttons to panel with color decorations
        for (int i = 0; i < 20; i++) {
            buttons[i] = new JButton(buttonLabels[i]);
            buttons[i].setFont(new Font("Arial", Font.BOLD, 18));
            buttons[i].setFocusPainted(false);

            // Set custom colors for different buttons
            if (buttonLabels[i].matches("[0-9]")) {
                buttons[i].setBackground(new Color(173, 216, 230)); // Light blue for numbers
                buttons[i].setForeground(Color.BLACK);
            } else if (buttonLabels[i].matches("[+\\-*/()]")) {
                buttons[i].setBackground(new Color(255, 160, 122)); // Light coral for operators
                buttons[i].setForeground(Color.WHITE);
            } else if (buttonLabels[i].equals("C")) {
                buttons[i].setBackground(new Color(255, 69, 0)); // Red for clear
                buttons[i].setForeground(Color.WHITE);
            } else if (buttonLabels[i].equals("=")) {
                buttons[i].setBackground(new Color(60, 179, 113)); // Medium sea green for equals
                buttons[i].setForeground(Color.WHITE);
            } else if (buttonLabels[i].equals(".")) {
                buttons[i].setBackground(new Color(255, 215, 0)); // Gold for decimal point
                buttons[i].setForeground(Color.BLACK);
            }

            buttons[i].addActionListener(new ButtonClickListener());
            buttonPanel.add(buttons[i]);
        }

        // Add button panel to center
        add(buttonPanel, BorderLayout.CENTER);

        // Result label
        resultLabel = new JLabel("Result: ");
        resultLabel.setFont(new Font("Arial", Font.BOLD, 24));
        resultLabel.setHorizontalAlignment(JLabel.RIGHT);
        resultLabel.setBackground(new Color(224, 224, 224)); // Light gray background
        resultLabel.setOpaque(true);
        add(resultLabel, BorderLayout.SOUTH);

        // Make the frame visible
        setVisible(true);
    }

    private class ButtonClickListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JButton button = (JButton) e.getSource();
            String buttonText = button.getText();

            if (buttonText.equals("=")) {
                String expression = inputField.getText();
                try {
                    double result = evaluate(expression);
                    resultLabel.setText("Result: " + result);
                } catch (Exception ex) {
                    resultLabel.setText("Error: " + ex.getMessage());
                }
            } else if (buttonText.equals("C")) {
                inputField.setText("");
                resultLabel.setText("Result: ");
            } else {
                inputField.setText(inputField.getText() + buttonText);
            }
        }
    }

    private double evaluate(String expression) {
        Stack<Double> values = new Stack<>();
        Stack<Character> operators = new Stack<>();

        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);
            if (ch == ' ') continue;

            // Handle number parsing
            if (ch >= '0' && ch <= '9' || ch == '.') {
                StringBuilder sb = new StringBuilder();
                while (i < expression.length() && (expression.charAt(i) >= '0' && expression.charAt(i) <= '9' || expression.charAt(i) == '.')) {
                    sb.append(expression.charAt(i++));
                }
                values.push(Double.parseDouble(sb.toString()));
                i--;
            }
            // Handle parentheses
            else if (ch == '(') {
                operators.push(ch);
            } else if (ch == ')') {
                while (operators.peek() != '(') {
                    values.push(applyOp(operators.pop(), values.pop(), values.pop()));
                }
                operators.pop();
            }
            // Handle operators
            else if (ch == '+' || ch == '-' || ch == '*' || ch == '/') {
                while (!operators.isEmpty() && hasPrecedence(ch, operators.peek())) {
                    values.push(applyOp(operators.pop(), values.pop(), values.pop()));
                }
                operators.push(ch);
            }
        }

        while (!operators.isEmpty()) {
            values.push(applyOp(operators.pop(), values.pop(), values.pop()));
        }

        return values.pop();
    }

    private boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')') return false;
        if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-')) return false;
        return true;
    }

    private double applyOp(char op, double b, double a) {
        switch (op) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0) throw new ArithmeticException("Cannot divide by zero");
                return a / b;
            default:
                throw new UnsupportedOperationException("Unknown operator: " + op);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BasicCalculatorGUI::new);
    }
}
