package com.tutorial.telegrambot;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataUrlUtils {
    
    private static final Pattern DATA_URL_PATTERN = Pattern.compile("^data:(.+?);base64,(.+)$");
    
    /**
     * 将Data URL保存为临时文件
     */
    public static File saveDataUrlAsTempFile(String dataUrl) throws IOException {
        Matcher matcher = DATA_URL_PATTERN.matcher(dataUrl);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid data URL format");
        }
        
        String mimeType = matcher.group(1);
        String base64Data = matcher.group(2);
        
        // 解码Base64数据
        byte[] imageData = Base64.getDecoder().decode(base64Data);
        
        // 确定文件扩展名
        String extension = getFileExtension(mimeType);
        
        // 创建临时文件
        File tempFile = File.createTempFile("temp_image_", extension);
        tempFile.deleteOnExit(); // 程序退出时自动删除
        
        // 写入文件
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(imageData);
        }
        
        return tempFile;
    }
    
    /**
     * 根据MIME类型获取文件扩展名
     */
    private static String getFileExtension(String mimeType) {
        switch (mimeType) {
            case "image/jpeg":
            case "image/jpg":
                return ".jpg";
            case "image/png":
                return ".png";
            case "image/gif":
                return ".gif";
            case "image/webp":
                return ".webp";
            case "image/bmp":
                return ".bmp";
            default:
                return ".jpg"; // 默认使用jpg
        }
    }
    
    /**
     * 将视频Data URL保存为临时文件
     */
    public static File saveVideoDataUrlAsTempFile(String dataUrl) throws IOException {
        // 验证并清理Data URL格式
        String cleanedDataUrl = cleanDataUrl(dataUrl);
        
        Matcher matcher = DATA_URL_PATTERN.matcher(cleanedDataUrl);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid data URL format");
        }

        String mimeType = matcher.group(1);
        String base64Data = matcher.group(2);
        
        // 验证Base64数据格式
        if (!isValidBase64(base64Data)) {
            throw new IllegalArgumentException("Invalid Base64 data");
        }

        // 解码Base64数据
        byte[] videoData = Base64.getDecoder().decode(base64Data);

        // 确定文件扩展名
        String extension = getVideoFileExtension(mimeType);

        // 创建临时文件
        File tempFile = File.createTempFile("temp_video_", extension);
        tempFile.deleteOnExit(); // 程序退出时自动删除

        // 写入文件
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(videoData);
        }

        return tempFile;
    }
    
    /**
     * 清理Data URL，移除可能的无效字符
     */
    private static String cleanDataUrl(String dataUrl) {
        if (dataUrl == null) {
            throw new IllegalArgumentException("Data URL cannot be null");
        }
        
        // 移除可能的前后空白字符
        String cleaned = dataUrl.trim();
        
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Data URL cannot be empty");
        }
        
        // 确保格式正确
        if (!cleaned.startsWith("data:")) {
            throw new IllegalArgumentException("Data URL must start with 'data:'");
        }
        
        // 查找;base64,的位置
        int base64Index = cleaned.indexOf(";base64,");
        if (base64Index == -1) {
            throw new IllegalArgumentException("Data URL must contain ';base64,'");
        }
        
        // 提取MIME类型部分和Base64数据部分
        String mimePart = cleaned.substring(0, base64Index + 7); // +7 for ";base64"
        String base64Part = cleaned.substring(base64Index + 8); // +8 for ";base64,"
        
        // 验证MIME类型部分不包含非法字符
        if (!mimePart.matches("^data:[a-zA-Z0-9\\-+/]+/[a-zA-Z0-9\\-+/]+$")) {
            throw new IllegalArgumentException("Invalid MIME type in Data URL");
        }
        
        // 清理Base64数据部分
        String cleanedBase64 = base64Part.replaceAll("[^a-zA-Z0-9+/=]", "");
        
        return mimePart + cleanedBase64;
    }
    
    /**
     * 验证Base64字符串格式
     */
    private static boolean isValidBase64(String base64) {
        try {
            Base64.getDecoder().decode(base64);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 根据MIME类型获取视频文件扩展名
     */
    private static String getVideoFileExtension(String mimeType) {
        switch (mimeType) {
            case "video/mp4":
                return ".mp4";
            case "video/avi":
                return ".avi";
            case "video/mov":
                return ".mov";
            case "video/wmv":
                return ".wmv";
            case "video/flv":
                return ".flv";
            case "video/webm":
                return ".webm";
            case "video/mpeg":
                return ".mpeg";
            case "video/quicktime":
                return ".mov";
            default:
                return ".mp4"; // 默认使用mp4
        }
    }
    
    /**
     * 将Data URL转换为字节数组
     */
    public static byte[] dataUrlToBytes(String dataUrl) {
        Matcher matcher = DATA_URL_PATTERN.matcher(dataUrl);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid data URL format");
        }
        
        String base64Data = matcher.group(2);
        
        // 解码Base64数据
        return Base64.getDecoder().decode(base64Data);
    }
    
}