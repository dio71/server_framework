package s2.adapi.framework.crypto;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DecryptInputStream extends FilterInputStream {

	private Cryptor cryptor = null;
	
	public DecryptInputStream(InputStream in, Cryptor cryptor) {
		super(in);
		this.cryptor = cryptor;
	}
	 
	 @Override
	 public int read() throws IOException {
		int bb = super.read();
		
		return cryptor.decrypt(bb);
	 }
	 
	 @Override
	 public int read(byte[] bb, int offset, int len) throws IOException {
		 int n = super.read(bb,offset,len);
		 if (n > 0) {
			 cryptor.decrypt(bb, offset, n);
		 }
		  
		 return n;
	 }
}
