package com.technion.ir.trainers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.technion.ir.Utils.Utils;

public class ParamTrainer {
	
	//private static final String[] addedFeatureArray = {"1","2","3","4","5","6","7"};
	
/*	private static final String[] addedFeatureArray = {"1","2","3","5","6","7","8","9","10","11","12","14","15",
		"16","17","18","19","20","21","23","24","25","26","27"};*/
	
	private static final String[] addedFeatureArray = {"1","2","3","5","6","7","8","9"};
	
	private String baseFeatureDirectory; //Including RelevanceFiveCategoryTenBucket
	private String takenFeatureDirectory;
	private String outDirectory;
	
	
	public ParamTrainer (String baseDirectory, String takenDirectory, String out) {
		
		this.baseFeatureDirectory = baseDirectory;
		this.takenFeatureDirectory = takenDirectory;
		this.outDirectory = out;
	}
	
	public void combineFeautres () throws IOException {
		System.out.println("Create Combine Directory");
		
		String updatedOutPath = openNewDirectory (FilenameUtils.getName(baseFeatureDirectory), this.outDirectory);
		
		File[] baseQueriesDirectory = new File(baseFeatureDirectory).listFiles();
		File[] takenQueriesDirectory = new File(takenFeatureDirectory).listFiles();
	
		
		for (File baseDirectory : baseQueriesDirectory) {//loop over ParamDirecotries 500_0.8/1500_0.9..
			
			
			File takenDirectory = getCorrespondingFile (takenQueriesDirectory,baseDirectory.getName());//Get the relevant directory param
			
			if (takenDirectory == null) continue;
			String currentPath = openNewDirectory(baseDirectory.getName(), updatedOutPath);
			
			combineFeatures(currentPath, baseDirectory, takenDirectory);

			
		}
		
		
		
		
	}

	private void combineFeatures(String currentPath, File baseDirectory,
			File takenDirectory) throws IOException {
		
		File[] baseQueriesFeatures = baseDirectory.listFiles();
		File[] takenQueriesFeatures = takenDirectory.listFiles();
		
		for (File baseQuery : baseQueriesFeatures) {
			System.out.println("Check " + baseQuery.getName());
			//If the file is the passage re-rank list than write to directory
/*			if (baseQuery.getName().contains("DOC_LTR_FEATURES") || baseQuery.getName().contains("DOC_RANKED_LIST_QL")){
				Utils.writeFeatures( currentPath+"/"+baseQuery.getName(), FileUtils.readLines(baseQuery));
				continue;
			}*/
			
			File takenFile = getCorrespondingFile(takenQueriesFeatures, baseQuery.getName());
			if (takenFile == null){//The same query file must be found
				System.out.println("No corresponding file found");
				System.exit(1);
			}
			
			List<String> baseFileLines = FileUtils.readLines(baseQuery);
			List<String> takenFileLines = FileUtils.readLines(takenFile);
			
			//check line numbers
			if (baseFileLines.size() != takenFileLines.size()){
				System.out.println("Files dont have the same number of lines: " + baseQuery.getName());
				System.exit(1);
			}
			
			List<String> newBaseFileLines = new ArrayList<String> (); // store the baseFile with its added features
			
			for (int i=0 ; i < baseFileLines.size(); i++) {
				
				
				String[] baseLineArray = baseFileLines.get(i).split("\\s+");
				int baseLineLength = baseLineArray.length; // in order to know the number of features in the baseline
				String[] takenLineArray = takenFileLines.get(i).split("\\s+");
				List<String> takenNeededFeatures = getNeededFeatures (takenLineArray);
				
				String baseLine = baseFileLines.get(i);
				for (String feaureValue : takenNeededFeatures) {
					
					int featureID = baseLineLength-1;
					baseLine = baseLine.concat(featureID+":"+feaureValue+" ");
					baseLineLength++;
				}
				
				newBaseFileLines.add(baseLine);
				
				
			}
			

			//Print QueryFile
			System.out.println("print new " + baseQuery.getName());
			Utils.writeFeatures( currentPath+"/"+baseQuery.getName(), newBaseFileLines);
		}
	}
	/**
	 * Take the needed features from the corresponding line according to the addedFeatureArray 
	 * @param takenLineArray
	 * @return
	 */
	private List<String> getNeededFeatures(String[] takenLineArray) {
		
		List<String> featureValue = new ArrayList<String>();
 		
		for (int i=2 ; i < takenLineArray.length; i++) { //starts from 2 because the first are "4 qid:2009001" and I need to start from the feature vector
			
			String[] splitedFeaure = takenLineArray[i].split(":");
			if (splitedFeaure.length < 2) {
				System.out.println("splited feature no feature " + takenLineArray[1] );
			}
			for (String featureID : addedFeatureArray) {
				if (splitedFeaure[0].equals(featureID))
					featureValue.add(splitedFeaure[1]);
			}
			
	}
		
			
		return featureValue;
	}

	/**
	 * Find the corresponding file in the given file array
	 * @param takenQueriesFeatures
	 * @param name
	 * @return
	 */
	private File getCorrespondingFile(File[] takenQueriesFeatures, String name) {
		
		for (File file : takenQueriesFeatures) {
			if (file.getName().equals(name))
				return file;
		}

		return null;
	}

	private String openNewDirectory(String name, String outDirectory2) {
		
		File file = new File(outDirectory2+"/" + name);
		if (!file.exists()) {
			if (file.mkdir()){
				System.out.println("directory created");
			} else {
				System.out.println("Failed to create directory");
			}
		}
		return outDirectory2+"/" + name+"/";
		
	}
	

}
