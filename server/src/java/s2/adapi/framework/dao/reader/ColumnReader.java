/*
 * Copyright(c) 2010 s2adapi Corporation. All rights reserved. 
 * http://www.s2adapi.com
 */
package s2.adapi.framework.dao.reader;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ResultSet과 CallableStatement에서 결과 data를 fetch해오는 인터페이스이다.
 */
public interface ColumnReader {
    
    /**
     * ResultSet 에서 해당 columnIndex에 해당하는 row의 값을 fetch하여 return하도록 
     * 하위 Class에서 구현해야 한다.
     * @param columnIndex fetch할 ResultSet의 해당 index
     * @param rs fetch할 대상이 되는 ResultSet
     * @return ResultSet에서 fetch한 value
     * @throws SQLException
     */
    public Object read(int columnIndex, ResultSet rs) throws SQLException;
    
    /**
     * 실행된 CallableStatement에서 해당 columnIndex에 해당되는 값을 fetach하여 return 하도록
     * 하위 Class에서 구현해야 한다.
     * @param columnIndex
     * @param cstmt
     * @return
     * @throws SQLException
     */
    public Object read(int columnIndex, CallableStatement cstmt) throws SQLException;
}
