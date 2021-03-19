package com.ausclouds.bdbsec.entity;

import java.io.Serializable;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class HostInfoEntity implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6220241844892827798L;
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getHost_name() {
		return host_name;
	}
	public void setHost_name(String host_name) {
		this.host_name = host_name;
	}
	public String getHost_version() {
		return host_version;
	}
	public void setHost_version(String host_version) {
		this.host_version = host_version;
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
		return "HostInfoEntity [ip=" + ip + ", host_name=" + host_name + ", host_version=" + host_version + ", cluster="
				+ cluster + ", description=" + description + "]";
	}

	protected String ip;
	protected String host_name;
	protected String host_version;
	protected String cluster;
	protected String description;
	

}
