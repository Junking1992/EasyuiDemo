package com.jun.service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpeningStoreDataXls extends ProgressUtil {

	public void initIquantityDate(List<Map<String, String>> xlsAryList) throws Exception {
		// 设置进度总数
		setAllCount(xlsAryList.size());
		// 控制commit
		boolean flag = true;
		// 获取conn
		conn = getConnection();
		// 开启事务
		conn.setAutoCommit(false);
		try {
			for (Map<String, String> row : xlsAryList) {
				// 处理每行
				if (!upload(row)) {
					flag = false;
				}
				// 进度增长
				addCount();
			}
			if (!flag) {
				throw new Exception("上传错误,请查看错误信息:");
			}
		} catch (Exception ex) {
			if(conn != null){
				conn.rollback();
			}
			throw ex;
		} finally {
			conn.close();
		}

	}

	private boolean upload(Map<String, String> row) throws SQLException, IOException {
		boolean flag = true;
		String funcArea = getStrMapValue(row, "B");
		String funcLocal = getStrMapValue(row, "C");
		String storeHouse = getStrMapValue(row, "E");

		String ts, pk_area, pk_building, pk_store;
		String querySql = "select to_char(sysdate,'yyyy-mm-dd hh24:mi:ss') as ts,c.pk_pubdoc as pk_area,c.code as areaCode,b.pk_pubdoc as pk_building,b.code as buildingCode,a.pk_pubdoc as pk_store,a.code as storeCode from mtws_pubdoc a,mtws_pubdoc b,mtws_pubdoc c where a.pid=b.pk_pubdoc and b.pid=c.pk_pubdoc and a.code like '_____"
				+ String.format("%04d", Integer.parseInt(storeHouse)) + "' and a.name like '%库'";
		List<Map<String, String>> list = query(querySql);
		if (list.size() > 0) {
			Map<String, String> map = list.get(0);
			ts = map.get("TS");
			pk_area = map.get("PK_AREA");
			pk_building = map.get("PK_BUILDING");
			pk_store = map.get("PK_STORE");

			// 判断库表是否有资料
			List<Map<String, String>> queryList = query(
					"select pk_iqtystore from mtws_iqtystore where nvl(dr,0)=0 and pk_iqtystore='" + pk_store + "'");
			if (queryList.size() > 0) {// 已存在
				// 更新def19和def20
				update("update mtws_iqtystore set def19='" + funcArea + "',def20='" + funcLocal
						+ "' where nvl(dr,0)=0 and pk_iqtystore='" + pk_store + "'");
			} else {// 不存在
				// 新增
				create("insert into mtws_iqtystore (pk_iqtystore,pk_area,pk_building,pk_store,pk_measure,def19,def20,pk_group,pk_org,pk_org_v,dbilldate,vbillstatus,maketime,ts,dr) values ('"
						+ pk_store + "','" + pk_area + "','" + pk_building + "','" + pk_store
						+ "','1001A41000000000034A','" + funcArea + "','" + funcLocal
						+ "','0001A5100000000001KL','0001A410000000000954','0001A410000000000953','" + ts + "','-1','"
						+ ts + "','" + ts + "',0)");
			}
//			conn.commit();
		} else {
			flag = false;
			logMessage("行号:" + row.get("RN") + "	库号" + storeHouse + "不存在.");
		}
		return flag;
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

	public List<Map<String, String>> query(String sql) throws SQLException {
		// PreparedStatement:是预编译的,对于批量处理可以大大提高效率.也叫JDBC存储过程
		// Statement:在对数据库只执行一次性存取的时侯，用 Statement对象进行处理。
		Statement Stmt = conn.createStatement();
		// 返回新增或更新数据量
		ResultSet rst = Stmt.executeQuery(sql);
		ResultSetMetaData md = rst.getMetaData(); // 得到结果集(rs)的结构信息，比如字段数、字段名等
		int columnCount = md.getColumnCount(); // 返回此 ResultSet 对象中的列数
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		Map<String, String> rowData = null;
		while (rst.next()) {
			rowData = new HashMap<String, String>(columnCount);
			for (int i = 1; i <= columnCount; i++) {
				rowData.put(md.getColumnName(i), rst.getObject(i).toString());
			}
			list.add(rowData);
		}
		Stmt.close();
		return list;
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

	private String getStrMapValue(Map map, String key) {
		return map != null && map.get(key) != null ? map.get(key).toString().trim() : "";
	}

}
