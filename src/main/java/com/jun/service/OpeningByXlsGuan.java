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

public class OpeningByXlsGuan extends ProgressUtil {
	Map<String, String> pubdocMap = new HashMap<String, String>();
	String areaNo = "";// 片区号
	String buildingNo = "";// 栋号
	String storeNo = "";// 库号
	String jarName = "";// 罐名

	public void initIquantityDate(List<Map<String, String>> xlsAryList) throws Exception {
		// 设置进度总数
		setAllCount(xlsAryList.size());
		try {
			boolean flaf = true;
			conn = getConnection();
			conn.setAutoCommit(false);
			
			// 取得片区栋库编码
			pubdocMap = initPubDocMap();
			
			for (int i = 0; i < xlsAryList.size(); i++) {
				Map<String, String> map = (Map<String, String>) xlsAryList.get(i);
				errorRowNum = (String) map.get("RN");
				try {
					//片
					String pk_area = getPk_area(getStrMapValue(map, "A"));
					//栋
					String pk_building = getPk_building(getStrMapValue(map, "B"));
					//库
					String pk_store = getPk_store(getStrMapValue(map, "C"));
					//灌号
					Map<String,String> jarMap = getPK_jar(getStrMapValue(map, "D"),pk_store);
					String pk_jar = jarMap.get("pk_jar");
					String code = jarMap.get("code");
					if("".equals(pk_jar)){
						flaf = false;
						logMessage("Excel第"+errorRowNum+"行, 灌号有误！");
					}
					//数量
					BigDecimal qty = getDecMapValue(map, "M");
					if(qty.compareTo(new BigDecimal("0")) <= 0){
						flaf = false;
						logMessage("Excel第"+errorRowNum+"行, 数量有误！");
					}
					//备注
					String memo = getStrMapValue(map, "Q");
					
					//入库时间
					String inDate = getStrMapValue(map, "F");
					
					//等级
					String grade = getGrade(getStrMapValue(map, "K"));
					
					String insertSql = "insert into mtws_iquantity (pk_iquantity,pk_area,pk_building,pk_store," + "tubcode,pk_jar,iquertity,pk_measure,def7,def10,def14,def15,def17," + "def20,ts,dr)" + 
							" values ('" + pk_jar + "','" + pk_area + "','" + pk_building + "','" + pk_store + "'," + "'LG" + code + "','" + pk_jar + "'," + qty + ",'1001A41000000000034A','" + memo + "','" + getWineCode(getStrMapValue(map, "H")) + "'," + qty + ",'" + getType(getStrMapValue(map, "H")) + "','" + inDate + "','" + grade + "',to_char(sysdate,'yyyy-mm-dd hh24:mi:ss')," + "'0')";
					create(insertSql);
				} catch (Exception e) {
					flaf = false;
					logMessage(e.getMessage());
				}
			}
			if (flaf) {
				conn.commit();
			} else {
				conn.rollback();
				throw new Exception("上传错误,请查看错误信息:");
			}
		} catch (Exception e) {
			conn.rollback();
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
			throw new Exception("第"+errorRowNum+"行Excel的片区[" + area + "]未找到对应的片区!");
		if (pk_area.equals(""))
			throw new Exception("第"+errorRowNum+"行Excel的片区[" + area + "]未找到对应的片区编码!");
		return pk_area;
	}

	private String getPk_building(String building) throws Exception {
		if ("".equals(building.trim())){
			throw new Exception("第"+errorRowNum+"行Excel的栋号不能为空！");
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
					throw new Exception("第"+errorRowNum+"行Excel的栋号[" + building + "]未找到对应的栋号编码!");
				}
			}else{
				throw new Exception("第"+errorRowNum+"行Excel的栋号[" + building + "]不能识别!");
			}
		}else{
			if(buildingNow.length() != 3){
				throw new Exception("第"+errorRowNum+"行Excel的栋号[" + building + "]未找到对应的栋号编码!");
			}
			buildingNo = areaNo + buildingNow;
			if(!"".equals(getStrMapValue(pubdocMap, buildingNo))){
				return getStrMapValue(pubdocMap, buildingNo);
			}else{
				throw new Exception("第"+errorRowNum+"行Excel的栋号[" + building + "]未找到对应的栋号编码!");
			}
		}
	}

	private String getPk_store(String store) throws Exception {
		if ("".equals(store)){
			throw new Exception("第"+errorRowNum+"行Excel的库号不能为空！");
		}
		if("地下室".equals(store.trim())){
			//地下室由三码栋号加A组成
			storeNo = buildingNo + buildingNo.substring(2) + "A";
			if(!"".equals(getStrMapValue(pubdocMap, storeNo))){
				return getStrMapValue(pubdocMap, storeNo);
			}else{
				throw new Exception("第"+errorRowNum+"行Excel的库号[" + store + "]未找到对应的库号编码!");
			}
		}else if (buildingNo.indexOf("E") > -1){
			if(store.length() > 1){
				throw new Exception("第"+errorRowNum+"行Excel的库号[" + store + "]未找到对应的库号编码!");
			}
			storeNo = buildingNo + buildingNo.substring(2) + store;
			if(!"".equals(getStrMapValue(pubdocMap, storeNo))){
				return getStrMapValue(pubdocMap, storeNo);
			}else{
				throw new Exception("第"+errorRowNum+"行Excel的库号[" + store + "]未找到对应的库号编码!");
			}
		}else{
			return "";
		}
	}
	
	private Map<String, String> getPK_jar(String name, String pk_store) throws Exception {
		if ("".equals(name)){
			throw new Exception("第"+errorRowNum+"行Excel的罐号不能为空！");
		}
		int index;
		if((index=name.indexOf("吨")) > -1){
			name = name.substring(index+1);
		}
		name = name.replaceAll("#", "");
		String sql = "select pk_jar,code from mtws_jar where name='" + name + "' and pk_store='"+ pk_store +"' and dr=0";
		Statement Stmt = conn.createStatement();
		ResultSet rs = Stmt.executeQuery(sql);
		Map<String, String> map = new HashMap<String,String>();
		while (rs.next()) {
			map.put("pk_jar", rs.getString("pk_jar"));
			map.put("code", rs.getString("code"));
		}
		Stmt.close();
		rs.close();
		return map;
	}
	
	private String getWineCode(String strMapValue) {
		// TODO 自动生成的方法存根
		String productName = "";
		if (strMapValue.indexOf("新酒") > -1) {
			productName = "10";
		} else if (strMapValue.indexOf("盘勾酒") > -1) {
			productName = "20";
		} else if (strMapValue.indexOf("回收酒") > -1) {
			productName = "02";
		} else if (strMapValue.indexOf("其他酒") > -1) {
			productName = "03";
		} else if (strMapValue.indexOf("勾兑酒") > -1) {
			productName = "30";
		} else if (strMapValue.indexOf("坛底") > -1 || strMapValue.equals("")) {
			productName = "01";
		}
		return productName;
	}
	
	private String getType(String strMapValue) {
		// TODO 自动生成的方法存根
		String productName = "";
		if (strMapValue.indexOf("新酒") > -1) {
			productName = "NW";
		} else if (strMapValue.indexOf("盘勾酒") > -1) {
			productName = "PG";
		} else if (strMapValue.indexOf("回收酒") > -1) {
			productName = "RP";
		} else if (strMapValue.indexOf("其他酒") > -1) {
			productName = "OW";
		} else if (strMapValue.indexOf("勾兑酒") > -1) {
			productName = "GD";
		} else if (strMapValue.indexOf("坛底") > -1 || strMapValue.equals("")) {
			productName = "TD";
		}
		return productName;
	}
	
	private String getGrade(String gradeStr) {
		String grade = "";
		if (gradeStr.indexOf("特级") > -1) {
			grade = "0";
		} else if (gradeStr.indexOf("一级") > -1) {
			grade = "1";
		} else if (gradeStr.indexOf("二级") > -1) {
			grade = "2";
		} else if (gradeStr.indexOf("三级") > -1) {
			grade = "3";
		} else if (gradeStr.indexOf("四级") > -1) {
			grade = "4";
		} else if (gradeStr.indexOf("未定级") > -1) {
			grade = "9";
		}
		return grade;
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
