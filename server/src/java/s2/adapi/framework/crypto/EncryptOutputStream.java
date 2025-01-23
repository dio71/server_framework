package s2.adapi.framework.crypto;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class EncryptOutputStream extends FilterOutputStream {

	private Cryptor cryptor = null;
	   
	public EncryptOutputStream(OutputStream os, Cryptor cryptor) {
		super(os);
		this.cryptor = cryptor;
	}
	
	@Override
	public void write(int bb) throws IOException {
		super.write(this.cryptor.encrypt(bb));
	}
}
