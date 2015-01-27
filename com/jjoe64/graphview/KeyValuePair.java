package com.jjoe64.graphview;

/**
 * 6.1add-----add to fit return data by HTTP request.-----
 * 
 * @author RenXin.
 * 
 */
public class KeyValuePair {
	private double key;
	private String value;

	public KeyValuePair(double key, String value) {
		this.key = key;
		this.value = value;
	}

	public double getKey() {
		return key;
	}

	public void setKey(double key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
