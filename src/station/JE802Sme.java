/*
 * 
 * This is Jemula.
 *
 *    Copyright (c) 2009 Stefan Mangold, Fabian Dreier, Stefan Schmid
 *    All rights reserved. Urheberrechtlich geschuetzt.
 *    
 *    Redistribution and use in source and binary forms, with or without modification,
 *    are permitted provided that the following conditions are met:
 *    
 *      Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer. 
 *    
 *      Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation and/or
 *      other materials provided with the distribution. 
 *    
 *      Neither the name of any affiliation of Stefan Mangold nor the names of its contributors
 *      may be used to endorse or promote products derived from this software without
 *      specific prior written permission. 
 *    
 *    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 *    EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *    IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 *    INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *    BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 *    OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *    WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 *    OF SUCH DAMAGE.
 *    
 */

package station;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import kernel.JEEvent;
import kernel.JEEventHandler;
import kernel.JEEventScheduler;
import kernel.JETime;
import layer0_medium.JEWirelessChannel;
import layer2_80211Mac.JE802HopInfo;
import layer2_80211Mac.JE802_11Mac;
import layer2_80211Mac.JE802_11Mpdu;
import layer3_network.JE802IPPacket;
import org.w3c.dom.Node;

/** @author Stefan Mangold */
public class JE802Sme extends JEEventHandler { // SME = Station Management
	// Entity

	// TODO: Roman: Do we need separate Maps for 802_11 and 802_15?
	// In other words: Can the SME be generalized?
	// STEFAN: yes, SME must remain generalized.

	private Map<Integer, JE802_11Mac> macDot11Map;

	private int ipHandlerId;

	private int channelHandlerId;

	private long seqNo = 0;

	private List<JE802Station> wiredStations;

	private final JE802Station station;

	public JE802Sme(JEEventScheduler aScheduler, Random aGenerator, Node aTopLevelNode, JE802Station myStation) {
		super(aScheduler, aGenerator);
		this.station = myStation;

		this.theState = state.active;
	}

	@Override
	public void event_handler(JEEvent anEvent) {
		JETime now = anEvent.getScheduledTime();
		String anEventName = anEvent.getName();

		// an event arrived
		// this.message("Sme at Station " + this.getAddress() +
		// " received event " + anEventName);

		if (anEventName.equals("packet_forward")) {
			JE802_11Mpdu aMpdu = (JE802_11Mpdu) anEvent.getParameterList().get(0);
			int nextChannel = aMpdu.getHopAddresses().get(0).getChannel();
			JE802HopInfo nextHop = aMpdu.getHopAddresses().get(0);
			JE802_11Mac mac = macDot11Map.get(nextChannel);
			if (mac != null) {
				parameterlist = new Vector<Object>();
				parameterlist.add(nextHop); // Destination Address of MPDU
				parameterlist.add(aMpdu.getAC());
				parameterlist.add(aMpdu.getHopAddresses());
				parameterlist.add(aMpdu.getPayload());
				parameterlist.add(aMpdu.getSeqNo());
				parameterlist.add(aMpdu.getSourceHandler());
				JEEvent groupForwardEvent = new JEEvent("groupForwardEvent", mac.getHandlerId(), theUniqueEventScheduler.now(),
						parameterlist);
				this.send(groupForwardEvent);
			} else {
				this.error("Station " + this.getAddress() + " does not know channel " + nextChannel);
			}

		} else if (anEventName.equals("start_req")) {
			this.parameterlist.clear();
			// this.send(new JEEvent("listen_for_pages_req",
			// this.macDot15Map.values().iterator().next().getHandlerId(),
			// theUniqueEventScheduler.now()));
		} else if (anEventName.equals("Channel_Switch_req")) {
			Integer switchFrom = (Integer) anEvent.getParameterList().get(0);
			Integer switchTo = (Integer) anEvent.getParameterList().get(1);
			JE802_11Mac macThatSwitches = macDot11Map.get(switchFrom);

			if (macDot11Map.get(switchTo) != null) {
				System.err.println("Switching to channel at station" + this.getAddress());
			}
			// change assigned channel of mac
			// this.message("Switching from " + switchFrom + " to " + switchTo +
			// " at Station" + this.getAddress());
			macDot11Map.remove(switchFrom);
			macDot11Map.put(switchTo, macThatSwitches);
			this.parameterlist = new Vector<Object>();
			this.parameterlist.add(switchTo);
			this.send(new JEEvent("Channel_Switch_req", macThatSwitches.getHandlerId(), theUniqueEventScheduler.now(),
					this.parameterlist));

		} else if (anEventName.equals("IP_Deliv_req")) {
			JE802HopInfo nextHop = (JE802HopInfo) anEvent.getParameterList().get(0);
			boolean sent = false;
			if (wiredStations != null) {
				for (JE802Station station : wiredStations) {
					if (station.getMacAddress() == nextHop.getAddress() || nextHop.getAddress() == 255) {
						seqNo++;
						anEvent.getParameterList().add(seqNo);
						anEvent.getParameterList().add(this.getAddress());
						this.send(new JEEvent("wiredForward", station.getSme().getHandlerId(), now, anEvent.getParameterList()));
						sent = true;
					}
				}
			}
			if (!sent || nextHop.getAddress() == 255) {
				int channel = nextHop.getChannel();
				JE802_11Mac macOnChannel = macDot11Map.get(channel);
				anEvent.getParameterList().add(seqNo);
				anEvent.getParameterList().add(this.getHandlerId());
				seqNo++;
				if (macOnChannel != null) {
					this.send(new JEEvent("MSDUDeliv_req", macOnChannel.getHandlerId(), now, anEvent.getParameterList()));
				} else {
					this.warning("Station " + this.getAddress() + " does not know channel " + channel);
				}
			}
		} else if (anEventName.equals("wiredForward")) {
			JE802IPPacket packet = (JE802IPPacket) anEvent.getParameterList().get(3);
			Integer ac = (Integer) anEvent.getParameterList().get(1);
			JE802HopInfo hop = (JE802HopInfo) anEvent.getParameterList().get(0);
			Long sequenceNo = (Long) anEvent.getParameterList().get(4);
			Integer sa = (Integer) anEvent.getParameterList().get(5);
			Vector<Object> parameterList = new Vector<Object>();
			parameterList.add(packet);
			parameterList.add(now);
			parameterList.add(ac);
			parameterList.add(hop.getChannel());
			parameterList.add(sequenceNo);
			parameterList.add(sa);
			this.send(new JEEvent("packet_exiting_system_ind", this.ipHandlerId, now, parameterList));

		} else if (anEventName.equals("broadcast_sent")) {
			this.send(new JEEvent("broadcast_sent", channelHandlerId, anEvent.getScheduledTime(), anEvent.getParameterList()));

		} else if (anEventName.equals("hop_evaluation")) {

			JE802_11Mpdu aMpdu = (JE802_11Mpdu) anEvent.getParameterList().get(0);
			anEvent.getParameterList().setElementAt(aMpdu.getPayload(), 0);
			anEvent.getParameterList().setElementAt(aMpdu.getSeqNo(), 2);
			anEvent.getParameterList().add(this.getAddress());
			this.send(new JEEvent("hop_evaluation", this.ipHandlerId, anEvent.getScheduledTime(), anEvent.getParameterList()));

		} else if (anEventName.equals("empty_queue_ind")) {

			this.send(new JEEvent("empty_queue_ind", this.ipHandlerId, anEvent.getScheduledTime(), anEvent.getParameterList()));

		} else if (anEventName.equals("packet_exiting_system_ind")) {

			JE802_11Mpdu aMpdu = (JE802_11Mpdu) anEvent.getParameterList().get(0);
			anEvent.getParameterList().setElementAt(aMpdu.getPayload(), 0);
			// mac sequence number
			anEvent.getParameterList().add(aMpdu.getSeqNo());
			anEvent.getParameterList().add(aMpdu.getSA());
			this.send(new JEEvent("packet_exiting_system_ind", this.ipHandlerId, now, anEvent.getParameterList()));

		} else if (anEventName.equals("MSDU_discarded_ind")) {
			JE802_11Mpdu aMPDU = (JE802_11Mpdu) anEvent.getParameterList().get(0);

			Integer channel = (Integer) anEvent.getParameterList().get(2);
			Integer retries = (Integer) anEvent.getParameterList().get(1);
			this.parameterlist = new Vector<Object>();
			this.parameterlist.add(aMPDU.getPayload());
			this.parameterlist.add(retries);
			this.parameterlist.add(aMPDU.getDA());
			this.parameterlist.add(channel);
			this.send(new JEEvent("IPPacket_discarded_ind", ipHandlerId, theUniqueEventScheduler.now(), this.parameterlist));
		} else if (anEventName.equals("MSDU_delivered_ind")) {
			this.send(new JEEvent("IPPacket_delivered_ind", ipHandlerId, now, anEvent.getParameterList()));
		} else if (anEventName.equals("push_back_packet")) {
			JE802_11Mpdu aMpdu = (JE802_11Mpdu) anEvent.getParameterList().get(0);
			Integer channel = (Integer) anEvent.getParameterList().get(1);
			Vector<Object> params = new Vector<Object>();
			params.add(aMpdu.getPayload());
			params.add(channel);
			params.add(aMpdu.getDA());
			this.send(new JEEvent("push_back_packet", channelHandlerId, now, params));
		} else {
			this.error("undefined event '" + anEventName + "' in state " + this.theState.toString());
		}
	}

	public int getAddress() { // TODO: Pietro: this method should be generalized
								// to all MACs
		if (macDot11Map != null) {
			return this.macDot11Map.values().iterator().next().getMacAddress();
		}
		return 0;
	}

	public List<JEWirelessChannel> getAvailableChannels() { // TODO Pietro: this
															// method should be
															// generalized to
															// all MACs
		if (this.macDot11Map != null) {
			message("80211Mac channels", 10);
			return this.macDot11Map.values().iterator().next().getPhy().getAvailableChannels();
		} else {
			return null;
		}
	}

	public List<Integer> getChannelsInUse() { // TODO Pietro: this method should
												// be generalized to all MACs
		List<Integer> channels = new ArrayList<Integer>();
		if (this.macDot11Map != null) {
			for (JE802_11Mac mac : macDot11Map.values()) {
				channels.add(mac.getChannel());
			}
		}
		return channels;
	}

	public void setMacs(List<JE802_11Mac> macs) { // TODO Pietro: this method
													// should be generalized to
													// all MACs
		this.macDot11Map = new HashMap<Integer, JE802_11Mac>();
		for (JE802_11Mac mac : macs) {
			macDot11Map.put(mac.getChannel(), mac);
		}
	}

	public void checkQueueSize(int size) { // TODO Pietro: this method should be
											// generalized to all MACs
		if (this.macDot11Map != null) {
			for (JE802_11Mac mac : macDot11Map.values()) {
				mac.checkQueueSize(size);
			}
		}
	}

	/*
	 * public Map<Integer, Double> getETT() { for (JE802_11Mac mac :
	 * macMap.values()) { int phyMode =
	 * mac.getPhy().getCurrentPhyMode().getRateMbps(); double bytePerSecond =
	 * phyMode / 8.0 * 1E6; double etx = mac.getETX(); double ett = etx * (1024
	 * / bytePerSecond); ettMap.put(mac.getChannel(), ett); } return ettMap; }
	 */

	/*
	 * public double getETT(int channel) { JE802_11Mac mac =
	 * macMap.get(channel); if (mac != null) { int phyMode =
	 * mac.getPhy().getCurrentPhyMode().getRateMbps(); double bytePerSecond =
	 * phyMode / 8.0 * 1E6; double etx = mac.getETX(); double ett = etx * (1024
	 * / bytePerSecond); return ett; } else { return 0; } }
	 */

	public void setIpHandlerId(int tcpHandlerId) {
		this.ipHandlerId = tcpHandlerId;
	}

	public void setChannelHandlerId(int channelHandlerId) {
		this.channelHandlerId = channelHandlerId;
	}

	@Override
	public String toString() {
		return "Sme at station " + this.getAddress();
	}

	public void setWiredStations(List<JE802Station> wiredStations) {
		if (wiredStations != null && wiredStations.isEmpty()) {
			this.wiredStations = null;
		} else {
			this.wiredStations = wiredStations;
		}
	}

}
