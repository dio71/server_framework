package s2.adapi.framework.query.element;

import s2.adapi.framework.dao.reader.ColumnReader;

/**
 * ResultItem은 ResultMap을 구성하는 요소이며 하나의 ResultItem은 하나의 ColumnReader를 지정한다.
 * 조회된 컬럼 명을 기준으로 ColumnReader를 지정하거나, 컬럼의 특정 타입 전체에 대하여 ColumnReader를 
 * 지정할 수 있다.
 * @author 김형도
 * @since 4.0
 */
public class ResultMapItem {
	private String name = null;
	private boolean isColumnItem = true;
	private ColumnReader reader = null;
	
	public ResultMapItem(String name, boolean isColumn, ColumnReader reader) {
		this.name = name;
		this.isColumnItem = isColumn;
		this.reader = reader;
	}
	
	public boolean isColumnItem() {
		return isColumnItem;
	}
	
	public String getName() {
		return name;
	}
	
	public ColumnReader getReader() {
		return reader;
	}
	public String toString() {
		return name+","+isColumnItem+","+reader.getClass().getName();
	}
}
