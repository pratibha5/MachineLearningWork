package org.probe.util;

//import java.util.ArrayList;
import java.io.*;

//import parameters.algorithm.WEKAParameter;
import org.probe.data.discretize.Discretizers;
import data.dataset.*;

//import corefiles.structures.data.dataset.attribute.*;
//import corefiles.structures.results.Predictions.BayesPrediction;
//import weka.classifiers.Evaluation;
//import weka.classifiers.evaluation.NominalPrediction;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class Util {
	public static String FILE_PATH_SEP = System.getProperty("file.separator");

	private static Instance createInstance(DataModel trainData, DataModel origData,
			FastVector wekaAtts, boolean uD, int sampleNum)
			throws AttributeDoesNotExistException, InstanceNotFoundException {
		Instance newInst = new Instance(wekaAtts.size());
		for (int i = 0; i < wekaAtts.size(); i++) {
			try {
				weka.core.Attribute wAtt = (Attribute) wekaAtts.elementAt(i);
				if (wAtt.name().indexOf("#") > -1) {
					System.err.println("ERROR!!! Sample attribute retained!");
					System.exit(300);
				}
				data.dataset.Attribute attr = trainData
						.attribute(wAtt.name());
				if (attr.hasContinuousValues()) {
					if (uD) {
						if (wAtt.indexOfValue(attr.getValue(trainData.attributeValue(
								sampleNum, attr.position()))) < 0) {
							wAtt.addStringValue(attr.getValue(trainData.attributeValue(sampleNum, attr.position())));
							System.err.println("Had TO add " 
									+ attr.getValue(trainData.attributeValue(sampleNum, attr.position())));
						}
						newInst.setValue((Attribute) wekaAtts.elementAt(i),
								wAtt.indexOfValue(attr.getValue(trainData
										.attributeValue(sampleNum, attr
												.position()))));
					} else
						newInst.setValue((Attribute) wekaAtts.elementAt(i), trainData
								.attributeValue(sampleNum, attr.position()));
				} else
					newInst.setValue((Attribute) wekaAtts.elementAt(i), trainData
							.attributeValue(sampleNum, attr.position()));
			} catch (Exception e) {
				System.err.println(e.getLocalizedMessage());
				e.printStackTrace();
				System.exit(1);
			}
		}
		return newInst;
	}

	/**
	 * @param train
	 * @param test
	 * @param uD
	 * @return
	 * @throws AttributeDoesNotExistException
	 * @throws InstanceNotFoundException
	 */
	public static Instances[] createInstances(DataModel train, DataModel test, boolean uD) 
				throws AttributeDoesNotExistException, InstanceNotFoundException {
		Instances[] nIs = new Instances[2];
		FastVector dataAtts = train.getWekaAttributes(uD);
		nIs[0] = new Instances(train.getFileName(), dataAtts, train.numInstances());
		for (int ts = 0; ts < train.numInstances(); ts++) {
			nIs[0].add(createInstance(train, train, dataAtts, uD, ts));
		}
		nIs[0].setClassIndex(dataAtts.size() - 1);
		// Test Set
		if (test != null && test.numInstances() != 0) {
			nIs[1] = new Instances(test.getFileName(), dataAtts, test.numInstances());
			for (int ts = 0; ts < test.numInstances(); ts++) {
				nIs[1].add(createInstance(test, train, dataAtts, uD, ts));
			}
			nIs[1].setClassIndex(dataAtts.size() - 1);
		} else
			nIs[1] = null;
		return nIs;
	}

	/**
	 * @param train
	 * @param uD
	 * @return
	 * @throws AttributeDoesNotExistException
	 * @throws InstanceNotFoundException
	 */
	public static Instances createInstances(DataModel train, boolean uD)
			throws AttributeDoesNotExistException, InstanceNotFoundException {
		Instances[] inst = createInstances(train, null, uD);
		return inst[0];
	}

	public static void discDataModels(DataModel trn, DataModel tst, int meth,
			double methVal) throws AttributeDoesNotExistException {
		DataModel[] sets = new DataModel[2];
		sets[0] = trn;
		sets[1] = tst;
		discDataModels(sets, meth, methVal);
	}

	public static void discDataModels(DataModel[] dats, int meth, double methVal) throws AttributeDoesNotExistException {
		if (dats[0].numContinuousAttributes() > 0) {
			Discretizers d = new Discretizers();
			d.setMehtod(meth, methVal);
			d.setData(dats[0]);
			double[][] pts = d.discretizeMDL();
			dats[0].setDiscretization(pts);
			for (int i = 1; i < dats.length; i++) {
				if (dats[i] != null) {
					dats[i].setDiscretization(pts, dats[0]);
					dats[i].removeTrivialAttributes();
				}
			}
			dats[0].removeTrivialAttributes();
		}
	}

	public static void discDataModelsWithoutTrim(DataModel[] dats, int meth,
			double methVal) throws AttributeDoesNotExistException {
		Discretizers d = new Discretizers();
		d.setMehtod(meth, methVal);
		d.setData(dats[0]);
		double[][] pts = d.discretizeMDL();
		dats[0].setDiscretization(pts);
		for (int i = 1; i < dats.length; i++)
			dats[i].setDiscretization(pts, dats[0]);
	}

	public static void discDataModel(DataModel dats, int meth, double methVal) {
		Discretizers d = new Discretizers();
		d.setMehtod(meth, methVal);
		d.setData(dats);
		double[][] pts = d.discretizeMDL();
		dats.setDiscretization(pts);
		dats.removeTrivialAttributes();
	}

	public static void discDataModelWithoutTrim(DataModel dats, int meth,
			double methVal) {
		Discretizers d = new Discretizers();
		d.setMehtod(meth, methVal);
		d.setData(dats);
		double[][] pts = d.discretizeMDL();
		dats.setDiscretization(pts);
	}

	public static Instances[] createInstances(DataModel train, DataModel test,
			boolean uD, int[] vals) throws AttributeDoesNotExistException,
			InstanceNotFoundException {
		Instances[] nIs = new Instances[2];
		FastVector dataAtts = train.getWekaAttributes(uD, vals);
		nIs[0] = new Instances(train.getFileName(), dataAtts, train
				.numInstances());
		for (int ts = 0; ts < train.numInstances(); ts++) {
			nIs[0].add(createInstance(train, train, dataAtts, uD, ts));
		}
		nIs[0].setClassIndex(dataAtts.size() - 1);
		// Test set
		if (test != null && test.numInstances() != 0) {
			nIs[1] = new Instances(test.getFileName(), dataAtts, test
					.numInstances());
			for (int ts = 0; ts < test.numInstances(); ts++) {
				nIs[1].add(createInstance(test, train, dataAtts, uD, ts));
			}
			nIs[1].setClassIndex(dataAtts.size() - 1);
		} else
			nIs[1] = null;
		return nIs;
	}
	
	public static String guessSeparator(String line) {
		int nCommas = 0;
		int nTabs = 0;
		for (int i = 0; i < line.length(); i++) {
			if (line.charAt(i) ==  ',') {
				nCommas++;
			} else if (line.charAt(i) == '	') {
				nTabs++;
			}
		}
		return (nTabs < nCommas) ? "," : "	"; 
	}	

	public static String guessSeparator(File inFile) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inFile));
			String line = reader.readLine();
			return guessSeparator(line);
		} catch (IOException x) {
			x.printStackTrace(System.err);
		}
		return null;
	}

	public static String trimDirName(String dirName) {
		String newDirName = dirName.trim();
		while (newDirName.endsWith(FILE_PATH_SEP)) {
			newDirName = newDirName.substring(0, newDirName.length() - FILE_PATH_SEP.length());
		}
		return newDirName;
	}

	public static String appendFileNameSuffix(String fileName, String suffix) {
		if (suffix == null || suffix == "")
			return fileName;
		String[] name = fileNameStemAndSuffix(fileName, ".");
		//return name[0] + "." + suffix + "." + name[1];
		String ret = "";
		if (name[0] != null && ! name[0].equals(""))
			ret += name[0]+ ".";
		ret += suffix;
		if (name[1] != null && ! name[1].equals(""))
			ret += "." + name[1];
		return ret;
	}
	
	public static String replaceFileNameSuffix(String fileName, String suffix) {
		String[] name = fileNameStemAndSuffix(fileName, ".");
		return name[0] + "." + suffix;
	}
	
	public static String[] fileNameStemAndSuffix(String fileName, String delimiter) {
		String[] ret = new String[2];
		int i = fileName.lastIndexOf(delimiter);
		if (i >= 0) {
			ret[0] = fileName.substring(0, i);
			ret[1] = fileName.substring(i + 1, fileName.length());
		} else {
			ret[0] = fileName;
			ret[1] = "";
		}
		return ret;
	}
	
	public static String timeSince(long millis) {
		millis = System.currentTimeMillis() - millis;
		int seconds = (int) (millis / 1000);
		int days  = seconds / (60 * 60 * 24);
		seconds %= 60 * 60 * 24;
		int hours = seconds / (60 * 60);
		seconds %= 60 * 60;
		int minutes = seconds / 60;
		seconds %= 60;
		return 
				(days > 0 ? days + " d " : "")
				+ (hours > 0 ? hours + " h " : "")
				+ (minutes > 0 ? minutes + " m " : "")
				+ seconds + " s.";
	}
}