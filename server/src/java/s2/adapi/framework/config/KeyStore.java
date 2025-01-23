package s2.adapi.framework.config;

import java.util.HashMap;
import java.util.Map;

import s2.adapi.framework.Constants;
import s2.adapi.framework.id.IdGenerator;
import s2.adapi.framework.id.impl.HashIdGenerator;
import s2.adapi.framework.util.ObjectHelper;
import s2.adapi.framework.util.StringHelper;

public class KeyStore {

	private static KeyStore singleInstance = null;
	
	public static KeyStore instance() {
		if (singleInstance == null) {
			singleInstance = new KeyStore();
		}
		
		return singleInstance;
	}

	private Map<String, KeyConfig> keyMap = new HashMap<String, KeyConfig>();

	private KeyStore() {	
	}
	
	public KeyConfig getConfig(String keyName) {
		return keyMap.get(keyName);
	}

	public synchronized void addConfig(String keyName) {
		KeyConfig keyConfig = buildKeyConfig(keyName);
		keyMap.put(keyName, keyConfig);
	}
	
	private KeyConfig buildKeyConfig(String keyName) {
		try {
			Configurator config = ConfiguratorFactory.getConfigurator();
			
			String keyStorePath = config.getString(Constants.CONFIG_KEY_STORE_KEY + keyName);
			
			if (StringHelper.isEmpty(keyStorePath)) {
				return null;
			}
			
			KeyConfig keyConfig = null;
			try {
				Object builder = ObjectHelper.instantiate("s2.adapi.framework.internal.config.KeyConfigBuilder");
				keyConfig = (KeyConfig)ObjectHelper.invoke(builder,"build", 
						new Class<?>[] {String.class, String.class}, new Object[] {keyName, keyStorePath});
			}
			catch(Throwable ex) {
				keyConfig = buildDefault(keyName, keyStorePath);
			} 
			
			return keyConfig;
		}
		catch (ConfiguratorException e) {
			return null;
		}
	}
	
	private KeyConfig buildDefault(String keyName, String keyPath) {
		KeyConfig keyConfig = new KeyConfig() {
			
			private String key = null;
			private String value = null;
			
			@Override
			public String keyName() {
				return key;
			}
			
			@Override
			public String keyValue() {
				String caller = ObjectHelper.getCallerClassName();
				if (caller != null && caller.startsWith(Constants.Package)) {
					return value;
				}
				else {
					return null;
				}
			}
			
			private KeyConfig init(String key, String keyPath, IdGenerator idGen) {
				this.key = key;
				this.value = idGen.getNextId(keyPath).toString();
				return this;
			}
		}.init(keyName, keyPath, new HashIdGenerator(keyName, keyName.length()));
		
		return keyConfig;
	}
	
}
