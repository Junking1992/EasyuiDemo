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

public class OpeningJarDataByXls extends ProgressUtil {
	Map<String, String> pubdocMap = new HashMap<String, String>();
	Map<String, String> storeMap = new HashMap<String, String>();
	String areaNo = "";
	String buildingNo = "";
	String storeNo = "";
	String rowNum = "";
	Map<String, String> jarNumByStoreMap = new HashMap<String, String>();
	Map<String, String> jarCubageByStoreMap = new HashMap<String, String>();

	public void initIquantityDate(List xlsAryList) throws Exception {
		// 设置进度总数
		setAllCount(xlsAryList.size());
		try {
			boolean flag = true;
			conn = getConnection();
			conn.setAutoCommit(false);

			// 取得片区栋库编码
			pubdocMap = initPubDocMap();
			// 取得库号CODE
			storeMap = initStoreMap();
			Map<String, String> areaMap = new HashMap<String, String>();
			// 有坛号的酒记录
			// Map jarMap = new HashMap();
			// 取得片区
			if (xlsAryList != null && xlsAryList.size() > 0) {
				getPk_area(getStrMapValue((Map) xlsAryList.get(0), "A"));
			}
			// 取得各库的总坛数
			// Map jarNumByStoreMap = initJarNumByStoreMap();

			for (int i = 0; i < xlsAryList.size(); i++) {
				Map map = (Map) xlsAryList.get(i);
				rowNum = (String) map.get("RN");
				areaNo = "";
				buildingNo = "";
				storeNo = "";
				Map<String, String> abstoreMape = getABStore(getStrMapValue(map, "C"));
				String pk_area = getStrMapValue(abstoreMape, areaNo);
				if (getStrMapValue(areaMap, areaNo).equals("")) {
					areaMap.put(areaNo, areaNo);
					initJarNumByStoreMap();
					initJarCubageByStoreMap();
				}
				String pk_store = getStrMapValue(abstoreMape, storeNo);
				String pk_building = getStrMapValue(abstoreMape, buildingNo);
				BigDecimal jarNumSum = getDecMapValue(map, "D");
				String strE = getStrMapValue(map, "E");
				String strF = getStrMapValue(map, "F");
				String strG = getStrMapValue(map, "G");
				List<String> jarAry250kg = getJarList(strE);
				List<String> jarAry350kg = getJarList(strF);
				List<String> jarAry500kg = getJarList(strG);				
				BigDecimal jarNum250kg = new BigDecimal(jarAry250kg.size());
				BigDecimal jarNum350kg = new BigDecimal(jarAry350kg.size());
				BigDecimal jarNum500kg = new BigDecimal(jarAry500kg.size());

				// 主逻辑 先干掉整个片区资料
				String sql = "update mtws_jar set dr=2 where dr=0 and code like '" + areaNo + buildingNo + storeNo + "%'";
				update(sql);

				if (jarNum250kg.compareTo(new BigDecimal("0")) == 0 && jarNum350kg.compareTo(new BigDecimal("0")) == 0 && jarNum500kg.compareTo(new BigDecimal("0")) == 0) {
					flag = false;
					logMessage("Excel档中的酒库[" + areaNo + buildingNo + storeNo + "]中的250kg坛数,350kg坛数,500kg坛数不可都为0!");
					//throw new Exception("Excel档中的酒库[" + areaNo + buildingNo + storeNo + "]中的250kg坛数,350kg坛数,500kg坛数不可都为0!");
				}
				if (jarNumSum.compareTo(jarNum250kg.add(jarNum350kg).add(jarNum500kg)) != 0) {
					int hasCount = 0;
					if((strE+","+strF+","+strG).indexOf("有")>-1){						
						if(strE.trim().equals("有")){
							hasCount++;
							for(int s=1; s<jarNumSum.intValue();s++){
								String jarNo = areaNo + buildingNo + storeNo + "0000".substring(0, 4 - String.valueOf(s).length()) + s;
								if(!jarAry350kg.contains(jarNo)&&!jarAry500kg.contains(jarNo)){
									jarAry250kg.add(jarNo);
								}
							}
						}
						if(strF.trim().equals("有")){
							if(hasCount==1){
								logMessage("Excel档中的酒库[" + areaNo + buildingNo + storeNo + "]350KG出现第二个'有'标识!");
							}
							for(int s=1; s<jarNumSum.intValue();s++){
								String jarNo = areaNo + buildingNo + storeNo + "0000".substring(0, 4 - String.valueOf(s).length()) + s;
								if(!jarAry250kg.contains(jarNo)&&!jarAry500kg.contains(jarNo)){
									jarAry350kg.add(jarNo);
								}
							}
							hasCount++;
						}
						if(strG.trim().equals("有")){
							if(hasCount==1){
								logMessage("Excel档中的酒库[" + areaNo + buildingNo + storeNo + "]500KG出现第二个'有'标识!");
							}
							for(int s=1; s<jarNumSum.intValue();s++){
								String jarNo = areaNo + buildingNo + storeNo + "0000".substring(0, 4 - String.valueOf(s).length()) + s;
								if(!jarAry250kg.contains(jarNo)&&!jarAry350kg.contains(jarNo)){
									jarAry500kg.add(jarNo);
								}
							}
							hasCount++;
						}
					}else{						
						flag = false;
						logMessage("Excel档中的酒库[" + areaNo + buildingNo + storeNo + "]总坛数[" + jarNumSum + "]与250kg坛数[" + jarNum250kg + "]或350kg坛数[" + jarNum350kg + "]或500kg坛数[" + jarNum500kg + "]坛数总数不一致!");
						//throw new Exception("Excel档中的酒库[" + areaNo + buildingNo + storeNo + "]总坛数[" + jarNumSum + "]与250kg坛数[" + jarNum250kg + "]或350kg坛数[" + jarNum350kg + "]或500kg坛数[" + jarNum500kg + "]坛数总数不一致!");
					}
				}
				//交叉重复检查
				for(int c=0; c<jarAry250kg.size(); c++){
					if(jarAry350kg.contains(jarAry250kg.get(c))||jarAry500kg.contains(jarAry250kg.get(c))){
						logMessage("Excel档中的酒库[" + areaNo + buildingNo + storeNo + "]坛号[" + jarAry250kg.get(c) + "]在350KG或500KG有重复!");
					}
				}
				for(int c=0; c<jarAry350kg.size(); c++){
					if(jarAry500kg.contains(jarAry350kg.get(c))){
						logMessage("Excel档中的酒库[" + areaNo + buildingNo + storeNo + "]坛号[" + jarAry350kg.get(c) + "]在500KG有重复!");
					}
				}

				BigDecimal storejarNum = new BigDecimal("0");
				String jarType = "";

				if (jarNum250kg.compareTo(new BigDecimal("0")) > 0) {
					storejarNum = jarNum250kg;
					jarType = "250";

					String jarcubage = getJarcubage(areaNo, jarType);
					for (int j = 0; j < jarAry250kg.size(); j++) {
						//String jarNo = areaNo + buildingNo + storeNo + "0000".substring(0, 4 - String.valueOf(seq).length()) + seq;
						String jarNo = jarAry250kg.get(j).toString();
						String updateSql = " update mtws_jar set dr=0,pk_store='" + pk_store + "',jarcubage=" + jarcubage + ",jarweigth=40,def2='" + jarcubage + "',def3='" + jarType + "' where code = '" + jarNo + "' ";
						int num = update(updateSql);
						String pk_jar = pk_store.substring(0, 7) + jarNo;
						if (num <= 0) {
							String insertSql = "insert into mtws_jar (pk_jar,code,name,jarcubage,jarweigth,def2,def3,pk_measure," + "pk_store,isseal,creator,modifier,creationtime,modifiedtime,pk_org,pk_group,def1,isstandard," + "islock,isok,ts,dr) values('" + pk_jar + "','" + jarNo + "','" + jarNo + "'," + jarcubage + "," + "40," + jarcubage + "," + jarType + ",'1001A41000000000034A','" + pk_store + "','N','1001A4100000000000OU'," + "'1001A4100000000000OU',to_char(sysdate,'yyyy-mm-dd hh24:mi:ss'),to_char(sysdate,'yyyy-mm-dd hh24:mi:ss')," + "'0001A410000000000954','0001A5100000000001KL','已启用','N','N','N',to_char(sysdate,'yyyy-mm-dd hh24:mi:ss'),0)";
							create(insertSql);
						}
					}
				}

				if (jarNum350kg.compareTo(new BigDecimal("0")) > 0) {
					storejarNum = jarNum350kg;
					jarType = "350";

					String jarcubage = getJarcubage(areaNo, jarType);
					for (int j = 0; j < jarAry350kg.size(); j++) {
						//String jarNo = areaNo + buildingNo + storeNo + "0000".substring(0, 4 - String.valueOf(seq).length()) + seq;
						String jarNo = jarAry350kg.get(j).toString();
						String updateSql = " update mtws_jar set dr=0,pk_store='" + pk_store + "',jarcubage=" + jarcubage + ",jarweigth=40,def2='" + jarcubage + "',def3='" + jarType + "' where code = '" + jarNo + "' ";
						int num = update(updateSql);
						String pk_jar = pk_store.substring(0, 7) + jarNo;
						if (num <= 0) {
							String insertSql = "insert into mtws_jar (pk_jar,code,name,jarcubage,jarweigth,def2,def3,pk_measure," + "pk_store,isseal,creator,modifier,creationtime,modifiedtime,pk_org,pk_group,def1,isstandard," + "islock,isok,ts,dr) values('" + pk_jar + "','" + jarNo + "','" + jarNo + "'," + jarcubage + "," + "40," + jarcubage + "," + jarType + ",'1001A41000000000034A','" + pk_store + "','N','1001A4100000000000OU'," + "'1001A4100000000000OU',to_char(sysdate,'yyyy-mm-dd hh24:mi:ss'),to_char(sysdate,'yyyy-mm-dd hh24:mi:ss')," + "'0001A410000000000954','0001A5100000000001KL','已启用','N','N','N',to_char(sysdate,'yyyy-mm-dd hh24:mi:ss'),0)";
							create(insertSql);
						}
					}
				}

				if (jarNum500kg.compareTo(new BigDecimal("0")) > 0) {
					storejarNum = jarNum500kg;
					jarType = "500";

					String jarcubage = getJarcubage(areaNo, jarType);
					for (int j = 0; j < jarAry500kg.size(); j++) {
						//String jarNo = areaNo + buildingNo + storeNo + "0000".substring(0, 4 - String.valueOf(seq).length()) + seq;
						String jarNo = jarAry500kg.get(j).toString();
						String updateSql = " update mtws_jar set dr=0,pk_store='" + pk_store + "',jarcubage=" + jarcubage + ",jarweigth=40,def2='" + jarcubage + "',def3='" + jarType + "' where code = '" + jarNo + "' ";
						int num = update(updateSql);
						String pk_jar = pk_store.substring(0, 7) + jarNo;
						if (num <= 0) {
							String insertSql = "insert into mtws_jar (pk_jar,code,name,jarcubage,jarweigth,def2,def3,pk_measure," + "pk_store,isseal,creator,modifier,creationtime,modifiedtime,pk_org,pk_group,def1,isstandard," + "islock,isok,ts,dr) values('" + pk_jar + "','" + jarNo + "','" + jarNo + "'," + jarcubage + "," + "40," + jarcubage + "," + jarType + ",'1001A41000000000034A','" + pk_store + "','N','1001A4100000000000OU'," + "'1001A4100000000000OU',to_char(sysdate,'yyyy-mm-dd hh24:mi:ss'),to_char(sysdate,'yyyy-mm-dd hh24:mi:ss')," + "'0001A410000000000954','0001A5100000000001KL','已启用','N','N','N',to_char(sysdate,'yyyy-mm-dd hh24:mi:ss'),0)";
							create(insertSql);
						}
					}
				}

				if (storejarNum.compareTo(new BigDecimal("0")) == 0) {
					flag = false;
					logMessage("取得Excel档中的酒库[" + areaNo + buildingNo + storeNo + "]的酒坛个数失败!");
					//throw new Exception("取得Excel档中的酒库[" + areaNo + buildingNo + storeNo + "]的酒坛个数失败!");
				}

				// 进度增长
				addCount();
			}
			if(flag){
				conn.commit();
			}else{
				conn.rollback();
				throw new Exception("上传错误,请查看错误信息:");
			}
		} catch (Exception e) {
			conn.rollback();
			System.out.println("erro at " + rowNum);
			throw e;
		} finally {
			close(conn);
		}

	}

	private String getJarcubage(String areaNo, String jarType) throws Exception {
		if (jarType.equals("250")) {
			if (areaNo.equals("01"))
				return "220";
			else if (areaNo.equals("02"))
				return "240";
			else
				throw new Exception("250KG酒坛的理论容量为一片区220，二片区240,其他片区无设定!");
		} else if (jarType.equals("350")) {
			if (areaNo.equals("02"))
				return "320";
			else if (areaNo.equals("06") || areaNo.equals("07"))
				throw new Exception("350KG酒坛的理论容量六片区七片区无设定!");
			else
				return "310";
		} else if (jarType.equals("500")) {
			if (areaNo.equals("05") || areaNo.equals("07"))
				return "460";
			else
				return "440";
		} else
			throw new Exception("该片区[" + areaNo + "]," + jarType + "KG酒坛的未取到理论容易量!");
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
		if (area.indexOf("一") > -1) {
			areaNo = "01";
			pk_area = getStrMapValue(pubdocMap, "01");
		} else if (area.indexOf("二") > -1) {
			areaNo = "02";
			pk_area = getStrMapValue(pubdocMap, "02");
		} else if (area.indexOf("三") > -1) {
			areaNo = "03";
			pk_area = getStrMapValue(pubdocMap, "03");
		} else if (area.indexOf("四") > -1) {
			areaNo = "04";
			pk_area = getStrMapValue(pubdocMap, "04");
		} else if (area.indexOf("五") > -1) {
			areaNo = "05";
			pk_area = getStrMapValue(pubdocMap, "05");
		} else if (area.indexOf("六") > -1) {
			areaNo = "06";
			pk_area = getStrMapValue(pubdocMap, "06");
		} else if (area.indexOf("七") > -1 || area.indexOf("中华") > -1) {
			areaNo = "07";
			pk_area = getStrMapValue(pubdocMap, "07");
		}
		if (areaNo.equals(""))
			throw new Exception("Excel的片区[" + area + "]未找到对应的片区!");
		if (pk_area.equals(""))
			throw new Exception("Excel的片区[" + area + "]未找到对应的片区编码!");
		return pk_area;
	}

	private String getPk_building(String building) throws Exception {
		if (building.equals(""))
			throw new Exception("Excel的栋号[" + building + "]未找到对应的栋号!");
		buildingNo = "000".substring(0, 3 - building.length()) + building;
		String pk_building = getStrMapValue(pubdocMap, areaNo + buildingNo);
		if (pk_building.equals(""))
			throw new Exception("Excel的栋号[" + building + "]未找到对应的栋号编码!");
		return pk_building;
	}

	private String getPk_store(String store) throws Exception {
		if (store.equals(""))
			throw new Exception("Excel的库号[" + store + "]未找到对应的库号!");
		if (store.length() > 4) {
			store = store.substring(store.length() - 4, store.length());
		}
		storeNo = "0000".substring(0, 4 - store.length()) + store;
		String storeCode = getStrMapValue(storeMap, storeNo);
		if (storeCode.equals("") || storeCode.length() < 9)
			throw new Exception("Excel的库号[" + store + "]未找到正确的库号code!");
		buildingNo = storeCode.substring(2, 5);
		String pk_store = getStrMapValue(pubdocMap, storeCode);
		if (pk_store.equals(""))
			throw new Exception("Excel的库号[" + store + "]未找到对应的库号编码!");
		return pk_store;
	}

	private Map<String, String> initStoreMap() throws SQLException {
		// TODO 自动生成的方法存根
		// String sql = "select substr(code,6,4) as storeno,code from
		// mtws_pubdoc where def2='2' and dr=0";
		String sql = "select substr(code,6,4) as storeno,code from mtws_pubdoc where dr=0 and code like '" + areaNo + "%' and name like '%库'";
		Statement Stmt = conn.createStatement();
		ResultSet rs = Stmt.executeQuery(sql);
		Map<String, String> rstMap = new HashMap<String, String>();
		while (rs.next()) {
			rstMap.put(rs.getString("storeno"), rs.getString("code"));
		}
		Stmt.close();
		rs.close();
		return rstMap;
	}

	/**
	 * 获取库号PK
	 * 
	 * @param storeStr
	 * @return
	 * @throws BusinessException
	 */
	private Map<String, String> getABStore(String storeStr) throws Exception {
		if (storeStr.equals("")) {
			throw new Exception("库号[" + storeStr + "]不可为空");
		}
		Map<String, String> rstMap = new HashMap<String, String>();
		storeStr = String.format("%04d", Integer.parseInt(storeStr.trim()));
		String sql = "select c.pk_pubdoc,c.code,b.pk_pubdoc,b.code,a.pk_pubdoc,a.code from mtws_pubdoc a,mtws_pubdoc b,mtws_pubdoc c where   a.pid=b.pk_pubdoc and b.pid=c.pk_pubdoc and a.code like '_____" + storeStr + "' and a.name like '%库'";
		Statement Stmt = conn.createStatement();
		ResultSet rs = Stmt.executeQuery(sql);
		Object[] objs = new Object[rs.getMetaData().getColumnCount()];
		List<Object[]> result = new ArrayList<Object[]>();
		while (rs.next()) {
			for (int col = 0; col < rs.getMetaData().getColumnCount(); col++) {
				objs[col] = rs.getObject(col + 1);
			}
			result.add(objs);
		}
		Stmt.close();
		rs.close();
		if (result.size() < 1) {
			throw new Exception("查无库号[" + storeStr + "]的片区冻库档案资料");
		}
		for (int i = 0; i < 6; i++) {
			if (result.get(0)[i] == null || "".equals(result.get(0)[i])) {
				throw new Exception("库号[" + storeStr + "]的片区栋库资料[" + result.get(0)[i] + "]不可为空");
			}
		}

		areaNo = result.get(0)[1].toString();
		buildingNo = result.get(0)[3].toString();
		if (buildingNo.length() != 5) {
			throw new Exception("栋号编码[" + buildingNo + "]的长度不为5码");
		}
		buildingNo = buildingNo.substring(2, 5);
		storeNo = result.get(0)[5].toString();
		if (storeNo.length() != 9) {
			throw new Exception("库号编码[" + storeNo + "]的长度不为9码");
		}
		storeNo = storeNo.substring(5, 9);
		rstMap.put(areaNo, result.get(0)[0].toString());
		rstMap.put(buildingNo, result.get(0)[2].toString());
		rstMap.put(storeNo, result.get(0)[4].toString());

		return rstMap;
	}

	private List<String> getJarList(String strMapValue) throws Exception {
		List<String> jarList = new ArrayList<String>();
		if ("".equals(strMapValue.trim())||"无".equals(strMapValue.trim())||"有".equals(strMapValue.trim())) {
			return jarList;
		}
		// TODO 自动生成的方法存根
		strMapValue = strMapValue.replaceAll("--", "-");
		strMapValue = strMapValue.replaceAll("－－", "-");
		strMapValue = strMapValue.replaceAll("－", "-");
		strMapValue = strMapValue.replaceAll("—", "-");
		strMapValue = strMapValue.replaceAll("——", "-");
		strMapValue = strMapValue.replaceAll("、", "@");
		strMapValue = strMapValue.replaceAll(",", "@");
		strMapValue = strMapValue.replaceAll("，", "@");
		strMapValue = strMapValue.replaceAll("\\.", "@");
		String[] strAry = strMapValue.split("@");
		for (int i = 0; i < strAry.length; i++) {
			String strTmp = strAry[i].trim();
			if (strTmp.indexOf("-") > -1) {
				String[] strTmpAry = strTmp.split("-");
				int min = Integer.parseInt(strTmpAry[0].trim());
				int max = Integer.parseInt(strTmpAry[1].trim());
				if (min > max) {
					int temp = min;
					min = max;
					max = temp;
				}
				if(max>9999 || min>9999){
					throw new Exception("坛号["+min+"]-["+max+"]超出范围!");
				}
				
				String minJar = areaNo + buildingNo + storeNo + "0000".substring(0, 4 - String.valueOf(min).length()) + min;
				String maxJar = areaNo + buildingNo + storeNo + "0000".substring(0, 4 - String.valueOf(max).length()) + max;
				
				for (int j = 0; j <= max - min; j++) {
					String jarNo = String.valueOf(min + j);
					jarList.add(areaNo + buildingNo + storeNo + "0000".substring(0, 4 - jarNo.length()) + jarNo);
				}
			} else {
				if(strTmp.length() > 4){
					throw new Exception("坛号["+strTmp+"]超出范围!");
				}
				String jarCode = areaNo + buildingNo + storeNo + "0000".substring(0, 4 - strTmp.length()) + strTmp;
				jarList.add(jarCode);
			}
		}
		//检查是否有重复的数据
		for(int c=0; c<jarList.size(); c++){
			for(int d=c+1; d<jarList.size(); d++){
				if(jarList.get(c).equals(jarList.get(d))){
					logMessage("Excel档中的酒库[" + areaNo + buildingNo + storeNo + "]坛号[" + jarList.get(c) + "]有重复!");
				}
			}
		}
		Collections.sort(jarList);
		return jarList;
	}
	
	private void initJarNumByStoreMap() throws SQLException {
		// TODO 自动生成的方法存根
		// 取得05片区各库位总坛数
		if (areaNo.equals(""))
			areaNo = "XXXXX";
		String jarNumStoreSql = "select substr(code,1,9) as store ,count(code) jarnum from mtws_jar where code like '" + areaNo + "%' and dr=0 " + " group by substr(code,1,9) ";
		Statement Stmt = conn.createStatement();
		ResultSet rs = Stmt.executeQuery(jarNumStoreSql);
		while (rs.next()) {
			jarNumByStoreMap.put(rs.getString("store"), rs.getString("jarnum"));
		}
		Stmt.close();
		rs.close();
	}

	private void initJarCubageByStoreMap() throws SQLException {
		// TODO 自动生成的方法存根
		// TODO 自动生成的方法存根
		// 取得05片区各库位总坛数
		if (areaNo.equals(""))
			areaNo = "XXXXX";
		String jarNumStoreSql = "select substr(code,1,9) as store ,jarcubage from mtws_jar where code like '" + areaNo + "%' and dr=0 " + " group by substr(code,1,9),jarcubage ";
		Statement Stmt = conn.createStatement();
		ResultSet rs = Stmt.executeQuery(jarNumStoreSql);
		while (rs.next()) {
			jarCubageByStoreMap.put(rs.getString("store"), rs.getString("jarcubage"));
		}
		Stmt.close();
		rs.close();
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
