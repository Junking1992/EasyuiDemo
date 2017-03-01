package com.jun.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.jun.service.OpeningByXlsGuan;
import com.jun.service.OpeningByXlsNew;
import com.jun.service.OpeningGuanDataByXls;
import com.jun.service.OpeningJarDataByXls;
import com.jun.service.OpeningStoreDataXls;
import com.jun.service.ProgressUtil;

public class ExcelController extends HttpServlet {
	/**
	 * 将待转换Excel此路径
	 */
	public String sourcePath = "E:/UploadExcel/";

	/**
	 * 从第几行开始读取数据(第一行为0),默认从第二行开始
	 */
	public int startRowNum = 1;
	
	/**
	 * 文件名
	 */
	public String fileName = "";
	
	/**
	 * action
	 */
	public String action = "";

	/**
	 * 处理线程
	 */
	public Thread run = null;

	/**
	 * 最后一行和最后一列
	 */
	public int lastRowNum, lastCellNum = 0;

	/**
	 * 运行是否异常
	 */
	public boolean flag = false;
	
	/**
	 * 是否运行完成
	 */
	public boolean state = false;

	/**
	 * 异常信息
	 */
	public String msg = "";
	
	/**
	 * 公共类
	 */
	public ProgressUtil main;
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, UnsupportedEncodingException {
		if (run != null && run.isAlive()) {
			flag = true;
			//把特殊异常放在前面
			main.logMsg = new StringBuffer("后台正在处理：" + fileName + "中，请稍后...<br/>").append(main.logMsg);
			return;
		} else {
			init();
		}
		req.setCharacterEncoding("utf-8");
		fileName = req.getParameter("fileName");
		action = req.getParameter("action");
		Runnable parse = new Runnable() {
			public void run() {
				try {
					List<Map<String, String>> data;
					if("uploadA".equals(action)){
						main = new OpeningByXlsNew();
						//从第三行开始读取数据(第一行为0)
						startRowNum = 2;
						main.fileName = fileName;
						data = parseExcel(sourcePath + fileName);
						main.initIquantityDate(data);
					}else if("uploadB".equals(action)){
						main = new OpeningJarDataByXls();
						main.fileName = fileName;
						data = parseExcel(sourcePath + fileName);
						main.initIquantityDate(data);
					}else if("uploadC".equals(action)){
						main = new OpeningStoreDataXls();
						main.fileName = fileName;
						data = parseExcel(sourcePath + fileName);
						main.initIquantityDate(data);
					}else if("uploadD".equals(action)){
						main = new OpeningGuanDataByXls();
						main.fileName = fileName;
						data = parseExcel(sourcePath + fileName);
						main.initIquantityDate(data);
					}else if("uploadE".equals(action)){
						main = new OpeningByXlsGuan();
						main.fileName = fileName;
						data = parseExcel(sourcePath + fileName);
						main.initIquantityDate(data);
					}
					state = true;
				} catch (Exception e) {
					flag = true;
					//把特殊异常放在前面
					main.logMsg = new StringBuffer(e.getMessage() + "<br/>").append(main.logMsg);
					e.printStackTrace(); 
				}
			}
		};
		run = new Thread(parse);
		run.start();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding("utf-8");
		PrintWriter out = resp.getWriter();
		if (flag) {
			out.print("Msg" + main.logMsg.toString());
		}else if(state){
			if(main != null){
				out.print("Msg上传成功!用时：" + main.getTime());
			}
		}else{
			if(main != null){
				out.print(main.getProgress() + ":" + main.logMsg.toString());
			}
		}
		out.close();
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
		lastRowNum = sheet.getLastRowNum();
		//将excle转换成List
		List<Map<String, String>> excelData = new ArrayList<Map<String, String>>();
		// 读取每一行
		for (int i = startRowNum; i < lastRowNum + 1; i++) {
			Row row = sheet.getRow(i);
			// 获取当前行最后单元格列号
			lastCellNum = row.getLastCellNum();
			// 每一列存进Map
			Map<String, String> rowData = new HashMap<String ,String>();
			// 读取该行每一个cell
			for (int j = 0; j < lastCellNum; j++) {
				rowData.put(getColumnCharName(j), parseRow(row, j));
			}
			rowData.put("RN", i + 1 + "");
			excelData.add(rowData);
		}
		workbook.close();
		return excelData;
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
		lastRowNum = sheet.getLastRowNum();
		//将excle转换成List
		List<Map<String, String>> excelData = new ArrayList<Map<String, String>>();
		// 读取每一行
		for (int i = startRowNum; i < lastRowNum + 1; i++) {
			Row row = sheet.getRow(i);
			// 获取当前行最后单元格列号
			lastCellNum = row.getLastCellNum();
			// 每一列存进Map
			Map<String, String> rowData = new HashMap<String ,String>();
			// 读取该行每一个cell
//			for (int j = 0; j < lastCellNum; j++) {
			rowData.put(getColumnCharName(2), parseRow(row, 2));
			rowData.put(getColumnCharName(3), parseRow(row, 3));
			rowData.put(getColumnCharName(12), parseRow(row, 12));
//			}
			excelData.add(rowData);
		}
		workbook.close();
		return excelData;
	}
	
	public String parseRow(Row row, int j){
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
	
	public String getColumnCharName(int index){
		return String.valueOf((char) (65 + index));
	}
	
	public void init() {
		fileName = "";
		lastRowNum = 0;
		lastCellNum = 0;
		flag = false;
		msg = "";
		state = false;
	}

}
