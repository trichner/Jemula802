package layer2_802Algorithms;

import plot.JEMultiPlotter;
import layer2_80211Mac.JE802_11BackoffEntity;
import layer2_80211Mac.JE802_11Mac;
import layer2_80211Mac.JE802_11MacAlgorithm;

public class Bss1Station extends JE802_11MacAlgorithm {
	
	private JE802_11BackoffEntity theBackoffEntity;
	
	private int theBSS;
	
	private int step;
	
	public Bss1Station(String name, JE802_11Mac aMac) {
		super(name, aMac);
		this.theBSS = 01;
		this.theBackoffEntity = this.mac.getBackoffEntity(theBSS);
		this.step = 0;
	}
	
	@Override
	public void compute() {
		this.step++;
	}
	
	@Override
	public void plot() {
	}

}
