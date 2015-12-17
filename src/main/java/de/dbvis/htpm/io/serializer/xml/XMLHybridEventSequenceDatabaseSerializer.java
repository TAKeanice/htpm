package de.dbvis.htpm.io.serializer.xml;

import java.io.OutputStream;
import java.util.List;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import de.dbvis.htpm.db.HybridEventSequenceDatabase;
import de.dbvis.htpm.hes.HybridEventSequence;
import de.dbvis.htpm.io.serializer.Serializer;

public class XMLHybridEventSequenceDatabaseSerializer extends XMLAbstractSerializer<HybridEventSequenceDatabase> {
    public XMLHybridEventSequenceDatabaseSerializer(OutputStream out) throws SAXException {
        super(out);
    }

    public XMLHybridEventSequenceDatabaseSerializer(ContentHandler handler) {
    	super(handler);
    }

	protected void serializeImpl(HybridEventSequenceDatabase db) {
		try {
			m_outHandler.startElement("", "", "db", null);
			
			AttributesImpl atts = new AttributesImpl();
			
			this.addAdditionalAttributes(db, atts);
			
			m_outHandler.startElement("", "", "parameter", atts);
			m_outHandler.endElement("", "", "parameter");
			
			this.addAdditionalData(db);
			
			Serializer<HybridEventSequence> she = new XMLHybridEventSequenceSerializer(m_outHandler);
			she.serialize(db.getSequences());
			
			m_outHandler.endElement("", "", "db");

		} catch (SAXException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void addAdditionalData(HybridEventSequenceDatabase db) {
		
	}
	
	protected void addAdditionalAttributes(HybridEventSequenceDatabase db, AttributesImpl atts) {
		
	}

	@Override
	public void serialize(HybridEventSequenceDatabase element) {
		this.serializeImpl(element);
	}

	@Override
	public void serialize(List<HybridEventSequenceDatabase> elements) {
		throw new RuntimeException("Not supported.");
	}
	
}
