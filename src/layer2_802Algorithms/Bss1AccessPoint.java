package layer2_802Algorithms;

import plot.JEMultiPlotter;
import layer2_80211Mac.JE802_11BackoffEntity;
import layer2_80211Mac.JE802_11Mac;
import layer2_80211Mac.JE802_11MacAlgorithm;

public class Bss1AccessPoint extends JE802_11MacAlgorithm {

	private JE802_11BackoffEntity theBackoffEntity;

	private int theBSS;

	private int step;

	public Bss1AccessPoint(String name, JE802_11Mac aMac) {
		super(name, aMac);
		this.theBSS = 01;
		this.theBackoffEntity = this.mac.getBackoffEntity(theBSS);
		this.step = 0;
	}

	@Override
	public void compute() {
		this.step++;
		message(step + ": AP " + this.theBSS + " with MAC address "
				+ this.dot11MACAddress.toString() + ". Algorithm: '"
				+ this.algorithmName + "'.", 10);

		// int aAIFSN = this.theBackoffEntity.getDot11EDCAAIFSN();
		// int aCWmin = this.theBackoffEntity.getDot11EDCACWmin();
		// int aQueueSize = this.theBackoffEntity.getQueueSize();
		// int aCurrentQueueSize = this.theBackoffEntity.getCurrentQueueSize();
		//
		// double aCurrentTxPower_dBm = this.mac.getPhy()
		// .getCurrentTransmitPowerLevel_dBm();
		// JE802PhyMode aCurrentPhyMode = this.mac.getPhy().getCurrentPhyMode();

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

	@Override
	public void plot() {
//		if (plotter == null) {
//			plotter = new JEMultiPlotter("", "TxPower/30dBm",
//					"emulation time [ms]", "Access Point 1" + "", theUniqueEventScheduler.getEmulationEnd().getTimeMs(), true);
//			plotter.addSeries("PhyMode/54Mb/s");
//			plotter.addSeries("queue");
//			plotter.display();
//		}
//		plotter.plot(((Double) theUniqueEventScheduler.now().getTimeMs())
//				.doubleValue(), this.mac.getPhy()
//				.getCurrentTransmitPowerLevel_dBm() / 30.0, 0);
//		plotter.plot(((Double) theUniqueEventScheduler.now().getTimeMs())
//				.doubleValue(), new Double(this.mac.getPhy()
//				.getCurrentPhyMode().getRateMbps()) / 54.0, 1);
//		plotter.plot(((Double) theUniqueEventScheduler.now().getTimeMs())
//				.doubleValue(),
//				0.01 + new Double(this.theBackoffEntity.getCurrentQueueSize()
//						/ this.theBackoffEntity.getQueueSize()), 2);
	}

}
