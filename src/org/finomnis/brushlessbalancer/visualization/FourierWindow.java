package org.finomnis.brushlessbalancer.visualization;

import org.finomnis.brushlessbalancer.arduinointerface.DataReceiver;
import org.finomnis.brushlessbalancer.arduinointerface.Measurement;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;

public class FourierWindow extends GraphWindow implements DataReceiver {

	private static final long serialVersionUID = 1L;
	private final int fourier_size;
	private float fourier_scale;
	private boolean logarithmic;

	private FloatFFT_1D fftConverter;
	
	private Measurement[] buffer;
	private float[] data;

	public FourierWindow(int size_x, int size_y, int fourier_size,
			float fourier_scale, boolean logarithmic) {
		super(size_x, size_y, -fourier_scale/100.0f, fourier_scale, 3, fourier_size, "Fourier Transform");
		this.fourier_scale = fourier_scale;
		this.fourier_size = fourier_size;
		this.logarithmic = logarithmic;
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
			
			//FFT.
			
			
			
			setData(i, data, 2);
			this.repaint();
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

}
