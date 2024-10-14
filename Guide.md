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

**service configuration**

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

