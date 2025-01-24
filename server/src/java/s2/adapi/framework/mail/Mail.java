package s2.adapi.framework.mail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.util.ByteArrayDataSource;

import org.apache.commons.mail2.core.EmailException;
import org.apache.commons.mail2.jakarta.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import s2.adapi.framework.exception.ApplicationException;
import s2.adapi.framework.vo.ValueObject;


/**
 * <p/>
 * 메일 자체를 나타내는 클래스로 MultiPartBody를 지원한다.(Text, Image 및 첨부파일등)<br>
 * </p>
 *
 * @author kimhd
 * @version 
 * @since 1.0
 */

public class Mail {
    private static final String DEFAULT_CHARSET = "EUC-KR";
    
    private String mailCharset = DEFAULT_CHARSET;

    private static final Logger log = LoggerFactory.getLogger(Mail.class);

    private HtmlEmail htmlEmail = new HtmlEmail();

    /**
     * <p/>
     * 기본생성자이며, 기본적으로 첨부파일 없음, charset을 euc-kr로 지정한다.<br>
     * </p>
     */
    public Mail() {
        htmlEmail.setCharset(DEFAULT_CHARSET);
        htmlEmail.setBoolHasAttachments(false);
    }

    /**
     * <p/>
     * 생성시 사용할 메일서버에 대한 정보를 얻어서 생성한다.<br>
     * 기본적으로 문자셋은 "euc-kr", 첨부파일 없음으로 설정된다.<br>
     * </p>
     *
     * @param hostname <code>String</code>사용할 메일서버 주소
     * @param username <code>String</code>사용할 메일서버의 계정
     * @param pwd      <code>String</code>계정에 대한 비밀번호
     */
    public Mail(String hostname, String username, String pwd) {
        htmlEmail.setCharset(DEFAULT_CHARSET);
        htmlEmail.setBoolHasAttachments(false);
        htmlEmail.setHostName(hostname);
        htmlEmail.setAuthentication(username, pwd);
    }

    /**
     * <p/>
     * 메일서버로 사용할 호스트를 셋팅한다.<br>
     * </p>
     *
     * @param hostname <code>String</code>사용할 메일서버 주소
     */
    public void setHostName(String hostname) {
        htmlEmail.setHostName(hostname);
    }

    /**
     * <p/>
     * 사용할 메일서버에 대한 인증을 설정한다.<br>
     * </p>
     *
     * @param username <code>String</code>사용할 메일서버의 계정
     * @param pwd      <code>String</code>게정에 대한 비밀번호
     */
    public void setAuthentication(String username, String pwd) {
        htmlEmail.setAuthentication(username, pwd);
    }

    /**
     * <p/>
     * 문자셋을 설정한다. 기본값은 "euc-kr"이다.<br>
     * </p>
     * <p/>
     * {@link java.nio.charset.Charset } 참고
     *
     * @param charset <code>String</code>문자셋이 종류
     */
    public void setCharset(String charset) {
        this.mailCharset = charset;
        htmlEmail.setCharset(this.mailCharset);
    }

    /**
     * <p/>
     * 현재 설정된 문자셋을 얻어온다. 기본값은 "euc-kr"이다.<br>
     * </p>
     *
     * @return <code>String</code>
     */
    public String getCharset() {
        return this.mailCharset;
    }


    /**
     * <p/>
     * 보내는 이의 메일주소를 셋팅한다.<br>
     * </p>
     *
     * @param fromAddress <code>String</code> 보내는 이의 이메일 주소
     *
     * @throws ApplicationException 주소형식이 옳지 않을 경우 발생
     */
    public void setFrom(String fromAddress) throws ApplicationException {
        try {
            htmlEmail.setFrom(fromAddress);
        } catch (EmailException ee) {
            throw new ApplicationException("service.error.10000", ee);
        }
    }

    /**
     * <p/>
     * 보내는 이의 메일주소와 이름을 셋팅한다.<br>
     * </p>
     *
     * @param fromAddress <code>String</code> 보내는 이의 이메일 주소
     * @param fromName    <code>String</code> 보내는 이의 이름
     *
     * @throws ApplicationException 주소형식이 옳지 않을 경우 발생
     */
    public void setFrom(String fromAddress, String fromName) throws ApplicationException {
        try {
            htmlEmail.setFrom(fromAddress, fromName);
        } catch (EmailException ee) {
            throw new ApplicationException("service.error.10000", ee);
        }
    }

    /**
     * <p/>
     * 받는 사람의 메일 주소를 추가한다.<br>
     * </p>
     *
     * @param toAddress <code>String</code> 받는 이의 이메일 주소
     *
     * @return {@link s2.adapi.framework.mail.Mail }
     *
     * @throws ApplicationException 주소형식이 옳지 않을 경우 발생
     */
    public Mail addTo(String toAddress) throws ApplicationException {
        try {
            htmlEmail.addTo(toAddress);
        } catch (EmailException ee) {
            throw new ApplicationException("service.error.10000", ee);
        }

        return this;
    }

    /**
     * <p/>
     * 받는 사람의 메일 주소와 이름을 추가한다.<br>
     * </p>
     *
     * @param toAddress <code>String</code> 받는 이의 이메일 주소
     * @param toName    <code>String</code> 받는 이의 이름
     *
     * @return {@link s2.adapi.framework.mail.Mail }
     *
     * @throws ApplicationException 주소형식이 옳지 않을 경우 발생
     */
    public Mail addTo(String toAddress, String toName) throws ApplicationException {
        try {
            htmlEmail.addTo(toAddress, toName);
        } catch (EmailException ee) {
            throw new ApplicationException("service.error.10000", ee);
        }

        return this;
    }

    /**
     * <p/>
     * 받는 사람의 메일 주소 리스트를 설정한다.<br>
     * {@link java.util.Collection } 타입으로 메일주소 리스트를 설정하면 내부에서 {@link jakarta.mail.internet.InternetAddress } 형태로 변환해서 처리한다.
     * </p>
     *
     * @param toList {@link java.util.Collection } 받는 이의 이메일 주소 리스트
     *
     * @return {@link s2.adapi.framework.mail.Mail }
     *
     * @throws ApplicationException 주소형식이 옳지 않을 경우 발생
     */
    public Mail setTo(Collection<String> toList) throws ApplicationException {
        List<InternetAddress> internetAddress = 
        		new ArrayList<InternetAddress>(toList.size());
 
        for (Iterator<String> i = toList.iterator(); i.hasNext();) {
            try {
                internetAddress.add(new InternetAddress(i.next()));
            } catch (AddressException ae) {
                throw new ApplicationException("service.error.10001", ae);
            }
        }

        try {
            htmlEmail.setTo(internetAddress);
        } catch (EmailException ee) {
            throw new ApplicationException("service.error.10000", ee);
        }

        return this;
    }

    /**
     * <p/>
     * 받는 사람의 이메일 주소와 이름을 가지는 {@link s2.adapi.framework.vo.ValueObject} 로 메일 주소를 설정한다.<br>
     * {@link java.util.Collection } 타입으로 메일주소 리스트를 설정하면 내부에서 {@link jakarta.mail.internet.InternetAddress } 형태로 변환해서 처리한다.
     * 이 때, <code>ValueObjce</code>의 key는 받는 사람의 이메일주소이며 value는 받는 사람의 이름이다.<br>
     * 예) ValueObject toList = new ValueObject();<br>
     * toList.set("amugae1@mailserver.com", "아무개1");<br>
     * toList.set("amugae2@mailserver.com", "아무개2");<br>
     * toList.set("amugae3@mailserver.com", "아무개3");<br>
     * Mail m_mail = new Mail();<br>
     * m_mail.setTo(toList);<br>
     * </p>
     *
     * @param toListVo {@link s2.adapi.framework.vo.ValueObject } 받는 이의 이메일 주소와 이름
     *
     * @return {@link s2.adapi.framework.mail.Mail }
     *
     * @throws ApplicationException 주소형식이 옳지 않을 경우 발생
     */
    public Mail setTo(ValueObject toListVo) throws ApplicationException {
        for (Iterator<String> i = toListVo.get(0).keySet().iterator(); i.hasNext();) {
            String toAddress = i.next();
            String toName = toListVo.getString(toAddress);
            this.addTo(toAddress, toName);
        }

        return this;
    }

    /**
     * <p/>
     * 받는 사람의 이메일 주소를 가지는 {@link s2.adapi.framework.vo.ValueObject} 로 메일 주소를 설정한다.<br>
     * 이 때 받는 사람의 이메일주소가 담긴 Key를 지정해준다.<br>
     * 사용예)
     * <pre>
     * ValueObject toListVO = new ValueObject();
     * toListVO.set(0,"addr","amugae1@mailserver.com");
     * toListVO.set(1,"addr","amugae2@mailserver.com");
     * toListVO.set(2,"addr","amugae3@mailserver.com");
     * Mail m_mail = new Mail();
     * m_mail.setTo(toListVO,"addr");
     * </pre>
     * @param toListVo {@link s2.adapi.framework.vo.ValueObject } 받는 이의 이메일 주소
     * @param addrKey 받는이의 이메일 주소가 담긴 컬럼의 컬럼명
     * @return {@link s2.adapi.framework.mail.Mail }
     * @throws ApplicationException 주소형식이 옳지 않을 경우 발생
     */
    public Mail setTo(ValueObject toListVO,String addrKey) throws ApplicationException {
    	return setTo(toListVO,addrKey,null);
    }
    
    /**
     * <p/>
     * 받는 사람의 이메일 주소와 이름을 가지는 {@link s2.adapi.framework.vo.ValueObject} 로 메일 주소를 설정한다.<br>
     * 이 때 받는 사람의 이메일주소와 받는 사람의 이름이 담긴 Key를 각각 지정해준다.<br>
     * 사용예)
     * <pre>
     * ValueObject toListVO = new ValueObject();
     * toListVO.set(0,"addr","amugae1@mailserver.com");
     * toListVO.set(0,"name","아무개1");
     * toListVO.set(1,"addr","amugae2@mailserver.com");
     * toListVO.set(1,"name","아무개2");
     * toListVO.set(2,"addr","amugae3@mailserver.com");
     * toListVO.set(2,"name","아무개3");
     * Mail m_mail = new Mail();
     * m_mail.setTo(toListVO,"addr","name");
     * </pre>
     *
     * @param toListVo {@link s2.adapi.framework.vo.ValueObject } 받는 이의 이메일 주소와 이름
     * @param addrKey 받는이의 이메일 주소가 담긴 컬럼의 컬럼명
     * @param nameKey 받는이의 이름이 담긴 컬럼의 컬럼명
     * @return {@link s2.adapi.framework.mail.Mail }
     * @throws ApplicationException 주소형식이 옳지 않을 경우 발생
     */
    public Mail setTo(ValueObject toListVO,String addrKey, String nameKey) 
    		throws ApplicationException {
    	for(int i=0;i<toListVO.size();i++) {
    		String toAddress = toListVO.getString(i,addrKey);
			if (nameKey == null) {
				this.addTo(toAddress);
			} else {
				String toName = toListVO.getString(i,nameKey);
				this.addTo(toAddress, toName);
			}
    	}
    	
    	return this;
    }
    
    /**
     * <p/>
     * 참조 이메일 주소를 추가한다.<br>
     * </p>
     *
     * @param ccAddress <code>String</code> 참조 대상 이메일 주소
     *
     * @return {@link s2.adapi.framework.mail.Mail }
     *
     * @throws ApplicationException 주소형식이 옳지 않을 경우 발생
     */
    public Mail addCC(String ccAddress) throws ApplicationException {
        try {
            htmlEmail.addCc(ccAddress);
        } catch (EmailException ee) {
            throw new ApplicationException("service.error.10000", ee);
        }

        return this;
    }

    /**
     * <p/>
     * 참조 이메일 주소를 추가한다.<br>
     * </p>
     *
     * @param ccAddress <code>String</code> 참조 대상 이메일 주소
     * @param ccName    <code>String</code> 참조 대상자 이름
     *
     * @return {@link s2.adapi.framework.mail.Mail }
     *
     * @throws ApplicationException 주소형식이 옳지 않을 경우 발생
     */
    public Mail addCC(String ccAddress, String ccName) throws ApplicationException {
        try {
            htmlEmail.addCc(ccAddress, ccName);
        } catch (EmailException ee) {
            throw new ApplicationException("service.error.10000", ee);
        }

        return this;
    }

    /**
     * <p/>
     * 참조 이메일 주소 리스트를 설정한다.<br>
     * {@link java.util.Collection } 타입으로 메일주소 리스트를 설정하면 내부에서 {@link jakarta.mail.internet.InternetAddress } 형태로 변환해서 처리한다.
     * </p>
     *
     * @param ccList {@link java.util.Collection } 참조 이메일 주소 리스트
     *
     * @return {@link s2.adapi.framework.mail.Mail }
     *
     * @throws ApplicationException 주소형식이 옳지 않을 경우 발생
     */
    public Mail setCC(Collection<String> ccList) throws ApplicationException {
        ArrayList<InternetAddress> internetAddress = 
        		new ArrayList<InternetAddress>(ccList.size());

        for (Iterator<String> i = ccList.iterator(); i.hasNext();) {
            try {
                internetAddress.add(new InternetAddress(i.next()));
            } catch (AddressException ae) {
                throw new ApplicationException("service.error.10001", ae);
            }
        }

        try {
            htmlEmail.setCc(internetAddress);
        } catch (EmailException ee) {
            throw new ApplicationException("service.error.10000", ee);
        }

        return this;
    }

    /**
     * <p/>
     * 참조 이메일 주소와 참조 대상의 이름을 가지는 {@link s2.adapi.framework.vo.ValueObject} 로 메일 주소를 설정한다.<br>
     * {@link java.util.Collection } 타입으로 메일주소 리스트를 설정하면 내부에서 {@link jakarta.mail.internet.InternetAddress } 형태로 변환해서 처리한다.
     * 이 때, <code>ValueObjce</code>의 key는 참조대상의 이메일주소이며 value는 참조대상의 이름이다.<br>
     * 예) ValueObject ccList = new ValueObject();<br>
     * ccList.set("amugae1@mailserver.com", "참조1");<br>
     * ccList.set("amugae2@mailserver.com", "참조2");<br>
     * ccList.set("amugae3@mailserver.com", "참조3");<br>
     * Mail m_mail = new Mail();<br>
     * m_mail.setTo(ccList);<br>
     * </p>
     *
     * @param ccListVo {@link s2.adapi.framework.vo.ValueObject } 받는 이의 이메일 주소와 이름
     *
     * @return {@link s2.adapi.framework.mail.Mail }
     *
     * @throws ApplicationException 주소형식이 옳지 않을 경우 발생
     */
    public Mail setCC(ValueObject ccListVo) throws ApplicationException {
        for (Iterator<String> i = ccListVo.get(0).keySet().iterator(); i.hasNext();) {
            String ccAddress = i.next();
            String ccName = ccListVo.getString(ccAddress);
            this.addCC(ccAddress, ccName);
        }

        return this;
    }

    /**
     * <p/>
     * 참조 이메일 주소를 가지는 {@link s2.adapi.framework.vo.ValueObject} 로 참조자의 메일 주소를 설정한다.<br>
     * 이 때 참조자의 이메일주소가 담긴 Key를 지정해준다.<br>
     * 사용예)
     * <pre>
     * ValueObject ccListVO = new ValueObject();
     * ccListVO.set(0,"addr","amugae1@mailserver.com");
     * ccListVO.set(1,"addr","amugae2@mailserver.com");
     * ccListVO.set(2,"addr","amugae3@mailserver.com");
     * Mail m_mail = new Mail();
     * m_mail.setCC(ccListVO,"addr");
     * </pre>
     * @param ccListVo {@link s2.adapi.framework.vo.ValueObject } 참조 이메일 주소
     * @param addrKey 참조 이메일 주소가 담긴 컬럼의 컬럼명
     * @return {@link s2.adapi.framework.mail.Mail }
     * @throws ApplicationException 주소형식이 옳지 않을 경우 발생
     */
    public Mail setCC(ValueObject ccListVO,String addrKey) throws ApplicationException {
    	return setCC(ccListVO,addrKey,null);
    }
    
    /**
     * <p/>
     * 참조 이메일 주소와 이름을 가지는 {@link s2.adapi.framework.vo.ValueObject} 로 참조자의 메일 주소를 설정한다.<br>
     * 이 때 참조자의 이메일주소와 참조자의 이름이 담긴 Key를 각각 지정해준다.<br>
     * 사용예)
     * <pre>
     * ValueObject ccListVO = new ValueObject();
     * ccListVO.set(0,"addr","amugae1@mailserver.com");
     * ccListVO.set(0,"name","아무개1");
     * ccListVO.set(1,"addr","amugae2@mailserver.com");
     * ccListVO.set(1,"name","아무개2");
     * ccListVO.set(2,"addr","amugae3@mailserver.com");
     * ccListVO.set(2,"name","아무개3");
     * Mail m_mail = new Mail();
     * m_mail.setCC(ccListVO,"addr","name");
     * </pre>
     *
     * @param ccListVo {@link s2.adapi.framework.vo.ValueObject } 참조자의 이메일 주소와 이름
     * @param addrKey 참조 이메일 주소가 담긴 컬럼의 컬럼명
     * @param nameKey 참조자의 이름이 담긴 컬럼의 컬럼명
     * @return {@link s2.adapi.framework.mail.Mail }
     * @throws ApplicationException 주소형식이 옳지 않을 경우 발생
     */
    public Mail setCC(ValueObject ccListVO,String addrKey, String nameKey) 
			throws ApplicationException {
		for(int i=0;i<ccListVO.size();i++) {
			String toAddress = ccListVO.getString(i,addrKey);
			if (nameKey == null) {
				this.addCC(toAddress);
			} else {
				String toName = ccListVO.getString(i,nameKey);
				this.addCC(toAddress, toName);
			}
		}

		return this;
    }
    
    /**
     * <p/>
     * 숨은 참조 이메일 주소를 추가한다.<br>
     * </p>
     *
     * @param bccAddress <code>String</code> 숨은 참조 이메일 주소
     *
     * @return {@link s2.adapi.framework.mail.Mail }
     *
     * @throws ApplicationException 주소형식이 옳지 않을 경우 발생
     */
    public Mail addBCC(String bccAddress) throws ApplicationException {
        try {
            htmlEmail.addBcc(bccAddress);
        } catch (EmailException ee) {
            throw new ApplicationException("service.error.10000", ee);
        }

        return this;
    }

    /**
     * <p/>
     * 숨은 참조 이메일 주소와 숨은 참조 대상의 이름를 추가한다.<br>
     * </p>
     *
     * @param bccAddress <code>String</code> 숨은 참조 이메일 주소
     * @param bccName    <code>String</code>  숨은 참조대상의 이름
     *
     * @return {@link s2.adapi.framework.mail.Mail }
     *
     * @throws ApplicationException 주소형식이 옳지 않을 경우 발생
     */
    public Mail addBCC(String bccAddress, String bccName) throws ApplicationException {
        try {
            htmlEmail.addBcc(bccAddress, bccName);
        } catch (EmailException ee) {
            throw new ApplicationException("service.error.10000", ee);
        }

        return this;
    }

    /**
     * <p/>
     * 숨은 참조 이메일 주소 리스트를 셋팅한다.<br>
     * {@link java.util.Collection } 타입으로 메일주소 리스트를 설정하면 내부에서 {@link jakarta.mail.internet.InternetAddress } 형태로 변환해서 처리한다.<br>
     * </p>
     *
     * @param bccList {@link java.util.Collection } 숨은 참조 대상의 이메일 리스트
     *
     * @return {@link s2.adapi.framework.mail.Mail }
     *
     * @throws ApplicationException 주소형식이 옳지 않을 경우 발생
     */
    public Mail setBCC(Collection<String> bccList) throws ApplicationException {
        ArrayList<InternetAddress> internetAddress = 
        		new ArrayList<InternetAddress>(bccList.size());

        for (Iterator<String> i = bccList.iterator(); i.hasNext();) {
            try {
                internetAddress.add(new InternetAddress(i.next()));
            } catch (AddressException ae) {
                throw new ApplicationException("service.error.10001", ae);
            }
        }

        try {
            htmlEmail.setBcc(internetAddress);
        } catch (EmailException ee) {
            throw new ApplicationException("service.error.10000", ee);
        }

        return this;
    }

    /**
     * <p/>
     * 숨은 참조 이메일 주소와 숨은 참조 대상의 이름을 가지는 {@link s2.adapi.framework.vo.ValueObject} 로 메일 주소를 설정한다.<br>
     * {@link java.util.Collection } 타입으로 메일주소 리스트를 설정하면 내부에서 {@link jakarta.mail.internet.InternetAddress } 형태로 변환해서 처리한다.
     * 이 때, <code>ValueObjce</code>의 key는 숨은 참조대상의 이메일주소이며 value는 참조대상의 이름이다.<br>
     * 예) ValueObject bccList = new ValueObject();<br>
     * bccList.set("amugae1@mailserver.com", "참조1");<br>
     * bccList.set("amugae2@mailserver.com", "참조2");<br>
     * bccList.set("amugae3@mailserver.com", "참조3");<br>
     * Mail m_mail = new Mail();<br>
     * m_mail.setTo(bccList);<br>
     * </p>
     *
     * @param bccListVo {@link s2.adapi.framework.vo.ValueObject } 숨은 참조 이메일 주소와 이름
     *
     * @return {@link s2.adapi.framework.mail.Mail }
     *
     * @throws ApplicationException 주소형식이 옳지 않을 경우 발생
     */
    public Mail setBCC(ValueObject bccListVo) throws ApplicationException {
        for (Iterator<String> i = bccListVo.get(0).keySet().iterator(); i.hasNext();) {
            String bccAddress = i.next();
            String bccName = bccListVo.getString(bccAddress);
            this.addBCC(bccAddress, bccName);
        }

        return this;
    }

    /**
     * <p/>
     * 메일의 제목을 설정한다.<br>
     * </p>
     *
     * @param subject <code>String</code> 메일 제목
     *
     * @return {@link s2.adapi.framework.mail.Mail }
     */
    public Mail setSubject(String subject) {
        htmlEmail.setSubject(subject);
        return this;
    }

    /**
     * <p/>
     * 메일의 내용을 설정한다.<br>
     * </p>
     *
     * @param msg <code>String</code> 메일 내용
     *
     * @return {@link s2.adapi.framework.mail.Mail }
     *
     * @throws ApplicationException 메일 본문형식이 옳지 않을 경우 발생<br>
     *                       참고 : {@link jakarta.mail.internet.MimeBodyPart}
     */
    public Mail setMsg(String msg) throws ApplicationException {
        try {
            htmlEmail.setMsg(msg);
        } catch (EmailException ee) {
            throw new ApplicationException("service.error.10002", ee);
        }

        return this;
    }
    
    /**
     * <p/>
     * HTML 형식의 메일의 내용을 설정한다.<br>
     * </p>
     *
     * @param msg <code>String</code> 메일 내용
     *
     * @return {@link s2.adapi.framework.mail.Mail }
     *
     * @throws ApplicationException 메일 본문형식이 옳지 않을 경우 발생<br>
     *                       참고 : {@link jakarta.mail.internet.MimeBodyPart}
     */
    public Mail setHtmlMsg(String msg) throws ApplicationException {
        try {
            htmlEmail.setHtmlMsg(msg);
        } 
        catch (EmailException ee) {
            throw new ApplicationException("service.error.10002", ee);
        }

        return this;
    }

    /**
     * <p/>
     * 메일의 제목과 내용을 설정한다.<br>
     * </p>
     *
     * @param subject <code>String</code> 메일 제목
     * @param msg     <code>String</code> 메일 내용
     *
     * @return {@link s2.adapi.framework.mail.Mail}
     *
     * @throws ApplicationException 메일 본문형식이 옳지 않을 경우 발생<br>
     *                       참고 : {@link jakarta.mail.internet.MimeBodyPart}
     */
    public Mail setContext(String subject, String msg) throws ApplicationException {
        this.setSubject(subject);
        this.setMsg(msg);

        return this;
    }

    /**
     * <p/>
     * 작성된 메일을 전송한다.<br>
     * </p>
     *
     * @throws ApplicationException 메일 전송중에 오류발생 시
     * @see jakarta.mail.MessagingException
     */
    public void send() throws ApplicationException {
        try {
            htmlEmail.send();
        } catch (EmailException ee) {
        	ee.printStackTrace();
            throw new ApplicationException("service.error.10003", ee);
        }

        if (log.isInfoEnabled()) {
            Date date = htmlEmail.getSentDate();
            log.info("Mail sent... at < " + date.toString() + " >");
        }
    }

    /**
     * <p/>
     * {@link s2.adapi.framework.mail.MailAttachment } 개체로 정의한 첨부파일을 첨주한다.<br>
     * </p>
     *
     * @param attachment {@link s2.adapi.framework.mail.MailAttachment } 미리 정의된 첨부파일 개체
     *
     * @return {@link s2.adapi.framework.mail.Mail }
     *
     * @throws ApplicationException attachment가 NULL 이거나 파일 첨부 중 오류가 발생했을 경우
     */
    public Mail attach(MailAttachment attachment) throws ApplicationException {
        if (attachment == null) {
            throw new ApplicationException("service.error.10005");
        }

        try {
            htmlEmail.attach(attachment.getAttachment());
        } 
        catch (EmailException ee) {
            throw new ApplicationException("service.error.10006", ee);
        }

        return this;
    }

    /**
     * <p/>
     * {@link s2.adapi.framework.mail.MailAttachment} 개체로 정의한 첨부파일 리스트 {@link java.util.Collection } 를 이용해서 여러개의 첨부파일을 한번에 첨부한다.
     *
     * @param attachList {@link java.util.Collection } 첨부파일 리스트
     *
     * @return {@link s2.adapi.framework.mail.Mail }
     *
     * @throws ApplicationException attachList가 NULL이가너 파일 첨부 중 오류가 발생한 경우
     */
    public Mail attach(Collection<MailAttachment> attachList) throws ApplicationException {
        if (attachList.size() < 1 || attachList == null) {
            throw new ApplicationException("service.error.10005");
        }

        try {
            for (Iterator<MailAttachment> i = attachList.iterator(); i.hasNext();) {
                htmlEmail.attach(i.next().getAttachment());
            }
        } 
        catch (EmailException ee) {
            throw new ApplicationException("service.error.10006", ee);
        }

        return this;
    }

    public Mail attach(byte[] attach, String name, String contentType) throws ApplicationException {
        if (attach == null || attach.length <= 0) {
            throw new ApplicationException("service.error.10005");
        }

        if (contentType == null) {
        	contentType = "application/octet-stream";
        }
        
        try {
        	ByteArrayDataSource ds = new ByteArrayDataSource(attach,contentType);
        	
            htmlEmail.attach(ds,name,"",jakarta.mail.Part.ATTACHMENT);
        } 
        catch (EmailException ee) {
            throw new ApplicationException("service.error.10006", ee);
        }

        return this;
    }
    
    
}
