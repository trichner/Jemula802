package layer2_802Algorithms;

import layer2_80211Mac.JE802_11Mac;
import layer2_80211Mac.JE802_11MacAlgorithm;
import layer2_802Algorithms.controller.STAPhyController;
import plot.JEMultiPlotter;

public class Bss1Station extends JE802_11MacAlgorithm {
	
	//private JE802_11BackoffEntity theBackoffEntity;
	
	private int theBSS;
	
	private int step;
	
	public Bss1Station(String name, JE802_11Mac aMac) {
		super(name, aMac);
		this.theBSS = 01;
		this.theBackoffEntity = this.mac.getBackoffEntity(theBSS);
		this.step = 0;

        this.controller  = new STAPhyController();
	}
	
	@Override
	public void compute() {
		this.step++;

        RRMConfig conf = controller.compute(this.prepareInput());

        double txPower = Math.min(1000,Math.max(0,conf.getTxPower()));
        this.mac.getPhy().setCurrentTransmitPower_mW(txPower);
        this.mac.getPhy().setCurrentPhyMode(conf.getPhymode());

	}
	
	@Override
	public void plot() {
        if (plotter == null) {
            plotter = new JEMultiPlotter("", "TxPower/30dBm",
                    "emulation time [ms]", "Station 1" + "", theUniqueEventScheduler.getEmulationEnd()
                    .getTimeMs(), true);
            plotter.addSeries("PhyMode/54Mb/s");
            //plotter.addSeries("queue");
            //plotter.addSeries("collisions");
            //plotter.addSeries("retry");
            plotter.display();
        }
        plotter.plot(theUniqueEventScheduler.now().getTimeMs(), this.mac.getPhy()
                .getCurrentTransmitPowerLevel_dBm() / 30.0, 0);
        plotter.plot(theUniqueEventScheduler.now().getTimeMs(), (double) this.mac.getPhy()
                .getCurrentPhyMode().getRateMbps() / 54.0, 1);
	}

}
