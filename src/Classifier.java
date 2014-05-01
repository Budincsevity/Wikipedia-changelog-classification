import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Random;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class Classifier {

	Instances			trainData;
	Instances			testData;
	StringToWordVector	filter;
	BayesNet			classifier;
	String				output = "";
	
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
	 */
	public Classifier(Instances trainData) {
		this.trainData = trainData;
	}
	
	/**
	 * Sets the BayesNet classifier for train data
	 */
	public void train() {
		try {
			classifier = new BayesNet();
			classifier.buildClassifier(trainData);
			
			System.out.println("Training is over.");
		}
		catch (Exception e) {
			System.out.println("Something went wrong at training:" + e.toString());
		}
	}
	
	/**
	 * Tests the train data with cross validation model
	 */
	public void evaluate() {
		try {
			Evaluation eval = new Evaluation(trainData);
			eval.crossValidateModel(classifier, trainData, 10, new Random(1));

			System.out.println("F-measure for vandalism:" + eval.fMeasure(0));
		} catch(Exception e) {
			System.out.println("Something went wrongt at testing:" + e.toString());
		}
	}
	
	/**
	 * Labeling the test data
	 * 
	 * @throws Exception
	 */
	public void classify() throws Exception {
		Instances forLabeling = new Instances(testData);
		
		for (int i=0; i< forLabeling.numInstances(); i++){
			double label = classifier.classifyInstance(testData.instance(i));
			forLabeling.instance(i).setClassValue(label);
			
			int docIDIndex = forLabeling.attribute("DocID").index(); 

			//Concatenate the docId and vandalism/regular for predictions
			output += (int)forLabeling.instance(i).value(docIDIndex)+"\t"+forLabeling.classAttribute().value((int) label)+"\n";
		}
		System.out.println("Labeling is done.");
	}
	
	/**
	 * Saves the concatenated string to predictions.txt
	 * 
	 * @throws IOException
	 */
	public void savePredictions() throws IOException {
		Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("predictions.txt"), "UTF-8"));
		out.write(output);
		out.close();
		System.out.println("predictions.txt saved.");
	}
}
