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
	//private float lastx;
	//private float lasty;
	//private float lastz;
	
	
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
    	//System.out.println(b + " - " + Integer.toBinaryString(b) + " - " + state);
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
    		if(valsInMeasurement > 3)
    		{
    			Measurement newM = new Measurement();
    			newM.x = newMeasurement[0]; if(newM.x < 0) newM.x += 256;
    			newM.y = newMeasurement[1]; if(newM.y < 0) newM.y += 256;
    			newM.z = newMeasurement[2]; if(newM.z < 0) newM.z += 256;
    			int tmp1, tmp2, tmp3;
    			tmp1 = (0x3 & (newMeasurement[3] >> 0));
    			tmp2 = (0x3 & (newMeasurement[3] >> 2));
    			tmp3 = (0x3 & (newMeasurement[3] >> 4));
    			newM.x += tmp1 * 256;
    			newM.y += tmp2 * 256;
    			newM.z += tmp3 * 256;
    			newM.x -= 512;
    			newM.y -= 512;
    			newM.z -= 512;
    			
    			
    			measurementsLock.lock();
    			measurements.add(newM);
    			measurementsLock.unlock();
    			//if(Math.abs(newM.x) + Math.abs(newM.y) + Math.abs(newM.z) > 2000)
    			//if(Math.abs(lastx - newM.x) > 128 || Math.abs(lasty - newM.y) > 128 || Math.abs(lastz - newM.z) > 128)
    			//	System.out.println(newM.x + ", " + newM.y + ", " + newM.z);
      			//lastx = newM.x;
      			//lasty = newM.y;
      			//lastz = newM.z;
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
		
		if(measurementArray.length < 1)
			return;
		for(DataReceiver receiver: receiverList)
		{
			receiver.receiveData(measurementArray);
		}
	}
	
}
