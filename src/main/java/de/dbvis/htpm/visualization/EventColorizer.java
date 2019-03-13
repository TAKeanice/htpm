package de.dbvis.htpm.visualization;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.dbvis.htpm.hes.events.HybridEvent;
import de.dbvis.htpm.htp.eventnodes.EventNode;

public class EventColorizer {
	private Map<String, Color> map;
	
	private List<ChangeListener> listener;
	
	public EventColorizer() {
		this.map = new HashMap<String, Color>();
		this.listener = new ArrayList<ChangeListener>();
	}
	
	public Set<String> getEventIds() {
		return this.map.keySet();
	}
	
	public Color getColor(EventNode e) {
		if(e == null) {
			return null;
		}
		return this.getColor(e.getStringEventNodeId());
	}
	
	public Color getColor(HybridEvent e) {
		if(e == null) {
			return null;
		}
		return this.getColor(e.getEventId());
	}
	
	public Color getColor(String event_id) {
		if(!this.map.containsKey(event_id)) {
			this.map.put(event_id, new Color((float) Math.random(), (float) Math.random(), (float) Math.random()));
		}
		return this.map.get(event_id);
	}
	
	public void setColor(HybridEvent event, Color c) {
		this.setColor(event.getEventId(), c);
	}
	
	public void setColor(String event_id, Color c) {
		if(event_id == null || c == null) {
			return;
		}
		this.map.put(event_id, c);
		this.fireChangeEvent(new ChangeEvent(this));
	}
	
	public void addChangeListener(ChangeListener cl) {
		this.listener.add(cl);
	}
	
	public void removeChangeListener(ChangeListener cl) {
		this.listener.remove(cl);
	}
	
	protected void fireChangeEvent(ChangeEvent ce) {
		for(ChangeListener cl : this.listener) {
			cl.stateChanged(ce);
		}
	}
}
