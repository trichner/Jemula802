package layer2_802Algorithms.controller;

import layer2_802Algorithms.PhyMinion;
import layer2_802Algorithms.RRMConfig;
import layer2_802Algorithms.RRMInput;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created on 02.11.2014.
 *
 * @author Thomas
 */
public class TXPowerController extends StatefulController {

    private static final int TX_POWER = 1000;

    private File logFile;
    private BufferedWriter writer;

    public TXPowerController() {
        super();

        logFile = new File("log_"+TX_POWER + "-"+System.currentTimeMillis()+".txt");
        try {
            writer = new BufferedWriter(new FileWriter(logFile));
            writer.write("collision,discarded,queueSize");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected RRMConfig evaluate(RRMInput input) {
        double txPower = input.getCurrentTxPower();
        String phymode = input.getCurrentPhyMode();

        // do compute

        // c_n, d_n
        int collision = collisions.getLast();
        int discarded = discards.getLast();

        // c'_n, d'_n
        int dCollision = collisions.diffLast();
        int dDiscarded = discards.diffLast();

        // integrate
        int window = 10;
        int ref = 20;
        int iCollision = collisions.intLast(window,ref);
        int iDiscarded = discards.intLast(window,ref);


        int dQueueSize = queueSizes.diffLast();
        int queueSize = queueSizes.getLast();

        int iQueueSize = queueSizes.intLast(window,10);

        // do calculate

        try {
            writer.write(collision + "," + discarded + "," + queueSize + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // static phymode
        phymode = PhyMinion.max(); //PhyMinion.offset(PhyMinion.max(),-2);
        txPower = TX_POWER;

        //txPower += P*iQueueSize;



        // queue got bigger
        // causes:
        // -phy mode to slow    (-> increase phymode)
        // -too many collisions (-> reduce power, increase phymode)
        // -too many transmission errors (-> increase power, if full, decrease phymode)

        // more collisions
        // causes:
        // - to much contention -> stay fair
        // - to much interference -> reduce power

        // increased discards
        // causes:
        // - to much contention -> stay fair
        // - to much transmission errors -> increase power/decrease phy mode

        return new RRMConfig(phymode,txPower);
    }
}
