package s2.adapi.framework.web.upload;

import java.io.IOException;

import org.apache.commons.fileupload2.core.FileItemInput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 2025.01.23
// Java EE -> Jakarta EE 로 migration 하기 위하여 commons fileupload -> fileupload2 로 재구현함
// 구현 방식이 바뀌어 대부분 수정함
public class FormFile {
    protected static Logger log = LoggerFactory.getLogger(FormFile.class);

    private String fieldName = null;
    private String contentType = null;
    private String fileName = null;
    private byte[] fileData = null;

    /**
     * <p>
     * FormFile 클래스의 생성자로써 FileItem 객체를 저장한다.
     * </p>
     *
     * @param fileItem 파일 업로드 리퀘스트로부터 얻어지는 FileItem 객체
     * @param key FileItem 객체에 대한 키 값
     */
    public FormFile(FileItemInput fileItem, int sizeLimit) throws IOException
    {
        this.fieldName = fileItem.getFieldName();
        this.contentType = fileItem.getContentType();
        this.fileName = fileItem.getName();
        this.fileData = fileItem.getInputStream().readNBytes(sizeLimit);
    }

    /**
     * <p>
     * 업로드 파일에 대한 컨텐트 타입을 리턴한다.
     * </p>
     *
     * @return 업로드 파일의 컨텐트 타입에 대한 문자열
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * <p>
     * 파일의 사이즈를 리턴한다.
     * </p>
     *
     * @return 파일 사이즈
     */
    public int getFileSize() {
        return fileData.length;
    }

    /**
     * <p>
     * 파일의 이름을 리턴한다.
     * </p>
     * @return  파일명
     */
    public String getFileName() {
        return getBaseFileName(fileName);
    }

    /**
     * <p>
     * 파일의 내용을 byte[] 형태로 리턴한다.
     * </p>
     *
     * @return 파일의 내용(byte[])
     */
    public byte[] getFileData() {
        return fileData;
    }

    /**
     * <p>
     * 리퀘스트로부터 업로드 파일을 식별하기 위한 키를 리턴한다.
     * </p>
     * @return 업로드 파일에 대한 키
     */
    public String getFieldName(){
        return fieldName;
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
        return fieldName + "," + fileName + "," + contentType + "," + fileData.length;
    }
    
    /**
     * <p>
     * 경로명을 제외한 파일명을 리턴한다.
     * </p>
     * @param filePath  파일의 전체 경로명
     * @return 경로명을 제외한 파일명
     */
    private String getBaseFileName(String filePath) {

        String[] split = filePath.split("/");
        return split[split.length-1];

        // String fileName = (new File(filePath)).getName();
        // int colonIndex = fileName.indexOf(":");
        // if (colonIndex == -1) {
        //     colonIndex = fileName.indexOf("\\\\");
        // }
        // int backslashIndex = fileName.lastIndexOf("\\");
        // if (colonIndex > -1 && backslashIndex > -1) {
        //     fileName = fileName.substring(backslashIndex + 1);
        // }
        // return fileName;
    }

    
}
