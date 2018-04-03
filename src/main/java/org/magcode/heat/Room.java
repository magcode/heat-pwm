package org.magcode.heat;

public class Room {
	private String topActTemp;
	private String topTargetTemp;
	private String topSwitch;
	private String topEnabled;
	private String name;
	private float actTemp;
	private float targetTemp;
	private int offset = 5;
	private int factor = 50;
	private int minimumCycle = 5;
	private int interval = 30;
	private boolean heatingEnabled = true;

	public int getTimeForDiff(float diff) {

		if (diff < 0.1) {
			return 0;
		}
		int time = (int) (this.offset + diff * factor);
		
		
		// calculated time too short to open valve ... we keep valve closed
		if (time < minimumCycle) {
			return 0;
		}
		// too close to intervall. e.g. time=28, intervall=30 ... we would close valve
		// for 2 minutes
		// -> makes no sense. Lets then rather return "full time"
		if (interval - time < minimumCycle) {
			return interval;
		}
		return time;
	}

	public String getTopActTemp() {
		return topActTemp;
	}

	public void setTopActTemp(String topActTemp) {
		this.topActTemp = topActTemp;
	}

	public String getTopTargetTemp() {
		return topTargetTemp;
	}

	public void setTopTargetTemp(String topTargetTemp) {
		this.topTargetTemp = topTargetTemp;
	}

	public String getTopSwitch() {
		return topSwitch;
	}

	public void setTopSwitch(String topSwitch) {
		this.topSwitch = topSwitch;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getActTemp() {
		return actTemp;
	}

	public void setActTemp(float actTemp) {
		this.actTemp = actTemp;
	}

	public float getTargetTemp() {
		return targetTemp;
	}

	public void setTargetTemp(float targetTemp) {
		this.targetTemp = targetTemp;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getFactor() {
		return factor;
	}

	public void setFactor(int factor) {
		this.factor = factor;
	}

	public int getMinimumCycle() {
		return minimumCycle;
	}

	public void setMinimumCycle(int minimumCycle) {
		this.minimumCycle = minimumCycle;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int intervall) {
		this.interval = intervall;
	}

	public boolean isHeatingEnabled() {
		return heatingEnabled;
	}

	public void setHeatingEnabled(boolean heatingEnabled) {
		this.heatingEnabled = heatingEnabled;
	}

	public String getTopEnabled() {
		return topEnabled;
	}

	public void setTopEnabled(String topEnabled) {
		this.topEnabled = topEnabled;
	}
}
