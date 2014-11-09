package layer2_802Algorithms;

import layer2_80211Mac.JE802_11Mac;
import layer2_80211Mac.JE802_11MacAlgorithm;
import layer2_802Algorithms.controller.NopController;

public class Bss2AccessPoint extends JE802_11MacAlgorithm {

	private int theBSS;

	private int step;

	public Bss2AccessPoint(String name, JE802_11Mac aMac) {
		super(name, aMac);
		this.theBSS = 02;
		this.theBackoffEntity = this.mac.getBackoffEntity(theBSS);
		this.step = 0;

        this.controller  = new NopController();//new PhyController();
	}

	@Override
	public void compute() {
		this.step++;
		message(step + ": AP " + this.theBSS + " with MAC address "
				+ this.dot11MACAddress.toString() + ". Algorithm: '"
				+ this.algorithmName + "'.", 10);

        RRMConfig conf = controller.compute(prepareInput());

        double txPower = Math.min(1000,Math.max(0,conf.getTxPower()));
        this.mac.getPhy().setCurrentTransmitPower_mW(txPower);
        this.mac.getPhy().setCurrentPhyMode(conf.getPhymode());
	}

	@Override
	public void plot() {

	}

}
