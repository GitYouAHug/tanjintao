package com.ausclouds.bdbsec.entity;

import java.io.Serializable;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ClusterEntity implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5649203926947513606L;
	
	

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
		return "ClusterEntity [cluster=" + cluster + ", description=" + description + "]";
	}
	protected String cluster;
	protected String description;
	
}
