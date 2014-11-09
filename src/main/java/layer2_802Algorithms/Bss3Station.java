package layer2_802Algorithms;

import layer2_80211Mac.JE802_11Mac;
import layer2_80211Mac.JE802_11MacAlgorithm;
import layer2_802Algorithms.controller.STAPhyController;

public class Bss3Station extends JE802_11MacAlgorithm {
	
	//private JE802_11BackoffEntity theBackoffEntity;
	
	private int theBSS;
	
	private int step;
		
	public Bss3Station(String name, JE802_11Mac aMac) {
		super(name, aMac);
		this.theBSS = 03;
		this.theBackoffEntity = this.mac.getBackoffEntity(theBSS);
		this.step = 0;

        this.controller  = new STAPhyController();
	}
	
	@Override
	public void compute() {
		this.step++;
        RRMConfig conf = controller.compute(prepareInput());

        double txPower = Math.min(1000,Math.max(0,conf.getTxPower()));
        this.mac.getPhy().setCurrentTransmitPower_mW(txPower);
        this.mac.getPhy().setCurrentPhyMode(conf.getPhymode());
	}
	
	@Override
	public void plot() {
	}

}
