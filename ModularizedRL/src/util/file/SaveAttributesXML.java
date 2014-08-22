/*
 * SaveAttributesXML.java
 *
 * Created on October 4, 2003, 12:30 AM
 */

package util.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import data.dataset.Attribute;
import data.dataset.AttributeDoesNotExistException;
import data.dataset.Dataset;
import structures.learner.attribute.*;


/**
 * 
 * The DTD is assumed, but can be made explicit within the document if validity
 * becomes an issue. The XML has been written to be well formed.
 * 
 * The associated DTD is: <!ELEMENT AttributeList (Attribute+)> <!ATTLIST
 * AttributeList dataset (#PCDATA) #OPTIONAL date (#PCDATA) #OPTIONAL> <!ELEMENT
 * Attribute (Tags?, Use, PerRule, Hierarchy)> <!ATTLIST Attribute name CDATA
 * #REQUIRED> <!ATTLIST Attribute type CDATA #REQUIRED> <!ELEMENT Tags
 * (#PCDATA)> <!ELEMENT Use (#PCDATA)> <!ELEMENT PerRule (#PCDATA)> <!ELEMENT
 * Hierarchy (InternalNode | Leaf | CLeaf)+> <!ELEMENT InternalNode
 * (InternalNode | CLeaf | Leaf)+> <!ATTLIST InternalNode name CDATA #REQUIRED>
 * <!ELEMENT Leaf (#PCDATA)> <!ELEMENT CLeaf (LBound, UBound)> <!ELEMENT LBound
 * (#PCDATA)> <!ELEMENT UBound (#PCDATA)>
 * 
 * 
 * @author will
 */
public class SaveAttributesXML {
	private BufferedWriter out;
	private Dataset data;
	private String dataset;

	/** Creates a new instance of SaveAttributesXML */
	// Mirrors the XML format given in ImportAttributesXML.java
	public SaveAttributesXML(Dataset d, File f, String dsname)
			throws IOException {
		// Open the file and do basic checks before attempting to output
		// Throws an IOException whenever the file cannot be opened.
		out = new BufferedWriter(new FileWriter(f));
		dataset = dsname;
		data = d;
	}

	public void save() throws IOException {
		StringBuffer buf = new StringBuffer();
		// There are other ways to build an XML document that don't require
		// you to work directly with the buffer (e.g., DocumentBuilder), but
		// this works just fine.
		buf.append("<?xml version=\"1.0\"?>\n");
		buf.append("<AttributeList size=\"" + data.numAttributes()
				+ "\" from=\"" + dataset + "\" date=\"");
		java.text.DateFormat df = new java.text.SimpleDateFormat(
				"MM/dd/yyyy HH:mm:ss");
		buf.append(df.format(new java.util.Date()));
		buf.append("\">\n");
		for (int i = 0; i < data.numAttributes(); i++) {
			Attribute att = null;
			try {
				att = data.attribute(i);
			} catch (AttributeDoesNotExistException e1) {
				System.err.println(e1.getLocalizedMessage());
				e1.printStackTrace();
				continue;
			}
			if (att.hasContinuousValues() && !att.wasContinuous())
				continue;
			VHierarchyNode avh = null;
			try {
				avh = att.hierarchy();
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			// <Attribute name="cdata" type="cdata">
			buf.append("<Attribute name=\"");
			buf.append(att.name());
			buf.append("\" numNodes=\"");
			buf.append(avh.numValues());
			buf.append("\" type=\"");
			buf.append((att.wasContinuous() ? "continuous" : "discrete"));
			buf.append("\">\n");

			// <Tags> cdata <\Tags>
			/*
			 * if (!currentAttribute.getTagString().trim().equals("")) {
			 * xmlBuffer.append("\t<Tags>");
			 * xmlBuffer.append(currentAttribute.getTagString());
			 * xmlBuffer.append("</Tags>\n"); }
			 */

			// <Use> cdata <\Use>
			buf.append("\t<Use>");
			buf.append((att.isId() ? "identification"
					: (att.isClass() ? "output" : "input")));
			buf.append("</Use>\n");

			buf.append("\t<PerRule>");
			buf.append(1);
			buf.append("</PerRule>\n");
			saveHierarchy(avh, buf);
			buf.append("</Attribute>\n\n");
			// finish the current attribute

		}
		buf.append("</AttributeList>\n");
		// Throws an IOException if writing went haywire.
		try {
			out.write(buf.toString());
		} finally {
			if (out != null) {
				out.flush();
				out.close();
			}
		}
	}

	private void saveHierarchy(VHierarchyNode currentHierarchy,
			StringBuffer xmlBuffer) {

		xmlBuffer.append("<Hierarchy>\n");
		// recursively enumerate through children
		saveChildren(currentHierarchy.top(), xmlBuffer);
		xmlBuffer.append("</Hierarchy>\n");
	}

	private String getTabs(VHierarchyNode vn) {
		StringBuffer nb = new StringBuffer();
		for (int i = 0; i < vn.getLevel() + 1; i++)
			nb.append("\t");
		return nb.toString();
	}

	private void saveChildren(VHierarchyNode vhn, StringBuffer xmlBuffer) {
		java.util.Enumeration e = vhn.children();
		while (e.hasMoreElements()) {
			VHierarchyNode child = (VHierarchyNode) e.nextElement();
			xmlBuffer.append(getTabs(vhn) + "<hierarchyNode numChildren=\""
					+ child.getChildCount() + "\" type=\"");
			if (child instanceof DiscreteNode) {
				xmlBuffer.append("DiscreteNode\" ");
				xmlBuffer.append("value=\"");
				xmlBuffer.append(child.getName());
				xmlBuffer.append("\">\n");
			} else if (child instanceof IntervalNode) {
				xmlBuffer.append("IntervalNode\">\n");
				xmlBuffer.append(getTabs(child) + "<lval>");
				IntervalNode inNode = (IntervalNode) child;
				xmlBuffer.append(inNode.begin);
				xmlBuffer.append("</lval>\n");
				xmlBuffer.append(getTabs(child) + "<rval>");
				xmlBuffer.append(inNode.end);
				xmlBuffer.append("</rval>\n");
			} else if (child instanceof HNode) {
				xmlBuffer.append("HNode\" ");
				xmlBuffer.append("value=\"");
				xmlBuffer.append(child.getName());
				xmlBuffer.append("\">\n");
			}
			if (child.getChildCount() > 0) {
				xmlBuffer.append(getTabs(child) + "<children>\n");
				saveChildren(child, xmlBuffer);
				xmlBuffer.append(getTabs(child) + "</children>\n");
			}
			xmlBuffer.append(getTabs(vhn) + "</hierarchyNode>\n");
		}
	}
}
