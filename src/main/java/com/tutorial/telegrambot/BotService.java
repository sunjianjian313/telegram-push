package com.tutorial.telegrambot;

import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class BotService {
    
    private final Bot bot;

    public BotService(Bot bot) {
        this.bot = bot;
    }

    public void sendPhotoWithCaption(Long chatId, String photoPath, String caption) {
        bot.sendPhotoWithCaption(chatId, photoPath, caption);
    }

    public void sendPhotoWithCaptionByUrl(Long chatId, String imageUrl, String caption) {
        bot.sendPhotoWithCaptionByUrl(chatId, imageUrl, caption);
    }

    public void sendPhotoWithCaptionAndFormat(Long chatId, String photoPath, String caption, String parseMode) {
        bot.sendPhotoWithCaptionAndFormat(chatId, photoPath, caption, parseMode);
    }

    public void sendPhotoWithCaptionFromBytes(Long chatId, byte[] photoBytes, String caption) {
        bot.sendPhotoWithCaptionFromBytes(chatId, photoBytes, caption);
    }
    
    public void sendText(Long chatId, String text) {
        bot.sendText(chatId, text);
    }
    
    public void sendVideoWithCaption(Long chatId, String videoPath, String caption) {
        bot.sendVideoWithCaption(chatId, videoPath, caption);
    }
    
    public void sendVideoWithCaptionByUrl(Long chatId, String videoUrl, String caption) {
        bot.sendVideoWithCaptionByUrl(chatId, videoUrl, caption);
    }
    
    public void sendVideoWithCaptionFromBytes(Long chatId, byte[] videoBytes, String caption) {
        bot.sendVideoWithCaptionFromBytes(chatId, videoBytes, caption);
    }
    
    public void sendMediaGroup(Long chatId, java.util.List<String> photoPaths, java.util.List<String> captions) {
        bot.sendMediaGroup(chatId, photoPaths, captions);
    }
    
    public void sendMediaGroupFromBytes(Long chatId, java.util.List<byte[]> photoBytesList, java.util.List<String> captions) {
        bot.sendMediaGroupFromBytes(chatId, photoBytesList, captions);
    }
}