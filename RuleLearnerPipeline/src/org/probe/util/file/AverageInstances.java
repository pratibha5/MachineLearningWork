package util.file;

import java.util.ArrayList;
import java.util.HashMap;

import data.dataset.*;
import util.Arrays;

public class AverageInstances {
	private DataModel data;
	private int iRepAtt;

	/* Stores which instances are replicates of each other, that is represent the 
	 * same patient, etc. */
	private HashMap<String, int[]> samplesMap; 

	public AverageInstances(DataModel set, int iRepCol) {
		data = set;
		samplesMap = new HashMap<String, int[]>(data.numInstances());
		this.iRepAtt = iRepCol;
	}

	private HashMap<String, int[]> populateReplicates(double[] sampls, Attribute repAtt) throws ValueNotFoundException {
		//HashMap samplesMap = new HashMap<String, int[]>(data.numInstances());
		for (int smp = 0; smp < sampls.length; smp++) {
			String smpName = null;
			smpName = repAtt.getStringValue(sampls[smp]);
			if (samplesMap.containsKey(smpName)) {
				int[] ixs = Arrays.append(samplesMap.get(smpName), smp);
				samplesMap.put(smpName, ixs);
			} else {
				int[] nints = new int[1];
				nints[0] = smp;
				samplesMap.put(smpName, nints);
			}			
		}		
		return samplesMap;
	}
	
	/**
	 * Find and record which instances are "replicates" of each other, that is, 
	 * they represent samples from the same patient, etc.  
	 * @throws ValueNotFoundException 
	 */
	private void linkReplicates(double[] sampls, Attribute idAtt) throws ValueNotFoundException {
		for (int smp = 0; smp < sampls.length; smp++) {
			String smpName = "";
			smpName = idAtt.getStringValue(sampls[smp]);

			String baseName = smpName;
			if (smpName.lastIndexOf("(") > 0) {
				// This instance is a replicate; remove the part of the name after "("
				baseName = smpName.substring(0, smpName.lastIndexOf("(")).trim();
			}
			if (samplesMap.containsKey(baseName)) {
				int[] nints = Arrays.append(samplesMap.get(baseName), smp);
				samplesMap.put(baseName, nints);
			} else {
				int[] nints = { smp };
				samplesMap.put(baseName, nints);
			}
		}
	}

	public DataModel averageReplicates() throws AttributeDoesNotExistException, ValueNotFoundException, IncompatibleDatatypeException {
			double[][] attValsOld = data.attributeValues();
			Attribute idAtt2;
			String[] sampleNames;	// Names of the instances after the averaging
		
			// Collect the non-rep instance names
			if (iRepAtt < 0) {
				Attribute idAtt = data.idAttribute();
				linkReplicates(attValsOld[idAtt.position()], idAtt);
				sampleNames = samplesMap.keySet().toArray(new String[0]);
				idAtt2 = new Attribute(idAtt.name(), idAtt.position(), sampleNames, data);
			} else {
				Attribute repAtt = data.attribute(data.iRepAtt);
				samplesMap = populateReplicates(attValsOld[repAtt.position()], repAtt);
				sampleNames = samplesMap.keySet().toArray(new String[0]);
				idAtt2 = new Attribute("#" + repAtt.name(), repAtt.position(), 
						sampleNames, data);
			}
			idAtt2.setIsId(true);
			
			// Go through the non-rep instances, averaging their replicates
			double[][] attValsNew = new double[attValsOld.length][sampleNames.length];
			for (int iInst = 0; iInst < sampleNames.length; iInst++) {	// For each non-replicate instance...
				int[] curRepIdIxs = samplesMap.get(sampleNames[iInst]);
				for (int iAtt = 0; iAtt < attValsOld.length; iAtt++) {			// For each attribute...
					if (iAtt == data.getIdAttIndex()) {
						attValsNew[iAtt][iInst] = idAtt2.getRepresentation(sampleNames[iInst]);
					} else if (curRepIdIxs.length == 1) {
						attValsNew[iAtt][iInst] = attValsOld[iAtt][curRepIdIxs[0]];
					} else {
						Attribute currAtt = data.attribute(iAtt);
						// If it has continuous values, average them
						if (currAtt.hasContinuousValues()) {
							double nval = 0;
							for (int s = 0; s < curRepIdIxs.length; s++)
								nval += attValsOld[iAtt][curRepIdIxs[s]];
							attValsNew[iAtt][iInst] = nval / (double) curRepIdIxs.length;
						} else {
							// It has categorical values; check that they are all the same 
							double val0 = attValsOld[iAtt][curRepIdIxs[0]];
							for (int s = 0; s < curRepIdIxs.length; s++) {
								double valj = attValsOld[iAtt][curRepIdIxs[s]];
								if (valj != val0) { 
									throw new data.dataset.IncompatibleDatatypeException(
											"The replicates instance " + sampleNames[iInst]
											+ ", attribute" + data.attribute(iAtt).name()
											+ " have different categorical values: " + val0 + " and " + valj);
								} else {
									// Take the first value
									attValsNew[iAtt][iInst] = attValsOld[iAtt][curRepIdIxs[0]];
								}
							}
						}
					}
				}
			}

			// Collect the new values and recalculate attribute info
			ArrayList<Attribute> newAtts = new ArrayList<Attribute>(
					attValsNew.length);
			// For each attribute in the data set...
			for (int i = 0; i < attValsOld.length; i++) {
					// Add the averaged ID attribute in place of the old ID attribute
				if (i == data.getIdAttIndex() && i != data.iRepAtt) {
					newAtts.add(idAtt2);
					continue;
				}
				Attribute att = data.attribute(i);
				if (att.hasContinuousValues()) {		// Continuous attribute
					att.recalculateInfo(attValsNew[i]);
					newAtts.add(att);
				} else {											// Discrete attribute
					// Collect the string values of the attribute for all the non-rep instances
					String[] strVals = new String[attValsNew[i].length];
					for (int j = 0; j < attValsNew[i].length; j++) {
						strVals[j] = att.getValue(attValsNew[i][j]);
					}
					Attribute newAtt = new Attribute(att.name(), att.position(), 
																	strVals, data);
					for (int j = 0; j < strVals.length; j++)
						attValsNew[i][j] = newAtt.getRepresentation(strVals[j]);
					newAtt.setIsClass(att.isClass());	
					newAtts.add(newAtt);
				}
			}

			// Finally, create the DataModel object representing the averaged data
			DataModel nd = new DataModel();
			nd.setAttributes(newAtts);
			nd.setAttributeValues(attValsNew);
			nd.setFileName(util.Util.appendFileNameSuffix(data.getFileName(), "av"));
			nd.setSeperator(data.getSeperator());
			if (data.getIdAttIndex() != data.iRepAtt) {
				nd.removeAttributes(new int[] {data.iRepAtt}, false);
			}
			return nd;
	}
}