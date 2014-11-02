package layer2_802Algorithms;

/**
 * Created on 02.11.2014.
 *
 * @author Thomas
 */
public class TXPowerController extends StatefulController {
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

        // static phymode
        phymode = PhyMinion.max(); //PhyMinion.offset(PhyMinion.max(),-2);
        txPower = 100;

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
