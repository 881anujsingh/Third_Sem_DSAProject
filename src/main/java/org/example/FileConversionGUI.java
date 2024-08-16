package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileConversionGUI extends JFrame {

    private JButton selectFilesButton;
    private JComboBox<String> formatComboBox;
    private JButton startButton;
    private JProgressBar progressBar;
    private JTextArea statusTextArea;
    private JButton cancelButton;
    private JList<ImageIcon> resizedImagesList;
    private JList<String> convertedDocsList;

    private DefaultListModel<ImageIcon> resizedImagesModel;
    private DefaultListModel<String> convertedDocsModel;
    private List<File> selectedFiles;
    private ExecutorService executorService;
    private boolean isCancelled = false;

    public FileConversionGUI() {
        setTitle("File Conversion Tool");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeComponents();
        setVisible(true);
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());

        // File selection panel
        JPanel fileSelectionPanel = new JPanel();
        fileSelectionPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        fileSelectionPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        selectFilesButton = createStyledButton("Select Files", new Color(100, 149, 237), 150, 30);
        selectFilesButton.addActionListener(e -> selectFiles());
        fileSelectionPanel.add(selectFilesButton);

        String[] formats = {"PDF to Docx", "Image Resize"};
        formatComboBox = new JComboBox<>(formats);
        formatComboBox.setFont(new Font("Arial", Font.BOLD, 14));
        formatComboBox.setPreferredSize(new Dimension(150, 30));
        fileSelectionPanel.add(formatComboBox);

        startButton = createStyledButton("Start", new Color(46, 204, 113), 100, 30);
        startButton.addActionListener(e -> startConversion());
        fileSelectionPanel.add(startButton);

        cancelButton = createStyledButton("Cancel", new Color(231, 76, 60), 100, 30);
        cancelButton.addActionListener(e -> cancelConversion());
        fileSelectionPanel.add(cancelButton);

        add(fileSelectionPanel, BorderLayout.NORTH);

        // Center panel with progress bar and status text area
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(1150, 30));
        centerPanel.add(progressBar, BorderLayout.NORTH);

        statusTextArea = new JTextArea(10, 30);
        statusTextArea.setFont(new Font("Arial", Font.PLAIN, 14));
        statusTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(statusTextArea);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Resized images list
        resizedImagesModel = new DefaultListModel<>();
        resizedImagesList = new JList<>(resizedImagesModel);
        resizedImagesList.setCellRenderer(new ImageCellRenderer());
        resizedImagesList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = resizedImagesList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        File file = new File("converted_files", resizedImagesModel.getElementAt(index).getDescription());
                        openFile(file);
                    }
                }
            }
        });
        JScrollPane resizedImagesScrollPane = new JScrollPane(resizedImagesList);
        resizedImagesScrollPane.setBorder(BorderFactory.createTitledBorder("Resized Images"));
        resizedImagesScrollPane.setPreferredSize(new Dimension(580, 300));

        // Converted docs list
        convertedDocsModel = new DefaultListModel<>();
        convertedDocsList = new JList<>(convertedDocsModel);
        convertedDocsList.setFont(new Font("Arial", Font.PLAIN, 14));
        convertedDocsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = convertedDocsList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        File file = new File("converted_files", convertedDocsModel.getElementAt(index));
                        openFile(file);
                    }
                }
            }
        });
        JScrollPane convertedDocsScrollPane = new JScrollPane(convertedDocsList);
        convertedDocsScrollPane.setBorder(BorderFactory.createTitledBorder("Converted Documents"));
        convertedDocsScrollPane.setPreferredSize(new Dimension(580, 300));

        // Split pane for resized images and converted docs
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, resizedImagesScrollPane, convertedDocsScrollPane);
        splitPane.setDividerLocation(600); // Initial position of the divider
        add(splitPane, BorderLayout.SOUTH);
    }

    private JButton createStyledButton(String text, Color backgroundColor, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(width, height));
        return button;
    }

    private void selectFiles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home"), "Desktop"));
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new FileNameExtensionFilter("All Files", "*"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFiles = List.of(fileChooser.getSelectedFiles());
            statusTextArea.append("Selected files:\n");
            for (File file : selectedFiles) {
                statusTextArea.append(file.getAbsolutePath() + "\n");
            }
        }
    }

    private void startConversion() {
        if (selectedFiles == null || selectedFiles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select files to convert.");
            return;
        }

        isCancelled = false;
        progressBar.setValue(0);
        statusTextArea.append("Starting conversion...\n");

        String selectedFormat = (String) formatComboBox.getSelectedItem();
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for (File file : selectedFiles) {
            executorService.submit(new FileConversionTask(file, selectedFormat));
        }

        executorService.shutdown();
    }

    private void cancelConversion() {
        isCancelled = true;
        if (executorService != null) {
            executorService.shutdownNow();
        }
        statusTextArea.append("Conversion cancelled.\n");
    }

    private class FileConversionTask extends SwingWorker<Void, String> {
        private final File file;
        private final String format;

        public FileConversionTask(File file, String format) {
            this.file = file;
            this.format = format;
        }

        @Override
        protected Void doInBackground() {
            try {
                if (isCancelled) return null;

                publish("Converting: " + file.getName());
                // Simulate file conversion with sleep
                Thread.sleep(2000);

                // Simulate file conversion and save the converted file
                String convertedFileName = simulateFileConversion(file, format);
                saveConvertedFile(file, convertedFileName);

                publish("Converted: " + convertedFileName);

                // Update progress bar
                int progress = (int) ((double) (selectedFiles.indexOf(file) + 1) / selectedFiles.size() * 100);
                setProgress(progress);
            } catch (InterruptedException | IOException e) {
                publish("Conversion cancelled: " + file.getName());
            }
            return null;
        }

        @Override
        protected void process(List<String> chunks) {
            for (String message : chunks) {
                statusTextArea.append(message + "\n");
                if (message.startsWith("Converted: ")) {
                    String convertedFileName = message.substring(11);
                    if (convertedFileName.endsWith(".docx")) {
                        convertedDocsModel.addElement(convertedFileName);
                    } else if (convertedFileName.endsWith(".jpg") || convertedFileName.endsWith(".png")) {
                        // Assuming images are .jpg or .png
                        ImageIcon icon = new ImageIcon("converted_files/" + convertedFileName);
                        icon.setDescription(convertedFileName);
                        resizedImagesModel.addElement(icon);
                    }
                }
            }
        }

        @Override
        protected void done() {
            setProgress(100);
            statusTextArea.append("All conversions finished.\n");
        }

        private String simulateFileConversion(File file, String format) {
            // This is a simulation, actual conversion logic should be implemented here
            if (format.equals("PDF to Docx")) {
                return file.getName().replace(".pdf", ".docx");
            } else if (format.equals("Image Resize")) {
                // Simulate resizing by appending new dimensions
                int newWidth = 800; // Example width
                int newHeight = 600; // Example height
                return "resized_" + newWidth + "x" + newHeight + "_" + file.getName();
            }
            return file.getName();
        }

        private void saveConvertedFile(File originalFile, String convertedFileName) throws IOException {
            File convertedDir = new File("converted_files");
            if (!convertedDir.exists()) {
                convertedDir.mkdir();
            }
            File convertedFile = new File(convertedDir, convertedFileName);
            Files.copy(originalFile.toPath(), convertedFile.toPath());
        }
    }

    private void openFile(File file) {
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error opening file: " + file.getAbsolutePath());
        }
    }

    private static class ImageCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof ImageIcon) {
                label.setIcon((ImageIcon) value);
                label.setText("");
            } else {
                label.setIcon(null);
                label.setText(value.toString());
            }
            return label;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FileConversionGUI::new);
    }
}
