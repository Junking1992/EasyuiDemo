package com.jun.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class OpeningByXlsTubGrade extends ProgressUtil {

	@Override
	public void initIquantityDate(List<Map<String, String>> xlsAryList) throws Exception {
		// 设置进度总数
		setAllCount(xlsAryList.size());
		try {
			//立flag!
			boolean flag = true;
			//获取数据库连接
			conn = getConnection();
			//开启事务
			conn.setAutoCommit(false);
			//循环Excel每行数据
			for (int i = 0; i < xlsAryList.size(); i++) {
				Map map = (Map) xlsAryList.get(i);
				//错误行号
				errorRowNum = getStrMapValue(map, "RN");
				//获取该行库号
				String storeCode = getStrMapValue(map, "A");
				//获取桶号
				String tubCode = getStrMapValue(map, "B");
				//获取等级
				String grade = getStrMapValue(map, "C");
				//检查库号是否为纯数字
				if(!storeCode.matches("[0-9]+")){
					flag = false;
					logMessage("Excel第" + errorRowNum + "行错误： 库号不能有汉字!");
				}
				//检查桶号是否为纯数字
				if(!tubCode.matches("[0-9]+")){
					flag = false;
					logMessage("Excel第" + errorRowNum + "行错误： 桶号不能有汉字!");
				}
			}
			//如果flag为true就提交到数据库，否则回滚
			if (flag) {
				conn.rollback();
			} else {
				conn.rollback();
				throw new Exception("上传错误,请查看错误信息:");
			}
		} catch (Exception e) {
			System.out.println(errorRowNum);
			if (conn != null) {
				conn.rollback();
			}
			throw e;
		} finally {
			close(conn);
		}
	}
	
	private String getStrMapValue(Map<String, String> map, String key) {
		return map != null && map.get(key) != null ? map.get(key).toString().trim() : "";
	}
	
	private String getGrade(String grade) {
		String newGrade = "";
		if (grade.indexOf("特") > -1) {
			newGrade = "0";
		} else if (grade.indexOf("一") > -1) {
			newGrade = "1";
		} else if (grade.indexOf("二") > -1) {
			newGrade = "2";
		} else if (grade.indexOf("三") > -1) {
			newGrade = "3";
		} else if (grade.indexOf("四") > -1) {
			newGrade = "4";
		} else if (grade.indexOf("禁") > -1) {
			newGrade = "8";
		} else if (grade.indexOf("未") > -1) {
			newGrade = "9";
		} else{
			return "";
		}

		if (grade.indexOf("-") > 0) {
			newGrade += "-";
		}
		return newGrade;
	}

	private String driver = "oracle.jdbc.driver.OracleDriver";
	private String url = "jdbc:oracle:thin:@10.0.5.152:1521/jknc";
	private String user = "jknc02";
	private String password = "jknc02";
	private Connection conn;

	public Connection getConnection() throws SQLException, ClassNotFoundException {
		if (conn == null) {
			Class.forName(driver);
			return DriverManager.getConnection(url, user, password);
		} else {
			return conn;
		}
	}

	public void close(Connection conn) throws SQLException {
		if (conn != null) {
			conn.close();
		}
	}

}
