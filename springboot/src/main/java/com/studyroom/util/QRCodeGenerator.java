package com.studyroom.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class QRCodeGenerator {

    /**
     * 生成二维码
     * @param content 二维码内容
     * @param width 宽度
     * @param height 高度
     * @return 二维码图片的Base64字符串
     */
    public String generateQRCode(String content, int width, int height) throws Exception {
        // 设置二维码参数
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 1);

        // 生成二维码
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);

        // 创建BufferedImage
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
            }
        }

        // 添加Logo（可选）
        // image = addLogo(image, logoPath);

        // 转换为Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        byte[] imageBytes = baos.toByteArray();

        return "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(imageBytes);
    }

    /**
     * 生成带Logo的二维码
     * @param content 二维码内容
     * @param width 宽度
     * @param height 高度
     * @param logoText Logo文字
     * @return 二维码图片的Base64字符串
     */
    public String generateQRCodeWithLogo(String content, int width, int height, String logoText) throws Exception {
        // 生成基础二维码
        String base64 = generateQRCode(content, width, height);
        
        // 解码为BufferedImage
        BufferedImage image = base64ToImage(base64.substring(23)); // 去掉data:image/png;base64,前缀
        
        // 添加文字Logo
        Graphics2D g2d = image.createGraphics();
        
        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 设置字体
        Font font = new Font("Arial", Font.BOLD, width / 10);
        g2d.setFont(font);
        g2d.setColor(Color.BLUE);
        
        // 计算文字位置（居中）
        FontMetrics metrics = g2d.getFontMetrics(font);
        int textWidth = metrics.stringWidth(logoText);
        int textHeight = metrics.getHeight();
        int x = (width - textWidth) / 2;
        int y = (height - textHeight) / 2 + metrics.getAscent();
        
        // 绘制文字背景
        g2d.setColor(Color.WHITE);
        g2d.fillRect(x - 5, y - metrics.getAscent() - 5, textWidth + 10, textHeight + 10);
        
        // 绘制文字
        g2d.setColor(Color.BLUE);
        g2d.drawString(logoText, x, y);
        
        g2d.dispose();
        
        // 转换为Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        byte[] imageBytes = baos.toByteArray();
        
        return "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(imageBytes);
    }

    /**
     * Base64字符串转BufferedImage
     */
    private BufferedImage base64ToImage(String base64) throws IOException {
        byte[] imageBytes = java.util.Base64.getDecoder().decode(base64);
        return ImageIO.read(new java.io.ByteArrayInputStream(imageBytes));
    }

    /**
     * 生成支付二维码（支付宝样式）
     */
    public String generateAlipayQRCode(String paymentUrl) throws Exception {
        return generateQRCodeWithLogo(paymentUrl, 300, 300, "支付宝");
    }

    /**
     * 生成支付二维码（微信样式）
     */
    public String generateWechatQRCode(String paymentUrl) throws Exception {
        return generateQRCodeWithLogo(paymentUrl, 300, 300, "微信支付");
    }
}
