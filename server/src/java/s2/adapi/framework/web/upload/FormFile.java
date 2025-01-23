package s2.adapi.framework.web.upload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.FileItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormFile {
    protected static Logger log = LoggerFactory.getLogger(FormFile.class);

    private FileItem fileItem;

    /**
     * <p>
     * FormFile 클래스의 생성자로써 FileItem 객체를 저장한다.
     * </p>
     *
     * @param fileItem 파일 업로드 리퀘스트로부터 얻어지는 FileItem 객체
     * @param key FileItem 객체에 대한 키 값
     */
    public FormFile(FileItem fileItem)
    {
        this.fileItem = fileItem;
    }

    /**
     * <p>
     * 업로드 파일에 대한 컨텐트 타입을 리턴한다.
     * </p>
     *
     * @return 업로드 파일의 컨텐트 타입에 대한 문자열
     */
    public String getContentType() {
        return fileItem.getContentType();
    }

    /**
     * <p>
     * 파일의 사이즈를 리턴한다.
     * </p>
     *
     * @return 파일 사이즈
     */
    public int getFileSize() {
        return (int) fileItem.getSize();
    }

    /**
     * <p>
     * 파일의 이름을 리턴한다.
     * </p>
     * @return  파일명
     */
    public String getFileName() {
        return getBaseFileName(fileItem.getName());
    }

    /**
     * <p>
     * 파일의 내용을 byte[] 형태로 리턴한다.
     * </p>
     *
     * @return 파일의 내용(byte[])
     * @throws FileNotFoundException 해당 파일을 찾지 못하는 경우
     * @throws IOException 해당 파일로 부터 내용을 읽지 못하는 경우
     */
    public byte[] getFileData()
            throws FileNotFoundException, IOException {
        return fileItem.get();
    }

    /**
     * <p>
     * 파일의 내용을 InputStream 형태로 리턴한다.
     * </p>
     *
     * @return 파일의 내용(InputStream)
     * @throws FileNotFoundException 해당 파일을 찾지 못하는 경우
     * @throws IOException 해당 파일로 부터 내용을 읽지 못하는 경우
     */
    public InputStream getInputStream()
            throws FileNotFoundException, IOException {
        return fileItem.getInputStream();
    }

    /**
     * <p>
     * 파일을 삭제한다.
     * </p>
     */
    public void destroy() {
        fileItem.delete();
    }

    /**
     * <p>
     * 리퀘스트로부터 업로드 파일을 식별하기 위한 키를 리턴한다.
     * </p>
     * @return 업로드 파일에 대한 키
     */
    public String getFieldName(){
        return fileItem.getFieldName();
    }

    /**
     * <p>
     * 파일 명을 리턴한다.
     * </p>
     *
     * @return  파일명
     */
    public String toString()
    {
        return fileItem.getName();
    }
    
    /**
     * 지정한 파일객체로 파일 내용을 저장한다.
     * @param file
     * @throws Exception
     */
    public void write(File file) throws Exception {
    	fileItem.write(file);
    }
    
    /**
     * <p>
     * 경로명을 제외한 파일명을 리턴한다.
     * </p>
     * @param filePath  파일의 전체 경로명
     * @return 경로명을 제외한 파일명
     */
    protected String getBaseFileName(String filePath) {
    	// TODO 구현 개선하자.
        String fileName = (new File(filePath)).getName();
        int colonIndex = fileName.indexOf(":");
        if (colonIndex == -1) {
            colonIndex = fileName.indexOf("\\\\");
        }
        int backslashIndex = fileName.lastIndexOf("\\");
        if (colonIndex > -1 && backslashIndex > -1) {
            fileName = fileName.substring(backslashIndex + 1);
        }
        return fileName;
    }
}
