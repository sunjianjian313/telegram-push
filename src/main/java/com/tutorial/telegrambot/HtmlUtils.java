package com.tutorial.telegrambot;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class HtmlUtils {
    
    /**
     * 清理HTML内容，保留基本格式
     */
    public static String cleanHtml(String html) {
        // 允许基本的文本格式化标签
        Safelist safelist = new Safelist()
            .addTags("b", "strong", "i", "em", "u", "br", "p", "a")
            .addAttributes("a", "href");
        
        return Jsoup.clean(html, safelist);
    }
    
    /**
     * 将HTML转换为适合Telegram的格式
     */
    public static String convertForTelegram(String html) {
        // 替换HTML标签为Telegram支持的格式
        String converted = html
            .replaceAll("<b>|<strong>", "*")      // 粗体标记
            .replaceAll("</b>|</strong>", "*")
            .replaceAll("<i>|<em>", "_")          // 斜体标记
            .replaceAll("</i>|</em>", "_")
            .replaceAll("<u>", "")                // 下划线（Telegram不直接支持）
            .replaceAll("</u>", "")
            .replaceAll("<br/?>", "\n")          // 换行
            .replaceAll("<p>", "\n")              // 段落
            .replaceAll("</p>", "")
            .replaceAll("&nbsp;", " ")            // 空格
            .replaceAll("&amp;", "&")              // & 符号
            .replaceAll("&lt;", "<")               // < 符号
            .replaceAll("&gt;", ">");              // > 符号
        
        // 移除img标签但保留图片占位符，其他标签则完全移除
        converted = converted.replaceAll("<img[^>]*>", "[图片]");
        converted = converted.replaceAll("<[^>]*>", "");
        
        return converted.trim();
    }
    
    /**
     * 提取HTML中的图片URL
     */
    public static List<String> extractImageUrls(String html) {
        // 更宽容的正则表达式，处理可能的属性顺序变化和空白符
        Pattern imgPattern = Pattern.compile("<img[^>]*?\\s+src\\s*=\\s*[\"']([^\"']*)[\"'][^>]*>", Pattern.CASE_INSENSITIVE);
        Matcher imgMatcher = imgPattern.matcher(html);
        List<String> imageUrls = new ArrayList<>();
            
        while (imgMatcher.find()) {
            String url = imgMatcher.group(1);
            if (url != null && !url.isEmpty()) {
                imageUrls.add(url);
            }
        }
            
        return imageUrls;
    }
    
    /**
     * 提取HTML中的视频URL
     */
    public static List<String> extractVideoUrls(String html) {
        List<String> videoUrls = new ArrayList<>();
        
        // 匹配video标签的src属性，更宽容地处理标签内的属性顺序
        String videoRegex = "<video[^>]*?\\s+src\\s*=\\s*[\"']([^\"']*)[\"'][^>]*>";
        Pattern videoPattern = Pattern.compile(videoRegex, Pattern.CASE_INSENSITIVE);
        Matcher videoMatcher = videoPattern.matcher(html);
        
        while (videoMatcher.find()) {
            String videoUrl = videoMatcher.group(1);
            if (videoUrl != null && !videoUrl.isEmpty()) {
                videoUrls.add(videoUrl);
            }
        }
        
        // 匹配iframe标签的src属性，更宽容地处理标签内的属性顺序
        String iframeRegex = "<iframe[^>]*?\\s+src\\s*=\\s*[\"']([^\"']*)[\"'][^>]*>";
        Pattern iframePattern = Pattern.compile(iframeRegex, Pattern.CASE_INSENSITIVE);
        Matcher iframeMatcher = iframePattern.matcher(html);
        
        while (iframeMatcher.find()) {
            String iframeUrl = iframeMatcher.group(1);
            if (iframeUrl != null && !iframeUrl.isEmpty()) {
                videoUrls.add(iframeUrl);
            }
        }
        
        return videoUrls;
    }
}