package utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.fenritz.safecamdesktop.SafeCamera;

public class Helpers {
	public static final String JPEG_FILE_PREFIX = "IMG_";
	public static final String SC_EXTENSION = ".sc";
	public static final int MAX_FILE_SIZE = 30;
	

	public static String getFilename(String prefix) {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = prefix + timeStamp;

		return imageFileName + ".jpg";
	}

	
	public static String decryptFilename(String fileName){
		return decryptFilename(fileName, null);
	}
	
	public static String decryptFilename(String fileName, AESCrypt crypt){
		String encryptedString = fileName;
		
		int extensionIndex = fileName.indexOf(SC_EXTENSION);
		if(extensionIndex > 0){
			encryptedString = fileName.substring(0, extensionIndex);
		}
		
		if(encryptedString.length() >= 4 && encryptedString.substring(0, 4).equals("zzSC")){
			encryptedString = encryptedString.substring(fileName.indexOf("_")+1);
		}
		
		String decryptedFilename;
		if(crypt != null){
			decryptedFilename = crypt.decrypt(encryptedString);
		}
		else{
			decryptedFilename = SafeCamera.crypto.decrypt(encryptedString);
		}

		
		if(decryptedFilename == null){
			String extension = SC_EXTENSION;
			
			if(fileName.endsWith(extension)){
				fileName = fileName.substring(0, fileName.length() - extension.length());
			}
			
			return fileName;
		}
		return decryptedFilename;
	}
	
	/*public static String encryptFilename(String fileName){
		return encryptFilename(fileName, null);
	}
	
	public static String encryptFilename(String fileName, AESCrypt crypt){
		return encryptString(fileName, crypt) + SC_EXTENSION;
	}
	
	public static String encryptString(String fileName){
		return encryptString(fileName, null);
	}
	
	public static String encryptString(String fileName, AESCrypt crypt){
		if(crypt != null){
			return crypt.encrypt(fileName);
		}
		else{
			return getAESCrypt(context).encrypt(fileName);
		}
	}
	
	*/
	
	
}
