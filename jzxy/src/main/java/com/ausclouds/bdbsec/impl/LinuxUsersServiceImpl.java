package com.ausclouds.bdbsec.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ausclouds.bdbsec.entity.ClusterEntity;
import com.ausclouds.bdbsec.entity.Constant;
import com.ausclouds.bdbsec.entity.HostInfoEntity;
import com.ausclouds.bdbsec.entity.LinuxConnEntity;
import com.ausclouds.bdbsec.entity.LinuxGroupEntity;
import com.ausclouds.bdbsec.entity.LinuxUserEntity;
import com.ausclouds.bdbsec.entity.ResetPasswdEntity;
import com.ausclouds.bdbsec.mapper.LinuxUsersMapper;
import com.ausclouds.bdbsec.service.LinuxUsersService;
import com.ausclouds.bdbsec.util.PasswordUtils;
import com.ausclouds.bdbsec.util.RemoteExecuteCommandutil;

//import springfox.documentation.swagger2.mappers.LicenseMapperImpl;

@Service
public class LinuxUsersServiceImpl implements LinuxUsersService {

	@Autowired
	LinuxUsersMapper linuxUsersMapper;

	private static final Logger LOG = Logger.getLogger(LinuxUsersServiceImpl.class);
	private static Map<String, RemoteExecuteCommandutil> linuxConnMap = new HashMap<String, RemoteExecuteCommandutil>();
	private static ArrayList<RemoteExecuteCommandutil> linuxConnList = new ArrayList<RemoteExecuteCommandutil>();
	private final Map<String, String> passwdMap = new HashMap<String, String>();
	HashMap<String, String> savePasswd = new HashMap<>();
	private final String USERADD = "useradd -m ";
	private final String PASSWD = " -p ";
	private final String GROUP = " -g ";

	@Override
	public LinuxUserEntity test() {
		LinuxUserEntity info = linuxUsersMapper.getInfo();
		return info;
	}

	/**
	 * createLinuxConn
	 */
	@Override
	public Map createLinuxConn(List<LinuxConnEntity> list) {
		Map<Object, Object> resultMap = new HashMap<>();
		String encryptPassword = null;
		for (int i = 0; i < list.size(); i++) {
			LinuxConnEntity linuxConnEntity = list.get(i);
			String ip = linuxConnEntity.getIp();
			String passwd = linuxConnEntity.getPasswd();
			String user = linuxConnEntity.getUser();
			String cluster = linuxConnEntity.getCluster();
			try {
				 encryptPassword = PasswordUtils.encryptPassword(passwd);
			} catch (IOException e1) {
				e1.printStackTrace();
				setResult(resultMap, Constant.ZERO, Constant.ENCRYPT_FIAL);
			}
			try {
				if (!StringUtils.isEmpty(ip) && !StringUtils.isEmpty(passwd) && !StringUtils.isEmpty(user)) {
					RemoteExecuteCommandutil remoteExecuteCommandutil = new RemoteExecuteCommandutil(ip, user, passwd);
					// 处理对端,有多个
					linuxConnMap.put(ip, remoteExecuteCommandutil);
					// 现在要保存到数据库中连接信息：Hosts管理
					try {
						List<String> allIpList = linuxUsersMapper.getAllIpList();
						if (!allIpList.contains(ip)|allIpList.isEmpty()) {
							linuxConnEntity.setPasswd(encryptPassword);
							linuxConnEntity.setFlag(Constant.HOST_CONN_FLAG);
							linuxUsersMapper.insertIntoDb(linuxConnEntity);
							// 建立conn后，查询一下用户信息，并保存多个节点保存
							//this.insertUsersInfoWhenConn(ip);
							// 初始化要不要也罢groups一把录进去：
							//this.InsertAllGroups(ip);
							//insert cluster table:
							HostInfoEntity hostInfoEntity = new HostInfoEntity();
							hostInfoEntity.setIp(ip);
							hostInfoEntity.setCluster(cluster);
							linuxUsersMapper.insertIntoHostInfo(hostInfoEntity);
						}
					} catch (Exception e) {
						e.printStackTrace();
						setResult(resultMap, Constant.ZERO, Constant.WRITE_FAIL);
					}

				}
			} catch (Exception e) {
				LOG.error(Thread.currentThread().getStackTrace()[1].getMethodName() + Constant.GET_EXCEPTION
						+ e.getMessage());
				setResult(resultMap, Constant.ZERO, Constant.ERECT_CONN_FAIL);
				return resultMap;
			}
		}
		setResult(resultMap, Constant.TWO_HUNDRED, Constant.CONN_MSG);
		return resultMap;
	}

	public void setResult(Map<Object, Object> resultMap, String statusCodeValue, String msgDescValue) {
		resultMap.put(Constant.STATUS_CODE, statusCodeValue);
		resultMap.put(Constant.MSG_DESC, msgDescValue);
	}

	/**
	 * findAllUserInfo
	 */
	@Override
	public Map findAllUserInfo(String ip, HttpServletRequest request) {
		Map<Object, Object> resultMap = new HashMap<>();
		String userName = null;
		String group = null;
		// StringBuilder cmd = new StringBuilder();
		int page = 0;
		int rows = 0;

		if (request != null) {
			userName = request.getParameter(Constant.USERNAME);
			group = request.getParameter(Constant.GROUP);

			if (request.getParameter(Constant.PAGE) != null) {
				page = Integer.parseInt(request.getParameter(Constant.PAGE));
			}
			if (request.getParameter(Constant.ROWS) != null) {
				rows = Integer.parseInt(request.getParameter(Constant.ROWS));
			}
			if (page <= 0) {
				page = 1;
			}
			if (rows <= 0) {
				rows = 20;
			}
		}

		if(StringUtils.isEmpty(ip)) {
			//分开查询，查数据库
			page = page - 1;
			List<LinuxUserEntity> usersListDb = linuxUsersMapper.getUsersList(ip, userName, group, page, rows);
			int size = usersListDb.size();
			resultMap.put(Constant.TOTAL_COUNT, size);
			resultMap.put(Constant.LIST, usersListDb);
			setResult(resultMap, Constant.TWO_HUNDRED, Constant.SEARCH_OK_DB_MSG);
		}else {
			//分开查询，查服务器
			if (StringUtils.isEmpty(linuxConnMap.get(ip))) {
				this.reConn(ip);
			}

			if (!linuxConnMap.isEmpty()) {
				RemoteExecuteCommandutil rec = linuxConnMap.get(ip);
				if (rec != null) {
					try {
						// String cmd = "cat /etc/passwd";
						List<LinuxUserEntity> listUsers = rec.executeCmd(Constant.CAT_USERS_SHELL, 3);
						for (int i = 0; i < listUsers.size(); i++) {
							LinuxUserEntity linuxUserEntity = listUsers.get(i);
							linuxUserEntity.setNode(ip);
						}

						resultMap = getDivide(userName, group, page, rows, listUsers);
						// delete by node
						//linuxUsersMapper.delUserByNode(ip);
						// synchronize
						//linuxUsersMapper.insertUsersInfotoDb(listUsers);
						setResult(resultMap, Constant.TWO_HUNDRED, Constant.SEARCH_OK_MSG);
					} catch (Exception e) {
						LOG.error(Thread.currentThread().getStackTrace()[1].getMethodName() + Constant.GET_EXCEPTION
								+ e.getMessage());
						setResult(resultMap, Constant.ZERO, Constant.SEARCH_FAIL_MSG + e.getMessage());
					}
				} else {
					setResult(resultMap, Constant.ZERO, Constant.CONN_IS_NULL);
				}
			} else {
				setResult(resultMap, Constant.ZERO, Constant.CONN_MAP_ISNULL_MSG);
			}
		}
		return resultMap;
	}

	/**
	 * getDivide
	 * 
	 * @param userName
	 * @param group
	 * @param page
	 * @param rows
	 * @param linuxUsers
	 * @return
	 */
	public Map getDivide(String userName, String group, int page, int rows, List<LinuxUserEntity> linuxUsers) {
		Map<Object, Object> resultMap = new HashMap<>();
		List<LinuxUserEntity> linuxUsersChoice = new ArrayList<>();
		// 搜索项为空的情况
		if (StringUtils.isEmpty(userName) && StringUtils.isEmpty(group)) {
			splitPage(page, rows, linuxUsers, resultMap);
		} else if (!StringUtils.isEmpty(userName) && !StringUtils.isEmpty(group)) {
			// 搜索项userName,group均不为空的情况
			if (linuxUsers != null) {
				for (int i = 0; i < linuxUsers.size(); i++) {
					LinuxUserEntity linuxUsersFilter = linuxUsers.get(i);
					String group1 = linuxUsersFilter.getGroups();
					if (group1 != null && group1 != "") {
						if (linuxUsersFilter.getUserName().contains(userName)
								&& linuxUsersFilter.getGroups().contains(group)) {
							linuxUsersChoice.add(linuxUsersFilter);
						}
					}
				}
				splitPage(page, rows, linuxUsersChoice, resultMap);
			}
		} else if (!StringUtils.isEmpty(userName) | !StringUtils.isEmpty(group)) {
			// 搜索项userName,group有一项不为空的情况
			if (linuxUsers != null) {
				for (int i = 0; i < linuxUsers.size(); i++) {
					LinuxUserEntity linuxUsersFilter = linuxUsers.get(i);
					String group1 = linuxUsersFilter.getGroups();
					if (group1 != null && group1 != "") {
						if (linuxUsersFilter.getUserName().contains(userName) && !StringUtils.isEmpty(userName)) {
							linuxUsersChoice.add(linuxUsersFilter);
						} else if (!StringUtils.isEmpty(group) && linuxUsersFilter.getGroups().contains(group)) {
							linuxUsersChoice.add(linuxUsersFilter);
						}
					}
				}
				splitPage(page, rows, linuxUsersChoice, resultMap);
			}
		}
		return resultMap;
	}

	/**
	 * splitPage
	 * 
	 * @param page
	 * @param rows
	 * @param linuxUsers
	 * @param resultMap
	 */
	public void splitPage(int page, int rows, List<LinuxUserEntity> linuxUsers, Map<Object, Object> resultMap) {
		int size = linuxUsers.size();
		int pageStart = page == 1 ? 0 : (page - 1) * rows;
		int pageEnd = size < page * rows ? size : page * rows;
		List<LinuxUserEntity> resultList = new ArrayList<>();
		if (size > pageStart) {
			resultList = linuxUsers.subList(pageStart, pageEnd);
		}
		resultMap.put(Constant.TOTAL_COUNT, size);
		resultMap.put(Constant.LIST, resultList);
	}

	@Override
	public List<String> getIpList() {
		// List<String> allIpList = linuxUsersMapper.getAllIpList();
		// return allIpList;
		return null;
	}

	@Override
	public List<String> getUserNameList() {
		/*
		 * List<String> allUserNameList = linuxUsersMapper.getAllUserNameList(); return
		 * allUserNameList;
		 */
		return null;
	}

	@Override
	public Map insertUsersInfoWhenConn(String ip) {
		Map<Object, Object> resultMap = new HashMap<>();
		// for(int i =0; i<linuxConnMap.size(); i++) {
		RemoteExecuteCommandutil rec = linuxConnMap.get(ip);
		if (rec != null) {
			// String cmd = "cat /etc/passwd";
			try {
				List<LinuxUserEntity> listUsers = rec.executeCmd(Constant.CAT_USERS_SHELL, 3);
				// TODO passwd:
				this.getUsersPass(rec);
				for (int j = 0; j < listUsers.size(); j++) {
					LinuxUserEntity linuxUserEntity = listUsers.get(j);
					linuxUserEntity.setNode(ip);
					linuxUserEntity.setPasswd(passwdMap.get(linuxUserEntity.getUserName()));
				}
				// Object executeCmd = rec.executeCmd(cmdPass, 4);
				// insert into db first one:
				int insertUsersInfotoDb = linuxUsersMapper.insertUsersInfotoDb(listUsers);
				if (insertUsersInfotoDb > 0) {
					setResult(resultMap, Constant.TWO_HUNDRED, Constant.SEARCH_WRITE_OK_MSG);
				} else {
					setResult(resultMap, Constant.ZERO, Constant.CONN_IS_NULL);
				}
			} catch (Exception e) {
				LOG.error(Thread.currentThread().getStackTrace()[1].getMethodName() + Constant.GET_EXCEPTION
						+ e.getMessage());
				setResult(resultMap, Constant.ZERO, Constant.SEARCH_FAIL_MSG + e.getMessage());
			}
		} else {
			setResult(resultMap, Constant.ZERO, Constant.CONN_IS_NULL);
		}

		// }
		return resultMap;
	}

	public void getUsersPass(RemoteExecuteCommandutil rec) {
		// String cmdPass = "cat /etc/shadow";
		try {
			List<LinuxUserEntity> listUserPasswd = rec.executeCmd(Constant.CAT_PASSWD_SHELL, 2);
			for (int i = 0; i < listUserPasswd.size(); i++) {
				LinuxUserEntity linuxUserEntity = listUserPasswd.get(i);
				passwdMap.put(linuxUserEntity.getUserName(), linuxUserEntity.getPasswd());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * newLinuxUser
	 */
	@Override
	public Map newLinuxUser(/*String cluster,*/ LinuxUserEntity linuxUserEntity) {
		Map<Object, Object> resultMap = new HashMap<>();
		//StringBuilder cmd = new StringBuilder();
		String ip = null;
		int a = 1;
		String userName = linuxUserEntity.getUserName();
		String passwd = linuxUserEntity.getPasswd();
		String group = linuxUserEntity.getGroups();
		String cluster = linuxUserEntity.getCluster();
		//linuxUserEntity.setNode(ip);
		// 可以不输入密码：
		if (StringUtils.isEmpty(passwd)) {
			passwd = UUID.randomUUID().toString();
		}
		List<String> ipList = linuxUsersMapper.getAllIpList();
		

		for (int k = 0; k < ipList.size(); k++) {
			 ip = ipList.get(k);
			if (StringUtils.isEmpty(linuxConnMap.get(ip))) {
				this.reConn(ip);
			}

			// *********
			if (!linuxConnMap.isEmpty()) {
				RemoteExecuteCommandutil rec = linuxConnMap.get(ip);
				if (rec != null) {
					// useradd fox;echo "fox" | passwd --stdin fox
					// 判断传入的三个参数都不为空才执行下去
					
					try {
						StringBuilder cmd = new StringBuilder();
						if (!StringUtils.isEmpty(userName) && !StringUtils.isEmpty(passwd)) {
							// String result = rec.executeCmd("useradd -m tjt0702 -p tjt0702 -g tjt", 4);
							// TODO增加情况：-s /sbin/nologin
							// if (nologin)
							// useradd -m you -g you useradd fox;echo "fox" | passwd --stdin fox
							if (StringUtils.isEmpty(group)) {
								cmd.append(USERADD).append(userName).append(Constant.ECHO_PASSWD_SHELL).append(passwd)
										.append(Constant.SWITCH_PASSWD_SHELL).append(userName);
								// cmd = USERADD+userName+";echo "+passwd+" | passwd --stdin "+userName;
								linuxUserEntity.setGroups(userName);
							} else {
								cmd.append(USERADD).append(userName).append(GROUP).append(group)
										.append(Constant.ECHO_PASSWD_SHELL + passwd + Constant.SWITCH_PASSWD_SHELL)
										.append(userName);
								// cmd = USERADD+userName+GROUP+group+";echo "+passwd+" | passwd --stdin
								// "+userName;
							}
							String result = rec.executeCmd(cmd.toString(), 4);
							//insert passwd to:
							if (a >0) {
								/*List<LinuxUserEntity> passwdEntityList = rec.executeCmd("cat /etc/shadow", 2);
								//insert into xxx where userName = userName
								for(int j=0; j<passwdEntityList.size(); j++) {
									LinuxUserEntity passwdEntity = passwdEntityList.get(j);
									savePasswd.put(passwdEntity.getUserName()+ip, passwdEntity.getPasswd());
								}*/
								a--;
							}
							if (result.contains(Constant.GROUP_NOT_EXIST)) {
								setResult(resultMap, Constant.ZERO, group + Constant.ADD_USER_FIAL_MSG1);
							} else if (result.contains(Constant.ALLREDY_EXIST)) {
								// 用户已经存在要不要把用来的delete，重新添加？
								setResult(resultMap, Constant.ZERO, userName + Constant.ADD_USER_FIAL_MSG2);
							} else if (result.contains(Constant.GROUP_ALLREDY_EXIST)) {
								// 当不输入用户组，则默认使用用户名创建一个组
								setResult(resultMap, Constant.ZERO, group + Constant.ADD_USER_FIAL_MSG3);
							} else {
								// save to db:
								List<LinuxUserEntity> passwdEntityList = rec.executeCmd("cat /etc/shadow", 2);
								//insert into xxx where userName = userName
								for(int j=0; j<passwdEntityList.size(); j++) {
									LinuxUserEntity passwdEntity = passwdEntityList.get(j);
									savePasswd.put(passwdEntity.getUserName()+ip, passwdEntity.getPasswd());
								}
								LinuxConnEntity linuxConnEntity = new LinuxConnEntity();
								linuxConnEntity.setUser(userName);
								linuxConnEntity.setPasswd(PasswordUtils.encryptPassword(passwd));
								linuxConnEntity.setLinuxPasswd(savePasswd.get(userName+ip));
								linuxConnEntity.setIp(ip);
								linuxConnEntity.setCluster(cluster);
								linuxConnEntity.setDescription(linuxUserEntity.getDescription());
								linuxUsersMapper.insertAUser2Conn(linuxConnEntity);
								//String decryptPassword = PasswordUtils.encryptPassword(passwd);
								//linuxUserEntity.setPasswd(PasswordUtils.encryptPassword(passwd));
								//linuxUsersMapper.insertAUser(linuxUserEntity);
								setResult(resultMap, Constant.TWO_HUNDRED, Constant.ADD_USER_OK);
							}
						} else {
							setResult(resultMap, Constant.ZERO, Constant.USER_INFO_LESS);
						}
					} catch (Exception e) {
						LOG.error(Thread.currentThread().getStackTrace()[1].getMethodName() + Constant.GET_EXCEPTION
								+ e.getMessage());
						setResult(resultMap, Constant.ZERO, Constant.ADD_FAIL_MSG + e.getMessage());
					}
					
				} else {
					setResult(resultMap, Constant.ZERO, Constant.CONN_IS_NULL);
				}
			} else {
				// 从数据库获取重连
				// RemoteExecuteCommandutil reConn = this.reConn(ip);
				setResult(resultMap, Constant.ZERO, Constant.CONN_MAP_ISNULL_MSG);
			}
			// *************
		}
		try {
			if (resultMap.get(Constant.STATUS_CODE).equals(Constant.TWO_HUNDRED)) {
				linuxUserEntity.setPasswd(PasswordUtils.encryptPassword(passwd));
				//linuxUserEntity.setCluster(cluster);
				linuxUsersMapper.insertAUser(linuxUserEntity);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resultMap;
	}

	public Map reConn(String ip) {
		Map<Object, Object> resultMap = new HashMap<>();
		RemoteExecuteCommandutil reRec = null;
		LinuxConnEntity connEntity = linuxUsersMapper.getByIp(ip,Constant.HOST_CONN_FLAG);
		if (!StringUtils.isEmpty(connEntity)) {
			String u = connEntity.getUser();
			String p = connEntity.getPasswd();
			try {
				String decryptPassword = PasswordUtils.decryptPassword(p);
				reRec = new RemoteExecuteCommandutil(ip, u, decryptPassword);
				linuxConnMap.put(ip, reRec);
			} catch (Exception e) {
				e.printStackTrace();
				setResult(resultMap, Constant.ZERO, Constant.ERECT_CONN_FAIL);
				return resultMap;
			}
		}
		return resultMap;
	}

	
	/**
	 * delLinuxUser
	 */
	@Override
	public Map delLinuxUser(HttpServletRequest request) {
		Map<Object, Object> resultMap = new HashMap<>();
		// String cmd = null;
		String ip = null;
		StringBuilder cmd = new StringBuilder();
		//String ip = request.getParameter(Constant.IP);
		String userName = request.getParameter(Constant.USERNAME);
		cmd.append(Constant.USER_DEL_SHELL).append(userName);
		List<String> ipList = linuxUsersMapper.getAllIpList();
		for(int i = 0; i< ipList.size(); i++) {
			 ip = ipList.get(i);
			 
			 if (StringUtils.isEmpty(linuxConnMap.get(ip))) {
					this.reConn(ip);
				}

				if (!linuxConnMap.isEmpty()) {
					if (!StringUtils.isEmpty(ip)) {
						RemoteExecuteCommandutil rec = linuxConnMap.get(ip);
						if (rec != null) {
							// userdel -rf user02
							if (!StringUtils.isEmpty(userName)) {
								//cmd.append(Constant.USER_DEL_SHELL).append(userName);
								// cmd = "userdel -rf "+userName;
								try {
									String result = rec.executeCmd(cmd.toString(), 4);
									if (result.contains(Constant.NOT_EXIST)) {
										setResult(resultMap, Constant.ZERO, Constant.DEL_USER_FIAL_MSG1);
									} else {
										// delete from db:
										linuxUsersMapper.delUserByNameAndNode(userName, ip);
										setResult(resultMap, Constant.TWO_HUNDRED, Constant.DEL_USER_OK);
									}
								} catch (Exception e) {
									LOG.error(Thread.currentThread().getStackTrace()[1].getMethodName() + Constant.GET_EXCEPTION
											+ e.getMessage());
									setResult(resultMap, Constant.ZERO, Constant.DEL_USER_FIAL_MSG2 + e.getMessage());
								}
							} else {
								setResult(resultMap, Constant.ZERO, Constant.USERNAME_NULL);
							}
						} else {
							setResult(resultMap, Constant.ZERO, Constant.CONN_IS_NULL);
						}
					} else {
						setResult(resultMap, Constant.ZERO, Constant.IP_NULL);
					}
				} else {
					setResult(resultMap, Constant.ZERO, Constant.CONN_MAP_ISNULL_MSG);
				}
			 
		}
		if (resultMap.get(Constant.STATUS_CODE).equals(Constant.TWO_HUNDRED)) {
			linuxUsersMapper.delUserByName(userName);
		}
		return resultMap;
	}

	
	/**
	 * updateUsrInfo
	 */
	@Override
	public Map updateUsrInfo(/*HttpServletRequest request,*/ LinuxUserEntity linuxUserEntity) {
		Map<Object, Object> resultMap = new HashMap<>();
		// String cmd = null;
		String ip = null;
		// echo 666 | passwd --stdin jzxy666 |usermod -g tjt jzxy666
		//String ip = request.getParameter(Constant.IP);
		String userName = linuxUserEntity.getUserName();
		String group = linuxUserEntity.getGroups();
		//String passwd = linuxUserEntity.getPasswd();
		String passwd = null;
		try {
			passwd = PasswordUtils.encryptPassword(linuxUserEntity.getPasswd());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		List<String> ipList = linuxUsersMapper.getAllIpList();
		for(int i = 0; i< ipList.size(); i++) {
			 ip = ipList.get(i);
			 if (StringUtils.isEmpty(linuxConnMap.get(ip))) {
					this.reConn(ip);
				}

				if (!linuxConnMap.isEmpty()) {
					if (!StringUtils.isEmpty(ip)) {
						RemoteExecuteCommandutil rec = linuxConnMap.get(ip);
						if (rec != null) {
							if (!StringUtils.isEmpty(userName) && !StringUtils.isEmpty(passwd) && !StringUtils.isEmpty(group)) {
								try {
									StringBuilder cmd = new StringBuilder();
									cmd.append(Constant.ECHO_USER_SHELL).append(passwd).append(Constant.USER_PASSWD_SHELL)
											.append(userName).append(Constant.USER_MOD_SHELL).append(group)
											.append(Constant.SPACE_SHELL).append(userName);
									// cmd = "echo "+passwd+" | "+"passwd --stdin "+userName+" |"+"usermod -g
									// "+group+" "+userName;
									// echo tbtb |passwd --stdin tb |usermod -g tjt tb
									String result = rec.executeCmd(cmd.toString(), 4);
									if (result.contains(Constant.GROUP_NOT_EXIST)) {
										setResult(resultMap, Constant.ZERO, group + Constant.UPDATE_USER_FIAL_MSG1);
									} else if (result.contains(Constant.UNKNOW_USER) | result.contains(Constant.NOT_EXIST)) {
										setResult(resultMap, Constant.ZERO, userName + Constant.UPDATE_USER_FIAL_MSG2);
									} else {
										// 修改数据库中的userName&group:
										//linuxUsersMapper.updateUserInfo(userName, passwd, group, ip);
										setResult(resultMap, Constant.TWO_HUNDRED, Constant.UPDATE_USER_OK);
									}
								} catch (Exception e) {
									LOG.error(Thread.currentThread().getStackTrace()[1].getMethodName() + Constant.GET_EXCEPTION
											+ e.getMessage());
									setResult(resultMap, Constant.ZERO, Constant.UPDATE_USER_FIAL_MSG3 + e.getMessage());
								}
							} else {
								setResult(resultMap, Constant.ZERO, Constant.USER_PASSWD_NULL_MSG);
							}
						} else {
							setResult(resultMap, Constant.ZERO, Constant.CONN_IS_NULL);
						}
					} else {
						setResult(resultMap, Constant.ZERO, Constant.IP_NULL);
					}
				} else {
					setResult(resultMap, Constant.ZERO, Constant.CONN_MAP_ISNULL_MSG);
				}
		} 
		//update db:
		if (resultMap.get(Constant.STATUS_CODE).equals(Constant.TWO_HUNDRED)) {
			// 修改数据库中的userName&group:
			linuxUsersMapper.updateUserInfo(userName, passwd, group, ip);
		}
		return resultMap;
	}

	@Override
	public List<LinuxGroupEntity> getAllGroups(String ip) {
		RemoteExecuteCommandutil rec = linuxConnMap.get(ip);
		List<LinuxGroupEntity> list = null;
		if (rec != null) {
			// String cmd = "cat /etc/group";
			try {
				list = rec.executeCmd(Constant.CAT_GROUP_SHELL, 1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// TODO
		}
		return list;
	}

	@Override
	public void InsertAllGroups(String ip) {
		List<LinuxGroupEntity> allGroupsList = this.getAllGroups(ip);
		for (int j = 0; j < allGroupsList.size(); j++) {
			LinuxGroupEntity linuxGroupEntity = allGroupsList.get(j);
			linuxGroupEntity.setNode(ip);
		}
		// insert into db first one:
		linuxUsersMapper.insertAllGroups(allGroupsList);
	}

	
	/**
	 * newLinuxGroup
	 */
	@Override
	public Map newLinuxGroup(/*String ipStr, */LinuxGroupEntity linuxGroupEntity) {
		Map<Object, Object> resultMap = new HashMap<>();
		String ip = null;
		//StringBuilder cmd = new StringBuilder();

		List<String> ipList = linuxUsersMapper.getAllIpList();
		for (int k = 0; k < ipList.size(); k++) {
			 ip = ipList.get(k);
			 if (StringUtils.isEmpty(linuxConnMap.get(ip))) {
					this.reConn(ip);
				}

				if (!linuxConnMap.isEmpty()) {
					RemoteExecuteCommandutil rec = linuxConnMap.get(ip);
					if (rec != null) {
						// groupadd ttt
						String groupName = linuxGroupEntity.getGroupName();
						//linuxGroupEntity.setNode(ip);
						try {
							StringBuilder cmd = new StringBuilder();
							if (!StringUtils.isEmpty(groupName)) {
								cmd.append(Constant.GROUP_ADD_SHELL).append(groupName);
								String result = rec.executeCmd(cmd.toString(), 4);
								if (result.contains(Constant.GROUP_ALLREDY_EXIST2)) {
									setResult(resultMap, Constant.ZERO, groupName + Constant.ADD_GROUP_FIAL_MSG1);
								} else {
									setResult(resultMap, Constant.TWO_HUNDRED, Constant.ADD_GROUP_OK);
								}
							} else {
								setResult(resultMap, Constant.ZERO, Constant.ADD_GROUP_FIAL_MSG2);
							}
						} catch (Exception e) {
							LOG.error(Thread.currentThread().getStackTrace()[1].getMethodName() + Constant.GET_EXCEPTION
									+ e.getMessage());
							setResult(resultMap, Constant.ZERO, Constant.ADD_GROUP_FIAL_MSG3 + e.getMessage());
						}
					} else {
						setResult(resultMap, Constant.ZERO, Constant.CONN_IS_NULL);
					}
				} else {
					setResult(resultMap, Constant.ZERO, Constant.CONN_MAP_ISNULL_MSG);
				}
		}
		if (resultMap.get(Constant.STATUS_CODE).equals(Constant.TWO_HUNDRED)) {
			// save to db:
			linuxUsersMapper.insertAGroup(linuxGroupEntity);
		}
		return resultMap;
	}

	
	/**
	 * delLinuxGroup
	 */
	@Override
	public Map delLinuxGroup(HttpServletRequest request) {
		Map<Object, Object> resultMap = new HashMap<>();
		//StringBuilder cmd = new StringBuilder();
		String ip =null;
		//String ip = request.getParameter(Constant.IP);
		String groupName = request.getParameter(Constant.GROUP_NAME);

		List<String> ipList = linuxUsersMapper.getAllIpList();
		for (int k = 0; k < ipList.size(); k++) {
			 ip = ipList.get(k);
			 if (StringUtils.isEmpty(linuxConnMap.get(ip))) {
					this.reConn(ip);
				}
				if (!linuxConnMap.isEmpty()) {
					if (!StringUtils.isEmpty(ip)) {
						RemoteExecuteCommandutil rec = linuxConnMap.get(ip);
						if (rec != null) {
							if (!StringUtils.isEmpty(groupName)) {
								StringBuilder cmd = new StringBuilder();
								cmd.append(Constant.GROUP_DEL_SHELL).append(groupName);
								try {
									String result = rec.executeCmd(cmd.toString(), 4);
									if (result.contains(Constant.GROUP_NOT_EXIST)) {
										setResult(resultMap, Constant.ZERO, Constant.DEL_GROUP_FIAL_MSG1);
									} else {
										// delete from db:
										//linuxUsersMapper.delGroupByGroupName(groupName, ip);
										setResult(resultMap, Constant.TWO_HUNDRED, Constant.DEL_GROUP_OK);
									}
								} catch (Exception e) {
									LOG.error(Thread.currentThread().getStackTrace()[1].getMethodName() + Constant.GET_EXCEPTION
											+ e.getMessage());
									setResult(resultMap, Constant.ZERO, Constant.DEL_GROUP_FIAL_MSG2 + e.getMessage());
								}
							} else {
								setResult(resultMap, Constant.ZERO, Constant.GROUP_NAME_NULL);
							}
						} else {
							setResult(resultMap, Constant.ZERO, Constant.CONN_IS_NULL);
						}
					} else {
						setResult(resultMap, Constant.ZERO, Constant.IP_NULL);
					}
				} else {
					setResult(resultMap, Constant.ZERO, Constant.CONN_MAP_ISNULL_MSG);
				}
			 
		}
		if (resultMap.get(Constant.STATUS_CODE).equals(Constant.TWO_HUNDRED)) {
			// delete from db:
			//linuxUsersMapper.delGroupByGroupName(groupName, ip);
			linuxUsersMapper.delGroupByGroupByGName(groupName);
		}
		return resultMap;
	}

	
	/**
	 * showLinuxGroups
	 */
	@Override
	public Map showLinuxGroups(/*String ip, */HttpServletRequest request) {
		Map<Object, Object> resultMap = new HashMap<>();
		String groupName = null;
		int page = 0;
		int rows = 0;

		if (request != null) {
			groupName = request.getParameter(Constant.GROUP_NAME);

			if (request.getParameter(Constant.PAGE) != null) {
				page = Integer.parseInt(request.getParameter(Constant.PAGE));
			}
			if (request.getParameter(Constant.ROWS) != null) {
				rows = Integer.parseInt(request.getParameter(Constant.ROWS));
			}
			if (page <= 0) {
				page = 1;
			}
			if (rows <= 0) {
				rows = 20;
			}
		}
		
		try {
			page = page - 1;
			List<LinuxGroupEntity> groupsListInfo = linuxUsersMapper.getGroupsListInfo(groupName, page, rows);
			resultMap.put(Constant.TOTAL_COUNT, groupsListInfo.size());
			resultMap.put(Constant.LIST, groupsListInfo);
			setResult(resultMap, Constant.TWO_HUNDRED, Constant.SEARCH_OK_DB_MSG);
		} catch (Exception e) {
			LOG.error(Thread.currentThread().getStackTrace()[1].getMethodName() + Constant.GET_EXCEPTION
					+ e.getMessage());
			setResult(resultMap, Constant.ZERO, Constant.SEARCH_G_FAIL_MSG + e.getMessage());
		}
		

		/*
		if (StringUtils.isEmpty(ip)) {
			//get from db:
			page = page - 1;
			List<LinuxGroupEntity> groupsList = linuxUsersMapper.getGroupsList(ip, groupName, page, rows);
			int size = groupsList.size();
			resultMap.put(Constant.TOTAL_COUNT, size);
			resultMap.put(Constant.LIST, groupsList);
			setResult(resultMap, Constant.TWO_HUNDRED, Constant.SEARCH_OK_DB_MSG);
		}else {
			//get from Linux:
			//组查询数据库中的信息
		}
		
		
		if (StringUtils.isEmpty(linuxConnMap.get(ip))) {
			this.reConn(ip);
		}

		try {
			// List<LinuxGroupEntity> allGroups = this.getAllGroups(ip);
			if (!StringUtils.isEmpty(linuxConnMap.get(ip))) {
				// delete by node:
				linuxUsersMapper.delGroupByNode(ip);
				// insert into db:
				this.InsertAllGroups(ip);
			}
			// get from db:
			page = page - 1;
			List<LinuxGroupEntity> groupsList = linuxUsersMapper.getGroupsList(ip, groupName, page, rows);
			int size = groupsList.size();
			resultMap.put(Constant.TOTAL_COUNT, size);
			resultMap.put(Constant.LIST, groupsList);
			setResult(resultMap, Constant.TWO_HUNDRED, Constant.SEARCH_OK_DB_MSG);
		} catch (Exception e) {
			LOG.error(Thread.currentThread().getStackTrace()[1].getMethodName() + Constant.GET_EXCEPTION
					+ e.getMessage());
			setResult(resultMap, Constant.ZERO, Constant.SEARCH_FAIL_MSG + e.getMessage());
		}*/
		
		return resultMap;
	}

	
	/**
	 * showHostInfo
	 */
	@Override
	public Map showHostInfo(HttpServletRequest request) {
		Map<Object, Object> resultMap = new HashMap<>();
		int page = 0;
		int rows = 0;

		String ip = request.getParameter(Constant.IP);
		if (request.getParameter(Constant.PAGE) != null) {
			page = Integer.parseInt(request.getParameter(Constant.PAGE));
		}
		if (request.getParameter(Constant.ROWS) != null) {
			rows = Integer.parseInt(request.getParameter(Constant.ROWS));
		}
		if (page <= 0) {
			page = 1;
		}
		if (rows <= 0) {
			rows = 20;
		}

		try {
			page = page - 1;
			List<LinuxConnEntity> hostInfoList = linuxUsersMapper.getHostInfo(ip, page, rows);
			int size = hostInfoList.size();
			resultMap.put(Constant.TOTAL_COUNT, size);
			resultMap.put(Constant.LIST, hostInfoList);
			setResult(resultMap, Constant.TWO_HUNDRED, Constant.OK);
		} catch (Exception e) {
			LOG.error(Thread.currentThread().getStackTrace()[1].getMethodName() + Constant.GET_EXCEPTION
					+ e.getMessage());
			setResult(resultMap, Constant.ZERO, Constant.DEl_HOST_FAIL_MSG1 + e.getMessage());
		}
		return resultMap;
	}

	
	/**
	 * delLinuxHost
	 */
	@Override
	public Map delLinuxHost(HttpServletRequest request) {
		Map<Object, Object> resultMap = new HashMap<>();
		String ip = request.getParameter(Constant.IP);
		if (!StringUtils.isEmpty(ip)) {
			// delete host:
			try {
				linuxUsersMapper.delHostByIp(ip,Constant.HOST_CONN_FLAG);
				// delete group:
				linuxUsersMapper.delGroupByNode(ip);
				// delete user:
				linuxUsersMapper.delUserByNode(ip);
				//delete hostinfo:
				linuxUsersMapper.delFromHostinfo(ip);
				setResult(resultMap, Constant.TWO_HUNDRED, Constant.DEL_HOST_OK);
			} catch (Exception e) {
				LOG.error(Thread.currentThread().getStackTrace()[1].getMethodName() + Constant.GET_EXCEPTION
						+ e.getMessage());
				setResult(resultMap, Constant.ZERO, Constant.DEl_HOST_FAIL_MSG1 + e.getMessage());
			}
		} else {
			setResult(resultMap, Constant.ZERO, Constant.DEL_GROUP_FIAL_MSG2);
		}

		return resultMap;
	}

	
	/**
	 * syncDbUserToHost
	 */
	@Override
	public Map syncDbUserToHost(List<String> userNameList,HttpServletRequest request) {
		Map<Object, Object> resultMap = new HashMap<>();
		String decryptPassword = null;
		// TODO Auto-generated method stub
		//点击同步按钮，返回DB中所有的用户信息，只展示用户名:
		//用户勾选需要同步的用户，把用户名List形式返回给后台，以及返回需要同步的主机IP:
		//后台根据用户名List获取所有需要同步的用户信息，根据IP获取服务器连接，同步选中的用户信息到新增加的Host
		String ip = request.getParameter(Constant.IP);
		
		if (StringUtils.isEmpty(linuxConnMap.get(ip))) {
			this.reConn(ip);
		}
		RemoteExecuteCommandutil rec = linuxConnMap.get(ip);
		
		//同步之前先查看该主机上已有的用户:
		try {
			List<LinuxUserEntity> executeCmd = rec.executeCmd(Constant.CAT_ETC_PASSWD, 3);
			LinkedList<String> hostUsersList = new LinkedList<>();
			LinkedList<String> sameUsersList = new LinkedList<>();
			for(LinuxUserEntity list : executeCmd) {
				hostUsersList.add(list.getUserName());
			}
			//if (Collections.disjoint(hostUsersList, userNameList)) {
				for(String str : hostUsersList) {
					for(String str_final : userNameList) {
						if (str.equals(str_final)) {
							sameUsersList.add(str);
						}
					}
				}
				if (!sameUsersList.isEmpty()) {
					resultMap.put(Constant.LIST, sameUsersList);
					setResult(resultMap, Constant.TWO_HUNDRED, Constant.SAME_USER);
					return resultMap;
				}
			//}
		} catch (Exception e) {
			LOG.error(Thread.currentThread().getStackTrace()[1].getMethodName() + Constant.GET_EXCEPTION
					+ e.getMessage());
			setResult(resultMap, Constant.ZERO, Constant.SYNC_USERS_FAIL + e.getMessage());
		}

		for(int i = 0; i<userNameList.size(); i++) {
			String userName = userNameList.get(i);
			LinuxUserEntity dbUserInfo = linuxUsersMapper.getDbUserByName(userName);
			try {
				 decryptPassword = PasswordUtils.decryptPassword(dbUserInfo.getPasswd());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			String group = dbUserInfo.getGroups();
			 //cmd = USERADD+userName+";echo "+passwd+" | passwd --stdin "+userName;
			try {
				rec.executeCmd("useradd -m "+userName+";echo "+decryptPassword+" | passwd --stdin "+userName, 4);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		setResult(resultMap, Constant.TWO_HUNDRED, Constant.OK);
		return resultMap;
	}

	
	/**
	 * showSyncUserList
	 */
	@Override
	public Map showSyncUserList(HttpServletRequest request) {
		Map<Object, Object> resultMap = new HashMap<>();
		String cluster = request.getParameter(Constant.CLUSTER);
		try {
			List<String> allUserNameList = linuxUsersMapper.getAllUserNameList(cluster);
			//Set<String> allUserNameSet = linuxUsersMapper.getAllUserNameList(cluster);
			resultMap.put(Constant.LIST, allUserNameList);
			setResult(resultMap, Constant.TWO_HUNDRED, Constant.OK);
		} catch (Exception e) {
			LOG.error(Thread.currentThread().getStackTrace()[1].getMethodName() + Constant.GET_EXCEPTION
					+ e.getMessage());
			setResult(resultMap, Constant.ZERO, Constant.GET_DB_USERS_FIAL + e.getMessage());
		}
		
		return resultMap;
	}

	
	/**
	 * reSetPasswd
	 */
	@Override
	public Map reSetPasswd() {
		Map<Object, Object> resultMap = new HashMap<>();
		HashMap<String, String> passwdMap = new HashMap<>();
		HashMap<String, String> passwdDBMap = new HashMap<>();
		String ip = null;
		String resetPasswd = null;
		int b =1;
		/*//List<LinuxUserEntity> userAndPasswdList = linuxUsersMapper.getUserAndPasswdList();
		List<LinuxConnEntity> userAndPasswdList = linuxUsersMapper.getUserAndPasswdList();
		for(int j = 0; j<userAndPasswdList.size(); j++) {
			//LinuxUserEntity linuxUserEntity = userAndPasswdList.get(j);
			LinuxConnEntity linuxUserEntity = userAndPasswdList.get(j);
			passwdDBMap.put(linuxUserEntity.getUser(), linuxUserEntity.getLinuxPasswd());
		}*/
		List<String> ipList = linuxUsersMapper.getAllIpList();
		//LinkedList<ResetPasswdEntity> reSetList = new LinkedList<>();
		LinkedList<ResetPasswdEntity> reSetList = new LinkedList<>();
		for (int k = 0; k < ipList.size(); k++) {
			ip = ipList.get(k);
			
			if (StringUtils.isEmpty(linuxConnMap.get(ip))) {
				this.reConn(ip);
			}
			
			 if (!linuxConnMap.isEmpty()) {
					RemoteExecuteCommandutil rec = linuxConnMap.get(ip);
					if (rec != null) {
						try {
							//DB:
							List<LinuxConnEntity> userAndPasswdListByNode = linuxUsersMapper.getUserAndPasswdListByNode(ip);
							for(int j = 0; j<userAndPasswdListByNode.size(); j++) {
								LinuxConnEntity linuxUserEntity = userAndPasswdListByNode.get(j);
								passwdDBMap.put(linuxUserEntity.getUser()+Constant.UNDERLINE+ip, linuxUserEntity.getLinuxPasswd());
							}
							
							List<LinuxUserEntity> result = rec.executeCmd(Constant.CAT_ETC_SHADOW, 2);
							for(int i = 0; i<result.size(); i++) {
								LinuxUserEntity linuxUserEntity = result.get(i);
								passwdMap.put(linuxUserEntity.getUserName()+Constant.UNDERLINE+ip, linuxUserEntity.getPasswd());
							}
							//two map:
							for(Map.Entry<String, String> entry : passwdDBMap.entrySet()) {
								String userName = entry.getKey();
								String passwd = passwdMap.get(userName);
								String passwdDB = entry.getValue();
								System.out.println("***passwdDB****"+passwdDB);
								System.out.println("****passwd***"+passwd);
								// !(passwd.equals(passwdDB))
								if (!(passwd.equals(passwdDB)) && !StringUtils.isEmpty(passwd) && !StringUtils.isEmpty(passwdDB)) {
									//change passwd:
									//[root@tjt ~]# echo 720 | passwd --stdin 719
									//String passwdDbByName = linuxUsersMapper.getPasswdDbByName(userName);
									String splitUserName = userName.split(Constant.UNDERLINE)[0];
									String decryptPassword = PasswordUtils.decryptPassword(linuxUsersMapper.getPasswdDbByName(splitUserName));
									System.out.println("********"+decryptPassword);
									try {
										rec.executeCmd("echo "+decryptPassword+" | passwd --stdin "+splitUserName, 4);
											//要不要也同时改下数据库的linuxPasswd
										List<LinuxUserEntity> usersList = rec.executeCmd(Constant.CAT_ETC_SHADOW, 2);
										for(LinuxUserEntity list : usersList) {
											if (list.getUserName().equals(splitUserName)) {
												 resetPasswd = list.getPasswd();
											}
										}
											linuxUsersMapper.changeLinuxPasswd(resetPasswd, splitUserName,ip);
											ResetPasswdEntity resetPasswdEntity = new ResetPasswdEntity();
											resetPasswdEntity.setUserName(splitUserName);
											resetPasswdEntity.setNode(ip);
											reSetList.add(resetPasswdEntity);
											resultMap.put(Constant.LIST, reSetList);
											setResult(resultMap, Constant.TWO_HUNDRED, Constant.GET_LINUX_PASSWD_CHANGE_OK);
											System.out.println("change passwd.............................");
											//setResult(resultMap, Constant.TWO_HUNDRED, Constant.GET_LINUX_PASSWD_CHANGE_OK);
									} catch (Exception e) {
										LOG.error(Thread.currentThread().getStackTrace()[1].getMethodName() + Constant.GET_EXCEPTION
												+ e.getMessage());
										setResult(resultMap, Constant.ZERO, Constant.GET_LINUX_PASSWD_CHANGE_FIAL + e.getMessage());
									}
								}else {
									System.out.println(" passwd unchange.............................");
									if (reSetList.isEmpty()) {
										setResult(resultMap, Constant.TWO_HUNDRED, Constant.PASSWD_UNCHANGE_MSG);
									}
								}
							}
							//setResult(resultMap, Constant.TWO_HUNDRED, Constant.GET_LINUX_PASSWD_CHANGE_OK);
						} catch (Exception e) {
							LOG.error(Thread.currentThread().getStackTrace()[1].getMethodName() + Constant.GET_EXCEPTION
									+ e.getMessage());
							setResult(resultMap, Constant.ZERO, Constant.GET_LINUX_PASSWD_FIAL + e.getMessage());
						}
					}else {
						setResult(resultMap, Constant.ZERO, Constant.CONN_IS_NULL);
					}
		}else {
			setResult(resultMap, Constant.ZERO, Constant.CONN_MAP_ISNULL_MSG);
		}
		}
		return resultMap;
	}

	
	/**
	 * checkPasswd
	 */
	@Override
	public Map checkPasswd() {
		Map<Object, Object> resultMap = new HashMap<>();
		HashMap<String, String> passwdMap = new HashMap<>();
		HashMap<String, String> passwdDBMap = new HashMap<>();
		String ip = null;
		List<String> ipList = linuxUsersMapper.getAllIpList();
		LinkedList<ResetPasswdEntity> reSetList = new LinkedList<>();
		//LinkedList<String> reSetList = new LinkedList<>();
		for (int k = 0; k < ipList.size(); k++) {
			ip = ipList.get(k);
			
			if (StringUtils.isEmpty(linuxConnMap.get(ip))) {
				this.reConn(ip);
			}
			
			 if (!linuxConnMap.isEmpty()) {
					RemoteExecuteCommandutil rec = linuxConnMap.get(ip);
					if (rec != null) {
						try {
							//DB:
							List<LinuxConnEntity> userAndPasswdListByNode = linuxUsersMapper.getUserAndPasswdListByNode(ip);
							for(int j = 0; j<userAndPasswdListByNode.size(); j++) {
								LinuxConnEntity linuxUserEntity = userAndPasswdListByNode.get(j);
								passwdDBMap.put(linuxUserEntity.getUser()+Constant.UNDERLINE+ip, linuxUserEntity.getLinuxPasswd());
							}
							
							List<LinuxUserEntity> result = rec.executeCmd(Constant.CAT_ETC_SHADOW, 2);
							for(int i = 0; i<result.size(); i++) {
								LinuxUserEntity linuxUserEntity = result.get(i);
								passwdMap.put(linuxUserEntity.getUserName()+Constant.UNDERLINE+ip, linuxUserEntity.getPasswd());
							}
							//two map:
							for(Map.Entry<String, String> entry : passwdDBMap.entrySet()) {
								String userName = entry.getKey();
								String passwd = passwdMap.get(userName);
								String passwdDB = entry.getValue();
								System.out.println("***passwdDB****"+passwdDB);
								System.out.println("****passwd***"+passwd);
								// !(passwd.equals(passwdDB))
								if (!passwd.equals(passwdDB) && !StringUtils.isEmpty(passwd) && !StringUtils.isEmpty(passwdDB)) {
									// do not change password immediately
									ResetPasswdEntity resetPasswdEntity = new ResetPasswdEntity();
									//String splitUserName = userName.split(Constant.UNDERLINE)[0];
									resetPasswdEntity.setUserName(userName.split(Constant.UNDERLINE)[0]);
									resetPasswdEntity.setNode(ip);
									reSetList.add(resetPasswdEntity);
									resultMap.put(Constant.LIST, reSetList);
									setResult(resultMap, Constant.TWO_HUNDRED, Constant.PASSWD_CHANGE_MSG);
								}else {
									System.out.println(" passwd unchange.............................");
									if (reSetList.isEmpty()) {
										setResult(resultMap, Constant.TWO_HUNDRED, Constant.PASSWD_UNCHANGE_MSG);
									}
								}
							}
						} catch (Exception e) {
							LOG.error(Thread.currentThread().getStackTrace()[1].getMethodName() + Constant.GET_EXCEPTION
									+ e.getMessage());
							setResult(resultMap, Constant.ZERO, Constant.GET_LINUX_PASSWD_FIAL + e.getMessage());
						}
					}else {
						setResult(resultMap, Constant.ZERO, Constant.CONN_IS_NULL);
					}
		}else {
			setResult(resultMap, Constant.ZERO, Constant.CONN_MAP_ISNULL_MSG);
		}
		}
		return resultMap;
	}

	
	/**
	 * addCluster
	 */
	@Override
	public Map addCluster(ClusterEntity clusterEntity) {
		Map<Object, Object> resultMap = new HashMap<>();
		try {
			linuxUsersMapper.addCluster(clusterEntity);
			setResult(resultMap, Constant.TWO_HUNDRED, Constant.OK);
		} catch (Exception e) {
			LOG.error(Thread.currentThread().getStackTrace()[1].getMethodName() + Constant.GET_EXCEPTION
					+ e.getMessage());
			setResult(resultMap, Constant.ZERO, Constant.FAIL + e.getMessage());
		}
		return resultMap;
	}

	
	/**
	 * updateCluster
	 */
	@Override
	public Map updateCluster(String cluster) {
		Map<Object, Object> resultMap = new HashMap<>();
		try {
			linuxUsersMapper.updateCluster(cluster);
			setResult(resultMap, Constant.TWO_HUNDRED, Constant.OK);
		} catch (Exception e) {
			LOG.error(Thread.currentThread().getStackTrace()[1].getMethodName() + Constant.GET_EXCEPTION
					+ e.getMessage());
			setResult(resultMap, Constant.ZERO, Constant.FAIL + e.getMessage());
		}
		return resultMap;
	}

	
	/**
	 * showCluster
	 */
	@Override
	public Map showCluster() {
		Map<Object, Object> resultMap = new HashMap<>();
		try {
			List<String> findCluster = linuxUsersMapper.findCluster();
			resultMap.put(Constant.ROWS, findCluster);
			setResult(resultMap, Constant.TWO_HUNDRED, Constant.OK);
		} catch (Exception e) {
			LOG.error(Thread.currentThread().getStackTrace()[1].getMethodName() + Constant.GET_EXCEPTION
					+ e.getMessage());
			setResult(resultMap, Constant.ZERO, Constant.FAIL + e.getMessage());
		}
		return resultMap;
	}

	
	/**
	 * delCluster
	 */
	@Override
	public Map delCluster(String cluster) {
		Map<Object, Object> resultMap = new HashMap<>();
		try {
			linuxUsersMapper.delCluster(cluster);
			setResult(resultMap, Constant.TWO_HUNDRED, Constant.OK);
		} catch (Exception e) {
			LOG.error(Thread.currentThread().getStackTrace()[1].getMethodName() + Constant.GET_EXCEPTION
					+ e.getMessage());
			setResult(resultMap, Constant.ZERO, Constant.FAIL + e.getMessage());
		}
		return resultMap;
	}
	
	

}
