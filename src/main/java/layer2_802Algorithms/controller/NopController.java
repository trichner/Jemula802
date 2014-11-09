package layer2_802Algorithms.controller;

import layer2_802Algorithms.RRMConfig;
import layer2_802Algorithms.RRMInput;

/**
 * Created by trichner on 11/3/14.
 */
public class NopController implements RRMController {
    @Override
    public RRMConfig compute(RRMInput input) {
        return new RRMConfig(input.getCurrentPhyMode(),input.getCurrentTxPower());
    }
}
