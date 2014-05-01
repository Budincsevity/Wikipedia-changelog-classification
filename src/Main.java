import weka.core.Instances;

public class Main {
	private static Classifier		classifier;
	private static FilterManager	filterManager;
	private static ArffCreator		trainCreator;
	private static ArffCreator		testCreator;
	private static String			trainFileName;
	private static String			testFileName;
	private static final int		ATTRIBUTE_NUMBER = 37;
	public static void main(String[] args) throws Exception {
		
		if (args.length != 2){
			System.out.println("Not enough parameters.\r\nUsage: program [trainFileName] [testFileName]");
		} else {
			trainFileName = args[0];
			testFileName = args[1];
			
			/*
			 * Read train file and save train.arff
			 */
			trainCreator = new ArffCreator(trainFileName, "train");
			Instances trainInstances = trainCreator.getData();
			
			System.out.println("Reading train file completed.");
	
					
			/*
			 * Read test file and save test.arff
			 */
			testCreator = new ArffCreator(testFileName, "test");
			Instances testInstances = testCreator.getData();
			
			System.out.println("Reading test file completed.");
	
			
			/*
			 * Select best ranked attributes for train and test instances
			 */
			filterManager = new FilterManager(trainInstances, testInstances);
			filterManager.initFilter();
		    filterManager.selectRankedAttributes(ATTRIBUTE_NUMBER);
			filterManager.saveSelectedData();
		    
		    trainInstances = filterManager.getTrainData();
		    testInstances = filterManager.getTestData();
		    
		    
			/*
			 * Start training
			 */
			classifier = new Classifier(trainInstances);
			
			classifier.train();
			classifier.evaluate();
		
			/*
			 * Label test instances and save predictions.txt
			 */
			classifier.setTestData(testInstances);
			classifier.classify();
			classifier.savePredictions();
		}
	}
}
