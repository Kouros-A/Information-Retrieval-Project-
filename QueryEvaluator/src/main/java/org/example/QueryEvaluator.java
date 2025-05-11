package org.example;

import javax.print.Doc;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static org.example.DocumentExtractor.docIds;

public class QueryEvaluator {


    // Compute cosine similarity between two TF-IDF vectors
    public static double computeCosineSimilarity(Map<String, Double> vector1, Map<String, Double> vector2,int relevance) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String term : vector1.keySet()) {
            dotProduct += vector1.getOrDefault(term, 0.0) * vector2.getOrDefault(term, 0.0);
            norm1 += Math.pow(vector1.getOrDefault(term, 0.0), 2);
            norm2 += Math.pow(vector2.getOrDefault(term, 0.0), 2);
        }

        if (relevance==2)norm1 = 1.2*Math.sqrt(norm1);
        else norm1 = Math.sqrt(norm1);
        norm2 = Math.sqrt(norm2);

        if (norm1 == 0 || norm2 == 0) {
            return 0.0; // Handle zero vector case
        }

        return dotProduct / (norm1 * norm2);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new KeywordSearchGUI().setVisible(true);
        });


    }

}

