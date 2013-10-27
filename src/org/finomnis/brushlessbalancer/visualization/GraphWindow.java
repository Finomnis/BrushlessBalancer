package org.finomnis.brushlessbalancer.visualization;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JFrame;
import javax.swing.JPanel;



public class GraphWindow extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1237530591748728314L;
	private final int WINDOW_WIDTH, WINDOW_HEIGHT;
	
	private final JFrame parentFrame;

	private Image doubleBufferImage;
	private Graphics2D doubleBufferGraphics;
	private int doubleBufferWidth  = 0;
	private int doubleBufferHeight = 0;
	private Lock dataLock = new ReentrantLock();
	private float[][] v;
	
	private float y_max, y_min;
	
	public GraphWindow(int size_x, int size_y, float y_min, float y_max, int num_data, int data_size, String name)
	{
		v = new float[num_data][];
		for(int i = 0; i < v.length; i++)
		{
			v[i] = new float[data_size];
		}
		this.y_max = y_max;
		this.y_min = y_min;
		WINDOW_WIDTH = size_x;
		WINDOW_HEIGHT = size_y;
		parentFrame = new JFrame(name);
		parentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		parentFrame.getContentPane().add(this);
		parentFrame.pack();
		parentFrame.setLocationByPlatform(true);
		parentFrame.setResizable(true);
		parentFrame.setVisible(true);
	}
	
	protected Color getColor(int graphId){
		switch(graphId)
		{
		case 0: return Color.GREEN;
		case 1: return Color.BLUE;
		case 2: return Color.RED;
		case 3: return Color.YELLOW;
		case 4: return Color.MAGENTA;
		case 5: return Color.CYAN;
		case 6: return Color.ORANGE;
		case 7: return Color.GRAY;
		case 8: return Color.WHITE;
		default: return Color.PINK;
		}
	}
	
	protected void paintBuffer(Graphics2D g){
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		//////////////////////////////////////
		/// THE MAIN DRAWING CODE
		///
		dataLock.lock();
		for(int graphId = v.length - 1; graphId >= 0; graphId--)
		{
			float stepWidth = doubleBufferWidth / (float)(v[graphId].length - 1);
			g.setColor(getColor(graphId));
			
			for(int i = 0; i < v[graphId].length - 1; i++)
			{
				g.drawLine((int)(i * stepWidth), (int)(doubleBufferHeight * (1 - (v[graphId][i] - y_min) / (y_max - y_min))) , (int)((i+1) * stepWidth), (int)(doubleBufferHeight * (1 - (v[graphId][i+1] - y_min) / (y_max - y_min))));
			}
		}
		dataLock.unlock();
		
		
	}
	
	protected void setData(int graphId, List<Float> list)
	{
		if(list.size() != v[graphId].length)
			throw new RuntimeException("Invalid argument, wrong data count!");
		dataLock.lock();
		Iterator<Float> it = list.iterator();
		for(int i = 0; i < list.size(); i++)
			v[graphId][i] = it.next();
		dataLock.unlock();
	}
	
	protected void setData(int graphId, float[] list, int offset)
	{
		if(list.length != v[graphId].length * offset)
			throw new RuntimeException("Invalid argument, wrong data count!");
		dataLock.lock();
		for(int i = 0; i < v[graphId].length; i++)
			v[graphId][i] = list[offset*i];
		dataLock.unlock();
	}
	
	protected void setData(int graphId, float[] list)
	{
		if(list.length != v[graphId].length)
			throw new RuntimeException("Invalid argument, wrong data count!");
		dataLock.lock();
		System.arraycopy(list, 0, v[graphId], 0, list.length);
		dataLock.unlock();
	}
	
	@Override
	protected void paintComponent(Graphics ggen){
		super.paintComponent(ggen);
		
		if(doubleBufferGraphics == null || doubleBufferWidth != getWidth() || doubleBufferHeight != getHeight())
		{
			doubleBufferWidth = getWidth();
			doubleBufferHeight = getHeight();
			doubleBufferImage = createImage(doubleBufferWidth, doubleBufferHeight);
			doubleBufferGraphics = (Graphics2D)doubleBufferImage.getGraphics();
			doubleBufferGraphics.setBackground(Color.BLACK);
		}
		
		doubleBufferGraphics.clearRect(0, 0, doubleBufferWidth, doubleBufferHeight);
		paintBuffer(doubleBufferGraphics);
		
		ggen.drawImage(doubleBufferImage, 0, 0, this);
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);
	}
	
	
}
