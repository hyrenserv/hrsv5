package hrds.commons.utils.ocr;

import hrds.commons.utils.CommonVariables;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

public class PictureTextExtract extends PictureServer implements PictureTextInterface {

	private static final Log logger = LogFactory.getLog(PictureTextExtract.class);

	public PictureTextExtract() {
		super(CommonVariables.OCR_RPC_ADDRESS);
	}

	public String extractText(String filePath) {

		logger.info("RPC 开始提取图片的路径为 : " + filePath);
		String question = null;
		try {
			Image image = ImageIO.read(FileUtils.getFile(filePath));
			if (image != null) {
				OrcPictureText pic = new OrcPictureText(filePath);
				//1 指定调用的方法chinese_ocr
				question = byteToStr(pic.readPicture());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return question;
	}

	public String byteToStr(byte[] bytes) {

		String question = null;
		try {
			Object[] params = new Object[]{bytes};
			question = (String) client.execute("chinese_ocr", params);
		} catch (XmlRpcException e) {
			e.printStackTrace();
		}
		return question;
	}

	public static void main(String[] args) {

		PictureTextExtract server = new PictureTextExtract();
		long start1 = System.currentTimeMillis();
		System.out.println(server.extractText("E:\\aa"));
		long end1 = System.currentTimeMillis();
		System.out.println("1111111==>" + (end1 - start1) / 1000);

	}
}
