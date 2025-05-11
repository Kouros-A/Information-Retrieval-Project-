package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class SimpleIndexerGUI extends JFrame implements ActionListener {
    private JTextField inputFolderField;
    private JTextField outputFolderField;
    private JButton browseInputButton;
    private JButton browseOutputButton;
    private JButton startButton;

    public SimpleIndexerGUI() {
        setTitle("Simple Indexer");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        JLabel inputLabel = new JLabel("Input Folder:");
        inputFolderField = new JTextField();
        browseInputButton = new JButton("Browse");
        browseInputButton.addActionListener(this);

        startButton = new JButton("Start Indexing");
        startButton.addActionListener(this);

        panel.add(inputLabel);
        panel.add(inputFolderField);
        panel.add(browseInputButton);

        add(panel, BorderLayout.CENTER);
        add(startButton, BorderLayout.SOUTH);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == browseInputButton) {
            JFileChooser inputChooser = new JFileChooser();
            inputChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = inputChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = inputChooser.getSelectedFile();
                inputFolderField.setText(selectedFile.getAbsolutePath());
            }
        } else if (e.getSource() == browseOutputButton) {
            JFileChooser outputChooser = new JFileChooser();
            outputChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = outputChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = outputChooser.getSelectedFile();
                outputFolderField.setText(selectedFile.getAbsolutePath());
            }
        } else if (e.getSource() == startButton) {

            String inputFolder = inputFolderField.getText();
            long startTime = System.currentTimeMillis();
            File folder = new File(inputFolder);
            NXMLIndexer.listFilesForFolder(folder);
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            System.out.println("Execution time for parsing all files of directory"+ folder.getAbsolutePath()+" : " + executionTime + " milliseconds");
            try {
                NXMLIndexer.createDocumentsFile();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            endTime = System.currentTimeMillis();
            executionTime = endTime - startTime;
            System.out.println("Execution time for creating the Documents File for directory"+ inputFolder+" : " + executionTime+ " milliseconds");
            startTime = System.currentTimeMillis();
            NXMLIndexer.createPostingFile();
            endTime = System.currentTimeMillis();
            executionTime = endTime - startTime;
            System.out.println("Execution time for creating the Posting File for directory"+ inputFolder+" : " + executionTime+ " milliseconds");
            startTime = System.currentTimeMillis();
            NXMLIndexer.createVocabularyFile();
            endTime = System.currentTimeMillis();
            executionTime = endTime - startTime;
            System.out.println("Execution time for creating the Vocabulary File for directory"+ inputFolder+" : " + executionTime + " milliseconds");
        }
    }

}
