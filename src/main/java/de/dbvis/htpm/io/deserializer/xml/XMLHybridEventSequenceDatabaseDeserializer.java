package de.dbvis.htpm.io.deserializer.xml;

import java.io.InputStream;
import java.text.ParseException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.dbvis.htpm.db.DefaultHybridEventSequenceDatabase;
import de.dbvis.htpm.db.HybridEventSequenceDatabase;

public class XMLHybridEventSequenceDatabaseDeserializer extends XMLAbstractDeserializer<HybridEventSequenceDatabase> {
    

    public XMLHybridEventSequenceDatabaseDeserializer(Element node) {
    	super(node);
    }
    
    public XMLHybridEventSequenceDatabaseDeserializer(InputStream in) {
    	super(in);
    }
    
	@Override
	protected HybridEventSequenceDatabase deserialize(Element node)
			throws ParseException {
		
		HybridEventSequenceDatabase mdb = new DefaultHybridEventSequenceDatabase();
		
		XMLHybridEventSequenceDeserializer hesdes = new XMLHybridEventSequenceDeserializer(node);
		
		NodeList nl = node.getChildNodes();
    	for(int i = 0; i < nl.getLength(); i++) {
    		mdb.add(hesdes.deserialize(((Element) nl.item(i))));
    	}
    	
    	return mdb;
	}
}

