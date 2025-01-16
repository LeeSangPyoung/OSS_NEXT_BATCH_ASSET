package nexcore.scheduler.monitor.internal;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;

import nexcore.framework.supports.EncryptionUtils;
import nexcore.scheduler.entity.JobNotifySendInfo;
import nexcore.scheduler.exception.SchedulerException;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.monitor.IJobEndNotifySender;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.DateUtil;
import nexcore.scheduler.util.Util;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어 </li>
 * <li>설  명 : Job End 통지 대상 중 Email 인 건들을 send 함 </li>
 * <li>작성일 : 2011. 4. 7.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobEndNotifyEmailSender implements IJobEndNotifySender {
	private boolean         enable;

	private String 			smtpHost;
	private int             smtpPort = 25;        // 기본값 25
	private boolean         smtpAuth = true;      // 인증
	private boolean    		smtpSsl  = false;     // 기본 nossl
	private String     		smtpUser;
	private String     		smtpPasswd;
	private String          defaultSender;        // 기본 전송 주소
	private String          subjectEncoding;      // 제목 인코딩
	private String          contentType;          // 본문 contenttype
	
	private String          templateFilename;     // 본문 템플릿
	private String          templateFileEncoding; // 본문 템플릿 인코딩
	
	// -------------
	
	private Properties      smtpConnectProperties;

	private File 			templateFile;
    private long 			templateFileLastLoadedTime;
    private String          templateSubjectString;
    private String          templateContentString;
	
    private Log             log;
    
	/*
		online.email.smtp.host=192.1.1.205
		online.email.smtp.port=25
		online.email.smtp.auth=true
		online.email.smtp.ssl=false
		online.email.smtp.user=cbscbm
		online.email.smtp.password=cbscbm
		online.email.smtp.defaultsender=system@solomonbank.com
	   "text/html; charset=" + encoding
	 */
	
	public void init() {
		log = LogManager.getSchedulerLog();
		smtpConnectProperties = getConnectProperties();
		
		templateFile = new File(templateFilename);
		if (!templateFile.exists()) {
			throw new SchedulerException("main.jobnotify.mail.template.file.error", templateFile.getAbsolutePath());
		}
	}
	
	public void destroy() {
	}
	
	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public String getSmtpHost() {
		return smtpHost;
	}

	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	public int getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(int smtpPort) {
		this.smtpPort = smtpPort;
	}

	public boolean isSmtpAuth() {
		return smtpAuth;
	}

	public void setSmtpAuth(boolean smtpAuth) {
		this.smtpAuth = smtpAuth;
	}

	public boolean isSmtpSsl() {
		return smtpSsl;
	}

	public void setSmtpSsl(boolean smtpSsl) {
		this.smtpSsl = smtpSsl;
	}

	public String getSmtpUser() {
		return smtpUser;
	}

	public void setSmtpUser(String smtpUser) {
		this.smtpUser = smtpUser;
	}

	public String getSmtpPasswd() {
		return smtpPasswd;
	}

	public void setSmtpPasswd(String smtpPasswd) {
//        if (!Util.isBlank(smtpPasswd) && (smtpPasswd.contains("{DES}") || smtpPasswd.contains("{AES}"))) {
        if (!Util.isBlank(smtpPasswd)) {
            this.smtpPasswd = EncryptionUtils.decode(smtpPasswd);
        }else {
            this.smtpPasswd = smtpPasswd;
        }
	}

	public String getDefaultSender() {
		return defaultSender;
	}

	public void setDefaultSender(String defaultSender) {
		this.defaultSender = defaultSender;
	}

	public String getSubjectEncoding() {
		return subjectEncoding;
	}

	public void setSubjectEncoding(String subjectEncoding) {
		this.subjectEncoding = subjectEncoding;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getTemplateFilename() {
		return templateFilename;
	}

	public void setTemplateFilename(String templateFilename) {
		this.templateFilename = templateFilename;
	}
	
	public String getTemplateFileEncoding() {
		return templateFileEncoding;
	}
	
	public void setTemplateFileEncoding(String templateFileEncoding) {
		this.templateFileEncoding = templateFileEncoding;
	}
	
	// =========================================================================================

	public void sendMail(String fromAddr, String toAddr, String subject, String content) {
        Authenticator authenticator = null;
        if (smtpAuth) {
            authenticator = new Authenticator() {
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUser, smtpPasswd);
                }
            };
        }
        
        Session session = Session.getInstance(smtpConnectProperties, authenticator);

    	MimeMessage  message = new MimeMessage(session);
        
        // 메일 작성
        try {
            // 송신자 설정
            message.setFrom(new InternetAddress(fromAddr) );
                        
            // 수신자 설정
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddr) );
            
            // 제목 설정
            message.setSubject(subject, subjectEncoding);
            
            // 내용 설정
//            message.setHeader("Content-Type", "text/html");
            message.setContent(content, contentType);
//            "text/html; charset=" + encoding
            
            // 메시지 저장
            message.saveChanges();
            
            Transport.send(message); 
            
            log.info("[JobEndNotifyEmailSender] send email to : "+toAddr);
            
        } catch (Exception e) {
        	throw new SchedulerException("main.jobnotify.send.mail.error", e);
        }
    }

	public void sendMailTest(String fromAddr, String toAddr, String subject, String content) {
		System.out.println("=======================");
		System.out.println("fromAddr="+fromAddr);
		System.out.println("toAddr="+toAddr);
		System.out.println("subject="+subject);
		System.out.println("content="+content);
		System.out.println("=======================");
    }

	private void checkAndLoadTemplateFile() {
    	if (templateFileLastLoadedTime < templateFile.lastModified()) {
    		 byte[] buffer = new byte[(int)templateFile.length()];
    		 DataInputStream in = null;
    		 try {
    			 in = new DataInputStream(new FileInputStream(templateFile));
    			 in.readFully(buffer);
    			 String s = new String(buffer, templateFileEncoding);
    			 
    			 templateSubjectString = s.substring(s.indexOf("SUBJECT:")+8, s.indexOf("CONTENT:"));
    			 templateContentString = s.substring(s.indexOf("CONTENT:")+8);
    			 
    			 templateFileLastLoadedTime = System.currentTimeMillis();
    			 
    		 }catch(Exception e) {
    			 throw new SchedulerException("main.jobnotify.mail.template.file.error", e, templateFile.getAbsolutePath());
    		 }finally {
    			 try { in.close(); }catch(Exception e) {}
    		 }
    	}
    }
    
	private long getExecutionRunningTime(Timestamp start, Timestamp end) {
		if (start == null || end == null) {
			return -1;
		}else {
			return end.getTime() - start.getTime();
		}
	}
	
    /**
     * main
     */
    /* 
     * 템플릿 파라미터 
     * {0}  : SCHEDULER ID
     * {1}  : JOB ID 
     * {2}  : JOB INSTANCE ID 
     * {3}  : JOB EXECUTION ID
     * {4}  : JOB DESC
     * {5}  : AGENT NODE
     * {6}  : JOB GROUP
     * {7}  : JOB OWNER
     * {8}  : JOB OWNER PHONE
     * {9}  : 실행 시작 시각
     * {10} : 실행 종료 시각
     * {11} : 실행 시간 ms
     * {12} : Operator ID
     * {13} : Operator 이름
     * {14} : Operator IP
     * {15} : OK (0) / FAIL (1) 
     * {16} : RETURN CODE
     * {17} : ERROR MSG
     * {18} : 에러 시각
     * 
     */
	public int doSend(List<JobNotifySendInfo> sendList) {
		if (!enable) {
			Util.logDebug(log, "SMTP Sender diabled. do not send.");
		}
		checkAndLoadTemplateFile(); // template 파일 변경됐는지 체크해서 다시 로드.
		
		if (sendList != null) {
			for (JobNotifySendInfo sendInfo : sendList) {
				try {
					Object[] params = new Object[]{
						System.getProperty("NEXCORE_ID"),
						sendInfo.getJobId(), sendInfo.getJobInstanceId(), sendInfo.getJobExecutionId(), sendInfo.getJobDesc(), sendInfo.getAgentNode(),
						sendInfo.getJobGroupId(), sendInfo.getOwner(), sendInfo.getOwnerTel(),
						sendInfo.getStartTime(), sendInfo.getEndTime(), getExecutionRunningTime(DateUtil.getTimestamp(sendInfo.getStartTime()), DateUtil.getTimestamp(sendInfo.getEndTime())), 
						sendInfo.getOperatorId(), sendInfo.getOperatorName(), sendInfo.getOperatorIp(), 
						sendInfo.getReturnCode()==0 ? 0 : 1, sendInfo.getReturnCode(), Util.nvl(sendInfo.getErrorMsg(),""), 
						sendInfo.getCreateTime()};
					
					String subject = MessageFormat.format(templateSubjectString, params);
					String content = MessageFormat.format(templateContentString, params);
					
					sendMail(defaultSender, sendInfo.getRecvPoint(), subject, content);
//					sendMailTest(defaultSender, sendInfo.getRecvPoint(), subject, content);
					
					sendInfo.setSendState("S");
				}catch(Exception e) {
					sendInfo.setSendState("F");
					Util.logError(log, MSG.get("main.jobnotify.send.mail.error")+"/"+sendInfo, e);
				}finally {
					sendInfo.setSendTime(DateUtil.getCurrentTimestampString());
					sendInfo.setTryCount(sendInfo.getTryCount()+1);
				}
			}
		}
		return 0;
	}
	
	private Properties getConnectProperties() {
        Properties props = new Properties();

        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", Integer.toString(smtpPort) );
        props.put("mail.smtp.auth", Boolean.toString(smtpAuth) );
        
        if (smtpSsl) {
            props.put("mail.transport.protocol", "smtps");
        } else {
            props.put("mail.transport.protocol", "smtp");
        }        
        
        if (smtpSsl) {
            props.put("mail.smtp.starttls.enable",        "true");
            props.put("mail.smtp.socketFactory.port",     Integer.toString(smtpPort) );
            props.put("mail.smtp.socketFactory.class",    "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
        }
        
        return props;
	}

}
