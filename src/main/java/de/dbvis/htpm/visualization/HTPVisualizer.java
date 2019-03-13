package de.dbvis.htpm.visualization;

import de.dbvis.htpm.htp.DefaultHybridTemporalPattern;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.htp.eventnodes.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class HTPVisualizer extends JComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8114634998708606733L;
	private static final int radius = 10;
	private static final int line_thickness = 4;
	
	protected boolean drawGridLines = false;

	protected EventColorizer ec;

	protected Map<String, Integer> unseen;

	protected HybridTemporalPattern pattern;
	protected HTPItem[] events;
	
	protected Integer total_Xgps;
	protected Integer total_Ygps;
	
	protected Graphics2D g;

	public HTPVisualizer(EventColorizer ec) {
		this.ec = ec;

		this.ec.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				repaint();
			}
		});
	}
	
	protected int getXPosition(int gridpoint) {
		gridpoint++;
		int width = this.getWidth(); //5px margin left & right
		int p = (int) Math.floor((float) width / (float) this.total_Xgps);
		return (gridpoint * p);
	}
	
	protected int getYPosition(int gridpoint) {
		gridpoint++;
		int width = this.getHeight(); //5px margin left & right
		int p = (int) Math.floor((float) width / (float) this.total_Ygps);
		return (gridpoint * p);
	}

	public void setPattern(HybridTemporalPattern pattern) {
		this.pattern = pattern;
		this.events = null;
		if (pattern != null) {
			this.events = pattern.getPatternItems().toArray(new HTPItem[0]);
		}
		
		this.total_Xgps = this.getNumberOfXGridPoints();
		this.total_Ygps = this.getNumberOfYGridPoints();
		
		this.repaint();
	}

	protected int getNumberOfXGridPoints() {
		int i = 1;
		for (HTPItem item : pattern.getPatternItems()) {
			if (item instanceof OrderRelation
					&& ((OrderRelation) item).equals(OrderRelation.SMALLER)) {
				i++;
			}
		}
		return i+1;
	}
	
	protected int getNumberOfYGridPoints() {
		return this.pattern.length()+1;
	}
	
	protected void drawGrid() {
		g.setColor(Color.black);
		for(int i = 0; i < this.getNumberOfXGridPoints(); i++) {
			g.drawLine(this.getXPosition(i), 0, this.getXPosition(i), this.getHeight());
		}
		
		for(int i = 0; i < this.getNumberOfYGridPoints(); i++) {
			g.drawLine(0, this.getYPosition(i), this.getWidth(), this.getYPosition(i));
		}
	}
	
	protected void paintEventNode(PointEventNode node, int gprx, int gpry) {
		int x = this.getXPosition(gprx);
		int y = this.getYPosition(gpry);
		
		g.setColor(ec.getColor(node));
		
		g.fillOval(x-(radius/2), y-(radius/2), radius, radius);

		g.setColor(Color.black);
		
		g.drawString(node.getStringEventNodeId(), x + radius + 5, y + (radius / 2));
	}
	
	protected void paintEventNode(IntervalStartEventNode node, int grpx, int grpy) {
		int x = this.getXPosition(grpx);
		int y = this.getYPosition(grpy);
		
		g.setColor(ec.getColor(node));
		
		g.fillOval(x-(radius/2), y-(radius/2), radius, radius);
	}
	
	protected void paintEventNode(IntervalEndEventNode node, int grpx, int grpy) {
		int x = this.getXPosition(grpx);
		int y = this.getYPosition(grpy);
		
		g.setColor(ec.getColor(node));
		
		g.fillOval(x-(radius/2), y-(radius/2), radius, radius);

		g.setColor(Color.black);
		
		g.drawString(node.getStringEventNodeId(), x + radius + 5, y + (radius / 2));
	}
	
	protected void paintEventNodeLine(IntervalEventNode node, int fromgrpx, int togprx, int grpy) {
		int fromx = this.getXPosition(fromgrpx);
		int tox = this.getXPosition(togprx);
		int y = this.getYPosition(grpy);
		
		g.setColor(ec.getColor(node));
		
		g.fillRect(fromx, // x
				(y - (line_thickness / 2)), // y
				tox-fromx, // width
				line_thickness); // height
	}

	private void paintLines(Graphics g) {
		int curgprx = 0;
		int curgpry = 0;
		for (int i = 0; i < events.length; i++) {
			if (events[i] instanceof EventNode) {
				EventNode e = (EventNode) events[i];
				if (events[i] instanceof IntervalEventNode) {

					String e_key = e.getStringEventNodeId()
							+ ((IntervalEventNode) events[i])
									.getOccurrenceMark();
					if (events[i] instanceof IntervalStartEventNode) {
						int togprx = curgprx;

						//search for corresponding end node
						for (int j = (i + 1); j < events.length; j++) {
							if (events[j] instanceof IntervalEndEventNode) {
								IntervalEndEventNode e2 = (IntervalEndEventNode) events[j];
								String e_key2 = e2.getStringEventNodeId()
										+ e2.getOccurrenceMark();

								if (e_key.equals(e_key2)) {
									break;
								}
							}

							if (events[j] instanceof OrderRelation
									&& ((OrderRelation) events[j])
											.equals(OrderRelation.SMALLER)) {
								togprx++;
							}
						}
						this.paintEventNodeLine((IntervalEventNode) events[i], curgprx, togprx, curgpry);
					}
				}
				curgpry++;
			}

			if (events[i] instanceof OrderRelation
					&& ((OrderRelation) events[i])
							.equals(OrderRelation.SMALLER)) {
				curgprx++;
			}
		}
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		this.g = (Graphics2D) g;
		
		
		if(this.drawGridLines) {
			this.drawGrid();
		}
		
		if (this.pattern == null) {
			return;
		}
		this.unseen = new HashMap<String, Integer>();
		
		this.paintLines(g);

		int curgprx = 0;
		int curgpry = 0;
		for (int i = 0; i < events.length; i++) {
			if (events[i] instanceof EventNode) {
				g.setColor(this.ec.getColor(((EventNode) events[i]).getStringEventNodeId()));
				if (events[i] instanceof PointEventNode) {
					this.paintEventNode((PointEventNode) events[i], curgprx, curgpry);
					curgpry++;
				} else if (events[i] instanceof IntervalEventNode) {
					String e_key = ((IntervalEventNode) events[i]).getStringEventNodeId()
							+ ((IntervalEventNode) events[i])
									.getOccurrenceMark();
					if (events[i] instanceof IntervalStartEventNode) {
						
						this.paintEventNode((IntervalStartEventNode) events[i], curgprx, curgpry);
						
						unseen.put(e_key, curgpry);
						
						curgpry++;
					} else if(events[i] instanceof IntervalEndEventNode) {
						if (unseen.get(e_key) != null) {
							
							this.paintEventNode((IntervalEndEventNode) events[i], curgprx, unseen.get(e_key));
							
							curgpry++;
						}
						unseen.remove(e_key);

					}
				}

			} else if (events[i] instanceof OrderRelation
					&& ((OrderRelation) events[i])
							.equals(OrderRelation.SMALLER)) {
				curgprx++;
			}
		}

	}
	
	public static void main(String[] args) {
		HybridTemporalPattern pattern = new DefaultHybridTemporalPattern("a<b+0=c<b-0");
		
		HTPVisualizer v = new HTPVisualizer(new EventColorizer());
		
		JFrame f = new JFrame();
		
		f.getContentPane().add(v);
		
		v.setPattern(pattern);
		
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		f.pack();
		f.setVisible(true);
		
	}
}
