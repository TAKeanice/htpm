package de.dbvis.htpm.io.serializer.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import de.dbvis.htpm.io.serializer.Serializer;

public abstract class XMLAbstractSerializer<T> implements Serializer<T> {
	protected static final SAXTransformerFactory TRANSFORMER_FACTORY =
            (SAXTransformerFactory)TransformerFactory.newInstance();

    protected final ContentHandler m_outHandler;

    protected final OutputStream m_outStream;
    
    /**
     * Creates a new XML serializer. Serialized objects are
     * written directly into the stream as a complete XML document including the
     * XML header. Calling {@link #close()} will finish the XML document and
     * close the stream.
     * 
     * @param out any output stream
     * @throws SAXException if an XML error occurs
     */
    public XMLAbstractSerializer(OutputStream out) throws SAXException {
        m_outStream = out;
        m_outHandler = createTransformerHandler(out);
        m_outHandler.startDocument();
    }

    /**
     * Creates a new XML serializer. Serialized objects will
     * generate events that are sent to the content handler. Calling
     * {@link #close()} will do nothing.
     * 
     * @param handler a content handler that receives SAX events
     */
    public XMLAbstractSerializer(ContentHandler handler) {
        m_outHandler = handler;
        m_outStream = null;
    }
    
    private static TransformerHandler createTransformerHandler(OutputStream out) {
        TransformerHandler tfh;
        try {
            tfh = TRANSFORMER_FACTORY.newTransformerHandler();
        } catch (TransformerConfigurationException ex) {
            throw new RuntimeException(ex);
        }
        Transformer t = tfh.getTransformer();

        t.setOutputProperty(OutputKeys.METHOD, "xml");
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        tfh.setResult(new StreamResult(out));

        return tfh;
    }
    
    public void close() {
        if (m_outStream != null) {
            try {
                m_outHandler.endDocument();
                m_outStream.close();
            } catch (SAXException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    
    public abstract void serialize(T element);
    
    public abstract void serialize(List<T> elements);
}
