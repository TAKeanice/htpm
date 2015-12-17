package de.dbvis.htpm.io.serializer.xml;

import java.io.OutputStream;
import java.util.List;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import de.dbvis.htpm.hes.HybridEventSequence;
import de.dbvis.htpm.hes.events.HybridEvent;
import de.dbvis.htpm.io.serializer.Serializer;


public class XMLHybridEventSequenceSerializer extends XMLAbstractSerializer<HybridEventSequence> {
	public XMLHybridEventSequenceSerializer(ContentHandler handler) {
		super(handler);
	}
	
	public XMLHybridEventSequenceSerializer(OutputStream stream) throws SAXException {
		super(stream);
	}

	@Override
	public void serialize(HybridEventSequence element) {
		try {
			AttributesImpl atts = new AttributesImpl();
			atts.addAttribute("", "", "id", "ID", element.getSequenceId());
			
			this.addAdditionalAttributes(element, atts);
			
			m_outHandler.startElement("", "", "sequence", atts);
			
			this.addAdditionalData(element);
			
			Serializer<HybridEvent> se = new XMLHybridEventSerializer(m_outHandler);
			se.serialize(element.getEvents());
			
			m_outHandler.endElement("", "", "sequence");
		} catch (SAXException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void addAdditionalData(HybridEventSequence hes) {
		
	}
	
	protected void addAdditionalAttributes(HybridEventSequence hes, AttributesImpl atts) {
		
	}

	@Override
	public void serialize(List<HybridEventSequence> elements) {
		for(HybridEventSequence s : elements) {
			this.serialize(s);
		}
	}
}
