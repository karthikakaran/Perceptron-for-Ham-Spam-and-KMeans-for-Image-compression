import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@SuppressWarnings("unused")
public class PerceptronMain {

	static Set<String> ham_fileset = new HashSet<String>();
	static Set<String> spam_fileset = new HashSet<String>();
	static Set<String> all_fileset = new HashSet<String>();
	static Set<String> vocab_set = new HashSet<String>();
	static Set<String> stopWords = new HashSet<String>();
	static HashMap<String, HashSet<String>> st = new HashMap<String, HashSet<String>>();
	static HashMap<String, HashMap<String,Integer>> ham_filemap = new HashMap<String, HashMap<String,Integer>>();
	static HashMap<String, HashMap<String,Integer>> spam_filemap = new HashMap<String, HashMap<String,Integer>>();
	static String stopWordsFlag;
	
	public static void main(String[] args) throws Exception {
		if (args.length < 1){
		    System.out.println("Usage: PerceptronMain <Y/N for stopwordsRemovalFlag>");
		    return;
		}
		
		//String dir_location = args[0];
		stopWordsFlag = args[0];
		
		if(stopWordsFlag.equalsIgnoreCase("Y")){
			BufferedReader sreader = new BufferedReader(new FileReader("Stopwords.txt"));
			for (String word = sreader.readLine(); word != null; word = sreader.readLine())
				stopWords.add(word.toLowerCase());
		}

		File dir_spam_train = new File("train/spam");
		File dir_ham_train = new File("train/ham");

		File dir_spam_test = new File("test/spam");
		File dir_ham_test = new File("test/ham");


		for (File file : dir_ham_train.listFiles()) {
			String current_filename= file.getName();
			HashMap<String, Integer> ham_filevocab = new HashMap<String, Integer>();
			ham_fileset.add(current_filename);
			all_fileset.add(current_filename);

			BufferedReader reader = new BufferedReader(new FileReader(file));
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				for (String _word : line.split("[^a-zA-Z+]")) {// \\W+
					String word = _word.toLowerCase();

					if(stopWordsFlag.equalsIgnoreCase("Y") && stopWords.contains(word))
						continue;
					
					HashSet<String> filenames = st.get(word);
					if (filenames == null) {
						filenames = new HashSet<String>();
						filenames.add(file.getName());
						st.put(word,filenames );

					}
					filenames.add(file.getName());

					if(ham_filevocab.containsKey(word)){
						ham_filevocab.put(word, ham_filevocab.get(word)+1);
					}
					else{
						ham_filevocab.put(word, 1);
					}

				}
			}
			ham_filemap.put(current_filename, ham_filevocab);

		}

		for (File file : dir_spam_train.listFiles()) {
			String current_filename= file.getName();
			HashMap<String, Integer> spam_filevocab = new HashMap<String, Integer>();

			spam_fileset.add(current_filename);
			all_fileset.add(current_filename);

			BufferedReader reader = new BufferedReader(new FileReader(file));
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				for (String _word : line.split("[^a-zA-Z+]")) {
					String word = _word.toLowerCase();

					if(stopWordsFlag.equalsIgnoreCase("Y") && stopWords.contains(word))
						continue;
					
					HashSet<String> filenames = st.get(word);
					if (filenames == null) {
						filenames = new HashSet<String>();
						filenames.add(file.getName());
						st.put(word,filenames );

					}
					filenames.add(file.getName());

					if(spam_filevocab.containsKey(word)){
						spam_filevocab.put(word, spam_filevocab.get(word)+1);
					}
					else{
						spam_filevocab.put(word, 1);
					}
				}
			}
			spam_filemap.put(current_filename, spam_filevocab);

		}
		System.out.println("Training completed");

		Perceptron perceptron = new Perceptron(ham_fileset, spam_fileset, all_fileset,ham_filemap,spam_filemap,st);
		perceptron.perceptronTrain();
		System.out.println("Testing...");

		int h_count =0;
		for(File f : dir_ham_test.listFiles()){
			HashMap<String, Integer> thismap = new HashMap<String, Integer>();
			BufferedReader reader = new BufferedReader(new FileReader(f));

			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				for (String _word : line.split("[^a-zA-Z+]")) {
					String word = _word.toLowerCase();
					
					if(stopWordsFlag.equalsIgnoreCase("Y") && stopWords.contains(word))
						continue;
					
					if(thismap.containsKey(word)){
						thismap.put(word, thismap.get(word)+1);
					}
					else{
						thismap.put(word, 1);
					}			
				}
			}
			reader.close();
			int result = perceptron.perceptronTest(thismap);
			if(result == 1){
				h_count++;
			}
		}
		//System.out.println("Accuracy of Ham Classification on test data : "+((float)h_count/(float)dir_ham_test.listFiles().length) * 100);
				
		int s_count =0;
		for(File f : dir_spam_test.listFiles()){
			HashMap<String, Integer> thismap = new HashMap<String, Integer>();
			BufferedReader reader = new BufferedReader(new FileReader(f));

			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				for (String _word : line.split("[^a-zA-Z+]")) {
					String word = _word.toLowerCase();
					
					if(stopWordsFlag.equalsIgnoreCase("Y") && stopWords.contains(word))
						continue;
					
					if(thismap.containsKey(word)){
						thismap.put(word, thismap.get(word)+1);
					}
					else{
						thismap.put(word, 1);
					}			
				}
			}
			reader.close();
			int result = perceptron.perceptronTest(thismap);
			if(result == 0){
				s_count++;
			}
		}
		//System.out.println("Accuracy of Spam Classification on test data : "+((float)s_count/(float)dir_spam_test.listFiles().length) * 100);
		int count = h_count + s_count;
		int total = dir_ham_test.listFiles().length + dir_spam_test.listFiles().length;
		System.out.println("Accuracy of Classification on test data : "+((float)count/(float)total) * 100);
	}
}
