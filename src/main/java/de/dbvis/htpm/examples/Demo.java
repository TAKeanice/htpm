package de.dbvis.htpm.examples;

import de.dbvis.htpm.HTPM;
import de.dbvis.htpm.DefaultHTPMConstraint;
import de.dbvis.htpm.db.DefaultHybridEventSequenceDatabase;
import de.dbvis.htpm.db.HybridEventSequenceDatabase;
import de.dbvis.htpm.hes.DefaultHybridEventSequence;
import de.dbvis.htpm.hes.HybridEventSequence;
import de.dbvis.htpm.hes.events.DefaultHybridEvent;
import de.dbvis.htpm.hes.events.HybridEvent;
import de.dbvis.htpm.htp.DefaultHybridTemporalPattern;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;
import de.dbvis.htpm.occurrence.OccurrencePoint;

import java.util.List;
import java.util.Map;

import static de.dbvis.htpm.htp.DefaultHybridTemporalPatternBuilder.buildFromSequence;

/**
 * Just a little demonstration and tutorial of how to use
 * the framework.
 * @author Wolfgang Jentner
 *
 */
public class Demo {
	public static void main(String[] args) {
		new Demo().startDemo();
	}
	
	public void startDemo() {
		//The first thing you need are events.
		//Therefore you can either use the DefaultHybridEvent class
		//or extending this class and define events that hold some more
		//information as it is shown below in the MyHybridEvent class.
		
		//An interval event with the id "b" that occurs from time point
		//1 to 5.
		HybridEvent e1 = new DefaultHybridEvent("b", 1, 5);
		
		//A point event "c" occurring at time point 3.
		HybridEvent e2 = new DefaultHybridEvent("c", 3);
		
		//And now our own event also occurring at time point 3.
		//This has the id "a", look at the inner class below.
		HybridEvent e3 = new MyHybridEvent(3, "some additional info we want to carry");
		
		//All these events have to be in a sequence, a sequence represents one logical
		//unit, mostly in time. Lets consider weekdays here (Monday to Wednesday)
		HybridEventSequence mo = new DefaultHybridEventSequence("Monday");
		
		//We add all the events to our sequence, the order does not matter.
		mo.add(e1);
		mo.add(e3);
		mo.add(e2);
		
		//Now we imply that all these three events occurred on
		//Monday at the specified timepoints.
		
		//Allow me some space to create some more events and sequences.
		HybridEventSequence tu = new DefaultHybridEventSequence("Tuesday");
		tu.add(new DefaultHybridEvent("b", 4, 6));
		tu.add(new DefaultHybridEvent("b", 5, 7));
		tu.add(new MyHybridEvent(5, "more information"));
		HybridEventSequence we = new DefaultHybridEventSequence("Wednesday");
		we.add(new DefaultHybridEvent("b", 10, 20));
		we.add(new DefaultHybridEvent("c", 5, 7));
		we.add(new MyHybridEvent(1, "and more..."));
		
		//Now we can already play a little bit:
		
		//We want to find out if event "c" occurs on Monday:
		System.out.println(mo.occur("c").toString());
		//As a result we get occurrences. Within the occurrence it is
		//stated when this event occurs.
		//the output here is: [Monday(3.0)]
		//So we know that event "c" occurred once on Monday at time point 3.
		
		//This does also work for intervals:
		System.out.println(tu.occur("b").toString());
		//Output: [Tuesday(4.0,6.0), Tuesday(5.0,7.0)]
		//Now we see that it occured twice on Tuesday from 4 to 6 and 5 to 7.
		
		System.out.println(tu.occur("doesnotoccur").toString());
		//This event does not occur (list is empty).
		//We also say that the sequence (Tuesday) does not support
		//the event.
		
		//This is pretty boring but we can also do that for patterns:
		//A pattern can be defined in several ways, 
		//let me use a string representation here, 
		//more can be found in the API
		
		//b+0<a<b-0
		//What is that?!
		//It means that an interval with id "b" starts, after that
		//(we do not define "when" after that) a point event "a" occurs
		//and after that the interval "b" ends.
		//Visualized it would look like that:
		// b: <--------->
		// a:      x
		
		//The plus and the minus indicates only when the interval is opened and closed.
		//The 0 is the occurrence mark, this is useful when there is more
		//than one interval event so the opening and closing points can be associated
		//to each other.
		//Example: b+0=b+1<b-1<b-0
		//Okay so now in words: we have two interval events "b" both of them
		//start at the same time but b1 ends before b2.
		//Visualized:
		// b0: <----------------->
		// b1: <----------->
		
		
		//Now you know the notation let us code this.
		System.out.println(mo.occur(new DefaultHybridTemporalPattern("b+0<a<b-0")));
		//Output: [Monday(1.0,3.0,5.0)]
		//Same as with the events we can conclude that the pattern defined above
		//occurs once and "b" starts at 1, then "a" occurs at 3 and "b" closes at 5.
		
		//Also we say that the sequence supports the pattern.
		System.out.println(mo.supports(new DefaultHybridTemporalPattern("b+0<a<b-0")));
		//Output: true
		
		//Note: Also Patterns have IDs, it does not matter what we set here.
		
		//We can also generate a pattern out of a whole sequence.
		System.out.println(buildFromSequence(mo).getPattern().toString());
		//Output: htp1=(b+0<a=c<b-0)
		//You should be able now to identify what that means.
		//Okay you are probably lazy so I'll tell you.
		//An interval event "b" starts, after that, "a" occurs and at the same
		//time also "c". After both occured "b" ends.
		//Visualization:
		// b0: <-------------->
		//  a:      x
		//  c:      x
		
		//Okay, quite cool but the real interesting stuff are frequent patterns.
		//To do that we use the HTPM algorithm.
		//Therefore we need to add all the sequences into a HybridEventSequenceDatabase.
		HybridEventSequenceDatabase db = new DefaultHybridEventSequenceDatabase();
		db.add(mo);
		db.add(we);
		db.add(tu);
		//Again, the ordering does not matter.
		
		//Finally, we are able to intantiate the HTPM.
		//Beside the database we also set a minimum support.
		//The minimum support has to be > 0 and <=1.
		//I've set it to 0.5 which means that a pattern
		//has to be occur at least by 50% of the sequences to be
		//considered as frequent. It does not matter how often it occurs
		//in each sequence!
		HTPM htpm = new HTPM(db, new DefaultHTPMConstraint(db, 0.5, Integer.MAX_VALUE));
		
		//Let it run, you can do that as a separate thread.
		//This class implements the Runnable interface.
		//Note the long runtime of this algorithm. More can be found
		//in the wiki.
		htpm.run();
		
		//I'm curious what the algorithm found.
		//The output is a map, each found pattern will be a key.
		//As its values, it has all the occurrences.
		//Length is maybe little bit unintuitive since it means
		//the number of events that the pattern has.
		Map<HybridTemporalPattern, List<Occurrence>> frequent_patterns = htpm.getPatternsSortedByLength();
		System.out.println(frequent_patterns.toString());
		//Output: (each key is in a seperate line)
		//htp1=(a)=[Monday(3.0), Wednesday(1.0), Tuesday(5.0)]
		//htp1=(b+0<b-0)=[Monday(1.0,5.0), Wednesday(10.0,20.0), Tuesday(4.0,6.0), Tuesday(5.0,7.0)]
		//htp1=(b+0<a<b-0)=[Monday(1.0,3.0,5.0), Tuesday(4.0,5.0,6.0)]
		
		//Okay of course the 1-length-pattern have to occur frequently in order
		//that more complex patterns can be joined. This algorithm is basically
		//an apriori algorithm.
		
		//Now we also want to know if our special event occurred frequently
		//and when and what information it carries... so many questions.
		//Lets just iterate over the Map
		for(HybridTemporalPattern p : frequent_patterns.keySet()) {
			//All occurrences
			for(Occurrence o : frequent_patterns.get(p)) {
				//All time points in each occurrence (called OccurrencePoints).
				for(OccurrencePoint op : o.ops()) {
					//When you only use a unique id for your own event,
					//you can also this as a reference.
					if(op.getHybridEvent() instanceof MyHybridEvent) {
						System.out.println("I occurred with pattern "+p+" in sequence "+o.getHybridEventSequence().getSequenceId()+" at time point "+op.getTimePoint()+" and carry this information for you: "+((MyHybridEvent) op.getHybridEvent()).getInfo());
					}
				}
			}
		}
		
		//Okay that's all, quite easy right?
		//Make sure you read the API it should be all in there.
		//Also the wiki in redmine provides some more information.
		//If all that does not help I definitely screwed up and you have to read
		//the paper.
		//Title: Discovering hybrid temporal patterns from sequences consisting of point- and interval-based events
		//Authors: Shin-Yi Wu, Yen-Liang Chen
		//Published: 2009 
	}
	
	/**
	 * Just a small example how the events can be used.
	 * @author Wolfgang Jentner
	 *
	 */
	private class MyHybridEvent extends DefaultHybridEvent {
		private String additionalinfo;
		
		public MyHybridEvent(double timepoint, String addinfo) {
			//by this, our event will always have the ID "a" and will be only
			//of type point-based event
			super("a", timepoint);
			
			this.additionalinfo = addinfo;
		}
		
		public String getInfo() {
			return this.additionalinfo;
		}
	}
}
