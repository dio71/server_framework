package s2.adapi.framework.id.impl;

import s2.adapi.framework.id.IdGenerator;

/**
 * 연속 ID 생성기
 * @author 김형도
 * @since 4.0
 */
public class SeqIdGenerator implements IdGenerator {
	
	private int seqNum = (this.hashCode()%1000)*1000000;
	
	public Object getNextId(Object key) {
		return Integer.valueOf(seqNum++);
	}
}
