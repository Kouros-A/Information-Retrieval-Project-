package org.example;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import gr.uoc.csd.hy463.NXMLFileReader;
import mitos.stemmer.Stemmer;
import javax.swing.*;

import static org.example.NXMLIndexer.loadStopwords;


public class NXMLIndexer {
    static ArrayList<String> stopwords = loadStopwords("src/main/resources/stopwords/stopwordsEn.txt","src/main/resources/stopwords/stopwordsGr.txt");
    static TreeMap<String, Integer > docs = new TreeMap<>();
    static ArrayList<String> documents = new ArrayList<>();
    static HashMap<String,Long> tokenPos = new HashMap<>();
    static ArrayList<Double> norms = new ArrayList<>();
    static TreeMap<String,Integer> tokensInDoc = new TreeMap<>();
    static TreeMap<String, HashMap<String,ArrayList<Integer>>> tokenPositions = new TreeMap<>();
    static TreeMap<String, HashMap<String, HashMap<String, Integer>>>parsed = new TreeMap<String, HashMap<String, HashMap<String, Integer>>>();
    static void createPostingFile(){
        File collectionIndexDir = new File("CollectionIndex");
        AtomicBoolean flag = new AtomicBoolean(false);
        if (!collectionIndexDir.exists()) {
            collectionIndexDir.mkdirs();
        }
        File postingFile = new File(collectionIndexDir, "PostingFile.txt");
        try (PositionTrackingFileWriter writer = new PositionTrackingFileWriter(postingFile)) {
            parsed.entrySet().forEach(token->{
                flag.set(false);
                try {
                    writer.write(token.getKey()+"\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                token.getValue().entrySet().forEach(filepath->{

                    double tf[] = {0};
                    double idf[]={0};
                    docs.entrySet().forEach(index->{
                        if ( index.getKey().equals(filepath.getKey())) {
                            if (!flag.get()) {
                                long pos;
                                pos = writer.getCurrentPosition();
                                tokenPos.put(token.getKey(),pos);
                                flag.set(true);
                            }
                            filepath.getValue().entrySet().forEach(tag->{
                                HashMap<String,ArrayList<Integer>> tmp = new HashMap<>();
                                tf[0] += tag.getValue().intValue();
                            });
                            int docsize = tokensInDoc.get(filepath.getKey());
                            idf[0] = Math.log((double) docsize / tf[0]);
                            double tfidf = tf[0]*idf[0];

                            try {
                                writer.write(" d:"+index.getValue()+"  tfidf:"+tfidf);
                                HashMap<String,ArrayList<Integer>> tmp = tokenPositions.get(token.getKey());
                                ArrayList<Integer> tmpA = tmp.get(filepath.getKey());
                                writer.write(String.valueOf("  "+tmpA+"\n"));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                });
            });
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    static void createVocabularyFile(){
        // Create the CollectionIndex directory
        File collectionIndexDir = new File("CollectionIndex");
        if (!collectionIndexDir.exists()) {
            collectionIndexDir.mkdirs();
        }
        File vocabularyFile = new File(collectionIndexDir, "VocabularyFile.txt");
        try (FileWriter writer = new FileWriter(vocabularyFile)) {
            parsed.entrySet().forEach(entry->{
                try {
                    writer.write(entry.getKey()+"\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                entry.getValue().entrySet().forEach(filepath->{
                    if(entry.getValue().keySet().contains(filepath.getKey())) {
                        try {
                            writer.write( filepath.getKey()+ " ");
                            filepath.getValue().entrySet().forEach(tag->{

                                try {
                                    writer.write(" "+tag.getKey()+" df:" +tag.getValue());
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                            writer.write("  ");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                try {
                    writer.write("Position:"+tokenPos.get(entry.getKey())+"\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void countTokens(String filename){
        int count[] ={0};
        parsed.entrySet().forEach(token->{
            docs.entrySet().forEach(doc->{
                if (doc.getKey().equals(filename)&& token.getValue().containsKey(filename)) count[0]++;
            });
        });
        tokensInDoc.put(filename,count[0]);
    }
    public static void createDocumentsFile() throws IOException {
        File collectionIndexDir = new File("CollectionIndex");
        if (!collectionIndexDir.exists()) {
            collectionIndexDir.mkdirs();
        }
        File documentsFile = new File(collectionIndexDir, "DocumentsFile.txt");
        try (FileWriter write = new FileWriter(documentsFile)) {

            Iterator it = documents.iterator();
            int i = 0;
            while (it.hasNext()) {
                final String str= it.next().toString();
                docs.put(str, i);
                norms.add(i, calculateNorm(str));
                try {
                    write.write(i+" " + str + " norm:" + norms.get(i) + "\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        docs.entrySet().forEach(doc->{
            countTokens(doc.getKey());
        });
    }
    public static double calculateNorm(String filepath) {
        final double[] sumOfSquares = {0.0};
        parsed.entrySet().forEach(token->{
            token.getValue().entrySet().forEach(file->{
                if ( file.getKey().equals(filepath)) {
                    file.getValue().entrySet().forEach(tag->{
                        int appearance  = tag.getValue().intValue();
                        sumOfSquares[0] += appearance*appearance;
                    });
                }
            });
        });
        return Math.sqrt(sumOfSquares[0]);
    }

    public static void tokenize(String s, String tag, String filename){
        String WORD_REGEX = "\\b[a-zA-Z_]+\\b";
        Pattern pattern = Pattern.compile(WORD_REGEX);
        StopwordFilter filter = new StopwordFilter();
        s = filter.filterStopwords(s);
        Matcher matcher = pattern.matcher(s);
        int position;
        while(matcher.find()) {
            String token = matcher.group();
            position = matcher.start();
            token = Stemmer.Stem(token);
            token = token.toLowerCase();

            // CASES
            if (parsed.get(token) == null) {
                HashMap<String, Integer> tmp = new HashMap<>();
                HashMap<String, HashMap<String, Integer>> tmp2 = new HashMap<>();
                tmp.put(tag, 1);
                tmp2.put(filename, tmp);
                parsed.put(token, tmp2);
                HashMap<String,ArrayList<Integer>> tmpa = new HashMap<>();
                ArrayList<Integer> tmpi = new ArrayList<>();
                tmpi.add(position);
                tmpa.put(filename,tmpi);
                tokenPositions.put(token,tmpa);
            } else {
                HashMap<String,ArrayList<Integer>> tmp = tokenPositions.get(token);
                if ( tmp.get(filename)==null) {
                    ArrayList<Integer> tmpi = new ArrayList<>();
                    tmpi.add(position);
                    tokenPositions.get(token).put(filename,tmpi);
                }else {
                    if ( tmp.get(filename).get(0)==null)  tokenPositions.get(token).get(filename).add(0,position);
                    else tokenPositions.get(token).get(filename).add(position);
                }
                HashMap<String, HashMap<String, Integer>> temp = parsed.get(token);
                if (temp.get(filename) == null) {
                    HashMap<String, Integer> temp1 = new HashMap<String, Integer>();
                    temp1.put(tag, 1);
                    parsed.get(token).put(filename, temp1);
                } else {
                    if (temp.get(filename).get(tag) == null) parsed.get(token).get(filename).put(tag, 1);
                    else parsed.get(token).get(filename).put(tag, parsed.get(token).get(filename).get(tag) + 1);
                }
            }


        }
    }
    public static void parseNXML(String filename) {

        File file = new File(filename);
        StopwordFilter filter = new StopwordFilter();

        int idx = 0;
        NXMLFileReader xmlFile = null;
        try {
            xmlFile = new NXMLFileReader(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String pmcid = xmlFile.getPMCID();
        String filteredPmcid = filter.filterStopwords(pmcid);
        tokenize(filteredPmcid, "pmcid", filename);
        String title = xmlFile.getTitle();
        String filteredTitle = filter.filterStopwords(title);
        tokenize(filteredTitle, "title", filename);
        String abstr = xmlFile.getAbstr();
        String filteredabstr= filter.filterStopwords(abstr);
        tokenize(filteredabstr, "abstr", filename);
        String body = xmlFile.getBody();
        String filteredbody = filter.filterStopwords(body);
        tokenize(filteredbody, "body", filename);
        String journal = xmlFile.getJournal();
        String filteredjournal= filter.filterStopwords(journal);
        tokenize(filteredjournal, "journal", filename);
        String publisher = xmlFile.getPublisher();
        String filteredpublisher = filter.filterStopwords(publisher);
        tokenize(filteredpublisher, "publisher", filename);
        ArrayList<String> authors = xmlFile.getAuthors();
        for (int i = 0; i < authors.size(); i++) {
            String filteredauthor = filter.filterStopwords(authors.get(i));
            tokenize(filteredauthor, "authors", filename);
        }
        HashSet<String> categories = xmlFile.getCategories();
        Iterator<String> it = categories.iterator();
        while (it.hasNext()) {
            String filteredcategory = filter.filterStopwords(it.next());
            tokenize(filteredcategory, "categories", filename);
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
    public static void listFilesForFolder(File folder) {
        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                documents.add(fileEntry.getAbsolutePath());
                parseNXML(fileEntry.getAbsolutePath());
            }
        }
    }
    public static void main(String[] args) throws UnsupportedEncodingException, IOException {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                SimpleIndexerGUI gui = new SimpleIndexerGUI();
                gui.setVisible(true);
            }
        });
    }
}
class StopwordFilter {
    static ArrayList<String> stopwords = loadStopwords("src/main/resources/stopwords/stopwordsEn.txt","src/main/resources/stopwords/stopwordsGr.txt");


    public StopwordFilter() {
        this.stopwords = stopwords;
    }

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




