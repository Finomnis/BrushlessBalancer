package org.finomnis.brushlessbalancer.arduinointerface;

public interface DataReceiver {

	public void receiveData(Measurement[] measurements);
	
}
