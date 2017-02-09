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
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class InputExcelByJK {
	
	private String driver = "oracle.jdbc.driver.OracleDriver";
	private String url = "jdbc:oracle:thin:@10.0.5.152:1521/jknc";
	private String user = "jknc02";
	private String password = "jknc02";
	private static Connection conn;
	
	public static void main(String[] args) throws Exception {
		InputExcelByJK jk = new InputExcelByJK();
		List<Map<String, String>> data = jk.parseFinanceExcel("E:/UploadExcel/2017年用1月改版账目.xlsx");
		System.out.println("共有" + data.size() + "条数据");
		conn = jk.getConnection();
		conn.setAutoCommit(false);
		System.out.println("Conn:" + conn);
		//清表
		Statement statemenet = conn.createStatement();
		statemenet.execute("delete from tmp_accstore");
		statemenet.close();
		conn.commit();
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
		//将excle转换成List
		List<Map<String, String>> excelData = new ArrayList<Map<String, String>>();
		// 读取每一行
		for (int i = 1; i < lastRowNum + 1; i++) {
			Row row = sheet.getRow(i);
			// 每一列存进Map
			Map<String, String> rowData = new HashMap<String ,String>();
			rowData.put(getColumnCharName(2), parseRow(row, 2));
			rowData.put(getColumnCharName(3), parseRow(row, 3));
			rowData.put(getColumnCharName(12), parseRow(row, 12));
			excelData.add(rowData);
		}
		workbook.close();
		return excelData;
	}
	
	public String getColumnCharName(int index){
		return String.valueOf((char) (65 + index));
	}
	
	public String parseRow(Row row, int j) throws Exception {
		Cell cell = row.getCell(j);
		if (cell != null) {
			return cell.getStringCellValue();
		}
		return null;
	}
	
	private static String parseTubcode(String tubcode) {
		String[] arrs = tubcode.split("00-");
		if(arrs.length == 2){
			return arrs[0] + String.format("%02d", Integer.parseInt(arrs[1]));
		}
		return tubcode;
	}
	
	public void superInsert(List<Map<String, String>> data) throws SQLException{
		String store,tubcode,qty;
		String sql = "insert into tmp_accstore (tmpid,storeno,tubcode,iqtytub) values (seq_tmp_accstore.nextval,?,?,?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		int count = 0;
		int all = data.size();
		for (Map<String, String> map : data) {
			store = map.get("C");//库号
			tubcode = map.get("D");//桶号
			qty = map.get("M");//重量
			
			//格式化库号
			if(store == null){
				store = "";
			}
			if(store.matches("^\\+?[1-9][0-9]*$")){//验证非零正整数
				store = String.format("%04d", Integer.parseInt(store));
			}
			//格式化桶号
			if(tubcode == null){
				tubcode = "";
			}
			tubcode = tubcode.replace("桶", "");
			tubcode = tubcode.replace("—", "-");
			tubcode = tubcode.replace("-", "-");
			if(tubcode.indexOf("00-") > -1){
				tubcode = parseTubcode(tubcode);
			}
			if(!"".equals(tubcode) && tubcode.matches("^\\+?[1-9][0-9]*$")){
				tubcode = String.format("%03d", Integer.parseInt(tubcode));
			}
			
			//格式化数量
			if(qty == null || "".equals(qty) || !qty.matches("^(-?\\d+)(\\.\\d+)?$")){
				qty = "0";
			}
			ps.setString(1, store);
			ps.setString(2, tubcode);
			ps.setString(3, qty);
			ps.addBatch();
			
			if(++count % 1000 == 0 ) {
		        ps.executeBatch();
		    }
			System.out.println("进度:" + count + "/" + all);
		}
		ps.executeBatch();
		ps.close();
	}

}
