package com.nguyenmp.puushforjava.parser;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import com.nguyenmp.puushforjava.things.Account;

public class AccountParser {
	
	
	public static Account getAccountFromHtml(String htmlString) throws SAXNotRecognizedException, SAXNotSupportedException, TransformerFactoryConfigurationError, TransformerException {
		Document doc = XMLParser.getDocumentFromString(htmlString);
		
		Element body = (Element) XMLParser.getChildFromName(doc.getDocumentElement(), "body");
		Element content = XMLParser.getChildFromAttribute(body, "id", "content");
		Element menubox = XMLParser.getChildFromAttribute(content, "id", "my-menubox");
		Element metaheader = XMLParser.getChildFromAttribute(menubox, "id", "header-meta");
		
		return getAccountFromHeader(metaheader);
	}
	
	private static Account getAccountFromHeader(Element metaheader) throws TransformerException {
		String textContent = XMLParser.nodeToString(metaheader);
		textContent = textContent.substring(textContent.indexOf('>')+1, textContent.lastIndexOf('<'));
		
		String[] accountAttributes = textContent.split("<br clear=\"none\"/>");
		if (accountAttributes.length != 3) throw new NullPointerException("Attributes could not be parsed");
		
		String username = accountAttributes[0];
		String accountType = accountAttributes[1];
		String capacity = accountAttributes[2];
		
		String[] parsedCapacity = parseCapacityString(capacity);
		String currentCapacity = parsedCapacity[0];
		String maximumCapacity = parsedCapacity[1];
		String percentCapacity = parsedCapacity[2];
		
		Account account = new Account(username, currentCapacity, maximumCapacity, percentCapacity, accountType);
		
		return account;
	}
	
	private static String[] parseCapacityString(String capacity) {
		int indexOfDivider = capacity.indexOf('/');
		int indexOfSpace = capacity.indexOf(' ');
		int indexOfPercent = capacity.indexOf('%');
		
		String currentSize = capacity.substring(0, indexOfDivider);
		String maximumSize = capacity.substring(indexOfDivider + 1, indexOfSpace);
		String percentage = capacity.substring(indexOfSpace + 2, indexOfPercent + 1);
		
		return new String[] {currentSize, maximumSize, percentage};
	}
}