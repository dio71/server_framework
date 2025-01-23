package s2.adapi.framework.crypto;

/**
 * RC4 Cryptor
 * @author kimhd
 *
 */
public class RC4Cryptor implements Cryptor {
	private byte[] key = null;
	private int[] salt = new int[256];
	private int b = 0;
	private int c = 0;

	public RC4Cryptor(byte[] key) {
		this.key = key;
	}
	
	public Cryptor init() {
		b = 0;
		c = 0;

		if (key.length < 1 || key.length > 32) {
			throw new IllegalArgumentException(
					"number of bytes must be between 1 and 32");
		}
		for (int i = 0; i < salt.length; i++) {
			salt[i] = i;
		}

		int keyIndex = 0;
		int saltIndex = 0;
		for (int i = 0; i < salt.length; i++) {
			saltIndex = (fixByte(key[keyIndex]) + salt[i] + saltIndex) % 256;
			swap(salt, i, saltIndex);
			keyIndex = (keyIndex + 1) % key.length;
		}
		
		return this;
	}
	
	public int encrypt(int data) {
		b = (b + 1) & 0xff;
		c = (salt[b] + c) & 0xff;
		swap(salt, b, c);
		int saltIndex = (salt[b] + salt[c]) & 0xff;
		
		return (data ^ (byte)salt[saltIndex]);
	 }
	
	public int decrypt(int data) {
		b = (b + 1) & 0xff;
		c = (salt[b] + c) & 0xff;
		swap(salt, b, c);
		int saltIndex = (salt[b] + salt[c]) & 0xff;
		
		return (data ^ (byte)salt[saltIndex]);
	 }
	
	 public void encrypt(byte[] data, int offset, int len) {
		 
		 decrypt(data,offset,len);
	 }
	
	 public void decrypt(byte[] data, int offset, int len) {
		 
		 for(int i=0;i<len;i++) {
			b = (b + 1) & 0xff;
			c = (salt[b] + c) & 0xff;
			swap(salt, b, c);
			int saltIndex = (salt[b] + salt[c]) & 0xff; //% 256;
			
			data[offset+i] = (byte)(data[offset+i] ^ (byte)salt[saltIndex]);
		 }
	 }

	/**
	 * This will swap two values in an array.
	 * 
	 * @param data The array to swap from.
	 * @param firstIndex The index of the first element to swap.
	 * @param secondIndex The index of the second element to swap.
	 */
	private final void swap(int[] data, int firstIndex, int secondIndex) {
		int tmp = data[firstIndex];
		data[firstIndex] = data[secondIndex];
		data[secondIndex] = tmp;
	}

	/**
	 * This will ensure that the value for a byte >=0.
	 * 
	 * @param aByte The byte to test against.
	 * @return A value >=0 and < 256
	 */
	private final int fixByte(byte aByte) {
		return aByte < 0 ? 256 + aByte : aByte;
	}
}
