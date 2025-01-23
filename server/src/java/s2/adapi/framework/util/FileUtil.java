package s2.adapi.framework.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 파일 탐색 기능을 제공하는 클래스이다.
 * @author kimhd
 * @since 1.0
 */
public class FileUtil {

    /**
     * <p>
     * 에러나 이벤트와 관련된 각종 메시지를 로깅하기 위한 Log 오브젝트
     * </p>
     */
    private static final Logger log = LoggerFactory.getLogger(FileUtil.class);

    /**
     * <p>
     * 지정한 확장자들만 가져올 수 있도록 하기 위한 FilenameFilter의 구현 클래스
     * </p>
     */
    private static class FileExtensionFilter implements FilenameFilter {

        String acceptableExtensions[];

        FileExtensionFilter(String ext[]) {
            acceptableExtensions = ext;
        }

        public boolean accept(File dir, String fname) {
            if (acceptableExtensions == null) {
                return true;
            }

            // check if the extension of the file is in the ecceptable list.
            for (int i = 0; i < acceptableExtensions.length; i++) {
                if (fname.endsWith(acceptableExtensions[i])) {
                    return true;
                }
            }

            // if the file is a directory, then return true.
            File tempFile = new File(dir, fname);
            return tempFile.isDirectory();
        }
    }

    /**
     * <p>
     * 지정된 패턴에 해당되는 파일명으로 검색하기 위한 FilenameFilter의 구현 클래스
     * </p>
     */
    private static class FilenamePatternFilter implements FilenameFilter {

    	List<Pattern> patternList = new ArrayList<Pattern>();

    	FilenamePatternFilter(String patternStr) {
    		if (patternStr != null) {
    			patternList.add(Pattern.compile(patternStr));
    		}
    	}
    	
        FilenamePatternFilter(String[] patternStr) {
        	if (patternStr == null) {
        		return;
        	}
        	
            for(int i=0;i<patternStr.length;i++) {
            	patternList.add(Pattern.compile(patternStr[i]));
            }
        }

        public boolean accept(File dir, String fname) {

            // check if the extension of the file is in the ecceptable list.
            for (int i=0;i<patternList.size();i++) {
            	if (patternList.get(i).matcher(fname).matches()) {
            		return true;
            	}
            }

            // if the file is a directory, then return true.
            File tempFile = new File(dir, fname);
            return tempFile.isDirectory();
        }
    }
 
    /**
     * 여러 개의 파일을 하나의 파일로 합치는 기능을 제공한다.
     * @param targetName
     * @param sourceNames
     * @param 파일 합친 후 소스 파일 삭제 여부
     * @throws IOException 
     */
    public static void mergeFile(String targetName, String[] sourceNames, boolean deleteSource) throws IOException {
    	File targetFile = new File(targetName);
    	FileOutputStream fos = new FileOutputStream(targetFile);
    	
    	File sourceFile = null;
    	FileInputStream fis = null;
    	int len = 0;
    	byte[] buf = new byte[1024*1024];
    	
    	try {
	    	for(int i=0;i<sourceNames.length;i++) {
	    		sourceFile = new File(sourceNames[i]);
	    		fis = new FileInputStream(sourceFile);
	    		while((len=fis.read(buf))>0) {
	    			fos.write(buf,0,len);
	    		}
	    		fis.close();
	    		sourceFile.delete();
	    	}
    	} finally {
    		fos.close();
    	}
    }
    
    
    /**
     * 주어진 dir 하위의 내용을 모두 삭제한다.
     * @param dir 삭제할 디렉토리
     * @param include dir도 삭제할지 여부
     * @return
     */
    public static boolean deleteDirectory(File dir, boolean include) {
    	if (dir.exists()) {
    		File[] files = dir.listFiles();
    		if (files != null) {
    			for(int i=0;i<files.length;i++) {
	    			if (files[i].isDirectory()) {
	    				deleteDirectory(files[i],true);
	    			} else {
	    				files[i].delete();
	    			}
	    		}
    		}
    		if (include) {
        		return dir.delete();
        	} else {
        		return true;
        	}
    	} else {
    		return true;
    	}
    	
    	
    }
    
    /**
     * <p> 지정한 디렉토리와 그 하위 디렉토리 내에 있는 파일들의 목록을 이름으로 가져온다.
     *
     * @param rootDir       시작 디렉토리 path 문자열
     * @param ext           가져올 파일들의 확장자 목록을 지정한 String 배열
     * @param excludingRoot true이면 파일이름의 Path명에서 rootFile의 Path명을 제외한다.
     * @return 파일들의 이름 리스트
     */
    public static String[] getFilenamesUnder(String rootDir, String ext[], boolean excludingRoot) {

        List<String> filenames = new ArrayList<String>();
        File rootFile = null;

        if (rootDir == null) {
            rootDir = ".";
        }
        rootFile = new File(rootDir);

        String rootPathString = rootFile.getPath();
        File tempFile = null;
        String tempString = null;

        for (Iterator<File> i = getAllFilesUnder(rootFile, new FileExtensionFilter(ext)).iterator();
             i.hasNext();) {
            tempFile = i.next();
            tempString = tempFile.getPath();
            if (excludingRoot) {
                tempString = tempString.substring(rootPathString.length() + 1, tempString.length());
            }
            if (tempString.startsWith(File.separator)) {
                tempString = tempString.substring(1);
            }

            filenames.add(tempString);
        }

        return filenames.toArray(new String[0]);
    }

    /**
     * <p> 지정한 디렉토리와 그 하위 디렉토리 내에 있는 클래스 파일들을 찾아서 그 클래스명을
     * 가져온다. <code>pkgPrefix</code>를 지정하면 클래스명의 패키지 구조 앞에 <code>pkgPrefix</code>가
     * 붙어서 클래스명이 생성된다.
     * </p>
     *
     * @param rootDir       시작 디렉토리 path 문자열
     * @param pkgPrefix     공통 패키지 명
     * @param excludingRoot true이면 클래스의 패키지명 시작을 rootDir의 위치를 기준으로 생성한다.
     * @return 검색된 클래스 명 리스트
     */
    public static String[] getClassnamesUnder(String rootDir, String pkgPrefix, boolean excludingRoot) {

        List<String> classnames = new ArrayList<String>();
        File rootFile = null;

        if (rootDir == null) {
            rootDir = ".";
        }
        rootFile = new File(rootDir);

        String rootPathString = rootFile.getPath();
        File tempFile = null;
        String tempString = null;

        for (Iterator<File> i = getAllFilesUnder(rootFile, new FileExtensionFilter(new String[]{".class"})).iterator();
             i.hasNext();) {
            tempFile = i.next();
            tempString = tempFile.getPath();
            if (excludingRoot) {
                tempString = tempString.substring(rootPathString.length(), tempString.length() - 6);
            } else {
                tempString = tempString.substring(0, tempString.length() - 6);
            }
            tempString = tempString.replace(File.separatorChar, '.');
            if (pkgPrefix != null) {
                tempString = pkgPrefix.concat(tempString);
            }
            if (tempString.startsWith(".")) {
                tempString = tempString.substring(1);
            }
            classnames.add(tempString);
        }

        return classnames.toArray(new String[0]);
    }

    /**
     * 지정한 디렉토리들에 대하여 각 디렉토리와 그 하위 디렉토리 내에 있는 파일들중에서 
     * 주어진 확장자를 가진 파일들의 목록을 가져온다.
     *
     * @param rootDir 시작 디렉토리 path 들
     * @param pattern 가져올 파일들의 파일명 패턴 목록(regexp)
     * @param subDir 하위 디렉토리 탐색 여부
     * @return 파일 목록을 담은 Set 객체, 디렉토리가 존재하지 않으면 null
     */
    public static Set<File> getFilesWithExtension(File[] rootDir, String[] ext, boolean subDir) {
    	FilenameFilter filter = new FileExtensionFilter(ext);
        TreeSet<File> set = new TreeSet<File>();
        for(int i=0;i<rootDir.length;i++) {
        	set.addAll(getAllFilesUnder(rootDir[i],filter,subDir));
        }
       
        return set;
    }
    
    /**
     * <p> 지정한 디렉토리와 그 하위 디렉토리 내에 있는 파일들중에서 주어진 확장자를 가진 파일들의 목록을 가져온다.
     *
     * @param rootDir 시작 디렉토리 path
     * @param pattern 가져올 파일들의 파일명 패턴 목록(regexp)
     * @param subDir 하위 디렉토리 탐색 여부
     * @return 파일 목록을 담은 Set 객체, 디렉토리가 존재하지 않으면 null
     */
    public static Set<File> getFilesWithExtension(File rootDir, String[] ext, boolean subDir) {
    	FilenameFilter filter = new FileExtensionFilter(ext);
    	return getAllFilesUnder(rootDir,filter,subDir);
    }
    
    /**
     * 지정한 디렉토리들에 대하여 각 디렉토리와 그 하위 디렉토리 내에 있는 파일들중에서 
     * 주어진 패턴들에 매치되는 파일들의 목록을 가져온다.
     *
     * @param rootDir 시작 디렉토리 path 들
     * @param pattern 가져올 파일들의 파일명 패턴 목록(regexp)
     * @param subDir 하위 디렉토리 탐색 여부
     * @return 파일 목록을 담은 Set 객체, 디렉토리가 존재하지 않으면 empty
     */
    public static Set<File> getFilesOfPattern(File[] rootDir, String[] pattern, boolean subDir) {
    	FilenameFilter filter = new FilenamePatternFilter(pattern);
        TreeSet<File> set = new TreeSet<File>();
        for(int i=0;i<rootDir.length;i++) {
        	set.addAll(getAllFilesUnder(rootDir[i],filter,subDir));
        }
       
        return set;
    }
    
    /**
     * 지정한 디렉토리들에 대하여 각 디렉토리와 그 하위 디렉토리 내에 있는 파일들 중에서
     * 주어진 패턴들에 매치되는 파일들의 목록을 가져온다. 
     * 파일들을 가져오는 순서는 pattern 순서대로 가져오도록 보장한다.
     * @param rootDir 시작 디렉토리 path 들
     * @param pattern 가져올 파일들의 파일명 패턴 목록(regexp)
     * @param subDir 하위 디렉토리 탐색 여부
     * @return 파일 목록을 담은 List 객체, 디렉토리가 존재하지 않으면 empty
     */
    public static List<File> getFilesOfPatternAsPatternOrder(File[] rootDir, String[] pattern, boolean subDir) {
    	List<File> list = new ArrayList<File>();
    	for(int i=0;i<pattern.length;i++) {
    		FilenameFilter filter = new FilenamePatternFilter(pattern[i]);
    		for(int j=0;j<rootDir.length;j++) {
    			list.addAll(getAllFilesUnder(rootDir[j],filter,subDir));
    		}
    	}
    	
    	return list;
    }
    
    /**
     * <p> 지정한 디렉토리와 그 하위 디렉토리 내에 있는 파일들중에서 주어진 패턴들에 매치되는 파일들의 목록을 가져온다.
     *
     * @param rootDir 시작 디렉토리 path
     * @param pattern 가져올 파일들의 파일명 패턴 목록(regexp)
     * @param subDir 하위 디렉토리 탐색 여부
     * @return 파일 목록을 담은 Set 객체, 디렉토리가 존재하지 않으면 null
     */
    public static Set<File> getFilesOfPattern(File rootDir, String[] pattern, boolean subDir) {
    	FilenameFilter filter = new FilenamePatternFilter(pattern);
    	return getAllFilesUnder(rootDir,filter,subDir);
    }
    
    /**
     * <p> 지정한 디렉토리와 그 하위 디렉토리 내에 있는 파일들의 목록을 가져온다.
     *
     * @param rootDir 시작 디렉토리 path
     * @param filter 가져올 파일들의 확장자 목록을 지정한 필터 객체
     * @return 파일 목록을 담은 Set 객체, 디렉토리가 존재하지 않으면 null
     */
    public static Set<File> getAllFilesUnder(File rootDir, FilenameFilter filter) {
    	return getAllFilesUnder(rootDir,filter,true);
    }
    
    /**
     * <p> 지정한 디렉토리와 그 하위 디렉토리 내에 있는 파일들의 목록을 가져온다.
     *
     * @param rootDir 시작 디렉토리 path
     * @param filter 가져올 파일들의 확장자 목록을 지정한 필터 객체
     * @param subDir 하위 디렉토리 탐색 여부
     * @return 파일 목록을 담은 Set 객체, 디렉토리가 존재하지 않으면 null
     */
    public static Set<File> getAllFilesUnder(File rootDir, FilenameFilter filter, boolean subDir) {
        TreeSet<File> set = new TreeSet<File>();
        if (rootDir.exists() && rootDir.isDirectory()) {
            getFilesIn(rootDir, filter, set, false, subDir);
        } else {
            if (log.isErrorEnabled()) {
                log.error("Directory does not exist. : " + rootDir.getPath());
            }
        }

        return set;
    }

    /**
	 * 주어진 경로를 받아서 .과 .. 을 제거한 경로를 반환한다.
	 * @param path
	 * @param includeLast true이면 마지막 경로를 포함시키지 않고 반환한다.
	 * @return
	 */
	public static String getCanonicalPath(String path, boolean includeLast) {
		if (StringHelper.isNull(path)) {
			return path;
		}
		Stack<String> stack = new Stack<String>();
		String[] list = path.split("/");
		for(int i=0;i<list.length;i++) {
			if (StringHelper.isNull(list[i])) {
				// skip
			} else if (".".equals(list[i])) {
				// skip
			} else if ("..".equals(list[i])) {
				if (!stack.isEmpty()) {
					stack.pop();
				}
			} else {
				stack.push(list[i]);
			}
		}
		
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<stack.size()-1;i++) {
			sb.append(stack.elementAt(i)).append("/");
		}
		if (!stack.isEmpty() && includeLast) {
			sb.append(stack.peek());
		}
		
		return sb.toString();
	}
	
	/**
	 * 주어진 경로를 받아서 .과 .. 을 제거한 경로를 반환한다.
	 * @param path
	 * @return
	 */
	public static String getCanonicalPath(String path) {
		return getCanonicalPath(path,true);
	}
	
	/**
	 * 상위 경로의 path를 반환한다. 경로에 .과 ..은 제거된 형태이다.
	 * @param path
	 * @return
	 */
	public static String getParentPath(String path) {
		return getCanonicalPath(path,false);
	}
	
    /**
     * <p> 지정한 Directory 내에 있는 파일들의 목록을 가져온다.
     *
     * @param rootFile      시작 디렉토리 path
     * @param filter        가져올 파일들의 확장자 목록을 지정한 필터 객체
     * @param set           가져온 파일들을 저장할 Set 객체
     * @param dirFlag       디렉토리도 가져오고자 하면 true로 입력
     * @param recursiveFlag 하위의 디렉토리도 계속적으로 탐색하고자 하면 true로 입력
     */
    private static void getFilesIn(File rootFile, FilenameFilter filter, Set<File> set, boolean dirFlag, boolean recursiveFlag) {

        File fileList[] = rootFile.listFiles(filter);

        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].isDirectory()) {
                if (recursiveFlag) {
                    getFilesIn(fileList[i], filter, set, dirFlag, recursiveFlag);
                }
                if (dirFlag) {
                    set.add(fileList[i]);
                }
            } else {
                set.add(fileList[i]);
            }
        }
    }

}