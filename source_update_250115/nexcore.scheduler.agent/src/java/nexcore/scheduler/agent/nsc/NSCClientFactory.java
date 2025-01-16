/**
 * 
 */
package nexcore.scheduler.agent.nsc;

import org.apache.commons.logging.Log;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;

import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : NSC 와 연결을 관리하는 매니저 클래스 </li>
 * <li>작성일 : 2012. 9. 13.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class NSCClientFactory extends BasePoolableObjectFactory implements INSCClientFactory {

	private String        nscHostname;
	private int           nscPort;
	private int           timeoutInMillis;
	private boolean       doLogData;
	private ObjectPool    objectPool;
	
	private Log           log;
	
	public NSCClientFactory() {
		log = LogManager.getLog("nsc");
	}
	
	public void init() {
		objectPool.setFactory(this);
	}
	
	public void destroy() {
	}
	
	public String getNscHostname() {
		return nscHostname;
	}

	public void setNscHostname(String nscHostname) {
		this.nscHostname = nscHostname;
	}

	public int getNscPort() {
		return nscPort;
	}

	public void setNscPort(int nscPort) {
		this.nscPort = nscPort;
	}

	public int getTimeoutInMillis() {
		return timeoutInMillis;
	}

	public void setTimeoutInMillis(int timeoutInMillis) {
		this.timeoutInMillis = timeoutInMillis;
	}
	
	public boolean isDoLogData() {
		return doLogData;
	}

	public void setDoLogData(boolean doLogData) {
		this.doLogData = doLogData;
	}
	
	public ObjectPool getObjectPool() {
		return objectPool;
	}

	public void setObjectPool(ObjectPool objectPool) {
		this.objectPool = objectPool;
	}
	
	
	
	
	
	// ===================== ObjectPoolFactory 메소드들 ================================
	/**
	 * NSCClient 객체가 소멸될때 호출됨.
	 */
	public void destroyObject(Object obj) throws Exception {
		INSCClient client = (INSCClient)obj;
		client.close();
	}

	/**
	 * 새로운 NSCClient 객체를 만든다.
	 */
	public Object makeObject() throws Exception {
		return new NSCClientImpl(nscHostname, nscPort, timeoutInMillis, doLogData);
	}

	/**
	 * Pool 에서 NSCClient를 꺼낼때 유효한지 여부를 체크함 
	 */
	public boolean validateObject(Object obj) {
		INSCClient client = (INSCClient)obj;
		try {
			client.check();
			return true;
		}catch(Throwable e) {
			return false;
		}
	}
	// ===================== 여기까지 ObjectPoolFactory 메소드들 ================================
	
	
	
	
	
	public INSCClient getClient() {
		try {
			return (INSCClient) objectPool.borrowObject(); 
		} catch (Throwable e) {
			Util.logError(log, "[NSC] "+this+" getClient() error", e);
			throw Util.toRuntimeException(e);
		}
			
	}
	
	public void returnClient(INSCClient client) {
		try {
			objectPool.returnObject(client);
		} catch (Throwable e) {
			Util.logError(log, "[NSC] "+this+" returnClient() error", e);
			
			try { // 반환 중 에러나면 버린다.
				client.close();
			} catch (Exception ignore) {
			}
			
			throw Util.toRuntimeException(e);
		}
	}
	
}
