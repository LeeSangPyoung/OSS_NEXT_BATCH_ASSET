/*
 * Copyright (c) 2007 SK C&C. All rights reserved.
 * 
 * This software is the confidential and proprietary information of SK C&C. You
 * shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with SK
 * C&C.
 */

package nexcore.scheduler.ioc;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CustomClassPathXmlApplicationContext extends ClassPathXmlApplicationContext {
	
	protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
		super.customizeBeanFactory(beanFactory);
		// 읽어들이는 서로 다른 XML 파일에 같은 id를 가지는 컴포넌트 정의가 있을 경우 에러가 나지 않고 설정을 덮어쓰는 특성을 금지.
		beanFactory.setAllowBeanDefinitionOverriding(false);
	}
	
}
