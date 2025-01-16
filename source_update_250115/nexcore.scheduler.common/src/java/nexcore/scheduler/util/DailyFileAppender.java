/*
 * Copyright (c) 2007 SK C&C. All rights reserved.
 * 
 * This software is the confidential and proprietary information of SK C&C. You
 * shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with SK
 * C&C.
 */

package nexcore.scheduler.util;


import java.io.File;
import java.text.SimpleDateFormat;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

/**
 * <p>일자 순으로 로그를 남기는 {@link Appender}.</p> 
 * <p>날자 적용을 하지 않은 파일에 기록을 하며 rolling 시점마다 날자를 붙인 이름의 파일을 생성하는 Log4J의 {@link DailyRollingFileAppender} 와는 달리,
 * {@link DailyFileAppender}는 처음 기록할 때 부터 날자를 부여한 파일명의 파일에 기록한다. 즉
 * <ul><li>app.log.20101225 : 현재가 12월 25일이라면 이 파일에 기록중</li>
 * <li>app.log.20101224 : 12월 24일에는 이 파일에 기록했었음</li>
 * <li>app.log.xxxxxxxx ... </li></ul> </p>
 * <p>날자파일명의 부여는 {@link SimpleDateFormat}의 문법을 그대로 활용한다.
 * 즉 app.log.20101225 와 같이 파일명이 정해지게 하고 싶다면 다음과 같이 log4j 설정을 정한다.
 * <code>(XML 의 예)<br>
 * &lt;appender name="file1" class="log.datedappender.DailyFileAppender"&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;      &lt;param name="encoding" value="UTF-8" /&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;      &lt;param name="Directory" value="c:/tmp/4" /&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;      &lt;param name="FileNamePattern" value="'trlog.log.'yyyyMMdd-HHmm" /&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;      &lt;layout class="org.apache.log4j.PatternLayout"&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;      &lt;param name="ConversionPattern" value="%d%p : %m%n" /&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;      &lt;/layout&gt; <br>
 * &lt;/appender&gt; <br>
 * </code></p>
 * <p>다른 {@link Appender}가 가지지 않는 이 클래스만의 속성의 의미는 다음과 같다.
 * <table border="1">
 * <tr><td><b>Name</b></td><td><b>Description</b></td></tr>
 * <tr><td>Directory</td><td>로그 파일이 저장될 디렉토리. (절대경로)</td></tr>
 * <tr><td>FileNamePattern</td><td>저장될 로그 파일 형식. {@link SimpleDateFormat} 이 지원하는 형식으로 기록해야 하며, 일반 문자열들은 ''로 감싸야 함.<br>
 * 이 값에 따라 로깅 파일명이 바뀌는 주기가 결정됨. (주기는 분, 시, 반일(오전/오후), 일, 월, 년 들이 가능)</td></tr>
 * </table>
 * 
 * </p>
 * <p>이 소스는 다음 소스를 기본으로 하여 적절히 수정한 것이다.<br>
 * 원 소스 이름 : DatedFileAppender version 1.0.2 <br>
 * 원 소스 저자 : Geoff Mottram<br>
 * 원 소스 작성일 : August 28, 2004<br>
 * 출처 : <a href="http://www.minaret.biz/tips/datedFileAppender.html">http://www.minaret.biz/tips/datedFileAppender.html</a>
 * </p>
 * 
 * @author 이상은
 * @modify 2012.04.18 jihwancha 상대경로 허용하도록 수정, 경로 미지정시 사용자경로를 사용하도록 수정 
 */
public class DailyFileAppender extends FileAppender {

    /**
     * 로그 파일이 저장될 디렉토리로서 사용자가 지정하는 값.
     */
    private String m_directory = null;

    /**
     * 로그 파일이 저장될 디렉토리의 절대경로. 내부에서 사용하는 값.
     */
    private File m_path = null;

    /**
     * date pattern. 
     */
    private String fileNamePattern;

    private DailyRollingFileName m_filename;

    // ----------------------------------------------------------- Constructors

    /** 
     * 기본 생성자.
     */
    public DailyFileAppender() {
    }

    /** 
     * 디렉토리를 부여한 생성자.
     * 
     * @param directory the directory in which log files are created.
     */
    public DailyFileAppender(String directory) {
    	setDirectory(directory);
    	activateOptions();
    }

    // ------------------------------------------------------------- Properties

    /**
     * 사용자 지정 로깅 위치(디렉토리)를 반환한다.  
     */
    public String getDirectory() {
        return m_directory;
    }

    /**
     * 사용자 지정 로깅 위치(디렉토리)를 설정한다.
     *
     * @param directory The new log file directory
     */
    public void setDirectory(String directory) {
        m_directory = directory;
    }
    
    /**
     * 실제 사용되는 로깅 위치(디렉토리)를 반환한다.
     */
    public String getRealDirectory(){
        return m_path == null ? null : m_path.getAbsolutePath();
    }

    /**
     * 파일이름 패턴을 "'Application.log.'yyyy-MM-dd" 과 같이 정한다.
     *
     * @param pattern
     */
    public void setFileNamePattern(String pattern){
        this.fileNamePattern = pattern;
    }

    /**
     * 파일이름 패턴을 반환한다. 제한된 용법으로만 사용을 권장함.
     * @return
     */
    public String getFileNamePattern(){
        return this.fileNamePattern;
    }
    
    // --------------------------------------------------------- Public Methods


    /**
     * {@link Appender}를 위한 모든 옵션이 설정되었을 때 호출되는 메소드.
     * 초기화를 위해 필요한 제반 작업을 수행한다.
     */
    public void activateOptions() {
        if ((m_directory == null) || (m_directory.length() == 0)) {
            String base = System.getProperty("user.dir");
            if(base == null || base.trim().length() < 1){
                base = System.getProperty("java.io.tmpdir");
            }
            if(base == null || base.trim().length() < 1){
                base = "/tmp";
            }
            LogLog.warn("File directory undefined. " + "Try that from '"+base+"' directory...");
            if (base != null) {
                m_path = new File(base);
            }   
        }
        else {
            m_path = new File(m_directory);
        }
        
        m_path.mkdirs();
        if (m_path.canWrite()) {
//            m_calendar = Calendar.getInstance();        // initialized
            m_filename = new DailyRollingFileName(fileNamePattern);
        }
    }

    /**
     * {@link AppenderSkeleton}의 doAppend() 메소드에 의해 호출되는 메소드.
     * 주어진 레이아웃에 따라 메시지를 로그한다.
     */
    public void append(LoggingEvent event) {
	    if(this.layout == null) {
	        errorHandler.error("No layout set for the appender named ["+ name+"].");
	        return;
	    }
	    if (this.m_filename == null) {
	        errorHandler.error("Improper initialization for the appender named ["+ name+"].");
	        return;
	    }
	    
	    if(m_filename.rollover(System.currentTimeMillis())){
	    	File newFile = new File(m_path, m_filename.getFileName());
	        this.fileName = newFile.getAbsolutePath();
	        super.activateOptions();            // close current file and open new file
	    }
	    if(this.qw == null) {               // should never happen
	        errorHandler.error("No output stream or file set for the appender named ["+ name+"].");
	        return;
	    }
	    subAppend(event);
    }

}

