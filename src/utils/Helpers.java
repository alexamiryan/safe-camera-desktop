package utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
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

	public static String decryptFilename(String fileName) {
		return decryptFilename(fileName, null);
	}

	public static String decryptFilename(String fileName, AESCrypt crypt) {
		String encryptedString = fileName;

		int extensionIndex = fileName.indexOf(SC_EXTENSION);
		if (extensionIndex > 0) {
			encryptedString = fileName.substring(0, extensionIndex);
		}

		if (encryptedString.length() >= 4 && encryptedString.substring(0, 4).equals("zzSC")) {
			encryptedString = encryptedString.substring(fileName.indexOf("_") + 1);
		}

		String decryptedFilename;
		if (crypt != null) {
			decryptedFilename = crypt.decrypt(encryptedString);
		}
		else {
			decryptedFilename = SafeCamera.crypto.decrypt(encryptedString);
		}

		if (decryptedFilename == null) {
			String extension = SC_EXTENSION;

			if (fileName.endsWith(extension)) {
				fileName = fileName.substring(0, fileName.length() - extension.length());
			}

			return fileName;
		}
		return decryptedFilename;
	}

	public static String encryptFilename(String fileName) {
		return encryptFilename(fileName, null);
	}

	public static String encryptFilename(String fileName, AESCrypt crypt) {
		return encryptString(fileName, crypt) + SC_EXTENSION;
	}

	public static String encryptString(String fileName) {
		return encryptString(fileName, null);
	}

	public static String encryptString(String fileName, AESCrypt crypt) {
		if (crypt != null) {
			return crypt.encrypt(fileName);
		}
		else {
			return SafeCamera.crypto.encrypt(fileName);
		}
	}

	public static String getNewDestinationPath(String path, String fileName, AESCrypt crypt) {
		return path + "/" + getNextAvailableFilePrefix(path) + encryptFilename(fileName, crypt);
	}

	public static String getNextAvailableFilePrefix(String path) {
		File dir = new File(path);
		File[] folderFiles = dir.listFiles();

		int maxNumber = 0;

		if (folderFiles != null) {
			for (File file : folderFiles) {
				String fileName = file.getName();
				Pattern p = Pattern.compile("^zzSC\\-\\d+\\_.+");
				Matcher m = p.matcher(fileName);
				if (m.find()) {
					try {
						int num = Integer.parseInt(fileName.substring(5, fileName.indexOf("_")));
						if (num > maxNumber) {
							maxNumber = num;
						}
					}
					catch (NumberFormatException e) {
					}
				}
			}
		}

		return "zzSC-" + String.valueOf(maxNumber + 1) + "_";
	}

	public static int getAltExifRotation(BufferedInputStream stream) {
		try {
			return getRotationFromMetadata(ImageMetadataReader.readMetadata(stream, false));
		}
		catch (ImageProcessingException e) {
		}
		catch (IOException e) {
		}

		return 0;
	}

	private static int getRotationFromMetadata(Metadata metadata) {
		try {
			ExifIFD0Directory directory = metadata.getDirectory(ExifIFD0Directory.class);
			if (directory != null) {
				int exifRotation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);

				switch (exifRotation) {
					case 3:
						// It's 180 deg now
						return 180;
					case 6:
						// It's 90 deg now
						return 90;
					case 8:
						// It's 270 deg now
						return 270;
					default:
						// It's 0 deg now
						return 0;
				}
			}
		}
		catch (MetadataException e) {
		}
		return 0;
	}

	public static ImageData rotate(ImageData srcData, int direction) {
		int bytesPerPixel = srcData.bytesPerLine / srcData.width;
		int destBytesPerLine = (direction == SWT.DOWN) ? srcData.width * bytesPerPixel : srcData.height * bytesPerPixel;
		byte[] newData = new byte[(direction == SWT.DOWN) ? srcData.height * destBytesPerLine : srcData.width * destBytesPerLine];
		int width = 0, height = 0;
		for (int srcY = 0; srcY < srcData.height; srcY++) {
			for (int srcX = 0; srcX < srcData.width; srcX++) {
				int destX = 0, destY = 0, destIndex = 0, srcIndex = 0;
				switch (direction) {
					case SWT.LEFT: // left 90 degrees
						destX = srcY;
						destY = srcData.width - srcX - 1;
						width = srcData.height;
						height = srcData.width;
						break;
					case SWT.RIGHT: // right 90 degrees
						destX = srcData.height - srcY - 1;
						destY = srcX;
						width = srcData.height;
						height = srcData.width;
						break;
					case SWT.DOWN: // 180 degrees
						destX = srcData.width - srcX - 1;
						destY = srcData.height - srcY - 1;
						width = srcData.width;
						height = srcData.height;
						break;
				}
				destIndex = (destY * destBytesPerLine) + (destX * bytesPerPixel);
				srcIndex = (srcY * srcData.bytesPerLine) + (srcX * bytesPerPixel);
				System.arraycopy(srcData.data, srcIndex, newData, destIndex, bytesPerPixel);
			}
		}
		// destBytesPerLine is used as scanlinePad to ensure that no padding is
		// required
		return new ImageData(width, height, srcData.depth, srcData.palette, srcData.scanlinePad, newData);
	}

	public static String getMainFolderPath() {
		Preferences prefs = Preferences.userNodeForPackage(com.fenritz.safecamdesktop.SafeCamera.class);
		String defaultPath = System.getProperty("user.home") + System.getProperty("file.separator") + "SafeCamera";

		return prefs.get(SafeCamera.PREF_MAINDIR, defaultPath);
	}

	public static String getOutputFolderPath() {
		Preferences prefs = Preferences.userNodeForPackage(com.fenritz.safecamdesktop.SafeCamera.class);
		String defaultPath = System.getProperty("user.home") + System.getProperty("file.separator") + "SafeCamera Decrypted";

		return prefs.get(SafeCamera.PREF_OUTDIR, defaultPath);
	}

	public static String ensureLastSlash(String path) {
		if (!path.endsWith("/")) {
			return path + "/";
		}
		return path;
	}

	public static void infoDialog(Shell shell, String message) {
		MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION);
		messageBox.setMessage(message);
		messageBox.open();
	}
	
	public static void errorDialog(Shell shell, String message) {
		MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
		messageBox.setMessage(message);
		messageBox.open();
	}

	public static boolean promtDialog(Shell shell, String message) {
		MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		messageBox.setMessage(message);
		int rc = messageBox.open();

		switch (rc) {
			case SWT.YES:
				return true;
			case SWT.NO:
				return false;
		}
		
		return false;
	}
}
