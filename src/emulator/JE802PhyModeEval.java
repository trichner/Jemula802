package emulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kernel.JETime;
import layer1_802Phy.JE802PhyMode;

public class JE802PhyModeEval {

	private final JETime evalStartTime;

	private final JETime evaluationInterval;

	private JETime nextEvaluation;

	private String path2Results;

	// keyed by hopSource, hopDestination
	private Map<Integer, Map<Integer, List<JE802ModeRecord>>> perHopModeCount;

	private List<JE802PhyMode> availablePhymodes;

	public JE802PhyModeEval(String path2Results, JETime evalStartTime, JETime evaluationInterval) {
		this.evalStartTime = evalStartTime;
		this.evaluationInterval = evaluationInterval;
		this.perHopModeCount = new HashMap<Integer, Map<Integer, List<JE802ModeRecord>>>();
		this.nextEvaluation = evalStartTime.plus(evaluationInterval);
		this.path2Results = path2Results;
	}

	public void recordTransmission(int source, int destination, JETime when, JE802PhyMode phyMode) {
		// only evaluate if time is later than evaluation Starttime
		if (when.isLaterThan(evalStartTime)) {
			// get addresses of neighbors from source
			Map<Integer, List<JE802ModeRecord>> destMap = perHopModeCount.get(source);
			if (destMap == null) {
				destMap = new HashMap<Integer, List<JE802ModeRecord>>();
				perHopModeCount.put(source, destMap);
			}
			// get the Record for the current interval
			List<JE802ModeRecord> modeCounts = destMap.get(destination);
			if (modeCounts == null) {
				modeCounts = new ArrayList<JE802ModeRecord>();
				destMap.put(destination, modeCounts);
			}
			if (modeCounts.isEmpty()) {
				JE802ModeRecord rec = new JE802ModeRecord(nextEvaluation.minus(evaluationInterval));
				modeCounts.add(rec);
			}
			// increase count for the current phymode by one
			JE802ModeRecord rec = modeCounts.get(modeCounts.size() - 1);
			rec.increaseCount(phyMode);
		}
	}

	public JE802PhyMode getMostUsedPhymode(int source, int destination, JETime when) {
		Map<Integer, List<JE802ModeRecord>> destMap = perHopModeCount.get(source);
		if (destMap != null) {
			List<JE802ModeRecord> mostUsedModes = destMap.get(destination);
			if (mostUsedModes != null) {
				for (JE802ModeRecord mode : mostUsedModes) {
					if (when.isLaterEqualThan(mode.getTime()) && when.isEarlierEqualThan(mode.getTime().plus(evaluationInterval))) {
						return mode.getMostUsedPhymode();
					}
				}
			}
		}
		return null;
	}

	public void endOfEmulation() {
		this.evaluate();
		// TODO: write to file, but how to organize the file?
	}

	public void evaluate() {
		nextEvaluation = nextEvaluation.plus(evaluationInterval);
		for (Integer source : perHopModeCount.keySet()) {
			Map<Integer, List<JE802ModeRecord>> destMap = perHopModeCount.get(source);
			for (Integer dest : destMap.keySet()) {
				List<JE802ModeRecord> recordList = destMap.get(dest);
				recordList.get(recordList.size() - 1).endOfInterval();
				recordList.add(new JE802ModeRecord(nextEvaluation));
			}
		}
	}

	public void setPhyModes(List<JE802PhyMode> availablePhymodes) {
		this.availablePhymodes = availablePhymodes;
	}

	private class JE802ModeRecord {

		private JETime when;

		private int[] counts = new int[availablePhymodes.size() + 1];

		private JE802PhyMode mostUsedPhymode;

		public JE802ModeRecord(JETime when) {
			this.when = when;
			mostUsedPhymode = null;
		}

		public void increaseCount(JE802PhyMode mode) {
			this.counts[mode.getModeId()]++;
		}

		public void endOfInterval() {
			int maxCount = Integer.MIN_VALUE;
			for (int i = 1; i < counts.length; i++) {
				if (counts[i] > maxCount) {
					maxCount = counts[i];
					mostUsedPhymode = availablePhymodes.get(i - 1);
				}
			}
			if (maxCount == 0) {
				mostUsedPhymode = null;
			}
			counts = null;
		}

		public JETime getTime() {
			return when;
		}

		public JE802PhyMode getMostUsedPhymode() {
			return mostUsedPhymode;
		}
	}

}
