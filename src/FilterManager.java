import java.io.File;
import java.io.IOException;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;

public class FilterManager {
	Instances			trainData;
	Instances			testData;
	StringToWordVector	filter;
	
	public Instances getTrainData() {
		return trainData;
	}

	public void setTrainData(Instances trainData) {
		this.trainData = trainData;
	}

	public Instances getTestData() {
		return testData;
	}

	public void setTestData(Instances testData) {
		this.testData = testData;
	}
	
	/**
	 * @param trainData
	 * @param testData
	 */
	public FilterManager(Instances trainData, Instances testData) {
		this.trainData = trainData;
		this.testData = testData;
	}
	
	/**
	 * Initializes StringToWordVector filter for train and test Instances
	 * 
	 * @throws Exception
	 */
	public void initFilter() throws Exception {
		filter = new StringToWordVector();

		int classIndex = trainData.attribute("Class").index(); 
		trainData.setClassIndex(classIndex);
		filter.setInputFormat(trainData);
		trainData = Filter.useFilter(trainData, filter);
			
		classIndex = testData.attribute("Class").index(); 
		testData.setClassIndex(classIndex);
		testData = Filter.useFilter(testData, filter);
		
		System.out.println("I have selected StringToWordVector filter.");
	}
	
	/**
	 * Selects best rated attributes with InfoGainAttributeEval and Ranker
	 * 
	 * @param numberOfAttributes How many attributes to select
	 * @throws Exception
	 */
	public void selectRankedAttributes(int numberOfAttributes) throws Exception {
		System.out.println("Selecting " + numberOfAttributes + " attributes.");
		
		AttributeSelection attributeSelection = new  AttributeSelection();
		InfoGainAttributeEval infoGainAttributeEval = new InfoGainAttributeEval();
		Ranker ranker = new Ranker(); 
	    ranker.setNumToSelect(numberOfAttributes); 
	    
		attributeSelection.setEvaluator(infoGainAttributeEval);
	    attributeSelection.setSearch(ranker); 
	    attributeSelection.setInputFormat(trainData);
	    
	    trainData = Filter.useFilter(trainData, attributeSelection);
	    testData = Filter.useFilter(testData, attributeSelection);
	    
	    System.out.println("I'm done.");
	}
	
	/**
	 * Saves the train and test arff with selected attributes
	 * 
	 * @throws IOException
	 */
	public void saveSelectedData() throws IOException {
		ArffSaver arffSaver = new ArffSaver();
		
		arffSaver.setInstances(testData);
		arffSaver.setFile(new File("data/filtered-test"+".arff"));
		arffSaver.writeBatch();	
		
		arffSaver.setInstances(trainData);
		arffSaver.setFile(new File("data/filtered-train"+".arff"));
		arffSaver.writeBatch();	 
	}
}
