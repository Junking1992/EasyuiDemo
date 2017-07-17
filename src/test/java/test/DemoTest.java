package test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DemoTest {
	
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
	
	public List<String[]> query(String sql) throws SQLException{
		List<String[]> result = new ArrayList<String[]>();
		Statement Stmt = conn.createStatement();
		ResultSet rs = Stmt.executeQuery(sql);
		ResultSetMetaData md = rs.getMetaData();
		int columns = md.getColumnCount();
		while(rs.next()){
			String[] strs = new String[columns];
			for (int i = 1; i <= columns; i++) {
				strs[i-1] = rs.getString(i);
			}
			result.add(strs);
		}
		return result;
	}
	
	public int update(String sql) throws SQLException {
		Statement Stmt = conn.createStatement();
		// 返回新增或更新数据量
		int i = Stmt.executeUpdate(sql);
		Stmt.close();
		return i;
	}

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		DemoTest demo = new DemoTest();
		demo.conn = demo.getConnection();
		demo.conn.setAutoCommit(false);
		List<String[]> list = demo.query("select a.srcbillid,b.pk_store from mtws_iblentjpaln a left join mtws_iblentjpaln_b b on a.pk_iblentjpaln = b.pk_iblentjpaln where nvl(a.dr, 0) = 0 and nvl(b.dr, 0) = 0");
		int all = 0;
		for(String[] arr : list){
			String sql = "update mtws_iblentdpaln_b set isallot = 'Y' where dr = 0 and pk_iblentdpaln = '"+arr[0]+"' and pk_store = '"+arr[1]+"'";
			all += demo.update(sql);
		}
		System.out.println(all);
//		demo.conn.rollback();
		demo.conn.commit();
	}

}
