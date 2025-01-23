/*
 * Copyright(c) 2010 s2adapi Corporation. All rights reserved. 
 * http://www.s2adapi.com
 */
package s2.adapi.framework.mail;

import java.io.UnsupportedEncodingException;
import java.net.URL;

import jakarta.mail.internet.MimeUtility;

import org.apache.commons.mail.EmailAttachment;

import s2.adapi.framework.exception.ApplicationException;

/**
 * 첨부파일을 명세한 클래스
 *
 * @author 김기호(현대정보기술)
 * @version $Revision: 1.1 $ $Date: 2006/09/26 08:20:15 $
 * @since 3.0
 * @see EmailAttachment
 */

public class MailAttachment {
    private static final String DEFAULT_CHARSET = "EUC-KR";

    private EmailAttachment emailAttachment = null;
    private String mailCharset = DEFAULT_CHARSET;

    /**
     * //TODO : description이 반영되는 메일서버 종류?
     */
    //private String m_description = null;

    /**
     * <p>
     * 기본 생성자로 메일 첨부파일 개체를 생성한다.<br>
     * </p>
     */
    public MailAttachment() {
        emailAttachment = new EmailAttachment();
        emailAttachment.setDisposition(jakarta.mail.Part.ATTACHMENT);
    }

    /**
     * <p>
     * 첨부될 파일의 경로와 별칭으로 첨부파일 개체를 생성한다.<br>
     * 첨부될 파일의 경로는 c:\file\added.jpg 처름 실제 파일이름을 포함하는 full path이다<br>
     *
     * @param path <code>String</code> 첨부될 파일의 실제 위치(파일이름 포함)
     * @param name <code>String</code> 첨부될 파일의 별칭
     * @throws LiveException 첨부파일 별칭의 encoding 방식이 올바르지 않을 경우 발생
     */
    public MailAttachment(String path, String name) throws ApplicationException {
        emailAttachment = new EmailAttachment();
        emailAttachment.setDisposition(jakarta.mail.Part.ATTACHMENT);

        this.setPath(path);
        this.setName(name);
    }

    /**
     * <p>
     * 문자셋을 설정한다. default는 "EUC-KR"이다.<br>
     * {@link java.nio.charset.Charset } 참고
     * </p>
     *
     * @param charset <code>String</code> 설정할 문자셋
     */
    public void setCharset(String charset) {
        this.mailCharset = charset;
    }

    /**
     * <p>
     * 현재 설정된 문자셋을 얻어온다. default는  "EUC_KR"이다.<br>
     * </P>
     *
     * @return <code>String</code> 현재 설정된 문자셋
     */
    public String getCharset() {
        return this.mailCharset;
    }

    /**
     * <p>
     * 첨부할 파일의 절대경로를 입력한다.<br>
     * 첨부될 파일의 경로는 c:\file\added.jpg 처름 실제 파일이름을 포함하는 full path이다<br>
     * </p>
     *
     * @param path <code>String</code> 첨부될 파일의 실제 위치(파일이름 포함)
     */
    public void setPath(String path) {
        emailAttachment.setPath(path);
    }

    /**
     * <p>
     * 설정된 첨부 파일의 절대경로를 얻어온다.<br>
     * </p>
     *
     * @return <code>String</code> 설정된 첨부파일의 절대경로
     */
    public String getPath() {
        return emailAttachment.getPath();
    }

    /**
     * <p>
     * 첨부할 파일의 URL을 설정한다.<br>
     * </p>
     *
     * @param url {@link java.net.URL } 첨부할 파일이 위치한 URL
     */
    public void setURL(URL url) {
        emailAttachment.setURL(url);
    }

    /**
     * <p>
     * 설정된 첨부 파일의 URL을 얻어온다.<br>
     * </p>
     *
     * @return {@link java.net.URL }  설정된 첨부파일의 URL
     */
    public URL getURL() {
        return emailAttachment.getURL();
    }

    /**
     * <p>
     * 첨부파일의 별칭을 설정한다.<br>
     * </p>
     *
     * @param name <code>String</code> 첨부파일의 별칭으로 받는 이에게 첨부파일이름으로 보여진다.
     * @throws LiveException 설정된 encoding 방식이 올바르지 않으면 발생한다. 기본 encoding 방식은 "EUC-KR"이다
     */
    public void setName(String name) throws ApplicationException {
        try {
            emailAttachment.setName(MimeUtility.encodeText(name, this.getCharset(), "B"));
        } 
        catch (UnsupportedEncodingException uee) {
            throw new ApplicationException("service.error.10004", uee);
        }
    }

    /**
     * <p>
     * 설정된 첨부파일의 별칭을 얻어온다.<br>
     * </p>
     *
     * @return <code>String</code> 설정된 첨부파일의 별칭
     */
    public String getName() {
        return emailAttachment.getName();
    }

    /**
     * <p>
     * 개체의 disposition을 설정한다.<br>
     * 기본값은 jakarta.mail.Part.ATTACHMENT 이다 <br>
     * </p>
     *
     * @param disposition <code>String</code> 원하는 disposition
     */
    public void setDisposition(String disposition) {
        emailAttachment.setDisposition(disposition);
    }

    /**
     * <p>
     * 설정된 disposition 값을 얻어온다.<br>
     * 기본값은 jakarta.mail.Part.ATTACHMENT 이다 <br>
     * </p>
     *
     * @return <code>String</code> 설정된 disposition
     */
    public String getDisposition() {
        return emailAttachment.getDisposition();
    }

    /**
     * <p>
     * EmailAttachment 개체를 반환한다.<br>
     * </p>
     *
     * @return {@link org.apache.commons.mail.EmailAttachment }
     * @throws ApplicationException MailAttachment 개체를 완전하게 설정하지 않았을 경우
     */
    public EmailAttachment getAttachment() throws ApplicationException {
        return this.emailAttachment;
    }
}
