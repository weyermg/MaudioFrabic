package com.mln.client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Playback {

    private AudioFormat format;
    private BlockingQueue<byte[]> audioQueue = new LinkedBlockingDeque<>();
    private SourceDataLine line;
    private volatile boolean running = false;

    public Playback() {
        this.format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                48000f,
                16,
                2,
                4,
                48000f,
                false);
    }

    public Playback(AudioFormat format) {
        this.format = format;
    }

    public void start() {
        if (running)
            return;

        running = true;

        try {
            line = AudioSystem.getSourceDataLine(format);
            line.open(format, 4096);
            line.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            running = false;
            return;
        }

        Thread playbackThread = new Thread(() -> {
            try {
                while (running) {
                    byte[] data = audioQueue.take();
                    line.write(data, 0, data.length);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        playbackThread.setDaemon(true);
        playbackThread.start();
    }

    public void submitAudio(byte[] data) {
        if (!running)
            start();

        audioQueue.offer(data.clone());
    }

    public void stop() {
        running = false;

        if (line != null) {
            line.drain();
            line.stop();
            line.close();
        }
    }

    public void playNoise() {
        submitAudio(generateNoise());
    }

    private static byte[] generateNoise() {
        byte[] noise = new byte[1024 * 128];
        double t = 0;
        double dt = 2 * Math.PI / 48000;
        for (int i = 0; i < noise.length; i++) {
            short sample = (short) (Math.sin(t) * Short.MAX_VALUE);

            int idx = i * 4;

            if (idx + 3 >= noise.length)
                break;

            noise[idx] = (byte) (sample & 0xFF);
            noise[idx + 1] = (byte) ((sample >> 8) & 0xFF);
            noise[idx + 2] = noise[idx];
            noise[idx + 3] = noise[idx + 1];

            t = (t + dt) % (Math.PI * 2);
        }
        return noise;
    }

    public static byte[] generateTone(double frequency, double durationSeconds) {
        int sampleRate = 48000;
        int frames = (int) (sampleRate * durationSeconds);

        byte[] audio = new byte[frames * 4];
        double t = 0;
        double dt = 2 * Math.PI * frequency / sampleRate;

        for (int i = 0; i < frames; i++) {
            short sample = (short) (Math.sin(t) * Short.MAX_VALUE);
            int idx = i * 4;

            audio[idx] = (byte) (sample & 0xFF);
            audio[idx + 1] = (byte) ((sample >> 8) & 0xFF);
            audio[idx + 2] = audio[idx];
            audio[idx + 3] = audio[idx + 1];

            t += dt;
        }
        return audio;
    }
}
