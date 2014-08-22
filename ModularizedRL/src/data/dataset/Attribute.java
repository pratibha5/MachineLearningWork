package data.dataset;

import java.text.DecimalFormat;
//import java.text.NumberFormat;
//import java.util.ArrayList;
import java.util.HashMap;

import structures.learner.attribute.DiscreteNode;
import structures.learner.attribute.IntervalNode;
import structures.learner.attribute.LearnerAttribute;
import structures.learner.attribute.LearnerAttributeType;
import structures.learner.attribute.VHierarchyNode;
import util.Arrays;
import util.MathUtil;

import weka.core.FastVector;

public class Attribute implements Cloneable {
	private String name;
	private HashMap<String, Double> valToRep;
	private HashMap<Double, String> repToVal;
	private boolean isContinuous;
	private boolean wasContinuous;
	private double minVal, maxVal, avgVal, stdVal;
	private double[] cutPoints;
	private int position;
	private double[] dists;
	private boolean isClassAtt;
	private boolean isIdAtt;
	private Dataset refDataset;
	private LearnerAttribute la;
	private int dblInd;

	//public Attribute() {
	//	this("", false, null);
	//}

	public Attribute(String name, Dataset refDataset) {
		this(name, false, refDataset);
	}

	public Attribute(String name, boolean isCont, Dataset refData) {
		this.name = name;
		isContinuous = isCont;
		wasContinuous = false;
		minVal = maxVal = avgVal = stdVal = -1;
		position = -1;
		isClassAtt = false;
		isIdAtt = false;
		refDataset = refData;
		dblInd = -1;
	}

	public Attribute(String name, int pos, double[] values, Dataset refData) {
		this.name = name;
		refDataset = refData;
		position = pos;
		dblInd = position;
		isContinuous = true;
		wasContinuous = false;
		minVal = Double.POSITIVE_INFINITY;
		maxVal = Double.NEGATIVE_INFINITY;
		avgVal = 0;
		for (int i = 0; i < values.length; i++) {
			if (values[i] < minVal)
				minVal = values[i];
			if (values[i] > maxVal)
				maxVal = values[i];
			avgVal += values[i];
		}
		avgVal = avgVal / ((double) values.length);
		stdVal = 0;
		for (int j = 0; j < values.length; j++)
			stdVal += Math.pow(avgVal - values[j], 2.0);
		stdVal = stdVal / ((double) values.length);
		stdVal = Math.sqrt(stdVal);
	}

	public Attribute(String name, int pos, String[] values, Dataset refData) {
		this.name = name;
		refDataset = refData;
		position = pos;
		dblInd = -1;
		isContinuous = false;
		wasContinuous = false;
		minVal = maxVal = avgVal = stdVal = -1;
		cutPoints = null;
		//position = -1;
		valToRep = new HashMap<String, Double>(values.length / 2);
		repToVal = new HashMap<Double, String>(values.length / 2);
		dists = new double[0];
		for (int i = 0; i < values.length; i++) {
			if (valToRep.containsKey(values[i])) {
				dists[((int) valToRep.get(values[i]).doubleValue())] += 1;
				continue;
			}
			valToRep.put(values[i], new Double(valToRep.size()));
			repToVal.put(valToRep.get(values[i]), values[i]);
			double[] ndist = new double[dists.length + 1];
			System.arraycopy(dists, 0, ndist, 0, dists.length);
			ndist[ndist.length - 1] = 1;
			dists = ndist;
			isClassAtt = false;
		}
		isIdAtt = false;
		la = null;
	}

	public Attribute(String name, int pos, HashMap<String, Double> VTR,
			HashMap<Double, String> RTV, double[] vals, Dataset refData) {
		this.name = name;
		refDataset = refData;
		position = pos;
		isContinuous = false;
		wasContinuous = false;
		minVal = maxVal = avgVal = stdVal = -1;
		//cutPts = null;
		//position = -1;
		valToRep = VTR;
		repToVal = RTV;
		dists = new double[valToRep.size()];
		for (int i = 0; i < vals.length; i++)
			dists[(int) vals[i]] += 1;
		isClassAtt = false;
		isIdAtt = false;
		dblInd = -1;
		//la = null;
	}

	public Object clone() {
		Attribute clone = new Attribute(this.name, refDataset);
		clone.isClassAtt = isClassAtt;
		clone.isIdAtt = isIdAtt;
		clone.isContinuous = isContinuous;
		clone.wasContinuous = wasContinuous;
		clone.dblInd = dblInd;
		clone.position = position;
		if (isContinuous || wasContinuous) {
			clone.minVal = minVal;
			clone.maxVal = maxVal;
			clone.avgVal = avgVal;
			clone.stdVal = stdVal;
		} else {
			clone.minVal = clone.maxVal = clone.avgVal = clone.stdVal = -1;
		}
		if (wasContinuous) {
			clone.cutPoints = new double[cutPoints.length];
			System.arraycopy(cutPoints, 0, clone.cutPoints, 0, cutPoints.length);
		} else
			clone.cutPoints = null;
		if (valToRep != null) {
			clone.valToRep = new HashMap<String, Double>(valToRep.size());
			clone.repToVal = new HashMap<Double, String>(repToVal.size());
			for (int i = 0; i < valToRep.size(); i++) {
				String currVal = repToVal.get(new Double(i));
				clone.valToRep.put(currVal.toString(), new Double(i));
				clone.repToVal.put(clone.valToRep.get(currVal), currVal.toString());
			}
			clone.dists = new double[dists.length];
			System.arraycopy(dists, 0, clone.dists, 0, dists.length);
		} else {
			clone.valToRep = null;
			clone.repToVal = null;
		}
		return clone;
	}

	public void setDiscretization(double[] vals) {
		if (isContinuous || wasContinuous) {
			valToRep = new HashMap<String, Double>(vals.length + 1);
			repToVal = new HashMap<Double, String>(vals.length + 1);
			if (vals == null || vals.length == 0) {
				cutPoints = new double[0];
				String v = "-inf..inf";
				valToRep.put(v, new Double(0));
				repToVal.put(valToRep.get(v), v);
			} else {
				cutPoints = new double[vals.length];
				System.arraycopy(vals, 0, cutPoints, 0, vals.length);
				cutPoints = MathUtil.sort(cutPoints);
				DecimalFormat nf = new DecimalFormat("#.####");
				String nginf = new String("-inf.." + nf.format(cutPoints[0]));
				valToRep.put(nginf, new Double(0));
				repToVal.put(valToRep.get(nginf), nginf);
				for (int i = 0; i < cutPoints.length; i++) {
					if (i == cutPoints.length - 1) {
						String v = new String(nf.format(cutPoints[i]) + "..inf");
						valToRep.put(v, new Double(valToRep.size()));
						repToVal.put(valToRep.get(v), v);
					} else {
						String v = new String(nf.format(cutPoints[i]) + ".." + nf.format(cutPoints[i + 1]));
						valToRep.put(v, new Double(valToRep.size()));
						repToVal.put(valToRep.get(v), v);
					}
				}
			}
			wasContinuous = true;
			isContinuous = false;
			dists = Arrays.init(new double[repToVal.size() + 1], 0);
		}
	}

	public double getRepresentation(String val) {
		if (valToRep == null || valToRep.size() == 0)
			return -1.0;
		if (valToRep.containsKey(val))
			return (valToRep.get(val)).doubleValue();
		return -1.0;
	}

	/**
	 * Gets the string representing the discretized range containing the double 
	 * value
	 * 
	 * @param val
	 *            The double value
	 * @return The discretized String
	 * @throws ValueNotFoundException
	 */
	public String getValue(double val) throws ValueNotFoundException {
		if (wasContinuous) {
			if (cutPoints == null || cutPoints.length == 0)
				return repToVal.get(new Double(0));
			else {
				if (val < cutPoints[0])
					return repToVal.get(new Double(0));
				else {
					for (int i = 1; i < repToVal.size(); i++) {
						if (i < cutPoints.length && val < cutPoints[i])
							return repToVal.get(new Double(i));
						else
							break;
					}
					return repToVal.get(new Double(repToVal.size() - 1));
				}
			}
		} else if (isContinuous && !wasContinuous) {
			return Double.toString(val);
		} else {
			if (repToVal.containsKey(new Double(val))) {
				return repToVal.get(new Double(val));
			} else
				throw new ValueNotFoundException((new Double(val)).toString(),
						this.name);
		}
	}

	public int numValues() {
		return valToRep.size();
	}

	public void setPosition(int pos) {
		position = pos;
	}

	public weka.core.Attribute getWekaAttribute() {
		FastVector attVals;
		weka.core.Attribute wAtt;
		if (wasContinuous || !isContinuous) {
			attVals = new FastVector(valToRep.size());
			for (int i = 0; i < repToVal.size(); i++)
				attVals.addElement(repToVal.get((new Double(i)).doubleValue()));
			wAtt = new weka.core.Attribute(name, attVals);
		} else
			wAtt = new weka.core.Attribute(name);
		return wAtt;
	}

	public String name() {
		return name;
	}

	public int position() {
		return position;
	}

	public int doublePosition() {
		return hasContinuousValues() ? dblInd : -1;
	}

	public boolean hasContinuousValues() {
		return (wasContinuous || isContinuous);
	}

	public boolean wasContinuous() {
		return wasContinuous;
	}

	public double[] getDistribution() {
		return dists;
	}

	public void recalculateInfo(double[] vals) {
		if (!wasContinuous && !isContinuous) { // Is a discrete attribute
			dists = Arrays.init(new double[repToVal.size() + 1], 0);
			for (int i = 0; i < vals.length; i++) {
				dists[(int) vals[i]] += 1;
				dists[dists.length - 1] += 1;
			}
		} else if (!isContinuous) {	// Was continuous, is now discrete (was discretized)
			if (cutPoints == null || cutPoints.length == 0) {
				dists = new double[2];
				dists[0] = vals.length;
				dists[1] = vals.length;
			} else {	// There is at least one cut point
				double[] nvals = new double[vals.length];
				System.arraycopy(vals, 0, nvals, 0, vals.length);
				nvals = MathUtil.sort(nvals);
				int j = 0;
				for (int i = 0; i < nvals.length; i++) {
					if (j != cutPoints.length && cutPoints[j] < nvals[i])
						j += 1;
					dists[j] += 1;
					dists[dists.length - 1] += 1;
				}
			}
		} else {	// Was and still is continuous (was not discretized)
			double[] mmasd = MathUtil.getMinMaxMeanSD(vals);
			this.avgVal = mmasd[2];
			this.maxVal = mmasd[1];
			this.minVal = mmasd[0];
			this.stdVal = mmasd[3];
		}
	}

	public double counts() {
		return dists[dists.length - 1];
	}

	public double count(double val) throws ValueNotFoundException {
		if (repToVal.containsKey(val))
			return dists[(int) val];
		else
			throw new ValueNotFoundException(val, this.name);
	}

	public boolean isClass() {
		return isClassAtt;
	}

	public void setIsClass(boolean b) {
		isClassAtt = b;
		if (isIdAtt && b)
			isIdAtt = false;
	}

	public boolean isId() {
		return isIdAtt;
	}
	
	public void setIsId(boolean b) {
		isIdAtt = b;
		if (isClassAtt && b)
			isClassAtt = false;
	}

	@Override
	protected void finalize() throws Throwable {
		refDataset = null;
		cutPoints = null;
		repToVal = null;
		valToRep = null;
		super.finalize();
	}

	public VHierarchyNode hierarchy() throws AttributeDoesNotExistException {
		return refDataset.getHierarchy(position());
	}

	/**
	 * This method is used for looking up the representation values. If
	 * you want a discretized representation, see
	 * {@link Attribute#getValue(double)}
	 * 
	 * @param d The double value representation
	 * @return The string stored at indexed by key d
	 * @throws ValueNotFoundException
	 *             {@link Attribute#getValue(double)}
	 */
	public String getStringValue(double d) throws ValueNotFoundException {
		if (repToVal == null || d > repToVal.size() - 1 || d < 0)
			throw new ValueNotFoundException(d, name());
		return repToVal.get(d);
	}

	public VHierarchyNode genHierarchy() {
		VHierarchyNode vh = new VHierarchyNode(name(), "ROOT");
		if (hasContinuousValues()) {
			if (cutPoints == null || cutPoints.length == 0) {
				vh.addValue(new IntervalNode(name(), Double.NEGATIVE_INFINITY,
						Double.POSITIVE_INFINITY));
			} else {
				vh.addValue(new IntervalNode(name(), Double.NEGATIVE_INFINITY,
						cutPoints[0]));
				for (int i = 0; i < cutPoints.length - 1; i++)
					vh.addValue(new IntervalNode(name(), cutPoints[i],
							cutPoints[i + 1]));
				vh.addValue(new IntervalNode(name(), cutPoints[cutPoints.length - 1],
						Double.POSITIVE_INFINITY));
			}
		} else {
			for (int i = 0; i < repToVal.size(); i++)
				vh.addValue(new DiscreteNode(name(), repToVal.get(((new Double(
						i)).doubleValue()))));
		}
		return vh;
	}

	/**
	 * Returns the lowest value currently recorded in the dataset. It returns
	 * {@value Double#NaN} if it is not a continuous feature.
	 * 
	 * @return The minimum value recorded
	 */
	public double minimumValue() {
		if (hasContinuousValues())
			return minVal;
		return Double.NaN;
	}

	/**
	 * Returns the highest value currently recorded in the dataset. It returns
	 * {@value Double#NaN} if it is not a continuous feature.
	 * 
	 * @return The maximum value recorded
	 */
	public double maximumValue() {
		if (hasContinuousValues())
			return maxVal;
		return Double.NaN;
	}

	public double average() {
		if (hasContinuousValues())
			return avgVal;
		return Double.NaN;
	}

	public double stdev() {
		if (hasContinuousValues())
			return stdVal;
		return Double.NaN;
	}

	public void scale(double scale, double[] ds) {
		if (isContinuous) {
			for (int i = 0; i < ds.length; i++) {
				ds[i] *= scale;
			}
		}
	}
	
	public boolean hasValue(double val) {
		if (wasContinuous) {
			if (repToVal.containsKey(val))
				return true;
			else {
				try {
					getValue(val);
					return true;
				} catch (ValueNotFoundException e) {
					return false;
				}
			}
		} else {
			if (repToVal.containsKey(val))
				return true;
		}
		return false;
	}

	public boolean hasValue(String d) {
		if (this.valToRep.containsKey(d))
			return true;
		return false;
	}

	public void setReferenceDataset(Dataset dataset) {
		refDataset = dataset;
	}

	public void addValues(Attribute a) {
		HashMap<String, Double> nattToRep = a.valToRep;
		if (nattToRep != null && valToRep != null) {
			int numAdd = 0;
			for (String v : nattToRep.keySet()) {
				if (!valToRep.containsKey(v)) {
					repToVal.put(new Double(valToRep.size()), v);
					valToRep.put(v, new Double(valToRep.size()));
					numAdd++;
					try {
						refDataset.getHierarchy(position()).addValue(
								new DiscreteNode(name(), v));
					} catch (AttributeDoesNotExistException e) {
						try {
							refDataset.setHierarchy(position(), genHierarchy());
						} catch (AttributeDoesNotExistException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
			double[] ndists = Arrays.init(new double[dists.length
					+ numAdd], 0);
			System.arraycopy(dists, 0, ndists, 0, dists.length - 1);
			ndists[dists.length - 1] = 0;
			ndists[ndists.length - 1] = dists[dists.length - 1];
			dists = ndists;
		}
	}

	public double[] cutPoints() {
		return this.cutPoints;
	}

	public void removeDiscretization() {
		if (wasContinuous) {
			repToVal = null;
			valToRep = null;
			cutPoints = null;
			dists = null;
			wasContinuous = false;
			isContinuous = true;
		}
	}

	public LearnerAttribute genLearnerAttribute() {
		if (la == null) {
			la = new LearnerAttribute(name(), position(), (
					hasContinuousValues() ? LearnerAttributeType.Continuous
					: LearnerAttributeType.Discrete));
			try {
				la.setHierarchy(hierarchy());
			} catch (AttributeDoesNotExistException e) {
				e.printStackTrace();
				la.setHierarchy(null);
			}
		}
		return la;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(name() + ": "
				+ (hasContinuousValues() ? "Numeric\n" : "Discrete\n"));
		sb.append("Position: " + position() + "\n");
		if (hasContinuousValues()) {
			sb.append("Range: " + minVal + "-" + maxVal + "\n");
			sb.append("Average: " + avgVal + " +/- " + stdVal + "\n");
		} else
			sb.append("Number of unique values: " + (dists.length - 1) + "\n");
		return sb.toString();
	}

	public void setDoublePosition(int numDA) {
		dblInd = numDA;
	}
}