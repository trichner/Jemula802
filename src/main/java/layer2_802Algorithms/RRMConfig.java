package layer2_802Algorithms;

/**
 * Created on 02.11.2014.
 *
 * @author Thomas
 */
public class RRMConfig {
    private final String phymode;
    private final double txPower;

    public RRMConfig(String phymode, double txPower) {
        this.phymode = phymode;
        this.txPower = txPower;
    }

    public String getPhymode() {
        return phymode;
    }

    public double getTxPower() {
        return txPower;
    }
}
