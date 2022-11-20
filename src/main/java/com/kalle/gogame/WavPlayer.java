package com.kalle.gogame;

import java.applet.Applet;
import java.applet.AudioClip;
import java.net.URL;

/**
 * Wav音效播放器
 *
 * @author kalle
 * @since 2022-11-18
 */
public class WavPlayer {

    /**
     * 私有化构造
     */
    private WavPlayer() {
    }

    /**
     * 落子音效
     */
    private static final AudioClip PLAY;

    /**
     * 吃一子音效
     */
    private static final AudioClip REMOVE1;

    /**
     * 吃两或三子音效
     */
    private static final AudioClip REMOVE2;

    /**
     * 吃四子以上音效
     */
    private static final AudioClip REMOVE4;

    static {
        // 初始化音效

        PLAY = loadAudio("wav/play.wav");
        REMOVE1 = loadAudio("wav/remove1.wav");
        REMOVE2 = loadAudio("wav/remove2.wav");
        REMOVE4 = loadAudio("wav/remove4.wav");
    }

    /**
     * 落子音效
     */
    public static void play() {
        PLAY.play();
    }

    /**
     * 吃子音效
     *
     * @param count count
     */
    public static void remove(int count) {
        if (count <= 0) {
            return;
        }
        if (count == 1) {
            REMOVE1.play();
        } else if (count < 4) {
            REMOVE2.play();
        } else {
            REMOVE4.play();
        }
    }

    /**
     * 加载音效
     *
     * @param audioPath audioPath
     * @return AudioClip
     */
    private static AudioClip loadAudio(String audioPath) {
        ClassLoader classLoader = WavPlayer.class.getClassLoader();
        URL playUrl = classLoader.getResource(audioPath);
        return Applet.newAudioClip(playUrl);
    }
}
