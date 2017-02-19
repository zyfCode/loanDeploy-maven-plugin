package com.hsjry.mavenplugin.linux.ssh;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KnownHosts;
import com.jcraft.jsch.Session;

public class SSHUtils {
	private static final String CHANNELTYPE_EXEC = "exec";
	private static final String CHANNELTYPE_SHELL = "shell";
	private static final String CHANNELTYPE_SFTP = "sftp";
	private static final String DEFAULTENCODING = "UTF-8";
	private static final ThreadLocal<Session> threadLocal = new ThreadLocal<Session>();
	private SSHUtils(){}
	
	/**
	 * ssh到linux上
	 * @param host
	 * @param port
	 * @param user
	 * @param password
	 * @throws RuntimeException
	 */
	public static void connectSSH(String host,int port,String user,String password) throws RuntimeException{
		Session session = threadLocal.get();
		if(session==null){
		  try {
			JSch jsch=new JSch();
			  session = jsch.getSession(user, host, port);
			  java.util.Properties config = new java.util.Properties();
			  config.put("StrictHostKeyChecking", "no");
			  session.setConfig(config);
			  session.setPassword(password);
			  session.connect();
			  threadLocal.set(session);
		} catch (JSchException e) {
			throw new RuntimeException("连接服务器异常:::host:"+host+"  port:"+port+"  userName:"+user+"  password:"+password,e);
		}
		}
	}
	/**
	 * ssh到linux上
	 * @param host
	 * @param port
	 * @param user
	 * @param password
	 * @throws RuntimeException
	 */
	public static void connectSSHInPublicKey(String host,int port,String user,String keyfile) throws RuntimeException{
		Session session = threadLocal.get();
		if(session==null){
			try {
				JSch jsch=new JSch();
				jsch.addIdentity(keyfile, keyfile);
				session = jsch.getSession(user, host, port);
				java.util.Properties config = new java.util.Properties();
				config.put("StrictHostKeyChecking", "no");
				session.setConfig(config);
//				session.setPassword(password);
				session.connect();
				threadLocal.set(session);
			} catch (JSchException e) {
				throw new RuntimeException("连接服务器异常:::host:"+host+"  port:"+port+"  userName:"+user,e);
			}
		}
	}
	
	public static void disconnect(){
		Session session = threadLocal.get();
		if(session!=null){
			threadLocal.remove();
			session.disconnect();
		}
	}
	
	
	
	/**
	 * 执行命令并返回结果
	 * @param command
	 * @return
	 * @throws Exception 
	 */
	public static String execCommand(String command) throws RuntimeException{
		Session session = threadLocal.get();
		if(session==null){
			throw new RuntimeException("disconnect!");
		}
		ChannelExec execChannel = null;
		try {
			try {
				execChannel = (ChannelExec) session.openChannel(SSHUtils.CHANNELTYPE_EXEC);
				execChannel.setCommand(command);
				execChannel.connect();
				InputStream in = execChannel.getInputStream();
				InputStream errStream = execChannel.getErrStream();
				BufferedReader bufReader = new BufferedReader(new InputStreamReader(in, SSHUtils.DEFAULTENCODING));
				StringBuffer result = new StringBuffer();
				String line = null;
				while((line=bufReader.readLine())!=null){
						result.append(line).append("\r\n");
				}
				BufferedReader bufErrReader = new BufferedReader(new InputStreamReader(errStream, SSHUtils.DEFAULTENCODING));
				line = null;
				while((line=bufErrReader.readLine())!=null){
						result.append(line).append("\r\n");
				}
				return result.toString();
			} finally{
				execChannel.disconnect();
			}
		} catch (Exception e) {
			throw new RuntimeException("",e);
		}
	}
	
	public static boolean mkdirs(String linuxDirectory){
		String execCommand = SSHUtils.execCommand("mkdirs -p "+linuxDirectory);
		if(execCommand.contains("mkdir: cannot")){
			throw new RuntimeException("mkdir -p "+linuxDirectory+" 失败:::"+execCommand);
		}
		return true;
	}
	/**
	 * 查看linux上的目录是否存在
	 * @param parentDirectory
	 * @param fileName
	 * @return
	 */
	public static boolean linuxFileIsExist(String parentDirectory,String fileName){
		boolean linuxDirectIsExists = SSHUtils.linuxDirectIsExists(parentDirectory);
		if(linuxDirectIsExists){
			String execCommand = SSHUtils.execCommand("find "+parentDirectory+" -name "+fileName);
			if(execCommand!=null&&execCommand.contains(fileName)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 查看文件目录是否存在
	 * @param linuxDirectory
	 * @return
	 */
	public static boolean linuxDirectIsExists(String linuxDirectory){
		String execCommand = SSHUtils.execCommand("ls "+linuxDirectory);
		if(execCommand.contains("No such file or directory")){
			return false;
		}
		return true;
	}
	
	/**
	 *sftp put文件到系统
	 * @param src
	 * @param dst
	 * @return
	 * @throws RuntimeException
	 */
	public static void sftpPut(String src,String dst) throws RuntimeException{
		if(src==null||src.trim().equals("")||!new File(src).exists()){ 
			throw new RuntimeException("unknow source:::"+src);
		}
		Session session = threadLocal.get();
		if(session==null){
			throw new RuntimeException("disconnect!");
		}
		ChannelSftp openChannel =null;
		try {
			try {
				openChannel = (ChannelSftp) session.openChannel(SSHUtils.CHANNELTYPE_SFTP);
				openChannel.connect();
				openChannel.put(src, dst);
			} finally {
				if(openChannel!=null){
					openChannel.disconnect();
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("",e);
		}
	}
	
	/**
	 * 执行shell脚本
	 * @param shell
	 * @return
	 * @throws Exception 
	 */
	public static String shellScript(String shell) throws RuntimeException{
		Session session = threadLocal.get();
		if(session==null){
			throw new RuntimeException("disconnect!");
		}
		try {
			ChannelShell openChannel =null;
			try {
				openChannel = (ChannelShell) session.openChannel(SSHUtils.CHANNELTYPE_SHELL);
				openChannel.setInputStream(System.in);
				openChannel.setOutputStream(System.out);
				openChannel.connect();
//				OutputStream out = openChannel.getOutputStream();
//				out.write(shell.getBytes(SSHUtils.DEFAULTENCODING));
//				InputStream in = openChannel.getInputStream();
//				BufferedReader bufReader = new BufferedReader(new InputStreamReader(in, SSHUtils.DEFAULTENCODING));
				StringBuffer result = new StringBuffer();
				char charArr[] = new char[1024];
				return result.toString();
			} finally {
				if(openChannel!=null){
					openChannel.disconnect();
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("",e);
		}
	}
	
}
