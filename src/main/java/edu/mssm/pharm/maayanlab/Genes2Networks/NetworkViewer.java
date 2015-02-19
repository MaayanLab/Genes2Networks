package edu.mssm.pharm.maayanlab.Genes2Networks;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JViewport;
import javax.swing.event.MouseInputAdapter;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphComponent.mxGraphControl;
import com.mxgraph.view.mxGraph;

public class NetworkViewer extends JFrame {
	
	private static final long serialVersionUID = -2707712944901661771L;
	
	HashMap<String, Object> vertexes = new HashMap<String, Object>();
	
	public NetworkViewer(HashSet<NetworkNode> network)
	{
		super("Network Visualization");

		mxGraph graph = new mxGraph();
		Object parent = graph.getDefaultParent();

		graph.getModel().beginUpdate();
		try
		{
			for (NetworkNode node : network) {
				Object vertex = graph.insertVertex(parent, null, node.getName(), 20, 20, 50, 50);
				vertexes.put(node.getName(), vertex);
			}
			for (NetworkNode node : network) {
				HashSet<NetworkNode> neighbors = node.getNeighbors();
				for (NetworkNode neighbor : neighbors) {
					if (vertexes.containsKey(neighbor.getName()))
						graph.insertEdge(parent, null, "", vertexes.get(node.getName()), vertexes.get(neighbor.getName()));
				}
			}
			mxCircleLayout layout = new mxCircleLayout(graph);
			layout.execute(graph.getDefaultParent());
		}
		finally
		{
			graph.getModel().endUpdate();
		}

		mxGraphComponent graphComponent = new mxGraphComponent(graph);
		DragScrollListener dsl = new DragScrollListener(graphComponent.getViewport(), graphComponent.getGraphControl());
		graphComponent.getGraphControl().addMouseListener(dsl);
		graphComponent.getGraphControl().addMouseMotionListener(dsl);
		graphComponent.addMouseWheelListener(new ZoomWheelListener(graphComponent));
		graphComponent.addKeyListener(new ZoomKeyListener(graphComponent));
		getContentPane().add(graphComponent);
	}
	
	class ZoomKeyListener implements KeyListener {
		
		private mxGraphComponent graphComponent;
		
		public ZoomKeyListener(mxGraphComponent graphComponent) {
			this.graphComponent = graphComponent;
		}
		
		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void keyPressed(KeyEvent e) {
		    switch (e.getKeyCode()) {
	    	case KeyEvent.VK_EQUALS:
		    case KeyEvent.VK_ADD: graphComponent.zoomIn(); break;
		    case KeyEvent.VK_MINUS:
		    case KeyEvent.VK_SUBTRACT: graphComponent.zoomOut(); break;
		    case KeyEvent.VK_HOME:
		    case KeyEvent.VK_BACK_SPACE: graphComponent.zoomActual(); break;
		    }
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}
	}
	
	class ZoomWheelListener implements MouseWheelListener {

		mxGraphComponent graphComponent;
		
		public ZoomWheelListener(mxGraphComponent graphComponent) {
			this.graphComponent = graphComponent;
		}
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			int notches = e.getWheelRotation();
			if (notches < 0) {
				graphComponent.zoomIn();
			}
			else {
				graphComponent.zoomOut();
			}
		}
		
	}
	
	class DragScrollListener extends MouseInputAdapter {
		
		private final Cursor defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
		private final Cursor handCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
		private Point point = new Point();
		private JViewport viewport;
		private mxGraphControl graphControl;
		
		public DragScrollListener(JViewport viewport, mxGraphControl graphControl) {
			super();
			this.viewport = viewport;
			this.graphControl = graphControl;
		}
		
		@Override
		public void mouseDragged(final MouseEvent e) {
			Point currentPoint = e.getPoint();
			Point viewPoint = viewport.getViewPosition();
			viewPoint.translate(point.x - currentPoint.x, point.y - currentPoint.y);
			viewport.setViewPosition(viewPoint);
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			graphControl.setCursor(handCursor);
			point.setLocation(e.getPoint());
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			graphControl.setCursor(defaultCursor);
			graphControl.repaint();
		}
		
	}

}
