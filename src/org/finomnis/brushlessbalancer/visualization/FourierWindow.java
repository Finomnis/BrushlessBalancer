package org.finomnis.brushlessbalancer.visualization;

import java.awt.Color;
import java.awt.Graphics2D;

import org.finomnis.brushlessbalancer.arduinointerface.DataReceiver;
import org.finomnis.brushlessbalancer.arduinointerface.Measurement;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;

public class FourierWindow extends GraphWindow implements DataReceiver {

	private static final long serialVersionUID = 1L;
	private final int fourier_size;
	private final int display_size;
	//private float fourier_scale;
	private boolean logarithmic;

	private final float dt;
	
	private FloatFFT_1D fftConverter;
	
	private Measurement[] buffer;
	private float[] data;
	
	public FourierWindow(int size_x, int size_y, int fourier_size, int display_size, float dt,
			float fourier_scale, boolean logarithmic) {
		super(size_x, size_y, -fourier_scale/100.0f, fourier_scale, 3, Math.min(display_size, fourier_size/2), "Fourier Transform");
		//this.fourier_scale = fourier_scale;
		this.fourier_size = fourier_size;
		this.display_size = Math.min(display_size, fourier_size/2);
		this.logarithmic = logarithmic;
		this.dt = dt;
		buffer = new Measurement[fourier_size];
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = new Measurement();
		}
		data = new float[2 * fourier_size];
		fftConverter = new FloatFFT_1D(fourier_size);
	}

	@Override
	public void receiveData(Measurement[] measurements) {
		if (measurements.length >= buffer.length) {
			System.arraycopy(measurements, measurements.length - buffer.length,
					buffer, 0, buffer.length);
		} else {
			for (int i = 0; i < buffer.length - measurements.length; i++) {
				buffer[i] = buffer[i+measurements.length];
			}
			for (int i = 0; i < measurements.length; i++)
			{
				buffer[i + buffer.length - measurements.length] = measurements[i];
			}
		}
		
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < buffer.length; j++)
			{
				data[2*j] = buffer[j].get(i);
				data[2*j+1] = 0.0f;
			}
			
			
			applyGauss(data);
			
			fftConverter.complexForward(data);
			
			calculateAbsolutes(data);
			
			if(logarithmic)
				applyLogarithm(data);
			
			setData(i, data, 0, display_size);
			this.repaint();
		}
	}
	
	private void applyLogarithm(float[] valueBuf) {

		for(int i = 0; i < valueBuf.length/4; i++)
		{
			valueBuf[i] = (float) Math.exp(valueBuf[i]) - 1;
		}
		
	}

	private void applyGauss(float[] valueBuf) {
		
		float a = (float) (1/Math.sqrt(2*Math.PI));
		float u = valueBuf.length/2;
		
		for(int i = 0; i < valueBuf.length; i+=2)
		{
			valueBuf[i] *= (float) (a*Math.exp(((i-u)*(i-u)*64.0f/(valueBuf.length*valueBuf.length))/-2));
		}
		
	}
	
	private void calculateAbsolutes(float[] valueBuf){
		
		float c = 1.0f/fourier_size;
		
		for(int i = 0; i < valueBuf.length/4; i++)
		{
			valueBuf[i] = c * (float) Math.sqrt(valueBuf[2*i] * valueBuf[2*i] + valueBuf[2*i+1] * valueBuf[2*i+1]);
		}
		
	}

	@Override
	protected String getName(int graphId){
		switch(graphId)
		{
		case 0: return "X-Axis";
		case 1: return "Y-Axis";
		case 2: return "Z-Axis";
		default: return "Invalid";
		}
	}
	
	@Override
	protected void paintBuffer(Graphics2D g){
		
		g.setColor(Color.DARK_GRAY);
		
		float freqPerPixel = (display_size / (float) fourier_size)  /(dt * super.doubleBufferWidth);
		float minDistLines = 40.0f;
		float minDistLineFrequencies = freqPerPixel * minDistLines;
		float freqDist = 1.0f;
		while(freqDist < minDistLineFrequencies) freqDist *= 2;
		float dist = freqDist / freqPerPixel; 
		
		for(float i = dist; i < super.doubleBufferWidth; i+= dist)
		{
			g.drawLine((int)i, 0, (int)i, super.doubleBufferHeight - 1);
			char[] label = ("" + Math.round(i * freqPerPixel)).toCharArray();
			g.drawChars(label, 0, label.length, (int)i+2, 10);
		}
		
		float p0 = doubleBufferHeight * 100.0f / 101.0f;
		
		for(int i = 0; i < 4; i++)
		{
			int y = (int) (- i * p0/4.0f + p0);
			g.drawLine(0, y, doubleBufferWidth, y);
		}
		
		g.setColor(new Color(40,40,40));
		for(int i = 0; i < 4; i++)
		{
			int y = (int) (- (i+0.5f) * p0/4.0f + p0);
			g.drawLine(0, y, doubleBufferWidth, y);
		}
			
		super.paintBuffer(g);
	}
	
}
