/*
 * Copyright(c) 2002-2007 Hyundai Information Technology (HIT). All rights reserved. 
 * http://www.hit.co.kr
 */
package s2.adapi.framework.util;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

/**
 * XML에서 사용되는 문자열 처리 유틸리티 클래스이다.
 * @author 김형도
 * @since 4.0
 */
public class XmlStringHelper {
	
	/**
	 * XML Entity 문자 매핑
	 */
	protected static Map<Character,String> delimeterMap = new HashMap<Character,String>();
	
	static {
		delimeterMap.put('"',"&quot;");
		delimeterMap.put('\'',"&apos;");
		delimeterMap.put('<',"&lt;");
		delimeterMap.put('>',"&gt;");
		delimeterMap.put('&',"&amp;");
	}
	
	/**
	 * ",',<,>,& 과 같은 XML entity 문자를 escape한다.
	 * @param inStr
	 * @return String
	 */ 
	public static String escapeEntity(String inStr) {
		int curIdx = 0;
		StringBuilder sb = new StringBuilder(inStr.length()+10);
		for(int i=0;i<inStr.length();i++) {
			char chr = inStr.charAt(i);
			String escaped = delimeterMap.get(chr);
			if (escaped != null) {
				sb.append(inStr.substring(curIdx,i));
				sb.append(escaped);
				curIdx = i+1;
			}
		}
		sb.append(inStr.substring(curIdx));
		return sb.toString();
	}
	
	/**
	 * XML 에서 사용할 수 없는 범위의 문자들을 제거하거나 사용가능한 문자로 변경한다.
	 * @param inStr
	 * @return String
	 */
	public static String stripInvalidXMLCharacter(String inStr) {
		if (inStr == null || "".equals(inStr) ) {
			return "";
		}
		
		StringBuilder sb = new StringBuilder(inStr.length());
		char c;
		for(int i=0;i<inStr.length();i++) {
			c = inStr.charAt(i);
			if ((c ==0x7)) {
				sb.append("\u2022");
			} else if ((c == 0x9) || (c == 0xA) || (c == 0xD) ||
	            ((c >= 0x20) && (c <= 0xD7FF)) ||
	            ((c >= 0xE000) && (c <= 0xFFFD)) ||
	            ((c >= 0x10000) && (c <= 0x10FFFF))) {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static String toString(Document doc) throws IOException {
		String xml = null;

		try {
			DOMSource domSource = new DOMSource(doc);
	    	StringWriter writer = new StringWriter();
	    	StreamResult result = new StreamResult(writer);
	    	TransformerFactory tf = TransformerFactory.newInstance();
	    	Transformer transformer = tf.newTransformer();
	    	
	    	transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    	
	    	transformer.transform(domSource, result);

			xml = writer.toString();
		} catch (TransformerException te) {
			throw new IOException("Error serializing Document as String: "
					+ te.getMessageAndLocation());
		}
		return xml;
	}
}
