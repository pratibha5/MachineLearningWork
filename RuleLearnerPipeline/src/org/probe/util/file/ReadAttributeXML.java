package org.probe.util.file;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.probe.stats.structures.learner.attribute.DiscreteNode;
import org.probe.stats.structures.learner.attribute.HNode;
import org.probe.stats.structures.learner.attribute.IntervalNode;
import org.probe.stats.structures.learner.attribute.VHierarchyNode;

// Cannot be resolved:
//import com.sun.org.apache.xerces.internal.dom.DeferredTextImpl;


public class ReadAttributeXML {

	private static VHierarchyNode getIntervalNode(Element nd, String attName) {
		NodeList vs = nd.getChildNodes();
		boolean element = false;
		double[] dvals = new double[2];
		int ind;
		for (int j = 0; j < vs.getLength(); j++) {
			Node vsn = vs.item(j);
			if (vsn instanceof Element) {
				if (vsn.getNodeName().equalsIgnoreCase("lval"))
					ind = 0;
				else if (vsn.getNodeName().equalsIgnoreCase("rval"))
					ind = 1;
				else
					continue;
				NodeList vls = vsn.getChildNodes();
				for (int k = 0; k < vls.getLength(); k++) {
					Node elval = vls.item(k);
					String val = elval.getNodeValue();
					if (val.equalsIgnoreCase("neginf"))
						dvals[ind] = Double.NEGATIVE_INFINITY;
					else if (val.equalsIgnoreCase("inf"))
						dvals[ind] = Double.POSITIVE_INFINITY;
					else
						dvals[ind] = Double.parseDouble(val);
				}
			}
		}
		VHierarchyNode elN = new IntervalNode(attName, dvals[0], dvals[1]);
		return elN;
	}

	private static VHierarchyNode getDiscreteNode(Element nd, String attName) {
		String val = nd.getAttribute("value");
		VHierarchyNode vhn = new DiscreteNode(attName, val);
		return vhn;
	}

	private static VHierarchyNode getHNode(Element nd, String attName) {
		String val = nd.getAttribute("value");
		VHierarchyNode vhn = new HNode(attName, val);
		return vhn;
	}

	private static void addChildren(VHierarchyNode parn, NodeList children,
			String attName) {
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i) instanceof Element) {
				Element chldNd = (Element) children.item(i);
				String typ = chldNd.getAttribute("type");
				int numChild = Integer.parseInt(chldNd
						.getAttribute("numChildren"));
				VHierarchyNode chlVH = null;
				if (typ.equalsIgnoreCase("IntervalNode")) {
					chlVH = getIntervalNode(chldNd, attName);
				} else if (typ.equalsIgnoreCase("HNode")) {
					chlVH = getHNode(chldNd, attName);
				} else if (typ.equalsIgnoreCase("DiscreteNode")) {
					chlVH = getDiscreteNode(chldNd, attName);
				} else
					continue;
				parn.addValue(chlVH);
				if (numChild > 0) {
					NodeList cnls = chldNd.getChildNodes();
					NodeList chnls = ((Element) cnls.item(0)).getChildNodes();
					addChildren(chlVH, chnls, attName);
				}
			}
		}
	}

	private static VHierarchyNode readAttribute(Element att, String AttName) {
		VHierarchyNode vnh = null;
		String attV = att.getAttribute("name");
		vnh = new VHierarchyNode(AttName, attV);
		NodeList nls = att.getChildNodes();
		for (int i = 0; i < nls.getLength(); i++) {
			if (nls.item(i) instanceof Element) {
				VHierarchyNode elNode = null;
				Element hn = (Element) nls.item(i);
				String typ = hn.getAttribute("type");
				int numChild = Integer.parseInt(hn.getAttribute("numChildren"));
				if (typ.equalsIgnoreCase("IntervalNode")) {
					elNode = getIntervalNode(hn, AttName);
				} else if (typ.equalsIgnoreCase("HNode")) {
					elNode = getHNode(hn, AttName);
				} else if (typ.equalsIgnoreCase("DiscreteNode")) {
					elNode = getDiscreteNode(hn, AttName);
				} else
					continue;
				vnh.addValue(elNode);
				if (numChild > 0) {
					NodeList cnls = hn.getChildNodes();
					NodeList chnls = null;
					for (int m = 0; m < cnls.getLength(); m++) {
						if (cnls.item(m) instanceof Element
								&& ((Element) cnls.item(m)).getNodeName()
										.equalsIgnoreCase("children")) {
							chnls = ((Element) cnls.item(m)).getChildNodes();
							break;
						}
					}
					addChildren(elNode, chnls, AttName);
				}
			}
		}
		return vnh;
	}

	public static HashMap<String, VHierarchyNode> readAttributeXMLFile(File f) {
		HashMap<String, VHierarchyNode> attToNode = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(f);
			Element attList = (Element) (doc
					.getElementsByTagName("AttributeList")).item(0);
			int numAtts = Integer.parseInt(attList.getAttribute("size"));
			attToNode = new HashMap<String, VHierarchyNode>(numAtts);
			NodeList ndlst = doc.getElementsByTagName("attribute");
			if (ndlst.getLength() != numAtts)
				throw new AttributeXMLSizeException("attribute", numAtts, ndlst
						.getLength());
			for (int s = 0; s < ndlst.getLength(); s++) {
				Element fstNode = (Element) ndlst.item(s);
				String attNm = fstNode.getAttribute("name");
				String type = fstNode.getAttribute("type");
				NodeList nls = fstNode.getElementsByTagName("hierarchy");
				VHierarchyNode newVH = readAttribute((Element) nls.item(0),
						attNm);
				attToNode.put(attNm, newVH);
			}
		} catch (AttributeXMLSizeException e) {
			System.err.println(e.getLocalizedMessage());
			e.printStackTrace();
			System.exit(1);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (SAXException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return attToNode;
	}

	public static void main(String[] args) {
		File f = new File("AttributeTemplate.xml");
		ReadAttributeXML.readAttributeXMLFile(f);
	}
}
