package org.example;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileConverterGUI extends JFrame {
    private static final Color BACKGROUND_COLOR = new Color(45, 45, 45);
    private static final Color PANEL_COLOR = new Color(60, 63, 65);
    private static final Color BUTTON_COLOR = new Color(70, 130, 180);
    private static final Color CANCEL_BUTTON_COLOR = new Color(220, 20, 60);
    private static final Color TEXT_COLOR = Color.WHITE;

    private JTextField fileTextField;
    private JComboBox<String> conversionTypeComboBox;
    private JProgressBar progressBar;
    private JTextArea statusTextArea;
    private JButton startButton, cancelButton;
    private JFileChooser fileChooser;
    private ExecutorService executorService;
    private SwingWorker<Void, ConversionTask> currentWorker;
    private JPanel resultPanel;
    private DefaultListModel<File> fileListModel;

    public FileConverterGUI() {
        setTitle("File Converter");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);

        fileTextField = createStyledTextField();
        JButton selectFileButton = createStyledButton("Select Files", BUTTON_COLOR);
        conversionTypeComboBox = createStyledComboBox(new String[]{"PDF to DOCX", "Image Resize"});
        progressBar = createStyledProgressBar();
        statusTextArea = createStyledTextArea();
        startButton = createStyledButton("Start", BUTTON_COLOR);
        cancelButton = createStyledButton("Cancel", CANCEL_BUTTON_COLOR);
        cancelButton.setEnabled(false);
        fileChooser = createFileChooser();

        resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBackground(PANEL_COLOR);

        fileListModel = new DefaultListModel<>();
        JList<File> fileList = createStyledFileList();

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BACKGROUND_COLOR);
        topPanel.add(new JScrollPane(fileList), BorderLayout.CENTER);
        topPanel.add(selectFileButton, BorderLayout.SOUTH);

        JPanel middlePanel = new JPanel(new BorderLayout());
        middlePanel.setBackground(BACKGROUND_COLOR);
        middlePanel.add(conversionTypeComboBox, BorderLayout.NORTH);
        middlePanel.add(progressBar, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(BACKGROUND_COLOR);
        bottomPanel.add(new JScrollPane(statusTextArea), BorderLayout.CENTER);
        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(BACKGROUND_COLOR);
        controlPanel.add(startButton);
        controlPanel.add(cancelButton);
        bottomPanel.add(controlPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(middlePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        add(new JScrollPane(resultPanel), BorderLayout.EAST);

        selectFileButton.addActionListener(e -> selectFiles());
        startButton.addActionListener(e -> startConversion());
        cancelButton.addActionListener(e -> cancelConversion());

        executorService = Executors.newFixedThreadPool(4);
    }

    private JTextField createStyledTextField() {
        JTextField textField = new JTextField();
        textField.setEditable(false);
        textField.setBackground(PANEL_COLOR);
        textField.setForeground(TEXT_COLOR);
        textField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        return textField;
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setBackground(PANEL_COLOR);
        comboBox.setForeground(TEXT_COLOR);
        comboBox.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        return comboBox;
    }

    private JProgressBar createStyledProgressBar() {
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(100, 149, 237));
        progressBar.setBackground(PANEL_COLOR);
        progressBar.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        return progressBar;
    }

    private JTextArea createStyledTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setBackground(PANEL_COLOR);
        textArea.setForeground(TEXT_COLOR);
        textArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        return textArea;
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        button.setFont(new Font("Arial", Font.BOLD, 14));
        return button;
    }

    private JFileChooser createFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(new FileNameExtensionFilter("Files", "pdf", "jpg", "png"));
        return chooser;
    }

    private JList<File> createStyledFileList() {
        JList<File> fileList = new JList<>(fileListModel);
        fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        fileList.setBackground(PANEL_COLOR);
        fileList.setForeground(TEXT_COLOR);
        fileList.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        return fileList;
    }

    private void selectFiles() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            fileListModel.clear();
            for (File file : selectedFiles) {
                fileListModel.addElement(file);
            }
        }
    }

    private void startConversion() {
        if (fileListModel.getSize() == 0) {
            JOptionPane.showMessageDialog(this, "No files selected!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File[] selectedFiles = new File[fileListModel.getSize()];
        for (int i = 0; i < fileListModel.getSize(); i++) {
            selectedFiles[i] = fileListModel.get(i);
        }

        String conversionType = (String) conversionTypeComboBox.getSelectedItem();
        progressBar.setValue(0);
        statusTextArea.setText("");
        startButton.setEnabled(false);
        cancelButton.setEnabled(true);

        resultPanel.removeAll();

        currentWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                int totalFiles = selectedFiles.length;
                for (int i = 0; i < totalFiles && !isCancelled(); i++) {
                    File file = selectedFiles[i];
                    ConversionTask task = new ConversionTask(file, conversionType);
                    executorService.submit(task);
                    publish(task);

                    try {
                        task.get();
                    } catch (Exception e) {
                        publish(new ConversionTask(file, conversionType, e));
                    }

                    int progress = (int) ((i + 1) / (float) totalFiles * 100);
                    setProgress(progress);
                }
                return null;
            }

            @Override
            protected void process(List<ConversionTask> chunks) {
                for (ConversionTask task : chunks) {
                    if (task.getError() == null) {
                        statusTextArea.append("Converted: " + task.getFile().getName() + " (" + task.getType() + ")\n");
                        addResult(task.getFile());
                    } else {
                        statusTextArea.append("Failed: " + task.getFile().getName() + " (" + task.getType() + ") - " + task.getError().getMessage() + "\n");
                    }
                }
            }

            @Override
            protected void done() {
                startButton.setEnabled(true);
                cancelButton.setEnabled(false);
                if (!isCancelled()) {
                    JOptionPane.showMessageDialog(FileConverterGUI.this, "Conversion completed!", "Info", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(FileConverterGUI.this, "Conversion cancelled!", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        };

        currentWorker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                progressBar.setValue((Integer) evt.getNewValue());
            }
        });

        currentWorker.execute();
    }

    private void cancelConversion() {
        if (currentWorker != null) {
            currentWorker.cancel(true);
        }
    }

    private void addResult(File file) {
        JLabel resultLabel = new JLabel("Converted File: " + file.getName());
        resultLabel.setForeground(TEXT_COLOR);
        resultPanel.add(resultLabel);
        resultPanel.revalidate();
        resultPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FileConverterGUI converterGUI = new FileConverterGUI();
            converterGUI.setVisible(true);
        });
    }

    private static class ConversionTask implements Runnable {
        private final File file;
        private final String type;
        private Exception error;

        public ConversionTask(File file, String type) {
            this.file = file;
            this.type = type;
        }

        public ConversionTask(File file, String type, Exception error) {
            this.file = file;
            this.type = type;
            this.error = error;
        }

        public File getFile() {
            return file;
        }

        public String getType() {
            return type;
        }

        public Exception getError() {
            return error;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(2000);
                // Implement actual conversion logic here
            } catch (InterruptedException e) {
                error = e;
            }
        }

        public void get() {
        }
    }
}
