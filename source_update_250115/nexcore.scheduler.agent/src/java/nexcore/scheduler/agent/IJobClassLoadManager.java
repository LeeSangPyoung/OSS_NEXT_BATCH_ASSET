/*
 * Copyright (c) 2007 SK C&C. All rights reserved.
 * 
 * This software is the confidential and proprietary information of SK C&C. You
 * shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with SK
 * C&C.
 */

package nexcore.scheduler.agent;


/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 코어-업무 컴포넌트 관리</li>
 * <li>설  명 : 에이전트에서 실행할 Job 클래스를 loading 하고 관리한다.</li>
 * <li>작성일 : 2016. 1. 15</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public interface IJobClassLoadManager {
    public String COMPONENT_ID = "nc.scheduler.agent.JobClassLoadManager";
    
    // 배치 클래스 로드
    public Object   loadAndInitClasses(String className);
    
    // 로드된 모든 배치 객체 리스트
    public Object[] listAllClasses();
}

