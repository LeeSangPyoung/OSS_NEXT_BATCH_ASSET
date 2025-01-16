/**
 * 
 */
package nexcore.scheduler.agent.runner;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.exception.AgentException;
import nexcore.scheduler.util.Util;

import org.apache.commons.logging.Log;


/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 :  </li>
 * <li>작성일 : 2013. 10. 20.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class DBProcedureJob {
	private String DATASOURCE_NAME_PARAM = "DATASOURCE_NAME";
	private String callSql; // {call procedure_name(?,?,?)} , {?=call procedure_name2(?,?,?)}
	
	private Log    log;
	
	public DBProcedureJob() {
	}

	/**
	 * 데이타 소스 획득.
	 * 파리미터 ("DATASOURCE_NAME") 가 존재하면 그 이름을 JNDI lookup 하여 가져오고
	 * 그렇지 않으면 기본 프레임워크 DS 매핑을 통해 가져옴.
	 * @param context
	 * @return DataSource
	 */
	private DataSource getDataSource(JobContext context) {
		String dataSourceName = context.getInParameter(DATASOURCE_NAME_PARAM);
		if (Util.isBlank(dataSourceName)) {
return null;
//			return getDefaultDBSession(context).getDataSource();
		}else {
			InitialContext initContext;
			DataSource dataSource;
			try {
				initContext = new InitialContext();
				dataSource  = (DataSource)initContext.lookup(dataSourceName);
				Util.logInfo(log, "Lookup DataSource. Name:"+dataSourceName+", DataSource:"+dataSource);
				return dataSource;
			} catch (NamingException e) {
				throw new AgentException("agent.cannot.find.datasource", e, dataSourceName, dataSourceName);
			}
		}
	}

	public void beforeExecute(JobContext context) {
		callSql = context.getInParameter("COMPONENT_NAME");
		log     = context.getLogger();
	}

	public void execute(JobContext context) {
		DataSource ds = getDataSource(context);
		
		Connection         conn  = null;
		CallableStatement  cstmt = null;
		try {
			conn  = ds.getConnection();
			Util.logInfo(log, "Prepare procedure. "+callSql);
			cstmt = conn.prepareCall(callSql);

			Util.logInfo(log, "Setting arguments");
			List<Integer> outParamIndexList = new ArrayList(); 
			for (int i=1; ; i++) {
				String argIO    = context.getInParameter("ARG"+i+"_IO");
				
				if (Util.isBlank(argIO)) { /* argument 가 다 끝났다 */
					break;
				}
				
				String argType  = context.getInParameter("ARG"+i+"_TYPE");
				String argValue = context.getInParameter("ARG"+i+"_VALUE");
				
				if ("I".equals(argIO)) {
					Util.logInfo(log, "  Argument "+i+" : "+argIO+","+argType+","+argValue);
					setCStmtParameter(cstmt, i, argType, argValue);
				}else if ("O".equals(argIO)) {
					Util.logInfo(log, "  Argument "+i+" : "+argIO+","+argType);
					cstmt.registerOutParameter(i, getSqlTypeValue(argType));
					outParamIndexList.add(i); /* 아래에서 결과 값 조회할때 사용한다 */
				}
			}
			Util.logInfo(log, "Executing procedure");
			int cnt = cstmt.executeUpdate();
			cstmt.execute();
			Util.logInfo(log, "Executed procedure. affected row : "+cnt);

			context.setReturnValue("AFFECTED_ROW", String.valueOf(cnt));
			context.setReturnValue("OUT_COUNT",    String.valueOf(outParamIndexList.size()));
			for (int i : outParamIndexList) {
				String outValue = cstmt.getString(i);
				Util.logInfo(log, "  Output ["+i+"] : "+outValue);
				context.setReturnValue("OUT_"+i, outValue);
			}
		}catch(Exception e) {
			throw new AgentException("agent.dbproc.call.error", e, callSql);
		}finally {
			if (cstmt != null) {
				try {
					cstmt.close();
				}catch (Exception e) {
					Util.logError(log, "Close CallableStatement fail. ", e);
				}
			}

			if (conn != null) {
				try {
					conn.close();
				}catch (Exception e) {
					Util.logError(log, "Close connection fail. ", e);
				}
			}
		}
	}
	
	/**
	 * 프로시저 호출용 파라미터 Set.
	 * sqlType 에 따라 set 메소드를 달리 한다. 
	 * 일부 DB 에서는 setObject() 메소드를 사용하면 값이 null 일때 에러가 나는 경우가 있다. 이때는 개별 set 메소드로 해주어야된다.
	 * @param cstmt
	 * @param i
	 * @param sqlType
	 * @param value
	 * @throws SQLException
	 */
	private void setCStmtParameter(CallableStatement cstmt, int i, String sqlType, String value) throws SQLException {
		/*
		 * http://docs.oracle.com/javase/6/docs/technotes/guides/jdbc/getstart/mapping.html#996858 참조
		 */
		if ("CHAR".equalsIgnoreCase(sqlType) || "VARCHAR".equalsIgnoreCase(sqlType) || "VARCHAR2".equalsIgnoreCase(sqlType) || "LONGVARCHAR".equalsIgnoreCase(sqlType)) {
			cstmt.setString(i,  value);
		}else if ("NUMERIC".equalsIgnoreCase(sqlType) || "DECIMAL".equalsIgnoreCase(sqlType)) {
			cstmt.setBigDecimal(i, new BigDecimal(value));
		}else if ("BIT".equalsIgnoreCase(sqlType)) {
			cstmt.setBoolean(i, Util.toBoolean(value));
		}else if ("TINYINT".equalsIgnoreCase(sqlType)) {
			cstmt.setByte(i, Byte.parseByte(value));
		}else if ("SMALLINT".equalsIgnoreCase(sqlType)) {
			cstmt.setShort(i, Short.parseShort(value));
		}else if ("INTEGER".equalsIgnoreCase(sqlType)) {
			cstmt.setInt(i, Integer.parseInt(value));
		}else if ("BIGINT".equalsIgnoreCase(sqlType)) {
			cstmt.setLong(i, Long.parseLong(value));
		}else if ("REAL".equalsIgnoreCase(sqlType)) {
			cstmt.setFloat(i, Float.parseFloat(value));
		}else if ("FLOAT".equalsIgnoreCase(sqlType)) {
			cstmt.setDouble(i, Double.parseDouble(value));
		}else if ("DOUBLE".equalsIgnoreCase(sqlType)) {
			cstmt.setDouble(i, Double.parseDouble(value));
		}else if ("DATE".equalsIgnoreCase(sqlType)) { /* yyyy-mm-dd */
			cstmt.setDate(i, Date.valueOf(value));
		}else if ("TIME".equalsIgnoreCase(sqlType)) { /* hh:mm:ss */
			cstmt.setTime(i, Time.valueOf(value));
		}else if ("TIMESTAMP".equalsIgnoreCase(sqlType)) { /* yyyy-mm-dd hh:mm:ss[.f...] */
			cstmt.setTimestamp(i, Timestamp.valueOf(value)); 
		}
	}
	
	/**
	 * String 값으로 받은 Types 의 멤버명으로 부터 실제 값을 읽어 옮
	 * @param typeName
	 * @return
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private int getSqlTypeValue(String typeName) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Field fld = Types.class.getDeclaredField(typeName);
		
		if (fld == null) {
			throw new IllegalArgumentException("No 'java.sql.Types." + typeName + "' type exists.");
		}
		
		int value = fld.getInt(null);
		
		return value;
	}
	
}
