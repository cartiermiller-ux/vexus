package project.monitor.mining.job;

import java.io.Serializable;

/**
 * 任务收益（暂存）
 *
 */
public class MiningIncome {

	private Serializable partyId;

	private double value;

	public Serializable getPartyId() {
		return partyId;
	}

	public void setPartyId(Serializable partyId) {
		this.partyId = partyId;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
}
