package de.dbvis.htpm.io.deserializer.xml;

import java.io.InputStream;
import java.text.ParseException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.dbvis.htpm.hes.DefaultHybridEventSequence;
import de.dbvis.htpm.hes.HybridEventSequence;
import de.dbvis.htpm.hes.events.HybridEvent;

public class XMLHybridEventSequenceDeserializer extends XMLAbstractDeserializer<HybridEventSequence> {

	public XMLHybridEventSequenceDeserializer(Element node) {
		super(node);
	}
	
	public XMLHybridEventSequenceDeserializer(InputStream in) {
		super(in);
	}

	@Override
	protected HybridEventSequence deserialize(Element node) throws ParseException {
    	if(!"sequence".equals(node.getNodeName())) {
    		throw new ParseException("It does not seem to be an HybridEventSequence", 0);
    	}
    	HybridEventSequence s = new DefaultHybridEventSequence(node.getAttribute("id"));
    	
    	XMLHybridEventDeserializer evdes = new XMLHybridEventDeserializer(node);
    	
    	NodeList nl = node.getChildNodes();
    	for(int i = 0; i < nl.getLength(); i++) {
    		Element e = (Element) nl.item(i);
    		HybridEvent he = null;
    		
    		he = evdes.deserialize(e);
    		
    		if(he != null) {
    			s.add(he);
    		}
    		
    	}
    	
    	return s;
	}
	
	

}
