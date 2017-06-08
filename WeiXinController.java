package com.mirun.club.controllers;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.annotation.rest.Get;
import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;

import com.mirun.club.dao.CountDAO;
import com.mirun.club.manager.impl.KeyWordManager;
import com.mirun.club.utils.DateUtils;

/**
 * @Description 类描述
 * @author hugh
 * @date
 */
@Path("wx")
public class WeiXinController {

	@Autowired
	public CountDAO countDAO;

	@Get("count")
	public void count(Invocation inv) {
		inv.getResponse().addHeader("Access-Control-Allow-Origin", "*");
		countDAO.insert(new Date(System.currentTimeMillis()));
	}

	/**
	 * guest/createorlogin 接受设备信息
	 * 
	 * @param inv
	 * @return
	 */
	@Get("config")
	public Object guest(Invocation inv) {
		inv.getResponse().addHeader("Access-Control-Allow-Origin", "*");
		String appId = "wx907905678981093e";
		String signature = "";
		String accessToken = this.getAccessToken();
		// 2、获取Ticket
		String jsapi_ticket = getTicket(accessToken);

		// 3、时间戳和随机字符串
		String noncestr = UUID.randomUUID().toString().replace("-", "")
				.substring(0, 16);// 随机字符串
		String timestamp = String.valueOf(System.currentTimeMillis() / 1000);// 时间戳

		System.out.println("accessToken:" + accessToken + "\njsapi_ticket:"
				+ jsapi_ticket + "\n时间戳：" + timestamp + "\n随机字符串：" + noncestr);

		// 4、获取url
		String url = inv.getRequest().getParameter("url");
		/*
		 * 根据JSSDK上面的规则进行计算，这里比较简单，我就手动写啦 String[] ArrTmp =
		 * {"jsapi_ticket","timestamp","nonce","url"}; Arrays.sort(ArrTmp);
		 * StringBuffer sf = new StringBuffer(); for(int
		 * i=0;i<ArrTmp.length;i++){ sf.append(ArrTmp[i]); }
		 */

		// 5、将参数排序并拼接字符串
		String str = "jsapi_ticket=" + jsapi_ticket + "&noncestr=" + noncestr
				+ "&timestamp=" + timestamp + "&url=" + url;

		// 6、将字符串进行sha1加密
		signature = SHA1(str);
		System.out.println("参数：" + str + "\n签名：" + signature);
		String result = appId + "," + noncestr + "," + timestamp + ","
				+ signature;
		return "@" + result;
	}

	private static long tokentime = 0;
	private static String access_token = "";

	private static long ticketTime = 0;
	private static String ticket = "";

	public String getAccessToken() {
		long now = System.currentTimeMillis();
		long cha = now - tokentime;
		long expire = 6000 * 1000;
		if (cha < expire && !"".equals(access_token)) {
			System.out.println("缓存命中token:" + access_token + "|剩余时间:" + cha);
			return access_token;
		}
		String grant_type = "client_credential";// 获取access_token填写client_credential
		String AppId = "wx907905678981093e";// 第三方用户唯一凭证
		String secret = "882725181580b96b3165ed827bf22fca";// 第三方用户唯一凭证密钥，
															// 即appsecret
		// 这个url链接地址和参数皆不能变
		String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type="
				+ grant_type + "&appid=" + AppId + "&secret=" + secret;

		try {
			URL urlGet = new URL(url);
			HttpURLConnection http = (HttpURLConnection) urlGet
					.openConnection();
			http.setRequestMethod("GET"); // 必须是get方式请求
			http.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			http.setDoOutput(true);
			http.setDoInput(true);
			System.setProperty("sun.net.client.defaultConnectTimeout", "30000");// 连接超时30秒
			System.setProperty("sun.net.client.defaultReadTimeout", "30000"); // 读取超时30秒
			http.connect();
			InputStream is = http.getInputStream();
			int size = is.available();
			byte[] jsonBytes = new byte[size];
			is.read(jsonBytes);
			String message = new String(jsonBytes, "UTF-8");
			JSONObject demoJson = JSONObject.fromObject(message);
			System.out.println("JSON字符串：" + demoJson);
			access_token = demoJson.getString("access_token");
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
			tokentime = 0;
			access_token = "";
		}

		tokentime = System.currentTimeMillis();
		return access_token;
	}

	public String getTicket(String access_token) {
		long now = System.currentTimeMillis();
		long cha = now - ticketTime;
		long expire = 6000 * 1000;
		if (cha < expire && !"".equals(ticket)) {
			System.out.println("缓存命中ticket:" + ticket + "|剩余时间:" + cha);
			return ticket;
		}
		String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token="
				+ access_token + "&type=jsapi";// 这个url链接和参数不能变
		try {
			URL urlGet = new URL(url);
			HttpURLConnection http = (HttpURLConnection) urlGet
					.openConnection();
			http.setRequestMethod("GET"); // 必须是get方式请求
			http.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			http.setDoOutput(true);
			http.setDoInput(true);
			System.setProperty("sun.net.client.defaultConnectTimeout", "30000");// 连接超时30秒
			System.setProperty("sun.net.client.defaultReadTimeout", "30000"); // 读取超时30秒
			http.connect();
			InputStream is = http.getInputStream();
			int size = is.available();
			byte[] jsonBytes = new byte[size];
			is.read(jsonBytes);
			String message = new String(jsonBytes, "UTF-8");
			JSONObject demoJson = JSONObject.fromObject(message);
			System.out.println("JSON字符串：" + demoJson);
			ticket = demoJson.getString("ticket");
			ticketTime = System.currentTimeMillis();
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
			tokentime = 0;
			access_token = "";
			ticketTime = 0;
			ticket = "";
		}
		return ticket;
	}

	public static String SHA1(String decript) {
		try {
			MessageDigest digest = java.security.MessageDigest
					.getInstance("SHA-1");
			digest.update(decript.getBytes());
			byte messageDigest[] = digest.digest();
			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			// 字节数组转换为 十六进制 数
			for (int i = 0; i < messageDigest.length; i++) {
				String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
				if (shaHex.length() < 2) {
					hexString.append(0);
				}
				hexString.append(shaHex);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 图片添加水印
	 * 
	 * @param srcImgPath
	 *            需要添加水印的图片的路径
	 * @param outImgPath
	 *            添加水印后图片输出路径
	 * @param markContentColor
	 *            水印文字的颜色
	 * @param waterMarkContent
	 *            水印的文字
	 */
	public static void mark(String srcImgPath, String outImgPath,Content[] content) {
		FileOutputStream outImgStream = null;
		try {
			// 读取原图片信息
			File srcImgFile = new File(srcImgPath);
			Image srcImg = ImageIO.read(srcImgFile);
			int srcImgWidth = srcImg.getWidth(null);
			int srcImgHeight = srcImg.getHeight(null);
			// 加水印
			BufferedImage bufImg = new BufferedImage(srcImgWidth, srcImgHeight,
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g = bufImg.createGraphics();			
			g.drawImage(srcImg, 0, 0, srcImgWidth, srcImgHeight, null);
			// Font font = new Font("Courier New", Font.PLAIN, 12);
			   g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g.rotate(Math.toRadians(2.5));
			for (int i = 0; i < content.length; i++) {
				Content c = content[i];
				if(c.isImage){
					//划伤图片
					ImageIcon imgIcon = new ImageIcon(c.name);
					Image image = imgIcon.getImage();
					g.drawImage(image, c.x,c.y,null);
				}else{
					g.setFont(c.font);
					g.setColor(c.color);
					g.drawString(c.name, c.x, c.y);
				}
				
			}
			
			g.dispose();
			// 输出图片
			outImgStream = new FileOutputStream(outImgPath);
			ImageIO.write(bufImg, "jpg", outImgStream);
			outImgStream.flush();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				outImgStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Get("gen")
	public String gen(Invocation inv) {
		String dPath = "";
		String imageName= "";
		try {
			inv.getResponse().addHeader("Access-Control-Allow-Origin", "*");
			String name = inv.getRequest().getParameter("n");
			String zhunkaozheng = inv.getRequest().getParameter("z");

			String yuwen = inv.getRequest().getParameter("yuwen");
			String shuxue = inv.getRequest().getParameter("shuxue");
			String yingyu = inv.getRequest().getParameter("yingyu");
			String wenzong = inv.getRequest().getParameter("wenzong");
			String lizong = inv.getRequest().getParameter("lizong");
			String jiafen = inv.getRequest().getParameter("jiafen");
			
			int yuwenCJ = ("13".equals(yuwen))? 150 : 0;
			int shuxueCJ = ("21".equals(shuxue))? 150 : 0;
			int yingyuCJ = ("33".equals(yingyu))? 150 : 0;
			int wenzongCJ = ("42".equals(wenzong))? 100 : 0;
			int lizongCJ = ("53".equals(lizong))? 100 : 0;
			int jiafenCJ = ("64".equals(jiafen))? 100 : 0;
			
			int n = (yuwenCJ+shuxueCJ+yingyuCJ+wenzongCJ+lizongCJ+jiafenCJ);


			String desc = "/data/www/taida/gaokao/";//图片路径
			if (n >= 750) {
				desc += "5.png";
			} else if (n >= 400 && n <= 650) {
				desc += "1.png";
			} else if (n >= 100 && n <= 350) {
				desc += "2.png";
			} else {
				desc += "3.png";
			}

			Font font32 = new Font("黑体", Font.PLAIN, 32);
			
			Font font39cu = new Font("宋体", Font.BOLD, 39);
			Font font39 = new Font("宋体", Font.PLAIN, 39);
			
			Font font42 = new Font("宋体", Font.PLAIN, 41);
			
			String date = DateUtils.getDayFormat(new Date(System.currentTimeMillis()));
			Content riqi = new Content(date, 791,237,font32,toColorFromString("#605142"),false);
			Content xingming = new Content(name,380,338,font39cu,toColorFromString("#726352"),false);
			
			Content zkz = new Content(zhunkaozheng, 372,420,font39,toColorFromString("#726352"),false);
			
			Content nianfen = new Content("2017年6月", 361,520,font39,toColorFromString("#726352"),false);
			
			Content yw = new Content(yuwenCJ+"",367,859,font42,toColorFromString("#e96030"),false);
			
			Content wz = new Content(wenzongCJ+"", 781,850,font42,toColorFromString("#e96030"),false);
			
			Content sx = new Content(shuxueCJ+"", 365,765,font42,toColorFromString("#e96030"),false);
			
			Content lz = new Content(lizongCJ+"", 783,760,font42,toColorFromString("#e96030"),false);
			
			Content yy = new Content(yingyuCJ+"",  359,957,font42,toColorFromString("#e96030"),false);
			
			Content jf = new Content(jiafenCJ+"",  775,950,font42,toColorFromString("#e96030"),false);
			
			Content zcj = new Content(n+"", 401,1046,font42,toColorFromString("#e96030"),false);
			
			Content zb = new Content(desc, 474,1190,null,null,true);
			
			Content[] c = {xingming,zkz,yw,wz,sx,lz,yy,jf,zcj,zb,riqi,nianfen};
			imageName = (zhunkaozheng+System.currentTimeMillis())+".jpg";
			dPath = "/data/www/taida/gaokao/cj/"+imageName;
			WeiXinController.mark("/data/www/taida/gaokao/cjd.jpg",
					dPath, c);
			System.out.println(new Date(System.currentTimeMillis())+"|"+dPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "@"+imageName;  
	}

	@Autowired
	public KeyWordManager keyWordManager;

	@Get("filter")
	public String filter(Invocation inv) {
		inv.getResponse().addHeader("Access-Control-Allow-Origin", "*");
		String name = inv.getRequest().getParameter("n");
		boolean has = keyWordManager.isHasFilterWord(name);
		countDAO.insert(new Date(System.currentTimeMillis()));
		if (has) {
			return "@1";
		} else {
			return "@0";
		}
	}
	
	@Get("test")
	public String test(Invocation inv) {
		return null;
	}

	public static void main(String[] args) {
		String dPath = "";
		String imageName= "";
		try {
//			inv.getResponse().addHeader("Access-Control-Allow-Origin", "*");
			String name = "谁人能比";//inv.getRequest().getParameter("n");
			String zhunkaozheng = "66612121212";//inv.getRequest().getParameter("z");

			String yuwen = "13";//inv.getRequest().getParameter("yuwen");
			String shuxue = "21";//inv.getRequest().getParameter("shuxue");
			String yingyu = "33";//inv.getRequest().getParameter("yingyu");
			String wenzong ="42";// inv.getRequest().getParameter("wenzong");
			String lizong = "53";//inv.getRequest().getParameter("lizong");
			String jiafen = "64";//inv.getRequest().getParameter("jiafen");
			
			int yuwenCJ = ("13".equals(yuwen))? 150 : 0;
			int shuxueCJ = ("21".equals(shuxue))? 150 : 0;
			int yingyuCJ = ("33".equals(yingyu))? 150 : 0;
			int wenzongCJ = ("42".equals(wenzong))? 100 : 0;
			int lizongCJ = ("53".equals(lizong))? 100 : 0;
			int jiafenCJ = ("64".equals(jiafen))? 100 : 0;
			
			int n = (yuwenCJ+shuxueCJ+yingyuCJ+wenzongCJ+lizongCJ+jiafenCJ);


			String desc = "/data/";//图片路径
			if (n >= 750) {
				desc += "5.png";
			} else if (n >= 400 && n <= 650) {
				desc += "1.png";
			} else if (n >= 100 && n <= 350) {
				desc += "2.png";
			} else {
				desc += "3.png";
			}

			Font font32 = new Font("黑体", Font.PLAIN, 32);
			
			Font font39cu = new Font("宋体", Font.BOLD, 39);
			Font font39 = new Font("宋体", Font.PLAIN, 39);
			
			Font font42 = new Font("宋体", Font.PLAIN, 41);
			
			
			Content riqi = new Content(DateUtils.getDayFormat(new Date(System.currentTimeMillis())), 791,237,font32,toColorFromString("#605142"),false);
			Content xingming = new Content(name,380,338,font39cu,toColorFromString("#726352"),false);
			
			Content zkz = new Content(zhunkaozheng, 372,420,font39,toColorFromString("#726352"),false);
			
			Content nianfen = new Content("2017年6月", 361,520,font39,toColorFromString("#726352"),false);
			
			Content yw = new Content(yuwenCJ+"",367,859,font42,toColorFromString("#e96030"),false);
			
			Content wz = new Content(wenzongCJ+"", 781,850,font42,toColorFromString("#e96030"),false);
			
			Content sx = new Content(shuxueCJ+"", 365,765,font42,toColorFromString("#e96030"),false);
			
			Content lz = new Content(lizongCJ+"", 783,760,font42,toColorFromString("#e96030"),false);
			
			Content yy = new Content(yingyuCJ+"",  359,957,font42,toColorFromString("#e96030"),false);
			
			Content jf = new Content(jiafenCJ+"",  775,950,font42,toColorFromString("#e96030"),false);
			
			Content zcj = new Content(n+"", 401,1046,font42,toColorFromString("#e96030"),false);
			
			Content zb = new Content(desc, 474,1190,null,null,true);
			
			Content[] c = {xingming,zkz,yw,wz,sx,lz,yy,jf,zcj,zb,riqi,nianfen};
			imageName = (zhunkaozheng+System.currentTimeMillis())+".jpg";
			dPath = "/data/"+imageName;
			WeiXinController.mark("/data/cjd.jpg",
					dPath, c);
			System.out.println(new Date(System.currentTimeMillis())+"|"+dPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
		
	public static Color toColorFromString(String colorStr){  
//        colorStr = colorStr.substring(4);  
//        Color color =  new Color(Integer.parseInt(colorStr, 16)) ;  
        //java.awt.Color[r=0,g=0,b=255]  
		Color color = Color.decode(colorStr);
        return color;  
    } 
}

class Content{
	public Content(String name,int x,int y,Font font,Color color,boolean isImage){
		this.name = name;
		this.x = x;
		this.y = y;
		this.font = font;
		this.color = color;
		this.isImage = isImage;
	}
	public String name;
	public int x;
	public int y;
	public Font font;
	public Color color;
	public boolean isImage;
}
