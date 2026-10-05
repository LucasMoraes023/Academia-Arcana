package br.com.lucasmoraes.arcana;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

final class Particles {
    static final int N = 320;
    final float[] x = new float[N], y = new float[N], vx = new float[N], vy = new float[N], life = new float[N], max = new float[N],
            sz = new float[N], rot = new float[N], vr = new float[N];
    final int[] col = new int[N], kind = new int[N];
    int n;

    void add(float px, float py, float pvx, float pvy, float l, float s, int c, int k) {
        if (n >= N) return;
        int i = n++;
        x[i] = px; y[i] = py; vx[i] = pvx; vy[i] = pvy; life[i] = l; max[i] = l; sz[i] = s; col[i] = c; kind[i] = k;
        rot[i] = Game.rnd.nextFloat() * 6; vr[i] = (Game.rnd.nextFloat() - 0.5f) * 10;
    }

    float q() { return Game.quality == 0 ? 0.4f : Game.quality == 1 ? 0.7f : 1f; }

    void burst(float cx, float cy, int count, int color, float speed) {
        int m = Math.max(1, (int) (count * q()));
        for (int i = 0; i < m; i++) {
            float a = Game.rnd.nextFloat() * 6.2832f, sp = speed * (0.3f + Game.rnd.nextFloat() * 0.7f);
            add(cx, cy, Gfx.cos(a) * sp, Gfx.sin(a) * sp, 0.6f + Game.rnd.nextFloat() * 0.8f, 2 + Game.rnd.nextFloat() * 3, color, Game.rnd.nextInt(3) == 0 ? 1 : 0);
        }
    }

    void confetti(int count) {
        int m = Math.max(1, (int) (count * q()));
        for (int i = 0; i < m; i++)
            add(Game.rnd.nextFloat() * Game.W, -10 - Game.rnd.nextFloat() * 60, (Game.rnd.nextFloat() - 0.5f) * 40, 70 + Game.rnd.nextFloat() * 90,
                    3.5f + Game.rnd.nextFloat() * 2, 3 + Game.rnd.nextFloat() * 3, Gfx.hue(Game.rnd.nextFloat() * 360), 2);
    }

    void embers(int count) {
        int m = Math.max(1, (int) (count * q()));
        for (int i = 0; i < m; i++)
            add(Game.rnd.nextFloat() * Game.W, Game.H + 5, (Game.rnd.nextFloat() - 0.5f) * 30, -(30 + Game.rnd.nextFloat() * 70),
                    2.5f + Game.rnd.nextFloat() * 2, 2 + Game.rnd.nextFloat() * 3, Game.rnd.nextBoolean() ? 0xFFFF5A3D : 0xFF8A1C2B, 3);
    }

    void update(float dt) {
        for (int i = n - 1; i >= 0; i--) {
            life[i] -= dt;
            if (life[i] <= 0) {
                n--;
                if (i != n) {
                    x[i] = x[n]; y[i] = y[n]; vx[i] = vx[n]; vy[i] = vy[n]; life[i] = life[n]; max[i] = max[n]; sz[i] = sz[n];
                    rot[i] = rot[n]; vr[i] = vr[n]; col[i] = col[n]; kind[i] = kind[n];
                }
                continue;
            }
            x[i] += vx[i] * dt; y[i] += vy[i] * dt; rot[i] += vr[i] * dt;
            if (kind[i] <= 1) { vy[i] += 120 * dt; vx[i] *= 0.985f; }
        }
    }

    void draw(Canvas c) {
        for (int i = 0; i < n; i++) {
            float a = Math.min(1f, life[i] / max[i] * 1.6f);
            int col2 = Gfx.alpha(col[i], a);
            switch (kind[i]) {
                case 1: Gfx.sparkle(c, x[i], y[i], sz[i] * 1.6f, col2); break;
                case 2:
                    c.save(); c.translate(x[i], y[i]); c.rotate(rot[i] * 57f);
                    Gfx.R.set(-sz[i], -sz[i] * 0.5f, sz[i], sz[i] * 0.5f);
                    c.drawRect(Gfx.R, Gfx.fill(col2));
                    c.restore();
                    break;
                default: c.drawCircle(x[i], y[i], sz[i] * (kind[i] == 3 ? a : 1f), Gfx.fill(col2));
            }
        }
    }
}

/** Fundo mágico: céu, estrelas, lua, castelo e poeira mágica. */
final class Bg {
    float w, h;
    int top = 0xFF0B0720, bot = 0xFF24124A;
    LinearGradient grad;
    RadialGradient moon;
    final Path castle = new Path();
    final float[] sx = new float[90], sy = new float[90], ss = new float[90], sp = new float[90];
    final float[] dx = new float[30], dy = new float[30], dv = new float[30], dp = new float[30];
    final float[] win = new float[40];
    int nwin;

    Bg() {
        Random r = new Random(5);
        for (int i = 0; i < sx.length; i++) { sx[i] = r.nextFloat(); sy[i] = r.nextFloat() * 0.8f; ss[i] = 0.5f + r.nextFloat() * 1.4f; sp[i] = r.nextFloat() * 6; }
        for (int i = 0; i < dx.length; i++) { dx[i] = r.nextFloat(); dy[i] = r.nextFloat(); dv[i] = 6 + r.nextFloat() * 14; dp[i] = r.nextFloat() * 6; }
    }

    void theme(int t, int b) {
        if (t != top || b != bot) { top = t; bot = b; if (h > 0) grad = new LinearGradient(0, 0, 0, h, top, bot, Shader.TileMode.CLAMP); }
    }

    void resize(float w, float h) {
        this.w = w; this.h = h;
        grad = new LinearGradient(0, 0, 0, h, top, bot, Shader.TileMode.CLAMP);
        moon = new RadialGradient(w * 0.8f, 95, 70, 0x66FFF2C8, 0x00FFF2C8, Shader.TileMode.CLAMP);
        castle.reset();
        castle.addRect(0, h - 58, w, h, Path.Direction.CW);
        float[][] tw = {{0.06f, 34, 118}, {0.28f, 40, 160}, {0.47f, 58, 205}, {0.72f, 38, 148}, {0.9f, 30, 104}};
        nwin = 0;
        for (float[] t : tw) {
            float x = w * t[0], tw2 = t[1], ty = h - t[2];
            castle.addRect(x, ty, x + tw2, h, Path.Direction.CW);
            castle.moveTo(x - 4, ty); castle.lineTo(x + tw2 / 2, ty - 36); castle.lineTo(x + tw2 + 4, ty); castle.close();
            for (int k = 0; k < 3 && nwin < 38; k++) { win[nwin++] = x + tw2 / 2 - 3; win[nwin++] = ty + 18 + k * 32; }
        }
    }

    void draw(Canvas c, float t, float dt, int q) {
        if (grad == null) return;
        c.drawRect(0, 0, w, h, Gfx.shader(grad));
        c.drawCircle(w * 0.8f, 95, 70, Gfx.shader(moon));
        c.drawCircle(w * 0.8f, 95, 17, Gfx.fill(0xFFFFF4D6));
        int ns = q == 0 ? 30 : q == 1 ? 60 : sx.length;
        for (int i = 0; i < ns; i++) {
            float a = 0.35f + 0.65f * (0.5f + 0.5f * Gfx.sin(t * (0.8f + ss[i]) + sp[i]));
            c.drawCircle(sx[i] * w, sy[i] * h, ss[i], Gfx.fill(Gfx.alpha(0xFFFFFFFF, a)));
        }
        c.drawPath(castle, Gfx.fill(0xD00A0618));
        for (int i = 0; i + 1 < nwin; i += 2) {
            float a = 0.45f + 0.4f * Gfx.sin(t * 1.3f + i);
            Gfx.R.set(win[i], win[i + 1], win[i] + 6, win[i + 1] + 10);
            c.drawRect(Gfx.R, Gfx.fill(Gfx.alpha(0xFFFFD27A, a)));
        }
        if (q > 0) {
            int nd = q == 1 ? 14 : dx.length;
            for (int i = 0; i < nd; i++) {
                dy[i] -= dv[i] * dt / h;
                if (dy[i] < -0.02f) dy[i] = 1.02f;
                float px = dx[i] * w + Gfx.sin(t * 0.7f + dp[i]) * 16, py = dy[i] * h;
                c.drawCircle(px, py, 1.3f + (i % 3) * 0.6f, Gfx.fill(Gfx.alpha(i % 2 == 0 ? 0xFFFFE08A : 0xFFB89CFF, 0.55f)));
            }
        }
    }
}

final class GameView extends View {
    final ArrayList<Screen> stack = new ArrayList<>();
    final Particles ps = new Particles();
    final Bg bg = new Bg();
    final ArrayList<String> toasts = new ArrayList<>();
    final ArrayList<Float> toastT = new ArrayList<>();
    static float testDt = 0;   // usado só em testes (0 = tempo real)
    long last;
    float time, u = 1, fade;

    GameView(Context ctx) {
        super(ctx);
        Game.view = this;
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        u = w / 360f;
        Game.W = 360;
        Game.H = h / u;
        bg.resize(Game.W, Game.H);
        if (stack.isEmpty()) go(new TitleScreen());
    }

    void toast(String s) {
        toasts.add(s);
        toastT.add(2.6f);
        while (toasts.size() > 4) { toasts.remove(0); toastT.remove(0); }
    }

    void go(Screen s) { stack.clear(); stack.add(s); fade = 1f; }
    void push(Screen s) { stack.add(s); fade = s.overlay ? 0f : 0.7f; }

    void pop() {
        if (stack.size() > 1) { stack.remove(stack.size() - 1); fade = 0.5f; }
        else go(new TitleScreen());
    }

    boolean back() {
        if (stack.isEmpty()) return false;
        stack.get(stack.size() - 1).onBack();
        return true;
    }

    @Override
    protected void onDraw(Canvas c) {
        long now = System.nanoTime();
        float dt = testDt > 0 ? testDt : last == 0 ? 0.016f : Math.min(0.05f, (now - last) / 1e9f);
        last = now;
        time += dt;
        c.drawColor(0xFF0B0720);
        c.save();
        c.scale(u, u);
        if (!stack.isEmpty()) {
            int base = stack.size() - 1;
            while (base > 0 && stack.get(base).overlay) base--;
            if (stack.get(base).wantsBg()) bg.draw(c, time, dt, Game.quality);
            for (int i = base; i < stack.size(); i++) {
                Screen s = stack.get(i);
                s.draw(c, i == stack.size() - 1 ? dt : 0f);
            }
        }
        ps.update(dt);
        ps.draw(c);
        drawToasts(c, dt);
        if (fade > 0) {
            fade = Math.max(0, fade - dt * 4f);
            c.drawRect(0, 0, Game.W, Game.H, Gfx.fill(Gfx.alpha(0xFF000000, fade)));
        }
        c.restore();
        if (Game.pl != null) {
            while (!Game.pl.notices.isEmpty()) toast(Game.pl.notices.remove(0));
            Screen top = stack.isEmpty() ? null : stack.get(stack.size() - 1);
            if (top != null && !top.overlay && top.allowPopups() && !Game.pl.levelUps.isEmpty()) push(new LevelUpScreen(Game.pl.levelUps.remove(0)));
        }
        if (Game.quality == 0) postInvalidateDelayed(33); else postInvalidateOnAnimation();
    }

    void drawToasts(Canvas c, float dt) {
        for (int i = toasts.size() - 1; i >= 0; i--) {
            float tt = toastT.get(i) - dt;
            if (tt <= 0) { toasts.remove(i); toastT.remove(i); } else toastT.set(i, tt);
        }
        for (int i = 0; i < toasts.size(); i++) {
            String s = toasts.get(i);
            float a = Math.min(1f, toastT.get(i) * 2f), tw = Math.min(Game.W - 24, Gfx.tw(s, 13, true) + 28), x = (Game.W - tw) / 2, y = Game.TOP + 4 + i * 34;
            Gfx.panel(c, x, y, tw, 28, 14, Gfx.alpha(0xFF1A1040, 0.95f * a), Gfx.alpha(Gfx.GOLD, a));
            Gfx.textC(c, s, Game.W / 2, y + 14, 13, Gfx.alpha(0xFFFFFFFF, a), true);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (stack.isEmpty()) return true;
        float x = e.getX() / u, y = e.getY() / u;
        Screen s = stack.get(stack.size() - 1);
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: s.down(x, y); break;
            case MotionEvent.ACTION_MOVE: s.move(x, y); break;
            case MotionEvent.ACTION_UP: s.up(x, y); break;
            case MotionEvent.ACTION_CANCEL: s.cancel(); break;
            default: break;
        }
        return true;
    }
}
