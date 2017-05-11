package com.jun.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FindDifferent extends ProgressUtil{

	@Override
	public void initIquantityDate(List<Map<String, String>> xlsAryList) throws Exception {
		// 设置进度总数
		setAllCount(xlsAryList.size());
		try {
			conn = getConnection();
			Map<String, String> map = null;
			String area = "";
			boolean flag = true;
			for (int i = 0; i < xlsAryList.size(); i++) {
				map = (Map<String, String>) xlsAryList.get(i);
				// 错误行号
				errorRowNum = getStrMapValue(map, "RN");
				if(!"".equals(getStrMapValue(map, "A"))){
					area = getStrMapValue(map, "A");
				}
				// 获取栋
				String dong = getStrMapValue(map, "C");
				// 获取库
				String ku = getStrMapValue(map, "D");
				if("".equals(dong.trim())){
					flag = false;
					logMessage("第"+errorRowNum+"栋号为空！");
					continue;
				}
				String[] dongArr = dong.split("—");
				String buiding = String.format("%03d", Integer.parseInt(dongArr[1]));
				String[] kuArr = ku.split("、");
				for(String kuStr : kuArr){
					String store = String.format("%04d", Integer.parseInt(kuStr));
					String code = area + buiding + store;
					List<String> codeList = queryStore(store);
					if(codeList.size() == 0){
						flag = false;
						logMessage("查无资料:" + code);
					}else if(codeList.size() > 1){
						for(String str : codeList){
							flag = false;
							logMessage("资料重复:" + str);
						}
					}else if(codeList.size() == 1){
						if(!code.equals(codeList.get(0))){
							flag = false;
							logMessage("调度室：" + code + "和数据库:" + codeList.get(0));
						}
					}
				}
			}
			if (!flag) {
				throw new Exception("比对有误,请查看错误信息:");
			}
		} catch (Exception e) {
			System.out.println(errorRowNum);
			throw e;
		} finally {
			close(conn);
		}
	}
	
	private String getStrMapValue(Map<String, String> map, String key) {
		return map != null && map.get(key) != null ? map.get(key).toString().trim() : "";
	}
	
	private List<String> queryStore(String stroeCode) throws SQLException {
		String sql = "select code from mtws_pubdoc where dr=0 and name='"+stroeCode+"库'";
		Statement Stmt = conn.createStatement();
		ResultSet rs = Stmt.executeQuery(sql);
		List<String> codeList = new ArrayList<String>();
		while (rs.next()) {
			codeList.add(rs.getString("code"));
		}
		Stmt.close();
		rs.close();
		return codeList;
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
