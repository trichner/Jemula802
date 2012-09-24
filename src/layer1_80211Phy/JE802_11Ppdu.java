package layer1_80211Phy;

import layer1_802Phy.JE802Ppdu;
import layer2_80211Mac.JE802_11Mpdu;

public class JE802_11Ppdu extends JE802Ppdu {

	public JE802_11Ppdu(JE802_11Mpdu aMpdu, double aTxPower, int aChannel) {
		super(aTxPower, aChannel);
		this.theMpdu = aMpdu;
	}

	@Override
	public String toString() {
		// PPDU(SA:00->DA:00/AC:00/NO:/type)
		return (theUniqueEventScheduler.now().toString() + " TX PPDU(" + (this.theMpdu.getTxTime()).toString() + " until:"
				+ (theUniqueEventScheduler.now().plus(this.theMpdu.getTxTime())).toString() + "/Channel:" + this.theChannelNumber
				+ "/Power:" + this.theTxPower_mW + "/" + this.theMpdu.toString() + ")");
	}

	@Override
	public JE802_11Mpdu getMpdu() {
		return (JE802_11Mpdu) theMpdu;
	}

	public void setMpdu(JE802_11Mpdu theMpdu) {
		this.theMpdu = theMpdu;
	}

	@Override
	public JE802_11Ppdu clone() {
		JE802_11Ppdu aCopy = new JE802_11Ppdu(getMpdu(), theTxPower_mW, theChannelNumber);
		aCopy.isJammed = isJammed;
		return aCopy;
	}
}
