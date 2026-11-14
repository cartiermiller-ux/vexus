package project.web.admin.controller.upload;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import kernel.exception.BusinessException;
import kernel.util.PropertiesLoaderUtils;
import kernel.web.ResultObject;

@RestController
@CrossOrigin
public class UploadImgController {
	
	private Logger logger = LoggerFactory.getLogger(UploadImgController.class);
	
	private static Properties properties = PropertiesLoaderUtils.loadProperties("config/system.properties");
	
	@RequestMapping(value = "normal/uploadimg!execute.action")
	public Object execute(HttpServletRequest request) {
		ResultObject resultObject = new ResultObject();

		try {
			MultipartResolver resolver = new CommonsMultipartResolver(request.getSession().getServletContext());
			File file = null;
			if(resolver.isMultipart(request)) {
				MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
				MultipartFile multipartFile = multipartRequest.getFile("file");
			    file = multipartFileToFile(multipartFile);
			}else {
				throw new BusinessException("文件上传失败");
			}
			String fileFileName = file.getName();

			HashMap extMap = new HashMap();
			extMap.put("image", "jpg,png");
			if (file.length() / 1024L > 10240L) {

				resultObject.setCode("1");
				resultObject.setMsg("图片大小不能超过10M");
				return resultObject;
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String ymd = sdf.format(new Date());

			String fileDir = properties.getProperty("images.dir");
			File f = new File(fileDir + "/" + ymd);
			if ((!f.exists()) && (!f.mkdirs())) {
				resultObject.setCode("1");
				resultObject.setMsg("服务器错误");
				logger.warn("文件:" + fileDir + "创建失败!");
				return resultObject;
			}

			String imagePath = "";
			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");
			String yms = sdf2.format(new Date());

			String imageDir = ymd + "/" + yms + new Random().nextInt(10000000) + "." + "png";
			imagePath = fileDir + imageDir.toLowerCase().trim();

			FileInputStream in = new FileInputStream(file);

			FileOutputStream outputStream = new FileOutputStream(imagePath);

			BufferedInputStream inputStream = new BufferedInputStream(in);
			byte[] buf = new byte[1024];
			int length = 0;
			while ((length = inputStream.read(buf)) != -1) {
				outputStream.write(buf, 0, length);
			}
			resultObject.setData(imageDir);
		} catch (FileNotFoundException e) {
			resultObject.setCode("1");
			resultObject.setMsg("服务器错误");
			logger.error("文件上传失败", e);
			return resultObject;
		} catch (Exception e) {

			resultObject.setCode("1");
			resultObject.setMsg("服务器错误");
			logger.error("文件上传失败", e);
			return resultObject;

		}

		return resultObject;
	}
	
	public static File multipartFileToFile(MultipartFile multiFile) {
        // 获取文件名
        String fileName = multiFile.getOriginalFilename();
        // 获取文件后缀
//        String prefix = fileName.substring(fileName.lastIndexOf("."));
        String prefix = "";
	    String contentType = multiFile.getContentType();
	    if(contentType.indexOf("image")!=-1) {
	    	prefix = "."+contentType.replace("image/", "");
	    }else {
	    	//不是图片则不处理
	    	throw new BusinessException("文件上传失败");
	    }
        // 若须要防止生成的临时文件重复,能够在文件名后添加随机码
 
        try {
            File file = File.createTempFile(fileName, prefix);
            multiFile.transferTo(file);
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
