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
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class InputExcelByJK2 {

	private String driver = "oracle.jdbc.driver.OracleDriver";
	private String url = "jdbc:oracle:thin:@10.0.5.152:1521/jknc";
	private String user = "jknc02";
	private String password = "jknc02";
	private static Connection conn;

	public static void main(String[] args) throws Exception {
		InputExcelByJK2 jk = new InputExcelByJK2();
		List<Map<String, String>> data = jk.parseFinanceExcel("E:/UploadExcel/一、二片区发蓝莓酒统计表.xlsx");
		System.out.println("共有" + data.size() + "条数据");
		conn = jk.getConnection();
		conn.setAutoCommit(false);
		System.out.println("Conn:" + conn);
		// 清表
		Statement statemenet = conn.createStatement();
		statemenet.execute("delete from tmp_a");
		statemenet.close();
		jk.superInsert(data);
		conn.commit();
		jk.close(conn);
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

	public List<Map<String, String>> parseFinanceExcel(String filePath) throws Exception {
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
		for (int i = 2; i < lastRowNum + 1; i++) {
			Row row = sheet.getRow(i);
			// 每一列存进Map
			Map<String, String> rowData = new HashMap<String, String>();
			for (int j = 2; j < 14; j++) {
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
		String store, tubcode, type, lv, qty, mome;
		String sql = "insert into tmp_a (pk_id,store,tubcode,type,lv,qty,mome) values (?,?,?,?,?,?,?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		int count = 0;
		int all = data.size();
		for (Map<String, String> map : data) {
			store = map.get("C");
			tubcode = map.get("D");
			type = map.get("H");
			lv = map.get("I");
			qty = map.get("J");
			mome = map.get("L");
			
			ps.setString(1, UUID.randomUUID().toString());
			ps.setString(2, store);
			ps.setString(3, tubcode);
			ps.setString(4, type);
			ps.setString(5, lv);
			ps.setString(6, qty);
			ps.setString(7, mome);
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
