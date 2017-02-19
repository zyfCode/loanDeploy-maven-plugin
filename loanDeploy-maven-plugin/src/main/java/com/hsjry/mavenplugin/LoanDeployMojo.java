package com.hsjry.mavenplugin;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.hsjry.mavenplugin.linux.ssh.SSHUtils;
import com.jcraft.jsch.HostKey;

@Mojo( name = "loanDeploy")
public class LoanDeployMojo extends AbstractMojo {
	
	@Parameter(property="h")
	private String host;
	@Parameter(property="port",defaultValue="22")
	private String port;
	@Parameter(property="u")
	private String user;
	@Parameter(property="p")
	private String pwd;
	@Parameter(property="restart")
	private boolean restart;
	/**
	 * 上传指定的war包名称（系统会将warName与sftpFileProperties设置war包名称进行匹配）.如果此项为空，则默认上传sftpFileProperties属性设置的所有包
	 */
	@Parameter(property="m")
	private String warName;
	
	/**
	 * 将本地目录下的文档上传到linux的目录 key是本地目录，value是linux上的目录
	 */
	@Parameter
	private Properties rsyncDirectoryProperties;
	/**
	 * 将本地文件上传到linux上,key是本地文件，value是linux上的文件
	 */
	@Parameter
	private Properties warFileProperties;
	
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Log log = super.getLog();
//		Map pluginContext = super.getPluginContext(); 
//		Set<Map.Entry<Object,Object>> entrySet = pluginContext.entrySet();
//		for(Map.Entry<Object,Object> entry:entrySet){
//			log.info("hsjry maven plugin:"+entry.getKey()+" :::: "+entry.getValue());
//		}
//		log.info("==================");
//		Properties properties = System.getProperties();
//		Set<Entry<Object, Object>> proEntry = properties.entrySet();
//		for(Map.Entry<Object, Object> entry:proEntry){
//			log.info("hsjry maven plugin:"+entry.getKey()+" :::: "+entry.getValue());
//		}
//		log.info("==================");
//		URL resource = this.getClass().getClassLoader().getResource("./");
//		log.info("My plugin 111:"+resource); 
//		log.info("==================");
//		log.info(this.toString());
		SSHUtils.connectSSH(host, new Integer(port), user, pwd); 
		putAndRestart();
		SSHUtils.disconnect();
	}

	/**
	 * 将war包上传到Linux,并重启相关服务 
	 * @param host
	 */
	private void putAndRestart(){
		if(warName==null||warName.trim().equals("")){//上传设置的所有包
			Set<Entry<Object, Object>> entrySet = warFileProperties.entrySet();
			for(Entry<Object, Object> entry:entrySet){
				String sourceFileStr = entry.getKey()+"";
				String targetFile = entry.getValue()+"";
				this.put(sourceFileStr, targetFile);
			}
		}else{
			Set<Entry<Object, Object>> entrySet = warFileProperties.entrySet();
			for(Entry<Object, Object> entry:entrySet){
				String sourceFileStr = entry.getKey()+"";
				String targetFile = entry.getValue()+"";
				File sourceFile = new File(sourceFileStr);
				if(sourceFile.exists()&&sourceFile.isDirectory()){
					File warFile = new File(sourceFile,warName);
					if(!warFile.exists()){
						continue;
					}
				}else{
					String name = sourceFile.getName();
					if(!warName.equals(name)){
						continue;
					}
				}
				this.put(sourceFileStr, targetFile);
			}
		}
	}
	 
	private void put(String sourceFileStr,String  targetFile){
		File sourceFile = new File(sourceFileStr);
		if(!sourceFile.exists()){
			throw new RuntimeException(sourceFileStr+"目录不存在");
		}
		if(sourceFile.isDirectory()){
			File[] listFiles = this.listWar(sourceFile);
			if(listFiles==null||listFiles.length!=1){
				throw new RuntimeException(sourceFile+"找到"+listFiles.length+"个war包，期望值是1个");
			}
			sourceFile = listFiles[0];
		}
		boolean linuxDirectIsExists = SSHUtils.linuxDirectIsExists(targetFile);
		if(!linuxDirectIsExists){
			SSHUtils.mkdirs(targetFile);
		}
		super.getLog().info("put file "+sourceFileStr+"-->" +targetFile+" begin...");
		SSHUtils.sftpPut(sourceFileStr, targetFile);
		super.getLog().info("put file "+sourceFileStr+"-->" +targetFile+" finished");
		if(this.restart){
			String shutdownSHDirectory = targetFile+"/../bin/";
			String shutdownSHFileName = "shutdown.sh";
			boolean linuxFileIsExist = SSHUtils.linuxFileIsExist(shutdownSHDirectory, shutdownSHFileName);
			if(linuxFileIsExist){
				String execCommand = SSHUtils.execCommand("exec "+shutdownSHDirectory+shutdownSHFileName)+"";
				super.getLog().info("shutdown begin..");
				//校验tomcat是否已经停止
				while(!execCommand.contains("Connection refused")){
					execCommand = SSHUtils.execCommand("exec "+shutdownSHDirectory+shutdownSHFileName)+"";
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
				super.getLog().info("shutdown finished");
			}
			//清空webapps
			super.getLog().info("清空work begin...");
			String clear = SSHUtils.execCommand("rm -rf "+targetFile+"/../work/*");
			super.getLog().info("清空work  "+clear);
			clear = SSHUtils.execCommand("rm -rf "+targetFile+"/../temp/*");
			super.getLog().info("清空temp  "+clear);
			clear = SSHUtils.execCommand("rm -rf "+targetFile+"/../logs/*");
			super.getLog().info("清空logs  "+clear);
			clear = SSHUtils.execCommand("rm -rf "+targetFile+"/../webapps/*");
			super.getLog().info("清空webapps  "+clear);
			super.getLog().info("清空  finished ");
			String execCommand2 = SSHUtils.execCommand("exec "+targetFile+"/../bin/startup.sh");
			super.getLog().info("startup:::"+execCommand2);
		}
	}
	
	private File[] listWar(File warParentFile){
		File[] listFiles = warParentFile.listFiles(new  FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if(pathname.isFile()&&pathname.getName().endsWith(".war")){
					return true;
				}
				return false;
			}
		});
		return listFiles;
	}

	public void setHost(String host) {
		this.host = host;
	}



	public void setPort(String port) {
		this.port = port;
	}



	public void setUser(String user) {
		this.user = user;
	}



	public void setPwd(String pwd) {
		this.pwd = pwd;
	}



	public void setRsyncDirectoryProperties(Properties rsyncDirectoryProperties) {
		this.rsyncDirectoryProperties = rsyncDirectoryProperties;
	}


	public void setRestart(boolean restart) {
		this.restart = restart;
	}

	public void setWarName(String warName) {
		this.warName = warName;
	}

	public void setWarFileProperties(Properties warFileProperties) {
		this.warFileProperties = warFileProperties;
	}

	@Override
	public String toString() {
		return "LoanDeployMojo [host=" + host + ", port=" + port + ", user=" + user + ", pwd=" + pwd + ", restart="
				+ restart + ", warName=" + warName + ", rsyncDirectoryProperties=" + rsyncDirectoryProperties
				+ ", warFileProperties=" + warFileProperties + "]";
	}


	
}
