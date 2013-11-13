package org.finomnis.brushlessbalancer.arduinointerface;

public class Measurement {

	public float x;
	public float y;
	public float z;
	
	
	public Measurement()
	{
		this.x = 0.0f;
		this.y = 0.0f;
		this.z = 0.0f;
	}
	
	public Measurement(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Measurement clone()
	{
		return new Measurement(x,y,z);
	}

	public float get(int i) {
		switch(i){
		case 0: return x;
		case 1: return y;
		case 2: return z;
		default: throw new RuntimeException("Invalid argument!");
		}
	}
	
}
