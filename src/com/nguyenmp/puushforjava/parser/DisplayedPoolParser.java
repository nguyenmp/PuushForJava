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

import com.nguyenmp.puushforjava.things.DisplayedPool;
import com.nguyenmp.puushforjava.things.Image;
import com.nguyenmp.puushforjava.things.Pool;

public class DisplayedPoolParser {
	
	
	public static DisplayedPool getDisplayedPoolFromHtml(String htmlString) throws SAXNotRecognizedException, SAXNotSupportedException, TransformerFactoryConfigurationError, TransformerException, URISyntaxException {
		Pool selectedPool = getSelectedPool(htmlString);
		Image[] images = getImagesFromHtml(htmlString);
		int currentPage = getCurrentPage(htmlString);
		int maxPage = getMaxPage(htmlString);
		
		return new DisplayedPool(selectedPool, currentPage, maxPage, images);
	}
	
	private static int getMaxPage(String htmlString) throws SAXNotRecognizedException, SAXNotSupportedException, TransformerFactoryConfigurationError, TransformerException {
		Document doc = XMLParser.getDocumentFromString(htmlString);
		
		Element body = (Element) XMLParser.getChildFromName(doc.getDocumentElement(), "body");
		Element content = XMLParser.getChildFromAttribute(body, "id", "content");
		Element contentDiv = XMLParser.getChildFromAttribute(content, "class", "content");
		Element pagination = XMLParser.getChildFromAttribute(contentDiv, "id", "pagination");
		
		int maxPage = 1;
		
		NodeList children = pagination.getChildNodes();
		
		for (int index = 0; index < children.getLength(); index++) {
			Node child = children.item(index);
			String childTextContent = child.getTextContent();
			try {
				maxPage = Integer.parseInt(childTextContent);
			} catch (NumberFormatException e) {
				//Do nothing
			}
		}
		
		return maxPage;
	}

	private static int getCurrentPage(String htmlString) {
		int currentPage = 1;
		
		int start = htmlString.indexOf("<span class=\"page\">") + "<span class=\"page\">".length();
		int end = htmlString.indexOf("</span>", start);
		
		try {
			String currentPageString = htmlString.substring(start, end);
			currentPage = Integer.parseInt(currentPageString);
		} catch (NumberFormatException e) {
			//Do nothing.  we initialized the current page to 1 already
		}
		
		return currentPage;
	}

	private static Pool getSelectedPool(String htmlString) throws SAXNotRecognizedException, SAXNotSupportedException, TransformerFactoryConfigurationError, TransformerException, URISyntaxException {
		Pool[] pools = PoolParser.getPoolsFromHtml(htmlString);
		String poolTitle = getPoolTitle(htmlString);
		
		Pool selectedPool = null;
		
		for (Pool pool : pools) {
			if (pool.getTitle().equals(poolTitle))
				selectedPool = pool;
		}
		
		return selectedPool;
	}
	
	private static String getPoolTitle(String htmlString) throws SAXNotRecognizedException, SAXNotSupportedException, TransformerFactoryConfigurationError, TransformerException {
		Document doc = XMLParser.getDocumentFromString(htmlString);
		
		Element body = (Element) XMLParser.getChildFromName(doc.getDocumentElement(), "body");
		Element content = XMLParser.getChildFromAttribute(body, "id", "content");
		Element contentDiv = XMLParser.getChildFromAttribute(content, "class", "content");
		Element historyDiv = XMLParser.getChildFromAttribute(contentDiv, "id", "puush_history");
		Element infoDiv = XMLParser.getChildFromAttribute(historyDiv, "id", "pool_info");
		
		String textContent = infoDiv.getTextContent();
		
		return textContent.substring(0, textContent.indexOf(" - "));
	}

	private static Image[] getImagesFromHtml(String htmlString) throws SAXNotRecognizedException, SAXNotSupportedException, TransformerFactoryConfigurationError, TransformerException, URISyntaxException {
		Document doc = XMLParser.getDocumentFromString(htmlString);
		
		Element body = (Element) XMLParser.getChildFromName(doc.getDocumentElement(), "body");
		Element content = XMLParser.getChildFromAttribute(body, "id", "content");
		Element contentDiv = XMLParser.getChildFromAttribute(content, "class", "content");
		Element historyDiv = XMLParser.getChildFromAttribute(contentDiv, "id", "puush_history");
		
		return getImagesFromHistory(historyDiv);
	}

	private static Image[] getImagesFromHistory(Element historyDiv) throws TransformerException, URISyntaxException {
		NodeList children = historyDiv.getChildNodes();
		List<Image> images = new ArrayList<Image>();
		
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			
			if (child.getNodeName().equals("div") && ((Element) child).getAttribute("class").equals("puush_tile unselected")) {
				images.add(getImageFromDiv(child));
			}
		}
		
		return images.toArray(new Image[] {});
	}

	private static Image getImageFromDiv(Node child) throws TransformerException, URISyntaxException {
		String content = XMLParser.nodeToString(child);
		
		URI thumbnail = getThumbnailFromDiv(content);
		URI link = getURLFromDiv(content);
		String title = getTitleFromDiv(content);
		String views = getViewsFromDiv(content);
		String id = getIDFromDiv(content);
		
		return new Image(link, thumbnail, title, views, id);
	}
	
	private static String getIDFromDiv(String content) {
		int start = content.indexOf("return puush_click('") + "return puush_click('".length();
		int end = content.indexOf("')", start);
		
		return content.substring(start, end);
	}

	private static URI getThumbnailFromDiv(String content) throws URISyntaxException {
		int start = content.indexOf("style=\"background-image: url(") + "style=\"background-image: url(".length();
		int end = content.indexOf(");\">", start);
		
		return new URI("http://puush.me"+content.substring(start, end));
	}
	
	private static URI getURLFromDiv(String content) throws URISyntaxException {
		int start = content.indexOf(" href=\"") + " href=\"".length();
		int end = content.indexOf('\"', start);
		return new URI(content.substring(start, end));
	}
	
	private static String getTitleFromDiv(String content) {
		int start = content.indexOf("\" title=\"") + "\" title=\"".length();
		int end = content.lastIndexOf(" (");
		
		return content.substring(start, end);
	}
	
	private static String getViewsFromDiv(String content) {
		int start = content.lastIndexOf(" (") + " (".length();
		int end = content.lastIndexOf(")");
		return content.substring(start, end);
	}
}