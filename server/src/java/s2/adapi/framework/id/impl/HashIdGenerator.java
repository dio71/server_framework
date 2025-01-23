package s2.adapi.framework.id.impl;

import s2.adapi.framework.id.IdGenerator;
import s2.adapi.framework.util.DigesterHelper;

public class HashIdGenerator implements IdGenerator {

	private String salt;
	private int iteration;
	public HashIdGenerator(String salt, int iteration) {
		this.salt = salt;
		this.iteration = iteration;
	}
	
	@Override
	public Object getNextId(Object key) {
		String id = String.valueOf(key);
		for(int i=0; i < iteration; i++) {
			id = DigesterHelper.sha256Hex(id + salt);
		}
		
		return id;
	}

}
