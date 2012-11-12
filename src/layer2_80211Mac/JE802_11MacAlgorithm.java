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

package layer2_80211Mac;

//import java.util.*;
//import java.lang.Math;
//import kernel.JETime;

import java.util.Random;
import java.util.Vector;

import kernel.JEEventScheduler;
import kernel.JEmula;
import plot.JEMultiPlotter;

/** @author Stefan Mangold */
public class JE802_11MacAlgorithm extends JEmula {

	private JE802_11BackoffEntity theBackoffEntityAC01;
	private JE802_11BackoffEntity theBackoffEntityAC02;
	private final JE802_11Mac theMac;
	private boolean flag_undefined;
	private final boolean flag_showplot;
	private final String theAlgorithmName;
	private final Integer dot11MACAddress;
	private JEMultiPlotter plotter;

	public JE802_11MacAlgorithm(JEEventScheduler aScheduler, Vector<JE802_11BackoffEntity> aListofBackoffEntities,
			JE802_11Mac aMac, Random aGenerator, String anAlgorithm, boolean showplot) {

		this.theUniqueEventScheduler = aScheduler;

		if (aListofBackoffEntities.size() >= 1) {
			if (aListofBackoffEntities.elementAt(0) != null) {
				this.theBackoffEntityAC01 = aListofBackoffEntities.elementAt(0);
			} else {
				this.theBackoffEntityAC01 = null;
			}
		} else {
			this.theBackoffEntityAC01 = null;
		}
		if (aListofBackoffEntities.size() >= 2) {
			if (aListofBackoffEntities.elementAt(1) != null) {
				this.theBackoffEntityAC02 = aListofBackoffEntities.elementAt(1);
			} else {
				this.theBackoffEntityAC02 = null;
			}
		} else {
			this.theBackoffEntityAC02 = null;
		}

		this.theMac = aMac;
		this.dot11MACAddress = this.theMac.getMacAddress();

		this.flag_undefined = true;

		this.flag_showplot = showplot;

		this.theAlgorithmName = anAlgorithm;

		if (this.flag_showplot) {
			if (plotter == null) {
				plotter = new JEMultiPlotter("", "AIFSN(AC01)", "time [ms]", this.theAlgorithmName + "", 10000, true);
				plotter.addSeries("CWmin(AC01)");
				plotter.addSeries("queue(AC01)");
				plotter.addSeries("AIFSN(AC02)");
				plotter.addSeries("CWmin(AC02)");
				plotter.addSeries("queue(AC02)");
				plotter.display();
			}
		}
	}

	public void compute() {

		if (this.theAlgorithmName.equalsIgnoreCase("none")) {

		} else if (this.theAlgorithmName.contains("power")) {
			this.compute_TxPower();
		} else if (this.theAlgorithmName.contains("phymode")) {
			this.compute_phymode();
		} else if (this.theAlgorithmName.equalsIgnoreCase("tutorial")) {
			this.compute_tutorial_verbose();
		} else if (this.theAlgorithmName.equalsIgnoreCase("tutorial_verbose")) {
			this.compute_tutorial_verbose();
		} else
			warning("undefined algorithm " + this.theAlgorithmName.toString());

		if (this.flag_showplot) {
			plotter.plot(((Double) theUniqueEventScheduler.now().getTimeMs()).doubleValue(),
					theBackoffEntityAC01.getDot11EDCAAIFSN(), 0);
			plotter.plot(((Double) theUniqueEventScheduler.now().getTimeMs()).doubleValue(),
					theBackoffEntityAC01.getDot11EDCACWmin(), 1);
			plotter.plot(((Double) theUniqueEventScheduler.now().getTimeMs()).doubleValue(),
					theBackoffEntityAC01.getCurrentQueueSize(), 2);
			plotter.plot(((Double) theUniqueEventScheduler.now().getTimeMs()).doubleValue(),
					theBackoffEntityAC02.getDot11EDCAAIFSN(), 3);
			plotter.plot(((Double) theUniqueEventScheduler.now().getTimeMs()).doubleValue(),
					theBackoffEntityAC02.getDot11EDCACWmin(), 4);
			plotter.plot(((Double) theUniqueEventScheduler.now().getTimeMs()).doubleValue(),
					theBackoffEntityAC02.getCurrentQueueSize(), 5);
		}

	}

	private void compute_tutorial_verbose() {

		message("---------------------------", 10);
		message("I am station " + this.dot11MACAddress.toString() +". My algorithm is called '" + this.theAlgorithmName + "'.", 10);

		// observe outcome:
		Integer AIFSN_AC01 = theBackoffEntityAC01.getDot11EDCAAIFSN();
		Integer AIFSN_AC02 = theBackoffEntityAC02.getDot11EDCAAIFSN();
		Integer CWmin_AC01 = theBackoffEntityAC01.getDot11EDCACWmin();
		Integer CWmin_AC02 = theBackoffEntityAC02.getDot11EDCACWmin();

		theBackoffEntityAC01.getQueueSize();
		theBackoffEntityAC02.getQueueSize();
		theBackoffEntityAC01.getCurrentQueueSize();
		theBackoffEntityAC02.getCurrentQueueSize();

		message("with the following contention window parameters ...", 10);
		message("    AIFSN[AC01] = " + AIFSN_AC01.toString() + " and AIFSN[AC02] = " + AIFSN_AC02.toString(), 10);
		message("    CWmin[AC01] = " + CWmin_AC01.toString() + " and CWmin[AC02] = " + CWmin_AC02.toString(), 10);
		message("... the backoff entity queues perform like this:", 10);
		
		// infer decision: (note, we just change the values arbitrarily
		if (flag_undefined) { // we should increase AIFSN
			AIFSN_AC01 = AIFSN_AC01 + 1;
		} else { // we should decrease AIFSN
			AIFSN_AC01 = AIFSN_AC01 - 1;
		}
		if (AIFSN_AC01 >= 20)
			flag_undefined = false;
		if (AIFSN_AC01 <= 2)
			flag_undefined = true;

		// AIFSN_AC02 = 10;
		// CWmin_AC02 = 5;

		// act:
		theBackoffEntityAC01.setDot11EDCAAIFSN(AIFSN_AC01);
		theBackoffEntityAC02.setDot11EDCAAIFSN(AIFSN_AC02);
		theBackoffEntityAC01.setDot11EDCACWmin(CWmin_AC01);
		theBackoffEntityAC02.setDot11EDCACWmin(CWmin_AC02);
	}


	private void compute_phymode() {

		if (this.theAlgorithmName.equals("phymode_06Mbps")) {
			if (!this.theMac.getPhy().getCurrentPhyMode().toString().equals("BPSK12"))
				this.theMac.getPhy().setCurrentPhyMode("BPSK12");
		} else if (this.theAlgorithmName.equals("phymode_09Mbps")) {
			if (!this.theMac.getPhy().getCurrentPhyMode().toString().equals("BPSK34"))
				this.theMac.getPhy().setCurrentPhyMode("BPSK34");
		} else if (this.theAlgorithmName.equals("phymode_12Mbps")) {
			if (!this.theMac.getPhy().getCurrentPhyMode().toString().equals("QPSK12"))
				this.theMac.getPhy().setCurrentPhyMode("QPSK12");
		} else if (this.theAlgorithmName.equals("phymode_18Mbps")) {
			if (!this.theMac.getPhy().getCurrentPhyMode().toString().equals("QPSK34"))
				this.theMac.getPhy().setCurrentPhyMode("QPSK34");
		} else if (this.theAlgorithmName.equals("phymode_24Mbps")) {
			if (!this.theMac.getPhy().getCurrentPhyMode().toString().equals("16QAM12"))
				this.theMac.getPhy().setCurrentPhyMode("16QAM12");
		} else if (this.theAlgorithmName.equals("phymode_36Mbps")) {
			if (!this.theMac.getPhy().getCurrentPhyMode().toString().equals("16QAM34"))
				this.theMac.getPhy().setCurrentPhyMode("16QAM34");
		} else if (this.theAlgorithmName.equals("phymode_48Mbps")) {
			if (!this.theMac.getPhy().getCurrentPhyMode().toString().equals("64QAM23"))
				this.theMac.getPhy().setCurrentPhyMode("64QAM23");
		} else if (this.theAlgorithmName.equals("phymode_54Mbps")) {
			if (!this.theMac.getPhy().getCurrentPhyMode().toString().equals("64QAM34"))
				this.theMac.getPhy().setCurrentPhyMode("64QAM34");
		}
	}

	private void compute_TxPower() {
		if (this.theBackoffEntityAC01.getCollisionCount() > this.theBackoffEntityAC01.getFaultToleranceThreshold()) {
			if (this.theMac.getPhy().getCurrentTransmitPowerLevel_dBm() < 0.0) {
				this.theMac.getPhy().setCurrentTransmitPowerLevel_dBm(0.0);
			} else if (this.theMac.getPhy().getCurrentTransmitPowerLevel_dBm() < 10.0) {
				this.theMac.getPhy().setCurrentTransmitPowerLevel_dBm(10.0);
			}
		}
	}

}