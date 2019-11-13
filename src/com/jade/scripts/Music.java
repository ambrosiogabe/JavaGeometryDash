package com.jade.scripts;

import com.jade.components.Component;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

public class Music extends Component {

    private String filepath;
    AudioInputStream audio = null;
    Clip audioClip = null;

    public Music(String filename) {
        filepath = new File(filename).getAbsolutePath();

        try {
            audio = AudioSystem.getAudioInputStream(new File(filepath).getAbsoluteFile());
            audioClip = AudioSystem.getClip();
            audioClip.open(audio);
            restartClip();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        audioClip.stop();
    }

    public void restartClip() {
        try {
            audioClip.stop();
            audioClip.setFramePosition(0);
            audioClip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Component clone() {
        return null;
    }
}
