package br.com.lucasmoraes.arcana;

import android.graphics.Canvas;
import android.graphics.RectF;

import java.util.ArrayList;

final class Btn {
    static final int[] FILL = {0xFF5B34B8, 0xFF2A2150, 0xFFA32230, 0xFF1B8454, 0xFFB8860B};
    static final int[] HI = {0xFF7C52E0, 0xFF3D3170, 0xFFD64550, 0xFF2FB070, 0xFFF2C14E};
    static final int[] BORD = {0xFFF2C14E, 0xFF7C6BC4, 0xFFFFA39E, 0xFFB8FFD9, 0xFFFFF2B3};
    final RectF r = new RectF();
    String label;
    int style, icon = -1;
    Runnable fn;
    boolean on = true, down, vis = true;

    Btn(float x, float y, float w, float h, String label, int style, Runnable fn) {
        r.set(x, y, x + w, y + h);
        this.label = label; this.style = style; this.fn = fn;
    }

    void draw(Canvas c) {
        if (!vis) return;
        float h = r.height(), w = r.width(), cx = r.centerX(), cy = r.centerY(), rad = Math.min(14f, h * 0.3f);
        c.save();
        if (down) { c.translate(cx, cy); c.scale(0.96f, 0.96f); c.translate(-cx, -cy); }
        int f = on ? FILL[style] : 0xFF3A3650, hi = on ? HI[style] : 0xFF4A4660, bd = on ? BORD[style] : 0xFF6B6785;
        if (down && on) f = Gfx.mix(f, 0xFFFFFFFF, 0.15f);
        Gfx.R.set(r.left + 1, r.top + 3, r.right + 1, r.bottom + 3);
        c.drawRoundRect(Gfx.R, rad, rad, Gfx.fill(0x66000000));
        c.drawRoundRect(r, rad, rad, Gfx.fill(f));
        Gfx.R.set(r.left + 2, r.top + 2, r.right - 2, r.top + h * 0.5f);
        c.drawRoundRect(Gfx.R, rad * 0.8f, rad * 0.8f, Gfx.fill(Gfx.alpha(hi, 0.55f)));
        c.drawRoundRect(r, rad, rad, Gfx.stroke(bd, 1.6f));
        int tc = !on ? 0xFF9A96B0 : style == 4 ? 0xFF3A2600 : 0xFFFFFFFF;
        float size = Math.min(16f, h * 0.4f), avail = w - 14 - (icon >= 0 ? h * 0.8f : 0);
        float tx = icon >= 0 ? cx + h * 0.3f : cx;
        while (size > 10 && Gfx.tw(label, size, true) > avail) size -= 0.5f;
        if (Gfx.tw(label, size, true) <= avail) {
            Gfx.textC(c, label, tx, cy, size, tc, true);
        } else {
            ArrayList<String> ls = Gfx.wrap(label, size, true, avail);
            float lh = size * 1.15f, y0 = cy - (ls.size() - 1) * lh / 2;
            for (int i = 0; i < ls.size(); i++) Gfx.textC(c, ls.get(i), tx, y0 + i * lh, size, tc, true);
        }
        if (icon >= 0) Gfx.icon(c, icon, r.left + h * 0.5f, cy, h * 0.28f, tc);
        c.restore();
    }
}

final class Scroll {
    float top, bottom, off, vel, contentH, lastY, moved;
    boolean drag;

    void set(float top, float bottom, float contentH) { this.top = top; this.bottom = bottom; this.contentH = contentH; clamp(); }
    float max() { return Math.max(0, contentH - (bottom - top)); }
    void clamp() { off = Math.max(0, Math.min(max(), off)); }
    void down(float y) { drag = true; lastY = y; moved = 0; vel = 0; }

    void move(float y) {
        if (!drag) return;
        float dy = y - lastY;
        lastY = y;
        moved += Math.abs(dy);
        if (moved > 6) { off -= dy; vel = -dy * 55; clamp(); }
    }

    /** @return true se foi um toque (sem arrastar) */
    boolean up() { drag = false; return moved < 8; }

    void update(float dt) {
        if (!drag && Math.abs(vel) > 5) {
            off += vel * dt;
            vel *= (float) Math.pow(0.04, dt);
            clamp();
            if (off <= 0 || off >= max()) vel = 0;
        }
    }

    boolean inside(float y) { return y >= top && y <= bottom; }
    float toContent(float y) { return y - top + off; }

    void begin(Canvas c) {
        c.save();
        c.clipRect(0, top, Game.W, bottom);
        c.translate(0, top - off);
    }

    void end(Canvas c) { c.restore(); }
}

abstract class Screen {
    boolean overlay;
    float t;
    final ArrayList<Btn> btns = new ArrayList<>();
    Btn pressed;

    abstract void draw(Canvas c, float dt);

    void tick(float dt) { t += dt; }
    boolean allowPopups() { return true; }
    boolean wantsBg() { return true; }
    void onBack() { Game.pop(); }

    Btn btn(float x, float y, float w, float h, String label, int style, Runnable fn) {
        Btn b = new Btn(x, y, w, h, label, style, fn);
        btns.add(b);
        return b;
    }

    Btn btnC(float y, float w, float h, String label, int style, Runnable fn) { return btn((Game.W - w) / 2, y, w, h, label, style, fn); }
    void drawBtns(Canvas c) { for (int i = 0; i < btns.size(); i++) btns.get(i).draw(c); }

    void down(float x, float y) {
        pressed = null;
        for (int i = btns.size() - 1; i >= 0; i--) {
            Btn b = btns.get(i);
            if (b.vis && b.on && b.r.contains(x, y)) { pressed = b; b.down = true; break; }
        }
    }

    void move(float x, float y) {
        if (pressed != null && !pressed.r.contains(x, y)) { pressed.down = false; pressed = null; }
    }

    void up(float x, float y) {
        Btn b = pressed;
        pressed = null;
        if (b != null) {
            b.down = false;
            if (b.r.contains(x, y) && b.on) { Audio.sfx(Audio.CLICK); b.fn.run(); }
        }
    }

    void cancel() {
        if (pressed != null) pressed.down = false;
        pressed = null;
    }

    /** Título padrão no topo das telas. */
    void header(Canvas c, String title) {
        Gfx.textS(c, title, Game.W / 2, Game.TOP + 26, 22, Gfx.GOLD, 1, true);
        c.drawLine(40, Game.TOP + 36, Game.W - 40, Game.TOP + 36, Gfx.stroke(Gfx.alpha(Gfx.GOLD, 0.5f), 1.5f));
    }
}
