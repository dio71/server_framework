package s2.adapi.framework.deploy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import s2.adapi.framework.vo.ValueObject;
import s2.adapi.framework.web.action.AbstractWebAction;
import s2.adapi.framework.web.action.WebActionForward;

// 배포기능을 제공한다. 프로젝트별로 작성되었던 코드를 프레임워크로 가져옴 (2025.01.24)
public class DeployWebAction extends AbstractWebAction {
	    
    private static final Logger log = LoggerFactory.getLogger(DeployWebAction.class);
    
    private String deployPasscode = "passcode";
    private String jarDirPath = null;
    
    public void setPasscode(String code) {
        deployPasscode = code;
    }

    public void setJarDir(String path) {
        jarDirPath = path;
    }

	@Override
	public WebActionForward execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		ValueObject paramVO = getRequestParamAsVO(request);
        
        ValueObject fileVO = getAttachFileAsVO(request, "file");
        
        String resultMessage = "Missing parameters or file.";

        if(paramVO.size() > 0) {
            String passcode = paramVO.getString("passcode");
            String sysCode = paramVO.getString("sys_code");
            String fileNm = paramVO.getString("file_nm");

            if (fileVO.size() > 0) {
                resultMessage = deploy(passcode, sysCode, fileNm, (byte[]) fileVO.get("data"));
            }
        }
        
        response.getWriter().printf(resultMessage);

        return null;
	}
	
	private String deploy(String passcode, String systemCode, String filename, byte[] jarBytes) {
        if (!deployPasscode.equals(passcode)) {
            return "Invalid passcode.";
        }
        
        String resultMessage = "deploy completed.";
        FileOutputStream fos = null;

        try {
            File jarDir = new File(jarDirPath, systemCode);
            
            if (!jarDir.exists() || !jarDir.isDirectory()) {
                resultMessage = "deploy failed : " + jarDir.getPath() + " is not exist or is not a directory.";
                log.error(resultMessage);
                
                return resultMessage;
            }
            
            File jarFile = new File(jarDir, filename);
        
            fos = new FileOutputStream(jarFile);
            fos.write(jarBytes);
            fos.close();
        } 
        catch (IOException e) {
            resultMessage = "deploy failed with an error : " + e.getMessage();
            log.error(resultMessage);
        }
        
        return resultMessage;
    }

}
