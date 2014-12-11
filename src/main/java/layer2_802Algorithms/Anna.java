package layer2_802Algorithms;

import layer2_80211Mac.JE802_11BackoffEntity;
import layer2_80211Mac.JE802_11Mac;
import layer2_80211Mac.JE802_11MacAlgorithm;
import plot.JEMultiPlotter;

/**
 * Created by marcel on 12/7/14.
 */
public class Anna extends JE802_11MacAlgorithm {

    private JE802_11BackoffEntity theBackoffEntityAC01;
    private JE802_11BackoffEntity theBackoffEntityAC02;
    private boolean flag_undefined = false;
    private static int count = 0;

    private int step;

    public Anna(String name, JE802_11Mac mac) {
        super(name, mac);
        count++;
        this.theBackoffEntityAC01 = this.mac.getBackoffEntity(1);
        this.theBackoffEntityAC02 = this.mac.getBackoffEntity(2);
    }

    @Override
    public void compute() {
        this.step++;
        message("---------------------------", 80);
        message("I am station " + this.dot11MACAddress.toString() +". My algorithm is called '" + this.algorithmName + "'.", 80);
        message("Count: " + String.valueOf(count));
        message("Step:" + String.valueOf(this.step));
        // observe outcome:
        Integer AIFSN_AC01 = theBackoffEntityAC01.getDot11EDCAAIFSN();
        Integer AIFSN_AC02 = theBackoffEntityAC02.getDot11EDCAAIFSN();
        Integer CWmin_AC01 = theBackoffEntityAC01.getDot11EDCACWmin();
        Integer CWmin_AC02 = theBackoffEntityAC02.getDot11EDCACWmin();
        Integer CWmax_AC01 = theBackoffEntityAC01.getDot11EDCACWmax();
        Integer CWmax_AC02 = theBackoffEntityAC02.getDot11EDCACWmax();


//        theBackoffEntityAC01.getQueueSize();
//        theBackoffEntityAC02.getQueueSize();
//        theBackoffEntityAC01.getCurrentQueueSize();
//        theBackoffEntityAC02.getCurrentQueueSize();

        message("with the following contention window parameters ...", 80);
        message("    AIFSN[AC01] = " + AIFSN_AC01.toString() + " and AIFSN[AC02] = " + AIFSN_AC02.toString(), 80);
        message("    CWmin[AC01] = " + CWmin_AC01.toString() + " and CWmin[AC02] = " + CWmin_AC02.toString(), 80);
        message("    CWmax[AC01] = " + CWmax_AC01.toString() + " and CWmax[AC02] = " + CWmax_AC02.toString(), 80);
        message("... the backoff entity queues perform like this:", 80);

        if(this.dot11MACAddress%count == this.step%count) {
            AIFSN_AC01 = AnnaConfig.EVIL_AC1_AIFSN;
            CWmin_AC01 = AnnaConfig.EVIL_AC1_CWMIN;
            CWmax_AC01 = AnnaConfig.EVIL_AC1_CWMAX;
            AIFSN_AC02 = AnnaConfig.EVIL_AC2_AIFSN;
            CWmin_AC02 = AnnaConfig.EVIL_AC2_CWMIN;
            CWmax_AC02 = AnnaConfig.EVIL_AC1_CWMAX;
        } else if (count>1) {
            AIFSN_AC01 = AnnaConfig.FAIR_AC1_AIFSN;
            CWmin_AC01 = AnnaConfig.FAIR_AC1_CWMIN;
            CWmax_AC01 = AnnaConfig.FAIR_AC1_CWMAX;
            AIFSN_AC02 = AnnaConfig.FAIR_AC2_AIFSN;
            CWmin_AC02 = AnnaConfig.FAIR_AC2_CWMIN;
            CWmax_AC02 = AnnaConfig.FAIR_AC1_CWMAX;
        }
        // act:
        theBackoffEntityAC01.setDot11EDCAAIFSN(AIFSN_AC01);
        theBackoffEntityAC02.setDot11EDCAAIFSN(AIFSN_AC02);
        theBackoffEntityAC01.setDot11EDCACWmin(CWmin_AC01);
        theBackoffEntityAC02.setDot11EDCACWmin(CWmin_AC02);
        theBackoffEntityAC01.setDot11EDCACWmin(CWmax_AC01);
        theBackoffEntityAC02.setDot11EDCACWmin(CWmax_AC02);
    }

    @Override
    public void plot() {
        if (plotter == null) {
            plotter = new JEMultiPlotter("", "AIFSN(AC01)", "time [ms]", this.algorithmName + "", 10000, true);
            plotter.addSeries("CWmin(AC01)");
            plotter.addSeries("queue(AC01)");
            plotter.addSeries("AIFSN(AC02)");
            plotter.addSeries("CWmin(AC02)");
            plotter.addSeries("queue(AC02)");
            plotter.display();
        }
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