package com.ausclouds.bdbsec.entity;

import java.io.Serializable;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class LinuxGroupEntity implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4600080648018753450L;


	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public String getGid() {
		return gid;
	}
	public void setGid(String gid) {
		this.gid = gid;
	}
	public String getNode() {
		return node;
	}
	public void setNode(String node) {
		this.node = node;
	}
	public String getCluster() {
		return cluster;
	}
	public void setCluster(String cluster) {
		this.cluster = cluster;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@Override
	public String toString() {
		return "LinuxGroupEntity [groupName=" + groupName + ", gid=" + gid + ", node=" + node + ", cluster=" + cluster
				+ ", description=" + description + "]";
	}
	protected String groupName;
	protected String gid;
	protected String node;
	protected String cluster;
	protected String description;

}
