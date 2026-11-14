package project.user.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import kernel.http.HttpHelper;
import kernel.util.ImageUtils;
import project.Constants;
import project.syspara.SysparaService;
import project.user.QRGenerateService;

public class QRGenerateServiceImpl implements QRGenerateService {

	private SysparaService sysparaService;
	
	@Override
	public String generate(String content) {
		String image_name = "/qr/" + content + ".png";
		File file = new File(Constants.IMAGES_DIR + image_name);
		
		content = Constants.WEB_URL + "/register.html?usercode=" + content;
		if(sysparaService.find("short_url_open_button").getBoolean()) {
			content = sysparaService.find("agent_qr_url").getValue() + "/register.html?usercode=" + content;
			content=sysparaService.find("short_url_cn_button").getBoolean()?shortUrlCn(content):shortUrl(content);
		}
		
		try {
			MatrixToImageWriter.writeToPath(
					new MultiFormatWriter().encode(content,BarcodeFormat.QR_CODE,260,260,Collections.singletonMap(EncodeHintType.CHARACTER_SET, "UTF-8")),
					"png",
					file.toPath());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return image_name;
	}
	
	public String shortUrl(String longUrl) {
        return HttpHelper.sendPostHttp("https://cutt.ly/scripts/shortenUrl.php",Collections.singletonMap("url",longUrl),false);
	}
	
	public String shortUrlCn(String longUrl) {
        Map<String,Object> param = new HashMap<String,Object>();
        param.put("url",longUrl);
        param.put("key",sysparaService.find("cn_short_url_key").getValue());
        
        return HttpHelper.sendGetHttp("https://www.xyixy.com/api/",param);
	}
	
	public String generate(String content,String imgName) {
		String image_uri = "/qr/" + imgName + ".png";

		try {
			MatrixToImageWriter.writeToPath(
					new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, 260, 260, Collections.singletonMap(EncodeHintType.CHARACTER_SET,"UTF-8")),
					"png",
					new File(Constants.IMAGES_DIR + image_uri).toPath());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return image_uri;
	}
	
	@Override
	public String generate185(String content) {
		String image_name = "/qr/" + content + "2.png";
		content = Constants.WEB_URL + "/register.html?usercode=" + content;
		
		HashMap<EncodeHintType,Object> hints = new HashMap<EncodeHintType,Object>();
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		hints.put(EncodeHintType.MARGIN, 1);

		try {
			MatrixToImageWriter.writeToPath(
					new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, 185, 185, hints),
					"png",
					new File(Constants.IMAGES_DIR+image_name).toPath());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return image_name;
	}

	public void generate_poster(String image_name, String usercode) {
		String smallPath = Constants.IMAGES_DIR + image_name;
		
		for (int i = 0; i < 5; i++) {
			ImageUtils.image_usercode(
					Constants.IMAGES_DIR+"/poster/poster_"+i+"_zh-CN.png", smallPath, "png", 
					Constants.IMAGES_DIR+"/qr/"+usercode+"_poster_"+i+"_zh-CN.png");
		}
		
		for (int i = 0; i < 5; i++) {
			ImageUtils.image_usercode(
					Constants.IMAGES_DIR + "/poster/poster_" + i + "_CN.png", smallPath, "png", 
					Constants.IMAGES_DIR + "/qr/" + usercode + "_poster_" + i + "_CN.png");
		}
		
		for (int i = 0; i < 5; i++) {
			ImageUtils.image_usercode(
					Constants.IMAGES_DIR + "/poster/poster_" + i + "_en.png", smallPath, "png", 
					Constants.IMAGES_DIR + "/qr/" + usercode + "_poster_" + i + "_en.png");
		}
	}

	@Override
	public String generateWithdraw(String content, String address) {
		String image_name = "/qr/" + content + ".png";
		
		try {
			MatrixToImageWriter.writeToPath(
					new MultiFormatWriter().encode(address, BarcodeFormat.QR_CODE, 260, 260, Collections.singletonMap(EncodeHintType.CHARACTER_SET,"UTF-8")),
					"png",
					new File(Constants.IMAGES_DIR+image_name).toPath());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return image_name;
	}

	public List<Map<String, String>> generate_poster_base64(String image_name, String usercode, String img_language) {
		List<Map<String, String>> list_image = new ArrayList<Map<String, String>>();
		String smallPath = Constants.IMAGES_DIR + image_name;
		String resultPaht = "";
		for (int i = 0; i < 5; i++) {
			resultPaht = usercode + "_poster_" + i + "_" + img_language;
			Map<String, String> map_image = new HashMap<String, String>();
			map_image.put(resultPaht, ImageUtils.image_usercodeBase64(Constants.IMAGES_DIR+"/poster/poster_"+i+"_"+img_language+".png", smallPath, "png", resultPaht));
			list_image.add(map_image);
		}
		
		return list_image;

	}
	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
	}
}
