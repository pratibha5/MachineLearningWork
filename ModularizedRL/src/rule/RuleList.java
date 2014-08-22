/*
 * @(#)RuleList.java    1.2 2002/01/21
 */

package rule;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import structures.learner.attribute.*;
import rule.Rule;
import util.MathUtil;

/**
 * A list of rules, whose functionality includes reading/writing to a text file
 * 
 * @version 1.0 2000/03/20
 * @author Jeremy Ludwig
 * 
 * Edited to make use of new organization. Rule Import and Export should be
 * considered broken. Ideally the value hierarchies should be exported as well
 * and the rules, attributes, and value hierarchies should be rebuilt based on
 * the exported rules and hierarchies.
 * 
 * @version 1.1 2002/01/21
 * @author Will Bridewell
 * 
 * Added <code>RuleList(Collection)</code> and <code>getIdsString()</code>.
 * Compacted and formatted code and comments.
 * 
 * @version 1.2 2002/10/28
 * @author Philip Ganchev (philip@cs.pitt.edu)
 */

public class RuleList {
	public ArrayList<Rule> rules;
	
	public RuleList() {
		rules = new ArrayList<Rule>();
	}

	public RuleList(int initialCapacity) {
		rules = new ArrayList<Rule>(initialCapacity);
	}

	public RuleList(ArrayList<Rule> c) {
		if (c == null)
			return;
		// Add the rules, setting the rule indexes
		rules = new ArrayList<Rule>(c.size());
		for (Rule r : c) {
			// FIXME: Uh-oh, we are modifying the original rules!
			r.setIndex(rules.size());
			rules.add(r);
		}
	}

	public RuleList(String fileName) {
		try {
			FileReader reader = new FileReader(fileName);
		} catch (FileNotFoundException x) {
			x.printStackTrace();
		}
		//while (reader.)
	}
	
	public Rule get(int index) {
		return rules.get(index);
	}

	public void add(Rule r) {
		rules.add(r);
	}

	/**
	 * @return the string of indeces of the rules in this list, separated by
	 *         commas
	 */
	public String getIndecesString() {

		StringBuffer ids = new StringBuffer();
		for (int i = 0; i < rules.size() - 1; i++) {
			ids.append((rules.get(i)).getIndex());
			ids.append(", ");
		}
		if (rules.size() > 0) {
			ids.append((rules.get(rules.size() - 1)).getIndex());
		}
		return ids.toString();
	}

	public int size() {
		return rules.size();
	}

	// We can't use AttributeList here unless we have the program state.
	public ArrayList<String> getAttributes() {
		java.util.HashSet atts = new java.util.HashSet(1 + (size() * 3));
		for (java.util.Iterator j = rules.iterator(); j.hasNext();) {
			Rule rule = (Rule) j.next();
			atts.addAll(rule.getLhs().getAttributes());
		}
		return new ArrayList<String>(atts);
	}
	/**
	 * Creates a shallow copy of this rule list. @return a copy of this
	 * rule list, whose rules have <code>lhs</code> fields that refer to the
	 * same objects as this rule list's rules' <code>lhs</code> fields. The
	 * other fields of the rules refer to the fields of the rules in this list.
	 */
	public Object clone() {
		RuleList r = new RuleList();
		r.rules = rules;
		return r;
	}

	public void replace(AttributeList atts) {
		for (int i = 0; i < size(); i++) {
			Conjunction l = rules.get(i).getLhs();
			for (int j = 0; j < l.size(); j++) {
				Conjunct c = l.getConjunct(j);
				for (int k = 0; k < atts.size(); k++) {
					LearnerAttribute a = atts.getAttribute(k);
					if (a.getName().equals(c.getAttributeName())) {
						c.setAttribute(a);
					}
				}
			}
		}
	}

	public void reIndex(int[] newIndeces) {
		ArrayList atts = getAttributes();
		for (int i = 0; i < atts.size(); i++) {
			LearnerAttribute a = (LearnerAttribute) atts.get(i);
			a.setIndex(newIndeces[a.getIndex()]);
		}
	}

	@SuppressWarnings("unchecked")
	public String toString() {
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < size(); i++) {
			Rule r = rules.get(i);
			if (r.getIndex() < 0)
				buff.append(i + ". ");
			buff.append(r.toString() + "\n\n");
		}
		ArrayList<String> attsUsed = getAttributes();
		buff.append("Attributes used (" +  attsUsed.size() + "):\n");
		if (attsUsed.size() > 0) {
			buff.append(attsUsed.get(0));
			for (int i = 1; i < attsUsed.size(); i++)
				buff.append(", " + attsUsed.get(i));
		}
		buff.append("\n");
		return buff.toString();
	}

	public void printXML(PrintStream out) {
		StringBuffer buf = new StringBuffer();
		buf.append("<?xml version=\"1.0\"?>\n");
		buf.append("<RuleList date=\"");
		java.text.DateFormat df = new java.text.SimpleDateFormat(
				"MM/dd/yyyy HH:mm:ss");
		buf.append(df.format(new java.util.Date()));
		buf.append("\" size=\"" + rules.size() + "\">\n");
		for (int i = 0; i < rules.size(); i++) {
			buf.append("\t<Rule index=\"" + (i + 1) + "\" ");
			Rule r = get(i);
			Conjunction lhs = r.getLhs();
			buf.append(" numAttsLHS=\"" + lhs.size() + "\">\n");
			buf.append("\t\t<LeftHandSide size=\"" + lhs.size() + "\">\n");
			for (int c = 0; c < lhs.size(); c++) {
				Conjunct cnj = (Conjunct) lhs.get(c);
				buf.append("\t\t\t<Conjunct index=\"" + c + "\">\n");
				buf.append("\t\t\t\t<Attribute name=\"" + cnj.getAttributeName() + "\">\n");
				buf.append("\t\t\t\t\t<Value name=\""+ cnj.getValue().toString() + "\">\n");
				buf.append("\t\t\t\t</Attribute>\n");
				buf.append("\t\t\t</Conjunct>\n");
			}
			buf.append("\t\t</LeftHandSide>\n");
			buf.append("\t\t<RightHandSide>\n");
			buf.append("\t\t</RightHandSide>\n");
			buf.append("\t</Rule>\n");
		}
		buf.append("\t</RuleList>\n");
	}
	
	public void addAll(RuleList rules) {
		this.rules.addAll(rules.rules);
	}

	public int indexOf(Rule r) {
		return rules.indexOf(r);
	}

	public ArrayList<Rule> toArrayList() {
		ArrayList<Rule> nrules = new ArrayList<Rule>(rules.size());
		for (Rule r : rules) {
			// TODO: Why clone??
			nrules.add(((Rule) r.clone()));
		}
		return nrules;
	}
}