package rule;

import util.Arrays;
import util.MathUtil;

public class MarkerToData {

	private String fromAttribute;

	private String value;

	private int[][] samplesAssociated;

	private MarkerToData parent;

	private int markerInd;

	public MarkerToData() {
		fromAttribute = null;
		value = null;
		samplesAssociated = null;
		parent = null;
		markerInd = -1;
	}

	public MarkerToData(String fromAtt, String val, int mI, int numClass) {
		fromAttribute = fromAtt;
		value = val;
		markerInd = mI;
		parent = null;
		samplesAssociated = new int[numClass][0];
	}

	/**
	 * @return the fromAttribute
	 */
	public String getFromAttribute() {
		return fromAttribute;
	}

	/**
	 * @param fromAttribute
	 *            the fromAttribute to set
	 */
	public void setFromAttribute(String fromAttribute) {
		this.fromAttribute = fromAttribute;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the samplesAssociated
	 */
	public int[][] getSamplesAssociated() {
		return samplesAssociated;
	}

	/**
	 * @param samplesAssociated
	 *            the samplesAssociated to set
	 */
	public void setSamplesAssociated(int[][] samplesAssociated) {
		this.samplesAssociated = samplesAssociated;
	}

	/**
	 * @return the parent
	 */
	public MarkerToData getParent() {
		return parent;
	}

	/**
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(MarkerToData parent) {
		this.parent = parent;
	}

	/**
	 * @return the markerInd
	 */
	public int getMarkerInd() {
		return markerInd;
	}

	/**
	 * @param markerInd
	 *            the markerInd to set
	 */
	public void setMarkerInd(int markerInd) {
		this.markerInd = markerInd;
	}

	public void addData(int sNum, int sClass) {
		samplesAssociated[sClass] = Arrays.append(samplesAssociated[sClass],
				sNum);
	}

	public void sortInst() {
		for (int i = 0; i < samplesAssociated.length; i++)
			samplesAssociated[i] = MathUtil.quickSort(samplesAssociated[i]);
	}
}
