package com.ausclouds.bdbsec.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;

import javax.servlet.http.HttpServletRequest;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ausclouds.bdbsec.entity.ClusterEntity;
import com.ausclouds.bdbsec.entity.Constant;
import com.ausclouds.bdbsec.entity.LinuxConnEntity;
import com.ausclouds.bdbsec.entity.LinuxGroupEntity;
import com.ausclouds.bdbsec.entity.LinuxUserEntity;
import com.ausclouds.bdbsec.impl.LinuxUsersServiceImpl;
import com.ausclouds.bdbsec.util.PasswordUtils;

@RestController
@RequestMapping("/simple/linux")
public class LinuxUsersController {

	@Autowired
	LinuxUsersServiceImpl linuxUsersServiceImpl;

	@GetMapping("/test")
	public LinuxUserEntity testOne() {
		LinuxUserEntity test = linuxUsersServiceImpl.test();
		return test;
	}

	/**
	 * get conn: getLinuxConn
	 * @param list
	 * @return
	 */
	@PostMapping("/add/host")
	public Map getLinuxConn(@RequestBody List<LinuxConnEntity> list) {
		Map resultMap = null;
		if (list != null) {
			resultMap = linuxUsersServiceImpl.createLinuxConn(list);
		} else {
			resultMap = setResult(resultMap, Constant.ZERO, Constant.REQUEST_NULL_MSG);
		}

		return resultMap;
	}

	/**
	 * showLinuxUserInfo
	 * @param request
	 * @return
	 */
	@GetMapping("/show/user")
	public Map showLinuxUserInfo(HttpServletRequest request) {
		Map resultMap = null;
		// 修改改为查数据库，根据id获取ip
		String ip = request.getParameter(Constant.IP);
		if (!StringUtils.isEmpty(request)) {
			resultMap = linuxUsersServiceImpl.findAllUserInfo(ip, request);
		} else {
			resultMap = setResult(resultMap, Constant.ZERO, Constant.REQUEST_NULL_MSG);
		}
		return resultMap;
	}
	
	
	/**
	 * showLinuxUserInfoFromDB
	 * @param request
	 * @return
	 */
	@GetMapping("/show/dbuser")
	public Map showLinuxUserInfoFromDB(HttpServletRequest request) {
		Map resultMap = null;
		if (!StringUtils.isEmpty(request)) {
			resultMap = linuxUsersServiceImpl.findAllUserInfo(Constant.CON_NULL,request);
		} else {
			resultMap = setResult(resultMap, Constant.ZERO, Constant.REQUEST_NULL_MSG);
		}
		return resultMap;
	}

	
	/**
	 * addLinuxUser
	 * @param linuxUserEntity
	 * @return
	 */
	@PostMapping("/add/user")
	public Map addLinuxUser(/*HttpServletRequest request,*/ @RequestBody LinuxUserEntity linuxUserEntity) {
		// request中获取当前节点ip，判断是哪个节点: *********?ip=xxx.xxx.xxx.xx
		Map resultMap = null;
		// String ip = request.getParameter("ip");
		//String cluster = request.getParameter(Constant.CLUSTER);
		if (/*!StringUtils.isEmpty(cluster) &&*/ linuxUserEntity != null) {
			resultMap = linuxUsersServiceImpl.newLinuxUser(linuxUserEntity);
		} else {
			resultMap = setResult(resultMap, Constant.ZERO, Constant.REQUEST_NULL_MSG);
		}
		return resultMap;
	}

	/**
	 * deleteLinuxUser
	 * @param request
	 * @return
	 */
	// @Delete("/delete")
	@PostMapping("/delete/user")
	public Map deleteLinuxUser(HttpServletRequest request) {
		Map resultMap = null;
		if (request != null) {
			resultMap = linuxUsersServiceImpl.delLinuxUser(request);
		} else {
			resultMap = setResult(resultMap, Constant.ZERO, Constant.REQUEST_NULL_MSG);
		}
		return resultMap;
	}

	
	/**
	 * updateLinuxUserInfo
	 * @param linuxUserEntity
	 * @return
	 */
	// @Update("/update")
	@PostMapping("/update/user")
	public Map updateLinuxUserInfo(/*HttpServletRequest request, */@RequestBody LinuxUserEntity linuxUserEntity) {
		Map resultMap = null;
		if (/*request != null &&*/ linuxUserEntity != null) {
			resultMap = linuxUsersServiceImpl.updateUsrInfo(linuxUserEntity);
		} else {
			resultMap = setResult(resultMap, Constant.ZERO, Constant.REQUEST_NULL_MSG);
		}
		return resultMap;
	}

	
	/**
	 * addLinuxGroup
	 * @param linuxGroupEntity
	 * @return
	 */
	// add group:
	@PostMapping("/add/group")
	public Map addLinuxGroup(/*HttpServletRequest request,*/ @RequestBody LinuxGroupEntity linuxGroupEntity) {
		Map resultMap = null;
		// String ip = request.getParameter("ip");
		//String ipStr = request.getParameter(Constant.IP);
		if (linuxGroupEntity != null) {
			resultMap = linuxUsersServiceImpl.newLinuxGroup(/*ipStr, */linuxGroupEntity);
			ConcurrentHashMap map = new ConcurrentHashMap<>();
			map.
		} else {
			resultMap = setResult(resultMap, Constant.ZERO, Constant.REQUEST_NULL_MSG);
		}
		return resultMap;
	}

	/**
	 * deleteLinuxGroup
	 * @param request
	 * @return
	 */
	// delete group:
	@PostMapping("/delete/group")
	public Map deleteLinuxGroup(HttpServletRequest request) {
		Map resultMap = null;
		if (request != null) {
			resultMap = linuxUsersServiceImpl.delLinuxGroup(request);
		} else {
			resultMap = setResult(resultMap, Constant.ZERO, Constant.REQUEST_NULL_MSG);
		}
		return resultMap;
	}

	
	/**
	 * showLinuxGroupInfo
	 * @param request
	 * @return
	 */
	// show groups:
	@GetMapping("/show/group")
	public Map showLinuxGroupInfo(HttpServletRequest request) {
		Map resultMap = null;
		//String ip = request.getParameter(Constant.IP);
		if (!StringUtils.isEmpty(request)) {
			resultMap = linuxUsersServiceImpl.showLinuxGroups(request);
		} else {
			resultMap = setResult(resultMap, Constant.ZERO, Constant.REQUEST_NULL_MSG);
		}
		return resultMap;
	}

	
	/**
	 * showLinuxHostsInfo
	 * @param request
	 * @return
	 */
	// show Hosts:
	@GetMapping("/show/host")
	public Map showLinuxHostsInfo(HttpServletRequest request) {
		Map resultMap = null;
		if (!StringUtils.isEmpty(request)) {
			resultMap = linuxUsersServiceImpl.showHostInfo(request);
		} else {
			resultMap = setResult(resultMap, Constant.ZERO, Constant.REQUEST_NULL_MSG);
		}
		return resultMap;
	}

	
	/**
	 * deleteLinuxHost
	 * @param request
	 * @return
	 */
	@PostMapping("/delete/host")
	public Map deleteLinuxHost(HttpServletRequest request) {
		Map resultMap = null;
		if (request != null) {
			resultMap = linuxUsersServiceImpl.delLinuxHost(request);
		} else {
			resultMap = setResult(resultMap, Constant.ZERO, Constant.REQUEST_NULL_MSG);
		}
		return resultMap;
	}

	/**
	 * setResult
	 * 
	 * @param resultMap
	 * @param statusCodeValue
	 * @param msgDescValue
	 * @return
	 */
	public Map setResult(Map<Object, Object> resultMap, String statusCodeValue, String msgDescValue) {
		if (resultMap == null) {
			resultMap = new HashMap<>();
		}
		resultMap.put(Constant.STATUS_CODE, statusCodeValue);
		resultMap.put(Constant.MSG_DESC, msgDescValue);
		return resultMap;
	}
	
	
	/**
	 * showDbUsersList
	 * @param request
	 * @return
	 */
	@GetMapping("/userList")
	public Map showDbUsersList(HttpServletRequest request) {
		Map resultMap = null;
		if (request != null) {
			resultMap = linuxUsersServiceImpl.showSyncUserList(request);
		}else {
			resultMap = setResult(resultMap, Constant.ZERO, Constant.REQUEST_NULL_MSG);
		}
		return resultMap;
	}
	
	
	/**
	 * syncDbUserToHost
	 * @param userNameList
	 * @param request
	 * @return
	 */
	//syncDbUserToHost
	@PostMapping("/syncUsers")
	public Map syncDbUserToHost(@RequestBody List<String> userNameList, HttpServletRequest request) {
		Map resultMap = null;
		if (!StringUtils.isEmpty(request) && !userNameList.isEmpty()) {
			resultMap = linuxUsersServiceImpl.syncDbUserToHost(userNameList, request);
		} else {
			resultMap = setResult(resultMap, Constant.ZERO, Constant.REQUEST_NULL_MSG);
		}
		return resultMap;
	}
	
	
	/**
	 * resetPasswd
	 * @return
	 */
	//reSet Passwd:
	@GetMapping("/resetPasswd")
	public Map resetPasswd() {
		return linuxUsersServiceImpl.reSetPasswd();
	}
	
	
	/**
	 * checkIsPasswdChange
	 * @return
	 */
	@GetMapping("/check/passwd")
	public Map checkIsPasswdChange() {
		return linuxUsersServiceImpl.checkPasswd();
	}
	
	
	/**
	 * showClusters
	 * @return
	 */
	@GetMapping("/show/cluster")
	public Map showClusters() {
		return linuxUsersServiceImpl.showCluster();
	}
	
	
	/**
	 * addClusters
	 * @param clusterEntity
	 * @return
	 */
	@PostMapping("/add/cluster")
	public Map addClusters(@RequestBody ClusterEntity clusterEntity) {
		Map resultMap = null;
		if (clusterEntity != null) {
			resultMap = linuxUsersServiceImpl.addCluster(clusterEntity);
		}else {
			resultMap = setResult(resultMap, Constant.ZERO, Constant.REQUEST_NULL_MSG);
		}
		return resultMap;
	}
	
	
	/**
	 * updateClusters
	 * @param clusterEntity
	 * @return
	 */
	@PostMapping("/update/cluster")
	public Map updateClusters(String cluster) {
		Map resultMap = null;
		if (!StringUtils.isEmpty(cluster)) {
			resultMap = linuxUsersServiceImpl.updateCluster(cluster);
		}else {
			resultMap = setResult(resultMap, Constant.ZERO, Constant.REQUEST_NULL_MSG);
		}
		return resultMap;
	}
	
	
	/**
	 * deleteCluster
	 * @param request
	 * @return
	 */
	@PostMapping("/delete/cluster")
	public Map deleteCluster(HttpServletRequest request) {
		Map resultMap = null;
		if (!StringUtils.isEmpty(request)) {
			String cluster = request.getParameter(Constant.CLUSTER);
			resultMap = linuxUsersServiceImpl.delCluster(cluster);
		}else {
			resultMap = setResult(resultMap, Constant.ZERO, Constant.REQUEST_NULL_MSG);
		}
		return resultMap;
	}

}
