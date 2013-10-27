package org.finomnis.brushlessbalancer.filter;

import java.util.LinkedList;
import java.util.List;

import org.finomnis.brushlessbalancer.arduinointerface.Measurement;

public class AveragingLowPass extends Filter {

	private final int averagingSize;
	private List<Measurement> tmpVals;
	
	public AveragingLowPass(int averagingSize)
	{
		this.averagingSize = averagingSize;
		tmpVals = new LinkedList<Measurement>();
		for(int i = 0; i < averagingSize; i++)
			tmpVals.add(new Measurement(0.0f, 0.0f, 0.0f));
	}
	
	@Override
	public void receiveData(Measurement[] measurements) {

		Measurement[] output = new Measurement[measurements.length];
		
		for(int i = 0; i < measurements.length; i++)
		{
			tmpVals.remove(0);
			tmpVals.add(measurements[i]);
			output[i] = new Measurement(0.0f, 0.0f, 0.0f);
			for(Measurement m : tmpVals)
			{
				output[i].x += m.x;
				output[i].y += m.y;
				output[i].z += m.z;
			}
			output[i].x /= averagingSize;
			output[i].y /= averagingSize;
			output[i].z /= averagingSize;
		}
		
		sendData(output);

	}

}
