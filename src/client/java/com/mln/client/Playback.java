package com.mln.client;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Playback {
    public static void playAudio() {
        playAudio(generateNoise());
    }

    public static void playAudio(byte[] toPlay) {
        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 48000f, 8, 2, 2, 48000f, false);
        Thread thread = new Thread(() -> playAudio(format, toPlay));
        thread.start();
    }

    public static void playAudio(AudioFormat format) {
        Thread thread = new Thread(() -> playAudio(format, generateNoise()));
        thread.start();
    }

    public static void playAudio(AudioFormat format, byte[] toPlay) {
        try {
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();

            int offset = 0;
            int bufSize = 4096;

            while (offset < toPlay.length) {
                int toWrite = Math.min(bufSize, toPlay.length - offset);

                line.write(toPlay, offset, toWrite);
                offset += toWrite;
            }

            line.drain();
            line.close();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private static byte[] generateNoise() {
        byte[] noise = new byte[1024 * 128];
        double t = 0;
        double dt = 2 * Math.PI / 48000;
        for (int i = 0; i < noise.length; i++) {
            noise[i] = (byte) Math.floor(Math.sin(t) * 127);
            t = (t + dt) % (Math.PI * 2);
        }
        return noise;
    }
}
