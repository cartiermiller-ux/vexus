package project.web.api;

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
	
	private static final Logger logger = LoggerFactory.getLogger(UploadImgController.class);
	
	public final String basePath = "/public/uploadimg";
	
	private static Properties properties = PropertiesLoaderUtils.loadProperties("config/system.properties");
	
//	@RequestMapping(value = basePath+"!execute.action")
//	public Object execute(HttpServletRequest request) {
//		String pic_str = request.getParameter("pic_str");
//		
//		ResultObject resultObject = new ResultObject();
//
//		try {
//			
//			if (StringUtils.isEmptyString(pic_str)) {
//				throw new BusinessException("文件上传失败");
//			}
//
//			if (pic_str.length() / 1024L > 30720L) {
//				resultObject.setCode("1");
//				resultObject.setMsg("图片大小不能超过30M");
//				return resultObject;
//			}
//
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//			String ymd = sdf.format(new Date());
//			
//			String fileDir = properties.getProperty("images.dir");
//			File f = new File(fileDir + "/" + ymd);
//			if ((!f.exists()) && (!f.mkdirs())) {
//				resultObject.setCode("1");
//				resultObject.setMsg("服务器错误");
//				logger.warn("文件:" + fileDir + "创建失败!");
//				return resultObject;
//			}
//
//			String imagePath = "";
//			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");
//			String yms = sdf2.format(new Date());
//
//			String imageDir = ymd + "/" + yms + new Random().nextInt(10000000) + "." + "png";
//			imagePath = fileDir + imageDir.toLowerCase().trim();
//			
//			// utf16转utf8
//			StringBuilder sb = new StringBuilder(pic_str.length());
//			for (int i = 0; i < pic_str.length(); i++) {
//				char ch = pic_str.charAt(i);
//				if (ch <= 0xFF) {
//					sb.append(ch);
//				}
//			}
//			
//			InputStream inputStream = new ByteArrayInputStream(pic_str.getBytes("UTF-16"));
//			
//			FileOutputStream outputStream = new FileOutputStream(imagePath);
//			
////			byte[] buf = new byte[1024];
////			int length = 0;
////			while ((length = inputStream.read(buf)) != -1) {
////				outputStream.write(buf, 0, length);
////			}
//			
//			OutputStreamWriter ow = new OutputStreamWriter(outputStream, "UTF-8");
//			
//			byte[] buf = new byte[1024];
//			int length = 0;
//			while ((length = inputStream.read(buf)) != -1) {
//				outputStream.write(buf, 0, length);
//			}
//			
//			OutputStreamWriter ow = new OutputStreamWriter(outputStream, "UTF-8");
//			ow.write(pic_str);
//			ow.close();
//			inputStream.close();
//
//			resultObject.setData(imageDir);
//			
//		} catch (FileNotFoundException e) {
//			resultObject.setCode("1");
//			resultObject.setMsg("服务器错误");
//			logger.error("文件上传失败", e);
//		} catch (BusinessException e) {
//			resultObject.setCode("1");
//			resultObject.setMsg(e.getMessage());
//		} catch (Throwable t) {
//			resultObject.setCode("1");
//			resultObject.setMsg("服务器错误");
//			logger.error("文件上传失败", t);
//		}
//
//		return resultObject;
//	}
//	
//	public void Utf16RedWrite(String filePath1,String filePath2) {
//		try {
//			StringBuffer buffer = new StringBuffer();
//			// --读
//			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath1), "UTF-16"));
//			// System.out.println(br.readLine());// 中a
//			while (br.ready()) {
//			buffer.append(br.readLine());
//			buffer.append("\n");
//			count++;
//		}	
//		// --正确的字符流写
//		OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream(filePath2), "UTF-8");
//			ow.write(buffer.toString());
//			ow.close();
//			br.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	@RequestMapping(value = basePath+"!execute.action")
	public Object execute(HttpServletRequest request) {
		ResultObject resultObject = new ResultObject();

		try {
			MultipartResolver resolver = new CommonsMultipartResolver(request.getSession().getServletContext());
			File file = null;
			if(resolver.isMultipart(request)) {
				MultipartHttpServletRequest multipartRequest =     (MultipartHttpServletRequest) request;
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

//			String fileExt = fileFileName.substring(fileFileName.lastIndexOf(".") + 1).toLowerCase();
//			if (!Arrays.asList(((String) extMap.get("image")).split(",")).contains(fileExt)) {
////				this.error = ("上传图片是不允许的扩展名。\n只允许" + (String) extMap.get("image") + "格式。");
////				resultObject.setCode("1");
////				resultObject.setMsg(error);
////				this.result = JsonUtils.getJsonString(resultObject);
////				out.println(this.result);
////				return null;
//			}

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
//	    if(contentType.indexOf("image")!=-1) {
//	    	prefix = "."+contentType.replace("image/", "");
//	    }else {
//	    	//不是图片则不处理
//	    	throw new BusinessException("文件上传失败");
//	    }
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
