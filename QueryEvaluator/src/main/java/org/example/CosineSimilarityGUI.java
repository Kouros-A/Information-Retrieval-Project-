package org.example;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class CosineSimilarityGUI extends JFrame {
    private JTable table;
    private DefaultTableModel model;

    public CosineSimilarityGUI( TreeMap<String,Double> similarityResults ) {

        setTitle("Cosine Similarity Results");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        model = new DefaultTableModel();
        table = new JTable(model);

        model.addColumn("Score");
        model.addColumn("Document Path");

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        displayResults(similarityResults);
    }

    private void displayResults(TreeMap<String, Double> similarityResults) {
        Comparator<String> valueComparator = (key1, key2) -> {
            int compare = Double.compare(similarityResults.get(key2), similarityResults.get(key1));
            if (compare == 0) {
                // If values are equal, sort by keys
                return key1.compareTo(key2);
            }
            return compare;
        };

        // Create a new TreeMap with custom comparator
        TreeMap<String, Double> sortedTreeMap = new TreeMap<>(valueComparator);
        sortedTreeMap.putAll(similarityResults);

        for (Map.Entry<String, Double> entry : sortedTreeMap.entrySet()) {
            double similarity = entry.getValue();
            String documentNumber = entry.getKey();
            model.addRow(new Object[]{similarity, documentNumber});
        }
    }

}
