 package layer2_802Algorithms;

import layer2_80211Mac.JE802_11Mac;
import layer2_80211Mac.JE802_11MacAlgorithm;

public class Phymode extends JE802_11MacAlgorithm {
	
	private String phymode;
	
	public Phymode(String name, JE802_11Mac mac) {
		super(name, mac);
		this.phymode = name;
	}

	@Override
	public void compute() {
		if (this.phymode.equals("phymode_06Mbps")) {
			if (!this.mac.getPhy().getCurrentPhyMode().toString().equals("BPSK12"))
				this.mac.getPhy().setCurrentPhyMode("BPSK12");
		} else if (this.phymode.equals("phymode_09Mbps")) {
			if (!this.mac.getPhy().getCurrentPhyMode().toString().equals("BPSK34"))
				this.mac.getPhy().setCurrentPhyMode("BPSK34");
		} else if (this.phymode.equals("phymode_12Mbps")) {
			if (!this.mac.getPhy().getCurrentPhyMode().toString().equals("QPSK12"))
				this.mac.getPhy().setCurrentPhyMode("QPSK12");
		} else if (this.phymode.equals("phymode_18Mbps")) {
			if (!this.mac.getPhy().getCurrentPhyMode().toString().equals("QPSK34"))
				this.mac.getPhy().setCurrentPhyMode("QPSK34");
		} else if (this.phymode.equals("phymode_24Mbps")) {
			if (!this.mac.getPhy().getCurrentPhyMode().toString().equals("16QAM12"))
				this.mac.getPhy().setCurrentPhyMode("16QAM12");
		} else if (this.phymode.equals("phymode_36Mbps")) {
			if (!this.mac.getPhy().getCurrentPhyMode().toString().equals("16QAM34"))
				this.mac.getPhy().setCurrentPhyMode("16QAM34");
		} else if (this.phymode.equals("phymode_48Mbps")) {
			if (!this.mac.getPhy().getCurrentPhyMode().toString().equals("64QAM23"))
				this.mac.getPhy().setCurrentPhyMode("64QAM23");
		} else if (this.phymode.equals("phymode_54Mbps")) {
			if (!this.mac.getPhy().getCurrentPhyMode().toString().equals("64QAM34"))
				this.mac.getPhy().setCurrentPhyMode("64QAM34");
		}
	}

	@Override
	public void plot() {
		// TODO Auto-generated method stub
		
	}

}
