package com.hsjry.test.com;

import java.util.Properties;

import org.apache.maven.model.Plugin;
import org.junit.Test;

import com.hsjry.mavenplugin.commons.CommonsUtil;
import com.hsjry.mavenplugin.commons.SecurertUtil;
import com.hsjry.mavenplugin.domain.PluginBean;
import com.hsjry.mavenplugin.linux.ssh.SSHUtils;

public class TestPlugin {
	
	@Test
	public void test5(){
		SSHUtils.connectSSH("10.139.54.208", 22,  "loan", "2o$SqwFy9");
		SSHUtils.shellScript("echo hello");
//		String execCommand = SSHUtils.execCommand("su tmpuser");
//		System.out.println(execCommand);
//		String command = "#!/usr/bin/expect \r\n"+
//							"spawn su root \r\n"+  
//							"expect \"password: \"\r\n"+   
//							"send \"2oSqwFy9.\r\" \r\n"  +
//							"expect eof \r\n"+  
//							"exit \r";
//		String suCommand = SSHUtils.suCommand("su tmpuser", "2oSqwFy9.");
//		System.out.println(suCommand);
//		String execCommand = SSHUtils.execCommand("ls ./","whoami");
//		System.out.println(execCommand);
		
	}

	@Test
	public void test4(){
		PluginBean plugin = new PluginBean();
		plugin.setHost("10.139.54.208");
		plugin.setPort(22);
		plugin.setPwd("loan");
		plugin.setUserName("2o$SqwFy9");
		Properties beanToProperties = CommonsUtil.beanToProperties(plugin);
		CommonsUtil.savePropertiesToUserHome(PluginBean.parentDirectory, PluginBean.fileName, beanToProperties);
		Properties readPropertiesFromUserHome = CommonsUtil.readPropertiesFromUserHome(PluginBean.parentDirectory, PluginBean.fileName);
		System.out.println(readPropertiesFromUserHome);
	}
	
//	@Test
//	public void test3(){
//		PluginBean plugin = new PluginBean();
//		plugin.setHost("10.139.54.208");
//		plugin.setPort(22);
//		plugin.setPwd("loan");
//		plugin.setUserName("2o$SqwFy9");
//		Properties beanToProperties = CommonsUtil.beanToProperties(plugin);
//		System.out.println(beanToProperties);
//		CommonsUtil.savePropertiesTotemFile(PluginBean.filePrefix, PluginBean.fileSuffix, beanToProperties);
//		PluginBean propertiesToBean = CommonsUtil.propertiesToBean(PluginBean.class, beanToProperties);
//		System.out.println(propertiesToBean);
//	}
	@Test
	public void test2(){
		String key = "12345678";
		String encryptData = SecurertUtil.encryptData("hello", key);
		System.out.println(encryptData);
		String decryPwd = SecurertUtil.decryPwd(encryptData, key);
		System.out.println(decryPwd);
	}
	
	@Test
	public void test1(){
		try {
			SSHUtils.connectSSH("10.139.54.208", 22,  "loan", "2o$SqwFy9");
			String result = null;
//			result = SSHUtils.execCommand("chmod 755 ./fortest/test.sh");
			result = SSHUtils.execCommand("./fortest/test.sh");
//			result = SSHUtils.execCommand("ls ./fortest/");
			System.out.println(result);
//			SSHUtils.sftpPut("./1.txt", "/hsdata/loan/fortest");
//			SSHUtils.disconnect();
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		String exec = SSHUtils.exec("10.139.54.208", "loan", "2o$SqwFy9" , 22 , "ls");
//		System.out.println(exec);
	}
}
