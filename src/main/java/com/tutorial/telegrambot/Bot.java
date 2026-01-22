package com.tutorial.telegrambot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description
 * @Author SUNJJ
 * @Date 2025/9/18
 **/
@Component
public class Bot extends TelegramLongPollingBot {

    @Value("${bot.username}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.default.chat.id}")
    private String defaultChatId;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }


    @Override
    public void onUpdateReceived(Update update) {
        // 安全检查更新对象是否包含消息
        if (update != null && update.hasMessage()) {
            var msg = update.getMessage();
            // 进一步检查消息对象及其关键属性是否为空
            if (msg != null && msg.getFrom() != null && msg.getText() != null) {
                var user = msg.getFrom();
                var id = user.getId();

                sendText(Long.valueOf(defaultChatId),"外语学院,新院长-樊院长,欢迎您的加入!");

                System.out.println(user.getFirstName() + " wrote " + msg.getText());
            }
        }
    }


    public void sendText(Long who, String what){
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString()) //Who are we sending a message to
                .text(what).build();    //Message content
        try {
            execute(sm);                        //Actually sending the message
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }

    /**
     * 发送本地图片和文字
     *
     * @param chatId  接收者聊天ID
     * @param photoPath 图片路径
     * @param caption 图片说明文字
     */
    public void sendPhotoWithCaption(Long chatId, String photoPath, String caption) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId.toString());
        
        // 检查是否为网络URL，如果是则使用InputFile包装URL，否则作为本地文件处理
        if (photoPath.startsWith("http://") || photoPath.startsWith("https://")) {
            // 网络URL，使用InputFile包装
            sendPhoto.setPhoto(new InputFile(photoPath));
        } else {
            // 本地文件路径，使用InputFile包装
            sendPhoto.setPhoto(new InputFile(new File(photoPath), new File(photoPath).getName()));
        }
        
        sendPhoto.setCaption(caption);
        
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 发送网络图片和文字
     *
     * @param chatId  接收者聊天ID
     * @param imageUrl 图片URL
     * @param caption 图片说明文字
     */
    public void sendPhotoWithCaptionByUrl(Long chatId, String imageUrl, String caption) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId.toString());
        // 根据Telegram Bot API要求，即使是URL也需要使用InputFile包装
        sendPhoto.setPhoto(new InputFile(imageUrl));
        sendPhoto.setCaption(caption);
        
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 发送图片和文字（可设置更多选项）
     *
     * @param chatId  接收者聊天ID
     * @param photoPath 图片路径
     * @param caption 图片说明文字
     * @param parseMode 解析模式，如 "HTML" 或 "Markdown"
     */
    public void sendPhotoWithCaptionAndFormat(Long chatId, String photoPath, String caption, String parseMode) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId.toString());
        
        // 检查是否为网络URL，如果是则使用InputFile包装URL，否则作为本地文件处理
        if (photoPath.startsWith("http://") || photoPath.startsWith("https://")) {
            // 网络URL，使用InputFile包装
            sendPhoto.setPhoto(new InputFile(photoPath));
        } else {
            // 本地文件路径，使用InputFile包装
            sendPhoto.setPhoto(new InputFile(new File(photoPath), new File(photoPath).getName()));
        }
        
        sendPhoto.setCaption(caption);
        if (parseMode != null && !parseMode.isEmpty()) {
            sendPhoto.setParseMode(parseMode);
        }
        
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 发送字节数组图片和文字
     *
     * @param chatId  接收者聊天ID
     * @param photoBytes 图片字节数组
     * @param caption 图片说明文字
     */
    public void sendPhotoWithCaptionFromBytes(Long chatId, byte[] photoBytes, String caption) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId.toString());
        
        // 创建临时文件来发送图片
        try {
            File tempFile = File.createTempFile("temp_photo_", ".jpg");
            tempFile.deleteOnExit();
            
            // 写入字节数组到临时文件
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(photoBytes);
            }
            
            sendPhoto.setPhoto(new InputFile(tempFile, tempFile.getName()));
            sendPhoto.setCaption(caption);
            
            execute(sendPhoto);
        } catch (TelegramApiException | java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 发送媒体组（多张图片）
     *
     * @param chatId  接收者聊天ID
     * @param photoPaths 图片路径列表
     * @param captions 图片说明文字列表
     */
    public void sendMediaGroup(Long chatId, List<String> photoPaths, List<String> captions) {
        SendMediaGroup sendMediaGroup = new SendMediaGroup();
        sendMediaGroup.setChatId(chatId.toString());
        
        List<InputMedia> mediaList = new ArrayList<>();
        
        for (int i = 0; i < photoPaths.size(); i++) {
            String photoPath = photoPaths.get(i);
            
            InputMediaPhoto inputMediaPhoto;
            // 检查是否为网络URL，如果是则直接使用字符串，否则作为本地文件处理
            if (photoPath.startsWith("http://") || photoPath.startsWith("https://")) {
                // 网络URL，直接使用字符串
                inputMediaPhoto = InputMediaPhoto.builder()
                    .media(photoPath)
                    .build();
            } else {
                // 本地文件路径，使用文件路径字符串
                inputMediaPhoto = InputMediaPhoto.builder()
                    .media(photoPath)
                    .build();
            }
            
            // 根据Telegram Bot API规范，caption只能设置在媒体组的第一个元素上
            if (captions != null && i < captions.size() && i == 0) {
                inputMediaPhoto.setCaption(captions.get(i));
            }
            
            mediaList.add(inputMediaPhoto);
        }
        
        sendMediaGroup.setMedias(mediaList);
        
        try {
            execute(sendMediaGroup);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 发送媒体组（多张图片）- 使用字节数组
     *
     * @param chatId  接收者聊天ID
     * @param photoBytesList 图片字节数组列表
     * @param captions 图片说明文字列表
     */
    public void sendMediaGroupFromBytes(Long chatId, List<byte[]> photoBytesList, List<String> captions) {
        SendMediaGroup sendMediaGroup = new SendMediaGroup();
        sendMediaGroup.setChatId(chatId.toString());
        
        List<InputMedia> mediaList = new ArrayList<>();
        
        for (int i = 0; i < photoBytesList.size(); i++) {
            try {
                // 创建临时文件来发送图片
                File tempFile = File.createTempFile("temp_photo_", ".jpg");
                tempFile.deleteOnExit();
                
                // 写入字节数组到临时文件
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    fos.write(photoBytesList.get(i));
                }
                
                // 使用构建器模式创建InputMediaPhoto，对于临时文件使用路径字符串
                InputMediaPhoto inputMediaPhoto = InputMediaPhoto.builder()
                    .media(tempFile.getAbsolutePath())
                    .build();
                
                // 根据Telegram Bot API规范，caption只能设置在媒体组的第一个元素上
                if (captions != null && i < captions.size() && i == 0) {
                    inputMediaPhoto.setCaption(captions.get(i));
                }
                
                mediaList.add(inputMediaPhoto);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        sendMediaGroup.setMedias(mediaList);
        
        try {
            execute(sendMediaGroup);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    public void copyMessage(Long who, Integer msgId){
        CopyMessage cm = CopyMessage.builder()
                .fromChatId(who.toString())  //We copy from the user
                .chatId(who.toString())      //And send it back to him
                .messageId(msgId)            //Specifying what message
                .build();
        try {
            execute(cm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 发送本地视频和文字
     *
     * @param chatId  接收者聊天ID
     * @param videoPath 视频路径
     * @param caption 视频说明文字
     */
    public void sendVideoWithCaption(Long chatId, String videoPath, String caption) {
        SendVideo sendVideo = new SendVideo();
        sendVideo.setChatId(chatId.toString());
        
        // 检查是否为网络URL，如果是则使用InputFile包装URL，否则作为本地文件处理
        if (videoPath.startsWith("http://") || videoPath.startsWith("https://")) {
            // 网络URL，使用InputFile包装
            sendVideo.setVideo(new InputFile(videoPath));
        } else {
            // 本地文件路径，使用InputFile包装
            sendVideo.setVideo(new InputFile(new File(videoPath), new File(videoPath).getName()));
        }
        
        sendVideo.setCaption(caption);
        
        try {
            execute(sendVideo);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 发送网络视频和文字
     *
     * @param chatId  接收者聊天ID
     * @param videoUrl 视频URL
     * @param caption 视频说明文字
     */
    public void sendVideoWithCaptionByUrl(Long chatId, String videoUrl, String caption) {
        SendVideo sendVideo = new SendVideo();
        sendVideo.setChatId(chatId.toString());
        // 根据Telegram Bot API要求，即使是URL也需要使用InputFile包装
        sendVideo.setVideo(new InputFile(videoUrl));
        sendVideo.setCaption(caption);
        
        try {
            execute(sendVideo);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 发送字节数组视频和文字
     *
     * @param chatId  接收者聊天ID
     * @param videoBytes 视频字节数组
     * @param caption 视频说明文字
     */
    public void sendVideoWithCaptionFromBytes(Long chatId, byte[] videoBytes, String caption) {
        SendVideo sendVideo = new SendVideo();
        sendVideo.setChatId(chatId.toString());
        
        // 创建临时文件来发送视频
        try {
            File tempFile = File.createTempFile("temp_video_", ".mp4");
            tempFile.deleteOnExit();
            
            // 写入字节数组到临时文件
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(videoBytes);
            }
            
            sendVideo.setVideo(new InputFile(tempFile, tempFile.getName()));
            sendVideo.setCaption(caption);
            
            execute(sendVideo);
        } catch (TelegramApiException | java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }
}