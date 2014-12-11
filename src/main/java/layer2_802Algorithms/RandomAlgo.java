package layer2_802Algorithms;

import plot.JEMultiPlotter;
import layer2_80211Mac.JE802_11BackoffEntity;
import layer2_80211Mac.JE802_11Mac;
import layer2_80211Mac.JE802_11MacAlgorithm;

import java.util.Random;

/**
 * Created by marcel on 12/11/14.
 */
public class RandomAlgo extends JE802_11MacAlgorithm {

    private JE802_11BackoffEntity theBackoffEntityAC01;
    private JE802_11BackoffEntity theBackoffEntityAC02;
    private boolean flag_undefined = false;

    public RandomAlgo(String name, JE802_11Mac mac) {
        super(name, mac);
        this.theBackoffEntityAC01 = this.mac.getBackoffEntity(1);
        this.theBackoffEntityAC02 = this.mac.getBackoffEntity(2);
    }

    @Override
    public void compute() {
        message("---------------------------", 10);
        message("I am station " + this.dot11MACAddress.toString() +". My algorithm is called '" + this.algorithmName + "'.", 10);

        // observe outcome:
        Integer AIFSN_AC01 = theBackoffEntityAC01.getDot11EDCAAIFSN();
        Integer AIFSN_AC02 = theBackoffEntityAC02.getDot11EDCAAIFSN();
        Integer CWmin_AC01 = theBackoffEntityAC01.getDot11EDCACWmin();
        Integer CWmin_AC02 = theBackoffEntityAC02.getDot11EDCACWmin();

        message("with the following contention window parameters ...", 10);
        message("    AIFSN[AC01] = " + AIFSN_AC01.toString() + " and AIFSN[AC02] = " + AIFSN_AC02.toString(), 10);
        message("    CWmin[AC01] = " + CWmin_AC01.toString() + " and CWmin[AC02] = " + CWmin_AC02.toString(), 10);
        message("... the backoff entity queues perform like this:", 10);

        Random random = new Random();

        // infer decision: (note, we just change the values arbitrarily
        if (random.nextBoolean()) { // we should increase AIFSN
            AIFSN_AC01 = AIFSN_AC01 + 1;
        } else { // we should decrease AIFSN
            AIFSN_AC01 = AIFSN_AC01 - 1;
        }
        if (AIFSN_AC01 >= 20)
            AIFSN_AC01 = 20;
        if (AIFSN_AC01 <= 2)
            AIFSN_AC01 = 2;

        // AIFSN_AC02 = 10;
        // CWmin_AC02 = 5;

        // act:
        theBackoffEntityAC01.setDot11EDCAAIFSN(AIFSN_AC01);
        theBackoffEntityAC02.setDot11EDCAAIFSN(AIFSN_AC02);
        theBackoffEntityAC01.setDot11EDCACWmin(CWmin_AC01);
        theBackoffEntityAC02.setDot11EDCACWmin(CWmin_AC02);
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
