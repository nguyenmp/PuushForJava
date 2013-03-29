package com.nguyenmp.puushforjava.parser;

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

import com.nguyenmp.puushforjava.things.Account;
import com.nguyenmp.puushforjava.things.Application;
import com.nguyenmp.puushforjava.things.Pool;

public class SettingsParser {
	
	public static Account getAccountFromHtml(String htmlString) throws SAXNotRecognizedException, SAXNotSupportedException, TransformerFactoryConfigurationError, TransformerException {
		return AccountParser.getAccountFromHtml(htmlString);
	}
	
	public static String getAPIKeyFromHtml(String htmlString) throws SAXNotRecognizedException, SAXNotSupportedException, TransformerFactoryConfigurationError, TransformerException {
		int start = htmlString.indexOf("<tr><td class=\"label\">API Key:</td><td class=\"value\">") + "<tr><td class=\"label\">API Key:</td><td class=\"value\">".length();
		int end = htmlString.indexOf("</td><td class=\"action\"><a href=\"#reset-api-key\" rel=\"facebox\">reset</a></td></tr>");
		
		return htmlString.substring(start, end);
	}
	
	public static String getPasswordFromHtml(String htmlString) throws SAXNotRecognizedException, SAXNotSupportedException, TransformerFactoryConfigurationError, TransformerException {
		int start = htmlString.indexOf("<tr><td class=\"label\">Password:</td><td class=\"value\">") + "<tr><td class=\"label\">Password:</td><td class=\"value\">".length();
		int end = htmlString.indexOf("</td><td class=\"action\"><a href=\"#change-password\" rel=\"facebox\">change</a></td></tr>");
		
		return htmlString.substring(start, end);
	}
	
	public static Pool[] getPoolsFromHtml(String htmlString) throws SAXNotRecognizedException, SAXNotSupportedException, TransformerFactoryConfigurationError, TransformerException {
		Document doc = XMLParser.getDocumentFromString(htmlString);
		
		Element body = (Element) XMLParser.getChildFromName(doc.getDocumentElement(), "body");
		Element content = XMLParser.getChildFromAttribute(body, "id", "content");
		Element menubox = XMLParser.getChildFromAttribute(content, "id", "my-settings");
		Element mySettings = XMLParser.getChildFromAttribute(menubox, "class", "content");
		Element settingsWrapper = XMLParser.getChildFromAttribute(mySettings, "id", "settings-wrapper");
		Element myAccountPools = XMLParser.getChildFromAttribute(settingsWrapper, "id", "myaccount-pools");
		Element table = (Element) XMLParser.getChildFromName(myAccountPools, "table");
//		Element tBody = (Element) XMLParser.getChildFromName(table, "tbody");
		
		List<Pool> pools = new ArrayList<Pool>();
		
		NodeList trList = table.getChildNodes();
		for (int i = 0; i < trList.getLength(); i++) {
			Node node = trList.item(i);
			if (node.getNodeName().equals("tr") && getPoolFromTableRow(node) != null) pools.add(getPoolFromTableRow(node));
		}
		
		return pools.toArray(new Pool[] {});
	}
	
	private static Pool getPoolFromTableRow(Node node) throws TransformerException {
		String title = node.getTextContent();
		
		String asText = XMLParser.nodeToString(node);
		if (!asText.contains("type=\"radio\"")) return null;
		
		int start = asText.indexOf("value=\"") + "value=\"".length();
		int end = asText.indexOf("\" ", start);
		
		String poolID = asText.substring(start, end);
		
		return new Pool(title, poolID, 0, null);
	}
	
	private static Pool getSelectedPoolFromTableRow(Node node) throws TransformerException {
		String title = node.getTextContent();
		
		String asText = XMLParser.nodeToString(node);
		if (!asText.contains("checked=\"checked\"")) return null;
		
		int start = asText.indexOf("value=\"") + "value=\"".length();
		int end = asText.indexOf("\" ", start);
		
		String poolID = asText.substring(start, end);
		
		return new Pool(title, poolID, 0, null);
	}

	public static Pool getSelectedPoolFromHtml(String htmlString) throws TransformerException, SAXNotRecognizedException, SAXNotSupportedException, TransformerFactoryConfigurationError {
		Document doc = XMLParser.getDocumentFromString(htmlString);
		Element body = (Element) XMLParser.getChildFromName(doc.getDocumentElement(), "body");
		Element content = XMLParser.getChildFromAttribute(body, "id", "content");
		Element menubox = XMLParser.getChildFromAttribute(content, "id", "my-settings");
		Element mySettings = XMLParser.getChildFromAttribute(menubox, "class", "content");
		Element settingsWrapper = XMLParser.getChildFromAttribute(mySettings, "id", "settings-wrapper");
		Element myAccountPools = XMLParser.getChildFromAttribute(settingsWrapper, "id", "myaccount-pools");
		Element table = (Element) XMLParser.getChildFromName(myAccountPools, "table");
		
		Pool selectedPool = null;
		
		NodeList trList = table.getChildNodes();
		for (int i = 0; i < trList.getLength(); i++) {
			Pool pool = getSelectedPoolFromTableRow(trList.item(i));
			if (pool != null) {
				selectedPool = pool;
				break;
			}
		}
		
		return selectedPool;
	}
	
	public static Application[] getThirdPartySupport(String htmlString) throws SAXNotRecognizedException, SAXNotSupportedException, TransformerFactoryConfigurationError, TransformerException {
		Document doc = XMLParser.getDocumentFromString(htmlString);
		Element thirdParty = doc.getElementById("thirdparty");
		
		List<Application> list = new ArrayList<Application>();
		
		NodeList children = thirdParty.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			list.add(getApplicationFromTableRow(children.item(i)));
		}
		
		return list.toArray(new Application[] {});
	}

	private static Application getApplicationFromTableRow(Node item) throws TransformerException {
		String thumbnail = null;
		String title = null;
		String summary = null;
		String apiEndPoint = null;
		String instructions = null;
		String url = null;
		String customURL = null;
		
		NodeList children = item.getChildNodes();
		
		for (int index = 0; index < children.getLength(); index++) {
			Node node = children.item(index);
			if (node.getNodeName().equals("td")) {
				
				if (((Element) node).hasAttribute("class")) {
					String asString = XMLParser.nodeToString(node);
					int start = asString.indexOf("http://");
					int end = asString.indexOf("\"", start);
					
					url = asString.substring(start, end);
					
					start = asString.indexOf("target=\"_blank\">") + "target=\"_blank\">".length();
					end = asString.indexOf("</a>");
					title = asString.substring(start, end);
					
					start = asString.indexOf(" - ") + " - ".length();
					end = asString.indexOf("<", start);
					System.out.println(start + " " + end);
					
					summary = asString.substring(start, end);
					
					start = asString.indexOf("API Endpoint:</span> <span>") + "API Endpoint:</span> <span>".length();
					end = asString.indexOf("</span>", start);
					
					apiEndPoint = asString.substring(start, end);
				} else {
					NodeList imagelist = node.getChildNodes();
					for (int i = 0; i < imagelist.getLength(); i++) {
						Node image = imagelist.item(i);
						if (image.getNodeName().equals("img")) thumbnail = ((Element) image).getAttribute("src");
					}
				}
			}
		}
		
		return new Application(url, thumbnail, title, summary, apiEndPoint, instructions, customURL);
	}
}