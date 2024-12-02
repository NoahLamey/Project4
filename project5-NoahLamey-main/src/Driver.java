import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

public class Driver {
	
	// Declare class data
		private static JFrame window;
		private static JPanel panel;
		private static JButton play;
		private static JCheckBox checkbox;
		private static JComboBox<String> timeBox;
		private static JMapViewer map;
		private static BufferedImage raccoon = null;
		private static int speed = 15;	
		private static Timer timer;
		private static boolean stopsVisible = false;
		private static int numOfStops;
		
		
	   public static void main(String[] args) throws FileNotFoundException, IOException {

	    	// Read file and call stop detection
	    	
	    	TripPoint.readFile("triplog.csv");
	    	numOfStops = TripPoint.h2StopDetection();
	    	ArrayList<TripPoint> fullTrip = TripPoint.getTrip();
	    	ArrayList<TripPoint> stops = new ArrayList<>(fullTrip);
	    	ArrayList<TripPoint> movingTrip = TripPoint.getMovingTrip();
	    	stops.removeAll(movingTrip);
	    	
	    	raccoon = ImageIO.read(new File("raccoon.png"));
	    	
	    	// Set up frame, include your name in the title
	    	window = new JFrame("Noah Lamey - Map");
	        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        window.setSize(1000, 1000);
	        window.setLayout(new BorderLayout());
	    
	        // Set up Panel for input selections
	        panel = new JPanel();
	    	
	        // Play Button
	        play = new JButton("Play");
	        panel.add(play);
	    	
	        // CheckBox to enable/disable stops
	        checkbox = new JCheckBox("Include Stops");
	        panel.add(checkbox);

	        // ComboBox to pick animation time
	        String[] Times = {"Animation Time","15 seconds", "30 seconds", "60 seconds", "90 seconds"};
	        timeBox = new JComboBox<String>(Times);
	        timeBox.setSelectedItem(0);
	    	timeBox.setEditable(false);
	    	 panel.add(timeBox);
	    	
	        // Add all to top panel
	        window.add(panel, BorderLayout.NORTH);
	        
	        // Set up tripMap
	        map = new JMapViewer();
	        map.setTileSource(new OsmTileSource.TransportMap());
	        window.add(map, BorderLayout.CENTER);
	        
	        // Add listeners for GUI components
	        play.addActionListener(new ActionListener() {
	        	@Override
	        	public void actionPerformed(ActionEvent e) {
	        		if(timer != null) 
	        			timer.stop();
					Play(fullTrip, stops, movingTrip);

	        	}

	        });
	        checkbox.addActionListener(new ActionListener() {
	        	@Override
	        	public void actionPerformed(ActionEvent e) {
	        		if(checkbox.isSelected())
	        			stopsVisible = true;
	        		else
	        			stopsVisible = false;
	        	}
	        });
	        timeBox.addItemListener(new ItemListener() {
	        	@Override
	        	public void itemStateChanged(ItemEvent e) {
	        		if(e.getStateChange() == ItemEvent.SELECTED) {
	        			String selected = (String) timeBox.getSelectedItem();
	        			if(timeBox.getSelectedIndex() != 0) {
	        				speed = Integer.parseInt(selected);
	        			}        			
	        		}
	        	}
	        });
	        

	        // Set the tripMap center and zoom level
	        map.setDisplayPosition(new Coordinate(35.211037, -97.438866), 5);
	        window.setVisible(true);
	    }
	    // Animate the trip based on selections from the GUI components


	    private static void Play(ArrayList<TripPoint> Trip, ArrayList<TripPoint> Stops, ArrayList<TripPoint> MovingTrip){
	    	
	    	map.removeAllMapMarkers();
	    	map.removeAllMapPolygons();
	    	List<Coordinate> line = new ArrayList<Coordinate>();
	    	List<Coordinate> point = new ArrayList<Coordinate>();

	    	Graphics2D g2d = (Graphics2D) window.getGraphics();
	    	g2d.setColor(Color.RED);
	    	int num = 1000;	
	    	for(TripPoint point1: Stops)
	    		point.add(new Coordinate(point1.getLat(),point1.getLon()));
	    	
	    	final int[] curr = {0};
	    	final int[] count = {0};
	    	final MapMarker[] prev = new MapMarker[1];
	    	if(stopsVisible) {
	    		for(TripPoint point2: Trip) 
	        		line.add(new Coordinate(point2.getLat(),point2.getLon()));
	    		num = (speed * 1000) / (Trip.size());
	    	}
	    	else
	    		for(TripPoint point2: MovingTrip) 
	        		line.add(new Coordinate(point2.getLat(),point2.getLon()));
	    		num =(speed * 1000) / (line.size());
	    	timer = new Timer(num, new ActionListener() {
	    			@Override
	    			public void actionPerformed(ActionEvent e) {
	    				
	    				if(curr[0] < line.size()-1) {
	    					Coordinate start = line.get(curr[0]);
	    					Coordinate end = line.get(curr[0] + 1);
	    					
	    					double calc = (end.getLat() - start.getLat()) / (end.getLon() - start.getLon());
	    					BufferedImage reset = raccoon;
	    					

	    					
		    		    	MapPolygonImpl line = new MapPolygonImpl(start,start ,end);
		    		    	MapMarker point4 = new IconMarker(end , raccoon);
		    		    	raccoon = reset;
		    		    	
		    		    	line.setColor(Color.RED);
		    		    	map.removeMapMarker(prev[0]);
		    		    	map.addMapMarker(point4);
		    		    	map.addMapPolygon(line);
		    		    	prev[0] = point4;
		    		    	if(stopsVisible && count[0] < numOfStops) { 
		    		    		Coordinate stop = point.get(count[0]);
		    		    		if(end.equals(stop)){
		    		    			MapMarkerDot stopPoint = new MapMarkerDot(stop);
		    		    			stopPoint.setBackColor(Color.RED);
		    		    			stopPoint.setColor(Color.RED);
		    		    			
		    		    			map.addMapMarker(stopPoint);
		    		    			count[0]++;
		    		    		}
		    		    	}	
	    				}
	    				curr[0]++;
	    			}
	    	});
	    	timer.setInitialDelay(0);
	    	timer.start();
	    }
	   
	}