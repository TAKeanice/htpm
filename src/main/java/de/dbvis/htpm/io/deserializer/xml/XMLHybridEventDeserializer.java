package de.dbvis.htpm.io.deserializer.xml;

import java.io.InputStream;
import java.text.ParseException;

import org.w3c.dom.Element;

import de.dbvis.htpm.hes.events.DefaultHybridEvent;
import de.dbvis.htpm.hes.events.HybridEvent;

public class XMLHybridEventDeserializer extends XMLAbstractDeserializer<HybridEvent> {

	public XMLHybridEventDeserializer(Element node) {
		super(node);
	}
	
	public XMLHybridEventDeserializer(InputStream in) {
		super(in);
	}

	@Override
	protected HybridEvent deserialize(Element node) throws ParseException {

    	if(!"event".equals(node.getNodeName())) {
    		throw new ParseException("Does not seem to be an HybridEvent.", 0);
    	}
    	if(node.getElementsByTagName("occurrence").getLength() != 1) {
    		throw new ParseException("Event has "+node.getElementsByTagName("occurrence").getLength()+" occurrences", 0);
    	}
    	
    	HybridEvent e = null;
    	
    	if("interval".equals(node.getAttribute("type"))) {
    		
    		e = new DefaultHybridEvent(node.getAttribute("id"), 
    				Double.parseDouble(node.getAttribute("start")), 
    				Double.parseDouble(node.getAttribute("end")));
    		
    	} else if("point".equals(node.getAttribute("type"))) {
    		
    		e = new DefaultHybridEvent(node.getAttribute("id"), 
    				Double.parseDouble(node.getAttribute("tp"))); 
    		
    	} else {
    		throw new ParseException("Unparseable type of occurrence", 0);
    	}
    	
    	return e;
		
	}

}
