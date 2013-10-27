package org.finomnis.brushlessbalancer.filter;

import java.util.ArrayList;
import java.util.List;

import org.finomnis.brushlessbalancer.arduinointerface.DataReceiver;
import org.finomnis.brushlessbalancer.arduinointerface.Measurement;

public abstract class Filter implements DataReceiver {

	private List<DataReceiver> receiverList = new ArrayList<DataReceiver>();
	
	protected void sendData(Measurement[] data)
	{
		for(DataReceiver receiver : receiverList)
		{
			receiver.receiveData(data);
		}
	}
	
	public synchronized void addReciever(DataReceiver reciever){
		receiverList.add(reciever);
	}

	public synchronized void removeReciever(DataReceiver reciever){
		receiverList.remove(reciever);
	}
}
