package layer2_802Algorithms.controller;

import layer2_802Algorithms.PhyMinion;
import layer2_802Algorithms.RRMConfig;
import layer2_802Algorithms.RRMInput;

import java.util.Random;

/**
 * Created by trichner on 11/3/14.
 */
public class PhyController extends StatefulController{
    private static Random rand = new Random();

    private int step = 0;
    private double previous = 0;
    private String prevPhy = PhyMinion.min();
    @Override
    protected RRMConfig evaluate(RRMInput input) {
        String phymode = input.getCurrentPhyMode();
        step++;

        // static TX Power, 100mW
        double txPower = 80;

        // average collisions over the last WINDOW values
        double WINDOW = 10;
        double iCollisions = this.collisions.intLast((int)WINDOW)/WINDOW;

        // MAGIC NUMBER
        int C_THRESHOLD = 25;

        // only allow changes every WINDOW steps
        if(step %WINDOW==0) {
            if (iCollisions >= C_THRESHOLD) {
                phymode = PhyMinion.increase(phymode);
            } else {
                phymode = PhyMinion.decrease(phymode);
            }
        }

        return new RRMConfig(phymode,txPower);
    }
}
