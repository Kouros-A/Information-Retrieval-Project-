package org.example;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocumentExtractor {

    static HashMap <Integer,Map<String,Double>> term_doc_idfs = new HashMap<>();

    static TreeMap<Integer,ArrayList<String>> docIds = new TreeMap<>();

    static HashMap<String,HashMap<String,ArrayList<HashMap<String,Integer>>>> tokenTagDf = new HashMap<>();

    static HashMap<Integer,String> docPmcIdhash = new HashMap<>();


    public static String extractNumberFromPath(String filePath) {


        // Define a regular expression pattern to match the last number before ".nxml" extension
        Pattern pattern = Pattern.compile("(\\d+)\\.nxml$");
        Matcher matcher = pattern.matcher(filePath);

        // Find the last matching number
        if (matcher.find()) {
            // Extract the matched number
            String numStr = matcher.group(1);
            // Convert the string number to an integer
            return numStr;
        } else {
            // If no match found, return a default value or handle the case accordingly
            return " "; // Or throw an exception, return 0, etc.
        }
    }
    public static void parseDocumentsFile(String filepath){

        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = br.readLine())!=null){
                line = line.trim();
                String [] parts = line.trim().split("\\s+");
                String docPath = parts[1];
                docPmcIdhash.put(Integer.parseInt(parts[0]),extractNumberFromPath(docPath));
            }

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        }

    public static void parsePostingsFile(String queryWord,String filePath) {

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim(); // Remove leading and trailing whitespace
                if (!line.isEmpty()&&line.equals(queryWord)) {
                    while ((line = br.readLine()) != null) {
                        //
                        if (line.charAt(0) == ' ') { // Check if the line starts with a space
                            String[] parts = line.trim().split("\\s+");
                            int docId = Integer.parseInt(parts[0].substring(2)); // Extract document ID
                            double tfidf = Double.parseDouble(parts[1].substring(6)); // Extract term frequency
                            Map<String,Double> tmp = new HashMap<>();
                            ArrayList<String> tmp3 = new ArrayList<>();
                            tmp3.add(queryWord);
                            tmp.put(queryWord,tfidf);
                            if (docIds.get(docId)==null) docIds.put(docId,tmp3);
                            else docIds.get(docId).add(queryWord);
                            if ( term_doc_idfs.get(docId)==null) term_doc_idfs.put(Integer.valueOf(docPmcIdhash.get(docId)),tmp);
                            else term_doc_idfs.get(Integer.valueOf(docPmcIdhash.get(docId))).put(queryWord,tfidf);

                        }else break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void parseVocabularyFile(String queryWord,String filePath){
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(queryWord)) {
                    ArrayList<HashMap<String,Integer>> tagsCounts = new ArrayList<>();
                    HashMap<String,ArrayList<HashMap<String,Integer>>> pathTagsCounts = new HashMap<>();
                    HashMap<String,Integer> tagCount = new HashMap<>();
                    String fileInfo;
                    fileInfo = reader.readLine();
                    String [] parts = fileInfo.trim().split("\\s+");
                    String path,tag;
                    int df;
                    for ( int i=0;i<parts.length;i++){
                        if ( parts[i].startsWith("C")) {
                            tagsCounts.clear();
                            path = parts[i];
                            i++;
                            tag=parts[i];
                            i++;
                            df = Integer.parseInt(parts[i].substring(3));
                            HashMap<String,Integer > tmp = new HashMap<>();
                            ArrayList<HashMap<String,Integer >> tmpA = new ArrayList<>();
                            tmp.put(tag,df);
                            tmpA.add(tmp);
                            pathTagsCounts.put(path,tmpA);
                            i++;
                            while ( !parts[i].startsWith("C")&&i<parts.length){
                                if (parts[i].startsWith("Position")) {
                                    tokenTagDf.put(queryWord,pathTagsCounts);
                                    break;
                                }
                                tag=parts[i];
                                i++;
                                df = Integer.parseInt(parts[i].substring(3));
                                i++;
                                tmp.put(tag,df);
                            }
                            if ( parts[i].startsWith("C")) i--;
                        }
                    }
                    return;
                    }
                }
            } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
