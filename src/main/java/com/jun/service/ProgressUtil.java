package com.jun.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public abstract class ProgressUtil {

	// 总进度
	public int allCount;
	// 当进度
	public int currentCount;
	// 开始时间
	public long startTime;
	// 错误行号
	public String errorRowNum = "";
	// 错误信息log文件路径名称
	public String fileName;
	// 错误信息文件
	public File logFile;
	// 错误信息
	public StringBuffer logMsg = new StringBuffer("");

	public ProgressUtil() {
		this.allCount = 0;
		this.currentCount = 0;
		this.startTime = System.currentTimeMillis();
	}

	public abstract void initIquantityDate(List<Map<String, String>> xlsAryList) throws Exception;

	public String getProgress() {
		if(allCount == 0){
			return "0";
		}
		return new BigDecimal(currentCount * 100).divide(new BigDecimal(allCount), 0, BigDecimal.ROUND_DOWN).toString();
	}

	public synchronized void addCount() {
		this.currentCount++;
	}

	public String getTime() {
		return (System.currentTimeMillis() - startTime) / 1000 + "秒";
	}

	public int getAllCount() {
		return allCount;
	}

	public void setAllCount(int allCount) {
		this.allCount = allCount;
	}

	public int getCurrentCount() {
		return currentCount;
	}

	public void setCurrentCount(int currentCount) {
		this.currentCount = currentCount;
	}

	public void initFile() throws IOException {
		if (logFile == null) {
			logFile = new File("E:/UploadExcel/Log/" + fileName.substring(0, fileName.indexOf('.')) + ".txt");
		}
		// 判断文件是否存在
		if (!logFile.exists()) {
			// 判断目标文件所在的目录是否存在
			if (!logFile.getParentFile().exists()) {
				// 创建文件所在目录
				logFile.getParentFile().mkdirs();
			}
			// 创建文件
			logFile.createNewFile();
		} else {
			FileWriter fileWriter = new FileWriter(logFile, true);
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
			fileWriter.write("**************************************************" + df.format(new Date()) + "**************************************************\r\n");
			fileWriter.close();
			//清空信息
			logMsg.delete(0, logMsg.length());
		}
	}

	public void logMessage(String msg) throws IOException {
		if (logFile == null) {
			initFile();
		}
		FileWriter fileWriter = new FileWriter(logFile, true);
		fileWriter.write(msg + "\r\n");
		fileWriter.close();
		logMsg.append( msg + "<br/>");
	}

}
