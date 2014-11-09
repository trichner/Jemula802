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

import kernel.JEmula;
import layer2_802Algorithms.controller.RRMController;
import layer2_802Algorithms.RRMInput;
import plot.JEMultiPlotter;

public abstract class JE802_11MacAlgorithm extends JEmula {

	protected JE802_11Mac mac;
	protected final Integer dot11MACAddress;
	protected JEMultiPlotter plotter;
	protected String algorithmName;
    protected JE802_11BackoffEntity theBackoffEntity;

    protected RRMController controller;

	public JE802_11MacAlgorithm(String name, JE802_11Mac mac) {
		this.mac = mac;
		this.algorithmName = name;
		this.dot11MACAddress = this.mac.getMacAddress();
		this.theUniqueEventScheduler = this.mac.getTheUniqueEventScheduler();
	}


    protected RRMInput prepareInput(){
        int aAIFSN      = this.theBackoffEntity.getDot11EDCAAIFSN();
        int aCWmin      = this.theBackoffEntity.getDot11EDCACWmin();
        int aQueueSize  = this.theBackoffEntity.getQueueSize();
        int aCurrentQueueSize = this.theBackoffEntity.getCurrentQueueSize();
        double txPower  = this.mac.getPhy().getCurrentTransmitPower_mW();
        String phyMode  = this.mac.getPhy().getCurrentPhyMode().getName();
        int collisions  = this.theBackoffEntity.getCollisionCount();
        int discardes   = this.theBackoffEntity.getDiscardedCounter();
        return new RRMInput(aAIFSN,aCWmin,collisions,discardes,aQueueSize,aCurrentQueueSize,txPower,
                phyMode);
    }

	public abstract void compute();

	public abstract void plot();


}