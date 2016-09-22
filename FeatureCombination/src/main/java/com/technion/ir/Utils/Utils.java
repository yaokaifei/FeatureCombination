package com.technion.ir.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;




import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;


public class Utils {
	
	public static String RESULT_LINE_FORMAT = "%s Q0 %s %s %s 3MMBase %s %s\n";
	public static String TREC_EVAL_LINE_FORMAT = "%s Q0 %s %s %s indri\n";
	public static String TREC_EVAL_LINE_FORMAT_LENGTH = "%s Q0 %s %s %s indri %s\n";
	private static final Logger logger = Logger.getLogger(Utils.class);
	private static Properties props = null;
	

	
	/**
	 * Contains all datasetQueries
	 * @param collection
	 * @return
	 */
	private static String handleCollectionQueries(String collection) {
		switch (collection) {
		case "INEX":
			return "/INEXqueries.txt";
		case "WT10G":
			return "/WT10Gqueries.txt";
		case "GOV2":
			return "/GOV2queries.txt";
		case "ROBUST":
			return "/ROBUSTqueries.txt";
		case "INEXTREC"://For the PassageBasedDocumentRanking
			return "/INEXqueries.txt";
		default:
			throw new IllegalArgumentException("Unrecognised collection for queires : " + collection);
		}
	}

	public static List<String> readFileToList (String resourcePath) throws IOException {
		String file = resourcePath;
		InputStream infile = Utils.class.getResourceAsStream(file);
		List<String> allLines = Utils.readAllLines(infile);
		return allLines;
	}
	
	// "/stopWordsClean.txt"; "/DocumentLength.txt"; "/DocumentLengthTEST.txt"; "/TenFoldRandFile.txt";
	
	
	public static List<String> readFile (String path) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF8")); 
		List<String> allLines = Utils.readAllLines(reader);
		
		return allLines;
	}
	
	public static List<String> readAllLines(BufferedReader reader) throws IOException {
		List<String> sentenceList = new ArrayList<String> ();
		String line="";
		while ((line = reader.readLine()) != null)//loop till you dont have any lines left
		{
			sentenceList.add(line);
		}
	
		return sentenceList;
	}
	
	private static List<String> readAllLines(InputStream queriesFile) throws IOException {
		List<String> queriesList = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(queriesFile));
		String line = "";
		while ((line = reader.readLine()) != null) {
			queriesList.add(line);
		}
		return queriesList;
	}
	
	/**
	 * get List of String files
	 * @param readLines
	 * @return each line is now array of separated words
	 */
	public static List<String[]> getSplitedFile(List<String> readLines) {
		
		List<String[]> splitedList = new ArrayList<String[]>();
		for (String line : readLines) {
			String[]  lineArray = splitToTokens(line);
			splitedList.add(lineArray);
		}
		
		return splitedList;
	}
	
	// for each line return array of strings containing only tokens (no spaces)
	public static String[] splitToTokens (String line)
	{
			String[] strings = line.split("\\s+");
			return strings;
	}
	/**
	 * save qrel relevant data into structure for INEX
	 * @param qrelRaws
	 * @return
	 */
	public static Map<String,int[][]> buildQrelStructure (List<String[]> qrelRaws, String queryKey) {
		
		boolean indicator = false;//false = not yet arrived to queryKey. True= arrived to queryKey
		Map<String,int[][]> documentRelPassagesMap = new HashMap<String,int[][]> ();
		for (String[] raw : qrelRaws) {
			if (!queryKey.equals(raw[0])) {
				if (!indicator) continue;
				else {
					return documentRelPassagesMap;
				}
			}
			else {
				indicator = true;
				String docKey = raw[2];
				int arrayLength = raw.length - 6;//-6 because only at column 6 starts relevant passages 
				int[][] relPassagesArray = new int[arrayLength][2];
				for (int i=6 ; i< raw.length; i++)
					{
						String[] relPassage = raw[i].split(":");//165:539
						relPassagesArray[i-6][0] = Integer.valueOf(relPassage[0]);//165
						relPassagesArray[i-6][1] = Integer.valueOf(relPassage[1]);//539
					}
				documentRelPassagesMap.put(docKey, relPassagesArray);
			
				}
			
			}
		//for last query in file
		return documentRelPassagesMap;
	}
	
	/**
	 * save qrel relevant data into structure for WT10G/GOV2
	 * @param qrelsLineArray
	 * @param queryID
	 * @return
	 */
	public static Map<String, int[][]> buildQrelStructureForDocuments(
			List<String[]> qrelRaws, String queryKey) {
		
		
		boolean indicator = false;//false = not yet arrived to queryKey. True= arrived to queryKey
		Map<String,int[][]> documentRelPassagesMap = new HashMap<String,int[][]> ();
		for (String[] raw : qrelRaws) {
			if (!queryKey.equals(raw[0])) {
				if (!indicator) continue;
				else {
					return documentRelPassagesMap;
				}
			}
			else {
				indicator = true;
				String docKey = raw[2]; 
				int[][] relPassagesArray = new int[1][2];
				relPassagesArray[0][0] = Integer.valueOf(raw[3]);//0/1/2
				relPassagesArray[0][1] = 0;//always 0 - to be consistent with INEX format
					
				documentRelPassagesMap.put(docKey, relPassagesArray);
			
				}
			}
		//for last query in file
		return documentRelPassagesMap;
	}
	
	
/**
 * print the features data obtained for trainSet and testSet.
 * @param content
 * @param resultOutPutPath
 */
	public static void writeFeatures_To_Ranker (String content, String resultOutPutPath) {
		System.out.println("Print Feature resutls to file");
		try {
			BufferedWriter wr = new BufferedWriter (new OutputStreamWriter(new FileOutputStream(resultOutPutPath), "UTF-8"));
			wr.write(content);
			wr.flush();
			wr.close();
		}
		catch(Exception ex) { 
 			System.out.println("Error while trying to write features to file: " + ex.toString());
		}
	}
	


	
	public static String readProperty(String propertyName) {
		String propertyValue = null;
		if (props == null) {
			initPropertyFile();
		}
		propertyValue = props.getProperty(propertyName);
		return propertyValue;
	}

	private static void initPropertyFile() {
		props = new Properties();
		InputStream resourceAsStream = Utils.class.getResourceAsStream("/application.properties"); 
		try {
			props.load(resourceAsStream);
			
		} catch (IOException e) {
			System.err.println("Failed to load property File");
			e.printStackTrace();
		}
	}
	


	
	/**
	 * return randomise 5 groups of queriesID for later calculate 5 fold cross validation
	 * @param queryIDs
	 * @return
	 */
	public static List<List<String>> splitToTrainTestSets(List<String> queryIDs) {
		
		List<List<String>> finalGroups = new ArrayList<List<String>> ();
		List<String> copyQueryList = new ArrayList<String>();
		copyQueryList.addAll(queryIDs);
		
		List<String> currentGroup = new ArrayList<String>();
		
		for (int i = 0; i < queryIDs.size(); i++) {
			
			if (currentGroup.size() == 24) {
				finalGroups.add(currentGroup);
				currentGroup = new ArrayList<String>();
			}
				
			Random rand = new Random(); 
			int value = rand.nextInt(copyQueryList.size());
			currentGroup.add(copyQueryList.get(value));
			copyQueryList.remove(value);
			
		}
		//for last group
		if (currentGroup.size() == 24) {
			finalGroups.add(currentGroup);
			currentGroup = new ArrayList<String>();
		}
		
		return finalGroups;
	}
/**
 * keep all files from given directory
 * @param pathDir
 * @return
 * @throws FileNotFoundException 
 */
	public static List<File> getResultsFilesFromDirectory(
			String pathDir) throws FileNotFoundException {
		
		List<File> filesOutQueriesDirectories = new ArrayList<File> ();
		filesOutQueriesDirectories = getAllFilesNames (pathDir);
		
		return filesOutQueriesDirectories;
	}

	private static List<File> getAllFilesNames(String rootDirectory) throws FileNotFoundException {
		
		File root = new File(rootDirectory);
		checkIfDirectoryExists(rootDirectory, root);
		File[] listFiles = root.listFiles();
		return new ArrayList<File>(Arrays.asList(listFiles));
	}
	
	/**
	 * 
	 * @param rootDirectory
	 * @return all feature files
	 * @throws FileNotFoundException 
	 */
	public static List<File> getAllFeatures (String rootDirectory) throws FileNotFoundException {
		File root = new File(rootDirectory);
		checkIfDirectoryExists(rootDirectory, root);
		File[] listFiles = root.listFiles();
		//return only features files
		List<File> featureList = new ArrayList<File>();
		for (File file : listFiles){
			if (FilenameUtils.getExtension(file.getPath().toString()).contains("txt")){
				featureList.add(file);
			}
		}
		
		
		return featureList;
	}
	
	/**
	 * 
	 * @param rootDirectory
	 * @return all res files
	 * @throws FileNotFoundException 
	 */
	public static List<File> getAllRes (String rootDirectory) throws FileNotFoundException {
		File root = new File(rootDirectory);
		checkIfDirectoryExists(rootDirectory, root);
		File[] listFiles = root.listFiles();
		//return only features files
		List<File> featureList = new ArrayList<File>();
		for (File file : listFiles){
			if (FilenameUtils.getExtension(file.getPath().toString()).contains("res")){
				featureList.add(file);
			}
		}
		
		
		return featureList;
	}
	
	/**
	 * 
	 * @param rootDirectory
	 * @return the path of the res file
	 * @throws FileNotFoundException
	 */
	public static String getInitialRankingFile (String rootDirectory) throws FileNotFoundException {
		
		File root = new File(rootDirectory);
		checkIfDirectoryExists(rootDirectory, root);
		File[] listFiles = root.listFiles();
		//return only initial ranking file .res files
		List<File> featureList = new ArrayList<File>();
		for (File file : listFiles){
			if (FilenameUtils.getExtension(file.getPath().toString()).contains("res")){
				return file.getPath();
			}
		}
		
		
		return null;
	}

	private static void checkIfDirectoryExists(String rootDirectory, File root) throws FileNotFoundException {
		if (!root.isDirectory()) {
			throw new FileNotFoundException(rootDirectory + " isn't  drectory");
		}
		
	}

	public static void WriteTrainTestToFile(List<String> allFilesLines, int i, String trainPath) {
		System.out.println("CreateTrainTestFile: + " + i);
		try {
			BufferedWriter wr = new BufferedWriter (new OutputStreamWriter(new FileOutputStream(trainPath), "UTF-8"));
			for (String line: allFilesLines){
				wr.write(line);
				wr.newLine();
				wr.flush();	
			}
			wr.close();
		}
		catch(Exception ex) { 
 			System.out.println("Error while trying to write features to file: " + ex.toString());
		}
		
	}
	
	public static void writeReRankList (List<String[]> lines, String outPath){
		
		System.out.println("Create ReRank file");
		try {
			BufferedWriter wr = new BufferedWriter (new OutputStreamWriter(new FileOutputStream(outPath), "UTF-8"));
			for (String[] line: lines){
				String printLine = String.format(RESULT_LINE_FORMAT, line[0], line[2], line[3],line[4],line[6],line[7]); 
				wr.write(printLine);
				wr.flush();	
			}
			wr.close();
		}
		catch(Exception ex) { 
 			System.out.println("Error while trying to write features to file: " + ex.toString());
		}
	}

	/**
	 * Write the length difference between document in WebAP to GOV2
	 * @param engine
	 * @param webAPDocLength
	 */
	public static void writeDocumentDifference(String webAPDocLengthFile,
			String GOV2DocLengthFile) throws Exception{
		//String out = "c:\\Temp\\DocumentLengthWT10G.txt";
		String out = "/lv_local/home/seilon/DocumentLengthDifferenceGOV2WebAP.txt";
		System.out.println("start calculating length difference");
		String content = "";
		List<String[]> webAPLengthSplited = getSplitedFile(FileUtils.readLines(new File (webAPDocLengthFile))); 
		List<String[]> GOV2LengthSplited = getSplitedFile(FileUtils.readLines(new File (GOV2DocLengthFile))); 
		try {
			BufferedWriter wr = new BufferedWriter (new OutputStreamWriter(new FileOutputStream(out), "UTF-8"));
			for (int i=0; i < webAPLengthSplited.size(); i++) {
				//split doc name without the query ID. i.e: GX268-35-11839875-701 to GX268-35-11839875
				String[] strings = webAPLengthSplited.get(i)[0].split("-");
				String docName = strings[0].concat("-"+strings[1]).concat("-"+strings[2]);
				for (int j=0; j < GOV2LengthSplited.size(); j++) {
					if (GOV2LengthSplited.get(j)[0].equals(docName)) {
						content = docName + " " + (Integer.parseInt(GOV2LengthSplited.get(j)[1]) - Integer.parseInt(webAPLengthSplited.get(i)[1]));
						wr.write(content);
						wr.newLine();
						wr.flush();
						break;
					}
				}
			}
			wr.close();	
		} 	
		catch(Exception ex) { 
 			System.out.println("Error while trying to write parameter file: " + ex.toString());
		}
	

	}

	public static void writeFeatures(String updatedOutPath,
			List<String> newBaseFileLines) {
		String content="";
		try {
			BufferedWriter wr = new BufferedWriter (new OutputStreamWriter(new FileOutputStream(updatedOutPath), "UTF-8"));
			for (String line : newBaseFileLines){
				content+= line + "\n";
			}
			wr.write(content);
			wr.flush();
			wr.close();
		}
		catch(Exception ex) { 
 			System.out.println("Error while trying to write features to file: " + ex.toString());
		}
	}
		

	
}
