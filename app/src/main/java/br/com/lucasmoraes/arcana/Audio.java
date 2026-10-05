package br.com.lucasmoraes.arcana;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/** Sons e música 100% procedurais (sem arquivos de áudio). Qualquer falha é ignorada. */
final class Audio {
    static final int CLICK = 0, OK = 1, FAIL = 2, HIT = 3, MAGIC = 4, CHEST = 5, LEVEL = 6;
    static final int RATE = 22050;
    static volatile boolean musicRun;
    static final AtomicInteger active = new AtomicInteger();
    static final Random RND = new Random();

    static short[] tone(float f0, float f1, int ms, float vol, int wave) {
        int n = RATE * ms / 1000;
        short[] s = new short[n];
        double ph = 0;
        for (int i = 0; i < n; i++) {
            float p = i / (float) n;
            ph += 2 * Math.PI * (f0 + (f1 - f0) * p) / RATE;
            double env = Math.min(1.0, i / 150.0) * (1 - p) * (1 - p * 0.3);
            double v = wave == 0 ? Math.sin(ph) : wave == 1 ? Math.sin(ph) + 0.35 * Math.sin(2 * ph) : RND.nextDouble() * 2 - 1;
            s[i] = (short) (v * env * vol * 16000);
        }
        return s;
    }

    static short[] cat(short[]... parts) {
        int n = 0;
        for (short[] p : parts) n += p.length;
        short[] o = new short[n];
        int k = 0;
        for (short[] p : parts) { System.arraycopy(p, 0, o, k, p.length); k += p.length; }
        return o;
    }

    static short[] gen(int id) {
        switch (id) {
            case CLICK: return tone(900, 700, 45, 0.5f, 0);
            case OK: return cat(tone(523, 523, 80, 0.6f, 1), tone(659, 659, 80, 0.6f, 1), tone(784, 784, 80, 0.6f, 1), tone(1047, 1047, 220, 0.6f, 1));
            case FAIL: return cat(tone(330, 300, 140, 0.6f, 1), tone(247, 190, 280, 0.6f, 1));
            case HIT: return cat(tone(0, 0, 90, 0.7f, 2), tone(140, 60, 100, 0.8f, 0));
            case MAGIC: return tone(600, 1700, 260, 0.45f, 1);
            case CHEST: return cat(tone(300, 900, 320, 0.45f, 1), tone(784, 784, 90, 0.6f, 1), tone(988, 988, 90, 0.6f, 1), tone(1319, 1319, 260, 0.6f, 1));
            default: return cat(tone(523, 523, 100, 0.6f, 1), tone(659, 659, 100, 0.6f, 1), tone(784, 784, 100, 0.6f, 1), tone(1047, 1047, 100, 0.6f, 1), tone(1319, 1319, 300, 0.6f, 1));
        }
    }

    static void sfx(final int id) {
        if (!Game.fx || Game.vol <= 0.01f || active.get() > 5) return;
        active.incrementAndGet();
        Thread t = new Thread(new Runnable() {
            public void run() {
                AudioTrack tr = null;
                try {
                    short[] d = gen(id);
                    float v = Game.vol;
                    for (int i = 0; i < d.length; i++) d[i] = (short) (d[i] * v);
                    int min = AudioTrack.getMinBufferSize(RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
                    tr = new AudioTrack(AudioManager.STREAM_MUSIC, RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, Math.max(min, 4096), AudioTrack.MODE_STREAM);
                    tr.play();
                    tr.write(d, 0, d.length);
                    tr.stop();
                    Thread.sleep(d.length * 1000L / RATE + 100);
                } catch (Throwable e) { /* ignora */ }
                try { if (tr != null) tr.release(); } catch (Throwable e) { /* ignora */ }
                active.decrementAndGet();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    static short[] bar(int[] chord, boolean high) {
        int noteMs = 430, n = RATE * noteMs / 1000;
        int[] pat = {0, 1, 2, 1, 0, 1, 2, 1};
        short[] out = new short[n * 8];
        for (int i = 0; i < 8; i++) {
            int midi = chord[pat[i]] + (high ? 12 : 0);
            double f = 440.0 * Math.pow(2, (midi - 69) / 12.0), ph = 0;
            for (int k = 0; k < n; k++) {
                ph += 2 * Math.PI * f / RATE;
                double p = k / (double) n, env = Math.exp(-3.2 * p) * Math.min(1.0, k / 120.0);
                out[i * n + k] = (short) ((Math.sin(ph) + 0.3 * Math.sin(2 * ph)) * env * 4500);
            }
        }
        double fb = 440.0 * Math.pow(2, (chord[0] - 12 - 69) / 12.0), ph2 = 0;
        for (int k = 0; k < out.length; k++) {
            ph2 += 2 * Math.PI * fb / RATE;
            out[k] += (short) (Math.sin(ph2) * 2200);
        }
        return out;
    }

    static void startMusic() {
        if (!Game.music || musicRun) return;
        musicRun = true;
        Thread t = new Thread(new Runnable() {
            public void run() {
                AudioTrack tr = null;
                try {
                    int min = AudioTrack.getMinBufferSize(RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
                    tr = new AudioTrack(AudioManager.STREAM_MUSIC, RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, Math.max(min * 2, 16384), AudioTrack.MODE_STREAM);
                    tr.play();
                    int[][] chords = {{57, 60, 64}, {53, 57, 60}, {48, 52, 55}, {55, 59, 62}};
                    int b = 0;
                    while (musicRun) {
                        short[] d = bar(chords[b % 4], b % 8 >= 4);
                        tr.setVolume(Math.max(0f, Math.min(1f, Game.vol * 0.55f)));
                        tr.write(d, 0, d.length);
                        b++;
                    }
                } catch (Throwable e) { /* ignora */ }
                try { if (tr != null) { tr.stop(); tr.release(); } } catch (Throwable e) { /* ignora */ }
                musicRun = false;
            }
        });
        t.setDaemon(true);
        t.start();
    }

    static void stopMusic() { musicRun = false; }
}
