package com.ausclouds.bdbsec.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.ausclouds.bdbsec.entity.ClusterEntity;
import com.ausclouds.bdbsec.entity.HostInfoEntity;
import com.ausclouds.bdbsec.entity.LinuxConnEntity;
import com.ausclouds.bdbsec.entity.LinuxGroupEntity;
import com.ausclouds.bdbsec.entity.LinuxUserEntity;

@Mapper
@Repository
public interface LinuxUsersMapper {

	public LinuxUserEntity getInfo();

	// save hosts info:
	public void insertIntoDb(LinuxConnEntity linuxConnEntity);

	// cluster insert:
	public void insertIntoHostInfo(HostInfoEntity hostInfoEntity);

	// save users info:
	public int insertUsersInfotoDb(List<LinuxUserEntity> list);

	// get all ip list:
	public List<String> getAllIpList();

	// get all userName list:
	public List<String> getAllUserNameList(@Param("cluster") String cluster);
	// public Set<String> getAllUserNameList(@Param("cluster")String cluster);

	// insert all G: "InsertAllGroups"
	public void insertAllGroups(List<LinuxGroupEntity> list);

	// add a user:
	public int insertAUser(LinuxUserEntity linuxUserEntity);

	// add a user into conn table:
	public int insertAUser2Conn(LinuxConnEntity linuxConnEntity);

	// get users list from db:
	public List<LinuxUserEntity> getUsersList(@Param("ip") String ip, @Param("userName") String userName,
			@Param("group") String group, @Param("page") int page, @Param("rows") int rows);

	// get conn info by ip:
	public LinuxConnEntity getByIp(@Param("ip") String ip, @Param("flag") String flag);

	// delete user by userName
	public int delUserByName(@Param("userName") String userName/* , @Param("node") String node */);

	// delete user by userName & ip
	public int delUserByNameAndNode(@Param("userName") String userName, @Param("ip") String ip);

	// update user info:
	public int updateUserInfo(@Param("userName") String userName, @Param("passwd") String passwd,
			@Param("groups") String groups, @Param("node") String node);

	// add a group:
	public int insertAGroup(LinuxGroupEntity linuxGroupEntity);

	// delete group by groupName & node
	public int delGroupByGroupName(@Param("groupName") String groupName, @Param("node") String node);
	
	// delete group by groupName
	public int delGroupByGroupByGName(@Param("groupName") String groupName);

	// get groups list from db by ip:
	public List<LinuxGroupEntity> getGroupsList(@Param("ip") String ip, @Param("groupName") String groupName,
			@Param("page") int page, @Param("rows") int rows);
	
	// get groups list from db by:
	public List<LinuxGroupEntity> getGroupsListInfo(@Param("groupName") String groupName,@Param("page") int page, @Param("rows") int rows);
				

	// delete group by node:
	public int delGroupByNode(@Param("node") String node);

	// get all host info by ip:
	public List<LinuxConnEntity> getHostInfo(@Param("ip") String ip, @Param("page") int page, @Param("rows") int rows);

	// delete user by node:
	public int delUserByNode(@Param("node") String node);

	// delete host by ip:
	public int delHostByIp(@Param("ip") String ip, @Param("flag") String flag);

	// delFromHostinfo
	public int delFromHostinfo(@Param("ip") String ip);

	// get users list from db by userName:
	public LinuxUserEntity getDbUserByName(@Param("userName") String userName);

	// insert linuxPasswd:
	public int changeLinuxPasswd(@Param("linuxPasswd") String passwd, @Param("userName") String userName,
			@Param("ip") String ip);

	// get users and passwd list
	// public List<LinuxUserEntity> getUserAndPasswdList();
	public List<LinuxConnEntity> getUserAndPasswdList();

	// get users and passwd list by node
	public List<LinuxConnEntity> getUserAndPasswdListByNode(@Param("ip") String ip);

	public String getPasswdDbByName(@Param("userName") String userName);
	
	public int addCluster(ClusterEntity clusterEntity);
	public int updateCluster(@Param("cluster")String cluster);
	public int delCluster(@Param("cluster")String cluster);
	public List<String> findCluster();

}
