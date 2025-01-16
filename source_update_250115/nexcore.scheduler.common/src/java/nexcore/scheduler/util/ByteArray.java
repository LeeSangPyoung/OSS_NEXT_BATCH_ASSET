/*
 * Copyright (c) 2007 SK C&C. All rights reserved.
 *
 * This software is the confidential and proprietary information of SK C&C.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with SK C&C.
 */

package nexcore.scheduler.util;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

// nexcore-bat-client 에 묶기 위해 nexcore-batch-2-5 프로젝트에 갖다 놓음. 정호철 2011-06-09
/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : </li>
 * <li>설  명 : </li>
 * <li>작성일 : 2009. 9. 2.</li>
 * <li>작성자 : 정호철</li>
 * byte[] 를 재사용하기 위해 리턴값으로 주기 위해서 byte[] 를 한번 wrap 한다.
 * </ul>
 */
public class ByteArray implements Serializable {
    private static final long serialVersionUID = 8677440855741790256L;

    private byte[] bytes;
    private int offset;
    private int length;
    
    public ByteArray(byte[] bytes) {
        this.bytes      = bytes;
        this.offset     = 0;
        this.length     = bytes.length;
    }

    public ByteArray(byte[] bytes, int offset, int length) {
        this.bytes      = bytes;
        this.offset     = offset;
        this.length     = length;
    }

    public byte[] getByteArray() {
        return this.bytes;
    }

    public int getOffset() {
        return this.offset;
    }

    public int getLength() {
        return this.length;
    }
    
    /**
     * ByteArray의 내용을 복제한 새로운 ByteArray을 리턴함. 
     * 내부 byte[]를 그대로 복제하는 것이 아니라, offset, length 를 이용하여 실제 데이타 만큼만 복제한다.
     * @return
     */
    public ByteArray copy() {
        byte[] newBytes = new byte[length];
        System.arraycopy(bytes, offset, newBytes, 0, length);
        return new ByteArray(newBytes);
    }
    
    /**
     * 로그 생성시 사용됨.
     */
    public String toString() {
        return new String(bytes, offset, length);
    }

    /**
     * 로그 생성시 사용됨.
     * @param charset 문자셋
     * @throws UnsupportedEncodingException 
     */
    public String toString(String charset) throws UnsupportedEncodingException  {
        return new String(bytes, offset, length, charset);
    }

}
