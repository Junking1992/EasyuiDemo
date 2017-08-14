package com.jun.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OpeningByXlsTubGrade extends ProgressUtil {

	@Override
	public void initIquantityDate(List<Map<String, String>> xlsAryList) throws Exception {
		// 设置进度总数
		setAllCount(xlsAryList.size());
		try {
			// 立flag!
			boolean flag = true;
			// 获取数据库连接
			conn = getConnection();
			// 开启事务
			conn.setAutoCommit(false);
			// 循环Excel每行数据
			for (int i = 0; i < xlsAryList.size(); i++) {
				Map map = (Map) xlsAryList.get(i);
				// 错误行号
				errorRowNum = getStrMapValue(map, "RN");
				// 获取该行库号
				String storeCode = getStrMapValue(map, "A");
				// 获取桶号
				String tubCode = getStrMapValue(map, "B");
				// 获取等级
				String grade = getStrMapValue(map, "C");
				// 实际等级
				grade = getGrade(grade);
				// 检查库号是否为纯数字
				if (!storeCode.matches("[0-9]+") || storeCode.length() > 4) {
					flag = false;
					logMessage("Excel第" + errorRowNum + "行错误： 库号必须是四位一下纯数字!");
				} else {
					storeCode = String.format("%04d", Integer.parseInt(storeCode));
				}
				// 库号主键
				String pk_store = "";
				// 检查桶号是否为纯数字

				// 如果是完整桶号则开启
				// tubCode=tubCode.substring(tubCode.length()-3,
				// tubCode.length());
				if (!tubCode.matches("[0-9]+")) {
					flag = false;
					logMessage("Excel第" + errorRowNum + "行错误： 桶号必须为三位以下纯数字!");
				}
				// 1.取得库号对应PK，如果库号不存在抛出异常

				List list1 = getPk_Store(storeCode);
				if (list1.size() == 1) {
					pk_store = (String) list1.get(0);
				} else {
					flag = false;
					logMessage("Excel第" + errorRowNum + "行错误： 该库号没有唯一的对应主键!");
				}
				// 2.根据三位桶号查询坛库存档完整桶号，如果不存在跳过本次循环
				if (tubCode.length() > 3) {
					flag = false;
					logMessage("Excel第" + errorRowNum + "行错误： 该桶号大于三位数!");

				} else {
					list1 = getTubcode(pk_store, tubCode);
					if (list1.size() == 1) {
					} else {
						continue;
					}
				}
				// 3.根据库号pk和完整桶号更新坛库存档和桶库存档的等级栏位
				// 修改坛档 获取该桶号和库号同事存在下的 桶号
				list1 = getTubcode(pk_store, tubCode);
				StringBuffer sb = new StringBuffer();
				// 实际存的桶号
				String tubcod = "";
				if (list1.size() == 1) {

					tubcod = (String) list1.get(0);
					int a = update("update mtws_iquantity set def20 = '" + grade + "' where  pk_store = '" + pk_store
							+ "'  and  dr = 0   and  tubcode = '" + tubcod + "'");
					System.out.println("Excel第" + errorRowNum);
					System.out.println("坛档修改：" + a + "条");
				} else {
					flag = false;
					logMessage("Excel第" + errorRowNum + "行错误： 该库号与桶号对应的坛档中桶号不唯一!");
				}
				// 修改桶档案
				int b = update("update mtws_iqtytub set grade='" + grade + "'  where dr=0 and   pk_store='" + pk_store
						+ "' and  tubcode ='" + tubcod + "'");
				System.out.println("桶档修改：" + b + "条");
				System.out.println("库号主键：  " + pk_store + "   桶号：  " + tubcod + " 等级：  " + grade);
				System.out.println("**********************************************");
			}
			// 如果flag为true就提交到数据库，否则回滚
			if (flag) {
				 conn.commit();

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
		if (grade.indexOf("特") > -1 || grade.indexOf("0") > -1) {
			newGrade = "0";
		} else if (grade.indexOf("一") > -1 || grade.indexOf("1") > -1) {
			newGrade = "1";
		} else if (grade.indexOf("二") > -1 || grade.indexOf("2") > -1) {
			newGrade = "2";
		} else if (grade.indexOf("三") > -1 || grade.indexOf("3") > -1) {
			newGrade = "3";
		} else if (grade.indexOf("四") > -1 || grade.indexOf("4") > -1) {
			newGrade = "4";
		} else if (grade.indexOf("禁") > -1 || grade.indexOf("8") > -1) {
			newGrade = "8";
		} else if (grade.indexOf("未") > -1 || grade.indexOf("9") > -1) {
			newGrade = "9";
		} else {
			return "";
		}

		if (grade.indexOf("-") > 0) {
			newGrade += "-";
		}
		return newGrade;
	}

	// update 修改操作
	public int update(String sql) throws SQLException {
		Statement Stmt = conn.createStatement();
		// 返回新增或更新数据量
		int i = Stmt.executeUpdate(sql);
		Stmt.close();
		return i;
	}

	// select 坛档是否有该库号的主键（只有一条）
	private List getPk_Store(String kuhao) throws SQLException {
		String sql = "select pk_pubdoc from mtws_pubdoc  where name like '" + kuhao + "%' and dr=0";
		// Statement Stmt = conn.createStatement();
		PreparedStatement pst = conn.prepareStatement(sql);
		ResultSet rs = pst.executeQuery();
		// ResultSet rs = Stmt.executeQuery(sql);
		List<String> list = new ArrayList<String>();
		while (rs.next()) {
			list.add(rs.getString("pk_pubdoc"));
		}
		// Stmt.close();
		pst.close();
		rs.close();
		return list;
	}

	// 查询 该桶号与该库号 同时存在坛档中 的 桶号（即需要修改坛档 桶号）
	private List getTubcode(String store, String tubcode) throws SQLException {
		String sql = "select distinct tubcode from mtws_iquantity   where  pk_store='" + store
				+ "'  and  dr=0     and  tubcode like '%" + String.format("%03d", Integer.parseInt(tubcode)) + "'";
		PreparedStatement pst = conn.prepareStatement(sql);
		ResultSet rs = pst.executeQuery();
		List<String> list = new ArrayList<String>();
		while (rs.next()) {
			list.add(rs.getString("tubcode"));
		}
		pst.close();
		rs.close();
		return list;
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
