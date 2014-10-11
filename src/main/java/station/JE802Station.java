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
import java.util.List;
import java.util.Random;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import kernel.JEEvent;
import kernel.JEEventScheduler;
import kernel.JETime;
import kernel.JEmula;
import layer0_medium.JEWirelessMedium;
import layer1_80211Phy.JE802_11Phy;
import layer1_802Phy.JE802Mobility;
import layer1_802Phy.JE802Phy;
import layer1_802Phy.JE802PhyMode;
import layer2_802Mac.JE802_Mac;
import layer2_80211Mac.JE802_11Mac;
import layer3_network.JE802RouteManager;
import layer4_transport.JE802TCPManager;
import layer5_application.JE802TrafficGen;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import emulator.JE802StatEval;
import gui.JE802Gui;

public class JE802Station extends JEmula {

	private List<JE802TrafficGen> trafficGenerators;

	private JE802_11Mac theMac;

	private final JE802StatEval statEval;

	private JE802Mobility mobility;

	private List<Integer> wiredAddresses;

	private JE802Sme theSme;

	private JE802RouteManager ipLayer;

	private JE802TCPManager tcp;

	private JE802Phy thePhy;

	private final XPath xpath = XPathFactory.newInstance().newXPath();

	public JE802Station(JEEventScheduler aScheduler, JEWirelessMedium aChannel,
			Random aGenerator, JE802Gui aGui, JE802StatEval aStatEval,
			Node topLevelNode, List<JE802PhyMode> phyModes, double longitude,
			double latitude) throws XPathExpressionException {
		Element aTopLevelNode = (Element) topLevelNode;
		this.theUniqueEventScheduler = aScheduler;
		this.statEval = aStatEval;
		if (aTopLevelNode.getNodeName().equals("JE802Station")) {
			this.message("XML definition " + aTopLevelNode.getNodeName()
					+ " found.", 1);

			this.trafficGenerators = new ArrayList<JE802TrafficGen>();

			if (aTopLevelNode.hasChildNodes()) {
				// ------------------------------------------------------------------------------------------------
				// -- create SME (Station Management Entity):
				Node smeNode = (Node) xpath.evaluate("JE802SME", aTopLevelNode,
						XPathConstants.NODE);
				if (smeNode == null) {
					this.error("No JE802SME element found in XML scenario file.");
				}
				this.message("allocating " + smeNode.getNodeName(),10);
				this.theSme = new JE802Sme(aScheduler, aGenerator, smeNode);

				// ----------------------------------------------------------------------------------------
				// -- create mobility:
				Node mobNode = (Node) xpath.evaluate("JE802Mobility",
						aTopLevelNode, XPathConstants.NODE);
				this.message("allocating " + mobNode.getNodeName(), 10);
				this.mobility = new JE802Mobility(mobNode, longitude, latitude);

				// ----------------------------------------------------------------------------------------
				// create PHY layer:
				Node phyNode = (Node) this.xpath.evaluate("JE80211PHY",
						aTopLevelNode, XPathConstants.NODE);
				if (phyNode == null) {
					this.error("No JE80211PHY element found in XML scenario file.");
				}
				this.thePhy = new JE802_11Phy(aScheduler, statEval, aGenerator,
						aChannel, aGui, phyNode);
				this.thePhy.setThePhyModeList(phyModes);
				this.thePhy.setMobility(this.mobility);
				this.thePhy.send(new JEEvent("start_req", this.thePhy,
						theUniqueEventScheduler.now()));

				// ----------------------------------------------------------------------------------------
				// -- create MAC layer:
				Node macNode = (Node) xpath.evaluate("JE80211MAC",
						aTopLevelNode, XPathConstants.NODE);
				if (macNode == null) {
					this.error("No JE80211Mac found.");
				} else {
					this.message("allocating " + macNode.getNodeName(), 10);
					JE802_11Mac aMac = new JE802_11Mac(aScheduler, aStatEval,
							aGenerator, aGui, aChannel, macNode,
							this.theSme.getHandlerId(),this.thePhy);
					this.theMac = aMac;
					this.theMac.getMlme().getTheAlgorithm().compute();

					this.theSme.setMac(this.theMac);
					this.thePhy.setMac(this.theMac);
					this.theMac.setPhy(this.thePhy);
					
					this.ipLayer = new JE802RouteManager(aScheduler,aGenerator, this.theSme, statEval);
					this.theSme.setIpHandlerId(this.ipLayer.getHandlerId());
				}

				// ------------------------------------------------------------------------------------------------
				// -- create TCP Manager:
				Node tcpNode = (Node) xpath.evaluate("JE802TCP", aTopLevelNode,
						XPathConstants.NODE);
				this.tcp = new JE802TCPManager(aScheduler, aGenerator, tcpNode,
						this.ipLayer, this.statEval);
				this.ipLayer.setTcpHandlerId(tcp.getHandlerId());

				// ----------------------------------------------------------------------------------------
				// -- create traffic generators:
				NodeList tgList = (NodeList) xpath.evaluate("JE802TrafficGen",
						aTopLevelNode, XPathConstants.NODESET);
				for (int i = 0; i < tgList.getLength(); i++) {
					Node tgNode = tgList.item(i);
					this.message("allocating " + tgNode.getNodeName(), 10);
					JE802TrafficGen aNewTrafficGen = new JE802TrafficGen(
							aScheduler, aGenerator, tgNode,
							this.theMac.getMacAddress(), aStatEval, tcp);
					this.trafficGenerators.add(aNewTrafficGen);
					// stop generating traffic, usually not used:
					aNewTrafficGen.send(new JEEvent("stop_req", aNewTrafficGen,
							aNewTrafficGen.getStopTime()));
					// start generating traffic:
					aNewTrafficGen.send(new JEEvent("start_req",
							aNewTrafficGen, aNewTrafficGen.getStartTime()));
				}
			} else {
				this.message("XML definition " + aTopLevelNode.getNodeName()
						+ " has no child nodes!", 10);
			}
		} else {
			this.message("XML definition " + aTopLevelNode.getNodeName()
					+ " found, but JE802Station expected!", 10);
		}

		if (aGui != null) {
			aGui.setupStation(this.theMac.getMacAddress());
		}

	}

	public JE802_Mac getMac() {
		return theMac;
	}

	public double getXLocation(JETime time) {
		return this.mobility.getXLocation(time);
	}

	public double getYLocation(JETime time) {
		return this.mobility.getYLocation(time);
	}

	public double getZLocation(JETime time) {
		return this.mobility.getZLocation(time);
	}

	public boolean isMobile() {
		return this.mobility.isMobile();
	}

	public List<JE802TrafficGen> getTrafficGenList() {
		return this.trafficGenerators;
	}

	public JE802StatEval getStatEval() {
		return statEval;
	}

	public JE802Sme getSme() {
		return this.theSme;
	}

	public JE802Phy getPhy() {
		return this.thePhy;
	}

	public double getTransmitPowerLeveldBm() {
		return this.getPhy().getCurrentTransmitPowerLevel_dBm();
	}

	public JE802Mobility getMobility() {
		return mobility;
	}

	public long getLostPackets() {
		return tcp.getLostPackets();
	}

	public List<Integer> getWiredAddresses() {
		return this.wiredAddresses;
	}

	public long getTransmittedPackets() {
		return tcp.getTransmittedPackets();
	}

	public void displayLossrate() {
		tcp.retransmissionRate();
	}

	@Override
	public String toString() {
		return Integer.toString(theMac.getMacAddress());
	}
}
