package com.jun.service;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class TimeSheetLate {
	private String driver = "oracle.jdbc.driver.OracleDriver";
	private String url = "jdbc:oracle:thin:@127.0.0.1:1521/myorcl";
	private String user = "jun";
	private String password = "junking";
	private static Connection conn;
	private static String allIdSql = "select distinct b.id from time_sheet a left join time_finger b on a.code=b.finger";
	private static String allDateSql = "select operdate from time_sheet group by operdate order by operdate";
	private static SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
	public static Map<String, List<Map<String,String>>> errorMsg = new HashMap<String, List<Map<String,String>>>();

	// 上班时间(晚于等于这个时间就算迟到)
	public static String startTime = "08:33";
	// 下班时间(早于等于这个时间就算早退)
	public static String endTime = "17:27";
	// 导出Excel路径
	public static String path = "C:/Users/lx/Desktop/Time/";

	public static void main(String[] args) throws Exception {
		TimeSheetLate late = new TimeSheetLate();
		conn = late.getConnection();
		List<String> allId = late.querySingle(allIdSql);
		List<String> allDate = late.querySingle(allDateSql);
		List<String> allTime = null;
		for (String id : allId) {
			for (String date : allDate) {
				if (late.getAllTime(id, date).size() < 1) {
					late.log(id, date, "未打卡", "");
					continue;
				}
				allTime = late.getAllTime(id, date);
				late.processorLate(allTime, id, date);
			}
		}
		late.close(conn);
		late.outPutExcel(errorMsg, path);
		System.out.println("检测完毕!请看异常信息表！");
	}
	
	public void outPut() throws Exception{
		conn = getConnection();
		List<String> allId = querySingle(allIdSql);
		List<String> allDate = querySingle(allDateSql);
		List<String> allTime = null;
		for (String id : allId) {
			for (String date : allDate) {
				if (getAllTime(id, date).size() < 1) {
					log(id, date, "未打卡", "");
					continue;
				}
				allTime = getAllTime(id, date);
				processorLate(allTime, id, date);
			}
		}
		close(conn);
		outPutExcel(errorMsg, path);
		System.out.println("检测完毕!请看异常信息表！");
	}

	private void outPutExcel(Map<String, List<Map<String,String>>> errorMsg, String path) throws Exception {
		Set<String> keys = errorMsg.keySet();
		for(String key : keys){
			// 创建HSSFWorkbook对象
			HSSFWorkbook wb = new HSSFWorkbook();
			// 创建HSSFSheet对象
			HSSFSheet sheet = wb.createSheet(key);
			sheet.setColumnWidth(0, 5000);
			sheet.setColumnWidth(1, 5000);
			sheet.setColumnWidth(2, 5000);
			sheet.setColumnWidth(3, 5000);
			HSSFRow row = null;
			// 创建HSSFRow对象
			row = sheet.createRow(0);
			row.createCell(0).setCellValue("姓名");
			row.createCell(1).setCellValue("打卡日期");
			row.createCell(2).setCellValue("异常类型");
			row.createCell(3).setCellValue("情况说明");
			Map<String, String> map = null;
			for (int i = 0; i < errorMsg.get(key).size(); i++) {
				// 创建HSSFRow对象
				row = sheet.createRow(i + 1);
				map = errorMsg.get(key).get(i);
				// 创建HSSFCell对象
				row.createCell(0).setCellValue(map.get("NAME"));
				row.createCell(1).setCellValue(map.get("DATE"));
				row.createCell(2).setCellValue(map.get("TYPE"));
				row.createCell(3).setCellValue(map.get("REMARK"));
			}
			// 输出Excel文件
			FileOutputStream output = new FileOutputStream(path + key + ".xls");
			wb.write(output);
			output.flush();
			wb.close();
		}
	}

	private void log(String id, String date, String type, String remark) throws SQLException {
		Map<String, String> info = getInfo(id);
		if(errorMsg.get(info.get("dept")) == null){
			errorMsg.put(info.get("dept"), new ArrayList<Map<String,String>>());
		}
		
		List<Map<String,String>> list = errorMsg.get(info.get("dept"));
		Map<String,String> msg = new HashMap<String, String>();
		msg.put("NAME", info.get("name"));
		msg.put("DATE", date);
		msg.put("TYPE", type);
		msg.put("REMARK", remark);
		list.add(msg);
	}

	private Map<String, String> getInfo(String id) throws SQLException {
		List<String[]> info = queryInfo("select name,dept from time_user where id='" + id + "'");
		if(info.size() != 1){
			throw new SQLException("ID:" + id + "数据重复!");
		}
		Map<String, String> userInfo = new HashMap<String, String>();
		userInfo.put("name", info.get(0)[0]);
		userInfo.put("dept", info.get(0)[1]);
		return userInfo;
	}

	private void processorLate(List<String> allTime, String id, String date) throws ParseException, SQLException {
		String first = allTime.get(0);
		String last = allTime.get(allTime.size() - 1);
		if (first.equals(last)) {
			log(id, date, "只打卡一次", "打卡时间:" + first);
			return;
		}
		if (formatter.parse(first).compareTo(formatter.parse(startTime)) >= 0) {
			if (formatter.parse(first).compareTo(formatter.parse("12:00")) >= 0) {
				log(id, date, "上午未打卡", "首次打卡时间:" + first);
			} else {
				log(id, date, "迟到", "迟到时间:" + first);
			}
		}
		if (formatter.parse(last).compareTo(formatter.parse(endTime)) <= 0) {
			if (formatter.parse(last).compareTo(formatter.parse("12:00")) <= 0) {
				log(id, date, "下午未打卡", "最后打卡时间:" + last);
			} else {
				log(id, date, "早退", "早退时间:" + last);
			}
		}
	}

	private List<String> getAllTime(String id, String date) throws SQLException {
		return querySingle("select a.opertime from time_sheet a left join time_finger b on a.code=b.finger where b.id='" + id + "' and a.operdate='" + date + "' order by opertime");
	}

	public List<String> querySingle(String sql) throws SQLException {
		List<String> list = new ArrayList<String>();
		Statement state = conn.createStatement();
		ResultSet result = state.executeQuery(sql);
		while (result.next()) {
			list.add(result.getString(1));
		}
		result.close();
		state.close();
		return list;
	}
	
	public List<String[]> queryInfo(String sql) throws SQLException {
		List<String[]> list = new ArrayList<String[]>();
		Statement state = conn.createStatement();
		ResultSet result = state.executeQuery(sql);
		while (result.next()) {
			String[] strs = new String[2];
			strs[0] = result.getString(1);
			strs[1] = result.getString(2);
			list.add(strs);
		}
		result.close();
		state.close();
		return list;
	}

	public Connection getConnection() throws SQLException, ClassNotFoundException {
		if (conn == null) {
			Class.forName(driver);
			Connection test = DriverManager.getConnection(url, user, password);
			return test;
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
