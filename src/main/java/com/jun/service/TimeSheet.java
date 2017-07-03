package com.jun.service;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class TimeSheet {
	private String driver = "oracle.jdbc.driver.OracleDriver";
	private String url = "jdbc:oracle:thin:@127.0.0.1:1521/myorcl";
	private String user = "jun";
	private String password = "junking";
	private static Connection conn;

	public static void main(String[] args) throws Exception {
		TimeSheet jk = new TimeSheet();
		List<Map<String, String>> data = jk.parseExcel("D:/考勤报表6月.xls");
		System.out.println("共有" + data.size() + "条数据");
		conn = jk.getConnection();
		conn.setAutoCommit(false);
		System.out.println("Conn:" + conn);
		// 清表
		Statement statemenet = conn.createStatement();
		statemenet.execute("delete from time_sheet");
		statemenet.close();
		jk.superInsert(data);
		conn.commit();
		jk.close(conn);
		
		TimeSheetLate late = new TimeSheetLate();
		late.outPut();
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

	public List<Map<String, String>> parseExcel(String filePath) throws Exception {
		File file = new File(filePath);
		// 创建Excel对象，读取文件
		Workbook workbook = null;
		if (filePath.endsWith("xls")) {
			workbook = new HSSFWorkbook(FileUtils.openInputStream(file));
		} else if (filePath.endsWith("xlsx")) {
			workbook = new XSSFWorkbook(FileUtils.openInputStream(file));
		} else {
			throw new Exception("文档类型错误！");
		}
		// 通过名字“Sheet0”获取工作表
		// HSSFSheet sheet = workbook.getSheet("Sheet0");
		// 读取默认第一个工作表sheet
		Sheet sheet = workbook.getSheetAt(0);
		// 最后一行行号
		int lastRowNum = sheet.getLastRowNum();
		// 将excle转换成List
		List<Map<String, String>> excelData = new ArrayList<Map<String, String>>();
		// 读取每一行
		for (int i = 1; i < lastRowNum + 1; i++) {
			Row row = sheet.getRow(i);
			// 每一列存进Map
			Map<String, String> rowData = new HashMap<String, String>();
			for (int j = 0; j < 6; j++) {
				rowData.put(getColumnCharName(j), parseRow(row, j));
			}
			excelData.add(rowData);
		}
		workbook.close();
		return excelData;
	}

	public String getColumnCharName(int index) {
		return String.valueOf((char) (65 + index));
	}

	public String parseRow(Row row, int j) throws Exception {
		Cell cell = row.getCell(j);
		if (cell != null) {
			switch (cell.getCellType()) {
			case Cell.CELL_TYPE_STRING:
				return cell.getStringCellValue();
			case Cell.CELL_TYPE_NUMERIC:
				HSSFDataFormatter dataFormatter = new HSSFDataFormatter();
				return dataFormatter.formatCellValue(cell);
			case Cell.CELL_TYPE_FORMULA:
				return cell.getCellFormula();
			default:
				return cell.getStringCellValue();
			}
		}
		return "";
	}

	public void superInsert(List<Map<String, String>> data) throws SQLException {
		String code, name, operweek, operdate, opertime;
		String sql = "insert into time_sheet (id,code,name,operweek,operdate,opertime) values (seq_time_sheet.nextval,?,?,?,?,?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		int count = 0;
		int all = data.size();
		for (Map<String, String> map : data) {
			code = map.get("A");
			name = map.get("C");
			operweek = map.get("D");
			operdate = map.get("E");
			opertime = map.get("F");
			
			if(code == null || "".equals(code)){
				continue;
			}

			ps.setString(1, code.trim());
			ps.setString(2, name);
			ps.setString(3, operweek);
			ps.setString(4, operdate);
			ps.setString(5, opertime);
			ps.addBatch();

			if (++count % 1000 == 0) {
				ps.executeBatch();
			}
			System.out.println("进度:" + count + "/" + all);
		}
		ps.executeBatch();
		ps.close();
	}
}
