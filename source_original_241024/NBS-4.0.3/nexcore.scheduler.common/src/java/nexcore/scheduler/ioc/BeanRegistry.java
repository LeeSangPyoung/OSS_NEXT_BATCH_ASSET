/*
 * Copyright (c) SK C&C. All rights reserved.
 * 
 * This software is the confidential and proprietary information of SK C&C. You
 * shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with SK
 * C&C.
 */

package nexcore.scheduler.ioc;


/**
 * <ul>
 * <li>업무 그룹명 : nexcore-scheduler-4</li>
 * <li>서브 업무명 : nexcore.scheduler.ioc</li>
 * <li>설  명 : Spring bean container 시작점</li>
 * <li>작성일 : 2016. 1. 12.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class BeanRegistry {
    protected static CustomClassPathXmlApplicationContext appContext;

    public static void init(String configFile) {
    	init(new String[]{configFile});
    }

    public static void init(String[] configFiles) {
    	// 초기화되지 않도록 생성
    	appContext = new CustomClassPathXmlApplicationContext();
    	appContext.setConfigLocations(configFiles);
    	
    	// 읽어들이는 서로 다른 XML 파일에 같은 id를 가지는 컴포넌트 정의가 있을 경우 에러가 나지 않고 설정을 덮어쓰는 특성을 금지.
//    	appContext.setAllowBeanDefinitionOverriding(false); // spring 3.0 이상에서 제공되는 메소드 
    	
    	// 초기화 실행
    	appContext.refresh();
    }

    /**
     * @see nexcore.framework.core.ioc.IRegistry#destroy()
     */
    public static void destroy() {
        if (appContext != null) {
            appContext.destroy();
            appContext = null;
        }
    }

    /**
     * klass에 따른 lookup을 수행한다. 이를 위해 각 <bean>의 id 속성 값은 해당 컴포넌트 인터페이스의 클래스명으로
     * 맞추어야 한다.
     * 
     * @param componentId
     *            컴포넌트 아이디
     * @return Object
     */
    public static Object lookup(String componentId) {
        return appContext.getBean(componentId);
    }

    public static Object getApplicationContext() {
		return appContext;
	}
}
