/*
 * Copyright (c) 2007 SK C&C. All rights reserved.
 *
 * This software is the confidential and proprietary information of SK C&C.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with SK C&C.
 */

package nexcore.scheduler.exception;

import nexcore.scheduler.msg.MSG;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 배치 Agent 에서 발생하는 exception</li>
 * <li>작성일 : 2010. 04. 08</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
// Agent 내부에서 throw 하는 시스템 Exception 
public class AgentException extends RuntimeException {
	private static final long serialVersionUID = 7771758420085672684L;

	private String msgName;

    public AgentException(String msgName) {
    	super(MSG.get(msgName));
    	this.msgName = msgName;
    }

    public AgentException(String msgName, Object ... messageParams) {
    	super(MSG.get(msgName, messageParams));
    	this.msgName = msgName;
    }

    public AgentException(String msgName, Throwable throwable) {
    	super(MSG.get(msgName), throwable);
    	this.msgName = msgName;
    }

    public AgentException(String msgName, Throwable throwable, Object ...  messageParams) {
        super(MSG.get(msgName, messageParams), throwable);
        this.msgName = msgName;
    }
    
	public String getMsgName() {
		return msgName;
	}
    

}
