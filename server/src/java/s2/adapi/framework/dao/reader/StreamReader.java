package s2.adapi.framework.dao.reader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.SQLException;
/**
 * <p>
 *  char[] 형태로 {@link java.io.Reader}에서 데이터를 fetch하여 반환한다.<br>
 * </p>
 *
 */
public class StreamReader {
	
	private static final int BUFFER_SIZE = 1024*1024;
	
	/**
	 * char[]형태로 {@link java.io.Reader}에서 데이터를 fetch하여 <br>
	 *  return 한다.
	 * @return char[]형태로 {@link java.io.Reader}에서 데이터를 fetch한 data
	 */
	protected char[] read(Reader reader) throws SQLException {
		
		char[] buffer = new char[BUFFER_SIZE];
		char[] retBuffer = new char[0];
		
		if (reader == null) {
			return retBuffer;	
		}
		
		// 스트림에서 데이타를 읽어 온다.
		try {
			while (true) {
				int n = reader.read(buffer, 0, buffer.length);
				if (n == -1) break;
				char[] temp = new char[retBuffer.length+n];
				System.arraycopy(retBuffer, 0, temp, 0, retBuffer.length);
				System.arraycopy(buffer, 0, temp, retBuffer.length, n);
				retBuffer = temp;
			}
		} catch (IOException ex) {
			throw (SQLException)new SQLException("stream read error.").initCause(ex);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}

		return retBuffer;

	}
	/**
	 * byte[]형태로 {@link InputStream}에서 데이터를 fetch하여 <br>
	 *  return 한다.
	 * @return byte[]형태로 {@link InputStream}에서 데이터를 fetch한 data
	 */
	public byte[] read(InputStream stream) throws SQLException {
		byte[] buffer = new byte[BUFFER_SIZE];
		byte[] retBuffer = new byte[0];
		
		if (stream == null) {
			return retBuffer;	
		}
		
		// 스트림에서 데이타를 읽어 온다.
		try {
			while (true) {
				int n = stream.read(buffer, 0, buffer.length);
				if (n == -1) break;
				byte[] temp = new byte[retBuffer.length+n];
				System.arraycopy(retBuffer, 0, temp, 0, retBuffer.length);
				System.arraycopy(buffer, 0, temp, retBuffer.length, n);
				retBuffer = temp;
			}
		} catch (IOException ex) {
			throw (SQLException)new SQLException("stream read error.").initCause(ex);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}

		return retBuffer;
	}
	/*
	public byte[] read(InputStream stream) throws SQLException {
		byte[] value = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if (stream == null) {
			return new byte[0];
		}
		try {
			while (true) {
				byte[] buffer = new byte[BUFFER_SIZE];
				int read = stream.read(buffer, 0, buffer.length);
				if (read == -1)
					break;
				baos.write(buffer, 0, read);
			}
			value = baos.toByteArray();
		} catch (IOException ex) {
			throw (SQLException)new SQLException("stream read error.").initCause(ex);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}
		return value;
	} */

}
