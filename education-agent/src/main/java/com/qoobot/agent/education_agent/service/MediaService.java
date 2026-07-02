package com.qoobot.agent.education_agent.service;

/**
 * 多媒体服务（语音交互）
 *
 * <p>Phase 1 提供基础 STT/TTS；Phase 3 扩展拍照识图/手写识别。
 */
public interface MediaService {

    /**
     * 语音转文字（Speech-to-Text）
     *
     * @param audioBytes 音频字节
     * @param format     音频格式 (mp3/wav/webm/ogg)
     * @return 识别文本
     */
    String speechToText(byte[] audioBytes, String format);

    /**
     * 文字转语音（Text-to-Speech）
     *
     * @param text  要合成的文字
     * @param voice 音色 (alloy/echo/fable/onyx/nova/shimmer)
     * @return 音频字节（MP3 格式）
     */
    byte[] textToSpeech(String text, String voice);

    /**
     * 获取支持的音色列表
     */
    String[] getAvailableVoices();
}
