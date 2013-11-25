package layer2_802Algorithms;

import layer2_80211Mac.JE802_11BackoffEntity;
import layer2_80211Mac.JE802_11Mac;
import layer2_80211Mac.JE802_11MacAlgorithm;

public class TxPower extends JE802_11MacAlgorithm {
	
	JE802_11BackoffEntity theBackoffEntityAC01;
	
	public TxPower(String name, JE802_11Mac mac) {
		super(name, mac);
		this.theBackoffEntityAC01 = this.mac.getBackoffEntity(1);
	}

	@Override
	public void compute() {
		if (this.theBackoffEntityAC01.getCollisionCount() > this.theBackoffEntityAC01.getFaultToleranceThreshold()) {
			if (this.mac.getPhy().getCurrentTransmitPowerLevel_dBm() < 0.0) {
				this.mac.getPhy().setCurrentTransmitPowerLevel_dBm(0.0);
			} else if (this.mac.getPhy().getCurrentTransmitPowerLevel_dBm() < 10.0) {
				this.mac.getPhy().setCurrentTransmitPowerLevel_dBm(10.0);
			}
		}	
	}

	@Override
	public void plot() {
		// TODO Auto-generated method stub
	}

}
