package de.dbvis.htpm.io.deserializer.xml;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.dbvis.htpm.io.deserializer.Deserializer;

public abstract class XMLAbstractDeserializer<T> implements Deserializer<T> {
	
	protected static final DocumentBuilderFactory DOMPARSER_FACTORY =
            DocumentBuilderFactory.newInstance();

    protected final InputStream m_in;

    protected final Element m_node;
    
    /**
     * Creates an XML deserializer for rooms that takes all data from an input
     * stream.
     * 
     * @param in any input stream
     */
    public XMLAbstractDeserializer(InputStream in) {
        m_in = in;
        m_node = null;
    }

    /**
     * Creates an XML deserializer for rooms that takes the rooms out of a DOM
     * tree node. The room(s) can either be directly contained in the give node
     * (also the node itself might be a room) or in any of its descendants.
     * 
     * @param node a node in a DOM tree that contains lecturers
     */
    public XMLAbstractDeserializer(Element node) {
        m_in = null;
        m_node = node;
    }
    
    protected abstract T deserialize(Element node) throws ParseException;
	
    public T deserialize() throws IOException, ParseException {
    	T ret = null;
    	
        if (m_node != null) {
            ret = deserialize(m_node);
        } else {
            DocumentBuilder parser;
            try {
                parser = DOMPARSER_FACTORY.newDocumentBuilder();
                Document root = parser.parse(m_in);
                ret = deserialize(root.getDocumentElement());
            } catch (ParserConfigurationException ex) {
                throw new RuntimeException(ex);
            } catch (SAXException ex) {
                throw new ParseException(ex.getMessage(), -1);
            }
        }
        return ret;
    }
	
}
