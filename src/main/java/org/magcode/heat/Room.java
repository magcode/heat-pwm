package org.magcode.heat;

public class Room {
	private String topActTemp;
	private String topTargetTemp;
	private String topSwitch;
	private String name;
	private float actTemp;
	private float targetTemp;
	
	
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
}
