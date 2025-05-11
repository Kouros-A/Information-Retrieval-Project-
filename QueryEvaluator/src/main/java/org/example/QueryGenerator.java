package org.example;
import gr.uoc.csd.hy463.Topic;
import gr.uoc.csd.hy463.TopicsReader;
import mitos.stemmer.Stemmer;

import javax.print.Doc;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QueryGenerator {
    static Map<Integer, Map<String, Integer>> topicDocumentMap = new HashMap<>();

    static HashMap<Integer,String> docHash = new HashMap<>();


    public static Map<Integer, Map<String, Integer>> parseTSV(String filePath) {
        Map<Integer, Map<String, Integer>> topicDocumentMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length == 4) {
                    int topicNumber = Integer.parseInt(parts[0]);
                    String documentPMCID = parts[2];
                    int relevanceScore = Integer.parseInt(parts[3]);

                    // Create or retrieve the map for the current topic
                    Map<String, Integer> documentRelevanceMap = topicDocumentMap.computeIfAbsent(topicNumber, k -> new HashMap<>());
                    // Store the relevance score for the document
                    documentRelevanceMap.put(documentPMCID, relevanceScore);
                } else {
                    System.err.println("Invalid line: " + line);
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return topicDocumentMap;
    }

    public static HashMap<String,Integer> generate(String type,String summary) throws Exception {
        StopwordFilter filter = new StopwordFilter();
        HashMap<String,Integer> query=new HashMap<>();
        DocumentExtractor.parseDocumentsFile("src/main/resources/CollectionIndex/DocumentsFile.txt");
        String[] summaryParts = summary.split("\\s+");
        for ( int i=0;i<summaryParts.length;i++) {
            summaryParts[i] = summaryParts[i].toLowerCase();
            summaryParts[i] = filter.filterStopwords(summaryParts[i]);
            summaryParts[i] = Stemmer.Stem(summaryParts[i]);
            DocumentExtractor.parseVocabularyFile(summaryParts[i], "src/main/resources/CollectionIndex/VocabularyFile.txt");
        }

        ArrayList<Topic> topics = TopicsReader.readTopics("src/main/resources/topics.xml");
        topicDocumentMap = parseTSV("src/main/resources/qrels.txt");
        for (Topic topic:topics ) {
            String s = String.valueOf(topic.getType());
            Integer i = topic.getNumber();
            String sum = topic.getSummary();
            String desc = topic.getDescription();
            if ( String.valueOf(topic.getType()).equals(type.toUpperCase()) ){
                topicDocumentMap.entrySet().forEach(entry->{
                    entry.getValue().entrySet().forEach(docId->{
                        if ( entry.getKey().equals(i)&&docId.getValue()>0) {
                            DocumentExtractor.tokenTagDf.entrySet().forEach(token->{
                                token.getValue().entrySet().forEach(docPath->{
                                    if ( DocumentExtractor.extractNumberFromPath(docPath.getKey()).equals(docId.getKey())) {
                                        query.put(docPath.getKey(), docId.getValue());
                                        if (!KeywordSearchGUI.genereatedQuery.contains(sum))KeywordSearchGUI.genereatedQuery.add(sum);
                                    }
                                });
                            });

                        }
                    });
                });
            }
        }
        return query;
    }

}
