package com.ausclouds.bdbsec.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import com.ausclouds.bdbsec.entity.ClusterEntity;
import com.ausclouds.bdbsec.entity.LinuxConnEntity;
import com.ausclouds.bdbsec.entity.LinuxGroupEntity;
import com.ausclouds.bdbsec.entity.LinuxUserEntity;

@Service
public interface LinuxUsersService {

	public LinuxUserEntity test();

	// create connection:
	public Map createLinuxConn(List<LinuxConnEntity> list);

	// get all LInux users info: and save it into db
	public Map findAllUserInfo(String ip, HttpServletRequest request);

	// get all ip list:
	public List<String> getIpList();

	// get all userName list:
	public List<String> getUserNameList();

	// insert node's users info when erect conn:
	public Map insertUsersInfoWhenConn(String ip);

	// add:
	public Map newLinuxUser(/*String cluster,*/ LinuxUserEntity linuxUserEntity);

	// delete:
	public Map delLinuxUser(HttpServletRequest request);

	// update:
	public Map updateUsrInfo(/*HttpServletRequest request,*/ LinuxUserEntity linuxUserEntity);

	// ************************************Group:
	// get all groups:
	public List<LinuxGroupEntity> getAllGroups(String ip);

	// insert all groups into db:
	public void InsertAllGroups(String ip);

	// add a group:
	public Map newLinuxGroup(/*String ip, */LinuxGroupEntity linuxGroupEntity);

	// delete group:
	public Map delLinuxGroup(HttpServletRequest request);

	// show all groups:
	public Map showLinuxGroups(/*String ip,*/ HttpServletRequest request);

	// *************************Host:
	// show all node:
	public Map showHostInfo(HttpServletRequest request);

	// delete node and node's user&group:
	public Map delLinuxHost(HttpServletRequest request);
	
	//同步用户:
	public Map syncDbUserToHost(List<String> userNameList,HttpServletRequest request);
	
	//同步node为空 且该cluster下的用户
	public Map showSyncUserList(HttpServletRequest request); 
	
	//reSet passwd:
	public Map reSetPasswd();
	
	//Check is any node's any user's password changed
	public Map checkPasswd();
	
	//add cluster:
	public Map addCluster(ClusterEntity clusterEntity); 
	
	//update cluster:
	public Map updateCluster(String cluster); 
	
	//show cluster:
	public Map showCluster();
	
	//delete cluster:
	public Map delCluster(String cluster);
	

}
