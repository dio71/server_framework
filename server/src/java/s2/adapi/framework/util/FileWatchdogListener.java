package s2.adapi.framework.util;

import java.util.EventListener;

public interface FileWatchdogListener extends EventListener {

	public void fileChanged();

}
