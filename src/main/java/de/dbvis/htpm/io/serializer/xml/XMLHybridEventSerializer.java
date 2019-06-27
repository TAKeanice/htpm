package de.dbvis.htpm.io.serializer.xml;

import de.dbvis.htpm.hes.events.HybridEvent;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.OutputStream;
import java.util.List;

public class XMLHybridEventSerializer extends XMLAbstractSerializer<HybridEvent> {
	public XMLHybridEventSerializer(ContentHandler handler) {
		super(handler);
	}
	
	public XMLHybridEventSerializer(OutputStream stream) throws SAXException {
		super(stream);
	}

	@Override
	public void serialize(HybridEvent event) {
		try {
			AttributesImpl atts = new AttributesImpl();
			atts.addAttribute("", "", "id", "ID", event.getEventId());

			if (event.isPointEvent()) {
				atts.addAttribute("", "", "type", "CDATA", "point");
				atts.addAttribute("", "", "tp", "CDATA", event.getTimePoint()+"");
			} else {
				atts.addAttribute("", "", "type", "CDATA", "interval");
				atts.addAttribute("", "", "start", "CDATA", event.getStartPoint()+"");
				atts.addAttribute("", "", "end", "CDATA", event.getEndPoint()+"");
			}

			this.addAdditionalAttributes(event, atts);
			
			m_outHandler.startElement("", "", "event", atts);
			
			this.addAdditionalData(event);
			
			m_outHandler.endElement("", "", "event");
		} catch (SAXException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void addAdditionalData(HybridEvent event) {
		
	}
	
	protected void addAdditionalAttributes(HybridEvent event, AttributesImpl atts) {
		
	}
	
	@Override
	public void serialize(List<HybridEvent> elements) {
		for(HybridEvent e : elements) {
			this.serialize(e);
		}
	}
}
