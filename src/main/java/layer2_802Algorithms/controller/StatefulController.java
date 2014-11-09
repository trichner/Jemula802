package layer2_802Algorithms.controller;

import layer2_802Algorithms.RRMConfig;
import layer2_802Algorithms.RRMInput;
import util.AggregateIntTracker;

/**
 * Created on 02.11.2014.
 *
 * @author Thomas
 */
public abstract class StatefulController implements RRMController {

    protected AggregateIntTracker collisions = new AggregateIntTracker();
    protected AggregateIntTracker discards = new AggregateIntTracker();
    protected AggregateIntTracker queueSizes = new AggregateIntTracker();


    @Override
    public RRMConfig compute(RRMInput input) {
        collisions.pushAggregated(input.getCollisionCount());
        discards.push(input.getDiscardedCount());
        queueSizes.push(input.getCurrentQueueSize());



        return this.evaluate(input);
    }

    abstract protected RRMConfig evaluate(RRMInput input);

}
