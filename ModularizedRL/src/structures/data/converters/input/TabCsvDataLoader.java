package structures.data.converters.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import data.dataset.Attribute;
import data.dataset.Dataset;
import data.dataset.IncompatibleDatatypeException;
import structures.learner.attribute.VHierarchyNode;
import util.Util;
import util.file.ReadAttributeXML;

/**
 * This class is responsible for loading all comma- and tab-delimited files,
 * either transposed or not.
 * 
 * @author Jonathan Lustgarten
 */
public class TabCsvDataLoader implements LoadDataset {

	private boolean attsAsRows;
	private String inFileName;
	private ArrayList<Attribute> atts;
	private double[][] attVals;
	private HashMap<String, VHierarchyNode> attToHierarchs;
	private VHierarchyNode[] attHierarchs;

	public TabCsvDataLoader(String fileName) {
		this(fileName, false);
	}

	public TabCsvDataLoader(String fileName, boolean attsAsRows) {
		this.attsAsRows = attsAsRows;
		inFileName = fileName;
		attToHierarchs = new HashMap<String, VHierarchyNode>();
		attHierarchs = new VHierarchyNode[0];
	}

	public TabCsvDataLoader(String fileName, String attributeFile) {
		if (attributeFile != null) {
			System.out.println("Loading attribute file '" + attributeFile + "'");
			attToHierarchs = ReadAttributeXML.readAttributeXMLFile(new File(
					attributeFile));
		}
		inFileName = fileName;
		attsAsRows = false;
		attHierarchs = new VHierarchyNode[0];
	}

	private StringBuffer[] extendBuffs(StringBuffer[] curr, String[] arr,
			int currCol) {
		StringBuffer[] nbf = new StringBuffer[arr.length];
		System.arraycopy(curr, 0, nbf, 0, curr.length);
		for (int i = curr.length; i < nbf.length; i++)
			nbf[i] = new StringBuffer();
		for (int s = 0; s < arr.length; s++) {
			if (nbf[s].length() > 0) {
				nbf[s].append("\t");
				nbf[s].append(arr[s]);
			} else if (currCol != 0) {
				for (int i = 0; i < currCol; i++)
					nbf[s].append("\t");
				nbf[s].append(arr[s]);
			} else
				nbf[s].append(arr[s]);
		}
		return nbf;

	}

	private StringBuffer[] transposeData(String[] input, String sep) throws IncompatibleDatatypeException {
		StringBuffer[] result = new StringBuffer[1];
		result[0] = new StringBuffer();
		int nCols = -1;
		for (int s = 0; s < input.length; s++) {
			String[] arr = input[s].split(sep);
			if (nCols >= 0&& nCols != arr.length) {
				throw new IncompatibleDatatypeException(
						"File " + inFileName + ": Wrong number of columns on row " 
						+ s + ". Found " + arr.length + " expected " + nCols);
			}
			if (arr.length > result.length)
				result = extendBuffs(result, arr, s);
			else {
				for (int i = 0; i < arr.length; i++) {
					result[i].append(sep);
					result[i].append(arr[i]);
				}
			}
		}
		for (int i = 0; i < result.length; i++)
			result[i].trimToSize();
		return result;
	}

	private String[] readRows() throws FileNotFoundException, IOException {
		System.out.print("Lines: ");
		BufferedReader reader = new BufferedReader(new FileReader(inFileName));
		ArrayList<String> lines = new ArrayList<String>(20);
		String line = "";
		while ((line = reader.readLine()) != null) {
			if (line.length() < 5 || line.equalsIgnoreCase("\n"))
				continue;
			lines.add(line);
		}
		lines.trimToSize();
		String[] result = new String[lines.size()];
		result = lines.toArray(result);
		System.out.print(result.length + ". ");
		return result;
	}

	private boolean isContinuous(String[] vals) {
		for (int i = 1; i < vals.length; i++) {
			// Start from index 1 because index 0 is the attribute name
			try {
				Double.parseDouble(vals[i]);
			} catch (NumberFormatException e) {
				return false;
			}
		}
		return true;
		/*
		HashMap<Integer, Integer> tmpVals = new HashMap<Integer, Integer>();
		boolean isCont = false;
		int minDiff = (vals.length - 1) > 10 ? (vals.length - 1) / 10 : 3;
		for (int i = 1; i < vals.length && minDiff >= 0; i++) {
			if (vals[i].indexOf(".") > -1) {
				isCont = true;
				break;
			}
			try {
				Integer tI = Integer.parseInt(vals[i]);
				if (!tmpVals.containsKey(tI)) {
					minDiff--;
					tmpVals.put(tI, 1);
				}
			} catch (Exception e) {
				break;
			}
		}
		if (minDiff < 0)
			return true;
		return isCont;
		*/
	}

	private Dataset parse(StringBuffer[] attRows, String sep) throws IncompatibleDatatypeException {
		Dataset resultData = new Dataset();
		atts = new ArrayList<Attribute>(attRows.length);
		attVals = new double[attRows.length][0];
		attHierarchs = new VHierarchyNode[attRows.length];
		attToHierarchs = new HashMap<String, VHierarchyNode>(attRows.length);
		System.out.print("Parsing");
		int nCols = -1;
		for (int i = 0; i < attRows.length; i++) {			
			if (attRows.length < 10 || i % (attRows.length / 10) == 0)
				System.out.print(".");
			Attribute att = null;
			String[] sVals = attRows[i].toString().split(sep);
			// Ignore empty rows
			if (sVals == null || sVals.length < 1)
				continue;
			
			// Check that the number of columns is the same on all rows
			if (nCols < 0)
				nCols =sVals.length;
			else if (nCols != sVals.length)
				throw new IncompatibleDatatypeException(
						"File " + inFileName + ": Wrong number of columns on row " 
						+ i + ". Found " + sVals.length + " expected " + nCols);
			
			HashMap<String, Double> valsS2d = new HashMap<String, Double>();
			HashMap<Double, String> valsD2s = new HashMap<Double, String>();
			double[] dVals = new double[sVals.length - 1];
			boolean isCont = isContinuous(sVals);
			for (int j = 1; j < sVals.length; j++) {
				if (isCont && !sVals[0].startsWith("#") && !sVals[0].startsWith("@")) {
					dVals[j - 1] = Double.parseDouble(sVals[j]);
				} else {
					if (valsS2d.containsKey(sVals[j])) {
						dVals[j - 1] = valsS2d.get(sVals[j]).doubleValue();
					} else {
						dVals[j - 1] = valsS2d.size();
						valsS2d.put(sVals[j], new Double(dVals[j - 1]));
						valsD2s.put(new Double(dVals[j - 1]), sVals[j]);
					}
				}
			}
			if (isCont && !sVals[0].startsWith("#") && !sVals[0].startsWith("@"))
				att = new Attribute(sVals[0], i, dVals, resultData);
			else
				att = new Attribute(sVals[0], i, valsS2d, valsD2s, dVals, resultData);
			if (sVals[0].startsWith("#")) {
				att.setIsId(true);
			} else if (sVals[0].startsWith("@"))
				att.setIsClass(true);
			atts.add(att);
			attVals[i] = dVals;
			if (attToHierarchs != null && attToHierarchs.containsKey(sVals[0]))
				attHierarchs[i] = attToHierarchs.get(sVals[0]);
			//else if (!att.hasContinuousValues() && !att.isId()) {
			else if (!att.hasContinuousValues()) {
				VHierarchyNode vh = att.genHierarchy();
				attHierarchs[i] = vh;
			} else
				attHierarchs[i] = null;
		}
		//if (resultData.classAttIndex() < 0) {
		//	throw new structures.data.dataset.IncompatibleDatatypeException(
		//			"The data file " + inFileName
		//			+ " does not specify a class attribute");
		//}
		System.out.print(" " + atts.size() + " attributes, ");
		return resultData;
	}

	public Dataset loadData() throws Exception {
		return loadData("\t");
	}

	//@Override - causes compile errors with javac 1.5. javac bug?? PG2009
	public Dataset loadData(String sep) throws Exception {
		String[] rawData = readRows();
		if (sep == null || sep == "") {
			sep = Util.guessSeparator(rawData[0]);
		}
		System.out.print("Separator is '"+ sep + "'. ");
		StringBuffer[] attRows;
		if (!attsAsRows) {
			attRows = transposeData(rawData, sep);
		} else {
			attRows = new StringBuffer[rawData.length];
			for (int r = 0; r < attRows.length; r++) {
				attRows[r] = new StringBuffer(rawData[r].length());
				attRows[r].append(rawData[r]);
			}
		}
		Dataset data  = parse(attRows, sep);
		data.setAttributes(atts);
		data.setAttributeValues(attVals);
		System.out.print(data.numInstances() + " instances" );
		data.setHierarchies(attHierarchs);
		data.setFileName((new File(inFileName)).getName());
		data.setSeperator(sep);
		System.out.println(".");
		return data;
	}

	public static void main(String[] args) throws Exception {
		TabCsvDataLoader ld = new TabCsvDataLoader(args[0]);
		Dataset d = null;
		d = ld.loadData();
		System.out.println("Loaded dataset.");
		System.out.println("Testing cross-fold...");
		Dataset cfvT = d.trainCV(3, 0);
		Dataset cfvTst = d.testCV(3, 0);
	}
}
