package com.jun.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpeningGuanDataByXls extends ProgressUtil {
	Map<String, String> pubdocMap = new HashMap<String, String>();
	String areaNo = "";// 片区号
	String buildingNo = "";// 栋号
	String storeNo = "";// 库号
	String rowNum = "";// 错误行号

	public void initIquantityDate(List xlsAryList) throws Exception {
		// 设置进度总数
		setAllCount(xlsAryList.size());
		try {
			conn = getConnection();
			conn.setAutoCommit(false);
			// 取得所有片栋库编码
			pubdocMap = initPubDocMap();
			//删除所有大罐（大罐标识def5=LG）
			update("delete from mtws_jar where def5='LG'");
			for (int i = 0; i < xlsAryList.size(); i++) {
				Map map = (Map) xlsAryList.get(i);
				rowNum = (String) map.get("RN");
				String areaNo_pk = getPk_area(getStrMapValue(map, "A"));
				String buildingNo_pk = getPk_building(getStrMapValue(map, "B"));
				String storeNo_pk = getPk_store(getStrMapValue(map, "C"));
				String jarCode = getJarCode(getStrMapValue(map, "D"));
				String jarName = Integer.parseInt(jarCode.substring(9)) + "号罐";
				String capacity = getDecMapValue(map, "E").toString();
				String insertSql = "insert into mtws_jar (pk_jar,code,name,jarcubage,jarweigth,def2,def3,pk_measure," + "pk_store,isseal,creator,modifier,creationtime,modifiedtime,pk_org,pk_group,def1,isstandard," + "islock,isok,ts,def5,dr) values('1001A41'||'" + jarCode + "','" + jarCode + "','" + jarName + "'," + capacity + "," + "100," + capacity + "," + capacity + ",'1001A41000000000034A','" + storeNo_pk + "','N','1001A4100000000000OU'," + "'1001A4100000000000OU',to_char(sysdate,'yyyy-mm-dd hh24:mi:ss'),to_char(sysdate,'yyyy-mm-dd hh24:mi:ss')," + "'0001A410000000000954','0001A5100000000001KL','已启用','N','N','N',to_char(sysdate,'yyyy-mm-dd hh24:mi:ss'),'LG',0)";
				create(insertSql);
				addCount();
			}
			conn.commit();
		} catch (Exception e) {
			conn.rollback();
			System.out.println("erro at " + rowNum);
			throw e;
		} finally {
			close(conn);
		}
	}

	public ResultSet query(String sql) throws SQLException {
		// PreparedStatement:是预编译的,对于批量处理可以大大提高效率.也叫JDBC存储过程
		// Statement:在对数据库只执行一次性存取的时侯，用 Statement对象进行处理。
		Statement Stmt = conn.createStatement();
		// 返回新增或更新数据量
		ResultSet rst = Stmt.executeQuery(sql);
		Stmt.close();
		return rst;
	}

	public int create(String sql) throws SQLException {
		// PreparedStatement:是预编译的,对于批量处理可以大大提高效率.也叫JDBC存储过程
		// Statement:在对数据库只执行一次性存取的时侯，用 Statement对象进行处理。
		Statement Stmt = conn.createStatement();
		// 返回新增或更新数据量
		int i = Stmt.executeUpdate(sql);
		Stmt.close();
		return i;
	}

	public int update(String sql) throws SQLException {
		Statement Stmt = conn.createStatement();
		// 返回新增或更新数据量
		int i = Stmt.executeUpdate(sql);
		Stmt.close();
		return i;
	}

	public int delete(String pk_jar) throws SQLException {
		Statement Stmt = conn.createStatement();
		// 返回新增或更新数据量
		int i = Stmt.executeUpdate("delete from mtws_iquantity where pk_jar='" + pk_jar + "' ");
		Stmt.close();
		return i;
	}

	private String getStrMapValue(Map map, String key) {
		return map != null && map.get(key) != null ? map.get(key).toString().trim() : "";
	}

	private BigDecimal getDecMapValue(Map map, String key) {
		return new BigDecimal(map != null && map.get(key) != null && !map.get(key).toString().trim().equals("") ? map.get(key).toString().trim() : "0");
	}

	private Map<String, String> initPubDocMap() throws SQLException {
		String sql = "select code,pk_pubdoc from mtws_pubdoc where dr=0";
		Statement Stmt = conn.createStatement();
		ResultSet rs = Stmt.executeQuery(sql);
		Map<String, String> rstMap = new HashMap<String, String>();
		while (rs.next()) {
			rstMap.put(rs.getString("code"), rs.getString("pk_pubdoc"));
		}
		Stmt.close();
		rs.close();
		return rstMap;
	}

	private String getPk_area(String area) throws Exception {
		String pk_area = "";
		if (area.indexOf("1") > -1) {
			areaNo = "01";
			pk_area = getStrMapValue(pubdocMap, "01");
		} else if (area.indexOf("2") > -1) {
			areaNo = "02";
			pk_area = getStrMapValue(pubdocMap, "02");
		} else if (area.indexOf("3") > -1) {
			areaNo = "03";
			pk_area = getStrMapValue(pubdocMap, "03");
		} else if (area.indexOf("4") > -1) {
			areaNo = "04";
			pk_area = getStrMapValue(pubdocMap, "04");
		} else if (area.indexOf("5") > -1) {
			areaNo = "05";
			pk_area = getStrMapValue(pubdocMap, "05");
		} else if (area.indexOf("6") > -1) {
			areaNo = "06";
			pk_area = getStrMapValue(pubdocMap, "06");
		} else if (area.indexOf("7") > -1 || area.indexOf("中华") > -1) {
			areaNo = "07";
			pk_area = getStrMapValue(pubdocMap, "07");
		} else if (area.indexOf("勾兑二部") > -1){
			areaNo = "99";
			pk_area = getStrMapValue(pubdocMap, "99");
		}
		if (areaNo.equals(""))
			throw new Exception("Excel的片区[" + area + "]未找到对应的片区!");
		if (pk_area.equals(""))
			throw new Exception("Excel的片区[" + area + "]未找到对应的片区编码!");
		return pk_area;
	}

	private String getPk_building(String building) throws Exception {
		if ("".equals(building.trim())){
			throw new Exception("第"+rowNum+"行Excel的栋号不能为空！");
		}
		String buildingNow  = building.replace("_", "-");
		buildingNow = building.replace("~", "-");
		if(buildingNow.indexOf("-") > -1){
			String second = building.split("-")[1];
			if(second != null && !"".equals(second.trim()) && second.length()<4){
				buildingNo = areaNo + String.format("%03d", Integer.parseInt(second.trim()));
				if(!"".equals(getStrMapValue(pubdocMap, buildingNo))){
					return getStrMapValue(pubdocMap, buildingNo);
				}else{
					throw new Exception("第"+rowNum+"行Excel的栋号[" + building + "]未找到对应的栋号编码!");
				}
			}else{
				throw new Exception("第"+rowNum+"行Excel的栋号[" + building + "]不能识别!");
			}
		}else{
			if(buildingNow.length() > 3){
				throw new Exception("第"+rowNum+"行Excel的栋号[" + building + "]未找到对应的栋号编码!");
			}
			buildingNo = areaNo + String.format("%03d", Integer.parseInt(buildingNow.trim()));
			if(!"".equals(getStrMapValue(pubdocMap, buildingNo))){
				return getStrMapValue(pubdocMap, buildingNo);
			}else{
				throw new Exception("第"+rowNum+"行Excel的栋号[" + building + "]未找到对应的栋号编码!");
			}
		}
	}

	private String getPk_store(String store) throws Exception {
		if ("".equals(store.trim())){
			throw new Exception("第"+rowNum+"行Excel的库号不能为空！");
		}
		if("地下室".equals(store.trim())){
			storeNo = buildingNo + "0000";
			if(!"".equals(getStrMapValue(pubdocMap, storeNo))){
				return getStrMapValue(pubdocMap, storeNo);
			}else{
				throw new Exception("第"+rowNum+"行Excel的库号[" + store + "]未找到对应的库号编码!");
			}
		}else{
			if(store.length() > 4){
				throw new Exception("第"+rowNum+"行Excel的库号[" + store + "]未找到对应的库号编码!");
			}
			storeNo = buildingNo + String.format("%04d", Integer.parseInt(store.trim()));
			if(!"".equals(getStrMapValue(pubdocMap, storeNo))){
				return getStrMapValue(pubdocMap, storeNo);
			}else{
				throw new Exception("第"+rowNum+"行Excel的库号[" + store + "]未找到对应的库号编码!");
			}
		}
	}
	
	private String getJarCode(String jarStr) throws Exception {
		if ("".equals(jarStr.trim())){
			throw new Exception("第"+rowNum+"行Excel的灌号不能为空！");
		}
		String jarStrNew = jarStr.replaceAll("--", "-");
		jarStrNew = jarStr.replaceAll("~", "-");
		if(jarStrNew.indexOf("-") > -1){
			String second = jarStrNew.split("-")[1];
			if(second != null && !"".equals(second)){
				return storeNo + String.format("%04d", Integer.parseInt(second.trim()));
			}else{
				throw new Exception("第"+rowNum+"行Excel的灌号["+jarStr+"]格式有误！");
			}
		}else{
			return storeNo + String.format("%04d", Integer.parseInt(jarStr.trim()));
		}
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
