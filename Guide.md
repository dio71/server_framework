## 목차

1. [ValueObject](#1-ValueObject)
   * [ValueObjectAssembler](#ValueObjectAssembler)
2. [Service Container](#2-Service-Contailer)
   * [1) EnlistedServiceContainer](#1-EnlistedServiceContainer)
4. [Context](#3-Context)
5. [AOP](#4-AOP)
6. [Data Access Object](#5-Data-Access-Object)
7. [Web Application](#6-Web-Application)
8. [Transaction](#7-Transaction)
9. [Messages](#8-Messages)
10. [Configuration](#9-Configuration)

# 1. ValueObject

ValueObject 는 S2API Framework의 내부뿐만 아니라 외부 클라이언트(Android/iOS Client)와의 데이터 통신시 사용되는Data Transfer Object(DTO) 이다. 

ValueObject 는 다음과 같은 특징을 갖는다.

-   정적인 필드가 아닌 동적인 필드 구성 방식이다. (Map-based DTO)
	- 여러 컬럼으로 구성된 레코드들을 리스트 형식으로 저장한다. (List of Map)
	- 저장된 데이터의 컬럼 정보를 별도로 가지고 있지 않다. 즉 각 row 별로 컬럼 구성이 다를 수 있다.
	- DB의 Table 데이터를 저장하기에 적합한 구조이다.

-   명명 규칙 : ValueObject 타입의 변수는 반드시 VO 로 끝나도록 명명한다.
	- 예시) retVO, reqVO, advVO, pubVO, paramVO 등

### (1) 클래스 선언부
- public class ValueObject implements List<Map<String,Object>>,Externalizable

### (2) 생성자
- public ValueObject() : 기본 생성자
- public ValueObject(String name) : 명칭을 지정하는 경우 ValueObjectAssembler 내에서 구분하기 위하여 사용됨

### (3) Set 메소드
- public void set([int index,] String key, boolean value)
- public void set([int index,] String key, int value)
- public void set([int index,] String key, long value)
- public void set([int index,] String key, float value)
- public void set([int index,] String key, double value)
- public void set([int index,] String key, Object value)
- public Map<String, Object> set(int index, Map<String,Object> element)
	- 하나의 row 전체를 설정한다. 이전에 저장되어 있던 row는 return 된다.
	- index 파라메터를 지정하지 않으면 0 번째 row 에 저장된다.
	- index 를 현재 row 수 보다 크게 지정하면 IndexOutOfBoundException 이 발생한다. 
- public boolean add(Map<String, Object> e) : 주어진 파라메터를 마지막 row 로 추가한다.
- public void add(ValueObject vo) : 주어진 ValueObject 객체의 row 들을 마지막 row 에 추가한다. 

### (4) Get 메소드

- public boolean getBoolean([int index,] String key[, boolean defaultValue])
- public int getInt([int index,] String key[, int defaultValue])
- public long getLong([int index,] String key[, long defaultValue])
- public float getFloat([int index,] String key[, float defaultValue])
- public double getDouble([int index,] String key[, double defaultValue])
- public String getString([int index,] String key[, String  defaultValue])
- public Object get([int index,] String key[, Object  defaultValue])
	- index 파라메터를 지정하지 않으면 0 번재 row 에서 값을 가져온다.
	- index 를 현재 row 수 보다 크게 지정하면 IndexOutOfBoundException 이 발생한다. 
	- defaultValue 를 지정하면 해당 컬럼명의 값이 존재하지 않는 경우 defaultValue가 반환된다.
	- defaultValue 를 생략하면 숫자형 타임의 경우 0, 그외에는 null 이 사용된다.
- public Map<String, Object> get(int index) : index 번째 row 전체를 반환한다.
- public ValueObject getRowAsVo(int index) : index 번째 row 전체를 ValueObject에 담아서 반환한다.

### (5) 기타 메소드

- public int size() : 현재 row 수를 반환한다.
- public Map<String, Object> remove(int index) : index 번째 row 전체를 삭제한다.
- public void clear() : 전체 row 를 삭제한다.

### (6) 사용 예시

**ValueObject Sample #1**

```java
// 2016.04.11 기기 년도 타게팅을 위한 기기 출시 년도 조회
String pubAllPhoneYearYn = pubVO.getString(Constants.Columns.ALL_PHONEY_YN, "Y");

ValueObject pyyVO = publisherDAO.executeQuery("getPhoneManufYear", sessionVO);
if (pyyVO.size() > 0) {
         pubVO.set(Constants.PHONE_YEAR, yyVO.getString(Constants.PHONE_YEAR));

         // 2016.06.20 매체의 기기 출시연도 노출여부 확인
         if (!"Y".equals(pubAllPhoneYearYn)) {

             // 전체 노출이 아니므로 설정된 단말기 년도 리스트를 조회한다.
             ValueObject ppyVO = publisherDAO.executeQuery("getPublisherPhoneYear", pubVO);
             if (("N".equals(pubAllPhoneYearYn) && ppyVO.size() == 0) ||
                   ("D".equals(pubAllPhoneYearYn) && ppyVO.size() > 0)) {

             // 특정년도의 단말기 노출인데 설정된 년도에 없다. 또는 특정년도의 단말기 제외 노출인다 설정된 년도에 있다.

             return emptyVO; // 노출 차단
         }
     }
}
```

**ValueObject Sample #2**

```java
for(int i = 0; i < urlVO.size(); i++) {
    try {
        // 전송 내용 : 사용자ID, 지급 포인트, 고유 ID
        String callbackUrl = urlVO.getString(i,Constants.Columns.CALLBACK_URL);
        String userId = urlVO.getString(i,Constants.Columns.USER_ID);
        int payPoint = urlVO.getInt(i, Constants.Columns.PAY_POINT);
        long pointMultiple = urlVO.getLong(i, Constants.Columns.POINT_MULTIPLE, 1L);
        long trId = urlVO.getLong(i, Constants.Columns.TRANSACTION_ID);
        String appKey = urlVO.getString(i, Constants.Columns.APP_KEY);
        String mediaUsername = urlVO.getString(i, Constants.Columns.MEDIA_USER_NM);
        long advAppId = urlVO.getLong(i,Constants.Columns.ADV_APP_ID);
        long pubAppId = urlVO.getLong(i, Constants.Columns.PUB_APP_ID);
        String udid = urlVO.getString(i,Constants.Columns.UDID);
        String advAppName = urlVO.getString(i,Constants.Columns.APP_NAME);
        String extData = urlVO.getString(i, Constants.Columns.EXT_DATA);
        int actionId = urlVO.getInt(i, Constants.Columns.ACTION_ID);

        // callback 방식
        String callbackCode = urlVO.getString(i, Constants.Columns.CALLBACK_CODE, DefaultCallbackAgent.AGENT_CODE);

        // 저장된 udid 가 있으면 거기에서 deviceId  생성한다.
       String deviceId = userId.toUpperCase();
       log.info("## deviceId = " + deviceId);
       if (udid != null) {
            deviceId = HexEncoder.extractDeviceId(udid);
       }

       if (sendCallbackUrl(pubAppId, callbackUrl, deviceId, payPoint, pointMultiple, trId, appKey, mediaUsername, advAppId, advAppName, extData, actionId, callbackCode)) {

            // 성공하였으면 db에 반영하기 위하여 별도 VO에 담아둔다.
            succVO.add(urlVO.get(i));
        }
        else {
            // 실패한 것은 에러 마크하여 DB에 반영한다.
            failVO.add(urlVO.get(i));
        }
    }
    catch(Exception ex) {
        log.error("sendCallbackUrls error : ", ex);
        failVO.add(urlVO.get(i)); // java 오류인 경우에 실패로 저장하기 위해서 추가됨 (2014.07.08)
    }
}
```

**ValueObject Sample #3**

```java
/**
 * 사용자가 해당 앱에 대하여 지급받았으므로 ppi_userpayed table에 저장한다. (pay_type = X)
 * @param appId 광고앱 ID
 * @param userId 사용자 기기 ID
 */
@Override
public void setUserPayed(long appId, String userId) {
    ValueObject paramVO = new ValueObject();
    paramVO.set(Constants.Columns.APP_ID, appId);
    paramVO.set(Constants.Columns.USER_ID, userId);
    if (publisherDAO.executeQuery("getUserPayed", paramVO).size() == 0) {
        publisherDAO.executeUpdate("insUserPayed", paramVO);
    }
}
```

## ValueObjectAssembler

ValueObjectAssembler 는 여러개의 ValueObject 를 한번에 모아서 전송할 수 있도록 하는 DTO 클래스이다. 내부적으로는 ValueObject 를 가지고 있는 Map 으로 구성되어 있다.

- 명명규칙 : ValueObjectAssembler 타입의 변수는 변수명이 반드시 VOs 로 끝나도록 명명한다.
	- 예시) retVOs, 

### (1) 클래스 선언부

-   public class ValueObjectAssembler implements Map<String,ValueObject>, Externalizable

### (2) 생성자

-   public ValueObjectAssembler()

### (3) 주요 메소드

- public void set(String key, ValueObject vo) : ValueObject 객체를 저장한다.
- public ValueObject get(String key) : key 로 저장했던 ValueObject 객체를 반환한다.
- public int size() : 현재 저장되어 있는 ValueObject 개수를 반환한다.
- public void clear() : 저장되어 있던 모든 ValueObject 객체들을 삭제한다.

**ValueObjectAssembler Sample #1**

```java
public ValueObjectAssembler getLockerRewardList(String phMdl, String phTelecom, boolean isKoreaTel) {

    ValueObjectAssembler retVOs = new ValueObjectAssembler();
    RwdApp2 rwdapp2 = new RwdApp2();
    ValueObject paramVO = new ValueObject();
    paramVO.set("ph_mdl", phMdl);

    ValueObject mdlVO = new ValueObject();
    mdlVO = appDAO.executeQuery("checkPhoneModel", paramVO);

    // 1. 2012년 이전 출시 단말기 = empty string
    // 2. 2012년 이후 출시 단말기 = 제조년
    String manufYYYY = "999912"; 
    if(mdlVO.size() > 0) {
        manufYYYY = mdlVO.getString("manuf_yyyy", "");
    }
    mdlVO.set("ph_mdl", phMdl);
    mdlVO.set("ph_telco", phTelecom);

    retVOs.set("mdl_info", mdlVO);
    retVOs.set("locker_reward", rwdapp2.getLockerRewardList(phMdl, manufYYYY));

    return retVOs;
}
```

# 2. Service Container

Service Container 는 애플리케이션 로직을 제공하는 IOC(Inversion Of Control) Container 이며 POJO(Plain Ordinary Java Object) 간의 종속성 삽입(DI, Dependency Injection) 기능 및 AOP(Aspect Oriented Programming) 기능을 제공하는 핵심적인 인프라이다.

- Service Container 내에서 모든 서비스들은 POJO 들로 구성된다.
- POJO 는 Interface 와 Implementation로 분리가 명시적으로 표현되어야 한다. (일부 POJO 제외)
- POJO 들간의 Dependency Injection(DI) 을 위하여 Setter Injection, Constructor Injection, Injection 기능이 제공된다.
- POJO 들간의 DI 관계를 XML 로 설정할 수 있다. 
- XML로 작성되는 서비스 설정 파일은 Service Module 별로 작성한다.
- Service Module 은 여러개의 POJO Interfaces, implementation classes, SQL files, XML Service 설정 파일들을 하나의 Jar 파일로 패키징한 것이다.
- AOP 기능을 제공한다.

**ServiceContainer.java**

```java
/**
 * 등록된 서비스 정의 목록으로부터 서비스 객체를 생성하고 요청된 서비스 객체를 반환해주는 서비스 컨테이너 구현을 위한 인터페이스이다.
 * @author 김형도
 */

public interface ServiceContainer {
    /**
     * 주어진 서비스 명에 해당되는 서비스 객체를 반환한다. 서비스 명이 서비스 정의 파일에 없다면 ServiceContainerException이 발생된다.
     * @param svcName 얻고자하는 서비스 명
     * @return 해당 서비스 객체
     * @throws ServiceContainerException 서비스가 정의되지 않았거나, 서비스 객체 생성 시 오류가 발생했을 경우
     */

    public Object getService(String svcName) throws ServiceContainerException;

    /**
     * 주어진 타입명에 해당되는 서비스 객체  목록을 반환한다. 하위클래스는 여부는 고려하지 않고 타입명이 정확히 일치하는 경우만 찾아서 반환한다.
     * 타입 명 비교 시에 서비스의 interface와 class 설정 값을 모두 확인한다.  
     * @param typeName 얻고자하는 서비스의 클래스 명(전체 패키지)
     * @return 해당 서비스 객체를 담고 있는 Map 객체
     * @throws ServiceContainerException 서비스 객체 생성 시 오류가 발생했을 경우
     */
    public Map<String,Object> getServicesOfType(String typeName) throws ServiceContainerException;

    /**
     * 서비스 컨테이너에 등록된 모든 서비스 명을 배열로 리턴한다.
     * @return String[] 서비스 명 배열
     */
    public String[] getAllServiceNames();

    /**
     * 서비스 컨테이너에 등록된 서비스 명 중에서 패턴형태의 명칭들을 배열로 반환한다.
     * @return
     */
    public String[] getPatternServiceNames();

    /**
     * 주어진 서비스 명이 등록되어 있는지 여부를 반환한다.
     * @param svcName
     * @return
     */
    public boolean containsService(String svcName);

    /**
     * 서비스 객체 생성후 후처리를 진행할 ServicePostProcessor 객체를 등록한다.
     * @param svcpost
     */
    public void addPostProcessor(ServicePostProcessor svcpost);

    /**
     * pre-init, singleton, activate 속성이 모두 true 인 서비스들을 미리 생성한다.
     */
    public void populateServices();

    /**
     * 서비스 객체들을 다시 생성한다.
     */
    public void reload();

    /**
     * 컨테이너를 종료한다. 필요한 리소스를 반환하고 서비스들을 제거하는 작업들을 수행한다.
     */
    public void close();

    /**
     * 등록된 서비스들에 대한 정보 목록을 반환한다.
     * @return ServiceInfo[]
     */
    public ServiceInfo[] getServiceInfo();

    /**
     * 특정 서비스에 대한 정보를 반환한다.
     * @param svcName
     * @return
     */
    public ServiceInfo getServiceInfo(String svcName);

    /**
     * 서비스 클래스를 로딩할 때 사용하는 클래스로더 객체를 반환한다.
     */
    public ClassLoader getClassLoader();
}
```

프레임워크 내에서는 ServiceContainer 의 구현 클래스를 아래와 같이 3개를 제공한다.
- EnlistedServiceCointainer : ServiceContainer 의 핵심 기능들을 제공하는 클래스이다. 서비스들간의 DI
설정을 위한 API 를 제공한다.
- XmlConfiguredServiceContainer : 서비스들간의 DI 설정을 XML 로 할 수 있도록 기능을 제공한다.
- ReloadableXmlServiceContainer : Hot deploy 기능을 제공하는 XmlConfiguredService Container 의 하위 클래스이다. 실제적으로 사용되는 ServiceContainer 이다.

## 1) EnlistedServiceContainer

ServiceContainer 의 핵심 기능들을 제공하는 클래스이다. 이 클래스를 직접적으로 사용하지는 않는다.

## 2) XmlConfiguredServiceContainer

XML 기반의 서비스 설정 기능을 제공하는 ServiceContainer 의 구현 클래스이다. 서비스 설정 파일은 XmlConfiguredServiceContainer의 생성자를 통하여 지정할 수 있다.

서비스 설정파일의 구조는 아래와 같다.

### (1) \<services>
서비스 설정파일의 루트 노드이다. 다음과 같은 속성(attribute)들을 갖는다.

-   module : 서비스의 모듈명을 지정한다. 모듈명이 지정되면 다른 모듈에서 여기의 서비스를 참조하고자 할 경우에는 "모듈명.서비스명" 형식으로 참조할 서비스를 지정해야한다.
- package : 서비스의 패키지명을 지정한다. 여기에 지정된 패키지명은 이후 ${package} 형태로 사용될 수 있다.
- pre-init : 서비스 컨테이너가 생성될 때 설정된 서비스 객체들을 미리 생성해 놓을 지 여부를 지정한다. 기본값은 false 이다. 만약 개별 서비스 객체의 설정에서 pre-init 여부를 지정했다면 해당 서비스 객체는 그 값을 사용한다.

### (2) \<service>
하나의 서비스 객체를 정의한다. 다음과 같은 속성들을 갖는다.

- name : 서비스 명칭으로 모듈내에서 고유해야한다.
- interface : 서비스를 사용하기 위한 interface 명이다. 전체 패키지 명칭으로 명시해야한다.
- class : 서비스를 구현한 class 명이다. 전체 패키지 명칭으로 명시해야한다.
- interceptor : Method Interceptor 를 사용하고자 할 경우 해당 interceptor의 서비스 명을 지정한다. 설정한 interceptor 서비스에 다른 interceptor가 설정되어 있는 경우에는 그 interceptor 는 적용되지 않는다.
- init-method : 초기화 메소드를 지정한다. 지정되는 메소드는 구현 class의 메소드이며 서비스 객체가 처음 생성되는 시점에 호출된다. (서비스 객체의 Constructor 호출 후 Property 설정이 모두 완료된 후 호출된다.)
- destroy-method : 소멸 메소드를 지정한다. 지정되는 메소드는 구현 class의 메소드이며 서비스 컨테이너가 종료될 때 singleton으로 생성되어 있던 서비스들을 대상으로 호출된다.
- singleton : Singleton 여부를 true 또는 false 로 지정한다. 기본값은 true 이다.
- pre-init : 서비스 컨테이너가 생성되는 시점에 이 서비스 객체를 미리 생성해 놓을 지 여부를 지정한다. 지정하지 않으면 \<services> 에 정의되어 있는 pre-init 속성값을 따른다. 기본값은 false 이다.
- activate : 서비스의 사용여부를 설정한다. false 로 설정하면 서비스 객체의 설정자체는 컨테이너에 등록되지만 그 서비스를 사용할 수는 없다. 또한 해당 서비스가 interceptor 로 사용되는 경우에는 그 interceptor 는 적용되지 않는다. 기본값은 true 이다.

### (3) \<factory>
서비스의 생성방법으로 메소드를 사용할 경우 <service> 노드의 하위에 설정한다. <constructor> 설정과 중복될 수 없다.

- class : Static method 인 경우에 사용된다. 사용할 class 명이다.
- ref : Object method 인 경우에 사용된다. 사용할 서비스 객체의 명칭이다.
- method : 호출할 의 메소드 명이다. 호출 시 파라메터가 필요한 경우 하위 노드로 <arg> 를 설정한다.
- class 속성과 ref 속성은 동시에 설정할 수 없다.

### (4) \<constructor>
서비스의 생성방법으로 구현클래스의 생성자를 사용할 경우 <service> 노드의 하위에 설정한다. 생성자의 파라메터가 필요할 경우 하위 노드에 <arg> 를 사용하여 설정한다.

### (5) \<arg>
\<factory> 또는 \<constructor> 의 하위 노드로 메소드 호출 시 필요한 파라메터가 있는 경우 파라메터 갯수에 맞추어 순서대로 정의한다.

- type : 파라메터의 클래스 명이다.
- value : 실제 파라메터 값이다. 이 값은 String 으로 파싱되므로 파라메터를 지정하기 위해서는 type 으로 설정된 클래스가 String 을 인자로 갖는 생성자를 제공해야한다.
- ref : 다른 서비스 객체가 파라메터로 사용될 경우 그 서비스 객체의 명칭을 지정한다.
- value 와 ref 속성은 동시에 설정할 수 없다.  

### (6) \<property>
서비스 객체 생성 후 서비스의 속성값을 설정하기 위하여 <service> 노드의 하위에 설정한다. 이것은 <factory> 나
<constructor> 와 중복되어 설정될 수 있으며 <property> 설정 자체도 여러번 설정될 수 있다.

-   name : 속성 명이다. 이 명칭을 이용하여 set 메소드 명을 설정한다.
	- name 속성명의 첫번째 글자를 대문자로 변경한 후 앞에 set 을 붙이면 호출될 set 메소드의 명칭이 된다.
- value : 실제 파라메터 값이다. 이 값은 String 으로 파싱되므로 파라메터를 지정하기 위해서는 type 으로 설정된 클래스가 String 을 인자로 갖는 생성자를 제공해야한다.
- ref : 다른 서비스 객체가 파라메터로 사용될 경우 그 서비스 객체의 명칭을 지정한다.
- value 와 ref 속성은 동시에 설정할 수 없다. 

**Sample #1**

```xml
<?xml version=\'1.0\' encoding=\'euc-kr\'?>
<!DOCTYPE services PUBLIC "-//S2 API//DTD s2adapi Services Config 0.1//EN" "s2/adapi/framework/container/service-config.dtd">

<services module="adzzle2" package="s2.adapi.app.service.adzzle2" pre-init="false">
    <service name="MemberDAO"
             class="s2.adapi.framework.dao.SqlQueryDAO"
              singleton="true">
        <property name="datasource" ref="jdbc.apidb"/>
        <property name="sql" value="sqls/member_sqls.xml"/>
    </service>

    <service name="Member"
             interface="${package}.Member"
             class="${package}.MemberImpl"
             pre-init="true"
             singleton="true">
         <property name="dao" ref="MemberDAO"/>
         <property name="mailer" ref="system.Mailer"/>
         <property name="smsClient" ref="system.SmsClient"/>
         <property name="User" ref="ad.u"/>
         <property name="pushManager" ref="PushManager"/>
    </service>

    <service name="RewardDAO"
             class="s2.adapi.framework.dao.SqlQueryDAO"
             singleton="true">
        <property name="datasource" ref="jdbc.apidb"/>
        <property name="sql" value="sqls/reward_sqls.xml"/>
    </service>

    <service name="Reward"
             interface="${package}.Reward"
             class="${package}.RewardImpl"
             pre-init="true"
             singleton="true">
         <property name="dao" ref="RewardDAO"/>
         <property name="pushManager" ref="PushManager"/>
         <property name="CouponEvent" ref="event.CouponEvent"/>
    </service>

    <service name="PushDAO"
             class="s2.adapi.framework.dao.SqlQueryDAO"
             singleton="true">
        <property name="datasource" ref="jdbc.apidb"/>
        <property name="sql" value="sqls/push_sqls.xml"/>
    </service>

    <service name="PushManager"
             interface="${package}.PushManager"
             class="${package}.PushManagerImpl"
             pre-init="true"
             singleton="true">
         <property name="dao" ref="PushDAO"/>
    </service>

</services>
```

**Sample #2**

```xml
<?xml version=\'1.0\' encoding=\'euc-kr\'?>
<!DOCTYPE ...>    

<services module="jdbc" pre-init="false">
    <service name="admdb"
             interface="javax.sql.DataSource"
             class="s2.adapi.framework.dao.sql.DataSource"
             singleton="true">
        <property name="dsn" value="java:comp/env/jdbc/admdb"/>
    </service>

    <service name="apidb01"
             interface="javax.sql.DataSource"
             class="s2.adapi.framework.dao.sql.DataSource"
             singleton="true">
        <property name="dsn" value="java:comp/env/jdbc/apidb01"/>
    </service>

    <service name="apidb02"
             interface="javax.sql.DataSource"
             class="s2.adapi.framework.dao.sql.DataSource"
             singleton="true">
        <property name="dsn" value="java:comp/env/jdbc/apidb02"/>
    </service>

<!-- apidb DataSourceSet for admin server -->

    <service name="apidbSet"
             class="s2.adapi.framework.dao.sql.DataSourceSet"
             singleton="true">
        <property name="datasource" ref="apidb01"/>
        <property name="datasource" ref="apidb02"/>
    </service>

</services>
```

**Sample #3**

```xml
<?xml version=\'1.0\' encoding=\'euc-kr\'?>
<!DOCTYPE ....>

<services package="test.container" module="test">

    <service name="LogProxy"
             interface="s2.adapi.framework.aop.InterceptorProxy" 
             class="s2.adapi.framework.aop.MethodInterceptorProxy"
             singleton="false">
        <property name="interceptor" ref="logInterceptor"/> 
    </service>

    <service name="foo" 
             interface="${package}.FooInf" 
             class="${package}.FooImpl" 
             interceptor="LogProxy" 
             init-method="init" 
             destroy-method="cleanup">
        <constructor>
            <arg type="java.lang.String" value="100"/>
        </constructor>
        <property name="name" value="happy"/>
        <property name="age" value="30"/>
        <property name="address" value="myhouse"/>
        <property name="child" ref="boo"/>
    </service>

    <service name="boo" 
             interface="${package}.BooInf"
             class="${package}.BooImpl" 
             interceptor="LogProxy" 
             singleton="false">
        <property name="name" value="Barbie"/>
        <property name="parent" ref="foo"/>
    </service>

</services>
```

**Sample #3 Java**

```java
// FooInf
package test.container;

public interface FooInf {
    public String getName();
    public String printout();
    public void setChild(BooInf boo);
}

// FooImpl
package test.container;

public class FooImpl implements FooInf {
    private String name;
    private Long age;
    private String address;
    private String key;
    private BooInf child;

    public FooImpl() {
    }

    public FooImpl(String t) {
        key = t;
    }

    public FooImpl(String t, BooInf b) {
        key = t;
        child = b;
    }

    public void init() {
        System.out.println("foo init!!");
    }

    public void setName(String n) {
        name = n;
    }

    public void setAge(Long a) {
        age = a;
    }

    public void setAddress(String addr) {
        address = addr;
    }

    public void setChild(BooInf boo) {
        child = boo;
    }

    public String getName() {
        return name;
    }

    public String printout() {
        StringBuffer sb = new StringBuffer();
        sb.append("\nName : " + name);
        sb.append("\nAge : " + age);
        sb.append("\nAddress : " + address);

        if (child != null) {
            sb.append("\nchild:" + child.printout());
        }

        sb.append("\nKey :" + key);
        return sb.toString();
    }

    public void cleanup() {
        System.out.println("##FooImpl Dying ##");
    }
}

// BooInf
package test.container;

public interface BooInf {
    public String getName();
    public String printout();
}

// BooImpl
package test.container;

public class BooImpl implements BooInf {
    private String name;
    private FooInf parent;
    public BooImpl() {
    }

    public BooImpl(FooInf f) {
        parent = f;
    }

    public void setName(String n) {
        name = n;
    }

    public void setParent(FooInf foo) {
        parent = foo;
    }

    public String getName() {
        return name;
    }

    public String printout() {

        StringBuffer sb = new StringBuffer();
        sb.append("\nName : " + name);
        sb.append("\nparent Name : " + parent.getName());
        return sb.toString();
    }
}
```

## 3) ReloadableXmlServiceContainer

Hot deploy 기능을 제공하는 ServiceContainer 의 구현 클래스이며 XmlConfiguredServiceContainer의 하위 클래스이다. 내부적으로 별도의 URLClassLoader를 생성하며 이를 사용하여 EnlistedServiceContainer 객체를 생성하여 사용한다.  FileWatchdog 를 사용하여 모듈 Jar 파일의 변화를 감지하며 모듈 Jar 파일이 변경되면 reload() 메소드가 호출된다. reload()
메소드는 새로운 URLClassloader 와 이를 사용한 EnlistedServiceContainer 객체를 새로 생성하여 기존 서비스 컨테이너 객체를 교체한다.

프레임워크 설정 파일(s2adapi-config.properties) 내에서 관련된 설정이 이루어지며 설정 항목은 아래와 같다. (프레임워크 설정 파일은 [Configuration] 을 참조)

- s2adapi.container.default.name : 디폴트 서비스 컨테이너의 명칭을 설정한다.
- s2adapi.container.<컨테이너명칭>.impl : 사용할 서비스 컨테이너의 구현 클래스를 설정한다. 
- s2adapi.container.<컨테이너명칭>.reload.interval : 모듈을 reload 하기 위하여 체크하는 시간 간격이다. (초)
- s2adapi.container.<컨테이너명칭>.dir.module : 모듈 Jar 파일들이 위치하는 경로이다. 절대 경로이다.
- s2adapi.container.<컨테이너명칭>.dire.class : 모듈 Jar 파일들의 압축을 풀 디렉토리 경로이다.
- s2adapi.container.<컨테이너명칭>.service.config.path : 서비스 설정파일이 존재하는 위치이다. 클래스 패스로 지정한다.
- s2adapi.container.<컨테이너명칭>.service.config : 실제 로딩할 서비스 설정파일의 파일 명칭 패턴을 지정한다. 콤마(,) 를 사용하여 여러개를 나열할 수 있다.

다음은 실제 설정 예시이다.

**service configuration(s2adapi-config.properties)**

```
####################################################################
#        s2adapi ServiceContailer configurations
####################################################################

s2adapi.container.default.name=s2adapi
s2adapi.container.s2adapi.impl=s2.adapi.framework.container.impl.ReloadableXmlServiceContainer
s2adapi.container.s2adapi.reload.interval=5
s2adapi.container.s2adapi.dir.module=${s2adapi.config.base.absolute}/../../../components
s2adapi.container.s2adapi.dir.class=${s2adapi.config.base.absolute}/../../../../../work/\_s2adapi\_
s2adapi.container.s2adapi.service.config.path=svcdefs
s2adapi.container.s2adapi.service.config=s2adapi\_.*\\\\.xml

# 참고) ${s2adapi.config.base.absolute} 는 이 설정 파일(s2adapi-config.properties)의 절대 경로를 담고 있다.
```

# 3. Context

## 1) ApplicationContext

어플리케이션 실행 관련 정보를 제공하는 Context 객체이다. 다음의 메소드를 제공한다.

- public String getApplicationName()
	- 어플리케이션 명칭을 반환한다. Web Application 이 로딩되는 시점에 ServletContext 의 getServletContextName() 을 사용하여 설정되는 값이다.
- public ServiceContainer getServiceContainer()
	- 어플리케이션에 사용되는 ServiceContainer 객체를 반환한다.

## 2) ServiceContext

현재 처리 중인 서비스의 세션 정보를 제공하는 객체이며 현재 실행에 대한 ServiceContext 객체는 ContextManager.getServiceContext() 메소드를 사용하여 가져올 수 있다. 사용되는 세션 항목들을 프레임워크에서 따로
정의한 것은 없으며 구현되는 시스템 내에서 필요한 세션 정보를 서비스 호출 시점에 설정하도록 기능을 구현해야한다. 세션으로 정의된 값은 이후 DAO 등의 서비스에서 Query 바인딩 파라메터로 사용할 수 있다.

ServiceContext 가 제공하는 메소드들은 다음과 같다.
- 패키지 명 : s2.adapi.framework.context.ServiceContext
- public Object getRole(String key)
	- key 에 해당하는 세션 값을 반환한다.
- public void setRole(String key, Object role)
	- key 에 해당되는 세션값으로 role 객체를 설정한다.
- public void setRole(Map<? extends String,? extends Object> roleMap)
	- Map 객체에 담겨진 항목들을 모두 세션 값으로 설정한다.
- public boolean isDebugingMode()
	- 현재 실행이 디버깅 모드인지 여부를 반환한다. 업무 로직 개발시 디버깅 상태에 따라 분기하기 위한 기능을 제공하는 용도이다.

-   public void setDebugingMode(boolean debuging)
	- 현재 실행을 디버깅 모드로 설정하거나 리셋한다.

## 3) DiagnosticContext

하나의 서비스를 처리할 때 내부 주요 시점에서의 성능 정보나 모니터링 정보를 확인하기 위하여 사용된다. 아래의 ContextAwareService의 getDiagnosticContext() 메소드를 통하여 받아 올 수 있으며 Map 객체로 관리된다. 저장되는 정보는 임의의 값이며 해당 시스템 내에서 표준을 정하여 사용하면된다. 프레임워크 내에서 사용되는 항목들은 다음과 같다.

- diag.web.action.uri : 호출된 HttpServletRequest의 URI 값
- diag.web.action.target : 호출되는 서비스 명
- diag.web.action.method : 호출되는 메소드 명
- diag.web.action.stime : 서비스 실행 시작시간
- diag.web.action.etime : 서비스 실행 종료시간
- diag.web.action.rtime : 서비스 실행시간
- diag.web.action.svc.errmsg : 서비스 오류 발생시 오류 메시지
- diag.web.action.retcount : 서비스 정상 실행시 반환되는 결과의 row 수 (반환 결과가 ValueObject 인 경우)

## 4) ContextAwareService

상속을 통하여 서비스의 세션정보를 알려주거나 트랜젝션 관련 기능을 제공한다. 이 클래스를 상속한 구현 클래스는 아래의 메소드를 사용할 수 있다.

- 패키지 명 : s2.adapi.framework.context.ContextAwareService
- protected ServiceContext getServiceContext()
	- 현재 처리중인 서비스의 ServiceContext 객체를 반환한다.
- protected Map<String,Object> getDiagnosticContext()
	- 현재 처리중인 서비스의 DiagnosticContext 정보를 Map 객체로 반환한다.
- protected ApplicationContext getApplicationContext()
	- 현재 처리중인 서비스의 ApplicationContext 객체를 반환한다.
- protected void setRollbackOnly() throws IllegalStateException
	- 현재 처리중인 서비스의 트랜젝션을 Rollback 상태로 마킹한다.
	- Rollback 상태로 마킹된 서비스는 서비스 실행이 완료되는 시점에 rollback 처리된다.

## 5) ContextManager

ServiceContext, DiagnosticContext 그리고 트랜젝션 상태정보를 관리하는 클래스이다. 위에서 설명한 ContextAwareService 의 메소드들도 모두 ContextManager 의 기능을 통하여 제공된다. ContextManager 에서 제공하는 정보는 모두 ThreadLocal 변수로 관리되며 아래의 메소드들을 제공한다. 제공되는 메소드들은 모두 static 메소드이다.

- public static int getContextType()
	- 현재 실행 중인 환경이 Web Application (0) 인지 일반 Java Application (1) 인지 반환한다.
- public static void setServiceContext(ServiceContext svcCtx)
	- ServiceContext 객체를 현재 쓰레드에 설정한다.
-  public static ServiceContext getServiceContext()
	- 현재 쓰레드에 설정된 ServiceContext 객체를 반환한다.
- public static void clearServiceContext()
	- 현재 쓰레드에 설정된 ServiceContext 객체를 삭제한다.
- public static Map<String,Object> getDiagnosticContext()
	- 현재 쓰레드에 설정된 DiagnosticContext 정보를 Map 객체로 반환한다.
- public static void clearDiagnosticContext()
	- 현재 쓰레드에 설정된 DiagnosticContext 정보를 삭제한다.
- public static void setDebugingMode(boolean debuging)
	- 현재 쓰레드를 디버깅 모드로 설정하거나 (true) 리셋한다. (false)
- public static boolean isDebugingMode()
	- 현재 쓰레드의 설정된 디버깅 모드 값을 반환한다.
- public static void setRollbackOnly()
	- 현재 쓰레드에 할당된 트랙젝션을 rollback 상태로 마킹한다.
- public static void clearAll()
	- 현재 쓰레드에 할당된 ServiceContext 객체와 DiagnosticContext 객체를 모두 삭제한다.

# 4. AOP

Aspect Oriented Programming (AOP)은 업무 로직에 공통적으로 사용되는 여러 기능들 (logging, transaction, security 등) 을 모듈화(Advice)하여 이를 런타임에 업무 로직에 삽입(Aspect Weaving)할 수 있도록 해주는 프로그래밍 기법이다. 본 프레임워크에서는 ServiceContainer 서비스 설정을 통하여 Aspect Weaving 기능을 제공하며 다음과 같은 특징이 있다. 

- [AOP Alliance](http://aopalliance.sourceforge.net/) 의 표준화된 interface 를 기반으로 하고 있으며 그중 Method Interceptor 를 구현하였다.
- Java의 Dynamic Proxy 를 기반으로 구현된 AOP 프레임워크이다.  그러므로 AOP 를 적용하고자 하는 서비스는 반드시 Interface 가 명시되어야 한다.

## 1) Method Interceptor

Method Interceptor 는 특정 객체의 메소드 실행 전과 실행 후에 특정 실행 로직(Advice)을 삽입하는 형태의 Aspect Weaving 이다. 본 프레임워크에서는 Method Interceptor 형태의 AOP 기능만을 제공하며 AOP Alliance 의 관련 Interface 를 구현하였다. 아래는 AOP Alliance 의 interface 들이다.

**AOPAlliance Interfaces**

```java
public interface Joinpoint {
   Object proceed() throws Throwable;
   Object getThis();
   AccessibleObject getStaticPart();
}

public interface Invocation extends Joinpoint {
   Object[] getArguments();
}

public interface MethodInvocation extends Invocation {
   Method getMethod();
}

public interface Advice {
}

public interface Interceptor extends Advice {
}

public interface MethodInterceptor extends Interceptor {
   Object invoke(MethodInvocation var1) throws Throwable;
}
```

### (1) MethodInvocation

AOP 에서는 메소드 호출을 MethodInvocation 이라는 Interface 로 표현하고 있다. 본 프레임워크에서는 이를 구현한
**s2.adapi.framework.aop.MethodInvocation** 클래스를 제공하고 있으며 구현된 각 메소드의 의미는 다음과 같다.

- public Method getMethod()
	- 호출되고 있는 메소드 객체를 반환한다.
- public Object[] getArguments()
	- 호출되고 있는 메소드의 Arguments 들을 배열로 반환한다.
- public Object getThis()
	- 메소드 호출 대상 객체를 반환한다. 
- public Object proceed() throws Throwable
	- 호출된 메소드를 진행하고 해당 메소드 실행으로 반환된 객체를 반환한다.

### (2) MethodInterceptor 구현하기

- MethodInterceptor 를 구현하기 위하여 클래스를 생성하고 org.aopalliance.intercept.MethodInterceptor 를 implements 한다. 
- public Object invoke(MethodInvocation invocation) throws Throwable 를 구현한다. 
- invoke() 메소드를 구현하는 일반적인 패턴은 아래와 같다.
	- 원래의 메소드 호출전에 수행할 작업을 구현한다.
	- invocation.proceed() 를 호출하여 원래의 메소드를 수행한다.
	- 리턴된 결과를 이용해 필요한 작업을 수행한다.
	- 메소드를 호출한 쪽으로 반환할 결과를 리턴한다. 이것은 proceed() 의 결과를 꼭 그대로 반환할 필요는 없다.
	- 다음은 실제 MethodInterceptor 의 구현 예시이다. 메소드 실행에 대한 정보를 로그에 남기는 기능을 수행한다.

**MethoInterceptor 구현 예시**

```java
package test.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.slf4j.Logger;

public class SimpleLogInterceptor implements MethodInterceptor {

    public Object invoke(MethodInvocation invocation) throws Throwable 
    {

        long stime = System.currentTimeMillis() ;

        // logging 용 객체를 생성
        Logger log = Logger.getLogger(invocation.getThis().getClass());

        // 호출된 메소드 명칭과 전달된 Argument들을 logging
        log.info(invocation.getMethod().getName()+"() starts.");

        Object[] args = invocation.getArguments();

        if (args != null) {
            for(int i = 0; i < args.length; i++) {
                log.info(String.valueOf(invocation.getArguments()[i]));
            }
        } 
        else {
            log.info("No Input Argument\...");
        }

        // 원래 메소드를 호출한다.

        Object retObj;

        try {
            retObj = invocation.proceed();
        }

        catch (Throwable e) {
            long etime = System.currentTimeMillis() ;
            log.error(invocation.getMethod().getName() + "() errors.[" + e.toString() + "]("  + (etime-stime) + " msecs}");

            throw e;
        }

        // 리턴된 결과값을 logging
        long etime = System.currentTimeMillis() ;
        log.info(String.valueOf(retObj));
        log.info(invocation.getMethod().getName() + "() ends.(" + (etime - stime) + " msecs}");

        // 원 메소드에서 반환한 값을 그대로 다시 반환한다.
        return retObj;
    }
}
```

### (3) 서비스 설정파일에 등록하기

- 작성된 MethodInterceptor 구현 클래스는 서비스 설정파일에 등록되어야한다. 이때 작성된 MethodInterceptor 자체를 등록하는 것 외에도 이를 다시 InterceptorProxy 에 등록하는 과정이 필요하다. 
- InterceptorProxy 는 실제 MethodInterceptor 기능을 다른 서비스에 적용하기 위하여 제공되는 기능이며 여기에는 하나 이상의 MethodInterceptor 를 설정할 수 있어 MethodInterceptor 가 여러 개 차례로 적용될 수 있도록 할 수 있다.
- Interceptor 를 서비스에 적용하기 위해서는 설정된 InterceptorProxy 의 서비스 명을 적용하고자 하는 서비스의 interceptor 속성에 지정한다.
- 다음은 서비스 설정파일 작성 예시이다.

**MethodInterceptor 서비스 설정하기**

```xml
<services module="system">
    <service name="myInterceptor1"
             interface="org.aopalliance.intercept.MethodInterceptor" 
             class="test.aop.SimpleLogInterceptor "
             activate="true"
             pre -init="false"
             singleton="true">
    </service>

    <service name="testInterceptorProxy" 
             interface="s2.adapi.framework.aop.InterceptorProxy" 
             class="s2.adapi.framework.aop.MethodInterceptorProxy" 
             activate="true"
             pre-init="false"
             singleton="true">
        <property name="interceptor" ref="**myInterceptor1**"/> 
        <property name="interceptor" ref="myInterceptor2"/>     
    </service>
</services>
```

**Interceptor 적용하기**

```xml
<services module="store" package="s2.adapi.app.service.store" pre-init="false">
    <service name="StoreServiceDAO"
             interface="${package}.dao.StoreServiceDAO"
             class="${package}.dao.StoreServiceDAOImpl"
             interceptor="system.testInterceptorProxy"
             singleton="true">
        <property name="datasource" ref="jdbc.apidb"/>
        <property name="sql" value="sqls/store_service_sqls.xml"/>
    </service>
</services>
```

위의 예에서는 SimpleLogInterceptor 클래스를 myInterceptor1 이라는 명칭으로 서비스 등록하였으며 이를 다시 testInterceptorProxy 서비스에 injection 하였다. testInterceptorProxy 는 프레임워크가 제공하는 MethodInterceptorProxy 를 사용하기 위하여 등록하였으며 MethodInterceptor 를 실제 업무용 서비스들에 적용하기 위하여 반드시 필요하다. MethodInterceptorProxy 는 injection 된 MethodInterceptor 들을 순서대로 실행시킨다. 위의 예시에서는 myInterceptor1 과 myInterceptor2 라는 2개의 MethodInterceptor 서비스를 injection 하였으며 이 경우 실행 순서는 myInterceptor1 -> myInterceptor2 -> 원래 메소드 실행 -> myInterceptor2 -> myInterceptor1 의 순서로 실행이 이루어진다.

업무 서비스에서 위 MethodInterceptor 를 적용하기 위하여 서비스 설정파일의 interceptor 속성에 사용하고자하는 MethodInterceptorProxy 서비스명을 설정하면 된다. interceptor 로 설정하였여도 해당 MethodInterceptorProxy 의 activate 설정이 false 로 되어 있으면 적용되지 않는다. 또 MethodInterceptorProxy 에 inject 된 각각의 MethodInterceptor들 중 activate 가 false 설정된 것도 적용되지 않는다.

## 2) Auto Proxy

각각의 서비스는 서비스 설정파일에 interceptor 설정을 통하여 원하는 MethodInterceptor 를 런타임에 적용할 수 있다. 그러나 이러한 방법은 각각의 서비스에서 개별로 설정을 해야하므로 Interceptor 를 변경하거나 제거하는 것이 매우 번거롭다. 각각의 서비스에서 interceptor 설정을 하지 않아도 원하는 서비스에 MethodInterceptor 를 설정할 수 있도록 제공되는 기능이 AutoProxy 이다.

여러 개발자가 같이 진행하는 큰 규모의 프로젝트에서는 AutoProxy 기능을 통하여 공통 Interceptor 설정을 쉽게 변경할 수 있는 장점이 있다. 아래는 프레임워크에서 제공하는 RegexpPatternAutoProxy 의 서비스 설정 예시이다.

**RegexpPatterAutoProxy**

```xml
<services module="system">
    <service name="myInterceptor1"
             interface="org.aopalliance.intercept.MethodInterceptor" 
             class="test.aop.SimpleLogInterceptor "
             activate="true"
             pre -init="false"
             singleton="true">
    </service>

    <service name="autoproxy"
             class="s2.adapi.framework.aop.auto.RegexpPatternAutoProxy"
             activate="true"
             pre-init="true"
             singleton="true">
       <property name="interceptor" ref="myInterceptor1"/>
       <property name="packagePattern" value="com\\.s2adapi\\.rwdapp\\..*"/>
  </service>

</services>
```

위의 예에서 myInterceptor1 으로 등록된 MethodInterceptor 를 autoproxy 에 inject 하였다. 등록된 autoproxy 서비스는 프레임워크에서 제공하는 s2.adapi.framework.aop.auto.RegexpPatter AutoProxy 클래스를 사용하였다.
RegexpPatterAutoProxy는 packagePattern 으로 지정된 패턴 문자열에 해당되는 클래스들에 자동적으로 MethodInterceptor 를 적용한다. 위의 예시에서는 구현 클래스가 s2.adapi.addzzl. 로 시작하는 모든 서비스에 자동적으로 SimpleLogInterceptor 가 적용되도록 설정하였다.

## 3) Target Proxy

Target Proxy 는 앞서 기술된 MethodInterceptor 와는 조금 다른 개념이다. Target Proxy 는 실제 호출되는 타겟 서비스를 흉내내기위하여  제공되는 기능이다. 서비스를 호출하는 입장에서 보면 같은 interface 를 사용하여 서비스를 호출하지만 그 서비스를 제공하는 원래의 클래스를 TargetProxy 가 중간에 흉내를 낸다고 생각하면 된다.

예를 들어 A 라는 서비스가 B 라는 서비스를 사용한다고 하자. 물리적으로 같은 서버에 같이 존재한다고 하면 A 가 B 를 그대로 호출(method invocation)하면된다. 이러한 구성에서 만약 B 서비스를 물리적으로 다른 서버로 옮겼다면 A 가 B 를 바로 호출할 수 없다. 이 경우 A 가 있는 시스템에 TargetProxy 를 생성하여 마치 B와 같은 역활을 시키고 (B의 interface 를 제공) TargetProxy 는 A의 메소드 호출을 받으면 원격지에 있는 실제 B 를 호출하여 결과를 반한한다고 하면 A 입장에서는 B가 마치 같은 시스템에 있는 것과 동일하게 사용할 수 있다. 이렇게 실제 서비스를 호출하는 방식을 다양하게 제공할 수 있는 것이 TargetProxy 이다. 

### (1) ThreadLocalTargetProxy

B 라는 서비스가 있다고 하자 그런데 B 서비스는 다중 쓰레드 환경을 제공하지 않아 한번에 하나의 쓰레드만 실행할 수 있다. 이 경우 실제 다중 쓰레드 환경에서는 각각의 호출을 모두 동기화시켜야 하는데 결국 동시성이 매우 제약이되고 B 서비스로 인하여 전체 시스템의 병목 현상이 발생할 수 있다. 이런 상황에서 ThreadLocalTragetProxy 를 사용할 수 있다. ThreadLocalTargetProxy는 원래 서비스를 실행중인 Thread 의 Local 변수로 생성하고 이를 사용하여 원래 서비스를 흉내낸다.

아래는 ThreadLocalTargetProxy 의 서비스 설정 예시이다.

**Test 코드 예시**

```java
package test.aop;

// ThreadLocalTargetProxy 테스트용 서비스 인터페이스
public interface ThreadTest {
    int callMe();
}

// ThreadLocalTargetProxy 테스트용 서비스 구현 클래스
public class SingleThreadTestImpl implements ThreadTest {

    private int count = 0;

    @Override
    public int callMe() {
        return count++;
    }

}

// 서비스를 호출하는 테스트 클래스

public class TestServiceCaller {

    private ThreadTest test = null;

    // 호출할 ThreadTest 서비스객체를 inject 받는다.

    public void setTestService(ThreadTest test) {
        this.test = test;
    }

    public void runTest() {

        for(int i = 0; i < 0; i++) {
            new Thread() {
                public void run() {
                    test.callMe();
                }
            }.start();
        }
    }
}
```

**서비스 설정 예시**

```xml
<services module="test">

    <service name="singleThreadService"
             interface="test.aop.ThreadTest" 
             class="test.aop.SingleThreadTestImpl "
             singleton="false">
    </service>

    <service name="multiThreadService"
             interface="test.aop.ThreadTest" 
             class="s2.adapi.framework.aop.target.ThreadLocalTargetProxy"
             singleton="true">
       <property name="targetName" value="singleThreadService"/>
    </service>

    <service name="callerService"
             class="test.aop.TestServiceCaller">
        <property name="testService" ref="multiThreadService"/>
    </service>

</services>
```

위의 예시에서 callerService 는 ThreadTest 객체를 inject 받아서 사용하고 있다. 그러나 inject 된 ThreadTest 구현 클래스는 실제로는 ThreadLocalTargetProxy 객체이며 이 객체는 ThreadTest 인터페이스를 제공하고 있기때문에 callerService 에서는 문제없이 호출이 가능하다. multiThreadService 로 설정된 ThreadLocalTargetProxy 는 targetName 속성으로 실제 흉내를 내야할 서비스의 명칭(singleThreadService)을 전달받아서 실행시점에 ThreadLocal 변수에 해당 서비스 객체를 생성하여
호출하도록 구현되어 있다. 주의할 점은 실제 구현 서비스인 singleThreadService 는 singleton = "false" 로 설정해야한다.

### (2) RpcCallTargetProxy

RpcCallTargetProxy 는 원격사이트에 있는 서비스를 로컬에 있는 서비스처럼 사용할 수 있도록 기능을 제공해주는 TargetProxy 구현 클래스이다. RpcCallTargetProxy 를 사용하여 원격지 서비스를 호출하기 위해서는 해당 서버에 프레임워크에서 제공하는 RpcWebAction 서비스가 사용가능하도록 설정되어 있어야 한다. 

주의사항) RpcCallTargetProxy 는 원격 트랜젝션을 제공하지 않으므로 트랜젝션은 원격지와 분리되어 처리되므로 이점을 주의하여야한다.

다음은 RcpCallTargetProxy 를 사용하여 원격지에 있는 서비스를 호출하는 실제 예시이다.

**원격지 호출 대상 서비스 설정 (common.Common) 서비스 예시**

```xml
<services module="common" package="s2.adapi.ad.service" pre-init="false">

    <service name="Common"
             interface="${package}.common.Common"
             class="${package}.common.CommonImpl"
             singleton="true">
        <property name="dao" ref="CommonDAO"/>
    </service>

<service name="CommonDAO"
             class="s2.adapi.framework.dao.SqlQueryDAO"
             singleton="true">
        <property name="datasource" ref="jdbc.apidb"/>
        <property name="sql" value="sqls/service_common_sqls.xml"/>
    </service>

</services>   
```
 
**원격지 RpcWebAction 서비스 설정 예시**

```xml
<services module="system">

    <!-- RPC -->
    <service name="rpcDAO"
             class="s2.adapi.framework.dao.SqlQueryDAO"
             singleton="true">
        <property name="datasource" ref="jdbc.apidb"/>
        <property name="sql" value="sqls/rpc_service_sqls.xml"/>
    </service>

    <service name="RpcService"
             interface="s2.adapi.framework.web.rpc.RpcService"
             class="${package}.rpc.AuthRpcService"
             singleton="true">
        <property name="dao" ref="rpcDAO"/>
    </service>

    <service name="rpc.main"
             interface="s2.adapi.framework.web.action.WebAction"
             class="s2.adapi.framework.web.rpc.RpcWebAction"
             singleton="true">
        <property name="rpcService" ref="RpcService"/>
    </service>

</services>
```

RpcWebAction 을 설정하기 위해서는 실제 서비스 호출 기능을 수행할 RpcService 의 구현 클래스가 필요하다. RpcService 는 실제 프로젝트에 맞추어 필요한 세션 정보나 인증 등의 처리 등을 구현한다. 

아래는 RpcService 인터페이스와 실제 구현 클래스 예시이다.

**RpcService 인터페이스**

```java
package s2.adapi.framework.web.rpc;

public interface RpcService {
    /**
     * 호출할 서비스 명칭과 메소드 명칭 그리고 메소드에 전달할 파라메터들을 배열로 받는다. 실제 서비스를 찾아서 호출해야하며 그 결과를 반환하도록 구현해야한다.
     * 
     * @param serviceName 호출할 서비스 명칭
     * @param methodName 호출할 메소드 명칭
     * @param params 메소드에 전달할 파라메터 들
     * @return 서비스 호출 결과
     * @throws Throwable
     */

    public Object invokeService(String serviceName, String methodName, Object[] params) throws Throwable;
}
```

**RpcService 구현 예시**

```java
package s2.adapi.common.service.rpc;

import s2.adapi.framework.context.ContextAwareService;
import s2.adapi.framework.context.ServiceContext;
import s2.adapi.framework.dao.SqlQueryDAO;
import s2.adapi.framework.util.ObjectHelper;
import s2.adapi.framework.vo.ValueObject;
import s2.adapi.framework.web.rpc.RpcService;

public class AuthRpcService extends ContextAwareService implements RpcService {

    private SqlQueryDAO rpcServiceDAO = null;

    public void setDao(SqlQueryDAO dao) {
        rpcServiceDAO = dao;
    }

    public Object invokeService(String serviceName, String methodName, Object[] params) throws Throwable {

        // RPC 코드확인 하여 IP 및 권한 확인한다.
        ValueObject rpcVO = rpcServiceDAO.executeQuery("getRpcInfo", null);

        if (rpcVO.size() == 0) {
            throw new Exception("no authorized requst...");
        }

        // 조회된 정보를 세션에 설정한다.
        ServiceContext serviceContext = getServiceContext();

        serviceContext.setRole(rpcVO.get(0)); // cust_id, up_cust_id, cust_nm, logn_id, auth_cd, tz

        // 요청한 서비스를 호출한다.
        Object svcObject = getApplicationContext().getServiceContainer().getService(serviceName);
        Object retObject = ObjectHelper.invoke((Class<?>)null, svcObject, methodName, null, params);

        return retObject;
    }
}
```

**호출하는 쪽의 서비스 설정 (RpcTargetProxy) 예시**

```xml
<services module="coin" package="s2.adapi.app.portal" pre-init="false">

    <service name="CommonProxy"
             interface="s2.adapi.ad.service.common.Common"
             class="s2.adapi.framework.web.rpc.RpcCallTargetProxy"
             singleton="true">
        <property name="rpcClient" ref="KkcRpcClient"/>
        <property name="targetName" value="common.Common"/>
    </service>

    <service name="KkcRpcClient"
             class="s2.adapi.framework.web.rpc.RpcClient"
             singleton="true">
        <property name="rpcKey" value="kkc"/>
        <property name="userKey" value="logn_id"/>
        <property name="serviceUrl" value="http://admin.s2adapi.com/s2adapi/system.rpc.main"/>
    </service>

</services> 
```

RpcClient 는 원격지 서버로 RPC 호출을 수행하는 기능을 제공하며 각 속성의 의미는 다음과 같다.

- rcpKey : 클라이언트 구분값으로 사용된다. 원격지 서버의 ServiceContext 에 "rpckey" 라는 이름으로 설정된다.
- userKey : 사용자 구분값으로 사용되는 ServiceContext 의 명칭이다. 호출하는 시스템에서 사용자 구분값을 가져오기 위하여 사용되는 key 값이며, 원격지 서버에서는 항상 "mduname" 라는 이름으로 ServiceContext 에 설정된다.
- serviceUrl : 호출할 원격지서버의 RpcWebAction URL 이다. 원격지 서버의 설정을 보면 system.rpc.main 으로 설정되어 있는 것을 확인할 수 있다.

CommonProxy 로 설정된 서비스는 interface 가 원격지 서버의 common.Common 서비스와 동일하기 때문에 마치 로컬에 있는 서비스처럼 사용할 수 있다. 실제 호출이 되면 이를 RpcCallTargetProxy 가 RpcClient 를 사용하여 원격지의 common.Common 서비스를 호출하는 구조이다.

### (3) TargetProxy  구현하기

원하는 기능을 수행하는 TargetProxy 의 구현 클래스를 작성할 수 있다. TargetProxy 는 프레임워크에서 제공하는
s2.adapi.framework.aop.target.AbstractTargetProxy 클래스를 상속받아서 아래의 invoke() 메소드를 override 하면 된다.

- public Object invoke(Method method, Object[] args) throws Throwable
	- method : 호출되는 Method 객체이다.
	- args : 호출되는 method 의 argument 들이다.
	- return : 실행 결과로 반환되는 객체이다.

아래는 ThreadLocalTargetProxy 의 전체 소스이다.

**ThreadLocalTargetProxy**

```java
package s2.adapi.framework.aop.target;

import java.lang.reflect.Method;

/**
 * ThreadLocal 변수를 사용하여 쓰레드 별로 target 객체를 생성하는 TargetProxy의 구현클래스이다.
 */

public class ThreadLocalTargetProxy extends AbstractTargetProxy {

    protected static ThreadLocal<Object> targetObject = new ThreadLocal<Object>();
    protected String targetName = null;

    public void setTargetName(String name) {
        targetName = name;
    }

    public Object invoke(Method method, Object[] args) throws Throwable {

        Object svcObject = targetObject.get();
        if (svcObject == null) {
            svcObject = getServiceContainer().getService(targetName);
            targetObject.set(svcObject);
        }

        return method.invoke(svcObject, args);
    }
}
```

# 5. Data Access Object

Data Access Object(DAO) 는 XML 파일로 정의된 SQL 문장을 통하여 다양한 데이터소스에 대한 접근기능을 제공한다. 아래의 구현클래스들을 제공한다.

- JdbcDAO : Jdbc 를 사용한 SQL 실행기능을 제공한다.
- JdbcQueryDAO : Framework의 Query  컴포넌트를 사용할 수 있도록 JdbcDAO의 기능을 확장한 클래스이다. 업무별 DAO 클래스는 JdbcQueryDAO를 상속하여 구현하도록 설계되어 있다.
- SqlQueryDAO : 업무용 DAO 클래스들을 별도로 작성하지 않고 바로 객체를 생성하여 사용할 수 있도록 기능을 제공한다.

## 1) JdbcDAO

Jdbc 를 직접적으로 이용하는 DAO 클래스이다.

-   업무별 DAO를 상속을 통하여 구현한다. 사용되는 SQL 문장은 DAO 클래스내에 문자열로 정의하여 사용한다.

**작성 예시**

```java
package test.dao;

import java.sql.Connection;
import java.util.ArrayList;
import s2.adapi.framework.dao.JdbcDAO;
import s2.adapi.framework.dao.SqlQueryException;
import s2.adapi.framework.dao.sql.DataSource;
import s2.adapi.framework.vo.ValueObject;

public class CreditAppraiseListDAO extends JdbcDAO {

    private static final String GET_CREDIT_LIST_SQL = ""
            + "SELECT rownum, guarnt_no, jumin_no, cust_no, last_crdt_grd, score"
            + "  FROM tbl_usercredit"
            + " WHERE guarnt_div = ?"
            + "   AND actp_dy = ?"
            + "   AND guarnt_no > ?"
            + " ORDER BY guarnt_no";

    public CreditAppraiseListDAO() {
    }

    public CreditAppraiseListDAO(DataSource dsn) {
        super(dsn);
    }

    public ValueObject getCreditAppraisList (ValueObject paramVO) throws SqlQueryException {

        Connection con = null;
        ValueObject getVO = null;

        try {
            con = getConnection();

            ArrayList<Object> params = new ArrayList<Object>();
            params.add(paramVO.get("guarnt_dv"));
            params.add(paramVO.get("acpt_dy"));
            params.add(paramVO.get("nk_guarnt_no"));

            getVO = executeQuery(con, GET_CREDIT_LIST_SQL, params);
        }
        finally {
            close(con);
        }

        return getVO;
    }
}
```

JdbcDAO 가 제공하는 메소드들은 다음과 같다.

### (1) 생성자

- public JdbcDAO() : 디폴트 생성자이다. 디폴트 DataSource 를 사용하여 초기화된다.
- public JdbcDAO(DataSource datasource) : DataSource 를 지정하여 생성한다.

### (2) Connection 관련

- protected Connection getConnection() throws SqlQueryException : Connection 객체를 반환한다.
- protected void close(Connection con) throws SqlQueryException : Connection 객체를 close 한다.
- protected void close(Connection con, PreparedStatement ps, ResultSet rs) throws SqlQueryException : Connection 객체와 PreparedStatement 객체 그리고 ResultSet 객체를 한번에 close 한다.

### (3) SELECT 문

- protected ValueObject executeQuery(Connection con, String sql, List\<Object> param\[, ResultMap rmap]) throws SqlQueryException
- protected ValueObject executeQuery(Connection con, String sql, List\<Object> param, ValueObject pageVO\[, ResultMap rmap]) throws SqlQueryException
	- Select 조회 SQL 을 수행한다.
	- sql : PreparedStatement 생성을 위한 SQL 문장
	- params : ? 파라메터에 매핑될 값들을 순서대로 담은 List 객체
	- pageVO : 페이지단위의 조회를 실행하고자 할 경우 페이지 정보를 담아서 전달한다.
	- ResultMap 은 DB에서 컬럼의 값을 읽어올때 어떤 ColumnReader 를 사용하여 값을 읽어 올지 지정하는 용도로 사용된다.
	- Return : 조회된 값들을 ValueObject 에 담아서 반환한다. 조회된 결과가 없는 경우에는 비어있는 ValueObject 객체를 반환한다.

### (4) INSERT, UPDATE, DELETE

- protected int executeUpdate(Connection con, String sql, List\<Object> param) throws SqlQueryException
	- Insert, update, delete SQL 문을 실행한다. 
	- sql : PreparedStatement 생성을 위한 SQL 문장
	- params : ? 파라메터에 매핑될 값들을 순서대로 담은 List 객체
	- Return : SQL 실행으로 영향을 받은 row 수를 반환한다. 
- protected ValueObject executeUpdateReturnKeys(Connection con, String sql, List\<Object> param, int numKeyCols\[, ResultMap ramp]) throws SqlQueryException
	- Insert SQL을 실행하며 실행결과로 테이블에 입력된 값을 다시 반환받기 위하여 사용된다.
	- numKeyCols : 리턴해 줄 컬럼수이며 DDL 로 테이블 생성시 만들어진 순서대로 담겨진다.
	- ResultMap 은 DB에서 컬럼의 값을 읽어올때 어떤 ColumnReader 를 사용하여 값을 읽어 올지 지정하는 용도로 사용된다. 컬럼의 타입별로 \[ColumnReader] 를 지정하거나 컬럼의 명칭에 따라서 ColumnReader 를 지정할 수 있다. ResultMap 을 지정하지 않는 경우에는 프레임워크 내부에 설정된 디폴트 ColumnReader 들이 사용된다. 디폴트 ColumnReader 들은 사용되는 DB의 종류에 따라서 다르게 설정되어 있다.
	- Return : 지정된 컬럼에 입력된 실제 값을 ValueObject 객체에 담아서 반환한다.

- protected ValueObject executeUpdateReturnKeys(Connection con, String sql, List\<Object> param, int\[] columnIndexes\[, ResultMap ramp]) throws SqlQueryException
	- Insert SQL을 실행하며 실행결과로 테이블에 입력된 값을 다시 반환받기 위하여 사용된다.
	- columnIndexes : 반환받을 컬럼의 위치 index 를 지정한다.
	- 나머지는 위 메소드와 동일한다.

- protected ValueObject executeUpdateReturnKeys(Connection con, String sql, List\<Object> param, String\[] columnNames\[, ResultMap ramp]) throws SqlQueryException
	- Insert SQL을 실행하며 실행결과로 테이블에 입력된 값을 다시 반환받기 위하여 사용된다.
	- columnNames : 반환받을 컬럼의 명칭을 지정한다.
	- 나머지는 위 메소드와 동일하다.

### (5) Batch SQL

- protected int\[] executeBatch(Connection con, String sql, List<?>\[] params)
	- 하나의 SQL 문에 여러건의 파라메터로 반복실행하고자 할 경우 사용된다.
	- Insert, update, delete SQL 을 실행할 수 있다.
	- params : SQL 문에 순서대로 매핑될 파라메터 리스트가 여러건 필요하므로 이를 배열에 담아서 전달한다.
	- Return : 각 실행에 대하여 처리된 row 수가 담겨진 int[] 이 반환된다. 실행된 SQL 중 에러가 발생한 경우에는 관련된 에러코드가 반환된다.


## ColumnReader

데이터베이스 테이블의 값을 조회할 때 해당 컬럼의 JDBC Type에 따라서 적합한 ColumnReader가 사용된다. 

**ColumnReader.java 인터페이스**

```java
package s2.adapi.framework.dao.reader;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ResultSet과 CallableStatement에서 결과 data를 fetch해오는 인터페이스이다.
 */
public interface ColumnReader {

    /**
     * ResultSet 에서 해당 columnIndex에 해당하는 row의 값을 fetch하여 return하도록 하위 Class에서 구현해야 한다.
     * @param columnIndex fetch할 ResultSet의 해당 index
     * @param rs fetch할 대상이 되는 ResultSet
     * @return ResultSet에서 fetch한 value
     * @throws SQLException
     */

    public Object read(int columnIndex, ResultSet rs) throws SQLException;

    /**
     * 실행된 CallableStatement에서 해당 columnIndex에 해당되는 값을 fetach하여 return 하도록 하위 Class에서 구현해야 한다.
     * @param columnIndex
     * @param cstmt
     * @return
     * @throws SQLException
     */
    public Object read(int columnIndex, CallableStatement cstmt) throws SQLException;

}
```

### (1) JdbcDAO가 제공하는 ColumnReader 들은 아래와 같다.

- StringColumnReader : 컬럼값을 String으로 읽어와서 java.lang.String 객체로 반환한다.
- DateColumnReader : 컬럼값을 Date으로 읽어와서 java.sql.Date 객체로 반환한다.
- TimeColumnReader : 컬럼값을 Time으로 읽어와서 java.sql.Time 객체로 반환한다.
- TimeStampColumnReader : 컬럼값을 Timestamp로 읽어와서 java.sql.Timestamp 객체로 반환한다.
- ByteColumnReader : 컬럼값을 Byte로 읽어와서 java.lang.Byte 객체로 반환한다.
- BooleanColumnReader : 컬럼값을 Boolean 으로 읽어와서 java.lang.Boolean 객체로 반환한다.
- ShortColumnReader : 컬럼값을 BigDecimal로 읽어와서 java.lang.Short 객체로 반환한다.
- IntColumnReader : 컬럼값을 BigDecimal로 읽어와서 java.lang.Integer 객체로 반환한다.
- LongColumnReader : 컬럼값을 BigDecimal으로 읽어와서 java.lang.Long 객체로 반환한다.
- RealColumnReader : 컬럼값을 BigDecimal로 읽어와서 java.lang.Float 객체로 반환한다.
- DoubleColumReader : 컬럼값을 BigDecimal로 읽어와서 java.lang.Double 객체로 반환한다.
- ScalarColumnReader : 컬럼값을 BigDecimal로 읽어와서 소수점 자리값이 없으면 Integer 객체로, 소수점 자리값이 있으면 Double 객체로 반환한다.
- BigDecimalColumnReader : 컬럼값을 BigDecimal 로 읽어와서 java.math.BigDecimal 객체로 반환한다.
- BinaryStreamColumnReader : 컬럼값을 BinaryStream 으로 읽어와서 byte[]  객체로 반환한다.
- BLOBColumnReader : 컬럼값을 Blob 으로 읽어와서 byte[]  객체로 반환한다.
- CharStreamColumnReader : 컬럼값을 CharacterStream으로 읽어와서 char[] 객체로 반환한다.
- CLOBColumnReader : 컬럼값을 CharacterStream으로 읽어와서 java.sql.Clob 객체로 반환한다.
- CLOBStringColumnReader : 컬럼값을 Clob 으로 읽어와서 java.lang.String 객체로 반환한다.
- EpochTimeStampColumnReader : 컬럼값을 TimeStamp 로 읽어와서 GMT 1970년 1월 1일 자정 기준으로 해당 시간까지의 milli-second 로 반환한다. 
- FormattedDateColumnReader : 컬럼값을 Date로 읽어와서 format을 적용하여 java.lang.String 객체로 반환한다.
- FormattedTimeColumnReader : 컬럼값을 Time으로 읽어와서 format을 적용하여 java.lang.String 객체로 반환한다.
- FormattedTimeStampColumnReader : 컬럼값을 Timestamp로 읽어와서 format을 적용하여 java.lang.String 객체로 반환한다.
- FormattedNumberColumnReader : 컬럼값을 Double로 읽어와서 format을 적용하여 java.lang.String 객체로 반환한다.
- SimpleIntegerColumnReader : 컬럼값을 Int 로 읽어와서 Int 로 반환한다.
- ISOStringColumnReader : DB에 저장된 한글의 charset 이 IOS8859_1 인 경우 이를 euc-kr 한글로 변환하기 위하여 사용한다.
- ArrayColumnReader : 컬럼값을 java.sql.Array 로 읽어와서 반환한다. 
- OracleCursorReader : CallableStatement의 OUT 파라메터를 ResultSet으로 읽어와서 다시 컬럼들을 해당 타입에 따라 읽어서 ValueObject 객체에 담아서 리턴한다.
- OracleXmlColumnReader : 컬럼값을 oracle.xdb.XMLType 으로 읽어와서 String으로 반환한다.
- DB2XmlColumnReader : 컬럼값을 com.ibm.db2.jcc.DB2Xml 으로 읽어와서 String으로 반환한다.
- ObjectColumnReader : 컬럼값을 Object로 읽어와서 java.lang.Object 객체로 반환한다.

### (2) 디폴트 ColumnReader (PostgreSQL, MySql, SqlServer, Sybase 포함)
![](vertopal_b378c48198ea424d858d505c4b800e25/media/image1.png)

### (3) Oracle 기본
ColumnReader![](vertopal_b378c48198ea424d858d505c4b800e25/media/image2.png)

### (4) DB2 기본
ColumnReader![](vertopal_b378c48198ea424d858d505c4b800e25/media/image3.png)

### (5) Global ColumnReader 설정하기

 프레임워크 내에서 DB별 디폴트 지정된 ColumnReader 를 사용하도록 되어있지만 특정 타입에 대해서는 다른 ColumnReader 를 사용하고자 할 경우에는 GlobalResultMap 파일을 사용하여 설정할 수 있다.

**작성 예시**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<sqls>

    <resultMap id="db2">
        <result type="SMALLINT" reader="s2.adapi.framework.dao.reader.IntColumnReader"/>
        <result type="DATE" reader="s2.adapi.framework.dao.reader.FormattedDateColumnReader"
                            format="yyyyMMdd"/>
        <result type="TIME" reader="s2.adapi.framework.dao.reader.FormattedTimeColumnReader"
                            format="HHmmss"/>
        <result type="TIMESTAMP" reader="s2.adapi.framework.dao.reader.FormattedTimeStampColumnReader"
                                 format="yyyyMMddHHmmssSSS"/>
    </resultMap>

    <resultMap id="oracle">
        <result type="SMALLINT" reader="s2.adapi.framework.dao.reader.IntColumnReader"/>
        <result type="DATE" reader="s2.adapi.framework.dao.reader.FormattedTimeStampColumnReader"
                            format="yyyyMMddHHmmss"/>
        <result type="TIMESTAMP" reader="s2.adapi.framework.dao.reader.FormattedTimeStampColumnReader"
                                 format="yyyyMMddHHmmssSSS"/>
    </resultMap>

    <resultMap id="postgres">
        <result type="TIMESTAMP" reader="s2.adapi.framework.dao.reader.EpochTimeStampColumnReader"/>
    </resultMap>

</sqls>
```

- \<resultMap id=""> : DBMS 종류별로 정의한다. id 에 DBMS 종류를 지정한다. DBMS 종류멸 명칭은 아래와 같다.
	- Oracle : oracle
	- DB2 : db2
	- PostgreSQL : postgres
	- MySql : mysql
	- SqlServer : mssql
	- Sybase : sybase
- \<result> : 하나의 매핑을 정의한다.
	- type : DB  컬럼의 타입 명칭을 지정한다. java.sql.Types 에서 정의한 명칭이다. (VARCHAR, DATE 등)
	- reader : 사용할 ColumnReader의 클래스명을 지정한다.
	- format : format 문자열이 필요한 ColumnReader 의 경우에는 format  문자열을 지정한다. (옵션사항)

## 2) JdbcQueryDAO

XML 파일로 작성된 SQL 문을 실행시키는 기능을 제공하는 DAO이며 [JdbcDAO]를 상속받아 구현되었다. XML내에서 여러가지 조건에 따른 분기처리가 가능하며 iBATIS의 XML 구조와 유사하게 구성되어 있다.

**Query XML 파일 예시**

```java
<?xml version=\'1.0\' encoding=\'utf-8\'?>
<sqls>

    <statement id="getCategoryList">
        SELECT a.cat_cd, COALESCE(c.cat_nm, b.cat_nm) as cat_nm, b.updt_dt 
          FROM str_pcatapps a, str_prdcats b 
               LEFT JOIN str_pcatlangs c ON b.cat_cd = c.cat_cd AND c.lang_cd = #lang_cd#
         WHERE a.app_id = #%appid#
           AND a.cat_cd = b.cat_cd
           AND b.use_yn = 'Y'
         ORDER BY a.seq_no
    </statement>

    <statement id="getCategoryImage">
        SELECT cat_img 
          FROM str_prdcats
         WHERE cat_cd = #cat_cd#
    </statement>

    <statement id="getProductListOfCategories">
        SELECT b.prd_cd, b.cat_cd, b.tr_type, 
               CAST(b.prd_price * #pnt_rate# AS NUMERIC(15,2)) as pnt_amt, b.updt_dt,
               COALESCE(c.corp_nm, b.corp_nm) as corp_nm,
               COALESCE(c.prd_nm, b.prd_nm) as prd_nm
          FROM str_prdapps a, str_products b LEFT JOIN str_prdlangs c ON b.prd_cd = c.prd_cd AND c.lang_cd = #lang_cd#
         WHERE a.app_id = #%appid#
           AND a.prd_cd = b.prd_cd
        <isNotEmpty property="cat_cd">
           AND b.cat_cd IN <iterate property="cat_cd" open="(" close=")" conjunction=",">#cat_cd[]#</iterate>
        </isNotEmpty>
        <isEmpty property="cat_cd">
           AND b.cat_cd = 0
        </isEmpty>
           AND b.use_yn = 'Y'
         ORDER BY b.ord_no, b.prd_price
    </statement>

    <statement id="getProductList">
        SELECT b.prd_cd, b.cat_cd, b.tr_type, b.updt_dt,
               CAST(b.prd_price * #pnt_rate# AS NUMERIC(15,2)) as pnt_amt, 
               COALESCE(c.corp_nm, b.corp_nm) as corp_nm,
               COALESCE(c.prd_nm, b.prd_nm) as prd_nm
          FROM str_prdapps a, str_products b LEFT JOIN str_prdlangs c ON b.prd_cd = c.prd_cd AND c.lang_cd = #lang_cd#
         WHERE a.app_id = #%appid#
           AND a.prd_cd = b.prd_cd
        <isNotEmpty property="cat_cd">
           AND b.cat_cd = #cat_cd# 
        </isNotEmpty>
           AND b.use_yn = 'Y'
         ORDER BY b.ord_no, b.prd_price
    </statement>
</sqls>
```

Query XML 파일의 작성 방법은 [Query XML] 을 참고한다.   

### (1) SqlQueryPage

하나의 Query 파일을 파싱하여 생성된다. Query 파일내의 <statement> 로 정의된 개별 SQL 문장을 SqlQuery 객체로 제공하며 또한 <resultMap> 으로 정의된 ResultMap 객체를 제공한다. 

- public SqlQueryPage(String path)
- public SqlQueryPage(String path, ClassLoader loader)
	- 생성자이다. path는 Query 파일의 클래스패스 상의 경로이다. ClassLoader를 지정하면 해당 클래스로더를 사용하여 Query 파일을 로딩하고 지정하지 않으면 기본 클래스로더를 사용하여 로딩한다.
- public SqlQuery getQuery(String id) throws SqlQueryException
	- id 로 지정된 하나의 SQL 문장을 SqlQuery 객체로 반환한다. 
- public ResultMap getResultMap(String id) throws SqlQueryException
	- id 로 지정된 하나의 ResultMap 객체를 반환한다.

### (2) SqlQuery

하나의 <statement> 를 표현하는 객체이다. SqlQueryPage 로부터 id 값을 통하여 획득할 수 있다. 내부는 <statement> 하위의 Node 구조를 SqlText 객체의 트리 구조로 가지고 있으며 파라메터 값에 따라 트리 탐색을 통하여 Sql 문장이 완성되도록 구현되어 있다.

### (3) JdbcQueryDAO

SqlQueryPage와 SqlQuery 객체를 사용하여 Sql 을 실행할 수 있도록 JdbcDAO를 확장하여 구현한 클래스이다. 다음의 메소드들을 제공한다.

- protected SqlQueryPage getQueryPage(String relativePath)
	- relatviePath 는 Query XML 파일의 경로이다. 자신의 패키지 경로를 기준으로 한 상대 경로이다.
- protected int executeUpdate(SqlQuery query, ValueObject paramVO) throws SqlQueryException
	- Insert, update, delete SQL 문을 실행한다.
	- query : 실행할 Query 정보를 가지고 있는 SqlQuery 객체이다.
	- paramVO : 파라메터 값을 가지고 있는 ValueObject 객체이다.
	- Return : SQL 실행으로 영향을 받은 row 수를 반환한다.
- protected ValueObject executeUpdateReturnKeys(SqlQuery query, ValueObject paramVO, int numKeyCols) throws SqlQueryException
	- Insert/Update SQL 을 실행하여 실행결과로 테이블에 입력된 값을 다시 반환받기 위하여 사용된다.
	- numKeyCols : 리턴해 줄 컬럼수이며 DDL 로 테이블 생성시 만들어진 순서대로 컬럼값이 담겨진다.
	- Return : 지정된 컬럼에 입력된 실제 값을 ValueObject 객체에 담아서 반환한다.
- protected ValueObject executeUpdateReturnKeys(SqlQuery query, ValueObject paramVO, int[] columnIndexes) throws SqlQueryException
	- Insert/Update SQL 을 실행하여 실행결과로 테이블에 입력된 값을 다시 반환받기 위하여 사용된다.
	- columnIndexes : 반환받을 컬럼의 위치 index 를 지정한다.
	- 나머지는 위 메소드와 동일한다.
- protected ValueObject executeUpdateReturnKeys(SqlQuery query, ValueObject paramVO, String[] columnNames) throws SqlQueryException
	- Insert/Update SQL 을 실행하여 실행결과로 테이블에 입력된 값을 다시 반환받기 위하여 사용된다.
	- columnNames : 반환받을 컬럼의 명칭을 지정한다.
	- 나머지는 위 메소드와 동일한다.
- protected ValueObject executeQuery(SqlQuery query, ValueObject paramVO) throws SqlQueryException
- protected ValueObject executeQuery(SqlQuery query, ValueObject paramVO, ValueObject pageVO) throws SqlQueryException
	- Select 조회 SQL 을 수행한다.
	- query : 실행할 Query 정보를 가지고 있는 SqlQuery 객체이다.
	- paramVO : 파라메터 값을 가지고 있는 ValueObject 객체이다.
	-pageVO : 페이지단위의 조회를 실행하고자 할 경우 페이지 정보를 담아서 전달한다.
	- Return : 조회된 값들을 ValueObject 에 담아서 반환한다. 조회된 결과가 없는 경우에는 비어있는 ValueObject 객체를 반환한다.
-   protected int[] executeBatch(SqlQuery query, ValueObject paramVO) throws SqlQueryException
	- 하나의 SQL 문에 여러건의 파라메터로 반복실행하고자 할 경우 사용된다.
	- Insert, update, delete SQL 을 실행할 수 있다.
	- paramVO : 파라메터를 값을 가지고 있는 ValueObject 객체이다.
	- paramVO의 row 수 만큼 Sql 이 실행된다.
	- Return : 각 실행에 대하여 처리된 row 수가 담겨진 int[] 이 반환된다. 실행된 SQL 중 에러가 발생한 경우에는 관련된 에러코드가 반환된다.
- protected ValueObject executeCall(SqlQuery query, ValueObject paramVO) throws SqlQueryException
	- Stored Procedure를 호출한다. 
	- 단순 IN/OUT/INOUT 파라메터만 있을 경우 사용된다.
	- IN/INOUT 파라메터 결과 값을 ValueObject에 담아서 반환한다.
- protected ValueObjectAssembler executeCall(SqlQuery query, ValueObject paramVO, String[] rsNames) throws SqlQueryException
	- ResultSet을 반환하는 Stored Procedure를 호출한다.
	- ResultSet 단위로 ValueObject 가 생성되며 모두 ValueObjectAssembler에 담겨서 반환된다.
	- rsName[0] 는 단순 IN/INOUT 파라메터 리턴값을 담을 ValueObject 명칭이며, rsName[1] 부터 리턴되는 ResultSet 결과를 담은 ValueObject의 명칭이 된다.
	- Stored Procedure 에서 리턴하는 ResultSet 의 개수가 n 개라면 rsName[] 배열의 크기는 n+1 이 되어야한다.

### (4) DAO 클래스 구현 절차

- 구현할 업무용 DAO 메소드들을 정의하는 Interface 를 작성한다.
	- DAO 클래스는 JdbcQueryDAO 를 extends 하고 위 interface를 implements 한다.
	- 사용할 Query XML 파일을 작성한다. (참고 : [Query XML])
	- DAO 클래스에는 SqlQueryPage를 inject 받기 위한 Setter method를 구현한다.
- 작성된 DAO interface, class 그리고 Query XML 파일을 하나로 연결하도록 서비스 설정 파일을 작성한다. (참고 : [XmlConfiguredServiceContainer])

**작성 예시 : DAO Interface**

```java
import java.util.List;
import s2.adapi.framework.vo.ValueObject;

public interface StoreServiceDAO {
    /**
     * 상품 카테고리 목록을 조회한다.
     * @param langCode 언어코드
     * @return
     */
    public ValueObject getCategoryList(String langCode);

    /**
     * 카테고리들에 속한 판매 상품 목록을 조회한다.
     * @param catList
     * @param pointRate
     * @param langCode
     * @return
     */
    public ValueObject getProductListOfCategories(List<Integer> catList, double pointRate, String langCode);

    /**
     * 전달된 카테고리 내의 판매중인 상품 목록을 조회한다. 카테고리 코드가 0 이면 전체 상품 조회이다.
     * @param categoryCode
     * @param pointRate 상품가격을 앱의 포인트로 전환하기 위한 비율
     * @param langCode 언어코드
     * @return
     */
    public ValueObject getProductList(int categoryCode, double pointRate, String langCode);

}
```

**작성 예시 : DAO class 구현**

```java
import java.util.List;
import s2.adapi.framework.dao.JdbcQueryDAO;
import s2.adapi.framework.query.SqlQuery;
import s2.adapi.framework.query.SqlQueryPage;
import s2.adapi.framework.vo.ValueObject;

public class StoreServiceDAOImpl extends JdbcQueryDAO implements StoreServiceDAO {

    private SqlQueryPage sqls = null;

    public void setSql(String path) {
        sqls = getQueryPage(path);
    }

    @Override
    public ValueObject getCategoryList(String langCode) {

        ValueObject paramVO = new ValueObject();
        paramVO.set("lang_cd", langCode);

        SqlQuery sql = sqls.getQuery("getCategoryList");

        return executeQuery(sql, paramVO);
    }

    @Override

    public ValueObject getProductListOfCategories(List<Integer> catList, double pointRate, String langCode) {

        ValueObject paramVO = new ValueObject();
        paramVO.set("lang_cd", langCode);
        paramVO.set("pnt_rate", pointRate);

        if (catList != null && catList.size() > 0) {
            paramVO.set("cat_cd", catList);
        }

        SqlQuery sql = sqls.getQuery("getProductListOfCategories");

        return executeQuery(sql, paramVO);
    }

    @Override
    public ValueObject getProductList(int categoryCode, double pointRate, String langCode) {

        ValueObject paramVO = new ValueObject();
        paramVO.set("lang_cd", langCode);
        paramVO.set("pnt_rate", pointRate);
        paramVO.set("cat_cd", categoryCode);

        SqlQuery sql = sqls.getQuery("getProductList");

        return executeQuery(sql, paramVO);
    }
}
```

**작성 예시 : 서비스 설정 파일**

```xml
<services module="store" package="s2.adapi.app.service.store" pre-init="false">

    <service name="StoreServiceDAO"
             interface="${package}.dao.StoreServiceDAO"
             class="${package}.dao.StoreServiceDAOImpl"
             singleton="true">
        <property name="datasource" ref="jdbc.apidb"/>
        <property name="sql" value="sqls/store_service_sqls.xml"/>
    </service>

</services>
```

## Query XML

Query XML  작성 방법에 대하여 설명한다.

### (1) \<sqls>
- Query XML 파일의 루트 노드이다. 속성(attribute) 들은 가지고 있지 않으며 하위에 \<statement> 노드와 \<resultMap> 노드를  갖는다.

### (2) \<statement>
- 하나의 SQL 문장을 정의한다. 파일내에서 고유한 id 속성을 가져야한다.
	- id : 프로그램내에서 SQL 문장을 가져오기 위하여 사용되는 ID 이다. 파일내에서 고유한 값을 가져야한다.
- SQL  문장
	- SQL 문장은 동적인 SQL 조립을 위하여 하위에 여러 노드를 가질 수 있다.
	- DML 및 DDL 문을 작성하며 런타임에 바인딩되는 파라메터는 아래와 같은 형태로 정의한다.
	- 파라메터 형식 : **#name:mode:type@format#** 또는 **$ name $** 를 사용
		- \# ... # 으로 정의되는 파라메터는 JDBC의 PreparedStatement의 ? 바인딩 방식으로 동작한다. 런타임에 파라메터 값이 달라져도 DB 에서는 같은 SQL 문으로 처리되므로 DB에서 SQL 문장 파싱 작업은 한번만 수행된다.
		- $ ... $ 방식의 파라메터는 SQL 문장 자체를 바꾸는 방식으로 파라메터 값이 바뀌면 DB는 매번 다른 SQL 문장으로 처리하므로 SQL 문장 파싱 작업이 여러번 발생할 수 있다. 성능 이슈가 있으므로 바인딩 방식의 파라메터가 불가능한 경우에만 제한적으로 사용해야한다.
		- 파라메터 escaping : SQL 문장내에서 \# 이나 $ 문자가 사용되는 경우에는 \## 과 $$ 를  사용한다.
	- name 
		- 파라메터 명칭이다. 
		- 런타임에 Query 실행시 전달되는 ValueObject 에서 값을 가져오기 위하여 사용된다.
		- name 앞에 % 가 붙으면 실행 중인 서비스의 컨텍스트 정보로부터 name에 해당되는 값을 가져온다. (일반적으로 현재 세션 정보와 바인딩된다.)
		- name 뒤에 \[] 가 붙으면 해당 값은 List 나 배열로 처리되어야 함을 의미한다. 이것은 \<iterate> 내에서 사용된다.
	- mode 
		- IN, OUT, INOUT 중 하나를 지정한다. 생략하면 디폴트로 IN 으로 지정된다. 
		- Stored Procedure 호출 시에만 사용되는 기능이다.
	- type
		- java.sql.Types 에서 지정된 JDBC Type 에 해당되는 문자를 지정한다. (예 : VARCHAR, NUMERIC) 
		- 명시적인 타입을 지정하기 위하여 사용된다.
		- Stored Procedure 호출 시에만 사용되지만 DATE, TIME, TIMESTAMP 의 경우는 일반 SQL 내에서도 사용할 수 있다.
	- format
		- type 이 DATE, TIME, TIMESTAMP 의 경우에는 타입변환을 위하여 실제 파라메터의 포멧 문자열을 지정할 수 있다.
		- format 지정은 \[java.text.SimpleDateFormat] 에서 정의하는 포멧 문자열을 사용한다.
		- DATE type 은 format 이 지정되지 않으면 디폴트로 \'yyyyMMdd\' 가 사용된다.
		- TIME type은 format 이 지정되지 않으면 디폴트로 \'HHmmss\' 가 사용된다.
		- TIMESTAMP type은 format 이 지정되지 않으면 디폴트로 \'yyyyMMddHHmmssSSS\' 가 사용된다.
		- 예를 들어 DB의 컬럼 타입이 Date 이고 여기에 저장될 값이 \'2016-10-27\' 과 같은 형태의 문자열로 전달이된다고 하면 이를 별도 코딩 작업없이 아래와 같이 파라메터로 처리할 수 있다.

**format 사용 예시**

```xml
<statement id="setComCode"><![CDATA[
    INSERT INTO comcdeptm (deptcd, todd, fromdd, deptnm, lastupdtdt) 
           VALUES (#deptcd#, #todd#, #fromdd#, #deptnm#, #updtdt::DATE@yyyy-MM-dd#) ]]>
</statement> 
```

- 동적 SQL 작성
	- 동적 SQL 용 element는 nested 되어 사용이 가능하다.
	- \<isEmpty property="name"> ... \</isEmpty>
		- name 에 해당되는 값이 정의되지 않았거나 "" (empty) 인 경우 적용된다.
	- \<isNotEmpty property="name"> ... \</isNotEmpty>
		- name 에 해당되는 값이 정의되었고 "" (empty) 가 아닌 경우 적용된다.
	- \<isEqual property="name" compare="value"> ... \</isEqual>
		- name 에 해당되는 값이 정의되었고 value 와 동일한 경우 적용된다.
	- \<isNotEqual property="name" compare="value"> ... \</isNotEqual>
		- name 에 해당되는 값이 정의되지 않았거나, 정의되었지만 value 와 값이 다른 경우 적용된다.
	- \<iterate property="name" open="" close="" conjunction=""> ... \</iterate>
		- name이 배열 또는 List 객체인 경우 그 개수만큼 반복되어 적용된다.
		- open : 반복 시작 시점에 출력되는 문자이다.
		- close : 반복 종료 후 출력되는 문자이다.
		- conjunction : 반복 사이에 출력되는 문자이다.
		- \<iterate> 하위에 다른 \<iterate> 가 nested 되어 사용될 수 없다.
		- \<iterate> 내에 작성된 SQL 문장에서는 property 속성의 컬럼 명칭이나 파라메터 명칭 다음에 [] 를 붙일 수 있으며 이때에는 해당 객체를 List 나 배열로 취급하여 값을 순서대로 꺼내어 binding 한다.
	- 동적 SQL 내에서 property 대신 session 을 사용하면 parameter 가 아니라 세션에서 값을 참조한다.
	- 예시) <isEmpty session="userid"> ...
```

**iterate 작성 예시**

```xml
<statement id="getProductListOfCategories">
        SELECT b.prd_cd, b.cat_cd, b.tr_type, b.updt_dt, c.corp_nm, c.prd_nm
          FROM str_prdapps a, str_products b LEFT JOIN str_prdlangs c ON b.prd_cd = c.prd_cd AND c.lang_cd = #lang_cd#
         WHERE a.app_id = #%appid#
           AND a.prd_cd = b.prd_cd
    <isNotEmpty property="cat_cd">
           AND b.cat_cd IN <iterate property="cat_cd" open="(" close=")" conjunction=",">#cat_cd[]#</iterate>
    </isNotEmpty>
           AND b.use_yn = 'Y'
         ORDER BY b.ord_no, b.prd_price
</statement>
```

- Stored Procedure 호출
	- Stored procedure 용 Query XML 작성시 파라메터는 반드시 #name:mode:type# 과 같은 형태로 정의해야한다.
	- name : 파라메터 명칭이다. 런타임에 Query 실행시 사용되는 실제 값을 가져오기 위하여 key 이다.
	- mode : IN, OUT, INOUT 중 하나를 지정한다.
	- type : java.sql.Types 에서 지정된 JDBC Type 에 해당되는 문자를 지정한다. (예 : VARCHAR, NUMERIC) 
	- 예를 들어 호출할 stored Procedure 가 아래와 같을 경우 이를 호출하기 위한 Query XML 파일 및 Java 코딩은 다음과 같이 작성할 수 있다. (DB2 의 경우임)

**DB2 Stored procedure sample**

```sql
-- stored procedure sample
CREATE PROCEDURE tmp.sptest
    (IN p_arg1 VARCHAR(10),
     IN p_arg2 VARCHAR(10), 
     OUT p_ret1 VARCHAR(10),
     OUT p_ret2 VARCHAR(10) )
SPECIFIC sptest 
DYNAMIC RESULT SETS 1 
LANGUAGE SQL
BEGIN

    DECLARE c_test01 CURSOR WITH RETURN FOR
        SELECT syscd,msgkind,msgno,msg1cnts FROM tmp.test01;
    SET p_ret1 = p_arg1; SET p_ret2 = p_arg2; OPEN c_test01;
END 
@
```

**Query XML sample**

```xml
<statement id="spTest"><![CDATA[
    {call tmp.sptest(#arg1:IN:VARCHAR#, #arg2:IN:VARCHAR#, 
            #ret1:OUT:VARCHAR#, #ret2:OUT:VARCHAR#)} ]]>
</statement>
```

**Java coding**

```java
public ValueObjectAssembler testSP(ValueObject pVO) throws SqlQueryException { 

    SqlQuery sql = sqlPage.getQuery("spTest");
    ValueObjectAssembler retVOs = executeCall(sql,pVO,new String[]{"outvo","retvo"});

    return retVOs;
}
```

- 위의 예시에 대한 설명
	- Stored procedure 에서 OUT 파라메터로 정의된 3번째 및 4번째 파라메터 (p_ret1 과 p_ret2) 는 Query XML 에서 #ret1# 과 #ret2# 라는 명칭으로 바인딩되었으므로 ValueObject 에 ret1 과 ret2 라는 컬럼 명으로 결과값이 저장된다.
	- Stored procedure 에서 Cursor 로 open 된 결과는 또 다른 ValueObject 에 저장된다.
	- 2개의 ValueObject 객체는 하나의 ValueObjectAssembler 에 각각 outvo 와 retvo 라는 명칭으로 저장되어 반환된다.

- Oracle 의 REF Cursor 사용하기
	- Oracle 의 REF Cursor 는 ORACLECURSOR 타입의 OUT 파라메터로 지정하여 받아 올 수 있으며, 지정된 파라메터 명칭의 ValueObject 로 생성되어 반환된다.
	- Java 에서 호출시에는 반드시 ValueObjectAssembler 를 리턴하는 executeCall() 메소드를 사용해야하며, OUT, INOUT 으로 지정된 파라메터값을 받아오기 위한 ValueObject  명칭을 마지막 파라메터로 지정해야한다.
	- 예를 들어 호출할 stored procedure 가 아래와 같을 때 이를 호출하기 위한 Query XML 파일 및 Java 코딩은 다음과 같이 작성할 수 있다. (Oracle에서 REF Cursor 사용시)

**Oracle REF Cursor 예시**

```sql
-- Stored procedure sample

CREATE OR REPLACE PROCEDURE sptest( 
    p_arg1 IN VARCHAR2,
    p_arg2 IN VARCHAR2,
    p_ret1 OUT VARCHAR2,
    p_ret2 OUT VARCHAR2,
    p_set1 OUT SYS_REFCURSOR ) 
IS

BEGIN

    p_ret1 := p_arg1; 
    p_ret2 := p_arg2; 

    OPEN p_set1 FOR
        SELECT codeflag, bascode, bascdnm FROM COMDBA.COMCCODEM
         WHERE codeflag='0008';

END;
```

**Query XML sample**

```xml
<statement id="spTest"><![CDATA[
    { call sptest(#arg1:IN:VARCHAR#, #arg2:IN:VARCHAR#, #ret1:OUT:VARCHAR#,
                       #ret2:OUT:VARCHAR#, #set1:OUT:ORACLECURSOR#)} ]]>
</statement>
```

**Java coding**

```java
public ValueObjectAssembler testSPOra(ValueObject pVO) throws SqlQueryException { 

    SqlQuery sql = sqlPage.getQuery("spTest");
    ValueObjectAssembler retVOs = executeCall(sql, pVO, new String[]{"a"});

    return retVOs;
}
```

### (3) \<resultMap>
- 개별 Query 문에 ColumnReader 매핑을 지정하기 위하여 사용된다. 파일 내에서 고유한 id 속성을 가져야한다.
	- id : resultMap 을 식별하기 위한 명칭이다.
	- \<result> : <resultMap> 은 하위에 하나 이상의 <result> element 를 가진다. <result> 는 하나의 ColumnReader 매핑을 정의하며 아래의 속성들(attribute)을 가지고 있다.
	- column : ColumnReader를 매핑할 컬럼명을 정의한다.
	-  type : ColumnReader를 매핑할 타입을 정의한다. column과 type은 둘 중 하나만을 정의해야 한다. 
	- reader : ColumnReader의 클래스 명을 정의한다.
	- format : format 기능이 있는 ColumnReader인 경우 format 문자열을지정한다.
	- <statement> 작성 시 SQL 결과 데이터 컬럼을 읽을 때 사용할 <resultMap>의 ID를 resultmap 속성으로 지정한다.

**ResultMap 사용 예시**

```xml
<?xml version="1.0" encoding="UTF-8"?>

<sqls>
    <resultMap id="map1">
        <result column="a"
                reader="s2.adapi.framework.dao.reader.FormattedDateColumnReader"
                format="yyyyMMdd"/>
        <result column="b"
                reader="s2.adapi.framework.dao.reader.FormattedTimeColumnReader"
                format="HHmmss"/>
        <result type="DOUBLE"
                reader="s2.adapi.framework.dao.reader.FormattedNumberColumnReader"
                format="###.###"/>
    </resultMap>

    <statement id="mapTest" resultMap="map1">
        SELECT a, b, c 
          FROM com.tbl_test
    </statement>

</sqls>
```

- ColumnReader 의 매핑 순서는 다음과 같다.
	- \<statement> 에 resultmap 이 지정되었을 경우 컬럼 데이터 조회 시 지정한 resultmap에 정의된 ColumnReader 매핑을 사용한다.
	- \<resultMap> 에 정의된 ColumnReader 매핑에 대하여 column 명으로 지정된 것이 있으면 그것을 우선 적용한다.
	- \<resultMap> 에 column 명으로 정의된 ColumnReader 매핑이 없으면 type으로 정의된 것을 적용한다.
	- \<resultMap>에 column 명과 type으로 정의된 ColumnReader 매핑이 모두 없으면 Global ResultMap에 정의된 것을 적용한다. (JdbcDAO 참조)
	- Global ResultMap에도 정의되지 않았다면 DBMS 별로 디폴트로 정의된 ColumnReader를 사용한다. (JdbcDAO 참조)

## 3) SqlQueryDAO

JdbcDAO 나 JdbcQueryDAO를 사용하기 위해서는 이를 상속받은 업무별 DAO 클래스를 생성하여 내부에 Query 실행 및 관련 로직을 구현해야한다. 그러나 대부분 DAO 에서는 Query 실행만 시키는 방식으로 구현이 되므로 업무별로 DAO 클래스를 작성하는 것은 비효율적인 개발이되기 쉽다.

SqlQueryDAO는 업무별 DAO 클래스를 작성하지 않고 실행할 Query XML 파일만 작성하여 이를 비지니스 로직에서 바로 호출할 수 있도록 만들어진 DAO 클래스이다. Query XML  파일을 작성하는 것은 JdbcQueryDAO 와 동일하며 프로그램내에서는 SqlQueryDAO 를 생성하여 바로 사용한다.

다음은 SqlQueryDAO 의 사용방법을 보여주기 위한 간단한 예시이다.

**SqlQueryDAO 예시**

```java
package test.business;

import s2.adapi.framework.dao.SqlQueryDAO;
import s2.adapi.framework.vo.ValueObject;

public class CommonCodeManagerImpl implements CommonCodeManager {

    private SqlQueryDAO dao = null;

    public CommonCodeManagerImpl() {

        // 기본 Datasource를 사용하는 DAO 를 생성한다. a
        // 실제 사용시에는 직접 생성하지 않고 ServiceContainer 에서 injection을 통하여 받아온다.
        dao = new SqlQueryDAO();
        dao.setSql("test/business/sqls/commoncodedao_sqls.xml");
    }

    @Override
    public ValueObject getCodeList(ValueObject paramVO) {

        // 비즈니스 메소드에서 바로 SqlQueryDAO를 사용한다.
        ValueObject getVO = dao.executeQuery("getCodeList", paramVO);

        // ...

        return getVO;
    }
}
```

SqlQueryDAO 에서 제공하는 메소드 들은 다음과 같다. Query 를 실행하는 execute~() 형태의 메소드들은 JdbcQueryDAO의 그것과 동일한 의미이다. 다만 SqlQueryDAO 에서는 실행할 Query의 statement ID 값이 첫번째 파라메터로 전달된다는 점이 다르다.

- 생성자
	- public SqlQueryDAO()
	- public SqlQueryDAO(Object parent)
	- public SqlQueryDAO(String sqlpath, Object parent)
	- public SqlQueryDAO(String sqlpath)
	- public SqlQueryDAO(DataSource ds, Object parent)
	- public SqlQueryDAO(DataSource ds)
	- public SqlQueryDAO(DataSource ds, String sqlpath, Object parent)
	- public SqlQueryDAO(DataSource ds, String sqlpath)
		- parent : 이 DAO 를 사용하는 객체를 지정한다. parent 객체의 classpath 로부터 Query XML 파일을 찾는다.
		- sqlPath : Query XML 파일의 경로이다. parent 로 설정된 객체의 classpath 로부터의 상대 경로이다.
		- ds : DB connection 을 위하여 사용되는 DataSource 객체이다.
- Setter 메소드
	- public void setParent(Object parent)
	- public void setSql(String path)
- INSERT, UPDATE, DELETE
	- public int executeUpdate(String queryname, ValueObject paramVO) throws SqlQueryException
	- public ValueObject executeUpdateReturnKeys(String queryname, ValueObject paramVO, int numKeyCols) throws SqlQueryException
	- public ValueObject executeUpdateReturnKeys(String queryname, ValueObject paramVO, int[] columnIndexes) throws SqlQueryException
	- public ValueObject executeUpdateReturnKeys(String queryname, ValueObject paramVO, String[] columnNames) throws SqlQueryException
- SELECT
	- public ValueObject executeQuery(String queryname, ValueObject paramVO) throws SqlQueryException
	- public ValueObject executeQuery(String queryname, ValueObject paramVO, ValueObject pageVO) throws SqlQueryException
- Batch SQL
	- public int[] executeBatch(String queryname, ValueObject paramVO) throws SqlQueryException
- Stored procecedure 호출
	- public ValueObject executeCall(String queryname, ValueObject paramVO) throws SqlQueryException 
	- public ValueObjectAssembler executeCall(String queryname, ValueObject paramVO, String[] rsNames) throws SqlQueryException

 SqlQueryDAO를 이용한 개발 예제는 아래와 같다.

**Query XML 파일 작성**

```xml
<?xml version='1.0' encoding='utf-8'?>
<sqls>
    <statement id="getCategoryList">
        SELECT a.cat_cd, COALESCE(c.cat_nm, b.cat_nm) as cat_nm, b.updt_dt 
          FROM str_pcatapps a, str_prdcats b 
               LEFT JOIN str_pcatlangs c ON b.cat_cd = c.cat_cd AND c.lang_cd = #lang_cd#
         WHERE a.app_id = #%appid#
           AND a.cat_cd = b.cat_cd
           AND b.use_yn = 'Y'
         ORDER BY a.seq_no
    </statement>
</sqls>
```

**서비스 설정 파일에 정의**

```xml
<services module="store" package="s2.adapi.app.service.store" pre-init="false">

    <service name="StoreServiceDAO"
             class="s2.adapi.framework.dao.SqlQueryDAO"
             singleton="true">
        <property name="datasource" ref="jdbc.apidb"/>
        <property name="sql" value="sqls/store_service_sqls.xml"/>
    </service>

    <service name="StoreService"
             interface="${package}.StoreService"
             class="${package}.StoreServiceImpl"
             singleton="true">
        <property name="dao" ref="StoreServiceDAO"/>
    </service>
</services>
```

**Java 에서 사용하기**

```java
public class StoreServiceImpl implements StoreService {

    private SqlQueryDAO storeDAO = null;
    public void setDao(SqlQueryDAO dao) {
        storeDAO = dao;
    }

    @Override
    public ValueObject getCategoryList(String langCode) {

        ValueObject paramVO = new ValueObject();
        paramVO.set(Constant.Columns.LANG_CD, langCode);

        ValueObject retVO = storeDAO.executeQuery("getCategoryList", paramVO);

        return retVO;
    }
}
```

# 6. Web Application

MVC Model 구조의 Web Application 개발을 위하여 아래의 클래스들이 제공된다.

- WebActionDispatcher
	- HTTP Request 를 ServiceContainer 가 관리하는 WebAction 구현 서비스로 전달하는 구조를 제공한다.
	- HTTP Request URL 상에 호출할 WebAction 서비스의 명칭을 명시해야한다.
	- MVC Model 의 Controller 에 해당되며 HttpServlet 으로 구현된다.
- WebAction 서비스
	- HTTP Request 를 처리하기 위한 로직을 구현하며 ServiceContainer 에 서비스로 등록된다.
	- WebAction 또는 WebMultiAction interface 를 구현한다.
- WebActionForward
	- WebAction 서비스 내에서 처리된 결과를 JSP 등에 보내기 위하여 사용되는 DTO 클래스이다.
	- 내부에 Forward 될 URL 정보와 결과 데이터를 저장한다.

## 1) WebActionDispatcher

Web Framework의 MVC 아키텍처 구현을 위한 Controller 클래스로서, HttpServletRequest를 받아서 해당 WebAction 객체로 전달해주는 역활을 수행한다. 이 Servlet이 동작하기 위해서는 항상 ServiceContainer 객체가 필요한데, 이것은 WebApplicationContextLoader 통하여 Servlet 컨테이너가 초기화되는 시점에 생성된다. 

WebApplicationContextLoader 는 Servlet 컨테이너가 초기화되는 시점과 Servlet 컨테이너가 중지되는 시점에 호출되는 ServletContextListener의 구현 클래스이다. WebActionDispatcher 가 생성되는 시점에 ServletContext 내에 WebApplicatinContext 객체를 생성하여 담아 놓는다. WebApplicationContext는 WebAction 구현 클래스에서 접근이 가능하며 서비스 객체를 가져오기 위한 ServiceContainer 객체를 제공한다. WebApplicationContextLoader 는 web.xml 파일 내에 아래와 같이 \<listener> 를 설정하여야 한다. \<listener>는 web.xml 내에서 \<filter-mapping>와 \<servlet> 사이에 위치한다.

```xml
    <listener>
      <listener-class>s2.adapi.framework.web.action.WebApplicationContextLoader</listener-class>
    </listener> 
```

프로그램 개발시에는 프레임워크에서 제공하는 s2.adapi.web.action.WebActionDispatcher 를 그대로 사용하거나 또는
상속받아 기능을 추가하여 사용할 수 있다. 기능 추가시에는 postProcess(), preProcess() 메소드를 override 한다.

**WebActionDispatcher**

```java
public class WebActionDispatcher extends HttpServlet {

    /**
     * WebAction의 execute()나 WebMultiAction()의 method 호출 전에 처리할
     * 전처리 로직을 구현한다.
     * @return
     */
    protected void preProcess(HttpServletRequest request,
            HttpServletResponse response) throws Throwable {

    }

    /**
     * WebAction의 execute()나 WebMultiAction()의 method 호출이
     * 정상 처리되었을 경우의 후처리 로직을 구현한다.
     * @param request
     * @param response
     * @param forward
     */
    protected void postProcess(HttpServletRequest request,
            HttpServletResponse response, WebActionForward forward) {

    }

    /**
     * WebAction의 execute()나 WebMultiAction()의 method 호출 시 Exception 발생하였을 경우의 후처리 로직이다.
     * 전달된 Exception에 맞는 처리를 한 후 Exception을 다시 던지거나 에러 처리용 WebActionForward를 리턴하는 방식으로 구현한다.
     * @param request
     * @param response
     * @param exeception
     * @return WebActionForward
     */
    protected WebActionForward postProcess(HttpServletRequest request,
            HttpServletResponse response, Throwable thr) throws Throwable {
        throw thr;

    }

   // ...

}
```

아래는 실제 개발 예시이다.

**WebActionDispatcher 커스터마이징 예시**

```java
package s2.adapi.server.s2adapi;

import java.sql.SQLException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.MDC;

import s2.adapi.framework.context.ContextManager;
import s2.adapi.framework.dao.sql.Transaction;
import s2.adapi.framework.exception.ApplicationException;
import s2.adapi.framework.util.StringHelper;
import s2.adapi.framework.web.action.WebActionForward;

public class WebActionDispatcher extends s2.adapi.framework.web.action.WebActionDispatcher {

    protected void preProcess(HttpServletRequest request, HttpServletResponse response) 
    {

        Object userId = ContextManager.getServiceContext().getRole("login_id");

        if (userId != null ) {
            MDC.put("userid", userId);
        }

        MDC.put("sid", idgen.getNextId(this).toString());
        MDC.put("ipaddr", StringHelper.null2void(request.getRemoteAddr()));

        Transaction.current().begin();
    }

    /**
     * ServiceContext 정보를 클리어한다.
     */

    protected void postProcess(HttpServletRequest request, HttpServletResponse response, 
WebActionForward forward) 
    {

        try {
            if (Transaction.current().isActive()) {
                Transaction.current().end();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        logDiagnostics(null);
        ContextManager.clearAll();
    }

    /**
     * ServiceContext 정보를 클리어한다.
     */

    protected WebActionForward postProcess(HttpServletRequest request, HttpServletResponse response, Throwable thr) throws Throwable {

        try {
            if (Transaction.current().isActive()) {
                Transaction.current().end();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        logDiagnostics(thr);
        ContextManager.clearAll();

        throw thr;
    }

    private void logDiagnostics(Throwable thr) {

        Map<String,Object> diagMap = ContextManager.getDiagnosticContext();

        // 성능 로깅을 남긴다. 

        // ...
    }
}
```

**web.xml 예시**

```xml
<?xml version="1.0" encoding="UTF-8"?>

<!DOCTYPE web-app PUBLIC "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN" "http://java.sun.com/dtd/web-app_2\_3.dtd">

<web-app id="s2adapi">

    <display-name>s2adapi</display-name>
    <description>This is S2adapi Web Application.</description>

    <listener>
      <listener-class>s2.adapi.framework.web.action.WebApplicationContextLoader</listener-class>
    </listener>

    <servlet>
      <servlet-name>dispatcher</servlet-name>
      <servlet-class>s2.adapi.server.s2adapi.WebActionDispatcher</servlet-class>
    </servlet>

    <servlet-mapping>
      <servlet-name>dispatcher</servlet-name>
      <url-pattern>*.ad</url-pattern>
    </servlet-mapping>

    <servlet-mapping>
      <servlet-name>dispatcher</servlet-name>
      <url-pattern>*.main</url-pattern>
    </servlet-mapping>

    <resource-ref>
      <description>apidb</description>
      <res-ref-name>jdbc/apidb</res-ref-name>
      <res-type>javax.sql.DataSource</res-type>
      <res-auth>Container</res-auth>
    </resource-ref>

</web-app>
```

## 2) WebAction

HTTP Request 를 받아서 로직을 처리하고자 할 경우에는 반드시 WebAction interface 를 구현해야한다. WebActionDispatcher 는 request URI 내에서 서비스 명을 얻어와 대상 서비스 객체의  execute() 메소드를 호출한다. 아래는 WebAction interface 이다.

```java
package s2.adapi.framework.web.action;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface WebAction {

    /**
     * WebActionDispatcher 서블릿이 호출하는 메소드이다.
     * 이 인터페이스를 구현하는 클래스는 이 메소드에서 업무처리를 수행하고 forward할 View의 URL을
     * WebActionForward로 리턴한다. null을 리턴할 경우에는 forward하지 않는다.
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public WebActionForward execute(HttpServletRequest request, HttpServletResponse response) throws Exception;

    /**

     * WebAction의 설정값을 출력하는 메소드이다.
     * @param request
     * @param response
     * @throws Exception
     */
    public void showConfiguration(HttpServletResponse response) throws IOException;

    /**

     * WebAction 객체를 실행하는 WebActionDispatcher 에서 자신의 WebApplicationtContext 값을 전달한다.
     * 처음 WebAction 객체가 생성될 때 호출된다.
     * @param ctx
     */
    public void setWebApplicationContext(WebApplicationContext ctx);

}
```

WebAction 의 처리 흐름은 아래 그림과 같다.

그림


### (1) WebActionForward

WebActionForward 는 WebAction에서 리턴되는 객체이며 이후 이동할 화면(JSP 등)의 URL 과 전달할 데이터를 가진다. WebActionForward 객체를 생성할 때에는 생성자를 통하여 생성하지않고 AbstractWebAction 클래스에서 제공하는 public WebActionForward createForward(String name) 메소드를 이용하는 것이 일반적이다.

전달할 데이터는 addModel() 메소드를 이용하여 설정하며 이 데이터는 request.getAttribute(modelName) 을 사용하여 접근이 가능하다.

아래는 WebActionForward 가 제공하는 메소드이다.

- 생성자
	- public WebActionForward(String name, String url)
	- public WebActionForward(String name, String url, boolean forward)
		- forward : true 이면 forward 방식, false 이면 redirect 방식으로 이동
- 모델 데이터
	- public WebActionForward addModel(String modelName, Object modelData)
	- public WebActionForward addModel(ValueObjectAssembler vos)
	- public Object getModel(String modelName)
	- public Set<String> getModelNames()
- Forward URL
	- public String getViewURL()
	- public String getViewName()
	- public boolean isForward()
	- public void sendView(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
		- 설정된 view의 url 로 forward 또는 redirect 한다.

### (2) AbstractWebAction 및 WebAction 구현 절차

AbstractWebAction 클래스는 WebAction 서비스를 쉽게 구현할 수 있도록 WebActionForward 관련된 편의 기능들을 제공하고 showConfiguration() 과 setApplicationContext() 메소드를 구현해놓았다. AbstractWebAction 클래스에서 제공하는 메소드들은 다음과 같다. 

- protected ValueObject getRequestParamAsVO(HttpServletRequest request)
	- GET 으로 전달된 파라메터들을 ValueObject 객체에 담아서 반환한다. 동일한 명칭의 값이 여러개인 경우 row 를 추가하여 ValueObject에 저장된다.
- protected ValueObject getAttachFileAsVO(HttpServletRequest request, String name)
- 첨부파일이 attach 된 경우에는 해당 첨부파일 정보를 담고 있는 ValueObject 객체를 반환한다. ValueObject 는 다음 같은 컬럼을 담고 있다.
	- type : 첨부파일의 content type
	- name : 첨부파일 명칭
	- size : 첨부파일의 크기 (Bytes 수)
	- data : 첨부파일 내용 (byte\[])
- public void setForward(String forwardString)
	- 서비스 실행 결과를 처리할 View 의 URL 을 설정하는 Setter Method 이다.
	- forwardString 의 형식 : name=\[forward:\|redirect:]URL
		- forward: : URL 이동시 forward 방식으로 이동한다. 동일한 Web context 내에서만 이동이 가능하다. 디폴트 이동방식이다.
		- redirect: : URL 이동시 redirect 방식으로 이동한다. Redirect 방식이므로 다른 도메인 URL 으로도 이동이 가능하다.
-  public void setPrefix(String prefix)
	- forward URL 앞에 공통으로 붙일 문자열을 설정하는 Setter Method 이다.
- public void setSuffix(String suffix)
	- forward URL 뒤에 공통으로 붙일 문자열을 설정하는 Setter Method 이다.
- public WebActionForward createForward(String name)
	- setForward() 메소드로 설정된 forwardURL 의 name을 파라메터로 전달하면 해당 URL을 찾아서 prefix와 suffix 를 추가한 후 WebActionForward 객체를 생성하여 반환한다.
	- name 에 해당되는 forwardURL 이 설정되어 있지 않다면 name 자체를 URL 로 간주하여 WebActionForward 객체를 생성하여 반환한다.

**작성 예시**

```xml
    <service name="testweb.main"
             interface="s2.adapi.framework.web.WebAction"
             class="test.web.TestWebActionImpl"
             singleton="true">
        <property name="forward" value="default:=/s2adapi/jsp/test.jsp"/>
        <property name="forward" value="main:=forward:/s2adapi/main.home.main"/>
        <property name="forward" value="market:=redirect:http://play.google.com"/>
        <property name="prefix" value="/webapps/test"/>
    </service>
```

WebAction 서비스는 아래와 같은 절차로 구현한다.

- WebAction 서비스는 s2.adapi.framework.web.action.WebAction 인터페이스를 implements 한다.
- showConfiguration() 과 setApplicationContext() 메소드가 구현된 AbstractWebAction 클래스가 제공되므로 이를 extends 한다.
- public WebActionForward execute(HttpServletRequest request, HttpServletResponse response) throws Exception 메소드를 구현한다. 
- 구현된 WebAction 서비스의 서비스 설정 파일을 작성한다. 

**TestWebActionImpl.java**

```java
package test.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import s2.adapi.framework.vo.ValueObject;
import s2.adapi.framework.web.action.AbstractWebAction;
import s2.adapi.framework.web.action.WebActionForward;

public class TestWebActionImpl extends AbstractWebAction {

    @Override
    public WebActionForward execute(HttpServletRequest request, HttpServletResponse replay) throws Exception {

        // sample data 
        ValueObject dataVO = new ValueObject();

        dataVO.set("name", "kimhd");
        dataVO.set("age", 30);
        dataVO.set("gender", "M");

        return createForward("default").addModel("code","member").addModel("userdata", dataVO);
    }

}
```

**서비스 설정 파일 예시**

```xml
<?xml version=\'1.0\' encoding=\'euc-kr\'?>
<!DOCTYPE services
    PUBLIC "-//S2adapi Corp.//DTD S2adapi Services Config 0.1//EN" "com/s2adapi/framework/container/service-config.dtd">

<services module="test" pre-init="false">

    <service name="testweb.main"
             interface="s2.adapi.framework.web.WebAction"
             class="test.web.TestWebActionImpl"
             singleton="true">
        <property name="forward" value="default:=/testweb/jsp/default.jsp"/>
        <property name="prefix" value="/webapps/test"/>
    </service>

</services>
```

위와 같이 서비스 설정파일이 작성되었다면 위 WebAction 서비스의 서비스명은 "test.testweb.main" 이 되므로 아래의 URL 로 호출할 수 있다.

- http://your.domina/your_context/test.testweb.main

위와 같이 호출하면 TestWebActionImpl.java 의 execute() 메소드가 호출된다.

또한 위의 예시에서 "default" 명칭으로 WebActionForward 를 생성하여 리턴하였으므로 서비스 설정 파일에 설정된 "/testweb/jsp/test.jsp" 의 앞에 prefix 로 설정된 "/webapps/test" 가 추가되어 "/webapps/test/testweb/jsp/test.jsp" 로 forward 된다.

## 3) WebMultiAction

WebAction 은 execute() 라는 하나의 서비스 메소드만을 가질 수 있는 반면 WebMultiAction은 여러개의 서비스 메소드를 가질 수 있다. WebMultiAction 을 호출할 경우에는 HTTP Request 내에 어떤 메소드를 호출할 지 지정하는 파라메터를 포함시켜야한다. 아래는 WebMultiAction 의 interface 이다.

**WebMultiAction**

```java
package s2.adapi.framework.web.action;

import javax.servlet.http.HttpServletRequest;

/**
 * 하나의 WebAction 객체에서 여러 request 처리 구현을 위한 WebMultiAction 인터페이스이다.
 */
public interface WebMultiAction extends WebAction {

    /**
     * 파라메터로 전달된 HttpServletRequest를 이용하여 호출할 Method 명을 결정하여 리턴한다.
     * @param request
     * @return 메소드 명
     */

    public String getMethodName(HttpServletRequest request);

}
```

아래는 WebMultiAction 의 처리흐름도이다.

그림


### (1) AbstractWebMultiAction

AbstractWebMultiAction 클래스는 WebMultiAction 서비스 구현을 쉽게 할 수 있도록 필요한 기능을 제공하는 클래스이다. AbstractWebAction 클래스를 extends 하였으며 실제 구현 소스는 아래와 같다.

**AbstractWebMultiAction 소스**

```java
package s2.adapi.framework.web.action;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import s2.adapi.framework.util.StringHelper;

public abstract class AbstractWebMultiAction extends AbstractWebAction implements WebMultiAction {

    /**
     * 호출할 메소드 명을 얻기위한 reqeust parameter 명
     */
    protected String methodSelector = null;

    /**
     * 호출할 메소드 명을 찾지 못할 경우 사용할 디폴트 메소드 명
     */
    protected String defaultMethodName = null;

    public WebActionForward execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        throw new Exception("execute() not implemented.");
    }

    public void showConfiguration(HttpServletResponse response) throws IOException {
        super.showConfiguration(response);

        PrintWriter out = response.getWriter();

        out.print("<p>");
        out.println("<ul>");
        out.println("<li> Method Selector : " + methodSelector);
        out.println("<li> Default Method : " + defaultMethodName);
        out.println("</ul></p>");
    }

    /**
     * 호출할 메소드 명을 얻기 위하여 사용할 파라메터의 이름(Selector)을 지정한다.
     * Selector가 지정되지 않은 경우 <code>getMethodName()</code>는 항상 null을 리턴한다.
     * @param selectorName
     */
    public void setSelector(String selectorName) {
        this.methodSelector = selectorName;
    }

    /**
     * 호출할 메소드 명을 찾지 못했을 경우 사용할 디폴트 메소드 명을 지정한다.
     * @param defaultName
     */
    public void setDefaultMethod(String defaultName) {
        this.defaultMethodName = defaultName;
    }

    /**
     * HttpServletRequest의 request parameter 중에서 selector로 지정된 파라메터 명을 사용하여 호출할 메소드 명을 얻어온다. 
     * Selector로 지정된 파라메터명에 해당되는 값이 존재하지 않으면 디폴트로 설정한 메소드 명을 반환한다.
     * Selector나 디폴트 메소드명이 지정되지 않은 경우에는 null을 반환한다.
     * @param request
     * @param defaultSelector 설정된 Selector가 없을 경우에 사용할. 디폴트 selector 값
     * @return 호출할 메소드명
     */
    public String getMethodName(HttpServletRequest request) {

        String methodName = null;

        if ( methodSelector != null ) {
            methodName = StringHelper.null2string(request.getParameter(methodSelector),defaultMethodName);
        }

        return methodName;
    }
}
```

### (2) WebMultiAction 서비스 구현하기

- 구현할 WebMultiAction 서비스의 interface 를 정의한다. 이때  s2.adapi.framework.web.action.WebMultiAction 을 extends 해야한다.
- WebMultiAction 의 메소드들은 다음과 같은 형태로 작성해야한다.
	- WebActionForward **your_method** (HttpServletRequest request, HttpServletResponse response) throws Exception
- WebMultiAction 서비스를 클래스로 구현한다. 이때 위에서 작성한 서비스용 interface 를 implements 해야하며s2.adapi.framework.web.action.AbstractWebMultiAction 클래스를 extends 해야한다.
- 구현된 WebMultiAction 의 서비스 설정 파일을 작성한다. 기본적으로는 WebAction 용 서비스 설정 파일 작성과 동일하며 여기에 selector 와 defaultMethod 설정을 추가하도록 한다.
- 아래는 interface 와 구현 class 그리고 서비스 설정파일의 작성 예시이다.

**TestWebMultiAction interface**

```java
package test.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import s2.adapi.framework.web.action.WebActionForward;
import s2.adapi.framework.web.action.WebMultiAction;

public interface TestWebMultiAction extends WebMultiAction {

    WebActionForward display(HttpServletRequest request, HttpServletResponse response) throws Exception; 
    WebActionForward update(HttpServletRequest request, HttpServletResponse response) throws Exception;

}
```

**TestWebMultiActionImpl class**

```java
package test.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import s2.adapi.framework.web.action.AbstractWebMultiAction;
import s2.adapi.framework.web.action.WebActionForward;

public class TestWebMultiActionImpl extends AbstractWebMultiAction implements TestWebMultiAction {

    @Override
    public WebActionForward display(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 여기에 구현한다.
    }

    @Override
    public WebActionForward update(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 여기에 구현한다.
    }
}
```

**서비스 설정파일 예시**

```xml
<services module="test" pre-init="false">

    <service name="testmulti.main" 
             interface="test.web.TestWebMultiAction" 
             class="test.web.TestWebMultiActionImpl" 
             singleton="true">
        <property name="selector" value="method"/>
        <property name="defaultMethod" value="display"/>
        <property name="forward" value="default:=/testweb/jsp/multi_default.jsp"/>
        <property name="prefix" value="/webapps/test"/>
    </service>

</services>
```

위의 설정에서 selector 를 "method" 로 설정하였으므로 HTTP Request 에서 method 파라메터의 값을 호출할 메소드 명으로 사용하게되며 만약 method 파라메터가 없다면 defaultMethod 로 설정된 "display" 메소드가 호출되게 된다. 결과적으로 아래와 같이 각각의 서비스 메소드를 호출할 수 있다.

- http://your_domain/your_context/test.testmulti.main  또는 http://your_domain/your_context/test.testmulti.main?method=display
	- TestWebMultiActionImpl 의 display() 메소드가 호출된다.
- http://your_domain/your_context/test.testmulti.main?method=update
	- TestWebMultiActionImpl 의 update() 메소드가 호출된다.

## 4) RestfulMultiWebAction

RestfulMultiWebAction 은 REST API 방식의 WebAction 구현을 위하여 필요한 기능을 제공해주는 WebMultiAction의 구현 클래스이다. RestfulMultiWebAction 의 실제 소스는 아래와 같다.

**RestfulMultiWebAction 소스**

```java
package s2.adapi.framework.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import s2.adapi.framework.container.NameAwareService;

public abstract class RestfulMultiWebAction extends AbstractWebAction implements WebMultiAction, NameAwareService {

    protected String serviceName = null;

    public void setServiceName(String name) {
        serviceName = name;
    }

    public String getServiceName() {
        return serviceName;
    }

    // Restful API 에서 action 및 그 이후 부분들을 array 로 담아 반환한다.
    protected String[] getPayload(HttpServletRequest request) {

        String path = request.getServletContext().getContextPath();
        String uri = request.getRequestURI();

        String actionStr = uri.substring(path.length() + serviceName.length() - 1);

        return actionStr.split("\\/");
    }

    @Override
    public String getMethodName(HttpServletRequest request) {

        String path = request.getServletContext().getContextPath();
        String uri = request.getRequestURI();

        if (serviceName.endsWith("*")) {
            String actionStr = uri.substring(path.length() + serviceName.length() - 1);
            return actionStr.split("\\/")[0];
        }
        else {
            return null;
        }
    }

    @Override
    public WebActionForward execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        throw new Exception("execute() not implemented.");
    }
}
```

각각의 메소드 기능은 다음과 같다.

- public void setServiceName(String name)
	- NameAwareService interface 의 메소드를 구현한 것이다. 
	- NameAwareService 를 구현하는 경우 ServiceContainer 가 서비스 객체 생성시 해당 서비스의 이름을 setServiceName() 메소드를 통하여 전달해준다. 
- public String getMethodName(HttpServletRequest request)
	- WebMultiAction 의 메소드를 구현한 것이다.
	- 호출되는 메소드 명은 HTTP Request의 URI에서 자신의 서비스 명칭 다음에 오는 path element 를 사용한다.
	- 예를 들어 서비스 설정파일에 등록된 서비스 명칭이 "/api/reward/*" 인 경우 호출된 URL이 http://\..../s2adapi/api/reward/adways/12334/56789 와 같다면 /api/reward/ 다음에 오는 adways 가 호출되는 메소드명이되며 따라서 구현된 RestfulMultiWebAction 서비스 내에는 public WebActionForward adways(HttpServletRequest request, HttpServletResponse response) throws Exception 메소드가 구현되어 있어야 한다.
- protected String[] getPayload(HttpServletRequest request)
	- 호출된 HTTP Request URI 에서 자신의 서비스 명칭 이후의 path element 들을 배열로 반환한다. 그러므로 반환된 배열의 첫번째 아이템은 호출되는 메소드 명이 들어있다.

RestfulMultiWebAction 의 구현하기

- 구현할 RestfulMultiWebAction 서비스의 interface 를 작성한다. 이때  **s2.adapi.framework.web.action.WebMultiAction** 을 extends 해야한다.
- RestfulMultiWebAction의 메소드들은 WebMultiAction 과 마찬가지로 아래와 같은 형태로 작성해야한다.
	- WebActionForward **your_method** (HttpServletRequest request, HttpServletResponse response) throws Exception
- RestfulMultiWebAction 서비스를 클래스로 구현한다. 이때 위에서 작성한 서비스용 interface 를 implements 해야하며  **s2.adapi.framework.web.action.RestfulMultiWebAction** 클래스를 extends 해야한다.
- 구현된 RestfulMultiWebAction 의 서비스 설정 파일을 작성한다.
	- 기본적으로는 WebAction 용 서비스 설정 파일 작성과 동일하다. 다만 아래의 내용에 주의해야한다.
		- 서비스 명은 path 형태의 문자열로 작성하며 마지막 path element 는 * 로 끝나도록 한다.
		- 예 : **"/api/ref/*"**, **"/api/reward/*"**
		- 서비스 명이 HTTP Request URI 내에 포함되는 형태이므로 서비스 설정 파일에는 module 을 정의하지 않는다.
		- 예를 들어 \<services module="ad" pre-init="false"> 와 같이 module 을 지정한 경우 서비스 명은 "ad./api/ref/*" 로 인식이되므로 실제적으로는 호출할 수 있는 URI 형태가 될 수 없다.
		- 서비스 명과 같은 URL 이 WebActionDispatcher 를 통해 처리될 수 있도록 web.xml 파일 내에 **<servlet-mapping>** 설정되어 있어야한다.
- 아래는 실제 구현 예시이다.

**RestfulMultiWebAction 예시용 interface**

```java
package s2.adapi.ad.service.adext;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import s2.adapi.framework.web.action.WebActionForward;
import s2.adapi.framework.web.action.WebMultiAction;

/**
 * 외부 postback 호출이 되어 conversion 처리를 수행한다.
 */
public interface ExternalEventWebAction extends WebMultiAction {

   /**
    * Adways 연동 광고앱 리워드 지급 요청 (Rest 방식 구현)
    * http://www.s2adapi.com/s2adapi/api/reward/adways/<adkey>
    */
    public WebActionForward adways(HttpServletRequest request, HttpServletResponse response) throws Exception;

}
```

**RestfulMultiWebAction 구현 클래스 예시**

```java
package s2.adapi.ad.service.adext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import s2.adapi.framework.web.action.RestfulMultiWebAction;
import s2.adapi.framework.web.action.WebActionForward;

public class ExternalEventWebActionRestImpl extends RestfulMultiWebAction implements ExternalEventWebAction {

    private ExternalEvent externalEvent = null;

    public void setExternal(ExternalEvent link) {
        externalEvent = link;
    }

    @Override
    public WebActionForward adways(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String[] payload = getPayload(request);
        String adkey = (payload.length > 1) ? payload[1] : null;
        String extData = (payload.length > 2) ? payload[2] : null;

        if (adkey == null) {
            writeDefaultPostbackResult(false, response);
            return;
        }

        // ... 

        return null;
    }
}
```

**서비스 설정 파일 예시**

```xml
<services pre-init="false">

    <service name="/api/reward/*"
             interface="s2.adapi.ad.service.adext.ExternalEventWebAction"
             class="s2.adapi.ad.service.adext.ExternalEventWebActionRestImpl"
             pre-init="true"
             singleton="true">
         <property name="external" ref="ad.ExternalEvent"/>
    </service>

</services>
```

**web.xml 의 <servlet-mapping> 설정 예시**

```xml
<?xml version="1.0" encoding="UTF-8"?>

<!DOCTYPE web-app PUBLIC "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN" "http://java.sun.com/dtd/web-app_2\_3.dtd">

<web-app id="s2adapi">

    <display-name>s2adapi</display-name>

    <description>This is S2adapi Web Application.</description>

    <listener>
      <listener-class>s2.adapi.framework.web.action.WebApplicationContextLoader</listener-class>
    </listener>

    <servlet>
      <servlet-name>dispatcher</servlet-name>
      <servlet-class>s2.adapi.server.s2adapi.WebActionDispatcher</servlet-class>
    </servlet>

    <servlet-mapping>
        <servlet-name>dispatcher</servlet-name>
        <url-pattern>/api/*</url-pattern>
    </servlet-mapping>

</web-app>
```

위 예시에서 http://your_domain/your_context/api/reward/adways/1234/5678 을 호출하면 ExternalEventWebActionRestImpl 클래스의 adways() 메소드가 호출되며 getPayload() 호출 결과로 
- payload\[0] = "always"
- payload\[1] = "1234"
- payload\[2] = "5678" 이 담겨지게 된다.

# 7. Transaction

트랜젝션 처리를 위하여 프레임워크에서는 Transaction 클래스를 제공한다. 제공되는 Transaction 클래스는 아래와 같은 특징이 있다.

- 프레임워크가 제공하는 DAO 클래스와 DataSource 클래스와 함께 사용해야 한다.
- Thread 당 동시에 하나의 트랜젝션만 처리가능하며, 여러 Thread 에 걸친 트랜젝션은 지원하지 않는다.
- 동시에 여러 DataSource 를 commit() 할 수 있으나, 2 Phase commit 기능은 제공하지 않는다. DataSource 별로 별도 commit() 처리된다.
- 트랜젝션 내부의 상태는 STATE_INACTIVE 와 STATE_ACTIVE 2개의 상태로 관리되며 트랜젝션이 시작(begin)되면 STATE_ACTIVE, 트랜젝션이 종료(end), 커밋(commit), 롤백(rollback)되면 STATE_INACTIVE 상태로 전이된다.

Transaction 클래스가 제공하는 메소드는 아래와 같다.

- public static Transaction current()
	- 현재 Thread 에 연결된 Transaction 객체를 반환한다.
- public void begin()
	- 트랜젝션을 시작하고 상태 값을 STATE_ACTIVE 로 변경한다.
- public void end() throws SQLException
	- 트랜젝션을 종료하고 상태 값을 STATE_INACTIVE로 변경한다. 현재 트랜젝션이 rolbackonly 로 마킹되어 있다면 rollback() 을 수행하고 아니라면 commit() 을 수행한다.
- public void commit() throws SQLException
	- 트랜젝션을 commit() 처리하고 상태 값을 STATE_INACTIVE로 변경한다. 직접적으로 호출하는 것보다 end() 를 호출하는 것이 바람직하다.
- public void rollback() throws SQLException
	- 트랜젝션을 rollback() 처리하고 상태 값을 STATE_INACTIVE로 변경한다. 직접적으로 호출하는 것보다 rollback 으로 마킹 후 end() 를 호출하는 것이 바람직하다.
- public void setRollbackOnly()
	- 현재 트랜젝션을 rollback 으로 마킹한다.
-   public boolean isActive()
	- 현재 트랜젝션이 STATE_ACTIVE 인 경우 true 를 반환한다.

Transaction 을 사용한 개발 예시는 다음과 같다.

**Transaction 적용 예시**

```java
public class WebActionDispatcher extends s2.adapi.framework.web.action.WebActionDispatcher {

    protected void preProcess(HttpServletRequest request, HttpServletResponse response) 
    {

        Object userId = ContextManager.getServiceContext().getRole("login_id");

        if (userId != null ) {
            MDC.put("userid", userId);
        }

        MDC.put("sid", idgen.getNextId(this).toString());
        MDC.put("ipaddr", StringHelper.null2void(request.getRemoteAddr()));

        Transaction.current().begin();
    }

    protected void postProcess(HttpServletRequest request, HttpServletResponse response, WebActionForward forward) 
    {

        try {

            if (Transaction.current().isActive()) {
                Transaction.current().end();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        logDiagnostics(null);
        ContextManager.clearAll();
    }

}
```

# 8. Messages

### (1) 메시지 파일

사용자에게 보여주는 각종 메시지를 메시지 파일을 사용하여 통합관리하기 위한 목적으로 사용한다. 언어별 메시지 파일을 준비하여 다국어 처리도 가능하다.

메시지 파일은 XML 형식으로 작성하며 구조는 다음과 같다.

- \<resources> : 최상위 노드이다. 하위에 \<resource>  노드들을 갖는다.
- \<resource> : 하나의 메시지를 정의한다. 다음과 같은 형식으로 작성한다.
	- \<resource id="message_id"><![CDATA[메시지 내용을 작성, 파라메터 가능함 {0} {1}]]></resource>
	- message_id : 메시지를 식별하기 위한 키 값이다.
	- 메시지 내용에는 파라메터를 정의할 수 있으며 첫번째 파라메터는 {0}, 두번째 파라메터는 {1} 과 같은 식으로 지정한다.

메시지 파일의 작성 예시는 다음과 같다.

**메시지 파일 작성 예시 : bundle_ko.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>

<resources>
    <resource id="login.login.main_id"><![CDATA[아이디]]></resource>
    <resource id="login.login.main_pwd"><![CDATA[비밀번호]]></resource>
    <resource id="login.login.main_name"><![CDATA[이름]]></resource>
    <resource id="login.login.main_email"><![CDATA[이메일]]></resource>
    <resource id="login.login.main_signup"><![CDATA[회원가입]]></resource>
    <resource id="login.login.main_forgot"><![CDATA[아이디/비밀번호 찾기]]></resource>
    <resource id="login.login.main_find_id"><![CDATA[아이디 찾기]]></resource>
    <resource id="login.login.main_find_pw"><![CDATA[비밀번호 찾기]]></resource>
    <resource id="login.login.main_invalid_email"><![CDATA[이메일 형식이 잘못되었습니다.]]></resource>
</resources>
```

메시지 파일 로딩 절차는 아래와 같다.

- 로딩할 메시지 파일은 기본 명칭으로 지정한다. (언어나 국가 코드를 제외한 파일 명) 예) bundle.xml
- 메시지 파일명에 현재 시스템의 언어코드와 국가 코드를 추가하여 메시지 파일을 찾는다. 예) bundle_ko_KR.xml
- 해당 메시지 파일이 없으면 언어코드만 추가하여 메시지 파일을 찾는다. 예) bundle_ko.xml
- 이 경우에도 해당 메시지 파일이 없으면 기본 파일 명으로 메시지 파일을 찾는다. 예) bundle.xml

메시지 내용을 가져오기 위해서는 Messages 클래스를 사용한다. Messages 클래스가 제공하는 메소드는 아래와 같다.

- public static Messages getMessages()
- public static Messages getMessages(String name)
	- Messages 객체를 전달받는다. 파라메터로 메시지 파일의 구분값을 전달한다. 이 값은 프레임워크 설정파일에서 지정되어 있다. 자세한 내용은 [Configuration]  을 참고한다.
	- 파라메터를 지정하지 않으면 디폴트로 설정된 메시지 파일이 사용된다.
- public String getMessage(String key)
- public String getMessage(String key, Object arg)
- public String getMessage(String key, Object[] args)
	- 전달 받은 Messages 객체에 설정된 메시지를 받아온다.
	- key : 메시지 파일에 설정된 message_id 이다.
	- arg, args : 메시지 내용에 파라메터({0}, {1} ..) 이 있는 경우 전달된 파라메터 값으로 치횐된다.

### (2) 예외 처리

업무 처리와 관련된 오류발생 시 프레임워크에서 제공하는 ApplicationException 을 생성하여 throw 한다. ApplicationException 클래스가 제공하는 메소드들은 다음과 같다.

- 생성자
	- public ApplicationException(String key)
	- public ApplicationException(Throwable cause)
	- public ApplicationException(String key, Object param1)
	- public ApplicationException(String key, Object param1, Throwable cause)
	- public ApplicationException(String key, Object[] params)
	- public ApplicationException(String key, Object[] params, Throwable cause)
		- 디폴트 메시지 파일에서 파라메터 전달된 key 에 해당되는 메시지를 이용하여 ApplicationException 객체를 생성한다.
		- param1, params : 해당 메시지에 파라메터({0}, {1} ...) 가 있는 경우 전달된 파라메터 값으로 치환된다.
		- cause : 원인이되는 Exception 이 있는 경우 이를 같이 지정한다.
 - 메시지 내용 가져오기
	- public String getKey()
		- 설정된 메시지 키 값이 반환된다.
	- public String getMessage()
		- key 로 지정된 메시지 내용이 반환된다.
	- public String getLocalizedMessage()
		- key 로 지정된 메시지 내용과 오류 발생위치가 반환된다.
		- 반환되는 문자열 구조는 아래와 같다. <메시지 내용>|<오류 발생위치>
		- 오류 발생 위치는 업무 프로그램 내에서의 StackTrace 를 사용하여 생성된다.

 예외 처리의 코딩 예시는 다음과 같다.

**예외처리 예시**

```java
    public Mail setHtmlMsg(String msg) throws ApplicationException {

        try {
            htmlEmail.setHtmlMsg(msg);
        } 
        catch (EmailException e) {
            throw new ApplicationException("s2adapi.error.10002", e);
        }

        return this;
    }
```

# 9. Configuration

### (1) Configurator

Configurator 는 정해진 절차에 따라 프레임워크의 내부 설정을 진행한다. 이를 위하여  프레임워크가 로딩 되는 시점에 아래의 절차에 따라서 설정 파일을 로딩한다.

- System Property 에서 "s2adapi.config.path" 로 지정된 경로가 있는지 확인한다. 있으면 그 경로를 설정 파일의 path로 사용한다. 지정된 경로가 없으면 기본값으로 "s2adapi-config.properties" 를 설정 파일 path로 사용한다.
- 결정된 설정 파일 path 를 class-path 에서 찾는다. 있으면 그 파일을 사용하여 설정 파일을 로딩한다.
- class-path 에서 설정 파일을 못찾으면 설정 파일 앞에 "resources" 경로를 추가하여 다시 설정 파일을 로딩한다.
- 위 경로에서도 설정파일을 찾을 수 없으면 ConfiguratorException 이 발생된다.
- 아래의 값들이 System Property 에 설정된다.
	- s2adapi.config.path : 로딩된 설정파일의 class-path 상의 경로 (파일명 포함)
	- s2adapi.config.base : 로딩된 설정파일의 class-path 상의 경로 (파일명 제외)
	- s2adapi.config.base.absolute : 설정 파일의 파일시스템 상의 절대 경로
	- s2adapi.config.parent.base : 설정 파일의 절대 경로의 상위 경로 명
	- s2adapi.config.parent.parent.base : 설정 파일의 절대 경로의 상위의 상위 경로 명
- 설정 값내에 "s2adapi.log4j.config" 항목이 있는 경우에는 그 파일 경로를 이용하여 log4j 를 재 설정한다.

Configurator 객체는 singleton 객체로 관리되며 아래의 static 메소드를 호출하여 획득할 수 있다.

- public static Configurator getConfigurator() throws ConfiguratorException
	- 위 절차에 따라서 설정파일을 로딩하고 설정결과를 당은 Configurator 객체를 반환한다.

Configurator 가 로딩한 설정값들은  Configurator 객체를 사용하여 접근할 수 있다. Configurator 객체는 Configurator.getConfigurator()를 호출하여 획득할 수 있으며 아래의 메소드들을 제공한다.

- public String getString(String key) throws ConfiguratorException
- public String getString(String key, String defaultValue)
- public int getInt(String key) throws ConfiguratorException
- public int getInt(String key, int defaultValue)
- public long getLong(String key) throws ConfiguratorException
- public long getLong(String key, long defaultValue)
- public boolean getBoolean(String key) throws ConfiguratorException
- public boolean getBoolean(String key, boolean defaultValue)
	- 설정파일에 key 명칭으로 설정된 값을 가져온다. 설정파일에 설정되지 않은 경우에는 System Property 에서 확인하고 SystemProperty 에도 없는 경우에는 ConfiguratorException 을 throw 한다.
	- 두번째 파라메터로 defaultValue 가 지정된 경우에는 해당 설정값이 없는 경우 defaultValue 가 반환된다.
- public String getPath(String key, String defaultPath)
	- 설정파일에 key 명칭으로 설정된 값을 경로로 반환한다. 시스템에서 사용하는 경로 구분자가 적용되어 반환된다.
- public Set\<Object> getKeySet()
	- 설정파일에 설정된 모든 설정 값들의 key를 반환한다.여기에는 System Property의 설정값들도 모두 포함되어 있다.

### (2) s2adapi-config.properties (설정파일)

프레임워크 설정파일에 설정되는 항목들과 그 의미는 다음과 같다. 참고로 설정 값을 지정할 때에 다른 설정값을 ${설정값명칭} 을 사용하여 참조할 수 있다.

- Resource 및 Messages 관련
	- s2adapi.resources.default.name : 디폴트 리소스 명칭을 지정한다.
	- s2adapi.resources.\<리소스명> : \<리소스명> 으로 로딩할 리소스 파일의 class-path 를 설정한다.
	- s2adapi.resources.\<리소스명> : \<리소스명>의 리소스 파일을 로딩하기 위하여 사용하는 클래스를 설정한다.
	- s2adapi.resources.reload.\<리소스명> : \<리소스명>의 리소스 파일을 자동으로 재로딩할 간격을 지정한다. (분)
- DAO 관련
	- s2adapi.dao.globalmap : global mapping 파일의 class-path 를 설정한다.
	- s2adapi.dao.fetch.limit : DAO 에서 조회 query 실행시 최대 건수를 설정한다. 여기에서 설정된 건수 이상이 조회되면 SqlQueryException 이 발생된다.
- Web 관련
	- s2adapi.web.upload.encoding : 파일업로드시에 적용할 인코딩 값을 설정한다. (디폴트 euc-kr)
	- s2adapi.web.default.encoding : HTTP request 에 인코딩 설정이 없을 경우 적용할 기본 인코딩 값을 설정한다. (디폴트 euc-kr)
- ServiceContainer 관련
	- s2adapi.container.default.name : 기본 서비스 컨테이너 명칭을 지정한다.
	- s2adapi.container.\<컨테이너명>.impl : \<컨테이너명> 으로 지정된 ServiceContainer 의 구현 클래스를 지정한다.
	- s2adapi.container.\<컨테이너명>.reload.interval : ServiceContainer 의 구현 클래스가  ReloadableXmlServiceContainer 인 경우에 리로딩여부를 체크하는 주기를 설정한다. (초)
	- s2adapi.container.\<컨테이너명>.dir.module : 모듈 Jar 파일을 저장하는 경로를 지정한다.
	- s2adapi.container.\<컨테이너명>.dir.class : 모듈 Jar 파일을 압축 해재하는 디렉토리 경로를 지정한다.
	- s2adapi.container.\<컨테이너명>.service.config.path : 서비스 설정파일들이 위치하는 class-path 경로를 지정한다.
	- s2adapi.container.\<컨테이너명>.service.config : 로딩할 서비스 설정파일의 패턴을 나열한다. 
- Log4j 관련
	- s2adapi.log4j.config : 로딩할 log4j 설정파일의 class-path 를 설정한다.
- 자동생성되는 설정 값
	- s2adapi.config.path : 로딩된 설정파일의 class-path 경로 (파일명 포함)
	- s2adapi.config.base : 로딩된 설정파일의 class-path 상의 경로 (파일명 제외)
	- s2adapi.config.base.absolute : 설정 파일의 파일시스템 상의 절대 경로
	- s2adapi.config.parent.base : 설정 파일의 절대 경로의 상위 경로 명
	- s2adapi.config.parent.parent.base : 설정 파일의 절대 경로의 상위의 상위 경로 명

아래는 실제 설정파일 예시이다.

**s2adapi-config.properties**

```
####################################################################
#
# 디폴트로 사용할 메시지 리소스 파일 명과 해당 파일을 로딩하기 위한 리소스 팩토리 클래스 명을 설정
# 주기적으로 리소스를 재로딩하고자 할 경우에는 reload 값을 설정(분)
#
####################################################################

s2adapi.resources.default.name=default
s2adapi.resources.default=${s2adapi.config.base}/default_messages
s2adapi.resources..default=s2.adapi.framework.resources.impl.XMLResources
s2adapi.resources.reload.default=10
s2adapi.resources.portal=${s2adapi.config.base}/portal
s2adapi.resources..portal=s2.adapi.framework.resources.impl.XMLResources
s2adapi.resources.reload.portal=10
s2adapi.resources.bundle=${s2adapi.config.base}/bundle
s2adapi.resources..bundle=s2.adapi.framework.resources.impl.XMLResources
s2adapi.resources.reload.bundle=10

####################################################################
# JdbcQueryDAO Global ResultMap
####################################################################

s2adapi.dao.globalmap=resources/dao_globalmap.xml
s2adapi.dao.fetch.limit=1000

####################################################################
#
# MultipartRequestWrapper의 인코딩 값과 HttpRequest의 Defualt 인코딩 값을 설정
#
# default = euc-kr
####################################################################

s2adapi.web.upload.encoding=utf-8
s2adapi.web.default.encoding=utf-8

####################################################################
# ServiceContainer configurations
####################################################################

s2adapi.container.default.name=s2adapi
s2adapi.container.s2adapi.impl=s2.adapi.framework.container.impl.ReloadableXmlServiceContainer
s2adapi.container.s2adapi.reload.interval=5
s2adapi.container.s2adapi.dir.module=${s2adapi.config.base.absolute}/../../../components
s2adapi.container.s2adapi.dir.class=${s2adapi.config.base.absolute}/../../../../../work/_api_
s2adapi.container.s2adapi.service.config.path=svcdefs
s2adapi.container.s2adapi.service.config=s2adapi\_.*\\.xml

#### log4j 설정 파일 

s2adapi.log4j.config=${s2adapi.config.base}/log4j.properties
s2adapi.config=${s2adapi.config.base.absolute}/s2adapi_config.xml
```
