import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

public class ArffCreator {
	private Instances		data;
	private String			fileName;
	private String			changes;
	private String			type;
	private FastVector		attributes;
	private FastVector		booleanAttributes;
	private FastVector		labels;
	
	/**
	 * Sets class index where the attribute is "Class"
	 */
	private void setClassIndex() {
		int classIndex = this.data.attribute("Class").index(); 
		this.data.setClassIndex(classIndex);
	}
	
	/**
	 * 
	 * Sets class index for Instances and returns Instances
	 * 
	 * @return Instances data
	 */
	public Instances getData() {
		setClassIndex();
		return this.data;
	}
	
	//Regular expression for IP checking
	public static final String IPADDRESS_REGEX = 
			"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

	
	/**
	 * @param fileName For example: data/train
	 * @param type Train or test
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public ArffCreator(String fileName, String type) throws FileNotFoundException, IOException {
		this.fileName = fileName;
		this.type = type;
		
		formatDataStructure();
		readFileToParsedText(this.fileName);
		saveArff();
	}
			
	/**
	 * Attributes are:
	 * 0: DocID numeric
	 * 1: Class vandalism/regular
	 * 2: UserID true if IP, false if username 
	 * 3: Comment true/false
	 * 4: Edits 
	 * 5: iCounter for counting the added rows
	 * 6: iWords for counting the added words
	 * 7: dCounter for counting the deleted rows
	 * 7: dWords for counting the deleted words
	 * 8: cLength comment length
	 * .
	 * .
	 * .
	 * there you can add even more specific attributes
	 */
	public void formatDataStructure() {
		System.out.println("Setting attributes.");
		
		attributes = new FastVector();
		booleanAttributes = new FastVector();
		
		booleanAttributes.addElement("true");
		booleanAttributes.addElement("false");
		
		labels = new FastVector();
		labels.addElement("vandalism");
		labels.addElement("regular");
		
		attributes.addElement(new Attribute("DocID"));
		attributes.addElement(new Attribute("Class", labels));
		attributes.addElement(new Attribute("UserID", booleanAttributes));
		attributes.addElement(new Attribute("Comment", booleanAttributes));
		attributes.addElement(new Attribute("Text", (FastVector)null));
		attributes.addElement(new Attribute("iCounter"));
		attributes.addElement(new Attribute("iWords"));
		attributes.addElement(new Attribute("dCounter"));
		attributes.addElement(new Attribute("dWords"));
		attributes.addElement(new Attribute("cLength"));
		
		//Create new Instances from the attributes
		this.data = new Instances("Wiki", attributes, 0);
	}

	/**
	 * @param fileName For example: data/train
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void readFileToParsedText(String fileName) throws IOException, FileNotFoundException {
		BufferedReader		br = null;
		String[]			splitLine;
		String				line;
		double[]			vals;
		int					index = 0;
		int					insertLength = 0;
		int					insertWords = 0;
		int					deleteLenght = 0;
		int					deleteWords = 0;
		int					commentLength = 0;

		//Array for storage
		vals = new double[data.numAttributes()];
		
		br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF8"));
		while ((line = br.readLine()) != null) {
			
			//In example the elements are separated with tabulator
			splitLine = line.split("\t");
					
			//There are three type of rows: head/edits/empty row
			
			//The head of the changelog
			if(splitLine.length > 2) {
				vals = new double[data.numAttributes()];
				//0: DocID
				vals[index++] = Double.parseDouble(splitLine[0]);
				
				//1: Vandalism/regular
				if(splitLine[1].equals("vandalism")) {
					vals[index++] = labels.indexOf("vandalism");
				} else if(splitLine[1].equals("regular")) {
					vals[index++] = labels.indexOf("regular");
				} else {
					vals[index++] = labels.indexOf("regular");
				}
				
				//2: UserID
				vals[index++] = this.booleanAttributes.indexOf(isUserIDIsIP(splitLine[2]));
				
				//3: Comment
				if(splitLine[2].equals("null")) {
					vals[index++] = this.booleanAttributes.indexOf("false");
				} else {
					vals[index++] = this.booleanAttributes.indexOf("true");
					commentLength = splitLine[2].length();
				}
			} 
			//Edits
			if(splitLine.length == 2) {
				
				//Concatenate changes
				String[] splitChange = splitLine[1].split(" ");
				
				for(int i = 0; i < splitChange.length; i++) {
					changes += splitLine[0] + " " + splitChange[i] + " ";
				}
			
				//Calculate the numbers
				if(splitLine[0].equals("I")) {
					insertLength += splitLine[1].length();
					insertWords += splitLine[1].split(" ").length;
				} else {
					deleteLenght += splitLine[1].length();
					deleteWords += splitLine[1].split(" ").length;
				}
				
			//Empty line, here we store the data
			} 
			if (splitLine[0].equals("")) {
				//4: Edits
				vals[index++] = data.attribute(4).addStringValue(changes);
				//5: iCounter
				vals[index++] = insertLength;
				//6: iWords
				vals[index++] = insertWords;
				//7: dCounter
				vals[index++] = deleteLenght;
				//8: dWords
				vals[index++] = deleteWords;
				//9: cLength
				vals[index++] = commentLength;
				
				this.data.add(new Instance(1.0, vals));
				index = 0;
				changes = "";
				commentLength = 0;
				insertLength = 0;
				insertWords = 0;
				deleteLenght = 0;
				deleteWords = 0;
			}
		}
		br.close();
	}
	
	/**
	 * @param userID
	 * @return true/false
	 */
	private String isUserIDIsIP(String userID) {
		Pattern pattern = Pattern.compile(IPADDRESS_REGEX);
		Matcher matcher = pattern.matcher(userID);
		return matcher.matches() ? "true" : "false";
	}
	
	/**
	 * Saves the type.arff file
	 * @throws IOException
	 */
	private void saveArff() throws IOException {
		ArffSaver saver = new ArffSaver();
		saver.setInstances(data);
	
		saver.setFile(new File("data/" + type + ".arff"));
		saver.writeBatch();
		System.out.println(type + ".arff is saved.");
	}
}
