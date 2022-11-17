package com.kalle.gogame;

import java.applet.Applet;
import java.applet.AudioClip;
import java.net.URL;

public class WavPlayer {

    // 落子音效

    private final AudioClip playAudio;

    // 吃多个子

    private final AudioClip remove1Audio;
    private final AudioClip remove2Audio;
    private final AudioClip remove4Audio;

    public WavPlayer() {
        ClassLoader classLoader = WavPlayer.class.getClassLoader();
        URL playUrl = classLoader.getResource("wav/play.wav");
        URL remove1Url = classLoader.getResource("wav/remove1.wav");
        URL remove2Url = classLoader.getResource("wav/remove2.wav");
        URL remove4Url = classLoader.getResource("wav/remove4.wav");

        this.playAudio = Applet.newAudioClip(playUrl);
        this.remove1Audio = Applet.newAudioClip(remove1Url);
        this.remove2Audio = Applet.newAudioClip(remove2Url);
        this.remove4Audio = Applet.newAudioClip(remove4Url);
    }

    public void play() {
        this.playAudio.play();
    }

    public void remove(int count) {
        if (count <= 0) {
            return;
        }
        if (count == 1) {
            this.remove1Audio.play();
        } else if (count < 4) {
            this.remove2Audio.play();
        } else {
            this.remove4Audio.play();
        }
    }
}
