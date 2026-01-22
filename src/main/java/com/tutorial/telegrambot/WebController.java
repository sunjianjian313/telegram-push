package com.tutorial.telegrambot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.tutorial.telegrambot.HtmlUtils;
import com.tutorial.telegrambot.DataUrlUtils;

@Controller
public class WebController {

    @Autowired
    private BotService botService;

    private static final String DEFAULT_CHAT_ID = System.getenv("DEFAULT_CHAT_ID") != null ? 
        System.getenv("DEFAULT_CHAT_ID") : "-1002979306798"; // 默认聊天ID

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("chatId", DEFAULT_CHAT_ID);
        return "index";
    }

    @PostMapping("/sendPhoto")
    @ResponseBody
    public String sendPhoto(@RequestBody Map<String, String> requestData) {
        try {
            String chatId = requestData.get("chatId");
            String photoPath = requestData.get("photoPath");
            String caption = requestData.get("caption");
            
            // 如果没有提供聊天ID，则使用默认ID
            String actualChatId = (chatId == null || chatId.trim().isEmpty()) ? DEFAULT_CHAT_ID : chatId;
            Long chatIdLong = Long.parseLong(actualChatId);
            String telegramCaption = HtmlUtils.convertForTelegram(caption);
            
            // 检查是否为Data URL
            if (photoPath.startsWith("data:image")) {
                // 处理Data URL，直接使用字节数组方式
                File tempFile = DataUrlUtils.saveDataUrlAsTempFile(photoPath);
                // 读取临时文件字节并发送
                Path path = tempFile.toPath();
                byte[] imageBytes = java.nio.file.Files.readAllBytes(path);
                botService.sendPhotoWithCaptionFromBytes(chatIdLong, imageBytes, telegramCaption);
                // 删除临时文件
                tempFile.delete();
            } else {
                // 处理普通文件路径
                botService.sendPhotoWithCaption(chatIdLong, photoPath, telegramCaption);
            }
            
            return "图片发送成功！";
        } catch (Exception e) {
            e.printStackTrace();
            return "发送失败：" + e.getMessage();
        }
    }

    @PostMapping("/sendPhotoByUrl")
    @ResponseBody
    public String sendPhotoByUrl(@RequestBody Map<String, String> requestData) {
        try {
            String chatId = requestData.get("chatId");
            String imageUrl = requestData.get("imageUrl");
            String caption = requestData.get("caption");
            
            // 如果没有提供聊天ID，则使用默认ID
            String actualChatId = (chatId == null || chatId.trim().isEmpty()) ? DEFAULT_CHAT_ID : chatId;
            Long chatIdLong = Long.parseLong(actualChatId);
            // 转换HTML内容为Telegram兼容格式
            String telegramCaption = HtmlUtils.convertForTelegram(caption);
            botService.sendPhotoWithCaptionByUrl(chatIdLong, imageUrl, telegramCaption);
            return "图片发送成功！";
        } catch (Exception e) {
            return "发送失败：" + e.getMessage();
        }
    }
    
    @PostMapping("/sendTextOnly")
    @ResponseBody
    public String sendTextOnly(@RequestBody Map<String, String> requestData) {
        try {
            String chatId = requestData.get("chatId");
            String caption = requestData.get("caption");
            
            // 如果没有提供聊天ID，则使用默认ID
            String actualChatId = (chatId == null || chatId.trim().isEmpty()) ? DEFAULT_CHAT_ID : chatId;
            Long chatIdLong = Long.parseLong(actualChatId);
            
            // 提取富文本中的图片URL
            List<String> imageUrls = HtmlUtils.extractImageUrls(caption);
            
            // 检查富文本中是否有视频
            List<String> videoUrls = HtmlUtils.extractVideoUrls(caption);
            
            if (!videoUrls.isEmpty()) {
                // 如果富文本中包含视频，优先发送视频
                String textWithoutVideos = HtmlUtils.convertForTelegram(caption);
                            
                // 发送所有视频
                for (String videoUrl : videoUrls) {
                    if (videoUrl.startsWith("data:video/")) {
                        // 如果是视频Data URL，保存为临时文件后发送
                        try {
                            // 首先验证Data URL格式
                            if (!videoUrl.matches("^data:video/[a-zA-Z0-9\\-+/]+;base64,[a-zA-Z0-9+/=]+$")) {
                                // 尝试修复格式
                                videoUrl = videoUrl.replaceAll("[^a-zA-Z0-9+/=;,:\\-]", "");
                                if (!videoUrl.matches("^data:video/[a-zA-Z0-9\\-+/]+;base64,[a-zA-Z0-9+/=]+$")) {
                                    throw new IllegalArgumentException("Invalid video Data URL format");
                                }
                            }
                                        
                            File tempVideoFile = DataUrlUtils.saveVideoDataUrlAsTempFile(videoUrl);
                            botService.sendVideoWithCaption(chatIdLong, tempVideoFile.getAbsolutePath(), textWithoutVideos);
                        } catch (Exception e) {
                            // 如果处理视频数据失败，尝试作为普通文本发送
                            String telegramCaption = HtmlUtils.convertForTelegram(caption);
                            botService.sendText(chatIdLong, telegramCaption);
                            return "视频数据处理失败，已发送文本内容：" + e.getMessage();
                        }
                    } else {
                        // 如果是外部视频URL
                        botService.sendVideoWithCaptionByUrl(chatIdLong, videoUrl, textWithoutVideos);
                    }
                }
                return "富文本中的视频及文字发送成功！共发送 " + videoUrls.size() + " 个视频";
            } else if (!imageUrls.isEmpty()) {
                // 如果富文本中包含图片（在没有视频的情况下）
                String textWithoutImages = HtmlUtils.convertForTelegram(caption);
                
                // 检查是否都是Data URL图片
                boolean allDataImages = imageUrls.stream().allMatch(url -> url.startsWith("data:image"));
                
                if (allDataImages && imageUrls.size() > 1) {
                    // 如果有多张Data URL图片，使用sendMediaGroup发送
                    try {
                        java.util.List<byte[]> photoBytesList = new java.util.ArrayList<>();
                        for (String imageUrl : imageUrls) {
                            byte[] imageBytes = DataUrlUtils.dataUrlToBytes(imageUrl);
                            photoBytesList.add(imageBytes);
                        }
                        
                        // 创建一个包含相同说明文字的列表
                        java.util.List<String> captions = new java.util.ArrayList<>();
                        for (int i = 0; i < imageUrls.size(); i++) {
                            captions.add(textWithoutImages);
                        }
                        
                        botService.sendMediaGroupFromBytes(chatIdLong, photoBytesList, captions);
                        
                        return "富文本中的图片及文字发送成功！共发送 " + imageUrls.size() + " 张图片（作为媒体组）";
                    } catch (Exception e) {
                        // 如果媒体组发送失败，回退到逐个发送
                        for (String imageUrl : imageUrls) {
                            if (imageUrl.startsWith("data:image")) {
                                // 如果是Base64编码的图片，需要特殊处理
                                botService.sendPhotoWithCaptionFromBytes(chatIdLong, DataUrlUtils.dataUrlToBytes(imageUrl), textWithoutImages);
                            } else {
                                // 如果是外部图片URL
                                botService.sendPhotoWithCaptionByUrl(chatIdLong, imageUrl, textWithoutImages);
                            }
                        }
                        return "富文本中的图片及文字发送成功！共发送 " + imageUrls.size() + " 张图片（媒体组发送失败，已逐个发送）";
                    }
                } else {
                    // 如果不是全部都是Data URL图片或只有一张图片，按原来方式处理
                    // 分离本地图片路径和外部图片URL
                    java.util.List<String> localPhotoPaths = new java.util.ArrayList<>();
                    java.util.List<String> externalPhotoUrls = new java.util.ArrayList<>();
                    java.util.List<String> localCaptions = new java.util.ArrayList<>();
                    java.util.List<String> externalCaptions = new java.util.ArrayList<>();
                    
                    for (String imageUrl : imageUrls) {
                        if (imageUrl.startsWith("data:image")) {
                            // Data URL图片单独处理
                            botService.sendPhotoWithCaptionFromBytes(chatIdLong, DataUrlUtils.dataUrlToBytes(imageUrl), textWithoutImages);
                        } else {
                            // 外部URL图片归类
                            externalPhotoUrls.add(imageUrl);
                            externalCaptions.add(textWithoutImages);
                        }
                    }
                    
                    // 如果有外部URL图片，尝试使用媒体组发送
                    if (!externalPhotoUrls.isEmpty()) {
                        if (externalPhotoUrls.size() == 1) {
                            // 单张外部图片
                            botService.sendPhotoWithCaptionByUrl(chatIdLong, externalPhotoUrls.get(0), textWithoutImages);
                        } else {
                            // 多张外部图片使用媒体组发送，先过滤有效的HTTP/HTTPS URL
                            java.util.List<String> validExternalPhotoUrls = externalPhotoUrls.stream()
                                .filter(url -> url != null && (url.startsWith("http://") || url.startsWith("https://")))
                                .collect(java.util.stream.Collectors.toList());
                                                    
                            if (!validExternalPhotoUrls.isEmpty()) {
                                // 仅发送有效的URL
                                java.util.List<String> validExternalCaptions = externalCaptions.stream()
                                    .limit(validExternalPhotoUrls.size())
                                    .collect(java.util.stream.Collectors.toList());
                                botService.sendMediaGroup(chatIdLong, validExternalPhotoUrls, validExternalCaptions);
                            } else {
                                // 如果没有有效URL，单独发送之前识别的每张图片
                                for (String imageUrl : imageUrls) {
                                    if (!imageUrl.startsWith("data:image")) {
                                        botService.sendPhotoWithCaptionByUrl(chatIdLong, imageUrl, textWithoutImages);
                                    }
                                }
                            }
                        }
                    }
                    
                    return "富文本中的图片及文字发送成功！共发送 " + imageUrls.size() + " 张图片";
                }
            } else {
                // 如果没有视频或图片，只发送文本
                String telegramCaption = HtmlUtils.convertForTelegram(caption);
                botService.sendText(chatIdLong, telegramCaption);
                return "文本发送成功！";
            }
        } catch (Exception e) {
            return "发送失败：" + e.getMessage();
        }
    }
    
    @PostMapping(value = "/sendGridContent", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public String sendGridContent(
            @RequestParam("chatId") String chatId,
            @RequestParam("caption") String caption,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "video", required = false) MultipartFile video) {
        try {
            // 如果没有提供聊天ID，则使用默认ID
            String actualChatId = (chatId == null || chatId.trim().isEmpty()) ? DEFAULT_CHAT_ID : chatId;
            Long chatIdLong = Long.parseLong(actualChatId);
            
            // 首先发送视频（如果有）
            if (video != null && !video.isEmpty()) {
                // 保存视频到临时文件
                File tempVideoFile = File.createTempFile("temp_video_", "." + getFileExtension(video.getOriginalFilename()));
                tempVideoFile.deleteOnExit();
                video.transferTo(tempVideoFile);
                
                // 发送视频 - 使用绝对路径（本地文件）
                botService.sendVideoWithCaption(chatIdLong, tempVideoFile.getAbsolutePath(), HtmlUtils.convertForTelegram(caption));
            }
            
            // 如果有图片，发送图片（作为媒体组）
            if (images != null && !images.isEmpty()) {
                List<String> imagePaths = new ArrayList<>();
                List<String> captions = new ArrayList<>();
                
                for (int i = 0; i < images.size(); i++) {
                    MultipartFile image = images.get(i);
                    if (!image.isEmpty()) {
                        // 保存图片到临时文件
                        File tempImageFile = File.createTempFile("temp_image_", "." + getFileExtension(image.getOriginalFilename()));
                        tempImageFile.deleteOnExit();
                        image.transferTo(tempImageFile);
                        
                        // 添加图片路径 - 使用绝对路径（本地文件）
                        imagePaths.add(tempImageFile.getAbsolutePath());
                        // 每个图片都使用相同的标题
                        captions.add(HtmlUtils.convertForTelegram(caption));
                    }
                }
                
                // 发送媒体组
                if (!imagePaths.isEmpty()) {
                    botService.sendMediaGroup(chatIdLong, imagePaths, captions);
                }
            }
            
            // 如果既没有图片也没有视频，只发送文本
            if ((images == null || images.isEmpty()) && (video == null || video.isEmpty())) {
                botService.sendText(chatIdLong, HtmlUtils.convertForTelegram(caption));
            }
            
            StringBuilder result = new StringBuilder("九宫格内容发送成功！");
            if (images != null && !images.isEmpty()) {
                result.append(" 共发送 ").append(images.size()).append(" 张图片");
            }
            if (video != null && !video.isEmpty()) {
                result.append(" 1 个视频");
            }
            if ((images == null || images.isEmpty()) && (video == null || video.isEmpty())) {
                result.append(" 纯文本");
            }
            
            return result.toString();
            
        } catch (Exception e) {
            return "发送失败：" + e.getMessage();
        }
    }
    
    // 辅助方法：获取文件扩展名
    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        return "jpg"; // 默认扩展名
    }
    
    @PostMapping("/api/getFolderFiles")
    @ResponseBody
    public List<String> getFolderFiles(@RequestParam String folderPath) {
        List<String> files = new ArrayList<>();
        try {
            File folder = new File(folderPath);
            if (folder.exists() && folder.isDirectory()) {
                File[] fileList = folder.listFiles((dir, name) -> {
                    String lowerName = name.toLowerCase();
                    return lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || 
                           lowerName.endsWith(".png") || lowerName.endsWith(".gif") || 
                           lowerName.endsWith(".bmp") || lowerName.endsWith(".webp");
                });
                
                if (fileList != null) {
                    for (File file : fileList) {
                        if (file.isFile()) {
                            files.add(file.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return files;
    }
}