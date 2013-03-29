package com.nguyenmp.puushforjava.parser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import com.nguyenmp.puushforjava.things.Pool;

public class PoolParser {
	
	
	public static Pool[] getPoolsFromHtml(String htmlString) throws SAXNotRecognizedException, SAXNotSupportedException, TransformerFactoryConfigurationError, TransformerException, URISyntaxException {
		Document doc = XMLParser.getDocumentFromString(htmlString);
		
		Element body = (Element) XMLParser.getChildFromName(doc.getDocumentElement(), "body");
		Element content = XMLParser.getChildFromAttribute(body, "id", "content");
		Element contentDiv = XMLParser.getChildFromAttribute(content, "class", "content");
		Element poolsDiv = XMLParser.getChildFromAttribute(contentDiv, "id", "puush_pools");
		
		return getPoolsFromDiv(poolsDiv);
	}
	
	private static Pool[] getPoolsFromDiv(Element poolsDiv) throws TransformerException, URISyntaxException {
		NodeList children = poolsDiv.getChildNodes();
		
		List<Pool> list = new ArrayList<Pool>();
		
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node.getNodeName().equals("div") && !((Element) node).getAttribute("id").equals("puushpool-label")) {
				list.add(getPoolFromNode(node));
			}
		}
		
		return list.toArray(new Pool[] {});
	}

	private static Pool getPoolFromNode(Node node) throws TransformerException, URISyntaxException {
		String title = getTitleFromNode(node);
		int size = getSizeFromNode(node);
		String id = getIDFromNode(node);
		URI thumbnail = getThumbnailFromNode(node);
		
		return new Pool(title, id, size, thumbnail);
	}

	private static URI getThumbnailFromNode(Node node) throws URISyntaxException, TransformerException {
		String nodeContent = XMLParser.nodeToString(node);
		
		int start = nodeContent.indexOf("\"background-image: url(") + "\"background-image: url(".length();
		int end = nodeContent.indexOf(");\"/>", start);
		
		return new URI("http://puush.me" + nodeContent.substring(start, end));
	}

	private static String getIDFromNode(Node node) throws TransformerException {
		String nodeContent = XMLParser.nodeToString(node);
		
		int start = nodeContent.indexOf("\"/account/?pool=") + "\"/account/?pool=".length();
		int end = nodeContent.indexOf('\"', start);
		
		return nodeContent.substring(start, end);
	}

	private static int getSizeFromNode(Node node) throws TransformerException {
		String nodeContent = XMLParser.nodeToString(node);
		
		int start = nodeContent.lastIndexOf('(') + 1;
		int end = nodeContent.lastIndexOf(')');
		
		return Integer.parseInt(nodeContent.substring(start, end));
	}

	private static String getTitleFromNode(Node node) throws TransformerException {
		String nodeContent = XMLParser.nodeToString(node);
		
		int start = nodeContent.indexOf("title=\"") + "title=\"".length();
		int end = nodeContent.indexOf("\">", start);
		
		return nodeContent.substring(start, end);
	}
}