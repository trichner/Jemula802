package layer2_802Algorithms.controller;

import layer2_802Algorithms.PhyMinion;
import layer2_802Algorithms.RRMConfig;
import layer2_802Algorithms.RRMInput;

/**
 * Created by trichner on 11/3/14.
 */
public class APPhyController extends StatefulController{
    private int step = 0;
    private double prevFrac = 0;

    @Override
    protected RRMConfig evaluate(RRMInput input) {
        String phymode = input.getCurrentPhyMode();
        step++;

        // average collisions over the last WINDOW values
        double WINDOW = 5;
        double iCollisions = this.collisions.intLast((int)WINDOW)/WINDOW;
        double iQueueSize = this.queueSizes.intLast((int) WINDOW)/WINDOW;

        double fraction = iCollisions/iQueueSize;

        // only allow changes every WINDOW steps
        if(step %5==0) {
            if (fraction > prevFrac*1.1) {
                phymode = PhyMinion.decrease(phymode);

            } else {
                phymode = PhyMinion.increase(phymode);
            }
            prevFrac = fraction;
        }

        double TX_MAX = 600;
        // adjust TX, keep SNR leveled
        double speed = PhyMinion.getSpeed(phymode);
        double txPower = 100 + (speed-6)/48*TX_MAX;

        return new RRMConfig(phymode,txPower);
    }
}
