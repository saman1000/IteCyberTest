package indrasoft.com.ite.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import org.apache.commons.codec.binary.Base64;

public class Configuration {
	private SecretKey secretKey;

	private String keyPath;

	private Map<String, String> parameters;
	
	public Configuration() throws Exception {
		parameters = new HashMap<>();
		keyPath = System.getProperty("user.home") + "/.SealedObject.ser";
		loadKeyFromFile();
		try (InputStream inputStream = Files.newInputStream(Paths.get("main/resources/application.properties"), StandardOpenOption.READ)) {
			PropertyResourceBundle resource = new PropertyResourceBundle(inputStream);
			for (String oneKey : resource.keySet()) {
				parameters.put(oneKey, resource.getString(oneKey));
			}
		}
	}

	public String getValue(String key) {
		return this.parameters.get(key);
	}
	
	public String getEncryptedValue(String key) {
		String result = getValue(key);
		
		return decrypt(result);
	}
	
	String decryptText(String encryptedText) {
		return decodeAndDecrypt(encryptedText);
	}

	String encryptText(String text) {
		return encrypt(text);
	}
	
	private String decodeAndDecrypt(String str) {
		return decrypt(str);
	}

	private void loadKeyFromFile() throws Exception {
		if (secretKey != null) {
			return;
		}
		
		try (ObjectInputStream fin = new ObjectInputStream(
				Files.newInputStream(Paths.get(getKeyPath()), StandardOpenOption.READ));) {
			secretKey = (SecretKey) fin.readObject();
		} catch (Exception e) {
			throw e;
		}
	}
	
	private String encrypt(String str) {
		String result = null;
		
		try {
			Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
			desCipher.init(Cipher.ENCRYPT_MODE, secretKey);
			byte[] encoded = desCipher.doFinal(str.getBytes());
			result = Base64.encodeBase64String(encoded);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		
		return result;
	}

	private String decrypt(String str) {
		String result = null;
		try {
			Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
			desCipher.init(Cipher.DECRYPT_MODE, secretKey);
			result = new String(desCipher.doFinal(Base64.decodeBase64(str.getBytes())));
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}

		return result;
	}

	private String getKeyPath() {
		return keyPath;
	}

	public void setKeyPath(String keyPath) {
		this.keyPath = keyPath;
	}

}
