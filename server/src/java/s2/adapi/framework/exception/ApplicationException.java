package s2.adapi.framework.exception;

import java.text.MessageFormat;

import s2.adapi.framework.Constants;
import s2.adapi.framework.config.ConfiguratorFactory;
import s2.adapi.framework.resources.Messages;

/**
 * <p>
 * 에플리케이션에서 발생한 에러나 이벤트에 대한 정보를 전달하기 위한 <code>Exception</code> 클래스로서
 * 두 가지 형태로 사용이 가능하다. 첫 번째  방법은  대부분의  <code>Exception</code> 클래스들 처럼
 * 해당 에러나 이벤트에 대한 완전한 메시지를 담아 전달하는 형태로 사용하는 것이고, 두 번째 방법은
 * 에러나 이벤트에 대한 메시지를 원하는 시점에 원하는 형태대로  생성할 수 있도록 메시지 포멧과 여기에 포함될
 * 파라미터 값들을 담아서 전달하는 형태로 사용하는 것이다. 두 번째 방법의 경우, 메시지 포멧 스트링 자체를
 * 직접 담는게 아니고 해당 메시지 포멧 스트링을 검색하여 사용할 수 있는 식별자인 키(Key)를 대신 사용한다.
 * </p>
 *
 * @author kimhd
 * @since 1.0
 */

public class ApplicationException extends RuntimeException {

	private static final long serialVersionUID = -8337948945930182883L;

	/**
     * <p>
     * Exception의 StackTraceElement에서 메시지로 전달할 Exception을 찾기위한 Class 명의
     * Prefix 문자열을 저장해 놓는다. static 초기화시 처음에 한번 저장된다.
     * </p>
     */
    private static String staticExceptionFilterPrefix = null;

    /**
     * <p>
     * Exception의 StackTraceElement에서 메시지로 전달할 Exception을 찾기위한 Class 명의
     * Prefix 문자열을 Exception 클래스 생성시 저장해 놓는다.
     * 이것은 Exception이 다른 서버로 전달될 때 다른 서버의 Configuration 파일이 아니라
     * Exception이 생성된 서버의 Configuration 파일의 내용을 유지하기 위해서이다.
     * </p>
     */
    private String localExceptionFilterPrefix = null;

    /**
     * 에러나 이벤트에 대한 메시지 생성 시에 포멧 스트링에 포함될 파라미터 값들
     */
    protected Object[] params = null;

    /**
     * 에러나 이벤트에 대한 메시지 생성 시에 사용하는 포멧 스트링
     */
    protected String formatString = null;

    /**
     * 클래스 Initializer로서 구성 화일을 읽어 들여 <code>EXCEPTION_STACK_FILTER_PREFIX</code>
     * 값을 저장해 놓는다.
     *
     */
    static {
        configure();
    }

    protected static void configure() {

        staticExceptionFilterPrefix = Constants.CONFIG_FQCN_PREFIX_DEFAULT;

        //
        // Configurator 오브젝트를 통하여 구성화일명을 얻는다.
        //
        try {
            staticExceptionFilterPrefix = ConfiguratorFactory.getConfigurator()
            		.getString(Constants.CONFIG_FQCN_PREFIX_KEY, Constants.CONFIG_FQCN_PREFIX_DEFAULT);
        }
        catch (Exception ex) {
        }
    }

    /**
     * <p>
     * 아무런 인자도 갖지 않는 디폴트 <code>ApplicationException</code> 컨스트럭터
     * </p>
     */
    public ApplicationException() {
        this(null, null, null);
    }

    /**
     * <p>
     * 에러나 이벤트에 대한 메시지나 해당 메시지의 생성을 위한 메시지 포멧 스트링을 나타내는
     * 키(Key)만을 인자로 갖는 <code>ApplicationException</code> 컨스트럭터
     * </p>
     *
     * @param key 에러나 이벤트에 대한 메시지나 해당 메시지의 생성을 위한 메시지 포멧 스트링을
     *            나타내는 키(Key)
     */
    public ApplicationException(String key) {
        this(key, null, null);
    }

    /**
     * <p>
     * 에러나 이벤트에 대한 메시지의 생성을 위한 메시지 포멧 스트링을 나타내는 키(Key)와
     * 해당 메시지 포멧 스트링에 적용될  파라미터 값들을 인자로 갖는 <code>ApplicationException</code> 컨스트럭터
     * </p>
     *
     * @param key    생성될 메시지를 위한 메시지 포멧 스트링을 나타내는  키(Key)
     * @param params 메시지 포멧 스트링에 적용될 파라미터 값들로 최대 4개까지 사용 가능
     */
    public ApplicationException(String key, Object[] params) {
        this(key, params, null);
    }

    /**
     * <p>
     * 에러나 이벤트에 대한 메시지의 생성을 위한 메시지 포멧 스트링을 나타내는 키(Key)와
     * 해당 메시지 포멧 스트링에 적용될 한 개의 파라미터 값을 인자로 갖는 <code>ApplicationException</code> 컨스트럭터
     * </p>
     *
     * @param key    생성될 메시지를 위한 메시지 포멧 스트링을 나타내는 키(Key)
     * @param param1 메시지 포멧 스트링에 적용될 첫 번째 파라미터 값
     */
    public ApplicationException(String key, Object param1) {
        this(key, new Object[]{param1}, null);
    }

    /**
     * <p>
     * 에러나 이벤트에 대한 메시지의 생성을 위한 메시지 포멧 스트링을 나타내는 키(Key)와
     * 해당 메시지 포멧 스트링에 적용될 두 개의 파라미터 값을 인자로 갖는 <code>ApplicationException</code> 컨스트럭터
     * </p>
     *
     * @param key    생성될 메시지를 위한 메시지 포멧 스트링을 나타내는 키(Key)
     * @param param1 메시지 포멧 스트링에 적용될 첫 번째 파라미터 값
     * @param param2 메시지 포멧 스트링에 적용될 두 번째 파라미터 값
     */
    public ApplicationException(String key, Object param1, Object param2) {
        this(key, new Object[]{param1, param2}, null);
    }

    /**
     * <p>
     * 에러나 이벤트에 대한 메시지의 생성을 위한 메시지 포멧 스트링을 나타내는 키(Key)와
     * 해당 메시지 포멧 스트링에 적용될 세 개의 파라미터 값을 인자로 갖는 <code>ApplicationException</code> 컨스트럭터
     * </p>
     *
     * @param key    생성될 메시지를 위한 메시지 포멧 스트링을 나타내는 키(Key)
     * @param param1 메시지 포멧 스트링에 적용될 첫 번째 파라미터 값
     * @param param2 메시지 포멧 스트링에 적용될 두 번째 파라미터 값
     * @param param3 메시지 포멧 스트링에 적용될 세 번째 파라미터 값
     */
    public ApplicationException(String key, Object param1, Object param2, Object param3) {
        this(key, new Object[]{param1, param2, param3}, null);
    }

    /**
     * <p>
     * 에러나 이벤트에 대한 메시지의 생성을 위한 메시지 포멧 스트링을 나타내는 키(Key)와
     * 해당 메시지 포멧 스트링에 적용될 네 개의 파라미터 값을 인자로 갖는 <code>ApplicationException</code> 컨스트럭터
     * </p>
     *
     * @param key    생성될 메시지를 위한 메시지 포멧 스트링을 나타내는 키(Key)
     * @param param1 메시지 포멧 스트링에 적용될 첫 번째 파라미터 값
     * @param param2 메시지 포멧 스트링에 적용될 두 번째 파라미터 값
     * @param param3 메시지 포멧 스트링에 적용될 세 번째 파라미터 값
     * @param param4 메시지 포멧 스트링에 적용될 네 번째 파라미터 값
     */
    public ApplicationException(String key, Object param1, Object param2, Object param3, Object param4) {
        this(key, new Object[]{param1, param2, param3, param4}, null);
    }

    /**
     * <p>
     * 에러나 이벤트의 원인이 되는 <code>Exception</code>만을 인자로 갖는 <code>ApplicationException</code> 컨스트럭터
     * </p>
     *
     * @param cause 에러나 이벤트의 원인이 되는 <code>Exception</code>
     */
    public ApplicationException(Throwable cause) {
        this(null, null, cause);
    }

    /**
     * <p>
     * 에러나 이벤트의 원인이 되는 <code>Exception</code>과 이에 대한 메시지나  해당 메시지의
     * 생성을 위한 메시지 포멧 스트링을 나타내는 키(Key)를  인자로 갖는
     * <code>ApplicationException</code> 컨스트럭터
     * </p>
     *
     * @param key   에러나 이벤트에 대한 메시지나 해당 메시지의 생성을 위한 메시지 포멧 스트링을
     *              나타내는 키(Key)
     * @param cause 에러나 이벤트의 원인이 되는 <code>Exception</code>
     */
    public ApplicationException(String key, Throwable cause) {
        this(key, null, cause);
    }

    /**
     * <p>
     * 에러나 이벤트의 원이이 되는 <code>Exception</code>과 이에 대한  메시지의 생성을 위한
     * 메시지 포멧 스트링을 나타내는 키(Key)와  메시지 포멧 스트링에 적용될  파라미터 값들을
     * 인자로 갖는 <code>ApplicationException</code> 컨스트럭터
     * </p>
     *
     * @param key    생성될 메시지를 위한 메시지 포멧 스트링을 나타내는  키(Key)
     * @param params 메시지 포멧 스트링에 적용될 파라미터 값들로 최대 4개까지 사용 가능
     * @param cause  에러나 이벤트의 원인이 되는 <code>Exception</code>
     */
    public ApplicationException(String key, Object[] params, Throwable cause) {

        super(key, cause);

        if (key != null) {
            this.params = params;
            this.formatString = Messages.getMessages().getMessage(key);
        }

        // Exception Filter prefix 문자열을 member 변수로 저장해 놓는다.
        localExceptionFilterPrefix = staticExceptionFilterPrefix;
    }

    /**
     * <p>
     * 에러나 이벤트의 원이이 되는 <code>Exception</code>과 이에 대한  메시지의 생성을 위한
     * 메시지 포멧 스트링을 나타내는 키(Key)와  메시지 포멧 스트링에 적용될  한 개의 파라미터 값을
     * 인자로 갖는 <code>ApplicationException</code> 컨스트럭터
     * </p>
     *
     * @param key    생성될 메시지를 위한 메시지 포멧 스트링을 나타내는  키(Key)
     * @param param1 메시지 포멧 스트링에 적용될 첫 번째 파라미터 값
     * @param cause  에러나 이벤트의 원인이 되는 <code>Exception</code>
     */
    public ApplicationException(String key, Object param1, Throwable cause) {
        this(key, new Object[]{param1}, cause);
    }

    /**
     * <p>
     * 에러나 이벤트의 원이이 되는 <code>Exception</code>과 이에 대한  메시지의 생성을 위한
     * 메시지 포멧 스트링을 나타내는 키(Key)와  메시지 포멧 스트링에 적용될  두 개의 파라미터 값을
     * 인자로 갖는 <code>ApplicationException</code> 컨스트럭터
     * </p>
     *
     * @param key    생성될 메시지를 위한 메시지 포멧 스트링을 나타내는  키(Key)
     * @param param1 메시지 포멧 스트링에 적용될 첫 번째 파라미터 값
     * @param param2 메시지 포멧 스트링에 적용될 두 번째 파라미터 값
     * @param cause  에러나 이벤트의 원인이 되는 <code>Exception</code>
     */
    public ApplicationException(String key, Object param1, Object param2, Throwable cause) {
        this(key, new Object[]{param1, param2}, cause);
    }

    /**
     * <p>
     * 에러나 이벤트의 원이이 되는 <code>Exception</code>과 이에 대한  메시지의 생성을 위한
     * 메시지 포멧 스트링을 나타내는 키(Key)와  메시지 포멧 스트링에 적용될  세 개의 파라미터 값을
     * 인자로 갖는 <code>ApplicationException</code> 컨스트럭터
     * </p>
     *
     * @param key    생성될 메시지를 위한 메시지 포멧 스트링을 나타내는  키(Key)
     * @param param1 메시지 포멧 스트링에 적용될 첫 번째 파라미터 값
     * @param param2 메시지 포멧 스트링에 적용될 두 번째 파라미터 값
     * @param param3 메시지 포멧 스트링에 적용될 세 번째 파라미터 값
     * @param cause  에러나 이벤트의 원인이 되는 <code>Exception</code>
     */
    public ApplicationException(String key, Object param1, Object param2, Object param3, Throwable cause) {
        this(key, new Object[]{param1, param2, param3}, cause);
    }

    /**
     * <p>
     * 메시지 포멧 스트링을 나타내는 키(Key)값을 리턴한다.
     * </p>
     *
     * @return 메시지 포멧 스트링을 나타내는 키(Key)
     */
    public String getKey() {
        return super.getMessage();
    }

    /**
     * 파라메터가 대체되어 변환된 메시지 스트링을 리턴한다.
     *
     * @return 메시지 스트링
     */
    public String getMessage() {
        // 메시지 파일에 등록된 메시지를 가져온다.
        String msgString = null;

        if (formatString == null) {
            msgString = getKey();
        } else {
            MessageFormat format = new MessageFormat(formatString);
            msgString = format.format(params);
        }
        return msgString;
    }

    /**
     * <p>
     * getMessage()결과에 추가적으로 Exception 발생 위치를 함께 리턴한다.
     * </p>
     * 예: 이미 존재하는 데이터 입니다.|himed.his.com.batchinfomgr.batchprogrammgt.
     * BatchProgramMgtImpl.exeSaveBatchProgramInfo() at line 53 in BatchProgramMgtImpl.java
     *
     * @return 메시지 스트링
     */
    public String getLocalizedMessage() {
        // 메시지 파일에 등록된 메시지를 가져온다.
        StringBuilder msgString = new StringBuilder();

        msgString.append(getMessage());

        // ExceptionFilterPrefix가 정의되었다면 발생위치 정보를 찾아서 추가한다.
        if (localExceptionFilterPrefix != null) {
            // Exception 발생 위치를 찾기 위한 로직
            // 마지막 caused Exception을 찾는다.
            Throwable th = this;
            while (th.getCause() != null) {
                th = th.getCause();
            }
            // 마지막 caused Exception의 StackTraceElement 배열을 얻어 온후
            // 클래스가localExceptionFilterPrefix 값으로 시작하는 첫번째 StackTraceElement를 가져온다.
            StackTraceElement ste[] = th.getStackTrace();
            int i = 0;
            for (; i < ste.length; i++) {
                if (ste[i].getClassName().startsWith(localExceptionFilterPrefix)) {
                    break;
                }
            }

            // 메시지 발생 원인 Exception명을 출력
            msgString.append("|");
            if (th != this) {
                msgString.append(th.toString());
                msgString.append("|");
            }
            // 메시지 발생 위치 설명문을 작성
            // 메시지 스트링 + "|" + 발생위치 스트링
            if (i < ste.length) {
                msgString.append(ste[i].getClassName());
                msgString.append(".");
                msgString.append(ste[i].getMethodName());
                msgString.append("() at line ");
                msgString.append(ste[i].getLineNumber());
                msgString.append(" in ");
                msgString.append(ste[i].getFileName());
            }
        }
        
        return msgString.toString();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getName()).append(":");
        if (params != null) {
        	sb.append("param=");
	        for (int i = 0; i < params.length; ++i) {
	            sb.append(params[i]).append(",");
	        }
        }
        sb.append("message=").append(getMessage());

        return sb.toString();
    }
}
