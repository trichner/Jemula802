package layer2_802Mac;

import kernel.JETime;
import kernel.JEmula;
import layer1_802Phy.JE802PhyMode;

public abstract class JE802_Mpdu extends JEmula {
	protected int theSA;

	protected int theDA;

	protected long theSeqNo;

	protected final int theFrameBodySize;

	protected final int theHeaderSize;

	protected JETime theTxTime;

	protected JETime lastArrivalTime;

	protected JE802PhyMode thePhyMode;

	protected final int theSourceHandler;

	protected boolean error;

	public JE802_Mpdu(final int aSourceAddress, final int aDestAddress, final long aSequenceNum, final int aFrameBodySize,
			final int aHeaderSize, final int aSourceHandler, final JETime lastArrival) {
		// the generator that generated the payload
		this.theSourceHandler = new Integer(aSourceHandler);
		this.theSA = aSourceAddress; // source MAC address
		this.theDA = aDestAddress; // destination MAC address
		this.lastArrivalTime = lastArrival;
		this.theSeqNo = aSequenceNum; // unique MAC frame number
		this.theFrameBodySize = aFrameBodySize; // size
		this.theHeaderSize = aHeaderSize; // size
		this.theTxTime = new JETime(); // required frame transmission time,
		// default is null
		this.thePhyMode = null; // the PhyMode of this MPDU
		this.error = false; // frame error
	}

	public JE802_Mpdu() {
		this.theSourceHandler = 0;
		this.theSA = 0; // source MAC address
		this.theDA = 0; // destination MAC address
		this.theSeqNo = 0; // unique MAC frame number
		this.theFrameBodySize = 0; // size
		this.theHeaderSize = 0; // size
		this.theTxTime = new JETime(); // required frame transmission time,
										// default is 0
		this.thePhyMode = null; // the PhyMode of this MPDU
		this.error = false; // frame error
	}

	@Override
	public abstract JE802_Mpdu clone();

	public abstract Object getType();

	public void setError(final Boolean error) {
		this.error = error;
	}

	public void setDA(final Integer theDA) {
		this.theDA = theDA;
	}

	public void setSeqNo(final long theSeqNo) {
		this.theSeqNo = theSeqNo;
	}

	public void setTxTime(final JETime theTxTime) {
		this.theTxTime = theTxTime;
	}

	public void setLastArrivalTime(final JETime lastArrivalTime) {
		this.lastArrivalTime = lastArrivalTime;
	}

	public void setSA(final Integer theSA) {
		this.theSA = theSA;
	}

	public void setPhyMode(final JE802PhyMode thePhyMode) {
		this.thePhyMode = thePhyMode;
	}

	public JETime getLastArrivalTime() {
		return this.lastArrivalTime;
	}

	public int getSourceHandler() {
		return this.theSourceHandler;
	}

	public boolean isError() {
		return this.error;
	}

	public int getSA() {
		return this.theSA;
	}

	public int getDA() {
		return this.theDA;
	}

	public long getSeqNo() {
		return this.theSeqNo;
	}

	public JETime getTxTime() {
		return this.theTxTime;
	}

	public int getFrameBodySize() {
		return this.theFrameBodySize;
	}

	public int getHeaderSize() {
		return this.theHeaderSize;
	}

	public JE802PhyMode getPhyMode() {
		return this.thePhyMode;
	}

	@Override
	public void display_status() {
		if (this.theSourceHandler == 0) {
			System.out.println("  - source handler: null");
		} else {
			System.out.println("  - source handler: " + this.theSourceHandler);
		}
		System.out.println("  - SA:              " + this.theSA);
		System.out.println("  - DA:              " + this.theDA);
		System.out.println("  - SeqNo:           " + this.theSeqNo);
		System.out.println("  - FrameBodySize:   " + this.theFrameBodySize);
		System.out.println("  - txtime:          " + this.theTxTime.toString());
		System.out.println("  - error:           " + this.error);
		if (this.thePhyMode != null) {
			System.out.println("  - phymode:         " + this.thePhyMode.toString());
		} else {
			System.out.println("  - phymode:         undefined");
		}
		super.display_status();
	}
}
