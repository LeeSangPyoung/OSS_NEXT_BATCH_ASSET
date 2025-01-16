package nexcore.scheduler.util;

import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XmlUtil {
	
	// #########################################################################
	// ########## 3.X 에서 사용하는 구버전 XML <--> MAP 변환 메소드  ########### 
	// #########################################################################
//	private static String mapToXml(Map map) {
//		ByteArrayOutputStream bout = new ByteArrayOutputStream();
//		XMLEncoder xmlenc = new XMLEncoder(bout);
//		xmlenc.writeObject(map);
//		xmlenc.close();
//		try {
//			return bout.toString("UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			throw Util.toRuntimeException(e);
//		}
//	}

	private static Map xmlToMap(String xml) {
		XMLDecoder xmldec = null;
		try {
			xmldec = new XMLDecoder(new ByteArrayInputStream( xml.getBytes("UTF-8")) );
			return (Map)xmldec.readObject();
		} catch (UnsupportedEncodingException e) {
			throw Util.toRuntimeException(e);
		} finally {
			try { 
				if (xmldec != null) { 
					xmldec.close(); 
				}
			}catch(Exception ignore) {}
		}
	}

	// #########################################################################
	// ########## 4.0 부터 사용하는 새버전 XML <--> MAP 변환 메소드  ########### 
	// #########################################################################
	/**
	 * XML -> Map 변환.
	 * <br>
	 * <pre>
	 * &lt;map&gt;
	 *   &lt;entry key="aaa" value="12345"/&gt; 
	 *   &lt;entry key="bbb" value="44444"/&gt;
	 * &lt;/map&gt;
	 * </pre>
	 *  
	 * 하위 버전 호환을 위해 root 가 java 일 경우 구 방식으로 동작한다.
	 * @param xmlContent
	 * @return
	 */
	public static Map toMap(String xmlContent) {
		try {
			DocumentBuilderFactory dbf     = DocumentBuilderFactory.newInstance();
			DocumentBuilder        builder = dbf.newDocumentBuilder();

			Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));
			Element root = doc.getDocumentElement();
			if ("java".equals(root.getNodeName())) { // 이전 버전 xml.
				return XmlUtil.xmlToMap(xmlContent); 
			}else if ("map".equals(root.getNodeName())) { // 새버전 xml.
				Map map = new LinkedHashMap();
				NodeList entryNodeList = root.getElementsByTagName("entry");
				for (int i=0; i<entryNodeList.getLength(); i++) {
					Element entryNode = (Element)entryNodeList.item(i);
					map.put(entryNode.getAttribute("key"), entryNode.getAttribute("value"));
				}
				return map;
			}
			throw new RuntimeException("Fail to parse xml. Unknown root tag ("+root.getNodeName()+")");
		} catch (Exception e) {
			throw Util.toRuntimeException(e);
		}
	}
	
	public static Properties toMapToProperties(String xmlContent) {
		Map map = toMap(xmlContent);
		Properties p = new Properties();
		p.putAll(map);
		return p;
	}
	
	/**
	 * XML -> Map 변환.
	 * <br>
	 * <pre>
	 * &lt;map&gt;
	 *   &lt;entry key="aaa" value="12345"/&gt; 
	 *   &lt;entry key="bbb" value="44444"/&gt;
	 * &lt;/map&gt;
	 * </pre>
	 *  
	 * @param xmlContent
	 * @return
	 */
	public static String toXml(Map map) {
		if (map==null) {
			throw new NullPointerException("Can not convert to xml. Map is null");
		}
		try {
			DocumentBuilderFactory dbf     = DocumentBuilderFactory.newInstance();
			DocumentBuilder        builder = dbf.newDocumentBuilder();

			Document doc = builder.newDocument();
			Element root = null;
			doc.appendChild(root = doc.createElement("map"));
			
			for (Object _entry : map.entrySet()) {
				Map.Entry entry = (Map.Entry<String, String>)_entry;
				Element entryElem = doc.createElement("entry");
				entryElem.setAttribute("key",    String.valueOf(entry.getKey()));
				entryElem.setAttribute("value",  String.valueOf(entry.getValue() == null ? "" : entry.getValue()));
				root.appendChild(entryElem);
			}
			
			/* XML Writing */
			StringWriter xmlout = new StringWriter(256);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();
			t.setOutputProperty(OutputKeys.METHOD,     "xml");
			t.setOutputProperty(OutputKeys.ENCODING,   "UTF-8");
			t.setOutputProperty(OutputKeys.INDENT,     "yes");
			t.transform(new DOMSource(doc), new StreamResult(xmlout));
			return xmlout.toString();
		} catch (Exception e) {
			throw Util.toRuntimeException(e);
		}
	}
	
	// test method
	public static void main(String[] args) throws IOException {
		Map map = new LinkedHashMap();
		map.put("DATE", "20160323");
		map.put("TIME", "12345");
		map.put("DIR", "c:/101010");
		map.put("KEY0", "\"1000\"");
		map.put("KEY1", "한글 처리 ");
		
		String xml = toXml(map);
		
		System.out.println(xml);
		System.out.println(System.getProperty("file.encoding"));
		
		FileOutputStream   fout = new FileOutputStream("c:/temp/a.xml");
		OutputStreamWriter out  = new OutputStreamWriter(fout, "UTF-8");
		out.write(xml);
		out.close();

		FileWriter out2 = new FileWriter("c:/temp/a.txt");
		
		out2.write((String)map.get("KEY1"));
//		out2.write("\n");
		out2.write((String)map.get("KEY0"));
		out2.close();
		
		System.out.println("======================================");
		System.out.println(toMap(xml));
		
	}

}
