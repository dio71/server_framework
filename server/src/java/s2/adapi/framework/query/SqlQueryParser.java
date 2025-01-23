package s2.adapi.framework.query;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import s2.adapi.framework.dao.reader.ColumnReader;
import s2.adapi.framework.query.element.ConditionalSqlText;
import s2.adapi.framework.query.element.DynamicSqlText;
import s2.adapi.framework.query.element.IterateSqlText;
import s2.adapi.framework.query.element.ResultMap;
import s2.adapi.framework.query.element.ResultMapItem;
import s2.adapi.framework.query.element.SqlParameter;
import s2.adapi.framework.query.element.SqlStatement;
import s2.adapi.framework.query.element.Sqls;
import s2.adapi.framework.query.element.StaticSqlText;
import s2.adapi.framework.query.element.tags.AlwaysTag;
import s2.adapi.framework.query.element.tags.EmptyTag;
import s2.adapi.framework.query.element.tags.EqualTag;
import s2.adapi.framework.query.element.tags.NotEmptyTag;
import s2.adapi.framework.query.element.tags.NotEqualTag;
import s2.adapi.framework.query.element.tags.NotNullTag;
import s2.adapi.framework.query.element.tags.NullTag;
import s2.adapi.framework.util.FileUtil;
import s2.adapi.framework.util.ObjectHelper;
import s2.adapi.framework.util.StringHelper;
import s2.adapi.framework.util.SystemHelper;

/**
 * <p>
 * 다음과 같은 형태로 정의된 SQL 문장을 파싱하여 Sqls 객체를 반환한다. 
 * 파라메터는 <code>#name:mode:type@format#</code> 형태로 정의하며 mode,type,format은 생략할 수 있다.
 * <ul>
 * <li> name : 파라메터 명, ValueObject에서 실제 값을 꺼내올 때 사용될 Key 이다.
 * name이 %로 시작하면 값을 ServiceContext의 getRole()을 사용하여 가져온다.
 * <li> mode : IN, OUT, INOUT 중의 하나를 지정한다. CallableStatement용 SQL 인 경우에만 사용되며, 그
 * 외에는 무시된다.
 * <li> type : java.sql.Types에서 지정된 JDBC Type에 해당되는 문자를 지정한다. 사용예) VARCHAR, NUMERIC 등
 * <li> format : type 중 DATE, TIME, TIMESTAMP 의 경우에는 지정하는 객체가 문자열인 경우 그 문자열의 포멧을
 * 지정할 수 있다.
 * <ul>
 * <li> format 지정은 java.text.SimpleDateFormat 에서 정의하는 포멧을 사용한다.
 * <li> DATE type은 포멧을 지정하지 않을 경우 디폴트 포멧은 'yyyyMMdd'가 사용된다.
 * <li> TIME type은 포멧을 지정하지 않을 경우 디폴트 포멧은 'hhmmss'가 사용된다.
 * <li> TIMESTAMP type은 포멧을 지정하지 않을 경우 디폴트 포멧은 'yyyyMMddhhmmssSSS'가 사용된다.
 * </ul>
 * </ul>
 * 다음은 SimpleDateFormat에서 정의하는 포멧에 대한 설명이다. 
 * 주의할 점은 날짜(일)은 MM 이며 분은 mm, 밀리초는 SSS로 표현된다는 점이다.
 * <table border=0 cellspacing=3 cellpadding=0 summary="Chart shows pattern
 * letters, date/time component, presentation, and examples.">
 * <tr bgcolor="#ccccff">
 * <th align=left>Letter
 * <th align=left>Date or Time Component
 * <th align=left>Presentation
 * <th align=left>Examples
 * <tr>
 * <td><code>G</code>
 * <td>Era designator
 * <td>Text</a>
 * <td><code>AD</code>
 * <tr bgcolor="#eeeeff">
 * <td><code>y</code>
 * <td>Year
 * <td>Year</a>
 * <td><code>1996</code>; <code>96</code>
 * <tr>
 * <td><code>M</code>
 * <td>Month in year
 * <td>Month</a>
 * <td><code>July</code>; <code>Jul</code>; <code>07</code>
 * <tr bgcolor="#eeeeff">
 * <td><code>w</code>
 * <td>Week in year
 * <td>Number</a>
 * <td><code>27</code>
 * <tr>
 * <td><code>W</code>
 * <td>Week in month
 * <td>Number</a>
 * <td><code>2</code>
 * <tr bgcolor="#eeeeff">
 * <td><code>D</code>
 * <td>Day in year
 * <td>Number</a>
 * <td><code>189</code>
 * <tr>
 * <td><code>d</code>
 * <td>Day in month
 * <td>Number</a>
 * <td><code>10</code>
 * <tr bgcolor="#eeeeff">
 * <td><code>F</code>
 * <td>Day of week in month
 * <td>Number</a>
 * <td><code>2</code>
 * <tr>
 * <td><code>E</code>
 * <td>Day in week
 * <td>Text</a>
 * <td><code>Tuesday</code>; <code>Tue</code>
 * <tr bgcolor="#eeeeff">
 * <td><code>a</code>
 * <td>Am/pm marker
 * <td>Text</a>
 * <td><code>PM</code>
 * <tr>
 * <td><code>H</code>
 * <td>Hour in day (0-23)
 * <td>Number</a>
 * <td><code>0</code>
 * <tr bgcolor="#eeeeff">
 * <td><code>k</code>
 * <td>Hour in day (1-24)
 * <td>Number</a>
 * <td><code>24</code>
 * <tr>
 * <td><code>K</code>
 * <td>Hour in am/pm (0-11)
 * <td>Number</a>
 * <td><code>0</code>
 * <tr bgcolor="#eeeeff">
 * <td><code>h</code>
 * <td>Hour in am/pm (1-12)
 * <td>Number</a>
 * <td><code>12</code>
 * <tr>
 * <td><code>m</code>
 * <td>Minute in hour
 * <td>Number</a>
 * <td><code>30</code>
 * <tr bgcolor="#eeeeff">
 * <td><code>s</code>
 * <td>Second in minute
 * <td>Number</a>
 * <td><code>55</code>
 * <tr>
 * <td><code>S</code>
 * <td>Millisecond
 * <td>Number</a>
 * <td><code>978</code>
 * <tr bgcolor="#eeeeff">
 * <td><code>z</code>
 * <td>Time zone
 * <td>General time zone</a>
 * <td><code>Pacific Standard Time</code>; <code>PST</code>;
 * <code>GMT-08:00</code>
 * <tr>
 * <td><code>Z</code>
 * <td>Time zone
 * <td>RFC 822 time zone</a>
 * <td><code>-0800</code> </table>
 * </p>
 * <p>
 * 동적으로 파라메터 값에 따라서 조립되는 Sql 문장 작성도 가능하다.
 * 
 * <pre>
 *    &lt;sqls&gt;
 *        &lt;resultMap id="datemap"&gt;
 *            &lt;result column="regdy" 
 *                    reader="s2.adapi.framework.dao.reader.FormattedDateColumnReader" 
 *                    format="yyyyMMdd"&gt;
  *            &lt;result column="updtym" 
 *                    reader="s2.adapi.framework.dao.reader.FormattedDateColumnReader" 
 *                    format="yyyyMM"&gt;
 *        &lt;/resultMap&gt;
 *        &lt;statement id=&quot;getEmpNo&quot;&gt;&lt;![CDATA[
 *          SELECT -- himed.com.comcodemgr.comcodedao
 *                 id, name, dept
 *            FROM emp_table
 *           WHERE id = #emp_id# ]]&gt;
 *           &lt;isNotEmpty property=&quot;emp_nm&quot;&gt;&lt;![CDATA[
 *                 and name = #emp_nm# ]]&gt;
 *           &lt;/isNotEmpty&gt;
 *        &lt;/statement&gt;
 *        
 *        &lt;statement id=&quot;setComCode&quot resultmap="datemap"&gt;&lt;![CDATA[
 *          INSERT -- himed.com.comcodemgr.comcodedao
 *            INTO comcdeptm 
 *                 (deptcd       ,
 *                  todd         ,
 *                  fromdd       ,
 *                  deptnm       ,
 *                  lastupdtdt   ,
 *                  regid )
 *          VALUES (#deptcd#       ,
 *                  NVL(#todd#     , '29991231'),
 *                  #fromdd#       ,
 *                  #deptnm#       ,
 *                  #updtdt::DATE@yyyy-MM-dd#,
 *                  #$userid#) ]]&gt;
 *        &lt;/statement&gt;   
 *        
 *        &lt;statement id=&quot;exeSwapEmail&quot;&gt;&lt;![CDATA[
 *          {call swapEmail(#email1:INOUT:VARCHAR#, #email2:INOUT:VARCHAR#)}
 *          ]]&gt;
 *        &lt;/statement&gt;
 *        
 *    &lt;/sqls&gt;
 * </pre>
 * 
 * </p>
 * <p>
 * 동적 SQL문을 위하여 다음과 같은 element들을 제공한다.
 * <ul>
 * <li>&lt;isEmpty property="name"&gt; &lt;/isEmpty&gt; : property로 지정된 명칭의 값이
 * 정의되지 않았거나 빈 문자열인경우 수행된다.
 * <li>&lt;isNotEmpty property="name"&gt; &lt;/isNotEmpty&gt; : property로 지정된
 * 명칭의 값이 정의되었고 빈 문자열이 아닌경우 수행된다.
 * <li>&lt;isEqual property="name" compare="value"&gt; &lt;/isEmpty&gt; :
 * property로 지정된 명칭의 값이 정의되었고 compare로 지정된 값과 동일한 경우 수행된다.
 * <li>&lt;isNotEqual property="name" compare="value"&gt; &lt;/isEmpty&gt; :
 * property로 지정된 명칭의 값이 정의되지 않은 경우 또는 정의되었지만 compare로 지정된 값과 다른 경우 수행된다.
 * </ul>
 * 위 element들은 nested 되어 사용이 가능하다.
 * </p>
 * <p>
 * SQL 문장내에서 사용할 상수값들을 정의할 수 있는 &lt;defintion&gt; 기능을 정의한다. 
 * <ul>
 * <li>&lt;definition name="system" value="A"&gt; : 'system'이라는 이름으로 값 'A'가 정의된다.
 * <li>&lt;definition name="system_ora" value="A" for="oracle"&gt; : 실행되는 DB가 oracle인 경우에만 'system_ora'이라는 이름으로 값 'A'가 정의된다.
 * </ul>
 * 정의된 definition들은 동적 SQL문의 조건으로 사용될 수 있다. 이때 동적 SQL element에서 property 속성 명 대신에  defintion을 사용하면 된다.
 * <ul>
 * <li>&lt;isNotEmpty definition="system_ora"&gt; &lt;/isNotEmpty&gt; : system_ora 라는 명칭으로 definition이 있으면 수행된다.
 * 위의 예시에서 Oracle DB에서 실행될 때만 system_ora가 정의되므로 이 문장은 Oracle에서만 실행되게 된다.
 * </ul>
 * </p>
 * @author 김형도
 * @since 4.0
 */
public class SqlQueryParser extends DefaultHandler {
	private static final Logger log = LoggerFactory.getLogger(SqlQueryParser.class);

	private static final String SQLS_NODE = "sqls";

	private static final String INCLUDE_NODE = "include";
	
	private static final String ATTR_INCLUDE_FILE = "file";
	
	private static final String RESULTMAP_NODE = "resultMap";
	
	private static final String ATTR_RESULTMAP_ID = "id";
	
	private static final String RESULTITEM_NODE = "result";
	
	private static final String ATTR_RESULTITEM_COLUMN = "column";
	
	private static final String ATTR_RESULTITEM_TYPE = "type";
	
	private static final String ATTR_RESULTITEM_READER = "reader";
	
	private static final String ATTR_RESULTITEM_FORMAT = "format";
	
	private static final String STATEMENT_NODE = "statement";

	private static final String ATTR_STATEMENT_ID = "id";
	
	//private static final String ATTR_STATEMENT_ADVICE = "advice";
	
	private static final String ATTR_STATEMENT_RESULTMAP = "resultmap";
	
	private static final String ISEMPTY_NODE = "isempty";

	private static final int ISEMPTY_ID = 1;

	private static final String ISNOTEMPTY_NODE = "isnotempty";

	private static final int ISNOTEMPTY_ID = 2;

	private static final String ISEQUAL_NODE = "isequal";

	private static final int ISEQUAL_ID = 3;

	private static final String ISNOTEQUAL_NODE = "isnotequal";

	private static final int ISNOTEQUAL_ID = 4;
	
	private static final String ITERATE_NODE = "iterate";
	
	private static final int ITERATE_ID = 5;

	private static final String ISNULL_NODE = "isnull";
	
	private static final int ISNULL_ID = 6;
	
	private static final String ISNOTNULL_NODE = "isnotnull";
	
	private static final int ISNOTNULL_ID = 7;
	
	private static final String ATTR_TAG_PROPERTY = "property";
	
	private static final String ATTR_TAG_SESSION = "session";

	private static final String ATTR_TAG_COMPARE = "compare";

	private static final String ATTR_TAG_OPEN = "open";
	
	private static final String ATTR_TAG_CLOSE = "close";
	
	private static final String ATTR_TAG_CONJUNCTION = "conjunction";
	
	private static Map<String, Integer> tagMap = null;

	/**
	 * 파싱 결과로 생성된 Sqls 객체를 생성해 놓는다.
	 */
	private Sqls sqls = null;

	/**
	 * 현재 파싱 중인 파일의 결과 내용을 담아 놓을 Sqls 객체
	 */
	private Sqls curSqls = null;

	/**
	 * 현재 파싱 중인 Sql Statement의 ID
	 */
	private String curStmtId = null;

	/**
	 * 현재 파싱 중인 Sql Statement의 ResultMap ID
	 */
	private String curStmtResultMap = null;
	
	/**
	 * 현재 파싱 중인 Sql Statement의 Advice 속성 값
	 */
	private String curAdvice = null;
	
	/**
	 * 현재 파싱 중인 ResultMap 객체
	 */
	private ResultMap curResultMap = null;
	
	/**
	 * 현재 파싱 중인 ResultMap 객체의 ID
	 */
	private String curResultMapId = null;
	
	/**
	 * 현재 파싱 중인 ResultItem 객체
	 */
	private ResultMapItem curResultItem = null;
	
	/**
	 * 현재 파싱 중인 sql text 저장용
	 */
	private StringBuilder sb = new StringBuilder();

	/**
	 * 새로운 DynamicSqlText가 나타나면 현재 것을 담아두기 위한 Stack 객체
	 */
	private Stack<DynamicSqlText> prevDynamicStack = new Stack<DynamicSqlText>();

	private DynamicSqlText curDynamicSql = null;
	
	private boolean underIterate = false; // <iterate> 내에서 true로 설정됨. nested <iterate>를 막기 위하여 사용.
	
	private long stime = 0; // 실행시간 측정용.

	private ClassLoader classLoader = null;
	
	private Set<String> includeSet = null;
	
	private String parentDir = null;
	
	static {
		tagMap = new HashMap<String, Integer>();
		tagMap.put(ISEMPTY_NODE, Integer.valueOf(ISEMPTY_ID));
		tagMap.put(ISNOTEMPTY_NODE, Integer.valueOf(ISNOTEMPTY_ID));
		tagMap.put(ISEQUAL_NODE, Integer.valueOf(ISEQUAL_ID));
		tagMap.put(ISNOTEQUAL_NODE, Integer.valueOf(ISNOTEQUAL_ID));
		tagMap.put(ITERATE_NODE, Integer.valueOf(ITERATE_ID));
		tagMap.put(ISNULL_NODE, Integer.valueOf(ISNULL_ID));
		tagMap.put(ISNOTNULL_NODE, Integer.valueOf(ISNOTNULL_ID));
	}

	public SqlQueryParser(ClassLoader loader) {
		classLoader = loader;
	}
	
	/**
	 * File 객체로 주어진 Sql 정의 파일을 파싱하여 Sqls 객체를 생성한다.
	 * 
	 * @param mapFile
	 * @return
	 */
	public Sqls parse(String path, Set<String> include) throws SAXException {
		
		// 파싱하는 파일의 경로를 저장해놓는다.
		// 이것은 <include>가 circular하게 발생하는 것을 막기위하여 사용한다.
		if (include == null) {
			includeSet = new HashSet<String>();
		} else {
			includeSet = include;
		}
		includeSet.add(path);
		
		// 파싱하는 파일의 부모 디렉토리 경로를 저장해 놓는다.
		// 이것은 <include> 파일의 절대 경로를 찾기 위해서 사용된다.
		parentDir = FileUtil.getParentPath(path);
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = factory.newSAXParser();
			InputStream is = SystemHelper.getResourceAsStream(classLoader,path);
			parser.parse(is, this);
		} catch (SAXException e) {
			throw e;
		} catch (IOException e) {
			throw new SAXException(e.getMessage(), e);
		} catch (ParserConfigurationException e) {
			throw new SAXException(e.getMessage(), e);
		}

		// 파싱이 에러 없이 완료되었다면 Sqls 객체가 생성되어 있다.
		return sqls;
	}
	
	//
	// SAXParser의 DefaultHandler 재정의
	//

	public void startDocument() throws SAXException {
		if (log.isDebugEnabled()) {
			stime = System.currentTimeMillis();
			//log.debug("parsing a sql query file...");
		}
	}

	public void endDocument() throws SAXException {
		if (log.isDebugEnabled()) {
			log.debug("parsing sqls has completed. ("
					+ (System.currentTimeMillis() - stime) + " msec)");
		}
	}

	public void startElement(String namespaceURI, String sName, String qName,
			Attributes attrs) throws SAXException {

		if (SQLS_NODE.equalsIgnoreCase(qName)) {
			startSqlsElement(attrs);
		}
		else if (STATEMENT_NODE.equalsIgnoreCase(qName)) {
			startStatementElement(attrs);
		}
		else if (RESULTMAP_NODE.equalsIgnoreCase(qName)) {
			startResultMapElement(attrs);
		}
		else if (RESULTITEM_NODE.equalsIgnoreCase(qName)) {
			startResultItemElement(attrs);
		}
		else if (INCLUDE_NODE.equalsIgnoreCase(qName)) {
			startIncludeElement(attrs);
		}
		else {
			startDynamicElement(qName, attrs);
		}

	}

	public void endElement(String namespaceURI, String sName, String qName)
			throws SAXException {

		if (SQLS_NODE.equalsIgnoreCase(qName)) {
			endSqlsElement();
		}
		else if (STATEMENT_NODE.equalsIgnoreCase(qName)) {
			endStatementElement();
		}
		else if (RESULTMAP_NODE.equalsIgnoreCase(qName)) {
			endResultMapElement();
		}
		else if (RESULTITEM_NODE.equalsIgnoreCase(qName)) {
			endResultItemElement();
		}
		else if (INCLUDE_NODE.equalsIgnoreCase(qName)) {
			endIncludeElement();
		}
		else {
			endDynamicElement(qName);
		}
	}

	public void characters(char buf[], int offset, int len) throws SAXException {

		if (sb != null) {
			sb.append(buf, offset, len);
		}
	}

	private void startSqlsElement(Attributes attrs) throws SAXException {

		if (log.isDebugEnabled()) {
			log.debug("sqls");
		}

		// 맴버 변수로 Sqls 객체 생성 및 StringBuilder 초기화.
		curSqls = new Sqls();
		sb.setLength(0);
	}

	private void endSqlsElement() throws SAXException {
		sqls = curSqls;
		curSqls = null;
	}

	private void startStatementElement(Attributes attrs) throws SAXException {
		if (curSqls == null) {
			throw new SAXException("unexpected <statement> element.");
		}

		if (curStmtId != null) {
			throw new SAXException("nested <statement> element.");
		}

		String id = attrs.getValue(ATTR_STATEMENT_ID);
		String resultmap = StringHelper.null2string(attrs.getValue(ATTR_STATEMENT_RESULTMAP),null);
		
		//if (log.isDebugEnabled()) {
		//	log.debug("statement [id=" + id + "]");
		//}

		// validation check.
		if (StringHelper.isNull(id)) {
			throw new SAXException("<statement> must have 'id' attribute.");
		}

		// sql statement ID 저장.
		curStmtId = id;
		curStmtResultMap = resultmap;
		
		// sql statement 문자열을 담기 위한 스트링버퍼 비우기
		sb.setLength(0);

		// sql statement 내 sql text들을 담아두기 위한 dynamic sql text를 생성
		curDynamicSql = new ConditionalSqlText(new AlwaysTag());
	}

	private void endStatementElement() throws SAXException {
		if (curStmtId == null) {
			throw new SAXException("unexpected </statement> element.");
		}

		// 지금까지의 Text가 있으면 현재 Dynmaic Sql 객체에 추가한다.
		if (sb.toString().trim().length() > 0) {
			//if (log.isDebugEnabled()) {
			//	log.debug("parsing simple sql=[" + sb.toString() + "]");
			//}
			curDynamicSql.addSqlText(new StaticSqlText(sb.toString()));

		}
		sb.setLength(0); // 버퍼 비우기

		// prevDynamicStack 이 비워져 있어야 한다.
		if (prevDynamicStack.size() > 0) {
			throw new SAXException("parsing stack is not empty..");
		}

		// curDynamic 객체내에 저장된 SqlText들을 사용하여 SqlStatement를 생성한다.
		//if (log.isDebugEnabled()) {
		//	log.debug("statement " + curStmtId + " parsed=[" + curDynamic
		//			+ "]");
		//}
		
		SqlStatement sqlStmt = 
				new SqlStatement(curStmtId, curDynamicSql.getSqlList(), curStmtResultMap, curAdvice);

		// 현재 Sqls 객체에 Statement 객체를 추가한다.
		curSqls.addStatement(sqlStmt);
		
		curAdvice = null;
		curDynamicSql = null;
		curStmtId = null;
		
		curStmtResultMap = null;
	}
	
	private void startDynamicElement(String qName, Attributes attrs)
			throws SAXException {

		Integer tagId = tagMap.get(qName.toLowerCase());

		if (tagId == null) {
			throw new SAXException("unknown element <" + qName + ">.");
		}

		// 지금까지의 Text가 있으면 현재 Dynmaic Sql 객체에 추가한다.
		if (sb.toString().trim().length() > 0) {
			//if (log.isDebugEnabled()) {
			//	log.debug("parsing simple sql=[" + sb.toString() + "]");
			//}
			curDynamicSql.addSqlText(new StaticSqlText(sb.toString()));
		}
		sb.setLength(0); // 버퍼 비우기

		String tagPropName = attrs.getValue(ATTR_TAG_PROPERTY);
		String tagRoleName = attrs.getValue(ATTR_TAG_SESSION);

		int tagValueType = -1;

		String tagName = null;
		String tagCompare = attrs.getValue(ATTR_TAG_COMPARE);
		
		if (tagPropName != null && tagRoleName == null) {
			tagName = tagPropName;
			tagValueType = SqlParameter.VO_VALUE_KIND;
		}
		else if (tagPropName == null && tagRoleName != null) {
			tagName = tagRoleName;
			tagValueType = SqlParameter.ROLE_VALUE_KIND;
		}
		
		DynamicSqlText newDynamic = null;

		switch (tagId.intValue()) {
		case ISEMPTY_ID:
			// validation check.
			if (tagValueType == -1) {
				throw new SAXException(
						"<isEmpty> must have one of 'property' or 'session' attributes.");
			}
			newDynamic = new ConditionalSqlText(new EmptyTag(tagName,tagValueType));
			break;
		case ISNOTEMPTY_ID:
			// validation check.
			if (tagValueType == -1) {
				throw new SAXException(
						"<isNotEmpty> must have one of 'property' or 'session' attributes.");
			}
			newDynamic = new ConditionalSqlText(new NotEmptyTag(tagName,tagValueType));
			break;
		case ISEQUAL_ID:
			// validation check.
			if (tagValueType == -1) {
				throw new SAXException(
						"<isEqual> must have one of 'property' or 'session' attributes.");
			}
			if (StringHelper.isNull(tagCompare)) {
				throw new SAXException(
						"<isEqual> must have 'compare' attribute.");
			}
			newDynamic = new ConditionalSqlText(new EqualTag(tagName,
					tagCompare, tagValueType));
			break;
		case ISNOTEQUAL_ID:
			// validation check.
			if (tagValueType == -1) {
				throw new SAXException(
						"<isNotEqual> must have one of 'property' or 'session' attributes.");
			}
			if (StringHelper.isNull(tagCompare)) {
				throw new SAXException(
						"<isNotEqual> must have 'compare' attribute.");
			}
			newDynamic = new ConditionalSqlText(new NotEqualTag(tagName,
					tagCompare,tagValueType));
			break;
		case ITERATE_ID:
			String tagOpen = attrs.getValue(ATTR_TAG_OPEN);
			String tagClose = attrs.getValue(ATTR_TAG_CLOSE);
			String tagConjunction = attrs.getValue(ATTR_TAG_CONJUNCTION);
			if (underIterate) {
				throw new SAXException("<iterate> cannot be nested.");
			}
			if (StringHelper.isNull(tagPropName)) {
				throw new SAXException(
						"<iterate> must have 'property' attribute.");
			}
			newDynamic = new IterateSqlText(tagPropName,tagOpen,tagClose,tagConjunction);
			underIterate = true;
			break;
		case ISNULL_ID:
			// validation check.
			if (StringHelper.isNull(tagPropName)) {
				throw new SAXException(
						"<isNull> must have 'property' attribute.");
			}
			newDynamic = new ConditionalSqlText(new NullTag(tagName,tagValueType));
			break;
		case ISNOTNULL_ID:
			// validation check.
			if (StringHelper.isNull(tagPropName)) {
				throw new SAXException(
						"<isNotNull> must have 'property' attribute.");
			}
			newDynamic = new ConditionalSqlText(new NotNullTag(tagName,tagValueType));
			break;
		}

		// 새로 생성된 DynamicSqlText객체를 현재 DynamicSqlText 객체에 추가한다.
		curDynamicSql.addSqlText(newDynamic);

		// 현재 DynamicSqlText객체를 Stack에 넣고 새로 생성된 DynamicSqlText 객체를 현재 객체로 설정한다.
		prevDynamicStack.push(curDynamicSql);
		curDynamicSql = newDynamic;
	}

	private void endDynamicElement(String qName) throws SAXException {
		Integer tagId = tagMap.get(qName.toLowerCase());

		// 지금까지의 Text가 있으면 현재 Dynamic Sql 객체에 추가한다.
		if (sb.toString().trim().length() > 0) {
			//if (log.isDebugEnabled()) {
			//	log.debug("parsing simple sql=[" + sb.toString() + "]");
			//}
			curDynamicSql.addSqlText(new StaticSqlText(sb.toString()));
		}
		sb.setLength(0); // 버퍼비우기

		// Stack에서 이전 DynamicSqlText 객체를 꺼내어 현재 DynamicSqlText 객체로 설정한다.
		curDynamicSql = prevDynamicStack.pop();
		if (curDynamicSql == null) {
			throw new SAXException("parsing stack is empty.");
		}
		
		if (tagId == ITERATE_ID) {
			underIterate = false;
		}
	}
	
	private void startResultMapElement(Attributes attrs) throws SAXException {
		if (curSqls == null || curStmtId != null) {
			throw new SAXException("unexpected <resultMap> element.");
		}

		if (curResultMap != null) {
			throw new SAXException("nested <resultMap> element.");
		}

		String id = attrs.getValue(ATTR_RESULTMAP_ID);

		//if (log.isDebugEnabled()) {
		//	log.debug("resultMap [id=" + id + "]");
		//}

		// validation check.
		if (StringHelper.isNull(id)) {
			throw new SAXException("<resultMap> must have 'id' attribute.");
		}

		// current ResultMap 객체 생성
		curResultMapId = id;
		curResultMap = new ResultMap();
	}
	
	private void endResultMapElement() throws SAXException {
		if ( curResultMap == null ) {
			throw new SAXException("unexpected </resultMap> element.");
		}
		
		// 현재 Sqls 객체에 ResultMap 객체를 추가한다.
		curSqls.addResultMap(curResultMapId,curResultMap);
		
		curResultMapId = null;
		curResultMap = null;
	}
	
	private void startResultItemElement(Attributes attrs) throws SAXException {
		if (curResultMap == null) {
			throw new SAXException("unexpected <result> element.");
		}

		if (curResultItem != null) {
			throw new SAXException("nested <result> element.");
		}
		
		String column = StringHelper.null2string(attrs.getValue(ATTR_RESULTITEM_COLUMN),null);
		String type = StringHelper.null2string(attrs.getValue(ATTR_RESULTITEM_TYPE),null);
		String reader = StringHelper.null2string(attrs.getValue(ATTR_RESULTITEM_READER),null);
		String format = StringHelper.null2string(attrs.getValue(ATTR_RESULTITEM_FORMAT),null);
		
		//if (log.isDebugEnabled()) {
		//	log.debug("result [column="+column+",type="+type+",reader="+reader+",format="+format+"]");
		//}

		// validation check.
		if (column == null && type == null) {
			throw new SAXException("<result> must have 'column' or 'type' attribute.");
		}
		if (column != null && type != null) {
			throw new SAXException("<result> must not have both 'column' and 'type' attribute.");
		}
		if (reader == null) {
			throw new SAXException("<result> must hava 'reader' attribute.");
		}
		
		// reader 객체 생성
		ColumnReader readerObj;
		try {
			if (format == null) {
				readerObj = (ColumnReader)ObjectHelper.instantiate(reader);
			} else {
				readerObj = (ColumnReader)ObjectHelper.instantiate(reader,
						new Class[]{String.class}, new Object[] {format});
			}
		} catch (ClassNotFoundException e) {
			throw new SAXException("<result>'s reader["+reader+"] class not found: "+e.getMessage());
		} catch (ClassCastException e) {
			throw new SAXException("<result>'s reader["+reader+"] is not ColumnReader: "+e.getMessage());
		} catch (Exception e) {
			throw new SAXException("<result>'s reader["+reader+"] cannot instantiated: "+e.getMessage());
		}
		
		if (column == null) {
			curResultItem = new ResultMapItem(type,false,readerObj);
		} else {
			curResultItem = new ResultMapItem(column,true,readerObj);
		}
	}
	
	private void endResultItemElement() throws SAXException {
		if ( curResultItem == null ) {
			throw new SAXException("unexpected </result> element.");
		}
		
		// 현재 ResultMap 객체에 ResultItem 객체를 추가한다.
		curResultMap.addItem(curResultItem);
		
		curResultItem = null;
	}
	
	private void startIncludeElement(Attributes attrs) throws SAXException {
		if (curSqls == null) {
			throw new SAXException("unexpected <include> element.");
		}
		
		String filename = StringHelper.null2string(attrs.getValue(ATTR_INCLUDE_FILE),null);

		// validation check.
		if (filename == null) {
			throw new SAXException("<include> must have 'file' attribute.");
		}
		
		filename = FileUtil.getCanonicalPath(parentDir + filename); // 절대 경로로 변경
		
		if (includeSet != null && includeSet.contains(filename)) {
			// circular-include
			throw new SAXException("circular <include> : "+filename+" is aleady included before.");
		}
		
		// QueryParser 객체 생성
		SqlQueryParser includeParser = new SqlQueryParser(classLoader);
		Sqls includeSqls = includeParser.parse(filename,includeSet);
		
		// 포함된 sqls 파싱 결과를 추가
		curSqls.addSqls(includeSqls);
	}
	
	private void endIncludeElement() throws SAXException {
	}
	
}
