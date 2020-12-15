package dev.codesoup.mc;

public class XZPair {
	
	public int A, B;
	
	public XZPair(int A, int B) {
		this.A = A; 
		this.B = B;
	}
	
	@Override
	public int hashCode() {
		return A * 1024 + B;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof XZPair) {
			XZPair otherPair = (XZPair)other;
			return otherPair.A == A && otherPair.B == B;
		} else {
			return false;
		}
	}
	
}