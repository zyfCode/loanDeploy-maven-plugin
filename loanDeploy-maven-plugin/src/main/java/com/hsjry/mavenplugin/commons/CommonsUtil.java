package com.hsjry.mavenplugin.commons;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * 
 * @author zhangyf18255
 */
public class CommonsUtil {
	private static final String DEFAULTENCODING = "UTF-8";
	private static final String USERHOME = System.getProperty("user.home ");

	
	private CommonsUtil(){}
	
	/**
	 * 将properties转换成bean
	 * @param clazz
	 * @param properties
	 * @return
	 */
	public static <T> T propertiesToBean(Class<T> clazz,Properties properties){
		try {
			BeanInfo sourceBeanInfo = Introspector.getBeanInfo(clazz);
			T targetBean = clazz.newInstance();
			PropertyDescriptor[] propertyDescriptors = sourceBeanInfo.getPropertyDescriptors(); 
			for (PropertyDescriptor pro : propertyDescriptors) {
				String name = pro.getName();
				if(name.equalsIgnoreCase("CLASS")){
					continue;
				}
				String value = properties.getProperty(name);
				Method writeMethod = pro.getWriteMethod();
				Class<?>[] parameterTypes = writeMethod.getParameterTypes();
				if(parameterTypes.length!=1){
					throw  new Exception("writeMethod 期望是一个，但实际上是："+parameterTypes.length);
				}
				Class<?> parameterType = parameterTypes[0];
				if(parameterType == byte.class||parameterType == Byte.class){
					byte b = new Byte(value);
					writeMethod.invoke(targetBean, b);
				}else if(parameterType == short.class || parameterType == Short.class){
					short s = new Short(value);
					writeMethod.invoke(targetBean, s);
				}else if(parameterType == char.class || parameterType == Character.class){
					int valueOf = Integer.valueOf(value);
					
					if(valueOf>Character.MAX_VALUE){
						throw new Exception("无效属性："+name+" 值:"+valueOf+" 超出最大值:"+Character.MAX_VALUE);
					}
					
					char valueChar = (char)valueOf;
					writeMethod.invoke(targetBean, valueChar);
				}else if(parameterType == int.class || parameterType == Integer.class){
					int valueOf = Integer.valueOf(value);
					writeMethod.invoke(targetBean, valueOf);
				}else if(parameterType == long.class || parameterType == Long.class){
					long valueOf = Long.valueOf(value);
					writeMethod.invoke(targetBean, valueOf);
				}else if(parameterType == float.class || parameterType == Float.class){
					float valueOf = Float.valueOf(value);
					writeMethod.invoke(targetBean, valueOf);
				}else if(parameterType == double.class || parameterType == Double.class){
					double valueOf = Double.valueOf(value);
					writeMethod.invoke(targetBean, valueOf);
				}else if(parameterType == String.class){
					writeMethod.invoke(targetBean, value);
				}else{
					throw new Exception("unsuport type "+parameterType.getName());
				}
			}
			return targetBean;
		} catch (Exception e) {
			throw new RuntimeException("转换失败",e);
		}
	}
	
	/**
	 * 将bean转换成propertes
	 * @param sourceBean
	 * @return
	 */
	public static Properties beanToProperties(Object sourceBean){
		try {
			BeanInfo sourceBeanInfo = Introspector.getBeanInfo(sourceBean.getClass());
			
			PropertyDescriptor[] propertyDescriptors = sourceBeanInfo.getPropertyDescriptors(); 
			Properties properties = new Properties();
			for (PropertyDescriptor pro : propertyDescriptors) {
				String name = pro.getName();
				Method readMethod = pro.getReadMethod();
				Object invoke = readMethod.invoke(sourceBean,null);
				if(invoke==null){
					properties.put(name, null);
				}else{
					properties.put(name, invoke.toString());
				}
			}
			return properties;
		} catch (Exception e) {
			throw new RuntimeException("转换失败",e);
		}
	}
	
	/**
	 * 从临时目录读取properties
	 * @param pro
	 */
	public static Properties readPropertiesFromUserHome(String directory,String fileName){
		try {
			File proFileParent = new File(USERHOME,directory);
			File temFile = new File(proFileParent,fileName);
			if(!temFile.exists()){
				return null;
			}
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(temFile),DEFAULTENCODING));
				Properties properties = new Properties();
				properties.load(reader);
				return properties;
			} finally {
				if(reader!=null){
					reader.close();
				}
			}
			
		} catch (IOException e) {
			throw new RuntimeException("",e);
		}
	}
	
	
	/**
	 * 把properties存到临时目录
	 * @param pro
	 */
	public static void savePropertiesToUserHome(String directory,String fileName,Properties pro){
		try {
			File propertiesParent = new File(USERHOME,directory);
			if(!propertiesParent.exists()){
				propertiesParent.mkdirs();
			} 
			File propertiesFile = new File(propertiesParent,fileName);
			
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(propertiesFile),DEFAULTENCODING));
				pro.store(writer, "linux properties");
				writer.flush();
			} finally {
				if(writer!=null){
					writer.close();
				}
			}
			
		} catch (IOException e) {
			throw new RuntimeException("",e);
		}
	}
}
