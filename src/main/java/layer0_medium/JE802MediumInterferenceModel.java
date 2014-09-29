package layer0_medium;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import kernel.JEEvent;
import kernel.JEEventHandler;
import kernel.JEEventScheduler;
import kernel.JETime;

import layer1_802Phy.JE802Mobility;
import layer1_802Phy.JE802Phy;
import layer1_802Phy.JE802Ppdu;

import util.Vector3d;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import animation.JE802KmlGenerator;

public class JE802MediumInterferenceModel extends JEEventHandler implements JEIWirelessMedium {

	private final Map<Integer, JE802ChannelEntry> channels;

	private final int interferenceDistance;

	private final double maxAllowedPathloss = dBToFactor(-110);

	private final double busyThreshold_mW;

	private final double noiseLevel_mW;

	private final List<JE802Phy> allPhys;

	// lookup table for attenuation factors
	private final Map<Integer, Map<Integer, Double>> attenuationTable;

	private final Map<Integer, JEWirelessChannel> availableChannels;

	public JE802MediumInterferenceModel(final JEEventScheduler aScheduler, final Random aGenerator, final Node aTopLevelNode) {
		super(aScheduler, aGenerator);
		channels = new HashMap<Integer, JE802ChannelEntry>();
		availableChannels = new HashMap<Integer, JEWirelessChannel>();
		attenuationTable = new HashMap<Integer, Map<Integer, Double>>();
		allPhys = new ArrayList<JE802Phy>();
		Element wirelessElem = (Element) aTopLevelNode;
		String interferenceDistanceString = wirelessElem.getAttribute("orthogonalChannelDistance");
		if (!interferenceDistanceString.isEmpty()) {
			this.interferenceDistance = new Integer(interferenceDistanceString);
		} else {
			this.interferenceDistance = 1;
		}
		String busyThresholdStr = wirelessElem.getAttribute("channelBusyThreshold_dBm");
		if (!busyThresholdStr.isEmpty()) {
			this.busyThreshold_mW = dBmtomW(new Double(busyThresholdStr));
		} else {
			this.busyThreshold_mW = dBmtomW(-82.0);
		}
		String noiseLevelStr = wirelessElem.getAttribute("noiseLevel_dBm");
		if (!noiseLevelStr.isEmpty()) {
			this.noiseLevel_mW = dBmtomW(new Double(noiseLevelStr));
		} else {
			this.noiseLevel_mW = dBmtomW(-100.0);
		}
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList channelNodeList;
		try {
			channelNodeList = (NodeList) xpath.evaluate("aChannel", wirelessElem, XPathConstants.NODESET);
			if (channelNodeList != null && channelNodeList.getLength() > 0) {
				for (int i = 0; i < channelNodeList.getLength(); i++) {
					Node channelNode = channelNodeList.item(i);
					JEWirelessChannel aNewChannel = new JEWirelessChannel(channelNode);
					availableChannels.put(aNewChannel.getChannelNumber(), aNewChannel);
				}
			} else {
				this.error("XML definition JE802WirelessChannels has no child nodes!");
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		for (JEWirelessChannel aChannel : availableChannels.values()) {
			JE802ChannelEntry entry = new JE802ChannelEntry(aChannel.getChannelNumber(), aChannel.getDot11CenterFreq_MHz());
			channels.put(aChannel.getChannelNumber(), entry);
		}
	}

	@Override
	public void event_handler(final JEEvent anEvent) {
		String eventName = anEvent.getName();
		if (eventName.equals("MEDIUM_TxStart_req")) {
			this.transmitStart(anEvent);
		} else if (eventName.equals("tx_end")) {
			this.transmitEnd(anEvent);
		} else if (eventName.equals("channel_switch_req")) {
			this.switchChannel(anEvent);
		} else if (eventName.equals("location_update_req")) {
			this.locationUpdate(anEvent);
		} else if (eventName.equals("register_req")) {
			this.registerPhy(anEvent);
		} else {
			this.error("Undefined Event " + eventName + " at WirelessMedium");
		}
	}

	// there are basically two things to do at the start of a transmission,
	// first calculate the interference of all other transmissions for all our
	// receivers
	// second compute the interference introduced by this transmission at all
	// the receivers of the other transmission
	// we also have to do this for neighboring channels
	private void transmitStart(final JEEvent anEvent) {
		JETime now = anEvent.getScheduledTime();
		JE802Ppdu aPpdu = (JE802Ppdu) anEvent.getParameterList().elementAt(1);
		JE802Phy aTxPhy = (JE802Phy) anEvent.getParameterList().elementAt(0);
		// channel on which to transmit
		int channel = aTxPhy.getCurrentChannelNumberTX();
		JE802ChannelEntry chan = channels.get(channel);
		// register packet transmission
		JE802MediumTxRecord ourTransmission = chan.addTransmittingPhy(aTxPhy,
				theUniqueEventScheduler.now().plus(aPpdu.getMpdu().getTxTime()));
		double txPower_mW = aTxPhy.getCurrentTransmitPower_mW();
		Map<Integer, Double> neighbors = attenuationTable.get(aTxPhy.getMac().getMacAddress());

		// //update interference introduced by this transmission at receivers of
		// all other transmissions on neighboring channels
		for (Integer interferingChannel : chan.getInterferingChannels()) {
			JE802ChannelEntry currentChan = channels.get(interferingChannel);
			List<JE802MediumTxRecord> otherChannelTransmissions = currentChan.getTransmittingPhys();
			double crossChannelInterference = crossChannelInterference(interferingChannel, chan.getChannel());
			for (JE802MediumTxRecord aTransmission : otherChannelTransmissions) {
				aTransmission.addInterference(aTxPhy, crossChannelInterference);
			}
		}

		// update interference introduced by this transmission at receivers of
		// all other transmissions on this channel
		List<JE802MediumTxRecord> otherTransmissions = chan.getTransmittingPhys();
		for (JE802MediumTxRecord aTransmission : otherTransmissions) {
			aTransmission.addInterference(aTxPhy, 1);
		}

		List<JE802Phy> channelPhys = chan.getPhyList();
		int size = channelPhys.size();
		for (int i = 0; i < size; i++) {
			JE802Phy rxPhy = channelPhys.get(i);
			// don't receive at own station and also not if too far away
			if (rxPhy != aTxPhy && neighbors.get(rxPhy.getMac().getMacAddress()) != null) {
				double neighborChannelInterferencemW = 0.0;
				// calculate interference for all neighboring channels
				for (Integer interferingChannel : chan.getInterferingChannels()) {
					JE802ChannelEntry currentChan = channels.get(interferingChannel);
					double neighborInterference = calculateInterference(rxPhy, aTxPhy, currentChan);
					// the neighboring channel interference depends on the
					// distance between channels
					double crossChannelInterference = crossChannelInterference(interferingChannel, chan.getChannel());
					neighborChannelInterferencemW += neighborInterference * crossChannelInterference;
				}

				// calculate interference of own transmissions on own channel
				double currentChannelInterferencemW = calculateInterference(rxPhy, aTxPhy, chan);
				double factor = getPathloss(rxPhy.getMac().getMacAddress(), aTxPhy.getMac().getMacAddress());
				double totalInterferencemW = currentChannelInterferencemW + neighborChannelInterferencemW;
				ourTransmission.addRxInterference(rxPhy, totalInterferencemW);
				double receptionPower = txPower_mW * factor;
				double SINR = receptionPower / (totalInterferencemW + noiseLevel_mW);

				// only deliver packet if power to interferenceRatio is bigger
				// than threshold
				JE802Ppdu rxPpdu = aPpdu.clone();
				Vector<Object> parameterList = new Vector<Object>();

				// jam the packet with a certain probabilty
				double prob = rxPpdu.getMpdu().getPhyMode().getPacketErrorProb(aPpdu.getMpdu().getFrameBodySize(), SINR);
				// double prob =
				// aTxPhy.getCurrentPhyMode().getPacketErrorProb(aPpdu.getMpdu().getFrameBodySize(),
				// SINR);
				if (prob > 0) {
					double rand = theUniqueRandomGenerator.nextDouble();
					if (rand < prob) {
						rxPpdu.jam();
					}
				}
				parameterList.addElement(rxPpdu);
				this.send(new JEEvent("MEDIUM_RxStart_ind", rxPhy, now, parameterList));
			}
		}
		Vector<Object> parameterList = new Vector<Object>();
		parameterList.add(aTxPhy);
		parameterList.add(aPpdu);
		this.send(new JEEvent("tx_end", this, now.plus(aPpdu.getMpdu().getTxTime()), parameterList));
	}

	private void transmitEnd(final JEEvent anEvent) {
		JETime now = anEvent.getScheduledTime();
		JE802Ppdu aPpdu = (JE802Ppdu) anEvent.getParameterList().elementAt(1);
		JE802Phy aTxPhy = (JE802Phy) anEvent.getParameterList().elementAt(0);
		JE802ChannelEntry channel = channels.get(aTxPhy.getCurrentChannelNumberTX());

		JE802MediumTxRecord transmission = channel.removeTransmittingPhy(aTxPhy);

		// create a list of all ongoing transmissions on ownChannel and on
		// interfering channels
		for (Integer neighborChannel : channel.getInterferingChannels()) {
			JE802ChannelEntry neighbor = channels.get(neighborChannel);
			List<JE802MediumTxRecord> txOnNeighborChanel = neighbor.getTransmittingPhys();
			double crossChannelInterference = crossChannelInterference(aTxPhy.getCurrentChannelNumberTX(), neighborChannel);
			for (JE802MediumTxRecord txNeighbor : txOnNeighborChanel) {
				txNeighbor.decreaseInterference(aTxPhy, crossChannelInterference);
			}
		}
		for (JE802MediumTxRecord ownChannelTx : channel.getTransmittingPhys()) {
			ownChannelTx.decreaseInterference(aTxPhy, 1);
		}

		// deliver packet and indicate whether interference was too big
		for (Integer rxStation : transmission.getRxStations()) {
			double interference = transmission.getMaxInterference(rxStation) + noiseLevel_mW;
			double attenuation = getPathloss(aTxPhy.getMac().getMacAddress(), rxStation);
			double powerAtRx = aTxPhy.getCurrentTransmitPower_mW() * attenuation;
			double SNIR = powerAtRx / interference;
			// this.message("" + SINR,70);
			JE802Ppdu aRxPpdu = aPpdu.clone(); // prevent aliasing, otherwise
												// jam signal would be set to
												// one value for all stations
			// jam the packet with a certain probability
			double prob = aRxPpdu.getMpdu().getPhyMode().getPacketErrorProb(aPpdu.getMpdu().getFrameBodySize(), SNIR);
			// double prob =
			// aTxPhy.getCurrentPhyMode().getPacketErrorProb(aPpdu.getMpdu().getFrameBodySize(),
			// SNIR);
			if (prob > 0) {
				double rand = theUniqueRandomGenerator.nextDouble();
				if (rand < prob) {
					aRxPpdu.jam();
				}
			}

			Vector<Object> parameterList = new Vector<Object>();
			parameterList.add(aRxPpdu);
			// TODO: optimize this
			JE802Phy rxPhy = null;
			// search for rxPhy
			for (JE802Phy aRxPhy : channel.getPhyList()) {
				if (aRxPhy.getMac().getMacAddress() == rxStation) {
					rxPhy = aRxPhy;
					break;
				}
			}
			// rx Station could have switched the channel during the time a
			// packet is transmitted
			if (rxPhy != null && rxPhy != aTxPhy) {
				this.send(new JEEvent("MEDIUM_RxEnd_ind", rxPhy.getHandlerId(), now, parameterList));
			}
		}
	}

	private void switchChannel(JEEvent anEvent) {
		JE802Phy phy = (JE802Phy) anEvent.getParameterList().get(0);
		Integer from = (Integer) anEvent.getParameterList().get(1);
		Integer to = (Integer) anEvent.getParameterList().get(2);
		JE802ChannelEntry fromEntry = channels.get(from);
		fromEntry.removePhy(phy);
		JE802ChannelEntry toEntry = channels.get(to);
		toEntry.addPhy(phy);
	}

	/*
	 * private double mWtodBm(final double mW) { return
	 * 10*Math.log10(mW/1000)+30; }
	 */

	private double dBToFactor(final double dB) {
		return Math.pow(10, dB / 10);
	}

	// compute the path loss from phy1 to phy2
	private double computePathlossFactor(final JE802Phy srcPhy, final JE802Phy dstPhy, final double p1m) {
		JETime now = theUniqueEventScheduler.now();

		JE802Mobility srcMob = srcPhy.getMobility();
		JE802Mobility dstMob = dstPhy.getMobility();

		Vector3d src = new Vector3d(srcMob.getXLocation(now), srcMob.getYLocation(now), srcMob.getZLocation(now));
		Vector3d dst = new Vector3d(dstMob.getXLocation(now), dstMob.getYLocation(now), dstMob.getZLocation(now));

		if (srcMob.isMobile()) {
			src.setAlt(src.getAlt() + JE802KmlGenerator.userModelHeight);
		} else {
			src.setAlt(src.getAlt() + JE802KmlGenerator.antennaModelHeight);
		}

		if (dstMob.isMobile()) {
			dst.setAlt(dst.getAlt() + JE802KmlGenerator.userModelHeight);
		} else {
			dst.setAlt(dst.getAlt() + JE802KmlGenerator.antennaModelHeight);
		}

		Vector3d pathDirection = dst.sub(src).normalize();
		double distance = src.getDistanceTo(dst);

		// compute attenuation
		double attenuation;
		if (distance > 1.0) {
			attenuation = p1m - 20 * Math.log10(distance);
		} else {
			attenuation = p1m;
		}

		// compute directional gains
		double srcDirectionalGain = srcPhy.getAntenna().getGainIndBForDirection(pathDirection, src.getLat(), src.getLon(),
				srcMob.getTraceHeading(now));
		double dstDirectionalGain = dstPhy.getAntenna().getGainIndBForDirection(pathDirection.reflect(), dst.getLat(),
				dst.getLon(), dstMob.getTraceHeading(now));

		return dBToFactor(attenuation + srcDirectionalGain + dstDirectionalGain);
	}

	// calculates the interference power level in mW at the position of atPhy
	private double calculateInterference(final JE802Phy atPhy, final JE802Phy txPhy, final JE802ChannelEntry chan) {
		double interferenceSummW = 0.0;
		List<JE802MediumTxRecord> physOnChannel = chan.getTransmittingPhys();
		int size = physOnChannel.size();
		// performance critical...
		for (int i = 0; i < size; i++) {
			JE802MediumTxRecord otherTx = physOnChannel.get(i);
			JE802Phy currentPhy = otherTx.getTxPhy();
			if (currentPhy != txPhy) {
				// first thing, add other transmissions interference to our own
				// interference
				double attenuationFactor = getPathloss(atPhy.getMac().getMacAddress(), currentPhy.getMac().getMacAddress());
				double powerLevelAtcurrentPhy = currentPhy.getCurrentTransmitPower_mW() * attenuationFactor;
				interferenceSummW += powerLevelAtcurrentPhy;
			}
		}
		return interferenceSummW;
	}

	private double crossChannelInterference(final int a, final int b) {
		double distance = Math.max(0, interferenceDistance - Math.abs(a - b));
		return distance / interferenceDistance;
	}

	// conversion from dBm to milliwatt
	private double dBmtomW(final double dBm) {
		return Math.pow(10, (dBm - 30) / 10);
	}

	private void registerPhy(final JEEvent anEvent) {
		JE802Phy newPhy = (JE802Phy) anEvent.getParameterList().elementAt(0);
		int channel = newPhy.getCurrentChannelNumberTX();
		JE802ChannelEntry entry = channels.get(channel);
		// add phy to existing channel
		entry.addPhy(newPhy);
		// compute the pathloss to all other stations and store in attenuation
		// table
		allPhys.add(newPhy);
		updateAttenuations(newPhy);
	}

	private void locationUpdate(JEEvent anEvent) {
		JE802Phy updatePhy = (JE802Phy) anEvent.getParameterList().get(0);
		// on a location update, we have to update the attenuation table and
		// recalculate the attenuations for the connections to all neighboring
		// stations
		this.updateAttenuations(updatePhy);
	}

	private void updateAttenuations(JE802Phy newPhy) {
		JE802ChannelEntry entry = channels.get(newPhy.getCurrentChannelNumberTX());
		Map<Integer, Double> attenuations = attenuationTable.get(newPhy.getMac().getMacAddress());
		if (attenuations == null) {
			attenuations = new HashMap<Integer, Double>();
		}
		for (JE802Phy otherPhy : allPhys) {
			double pathLossFactor = computePathlossFactor(newPhy, otherPhy, entry.getP1m());
			if (pathLossFactor < maxAllowedPathloss) {
				pathLossFactor = maxAllowedPathloss;
			}
			// update attenuation from us to other station
			attenuations.put(otherPhy.getMac().getMacAddress(), pathLossFactor);
			// update our attenuation at the other station
			Map<Integer, Double> othersAttenuations = attenuationTable.get(otherPhy.getMac().getMacAddress());
			if (othersAttenuations != null) {
				othersAttenuations.put(newPhy.getMac().getMacAddress(), pathLossFactor);
			} else {
				Map<Integer, Double> map = new HashMap<Integer, Double>();
				map.put(otherPhy.getMac().getMacAddress(), pathLossFactor);
			}
			attenuationTable.put(newPhy.getMac().getMacAddress(), attenuations);
		}
	}

	private double getPathloss(final Integer sta1, final Integer sta2) {
		Map<Integer, Double> mapPhy1 = attenuationTable.get(sta1);
		Double pathloss = mapPhy1.get(sta2);
		if (pathloss != null) {
			return pathloss;
		}
		// outside of transmission range
		return 0.0;
	}

	protected class JE802ChannelEntry {

		private final List<JE802Phy> phyList;

		private final List<JE802MediumTxRecord> transmittingPhys;

		private final List<Integer> interferingChannels;

		private final int channel;

		// reference power at 1m;
		private final double p1m;

		public JE802ChannelEntry(final int channel, final double frequencyMhz) {
			phyList = new ArrayList<JE802Phy>();
			this.channel = channel;
			interferingChannels = new ArrayList<Integer>();
			transmittingPhys = new ArrayList<JE802MediumTxRecord>();

			for (JEWirelessChannel chan : availableChannels.values()) {
				// double precision, not equal
				if (crossChannelInterference(chan.getChannelNumber(), channel) > 0.0 && chan.getChannelNumber() != channel) {
					interferingChannels.add(chan.getChannelNumber());
				}
			}
			double lambda = 3E8 / (frequencyMhz * 1E6);
			p1m = 20 * Math.log((lambda / 4 * Math.PI));
		}

		public void removePhy(JE802Phy toRemove) {
			phyList.remove(toRemove);
			for (JE802MediumTxRecord rec : transmittingPhys) {
				if (rec.getTxPhy().equals(toRemove)) {
					error("Switching Channel during packet transmission");
				}
			}
		}

		public List<JE802Phy> getPhyList() {
			return phyList;
		}

		public void addPhy(final JE802Phy phy) {
			phyList.add(phy);
			for (JE802MediumTxRecord rec : transmittingPhys) {
				double interference = calculateInterference(phy, rec.getTxPhy(), this);
				rec.addRxStation(phy, interference);
			}

		}

		public JE802MediumTxRecord removeTransmittingPhy(final JE802Phy aTxPhy) {
			int size = transmittingPhys.size();
			int index = 0;
			for (int i = 0; i < size; i++) {
				JE802Phy aPhy = transmittingPhys.get(i).getTxPhy();
				if (aPhy.equals(aTxPhy)) {
					index = i;
				}
			}
			return transmittingPhys.remove(index);
		}

		public JE802MediumTxRecord addTransmittingPhy(final JE802Phy txPhy, final JETime txEnd) {
			JE802MediumTxRecord txRec = new JE802MediumTxRecord(txPhy, txEnd);
			Map<Integer, Double> neighbors = attenuationTable.get(txPhy.getMac().getMacAddress());
			Set<Integer> rxStations = new HashSet<Integer>();
			for (JE802Phy addr : phyList) {
				if (neighbors.containsKey(addr.getMac().getMacAddress())) {
					rxStations.add(addr.getMac().getMacAddress());
				}
			}
			txRec.addRxStations(rxStations);
			transmittingPhys.add(txRec);
			return txRec;
		}

		public double getP1m() {
			return p1m;
		}

		public List<JE802MediumTxRecord> getTransmittingPhys() {
			return transmittingPhys;
		}

		public int getChannel() {
			return channel;
		}

		public List<Integer> getInterferingChannels() {
			return interferingChannels;
		}

		@Override
		public String toString() {
			return "Channel " + channel + " #Phys" + phyList.size() + " #transmittingPhys " + transmittingPhys.size();
		}
	}

	private class JE802MediumTxRecord {

		private final JETime txEnd;

		private final JE802Phy txPhy;

		// current interference at receiverStation
		private final Map<Integer, Double> rxInterferencemW;

		// maximal interference at receiverStation
		private final Map<Integer, Double> rxMaxInterferencemW;

		public JE802MediumTxRecord(final JE802Phy aPhy, final JETime txEnd) {
			txPhy = aPhy;
			this.txEnd = txEnd;
			rxInterferencemW = new HashMap<Integer, Double>();
			rxMaxInterferencemW = new HashMap<Integer, Double>();
		}

		public void addRxStation(JE802Phy phy, double interference) {
			rxInterferencemW.put(phy.getMac().getMacAddress(), interference);
			rxMaxInterferencemW.put(phy.getMac().getMacAddress(), interference);
		}

		public void addRxStations(final Set<Integer> keySet) {
			for (Integer addr : keySet) {
				rxInterferencemW.put(addr, new Double(0.0));
				rxMaxInterferencemW.put(addr, new Double(0.0));
			}
		}

		public double getCurrentInterference(final Integer addr) {
			Double interference = rxInterferencemW.get(addr);
			if (interference != null) {
				return interference;
			} else {
				return 0.0;
			}
		}

		public double getMaxInterference(final Integer addr) {
			Double interference = rxMaxInterferencemW.get(addr);
			if (interference != null) {
				return interference;
			} else {
				return 0.0;
			}
		}

		// update the rx interferences induced by txPhy
		public void addInterference(final JE802Phy aTxPhy, final double crossChannelInterference) {
			if (aTxPhy != txPhy) {
				for (Integer rxAddr : rxInterferencemW.keySet()) {
					double attenuation = getPathloss(aTxPhy.getMac().getMacAddress(), rxAddr);
					double interferenceAtRx = aTxPhy.getCurrentTransmitPower_mW() * attenuation * crossChannelInterference;
					Double existingInterference = rxInterferencemW.get(rxAddr);
					double newInterference = existingInterference + interferenceAtRx;
					rxInterferencemW.put(rxAddr, newInterference);
					Double maxInterference = rxMaxInterferencemW.get(rxAddr);
					if (maxInterference < newInterference) {
						rxMaxInterferencemW.put(rxAddr, newInterference);
					}
				}
			}
		}

		public void addRxInterference(final JE802Phy aRxPhy, final double interferencemW) {
			Integer addr = aRxPhy.getMac().getMacAddress();
			Double interference = rxInterferencemW.get(addr);
			Double newInterference = interference + interferencemW;
			rxInterferencemW.put(addr, newInterference);
			Double maxInterference = rxMaxInterferencemW.get(addr);
			if (maxInterference < newInterference) {
				rxMaxInterferencemW.put(addr, newInterference);
			}
		}

		public void decreaseInterference(final JE802Phy aTxPhy, final double crossChannelInterference) {
			if (aTxPhy != txPhy) {
				for (Integer rxAddr : rxInterferencemW.keySet()) {
					double attenuation = getPathloss(aTxPhy.getMac().getMacAddress(), rxAddr);
					double interferenceAtRx = aTxPhy.getCurrentTransmitPower_mW() * attenuation * crossChannelInterference;
					Double existingInterference = rxInterferencemW.get(rxAddr);
					rxInterferencemW.put(rxAddr, existingInterference - interferenceAtRx);
				}
			}
		}

		public Set<Integer> getRxStations() {
			return rxInterferencemW.keySet();
		}

		public JE802Phy getTxPhy() {
			return txPhy;
		}

		@Override
		public String toString() {
			return "TxRecord: " + txPhy + " txEnd: " + txEnd;
		}
	}

	@Override
	public List<JEWirelessChannel> getAvailableChannels() {
		List<JEWirelessChannel> result = new ArrayList<JEWirelessChannel>(availableChannels.values());
		return result;
	}

	@Override
	public double getReuseDistance() {
		double p1m = channels.values().iterator().next().getP1m();
		// TODO: not correct, use correct formula
		double distance = Math.sqrt(0.1) / (2 * Math.sqrt(Math.PI * 10E-8));
		return distance;
	}

	@Override
	public double getBusyPowerLevel_mW() {
		return busyThreshold_mW;
	}

	@Override
	public double getRxPowerLevel_mW(JE802Phy phy) {
		double neighborChannelInterference = 0.0;
		JE802ChannelEntry channel = channels.get(phy.getCurrentChannelNumberTX());
		if (channel == null) {
			this.error("Channel " + phy.getCurrentChannelNumberTX() + " not defined in XML");
			return 0.0;
		} else {
			for (Integer interferingChannel : channel.getInterferingChannels()) {
				JE802ChannelEntry entry = channels.get(interferingChannel);
				List<JE802MediumTxRecord> neighborTransmissions = entry.getTransmittingPhys();
				for (JE802MediumTxRecord rec : neighborTransmissions) {
					JE802Phy txPhy = rec.getTxPhy();
					double attenuation = getPathloss(phy.getMac().getMacAddress(), txPhy.getMac().getMacAddress());
					double interference = txPhy.getCurrentTransmitPower_mW() * attenuation;
					neighborChannelInterference += interference;
				}
			}
			List<JE802MediumTxRecord> ownTransmssions = channel.getTransmittingPhys();
			double ownChannelInterference = 0.0;
			for (JE802MediumTxRecord rec : ownTransmssions) {
				ownChannelInterference += rec.getCurrentInterference(phy.getMac().getMacAddress());
				double attenuation = getPathloss(rec.getTxPhy().getMac().getMacAddress(), phy.getMac().getMacAddress());
				double rxPower = rec.getTxPhy().getCurrentTransmitPower_mW() * attenuation;
				ownChannelInterference += rxPower;
			}
			return neighborChannelInterference + ownChannelInterference + noiseLevel_mW;
		}
	}

	@Override
	// only reception power according to distance
	public double getSnirAtRx(int rxAddr, JE802Phy txPhy) {
		double ownChannelInterference = noiseLevel_mW;
		double attenuation = getPathloss(txPhy.getMac().getMacAddress(), rxAddr);
		double rxPower = txPhy.getCurrentTransmitPower_mW() * attenuation;
		double snir = rxPower / ownChannelInterference;
		return snir;
	}
}
