Wikipedia changelog classification
==================================

Wikipedia changelog classification project, written in Java for Machine Learning course at the University of Szeged.

### The problem


There is a train database with plenty of changelogs. Every changelog contains a header and body.

- Header: DOCID \t regular/vandalism \t userID \t timestamp \t comment \t title

- Body: I/D \t changes

Changelogs are separated with an empty line.
 
The **goal is to label** the unlabeled changelogs from the test file.
 
 ## Usage
 
 First you must install Weka or just download the Weka.jar file. In Eclipse add Weka.jar as an External JAR file. 
 
 Before running the application you must specify the path for train and test files as an argument.
 
 For example: _**java Main data/train data/test**_
 
 The train and test files are included in data folder.
