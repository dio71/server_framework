package s2.adapi.framework.crypto;

public interface Cryptor {
	
	/**
	 * initialize internal state
	 * @return
	 */
	public Cryptor init();
	
	/**
	 * encrypt single byte
	 * @param data
	 * @return
	 */
	public int encrypt(int b);
	
	/**
	 * encrypt byte array
	 * @param data
	 * @param offset
	 * @param len
	 */
	public void encrypt(byte[] b, int offset, int len);
	
	/**
	 * decrypt single byte
	 * @param data
	 * @return
	 */
	public int decrypt(int b);
	
	/**
	 * decrypt byte array
	 * @param b
	 * @param offset
	 * @param len
	 */
	public void decrypt(byte[] b, int offset, int len);
}
