package utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Helpers {
	public static final String JPEG_FILE_PREFIX = "IMG_";
	public static final String SC_EXTENSION = ".sc";
	

	public static String getFilename(String prefix) {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = prefix + timeStamp;

		return imageFileName + ".jpg";
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
	
	public static String decryptFilename(Context context, String fileName){
		return decryptFilename(context, fileName, null);
	}
	
	public static String decryptFilename(Context context, String fileName, AESCrypt crypt){
		String encryptedString = fileName;
		
		int extensionIndex = fileName.indexOf(context.getString(R.string.file_extension));
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
			decryptedFilename = getAESCrypt(context).decrypt(encryptedString);
		}

		
		if(decryptedFilename == null){
			String extension = context.getString(R.string.file_extension);
			
			if(fileName.endsWith(extension)){
				fileName = fileName.substring(0, fileName.length() - extension.length());
			}
			
			return fileName;
		}
		return decryptedFilename;
	}*/
	
	
}
