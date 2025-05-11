package org.example;

import mitos.stemmer.Stemmer;

import javax.print.Doc;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class KeywordSearchGUI extends JFrame implements ActionListener {
    private final JTextField keywordField;
    private final JTextArea resultArea;
    static TreeMap<String,Double> cosineSimilarity= new TreeMap<>();
    private JTextField typeField;
    static ArrayList<String> genereatedQuery = new ArrayList<>();

    public KeywordSearchGUI() {
        setTitle("Query Search");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Panel for keyword and type input
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        JLabel keywordLabel = new JLabel("Query:");
        keywordField = new JTextField(20);
        JLabel typeLabel = new JLabel("Type:");
        typeField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(this);

        inputPanel.add(keywordLabel);
        inputPanel.add(keywordField);
        inputPanel.add(typeLabel);
        inputPanel.add(typeField);

        // Panel for the search button
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(searchButton);

        // Panel for displaying search result
        JPanel resultPanel = new JPanel();
        resultArea = new JTextArea(10, 30);
        resultArea.setEditable(false); // Make it read-only
        JScrollPane scrollPane = new JScrollPane(resultArea);

        resultPanel.add(scrollPane);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER); // Add buttonPanel to the center
        panel.add(resultPanel, BorderLayout.SOUTH); // Move resultPanel to the south

        add(panel);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Search")) {
            String keyword = keywordField.getText().trim();
            String type = typeField.getText().trim();
            if (!keyword.isEmpty() && !type.isEmpty()) {
                try {
                    search(keyword, type);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please enter both keyword and type.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void search(String query,String type) throws Exception {
        StopwordFilter filter = new StopwordFilter();
        String [] terms ;
        Map<String, Integer> termFrequency = new HashMap<>();
        Map<String, Double> queryVector = new HashMap<>();
        boolean flag = false;

        HashMap<String,Integer> docs = QueryGenerator.generate(type,query);
        if ( docs.isEmpty()) flag = true;
        Iterator it = genereatedQuery.iterator();
        while ( it.hasNext()){
            terms = String.valueOf(it.next()).split("\\s+");
            for (int i=0;i<terms.length;i++){
                terms[i] = terms[i].toLowerCase();
                terms[i] = filter.filterStopwords(terms[i]);
                terms[i] = Stemmer.Stem(terms[i]);
                DocumentExtractor.parsePostingsFile(terms[i],"src/main/resources/CollectionIndex/PostingFile.txt");
                termFrequency.put(terms[i], termFrequency.getOrDefault(terms[i], 0) + 1);
                double idf = Math.log((double) terms.length /termFrequency.get(terms[i]));
                double tfidf = (double) termFrequency.get(terms[i]) * idf;
                queryVector.put(terms[i], tfidf);
            }
        }


        docs.entrySet().forEach(doc->{
            Map<String,Double> tfidVec = new HashMap<>();
            tfidVec = DocumentExtractor.term_doc_idfs.get(Integer.valueOf(DocumentExtractor.extractNumberFromPath(doc.getKey())));
            int relevance = doc.getValue();
            cosineSimilarity.put(doc.getKey(),QueryEvaluator.computeCosineSimilarity(queryVector,tfidVec,relevance));
        });

        if (!flag){
            SwingUtilities.invokeLater(() -> {
                CosineSimilarityGUI gui = new CosineSimilarityGUI(cosineSimilarity);
                gui.setVisible(true);
                cosineSimilarity.clear();
                DocumentExtractor.docIds.clear();
                queryVector.clear();
                DocumentExtractor.term_doc_idfs.clear();
                QueryGenerator.topicDocumentMap.clear();
                QueryGenerator.docHash.clear();
                DocumentExtractor.tokenTagDf.clear();
                genereatedQuery.clear();
            });
        }else {
        resultArea.setText("No relevant articles found for this particular query");
    }

}
    public static ArrayList<String> loadStopwords(String filename1, String filename2) {
        ArrayList<String> stopwords = new ArrayList<>();
        try {
            File file1 = new File(filename1);
            File file2 = new File(filename2);
            InputStream inputStream1 = new FileInputStream(file1);
            java.util.Scanner scanner1 = new java.util.Scanner(inputStream1);
            InputStream inputStream2 = new FileInputStream(file2);
            java.util.Scanner scanner2 = new java.util.Scanner(inputStream2);
            while (scanner1.hasNextLine()) {
                stopwords.add(scanner1.nextLine().trim().toLowerCase());
            }
            while (scanner2.hasNextLine()) {
                stopwords.add(scanner2.nextLine().trim().toLowerCase());
            }
            scanner1.close();
            scanner2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stopwords;
    }
}
class StopwordFilter {

    static ArrayList<String> stopwords = KeywordSearchGUI.loadStopwords("src/main/resources/stopwords/stopwordsEn.txt","src/main/resources/stopwords/stopwordsGr.txt");

    public String filterStopwords(String text) {
        StringBuilder filteredText = new StringBuilder();

        String[] words = text.split("\\s+");
        for (String word : words) {
            if (!stopwords.contains(word.toLowerCase())) {
                filteredText.append(word).append(" ");
            }
        }
        return filteredText.toString().trim();
    }
}
