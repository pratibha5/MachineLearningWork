/*
 * ClusterPrediction.java
 *
 * Created on July 13, 2006, 12:04 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.probe.stats.structures.result;

import org.probe.stats.structures.learner.attribute.VHierarchyNode;
import org.probe.util.RuleList;

/**
 * 
 * @author Jonathan Lustgarten
 */
public class ClusterPrediction extends Prediction {
	private double[] distances;
	private String[] neighbors;

	/** Creates a new instance of ClusterPrediction */
	public ClusterPrediction(String d, Object target) {
		super(d, target);
		neighbors = new String[0];
		distances = new double[0];
	}

	public ClusterPrediction(RuleList rules, String d, Object observed,
			VHierarchyNode predicted, String[] neighborUsed, double[] distances) {
		super(rules, d, observed, predicted);
		neighbors = neighborUsed;
		this.distances = distances;
	}

	public ClusterPrediction(RuleList[] rules, String d, Object observed,
			VHierarchyNode predicted, String[] neighborUsed, double[] distances) {
		super(rules, d, observed, predicted);
		neighbors = neighborUsed;
		this.distances = distances;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(super.toString());
		for (int i = 0; i < neighbors.length - 1; i++)
			buf.append(neighbors[i] + ", ");
		buf.append(neighbors[neighbors.length - 1]);

		return buf.toString();
	}

	public String[] getNearestNeighbors() {
		return neighbors;
	}

	public double[] getDistances() {
		return distances;
	}
}