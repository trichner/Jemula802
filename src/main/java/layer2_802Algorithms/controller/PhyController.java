package layer2_802Algorithms.controller;

import layer2_802Algorithms.PhyMinion;
import layer2_802Algorithms.RRMConfig;
import layer2_802Algorithms.RRMInput;

/**
 * Created by trichner on 11/3/14.
 */
public class PhyController extends StatefulController{

    @Override
    protected RRMConfig evaluate(RRMInput input) {
        String phymode = input.getCurrentPhyMode();
        double txPower = input.getCurrentTxPower();

        txPower = 100;

        double WINDOW = 25;
        double iCollisions = this.collisions.intLast((int)WINDOW)/WINDOW;
        int C_THRESHOLD = 25;

        if(iCollisions>=C_THRESHOLD){
            phymode = PhyMinion.increase(phymode);
        }else{
            phymode = PhyMinion.decrease(phymode);
        }

        return new RRMConfig(phymode,txPower);
    }
}
