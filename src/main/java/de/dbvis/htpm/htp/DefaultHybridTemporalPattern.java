package de.dbvis.htpm.htp;

import de.dbvis.htpm.hes.HybridEventSequence;
import de.dbvis.htpm.hes.events.HybridEvent;
import de.dbvis.htpm.htp.eventnodes.*;

import java.util.*;

public class DefaultHybridTemporalPattern implements HybridTemporalPattern {
	private String id;
	private List<HTPItem> items;
	
	//stored for performance reasons
	private List<EventNode> eventnodes;
	private int length;
	private String patternstr;
	private int hashcode;
	private HTPItem[] aitems;
	private List<String> eventids;
	
	protected DefaultHybridTemporalPattern(String id) {
		this.id = id;
	}
	
	public String getPatternId() {
		return this.id;
	}
	
	public DefaultHybridTemporalPattern(String id, HybridEventSequence seq) {
		this(id, seq.getEvents());
		
		this.calcPropertiesOfHTP();
	}
	
	public DefaultHybridTemporalPattern(String id, HybridEvent e) {
		this(id);
		List<HybridEvent> l = new ArrayList<>();
		l.add(e);
		this.items = byEventSequence(l);
		
		this.calcPropertiesOfHTP();
	}
	
	public DefaultHybridTemporalPattern(String id, EventNode[] nodes) {
		this(id);
		this.items = makePattern(Arrays.asList(nodes));
		
		this.calcPropertiesOfHTP();
	}
	
	public DefaultHybridTemporalPattern(String id, List<HybridEvent> events) {
		this(id);
		this.items = byEventSequence(events);
		
		this.calcPropertiesOfHTP();
	}
	
	public DefaultHybridTemporalPattern(String id, String pattern) {
		this(id);
		this.items = makePattern(byPatternRepresentation(pattern));
		
		this.calcPropertiesOfHTP();
	}
	
	protected DefaultHybridTemporalPattern(String id, HTPItem[] items) {
		this(id);
		this.items = new ArrayList<>(Arrays.asList(items));
		
		this.calcPropertiesOfHTP();
	}
	
	private List<EventNode> byPatternRepresentation(String patternrepresentation) {
		//this is a hack, adding the = in the end of the pattern makes the pattern
		//iteself invalid, but the loop will continue once more and also adds
		//the last EventNode
		char[] p = (patternrepresentation + "=").toCharArray();
		List<EventNode> list = new LinkedList<>();
		
		StringBuilder id = new StringBuilder(100);
		StringBuilder oc = new StringBuilder(100);
		boolean parsingOccurencemark = false;
		boolean isStartEvent = false;
		int currenttimepoint = 0;
		for(char c : p) {
			if(!parsingOccurencemark && (c == '<' || c == '=')) {
				//is point event
				list.add(new PointEventNode(id.toString(), currenttimepoint));
				id = new StringBuilder(100);
				if(c == '<') {
					currenttimepoint++;
				}
				continue;
			}
			
			if(c == '+') {
				parsingOccurencemark = true;
				isStartEvent = true;
				continue;
			}
			
			if(c == '-') {
				parsingOccurencemark = true;
				isStartEvent = false;
				continue;
			}
			
			if(parsingOccurencemark && (c == '<' || c == '=')) {
				//we have parsed an intervalevent
				if(isStartEvent) {
					list.add(new IntervalStartEventNode(id.toString(), currenttimepoint, Integer.parseInt(oc.toString())));
				} else {
					list.add(new IntervalEndEventNode(id.toString(), currenttimepoint, Integer.parseInt(oc.toString())));
				}
				
				if(c == '<') {
					currenttimepoint++;
				}
				
				id = new StringBuilder(100);
				oc = new StringBuilder(100);
				isStartEvent = false;
				parsingOccurencemark = false;
				continue;
			}
			
			if(!parsingOccurencemark) {
				id.append(c);
			} else {
				oc.append(c);
			}
			
		}
		
		return list;
	}
	
	private List<HTPItem> byEventSequence(List<HybridEvent> events) {
		List<EventNode> nodes = new ArrayList<>();
		Map<String, Integer> occs = new HashMap<>();
		
		Collections.sort(events);
		
		for(HybridEvent e : events) {
			if(e.isPointEvent()) {
				nodes.add(new PointEventNode(e));
			}
			
			if(!e.isPointEvent()) {
				int occ = (!occs.containsKey(e.getEventId())) ? 0 : occs.get(e.getEventId());
				
				nodes.add(new IntervalStartEventNode(e, occ));
				nodes.add(new IntervalEndEventNode(e, occ));
				
				occs.put(e.getEventId(), occ+1);
			}
		}
		
		return this.makePattern(nodes);
	}
	
	private List<HTPItem> makePattern(List<EventNode> nodes) {
		List<HTPItem> items = new ArrayList<>();
		
		Collections.sort(nodes);
		
		for(int i = 0; i < (nodes.size()); i++) {
			items.add(nodes.get(i));
			if(i < nodes.size()-1) {
				if(nodes.get(i).getTimePoint() < nodes.get(i+1).getTimePoint()) {
					items.add(OrderRelation.SMALLER);
				} else {
					items.add(OrderRelation.EQUAL);
				}
			}
		}
		
		return items;
	}
	
	public String toString() {
		return "htp"+this.id+"="+this.patternStr();
	}
	
	public String patternStr() {
		return this.patternstr;
	}
	
	private void calcPropertiesOfHTP() {
		this.eventnodes = new ArrayList<>();
		//this.generalocc = new GeneralOccurrence();
		
		this.length = 0;
		
		this.patternstr = "(";
		
		this.aitems = new HTPItem[this.items.size()];
		int x = 0;
		
		this.eventids = new ArrayList<>();
		
		for(HTPItem i : this.items) {
			if(i instanceof EventNode) {
				this.eventnodes.add((EventNode) i);
				//this.generalocc.add(((EventNode) i).getTimePoint(), ((EventNode) i).getMetadata());
				
				if(i instanceof PointEventNode || i instanceof IntervalStartEventNode) {
					this.length++;
				}
				
				
				if(!this.eventids.contains(((EventNode)i).getEventNodeId())) {
					this.eventids.add(((EventNode)i).getEventNodeId());
				}
			}
			
			this.patternstr += i.toString();
			
			this.aitems[x] = i;		
			x++;
		}
		
		this.patternstr += ")";
		
		this.hashcode = this.patternstr.hashCode();
		
		if(!this.isValid()) {
			throw new IllegalArgumentException("Invalid pattern.");
		}
	}
	
	public List<EventNode> getEventNodes() {
		return new ArrayList<>(this.eventnodes);
	}
	
//	public GeneralOccurrence getGeneralOccurence() {
//		return new GeneralOccurrence(this.generalocc);
//	}
	
	/**
	 * The length defines the number of event occurences. 
	 * Not the number of event nodes.
	 * @return
	 */
	public int length() {
		return this.length;
	}
	
	public boolean equals(Object o) {
		if(o != null && o instanceof HybridTemporalPattern) {
			return this.patternStr().equals(((HybridTemporalPattern) o).patternStr());
		}
		return false;
	}
	
	public int hashCode() {
		return this.hashcode;
	}
	
	public HTPItem[] getPatternItems() {
		return this.aitems.clone();
	}
	
	public List<String> getEventIds() {
		return Collections.unmodifiableList(this.eventids);
	}
	
	private static OrderRelation small(List<OrderRelation> ors) {
		for(OrderRelation o : ors) {
			if(o != null && o.equals(OrderRelation.SMALLER)) {
				return OrderRelation.SMALLER;
			}
		}
		return OrderRelation.EQUAL;
	}
	
//	private static boolean subpattern(HTPItem[] subpattern, HTPItem[] pattern) {
//		if(pattern.length < subpattern.length) {
//			return false;
//		}
//		
//		boolean found = false;
//		for(int shift = 0; shift <= (pattern.length - subpattern.length); shift++) {
//			found = true;
//			for(int i = 0; i < subpattern.length; i++) {
//				HTPItem e1 = pattern[i+shift];
//				HTPItem e2 = subpattern[i];
//				
//				//1 type
//				if((e1 instanceof PointEventNode && e2 instanceof PointEventNode) || 
//						(e1 instanceof IntervalStartEventNode && e2 instanceof IntervalStartEventNode) ||
//						(e1 instanceof IntervalEndEventNode && e2 instanceof IntervalEndEventNode)) {
//					
//					//2 id
//					if(e1 instanceof EventNode && e2 instanceof EventNode
//							&& ((EventNode) e1).getId().equals(((EventNode) e2).getId())) {
//						
//						//3 occurencemark
//						if((e1 instanceof PointEventNode && e2 instanceof PointEventNode) || 
//								(
//								e1 instanceof IntervalEventNode && e2 instanceof IntervalEventNode
//								&& ((IntervalEventNode) e1).getOccurrenceMark() == ((IntervalEventNode) e2).getOccurrenceMark()
//								)) {
//							
//							//4 ?!?!? small
//							continue;
//							
//						}
//						
//					}
//					
//				}
//				found = false;
//				break;
//			}
//		}
//		return found;
//	}
	
//	public boolean subpattern(HybridTemporalPattern p) {
//		return subpattern(p.getPattern(), this.getPattern());
//	}
	
	private void deleteEventInPattern(HTPItem[] p, int i) {
		p[i] = null;
		
		List<OrderRelation> ors = new ArrayList<OrderRelation>();
		
		int min = Integer.MAX_VALUE;
		
		//search backwards for ORs until first EVnode appears
		for(int j = i-1; j >= 0; j--) {
			if(p[j] instanceof EventNode) {
				break;
			}
			
			if(p[j] instanceof OrderRelation) {
				ors.add((OrderRelation) p[j]);
				p[j] = null;
				min = j;
			}
		}
		
		//search forward
		for(int j = i+1; j < p.length; j++) {
			if(p[j] instanceof EventNode) {
				break;
			}
			
			if(p[j] instanceof OrderRelation) {
				ors.add((OrderRelation) p[j]);
				p[j] = null;
				if(j < min) {
					min = j;
				}
			}
		}
		if(ors.size() > 1 && min < Integer.MAX_VALUE) {
			p[min] = small(ors);
		}
	}
	
	public HybridTemporalPattern deleteLastEvent() {
		HTPItem[] p = this.getPatternItems();
		
		if(p.length == 0) {
			throw new RuntimeException("Invalid pattern, empty pattern");
		}
		
		//check for invalid pattern
		if(p[p.length-1] instanceof OrderRelation) {
			throw new RuntimeException("Invalid pattern, last item is OrderRelation "+Arrays.toString(p));
		}
		
		if(p[p.length-1] instanceof IntervalStartEventNode) {
			throw new RuntimeException("Invalid pattern, last node is IntervalStartEvent "+Arrays.toString(p));
		}
		
		//if end node search for next node
		for(int i = p.length-1; i >= 0; i--) {
			
			if(p[i] instanceof IntervalEndEventNode) {
				continue; //do nothing
			}
			
			if(p[i] instanceof PointEventNode) {
				//delete this node
				this.deleteEventInPattern(p, i);
				break;
			}
			
			if(p[i] instanceof IntervalStartEventNode) {
				
				for(int j = i; j < p.length; j++) {
					//search for end node
					if(p[j] instanceof IntervalEndEventNode 
							&& ((IntervalEndEventNode) p[j]).getEventNodeId().equals(((IntervalStartEventNode) p[i]).getEventNodeId())
							&& ((IntervalEndEventNode) p[j]).getOccurrenceMark() == ((IntervalStartEventNode) p[i]).getOccurrenceMark()) {
						this.deleteEventInPattern(p, i);
						this.deleteEventInPattern(p, j);
						break;
					}
				}
				
				break;
			}
			
			
		}
		
		return new DefaultHybridTemporalPattern(this.getPatternId(), prunePattern(p));
	}
	
	private HTPItem[] prunePattern(HTPItem[] items) {
		List<HTPItem> list = new ArrayList<HTPItem>();
		for(HTPItem i : items) {
			if(i != null && i instanceof HTPItem) {
				list.add(i);
			}
		}
		
		HTPItem[] res = new HTPItem[list.size()];
		list.toArray(res);
		
		if(!checkPattern(res)) {
			throw new RuntimeException("Invalid pattern after pruning " +Arrays.toString(res));
		}
		
		return res;
	}
	
	public boolean isValid() {
		return this.checkPattern(this.getPatternItems());
	}
	
	private boolean checkPattern(HTPItem[] items) {
		if(items.length == 0) {
			return true;
		}
		
		Map<String, Integer> m = new HashMap<String, Integer>();
		
		for(int i = 0; i < items.length; i+=2) {
			if((i < items.length-1 && !(items[i] instanceof EventNode) && !(items[i+1] instanceof OrderRelation))
					|| (i == items.length-1 && !(items[i] instanceof PointEventNode) && !(items[i] instanceof IntervalEndEventNode))) {
				return false;
			}
			if(items[i] instanceof IntervalStartEventNode) {
				if(!m.containsKey(((EventNode) items[i]).getEventNodeId())) {
					m.put(((EventNode) items[i]).getEventNodeId(), 0);
				}
				m.put(((EventNode) items[i]).getEventNodeId(), m.get(((EventNode) items[i]).getEventNodeId()) + 1);
			}
			
			if(items[i] instanceof IntervalEndEventNode) {
				if(!m.containsKey(((EventNode) items[i]).getEventNodeId())) {
					m.put(((EventNode) items[i]).getEventNodeId(), 0);
				}
				m.put(((EventNode) items[i]).getEventNodeId(), m.get(((EventNode) items[i]).getEventNodeId()) - 1);
			}
		}
		
		for(String s : m.keySet()) {
			if(m.get(s) != 0) {
				return false;
			}
		}
		
		return true;
	}
}
