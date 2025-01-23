package s2.adapi.framework.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Jar 파일 관련 기능을 제공한다.
 * @author kimhd
 *
 */
public class JarUtil {

	/**
	 * 지정한 Jar 파일을 outDir 아래에 extract 한다.
	 * @param jarFile
	 * @param outDir
	 * @param clean outDir 하위 내용을 모두 지우고 진행할지 여부
	 * @throws IOException
	 */
	public static void unjar(File jarFile, File outDir, boolean clean) throws IOException {
		
		if (!outDir.isDirectory()) {
			outDir.mkdirs();
		}
		
		if (clean) {
			FileUtil.deleteDirectory(outDir,false);
		}
		
		FileInputStream fis = new FileInputStream(jarFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		JarInputStream jis = new JarInputStream(bis);
		FileOutputStream fos = null;
		try {
			JarEntry je = null;
			File jfile = null;
			String jeName = null;
			while((je=jis.getNextJarEntry())!= null) {
				jeName = je.getName();
				//System.out.println(jeName);
				if (je.isDirectory()) {
					jfile = new File(outDir,jeName);
					jfile.mkdirs();
				} else {
					int size = (int)je.getSize();
					jfile = new File(outDir,jeName);
					jfile.getParentFile().mkdirs();
					fos = new FileOutputStream(jfile);
					fos.write(readBytes(jis,size));
					fos.close();
				}
			}
		} finally {
			jis.close();
		}
	}
	
	private static byte[] readBytes(InputStream is, int size) throws IOException {
		byte[] b = new byte[size];
		int rb = 0;
		int chunk = 0;
		while(rb < size) {
			chunk = is.read(b, rb, size-rb);
			if (chunk < 0) {
				break;
			}
			rb += chunk;
		}
		return b;
	}
}
