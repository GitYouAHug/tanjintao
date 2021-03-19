package com.ausclouds.bdbsec.entity;

public class ResetPasswdEntity {

	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getNode() {
		return node;
	}
	public void setNode(String node) {
		this.node = node;
	}
	
	protected String userName;
	@Override
	public String toString() {
		return "ResetPasswdEntity [userName=" + userName + ", node=" + node + "]";
	}

	protected String node;
}
