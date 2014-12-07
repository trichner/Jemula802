package layer2_802Algorithms;

import layer2_80211Mac.JE802_11Mac;
import layer2_80211Mac.JE802_11MacAlgorithm;
import layer2_802Algorithms.controller.APPhyController;
import plot.JEMultiPlotter;
import util.AggregateIntTracker;

public class Bss1AccessPoint extends JE802_11MacAlgorithm {
	private int theBSS;

	private int step;

	public Bss1AccessPoint(String name, JE802_11Mac aMac) {
		super(name, aMac);
		this.theBSS = 01;
		this.theBackoffEntity = this.mac.getBackoffEntity(theBSS);
		this.step = 0;

        this.controller  = new APPhyController();
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

        /*
        // c_n, d_n
        int collision = collisions.getLast(20);
        int discarded = discardeds.getLast(20);

        // c'_n, d'_n
        int dCollision = collisions.diffLast();
        int dDiscarded = discardeds.diffLast();

        // integrate
        int window = 10;
        int iCollision = collisions.intLast(10,20);
        int iDiscarded = discardeds.intLast(10,20);


        int aQueueSize = this.theBackoffEntity.getQueueSize();

		int aCurrentQueueSize = this.theBackoffEntity.getCurrentQueueSize();

		double txPower = this.mac.getPhy().getCurrentTransmitPower_mW();

		JE802PhyMode aCurrentPhyMode = this.mac.getPhy().getCurrentPhyMode();


        this.mac.getPhy().setCurrentPhyMode("BPSK34");


        double P = 1;
        double I = 0;
        double D = 0;

        double power = txPower - (P*collision + I*iCollision + D*dCollision);

        System.out.println("setting power: "+power);
        // limit power
        power = Math.min(1000,Math.max(100,power));
        this.mac.getPhy().setCurrentTransmitPower_mW(power);

        //this.mac.getPhy().setCurrentTransmitPowerLevel_dBm(30);



        */

//		if (step == 1) {
//			this.mac.getPhy().setCurrentPhyMode("BPSK12");
//			this.mac.getPhy().setCurrentTransmitPowerLevel_dBm(0);
//		}
//		if (step == 50) {
//			this.mac.getPhy().setCurrentPhyMode("64QAM34");
//			this.mac.getPhy().setCurrentTransmitPowerLevel_dBm(0);
//		}
//		if (step == 100) {
//			this.mac.getPhy().setCurrentPhyMode("64QAM34");
//			this.mac.getPhy().setCurrentTransmitPowerLevel_dBm(30);
//		}
//		if (step == 150) {
//			this.mac.getPhy().setCurrentPhyMode("BPSK12");
//			this.mac.getPhy().setCurrentTransmitPowerLevel_dBm(30);
//		}
//		if (step >= 200 && step < 250) {
//			this.mac.getPhy().setCurrentPhyMode("64QAM34");
//			this.mac.getPhy().setCurrentTransmitPowerLevel_dBm(
//					this.mac.getPhy().getCurrentTransmitPowerLevel_dBm()-0.5);
//		}
//		if (step == 250) {
//			this.mac.getPhy().setCurrentTransmitPowerLevel_dBm(0);
//		}
	}

    private AggregateIntTracker collisions = new AggregateIntTracker();
    private AggregateIntTracker discards = new AggregateIntTracker();

	@Override
	public void plot() {
        /*
		if (plotter == null) {
			plotter = new JEMultiPlotter("", "TxPower/30dBm",
					"emulation time [ms]", "Access Point 1" + "", theUniqueEventScheduler.getEmulationEnd().getTimeMs(), true);
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
        */
        /*
		plotter.plot(theUniqueEventScheduler.now().getTimeMs(),
				0.01 + (double) (this.theBackoffEntity.getCurrentQueueSize()
                        / this.theBackoffEntity.getQueueSize()), 2);

        collisions.pushAggregated(this.theBackoffEntity.getCollisionCount());
        discards.pushAggregated(this.theBackoffEntity.getDiscardedCounter());
        //System.out.println("Colls: " + collisions.getLast());
        //System.out.println("Discards: " + discards.getLast() + " counter: " + this.theBackoffEntity
        //        .getDiscardedCounter());
        plotter.plot(((Double) theUniqueEventScheduler.now().getTimeMs())
                        .doubleValue(),
                0.01 + new Double(collisions.getLast())/50, 3);
        plotter.plot(((Double) theUniqueEventScheduler.now().getTimeMs())
                        .doubleValue(),
                0.01 + new Double(discards.getLast())/50, 4);
                */

        if (plotter == null) {
            plotter = new JEMultiPlotter("AP1", "TxPower/30dBm",
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
