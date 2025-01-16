package nexcore.scheduler.controller.internal;

import java.sql.Connection;

import javax.sql.DataSource;

public class DBChecker {
	private DataSource dataSource;

	public DBChecker() {
	}

	public void init() {
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
		}catch(Exception e) {
			System.out.println("!!! DB Connection fail !!!");
			e.printStackTrace();
			System.exit(2);
		}finally {
			try { 
				if (conn!=null) conn.close();
			} catch(Exception ee) {
				ee.printStackTrace();
			}
		}
	}
	
	public void destroy() {
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

}
