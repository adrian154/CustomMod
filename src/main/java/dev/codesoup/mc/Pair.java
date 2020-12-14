package dev.codesoup.mc;

public class Pair {
	
	public int A, B;
	
	public Pair(int A, int B) {
		this.A = A; 
		this.B = B;
	}
	
	@Override
	public int hashCode() {
		return A * 1024 + B;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof Pair) {
			Pair otherPair = (Pair)other;
			return otherPair.A == A && otherPair.B == B;
		} else {
			return false;
		}
	}
	
}