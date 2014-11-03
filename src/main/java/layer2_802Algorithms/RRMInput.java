package layer2_802Algorithms;

/**
 * Created on 02.11.2014.
 *
 * @author Thomas
 */
public class RRMInput {

    private int aAIFSN;
    private int aCWmin;

    private int collisionCount;
    private int discardedCount;

    private int queueSize;
    private int currentQueueSize;

    private double currentTxPower;

    private String currentPhyMode;

    public RRMInput(int aAIFSN, int aCWmin, int collisionCount, int discardedCount, int queueSize,
                    int currentQueueSize, double currentTxPower, String currentPhyMode) {
        this.aAIFSN = aAIFSN;
        this.aCWmin = aCWmin;
        this.collisionCount = collisionCount;
        this.discardedCount = discardedCount;
        this.queueSize = queueSize;
        this.currentQueueSize = currentQueueSize;
        this.currentTxPower = currentTxPower;
        this.currentPhyMode = currentPhyMode;
    }

    public int getaAIFSN() {
        return aAIFSN;
    }

    public int getaCWmin() {
        return aCWmin;
    }

    public int getCollisionCount() {
        return collisionCount;
    }

    public int getDiscardedCount() {
        return discardedCount;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public int getCurrentQueueSize() {
        return currentQueueSize;
    }

    public double getCurrentTxPower() {
        return currentTxPower;
    }

    public String getCurrentPhyMode() {
        return currentPhyMode;
    }
}
