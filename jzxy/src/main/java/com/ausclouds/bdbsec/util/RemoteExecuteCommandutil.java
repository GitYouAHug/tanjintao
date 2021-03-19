
package com.ausclouds.bdbsec.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.ausclouds.bdbsec.entity.LinuxGroupEntity;
import com.ausclouds.bdbsec.entity.LinuxUserEntity;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

/**
 * @ClassName: RemoteExecuteCommand
 * @Description: 远程执行Linux命令
 */
@Service
public class RemoteExecuteCommandutil {

	// 字符编码默认是utf-8
	private static String DEFAULTCHART = "UTF-8";
	private Connection conn;
	private String ip;
	private String userName;
	private String userPwd;

	private static final Logger LOG = Logger.getLogger(RemoteExecuteCommandutil.class);

	private RemoteExecuteCommandutil() {
	}

	/**
	 * 远程登录linux主机
	 *
	 * @return 登录成功返回true，否则返回false
	 * @throws Exception
	 * @since V0.1
	 */
	public Connection login() throws Exception {
		boolean flg = false;
		try {
			conn = new Connection(ip);
			// 连接, 超时时长6s
			conn.connect(null, 0, 6000);
			// 认证
			flg = conn.authenticateWithPassword(userName, userPwd);
			if (flg) {
				return conn;
			}
		} catch (IOException e) {
			LOG.error(Thread.currentThread().getStackTrace()[1].getMethodName() + " ==|Exception : " + e.getMessage());
			throw new Exception("远程连接服务器失败", e);
		}
		return conn;
	}

	public void logout() {
		if (conn != null) {
			conn.close();
		}
	}

	/**
	 * RemoteExecuteCommandutil 初始化建立连接,异常时释放
	 *
	 * @param ip
	 * @param username
	 * @param password
	 * @throws Exception
	 */
	public RemoteExecuteCommandutil(String ip, String username, String password) throws Exception {
		this.ip = ip;
		this.userName = username;
		this.userPwd = password;

		Connection newConn = login();
		this.conn = newConn;
	}

	/**
	 * processStdoutForUserInfo
	 * 
	 * @param in
	 * @param charset
	 * @return
	 * @throws Exception
	 */
	private List<LinuxUserEntity> processStdoutForUserInfo(InputStream in, String charset) throws Exception {
		InputStream stdout = new StreamGobbler(in);
		StringBuffer buffer = new StringBuffer();
		InputStreamReader isr = null;
		BufferedReader br = null;
		List<LinuxUserEntity> linuxUserEntityList = new ArrayList<>();
		try {
			isr = new InputStreamReader(stdout, charset);
			br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.contains(":/")) {
					LinuxUserEntity linuxUserEntity = splitUserInfo(line);
					linuxUserEntityList.add(linuxUserEntity);
				}

				// RemoteException失败抛出异常, 注意下不同系统的异常串可能不同
				if (line.contains("RemoteException")) {
					throw new Exception(line);
				}
			}
		} catch (UnsupportedOperationException e) {
			throw new Exception("不支持的编码字符集异常", e);
		} catch (IOException e) {
			throw new Exception("读取失败", e);
		} finally {
			br.close();
			isr.close();
			stdout.close();
		}
		Collections.reverse(linuxUserEntityList);
		return linuxUserEntityList;
	}

	private List<LinuxUserEntity> processStdoutForUserPasswd(InputStream in, String charset) throws Exception {
		InputStream stdout = new StreamGobbler(in);
		StringBuffer buffer = new StringBuffer();
		InputStreamReader isr = null;
		BufferedReader br = null;
		List<LinuxUserEntity> linuxUserEntityList = new ArrayList<>();
		try {
			isr = new InputStreamReader(stdout, charset);
			br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] split = line.split(":");
				int length = split.length;
				if (length > 2) {
					LinuxUserEntity linuxUserEntity = splitUserPasswd(line);
					linuxUserEntityList.add(linuxUserEntity);
				}
				// RemoteException失败抛出异常, 注意下不同系统的异常串可能不同
				if (line.contains("RemoteException")) {
					throw new Exception(line);
				}
			}
		} catch (UnsupportedOperationException e) {
			throw new Exception("不支持的编码字符集异常", e);
		} catch (IOException e) {
			throw new Exception("读取失败", e);
		} finally {
			br.close();
			isr.close();
			stdout.close();
		}
		Collections.reverse(linuxUserEntityList);
		return linuxUserEntityList;
	}

	private List<LinuxGroupEntity> processStdoutForGroups(InputStream in, String charset) throws Exception {
		InputStream stdout = new StreamGobbler(in);
		StringBuffer buffer = new StringBuffer();
		InputStreamReader isr = null;
		BufferedReader br = null;
		List<LinuxGroupEntity> linuxGroupEntity = new ArrayList<>();
		try {
			isr = new InputStreamReader(stdout, charset);
			br = new BufferedReader(isr);
			String line = null;
			int a = 0;
			while ((line = br.readLine()) != null) {
				String[] split = line.split(":");
				int length = split.length;
				if (a > 6 && length > 2) {
					LinuxGroupEntity splitGroups = splitGroups(line);
					linuxGroupEntity.add(splitGroups);
				}
				a++;
				// RemoteException失败抛出异常, 注意下不同系统的异常串可能不同
				if (line.contains("RemoteException")) {
					throw new Exception(line);
				}
			}
		} catch (UnsupportedOperationException e) {
			throw new Exception("不支持的编码字符集异常", e);
		} catch (IOException e) {
			throw new Exception("读取失败", e);
		} finally {
			br.close();
			isr.close();
			stdout.close();
		}
		Collections.reverse(linuxGroupEntity);
		return linuxGroupEntity;
	}

	// resulttype
	// 0 STRING
	// 1 LIST
	// 2 AllKeys
	public <T> T executeCmd(String cmd, int resulttype) throws Exception {
		Session session = null;
		String BASH = "bash";
		String EXIT = "exit";
		Long TIMEOUT = 10000L;
		T t = null;
		try {
			if (conn != null) {
				session = conn.openSession();
				session.requestPTY(BASH);
				session.startShell();
				PrintWriter out = new PrintWriter(session.getStdin());
				out.println(cmd);
				out.println(EXIT);
				out.close();
				session.waitForCondition(ChannelCondition.CLOSED | ChannelCondition.EOF | ChannelCondition.EXIT_STATUS,
						TIMEOUT);
				if (resulttype == 1) {
					t = (T) processStdoutForGroups(session.getStdout(), DEFAULTCHART);
				} else if (resulttype == 2) {
					t = (T) processStdoutForUserPasswd(session.getStdout(), DEFAULTCHART);
				} else if (resulttype == 3) {
					// TODO
					t = (T) processStdoutForUserInfo(session.getStdout(), DEFAULTCHART);
				} else {
					t = (T) processStdout(session.getStdout(), DEFAULTCHART);
				}

				// 如果为输出为空，说明脚本执行出错了,消费它
				if (t == null) {
					processStdout(session.getStderr(), DEFAULTCHART);
				}
			} else {
				return null;
			}
		} catch (IOException e) {
			LOG.error(Thread.currentThread().getStackTrace()[1].getMethodName() + "resulttype:" + resulttype
					+ "==|Exception : " + e.getMessage());
			// 再次连接
			logout();
			login();
			throw new Exception("命令执行失败", e);
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return t;
	}

	/**
	 * 解析脚本执行返回的结果集
	 *
	 * @param in
	 * @param charset
	 * @return
	 * @throws Exception
	 */
	private String processStdout(InputStream in, String charset) throws Exception {
		InputStream stdout = new StreamGobbler(in);
		StringBuffer buffer = new StringBuffer();
		InputStreamReader isr = null;
		BufferedReader br = null;
		String[] strings = null;
		try {
			isr = new InputStreamReader(stdout, charset);
			br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				buffer.append(line + "\n");

				// RemoteException失败抛出异常, 注意下不同系统的异常串可能不同
				if (line.contains("RemoteException")) {
					throw new Exception(line);
				}
			}
		} catch (UnsupportedOperationException e) {
			throw new Exception("不支持的编码字符集异常", e);
		} catch (IOException e) {
			throw new Exception("读取指纹失败", e);
		} finally {
			br.close();
			isr.close();
			stdout.close();
		}
		return buffer.toString();
	}

	/**
	 * split for splitUserInfo
	 * 
	 * @param str
	 * @return
	 */
	public LinuxUserEntity splitUserInfo(String str) {
		LinuxUserEntity linuxUserEntity = new LinuxUserEntity();
		String[] split = str.split(":");
		linuxUserEntity.setUserName(split[0]);
		String[] split1 = split[5].split("/");
		if (split1.length != 0) {
			linuxUserEntity.setGroups(split1[split1.length - 1]);
		} else {
			// linuxUserEntity.setGruop(split[5].split("/")[0]);
		}
		// linuxUserEntity.setPasswd("******");
		return linuxUserEntity;
	}

	public LinuxUserEntity splitUserPasswd(String str) {
		LinuxUserEntity linuxUserEntity = new LinuxUserEntity();
		String[] split = str.split(":");
		linuxUserEntity.setUserName(split[0]);
		linuxUserEntity.setPasswd(split[1]);
		return linuxUserEntity;
	}

	public LinuxGroupEntity splitGroups(String str) {
		LinuxGroupEntity linuxGroupEntity = new LinuxGroupEntity();
		String[] split = str.split(":");
		linuxGroupEntity.setGroupName(split[0]);
		linuxGroupEntity.setGid(split[2]);
		return linuxGroupEntity;
	}

	// String ip, String username, String password
	public static void main(String[] args) throws Exception {
		// {"ip":"47.106.197.1","user":"root","passwd":"password@520"}
		RemoteExecuteCommandutil re = new RemoteExecuteCommandutil("47.106.197.1", "root", "password@520");
		// 新增用户user02，密码为user02,用户组为user02：
		// 用户组可能不存在
		String userName="root";
		//List<LinuxUserEntity> executeCmd = 
				Object executeCmd = re.executeCmd("cat /etc/shadow | grep "+userName, 4);
		// System.out.println("split one: "+ executeCmd.size());

		System.out.println(executeCmd);

		// 删除用户：
		// re.executeCmd("userdel -r user02",3);

		// 把用户 user02 的名称修改为 u02
		// re.executeCmd("usermod -l u02 user02",3);

		// 把用户 user02 的主组修改为 public 组
		// re.executeCmd("usermod -g public user02",3);

		// 通过一条命令修改用户名的密码： echo password(修改后的密码) | passwd --stdin ftpUser
		// 把u02的密码改为u02，之前是user02：
		// re.executeCmd("echo u02 | passwd --stdin u02",3);

		// 查看用户密码：
		// Object passwdList = re.executeCmd("cat /etc/shadow", 3);
		// System.out.println("passwdList: "+ passwdList);

		// 查询所有用户信息，ranger已经可以同步了
		// Object userList = re.executeCmd("cat /etc/passwd", 4);
		// String str = "用户“620”已存在";
		// System.out.println("userList: "+ o);

		// 添加组：新建工作组：groupadd groupname

		/*
		 * String str = "u02:x:1005:1005::/home/user02:/bin/bash"; String[] split =
		 * str.split(":"); //String s = split[5].split("/")[2];
		 * //System.out.println("split: "+s);
		 * System.out.println("split length: "+split[5].split("/").length);
		 * System.out.println("split length: "+split[5].split("/")[1]);
		 */

	}
}