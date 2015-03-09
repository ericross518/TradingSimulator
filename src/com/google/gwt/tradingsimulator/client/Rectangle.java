package com.google.gwt.tradingsimulator.client;
import com.google.gwt.user.client.rpc.IsSerializable;

public class Rectangle implements IsSerializable {

	public double x;
	public double y;
	public double width;
	public double height;
	public Rectangle() { }
	public Rectangle (double x, double y, double width, double height)
	{ 
		this.x = x; this.y = y; this.width = width; this.height = height;
	}
	public Rectangle (Rectangle r)
	{ 
		this.x = r.x; this.y = r.y; this.width = r.width; this.height = r.height;
	}
}
