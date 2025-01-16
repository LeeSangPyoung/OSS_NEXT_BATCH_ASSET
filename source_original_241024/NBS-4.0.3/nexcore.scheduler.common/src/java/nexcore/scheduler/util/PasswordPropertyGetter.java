/*
 * Copyright (c) 2007 SK C&C. All rights reserved.
 *
 * This software is the confidential and proprietary information of SK C&C.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with SK C&C.
 */

package nexcore.scheduler.util;

import nexcore.framework.supports.EncryptionUtils;

import org.springframework.beans.factory.FactoryBean;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : </li>
 * <li>설  명 : DES 암호화된 패스워드를 평문으로 바꾸어 리턴함</li>
 * <li>작성일 : 2011. 5. 6.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class PasswordPropertyGetter implements FactoryBean {
	private String password;
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Object getObject() throws Exception {
//        if (!Util.isBlank(password) && (password.contains("{DES}") || password.contains("{AES}"))) {
        if (!Util.isBlank(password)) {
            return EncryptionUtils.decode(password);
        }else {
            return password;
        }
    }

    public Class getObjectType() {
        return String.class;
    }

    public boolean isSingleton() {
        return true;
    }
    
}
