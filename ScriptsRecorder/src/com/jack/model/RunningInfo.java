package com.jack.model;

public class RunningInfo {

	private String threadName;
	private String state;
	private double percents;
	private String description;
	public RunningInfo() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * when replay the script, we need some info to show. 
	 * this is a model that describes the running info.
	 * @param threadName
	 * @param state
	 * @param percents
	 * @param description
	 */
	public RunningInfo(String threadName, String state, double percents, String description) {
		super();
		this.threadName = threadName;
		this.state = state;
		this.percents = percents;
		this.description = description;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return threadName + "::" + state + "::" + percents + "::" + description;
	}
	/**
	 * @return the threadName
	 */
	public String getThreadName() {
		return threadName;
	}
	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}
	/**
	 * @return the percents
	 */
	public double getPercents() {
		return percents;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param threadName the threadName to set
	 */
	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}
	/**
	 * @param state the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}
	/**
	 * @param percents the percents to set
	 */
	public void setPercents(double percents) {
		this.percents = percents;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
}
