package org.finomnis.brushlessbalancer.arduinointerface;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import gnu.io.CommPortIdentifier; 
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class ArduinoInterface extends Thread {

	private SerialPort port;
	private int numValuesLeft = -1;
	private int valsInMeasurement = 0;
	private byte newMeasurement[] = new byte[6];
	private byte lastByte = 0;
	private enum State{WAIT_FOR_HEADER, WAIT_FOR_NUM, RECEIVING};
	private State state = State.WAIT_FOR_HEADER;
	
	
	
	private List<Measurement> measurements = new ArrayList<Measurement>();
	private Lock measurementsLock = new ReentrantLock();
	
	private List<DataReceiver> receiverList = new ArrayList<DataReceiver>();
	
	
	
	public ArduinoInterface(String portName) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException
	{
		System.out.println("Creating ArduinoInterface on Port '" + portName + "'");
		
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName); 

		port = (SerialPort) portIdentifier.open("Balancer", 2000);
		
		port.setSerialPortParams(  
                2000000, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);  
   
        
		
	}
	
	public synchronized static String[] getPorts()
	{
		List<String> names = new ArrayList<String>();;
		@SuppressWarnings("unchecked")
		Enumeration<CommPortIdentifier> ports = (Enumeration<CommPortIdentifier>)CommPortIdentifier.getPortIdentifiers();  
		while(ports.hasMoreElements())
		{
			names.add(ports.nextElement().getName());
		}
		String[] arraystring = new String[0];
		return names.toArray(arraystring);
	}
	
	
	public synchronized void addReciever(DataReceiver reciever){
		receiverList.add(reciever);
	}

	public synchronized void removeReciever(DataReceiver reciever){
		receiverList.remove(reciever);
	}
	
    public void run() {  
        try {  
        	InputStream in = port.getInputStream();
            int b;  
            while(true) {  
                  
                // if stream is not bound in.read() method returns -1  
                while((b = in.read()) != -1) {  
                    onReceive((byte) b);  
                }  
                  
                // wait 10ms when stream is broken and check again  
                sleep(10);  
            }  
        } catch (IOException e) {  
            e.printStackTrace();  
        } catch (InterruptedException e) {  
            e.printStackTrace();  
        }   
    }  
    
    private void onReceive(byte b)
    {
    	//System.out.println(b + " - " + state);
    	if(lastByte == 64 && b == 64){
    		state = State.WAIT_FOR_NUM;
    		//System.out.println(measurements.size());
			return;
    	}
    	if(state == State.WAIT_FOR_HEADER){
    		lastByte = b;
    		return;
    	}
    	if(state == State.WAIT_FOR_NUM)
    	{
    		if(b <= 0){
    			state = State.WAIT_FOR_HEADER;
    			lastByte = b;
    			return;
    		}
    		numValuesLeft = b;
    		valsInMeasurement = 0;
    		state = State.RECEIVING;
    		lastByte = b;
    		return;
    	}
    	if(state == State.RECEIVING)
    	{
    		newMeasurement[valsInMeasurement] = b;
    		valsInMeasurement++;
    		if(valsInMeasurement > 5)
    		{
    			Measurement newM = new Measurement();
    			newM.x = newMeasurement[1] << 8 | newMeasurement[0];
    			newM.y = newMeasurement[3] << 8 | newMeasurement[2];
    			newM.z = newMeasurement[5] << 8 | newMeasurement[4];
    			measurementsLock.lock();
    			measurements.add(newM);
    			measurementsLock.unlock();
    			System.out.println(newM.x + ", " + newM.y + ", " + newM.z);
    			valsInMeasurement = 0;
    			numValuesLeft--;
    			if(numValuesLeft <= 0)
    			{
    				state=State.WAIT_FOR_HEADER;
    			}
    		}
    		lastByte = b;
    		return;
    	}
    }
	
	public synchronized void receive(){
		Measurement[] measurementArray = new Measurement[0];
		measurementsLock.lock();
		measurementArray = measurements.toArray(measurementArray);
		measurements.clear();
		measurementsLock.unlock();
		for(DataReceiver receiver: receiverList)
		{
			receiver.receiveData(measurementArray);
		}
	}
	
}
