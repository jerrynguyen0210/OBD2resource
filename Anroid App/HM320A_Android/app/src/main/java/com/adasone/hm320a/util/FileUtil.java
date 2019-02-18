/*
*  /hm310 - /logs
*         |
*         - /firmware
*         |
*         - /device  - /dev0001 - /calibration
*                               |
*                               - /video  -  /normal
*                                         |
*                                         -  /event
*                                         |
*                                         -  /parking
*
*/

package com.adasone.hm320a.util;

import android.os.Environment;
import android.util.Log;

import com.adasone.hm320a.application.Constants;

import java.io.File;
import java.io.FilenameFilter;

public class FileUtil {
	private final static String TAG = FileUtil.class.getSimpleName();
	private final static String CALIBRATION_NORMAL_FILENAME = "calibration.jpg";
    private final static String CALIBRATION_AUTO_FIRST_FILENAME = "calibration_auto1.jpg";
    private final static String CALIBRATION_AUTO_SECOND_FILENAME = "calibration_auto2.jpg";

	public final static long EXTRA_STORAGE_MARGIN = 52428800L; // (1024 * 1024 * 50);

	public static boolean checkDefaultDirectory () {
		File dir;

		// Model Dir
		dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.HM320A_DIR);
		if (!dir.exists()) {
			if (!dir.mkdir()) {
				return false;
			}
		}

		// Logs Dir
		dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.LOG_DIR);
		if (!dir.exists()) {
			if (!dir.mkdir()) {
				return false;
			}
		}

		// Firmware Dir
		dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.FW_DIR);
		if (!dir.exists()) {
			if (!dir.mkdir()) {
				return false;
			}
		}

		// Devices Dir
		dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.DEVICE_DIR);
		if (!dir.exists()) {
			if (!dir.mkdir()) {
				return false;
			}
		}
		return true;
	}

	public static boolean checkDeviceDirectory (String deviceToken) {
		String deviceCodeDirPath;
		File dir;

		deviceCodeDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() +
				Constants.DEVICE_DIR + Constants.DIR_STR + deviceToken;

		// Device code dir
		dir = new File(deviceCodeDirPath);
		if (!dir.exists()) {
			if (!dir.mkdir()) {
				return false;
			}
		}

		// Calibration picture dir
		dir = new File(deviceCodeDirPath + Constants.CALIBRATION_DIR);
		if (!dir.exists()) {
			if (!dir.mkdir()) {
				return false;
			}
		}

		// Video dir
		dir = new File(deviceCodeDirPath + Constants.VIDEO_DIR);
		if (!dir.exists()) {
			if (!dir.mkdir()) {
				return false;
			}
		}

		// Normal video dir
		dir = new File(deviceCodeDirPath + Constants.VIDEO_NORMAL_DIR);
		if (!dir.exists()) {
			if (!dir.mkdir()) {
				return false;
			}
		}

		// Event video dir
		dir = new File(deviceCodeDirPath + Constants.VIDEO_EVENT_DIR);
		if (!dir.exists()) {
			if (!dir.mkdir()) {
				return false;
			}
		}
		return true;
	}
    /*
    *  Normal Calibration
    */
	public static void deleteCalibrationNormalFileIfExist (String deviceToken) {
		// Calibration normal picture file
		File file = new File(FileUtil.getCalibrationNormalFileStorePath(deviceToken));
		if (file.exists()) {
			try {
				if (!file.delete()) {
					Log.e(TAG, "Delete fail : " + file.getAbsolutePath());
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
	}

    public static boolean isExistCalibrationNormalPicture (String deviceToken) {
        boolean ret = false;
        File file;

        // Calibration picture file
        file = new File(FileUtil.getCalibrationNormalFileStorePath(deviceToken));
        if (file.exists() && file.length() > 0) {
            ret = true;
        }

        return ret;
    }

    public static String getCalibrationNormalFilePathIfExist (String deviceToken) {
        String filepath = "";

        // Calibration picture file
        File file = new File(FileUtil.getCalibrationNormalFileStorePath(deviceToken));
        if (file.exists() && file.length() > 0) {
            filepath = file.getAbsolutePath();
        }

        return filepath;
    }

    public static String getCalibrationNormalFileStorePath (String deviceToken) {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.DEVICE_DIR
                + Constants.DIR_STR + deviceToken  + Constants.CALIBRATION_DIR
                + Constants.DIR_STR + CALIBRATION_NORMAL_FILENAME;
    }

    /*
    *  Auto  Calibration
    */
	public static void deleteCalibrationAutoFileIfExist (String deviceToken) {
		// Calibration auto first picture file
		File file;

		file = new File(FileUtil.getCalibrationFirstAutoFileStorePath(deviceToken));
		if (file.exists()) {
			try {
				if (!file.delete()) {
					Log.e(TAG, "Delete fail : " + file.getAbsolutePath());
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}

		// Calibration auto second picture file
		file = new File(FileUtil.getCalibrationSecondAutoFileStorePath(deviceToken));
		if (file.exists()) {
			try {
				if (!file.delete()) {
					Log.e(TAG, "Delete fail : " + file.getAbsolutePath());
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
	}


    public static boolean isExistCalibrationFirstAutoPicture (String deviceToken) {
        boolean ret = false;
        File file;

        // Calibration picture file
        file = new File(FileUtil.getCalibrationFirstAutoFileStorePath(deviceToken));
        if (file.exists() && file.length() > 0) {
            ret = true;
        }

        return ret;
    }

    public static String getCalibrationFirstAutoFilePathIfExist (String deviceToken) {
        String filepath = "";

        // Calibration picture file
        File file = new File(FileUtil.getCalibrationFirstAutoFileStorePath(deviceToken));
        if (file.exists() && file.length() > 0) {
            filepath = file.getAbsolutePath();
        }

        return filepath;
    }

    public static String getCalibrationFirstAutoFileStorePath (String deviceToken) {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.DEVICE_DIR
                + Constants.DIR_STR + deviceToken  + Constants.CALIBRATION_DIR
                + Constants.DIR_STR + CALIBRATION_AUTO_FIRST_FILENAME;
    }

    public static boolean isExistCalibrationSecondAutoPicture (String deviceToken) {
        boolean ret = false;
        File file;

        // Calibration picture file
        file = new File(FileUtil.getCalibrationSecondAutoFileStorePath(deviceToken));
        if (file.exists() && file.length() > 0) {
            ret = true;
        }

        return ret;
    }

    public static String getCalibrationSecondAutoFilePathIfExist (String deviceToken) {
        String filepath = "";

        // Calibration picture file
        File file = new File(FileUtil.getCalibrationSecondAutoFileStorePath(deviceToken));
        if (file.exists() && file.length() > 0) {
            filepath = file.getAbsolutePath();
        }

        return filepath;
    }

    public static String getCalibrationSecondAutoFileStorePath (String deviceToken) {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.DEVICE_DIR
                + Constants.DIR_STR + deviceToken  + Constants.CALIBRATION_DIR
                + Constants.DIR_STR + CALIBRATION_AUTO_SECOND_FILENAME;
    }



	public static String getNormalVideoFileStorePath (String deviceToken, String filename) {
		return Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.DEVICE_DIR
				+ Constants.DIR_STR + deviceToken  + Constants.VIDEO_NORMAL_DIR
				+ Constants.DIR_STR + filename;
	}

	public static String getEventVideoFileStorePath (String deviceToken, String filename) {
		return Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.DEVICE_DIR
				+ Constants.DIR_STR + deviceToken  + Constants.VIDEO_EVENT_DIR
				+ Constants.DIR_STR + filename;
	}

	public static File[] getNormalVideoLocalFileList(String deviceToken) {
		File fileList[] = null;

		String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.DEVICE_DIR
				+ Constants.DIR_STR + deviceToken  + Constants.VIDEO_NORMAL_DIR
				+ Constants.DIR_STR;
		try {
			File path = new File(dirPath);
			fileList = path.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.matches("^N\\d{8}_\\d{6}.mp4$");
				}
			});
		} catch (SecurityException | NullPointerException e) {
			e.printStackTrace();
		}
		return fileList;
	}

	public static File[] getEventVideoLocalFileList(String deviceToken) {
		File fileList[] = null;

		String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.DEVICE_DIR
				+ Constants.DIR_STR + deviceToken  + Constants.VIDEO_EVENT_DIR
				+ Constants.DIR_STR;
		try {
			File path = new File(dirPath);
			fileList = path.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
                    return name.matches("^E\\d{8}_\\d{6}.mp4$");
				}
			});
		} catch (SecurityException | NullPointerException e) {
			e.printStackTrace();
		}
		return fileList;
	}

	public static int getNormalVideoLocalFilesCount(String deviceToken) {
		int count = 0;

		String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.DEVICE_DIR
				+ Constants.DIR_STR + deviceToken  + Constants.VIDEO_NORMAL_DIR
				+ Constants.DIR_STR;
		try {
			File path = new File(dirPath);
			count = path.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
                    return name.matches("^N\\d{8}_\\d{6}.mp4$");
				}
			}).length;
		} catch (SecurityException | NullPointerException e) {
			e.printStackTrace();
		}
		return count;
	}

	public static int getEventVideoLocalFilesCount(String deviceToken) {
		int count = 0;

		String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.DEVICE_DIR
				+ Constants.DIR_STR + deviceToken  + Constants.VIDEO_EVENT_DIR
				+ Constants.DIR_STR;
		try {
			File path = new File(dirPath);
			count = path.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
                    return name.matches("^E\\d{8}_\\d{6}.mp4$");
				}
			}).length;
		} catch (SecurityException | NullPointerException e) {
			e.printStackTrace();
		}
		return count;
	}

	public static int getAllVideoLocalFilesCount(String deviceToken) {
		return FileUtil.getNormalVideoLocalFilesCount(deviceToken)
				+ FileUtil.getEventVideoLocalFilesCount(deviceToken);
	}

	public static boolean isExistDownloadedFirmwareFile (String filename, long size) {
		boolean ret = false;
		String filepath = FileUtil.getFirmwareFileStorePath(filename);
		File file = new File(filepath);

		if (file.exists() && file.length() == size) {
			ret = true;
		}
		return ret;
	}

	public static String getFirmwareFileStorePath (String filename) {
		return Environment.getExternalStorageDirectory().getPath() + Constants.FW_DIR + Constants.DIR_STR + filename;
	}

	public static long getExternalStorageFreeSpace () {
		return Environment.getExternalStorageDirectory().getFreeSpace();
	}
}
