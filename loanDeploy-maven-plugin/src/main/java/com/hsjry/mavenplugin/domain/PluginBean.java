package com.hsjry.mavenplugin.domain;

import java.io.Serializable;

public class PluginBean implements Serializable{
	private static final long serialVersionUID = 1L;
	public static final String parentDirectory = "hsdata/loan";
	public static final String fileName = "LINUX_ATTRI.properties";
	private String host;
	private int port;
	private String userName;
	private String pwd;
	
	@Override
	public String toString() {
		return "PluginBean [host=" + host + ", port=" + port + ", userName=" + userName + ", pwd=" + pwd + "]";
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
}
