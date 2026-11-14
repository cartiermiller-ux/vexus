package email;

import java.util.Properties;

import kernel.util.PropertiesLoaderUtils;

public class Config {
	
	private static Properties properties = PropertiesLoaderUtils.loadProperties("config/system.properties");
	/**
	 * host
	 */
	public static final String host = EmailPropertiesUtil.getProperty("email.host");
	/**
	 *username
	 */
	public static final String username = EmailPropertiesUtil.getProperty("email.username");
	/**
	 * password
	 */
	public static final String password = EmailPropertiesUtil.getProperty("email.password");
	/**
	 * from
	 */
	public static final String from = EmailPropertiesUtil.getProperty("email.from");

	public static final String ENCRYPTION_KEY  = properties.getProperty("encryption.key");
	
	public static final String VERSION_NUMBER  = properties.getProperty("version.number");
}
