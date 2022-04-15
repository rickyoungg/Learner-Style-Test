package com.RickYoung.Learner.Style.Test.Service;

import com.RickYoung.Learner.Style.Test.Model.Quiz;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.apache.commons.io.FileUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class LearnerServiceImpl implements LearnerService{
    private HashMap<String, HashMap<String,ArrayList<String>>> optionsMapping= new HashMap();

    public LearnerServiceImpl(){
        //creates a new hasmap called inner hashmap, String and ArrayList
        HashMap<String, ArrayList<String>> innerHashMap = new HashMap<>();
        ArrayList<String> outputs = new ArrayList<>();

        //sets the csv file for the scanner
        File f = null;
        try {
            //f = new ClassPathResource("options.csv").getFile();
            ClassPathResource classPathResource = new ClassPathResource("options.csv");

            InputStream inputStream = classPathResource.getInputStream();
            f= File.createTempFile("options", ".csv");
            try {
                FileUtils.copyInputStreamToFile(inputStream, f);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("File Path breaks");
        }

        Scanner scan = null;
        try {
            scan = new Scanner(f);
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            System.out.println("failed local file");
        }
        int questionAmount = 35;
        // new array with the 4 different options
        String[] arr = new String[]{"Strongly Disagree", "Somewhat Disagree","Somewhat Agree","Strongly Agree"};
        int count = 0;
        while(scan.hasNext() && count < questionAmount){
            // sets the question as the first line in the loop
            String question = scan.nextLine().replace("\"","").replace(".","");
            //format question to be only numeric and alpha

            char [] check = question.toCharArray();
            for(int y = 0; y< question.length(); y++){
                if (Character.isAlphabetic(check[y]) || (Character.isDigit(check[y]) || check[y] == ' ')){
                    continue;
                }
                else{
                    question = question.replace(String.valueOf(check[y]),"");
                }
            }

            // sets the array for 1,2,3,4, which is the next line
            ArrayList<String> options = getRecordFromLine(scan.nextLine());
            // sets the array of values such m4 or m3  or a combo by spliting them up by the comma
            ArrayList<String> values = getRecordFromLine(scan.nextLine());
            // a for loop that splits up value within the values arer list by the pipe.
            for (int i = 0; i < 4; i++) {
                // sets a new array that is equal to the ith item in value that gets split.
                String[] valuesSplitUp = values.get(i).split("\\|");
                // sets a new array list
                ArrayList<String> temp = new ArrayList<>();
                // for loop that adds each ith item in value (which was split up) found within the option.
                for(int x = 0; x < valuesSplitUp.length; x++){
                    // adds the value to the array
                    temp.add(valuesSplitUp[x].replace("\"","").trim());
                }
                // adds the temp array to be the value for the hashmap (inner one aka option,values)
                innerHashMap.put(arr[i], temp);
            }
            // now puts the previous (inner) hashmap as the value for the question
            optionsMapping.put(question, innerHashMap);
            // creates a new hashmap to clear the last one
            innerHashMap = new HashMap<>();
            // scans the next line of ,,,,
            scan.nextLine();
            // adds to the count
            count++;
        }

    }

    private ArrayList<String> getRecordFromLine(String line) {
        ArrayList<String> values = new ArrayList<String>();
        try (Scanner rowScanner = new Scanner(line)) {
            rowScanner.useDelimiter(",");
            while (rowScanner.hasNext()) {
                values.add(rowScanner.next());
            }
        }
        return values;
    }

    @Override
    public String GradeQuiz(Quiz submission) {
        HashMap<String, Integer> counter = new HashMap<String, Integer>();
        counter.put("M1", 0);
        counter.put("M2", 0);
        counter.put("M3", 0);
        counter.put("M4", 0);
        counter.put("F1", 0);
        counter.put("F2", 0);
        counter.put("F3", 0);
        counter.put("F4", 0);


        for(int i = 0; i < submission.getStudentAns().size(); i++){
           // submission.getStudentAns().get(2).getQuestion();
            String q = submission.getStudentAns().get(i).getQuestion().replace(".", "");
            String a = submission.getStudentAns().get(i).getAnswer();
            //optionsMapping.get(q) -> hashmap<ans, list(values)>: agree -> {m1,m3,m4}
            //optionsMapping.get(q).get(a);
            /*
                .get(q) returns a hashmap with the four different options (Strongly Disagree, Somewhat agree, etc) in a hashmap form ans,values see below
                .get(q).get(a) returns an arrayList which contains the list of Strings of values (M4, M3, F1, F2, etc) for a specific option of the question
             */
            // q1 -> SA
            // q1 -> {...} ->{SA -> m4


            char [] check = q.toCharArray();
            for(int y = 0; y< q.length(); y++){
                if (Character.isAlphabetic(check[y]) || (Character.isDigit(check[y]) || check[y] == ' ')){
                    continue;
                }
                else{
                    q = q.replace(String.valueOf(check[y]),"");
                }
            }

            ArrayList<String> ansArr = optionsMapping.get(q).get(a);
            // x  0  1  2
            //   [m1,m3,f1]
            // have a hashmap of k-v where k is the value name and v is the a count
            // go through our ans and add the respective amount to the said key's value
            for(int x = 0; x < ansArr.size(); x++){
                String current = ansArr.get(x);


                if(a.contains("Strongly")){
                    counter.put(current, counter.get(current) +1);
                }
                counter.put(current, counter.get(current) + 1);
            }

        }
        Integer total = 0;
        for (Map.Entry<String, Integer> entry : counter.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            total += value;
        }

        for (Map.Entry<String, Integer> entry : counter.entrySet()) {
            double percent  = 0;
            String key = entry.getKey();
            Integer value = entry.getValue();
            percent = (((double)value) / total) *100;
            percent = Math.round(percent);
        }
        // Control Freak Learner type.
        Integer controlFreakCount = 0;
        controlFreakCount += counter.get("F1");
        controlFreakCount += counter.get("M2");
        controlFreakCount += counter.get("M1");
        controlFreakCount += counter.get("F4");

        double controlFreakPercent = learnerPercent(controlFreakCount, total);


        Integer escapeArtistCount = 0;
        escapeArtistCount += counter.get("F2");
        escapeArtistCount += counter.get("M3");
        escapeArtistCount += counter.get("M4");

        double escapeArtistPercent = learnerPercent(escapeArtistCount, total);


        Integer nonchalantCount = 0;
        nonchalantCount += counter.get("M1");
        nonchalantCount += counter.get("M3");
        nonchalantCount += counter.get("F3");

        double nonchalantPercent = learnerPercent(nonchalantCount, total);


        Integer helplessVictimCount = 0;
        helplessVictimCount += counter.get("M1");
        helplessVictimCount += counter.get("M3");
        helplessVictimCount += counter.get("F3");

        double helplessVictimPercent = learnerPercent(helplessVictimCount, total);

        ArrayList<Pair> listOfTypes = new ArrayList<>();
        listOfTypes.add(new Pair("Control Freak",controlFreakPercent));
        listOfTypes.add(new Pair("Escape Artists", escapeArtistPercent));
        listOfTypes.add(new Pair("Nonchalant n'er do well", nonchalantPercent));
        listOfTypes.add(new Pair("Helpless Victim", helplessVictimPercent));


        PriorityQueue<Pair> maxheap = new PriorityQueue<Pair>((a,b) -> Double.compare(b.percent,a.percent));
        maxheap.addAll(listOfTypes);
        ArrayList<Pair> ans = new ArrayList<>();
        ans.add(maxheap.poll());
        double max = ans.get(0).percent;
        while (!maxheap.isEmpty() ){
            if(maxheap.peek().percent == max){
                ans.add(maxheap.poll());
            }else{
                maxheap.poll();
            }
        }
        // [a, b] or [a]  or [a,b,c]
        // a,b or a or a,b,c
        //urs: a,b, or a, or ...
        String ansStr = "";
        for(int z = 0; z < ans.size(); z++){
            ansStr += ans.get(z);
            if(z != ans.size()-1){
                ansStr += ",";
            }

        }
        return ansStr;
    }
    public double learnerPercent(Integer count, Integer total){
        double learnerPercent = (((double)count) / total) *100;
        learnerPercent = Math.round(learnerPercent);
        return  learnerPercent;
    }

    class Pair{
        String name;
        Double percent;
        public Pair(String name, Double percent){
            this.percent = percent;
            this.name = name;
        }

        @Override
        public String toString() {
            return "Pair{" +
                    "name='" + name + '\'' +
                    ", percent=" + percent +
                    '}';
        }
    }
}
