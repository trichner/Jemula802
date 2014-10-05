package layer0_medium;

import java.util.List;

import kernel.JEEvent;
import layer1_802Phy.JE802Phy;

public interface JEWirelessMedium {

	/*
	 * (non-Javadoc)
	 * 
	 * @see kernel.JEEventHandler#event_handler(kernel.JEEvent)
	 */
	public void event_handler(JEEvent anEvent);

	public double getReuseDistance();

	public List<JEWirelessChannel> getAvailableChannels();

	public Integer getHandlerId();

	public double getRxPowerLevel_mW(JE802Phy JE802Phy);

	public double getBusyPowerLevel_mW();

	public double getSnirAtRx(int da, JE802Phy je802Phy);

}