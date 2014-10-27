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

import java.util.List;
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

import org.w3c.dom.Node;

/** @author Stefan Mangold */
public class JE802Sme extends JEEventHandler {
	// SME = Station Management Entity

	private JE802_11Mac theMac;

	private int ipHandlerId;

	private int channelHandlerId;

	private long seqNo = 0;

	// private List<JE802Station> wiredStations;

	public JE802Sme(JEEventScheduler aScheduler, Random aGenerator,
			Node aTopLevelNode) {
		super(aScheduler, aGenerator);
		this.theState = state.active;
	}

	@Override
	public void event_handler(JEEvent anEvent) {
		JETime now = anEvent.getScheduledTime();
		String anEventName = anEvent.getName();

		if (anEventName.equals("packet_forward")) {
			JE802_11Mpdu aMpdu = (JE802_11Mpdu) anEvent.getParameterList().get(
					0);
			JE802HopInfo nextHop = aMpdu.getHopAddresses().get(0);
			parameterlist = new Vector<Object>();
			parameterlist.add(nextHop); // Destination Address of MPDU
			parameterlist.add(aMpdu.getAC());
			parameterlist.add(aMpdu.getHopAddresses());
			parameterlist.add(aMpdu.getPayload());
			parameterlist.add(aMpdu.getSeqNo());
			parameterlist.add(aMpdu.getSourceHandler());
			JEEvent groupForwardEvent = new JEEvent("groupForwardEvent",
					this.theMac.getHandlerId(), theUniqueEventScheduler.now(),
					parameterlist);
			this.send(groupForwardEvent);

		} else if (anEventName.equals("start_req")) {
			this.parameterlist.clear();

		} else if (anEventName.equals("Channel_Switch_req")) {
			Integer switchTo = (Integer) anEvent.getParameterList().get(1);
			this.parameterlist = new Vector<Object>();
			this.parameterlist.add(switchTo);
			this.send(new JEEvent("Channel_Switch_req", this.theMac
					.getHandlerId(), theUniqueEventScheduler.now(),
					this.parameterlist));

		} else if (anEventName.equals("IP_Deliv_req")) {
			anEvent.getParameterList().add(seqNo);
			anEvent.getParameterList().add(this.getHandlerId());
			seqNo++;
			this.send(new JEEvent("MSDUDeliv_req", this.theMac.getHandlerId(),
					now, anEvent.getParameterList()));
		} else if (anEventName.equals("broadcast_sent")) {
			this.send(new JEEvent("broadcast_sent", channelHandlerId, anEvent
					.getScheduledTime(), anEvent.getParameterList()));

		} else if (anEventName.equals("hop_evaluation")) {
			JE802_11Mpdu aMpdu = (JE802_11Mpdu) anEvent.getParameterList().get(
					0);
			anEvent.getParameterList().setElementAt(aMpdu.getPayload(), 0);
			anEvent.getParameterList().setElementAt(aMpdu.getSeqNo(), 2);
			anEvent.getParameterList().add(this.getAddress());
			this.send(new JEEvent("hop_evaluation", this.ipHandlerId, anEvent
					.getScheduledTime(), anEvent.getParameterList()));

		} else if (anEventName.equals("empty_queue_ind")) {
			this.send(new JEEvent("empty_queue_ind", this.ipHandlerId, anEvent
					.getScheduledTime(), anEvent.getParameterList()));

		} else if (anEventName.equals("packet_exiting_system_ind")) {
			JE802_11Mpdu aMpdu = (JE802_11Mpdu) anEvent.getParameterList().get(
					0);
			anEvent.getParameterList().setElementAt(aMpdu.getPayload(), 0);
			anEvent.getParameterList().add(aMpdu.getSeqNo()); // mac sequence
			// number
			anEvent.getParameterList().add(aMpdu.getSA());
			this.send(new JEEvent("packet_exiting_system_ind",
					this.ipHandlerId, now, anEvent.getParameterList()));

		} else if (anEventName.equals("MSDU_discarded_ind")) {
			JE802_11Mpdu aMPDU = (JE802_11Mpdu) anEvent.getParameterList().get(
					0);
			Integer channel = (Integer) anEvent.getParameterList().get(2);
			Integer retries = (Integer) anEvent.getParameterList().get(1);
			this.parameterlist = new Vector<Object>();
			this.parameterlist.add(aMPDU.getPayload());
			this.parameterlist.add(retries);
			this.parameterlist.add(aMPDU.getDA());
			this.parameterlist.add(channel);
			this.send(new JEEvent("IPPacket_discarded_ind", ipHandlerId,
					theUniqueEventScheduler.now(), this.parameterlist));
		} else if (anEventName.equals("MSDU_delivered_ind")) {
			this.send(new JEEvent("IPPacket_delivered_ind", ipHandlerId, now,
					anEvent.getParameterList()));
		} else if (anEventName.equals("push_back_packet")) {
			JE802_11Mpdu aMpdu = (JE802_11Mpdu) anEvent.getParameterList().get(
					0);
			Integer channel = (Integer) anEvent.getParameterList().get(1);
			Vector<Object> params = new Vector<Object>();
			params.add(aMpdu.getPayload());
			params.add(channel);
			params.add(aMpdu.getDA());
			this.send(new JEEvent("push_back_packet", channelHandlerId, now,
					params));
		} else {
			this.error("undefined event '" + anEventName + "' in state "
					+ this.theState.toString());
		}
	}

	public int getAddress() {
		return this.theMac.getMacAddress();
	}

	public Integer getChannelsInUse() {
		return this.theMac.getPhy().getCurrentChannel();
	}

	public List<JEWirelessChannel> getAvailableChannels() {
		return this.theMac.getPhy().getAvailableChannels();
	}

	public void setMac(JE802_11Mac mac) {
		this.theMac = mac;
	}

	public void checkQueueSize(int size) {
		this.theMac.checkQueueSize(size);
	}

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

}
