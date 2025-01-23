package s2.adapi.framework.crypto;

/**
 * XOR 기반의 단순 Cryptor 구현 클래스
 * @author kimhd
 *
 */
public class XorCryptor implements Cryptor {
	private byte[] key = null;
	private int[] salt = new int[256];
	private int b = 0;
	
	public XorCryptor(byte[] key) {
		this.key = key;
	}
	
	@Override
	public Cryptor init() {
		b = 0;

		if (key.length < 1 || key.length > 256) {
			throw new IllegalArgumentException(
					"number of bytes must be between 1 and 256");
		}
		for (int i = 0; i < salt.length; i++) {
			salt[i] = i;
		}

		int keyIndex = 0;
		for (int i = 0; i < salt.length; i++) {
			salt[i] = (salt[i] + key[keyIndex]) & 0xff;
			keyIndex = (keyIndex + 1) % key.length;
		}
		
		return this;
	}

	@Override
	public int encrypt(int data) {
		b = (b + 1) & 0xff;
		return (data ^ (byte)salt[b]);
	}

	@Override
	public void encrypt(byte[] data, int offset, int len) {
		decrypt(data,offset,len);
	}

	@Override
	public int decrypt(int data) {
		b = (b + 1) & 0xff;
		return (data ^ (byte)salt[b]);
	}

	@Override
	public void decrypt(byte[] data, int offset, int len) {
		for(int i=0;i<len;i++) {
			b = (b + 1) & 0xff;
			data[offset+i] = (byte)(data[offset+i] ^ (byte)salt[b]);
		 }
	}

}
