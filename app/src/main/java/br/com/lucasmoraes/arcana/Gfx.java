package br.com.lucasmoraes.arcana;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;

import java.util.ArrayList;

/** Utilitários de desenho 100% em código (Canvas): textos, painéis, ícones, cartas, monstros e baús. */
final class Gfx {
    static final int TXT = 0xFFEDE6FF, DIM = 0xFF9C93C4, GOLD = 0xFFF2C14E, RED = 0xFFE5484D, GREEN = 0xFF3DD68C, BLUE = 0xFF4DA3FF;
    static final Paint P = new Paint(Paint.ANTI_ALIAS_FLAG), TP = new Paint(Paint.ANTI_ALIAS_FLAG);
    static final RectF R = new RectF();
    static final Path PA = new Path(), PB = new Path();
    static final Typeface SB = Typeface.create(Typeface.SERIF, Typeface.BOLD), SN = Typeface.create(Typeface.SERIF, Typeface.NORMAL);

    // ---------- tintas ----------
    static Paint fill(int c) { P.reset(); P.setAntiAlias(true); P.setStyle(Paint.Style.FILL); P.setColor(c); return P; }

    static Paint stroke(int c, float w) {
        P.reset(); P.setAntiAlias(true); P.setStyle(Paint.Style.STROKE); P.setStrokeWidth(w);
        P.setStrokeCap(Paint.Cap.ROUND); P.setStrokeJoin(Paint.Join.ROUND); P.setColor(c); return P;
    }

    static Paint shader(Shader s) { P.reset(); P.setAntiAlias(true); P.setStyle(Paint.Style.FILL); P.setShader(s); return P; }

    static int alpha(int c, float a) {
        int al = Math.max(0, Math.min(255, (int) (((c >>> 24) & 0xFF) * a)));
        return (al << 24) | (c & 0x00FFFFFF);
    }

    static int mix(int a, int b, float t) {
        int r = (int) (((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t), g = (int) (((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t),
                bl = (int) ((a & 255) * (1 - t) + (b & 255) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }

    static int dark(int c, float f) { return mix(c, 0xFF000000, 1 - f); }
    static int hue(float h) { return Color.HSVToColor(new float[]{((h % 360f) + 360f) % 360f, 0.62f, 1f}); }
    static float sin(float x) { return (float) Math.sin(x); }
    static float cos(float x) { return (float) Math.cos(x); }

    // ---------- texto ----------
    static void tp(float size, int color, boolean bold, int align) {
        TP.reset(); TP.setAntiAlias(true); TP.setTextSize(size); TP.setColor(color); TP.setTypeface(bold ? SB : SN);
        TP.setTextAlign(align == 0 ? Paint.Align.LEFT : align == 1 ? Paint.Align.CENTER : Paint.Align.RIGHT);
    }

    static void text(Canvas c, String s, float x, float y, float size, int color, int align, boolean bold) {
        tp(size, color, bold, align);
        c.drawText(s, x, y, TP);
    }

    static void textS(Canvas c, String s, float x, float y, float size, int color, int align, boolean bold) {
        text(c, s, x + size * 0.06f, y + size * 0.08f, size, 0xAA000000, align, bold);
        text(c, s, x, y, size, color, align, bold);
    }

    static void textC(Canvas c, String s, float cx, float cy, float size, int color, boolean bold) {
        tp(size, color, bold, 1);
        float yb = cy - (TP.ascent() + TP.descent()) / 2f;
        c.drawText(s, cx, yb, TP);
    }

    static float tw(String s, float size, boolean bold) { tp(size, 0, bold, 0); return TP.measureText(s); }

    static ArrayList<String> wrap(String s, float size, boolean bold, float maxW) {
        ArrayList<String> out = new ArrayList<>();
        tp(size, 0, bold, 0);
        for (String para : s.split("\n")) {
            StringBuilder line = new StringBuilder();
            for (String w : para.split(" ")) {
                String t = line.length() == 0 ? w : line + " " + w;
                if (line.length() == 0 || TP.measureText(t) <= maxW) { line.setLength(0); line.append(t); }
                else { out.add(line.toString()); line.setLength(0); line.append(w); }
            }
            out.add(line.toString());
        }
        return out;
    }

    /** Bloco de texto com quebra de linha; (x,y) = canto superior esquerdo. Retorna a altura usada. */
    static float para(Canvas c, String s, float x, float y, float maxW, float size, int color, float lh, int align, boolean bold) {
        ArrayList<String> ls = wrap(s, size, bold, maxW);
        float yy = y + size;
        for (String l : ls) {
            text(c, l, align == 0 ? x : align == 1 ? x + maxW / 2 : x + maxW, yy, size, color, align, bold);
            yy += lh;
        }
        return ls.size() * lh;
    }

    static float paraH(String s, float size, float maxW, float lh, boolean bold) { return wrap(s, size, bold, maxW).size() * lh; }

    // ---------- formas básicas ----------
    static void panel(Canvas c, float x, float y, float w, float h, float r, int fill, int border) {
        R.set(x, y, x + w, y + h);
        c.drawRoundRect(R, r, r, fill(fill));
        if (border != 0) { R.set(x + 0.75f, y + 0.75f, x + w - 0.75f, y + h - 0.75f); c.drawRoundRect(R, r, r, stroke(border, 1.5f)); }
    }

    static void bar(Canvas c, float x, float y, float w, float h, float f, int c1, int c2) {
        f = Math.max(0, Math.min(1, f));
        R.set(x, y, x + w, y + h);
        c.drawRoundRect(R, h / 2, h / 2, fill(0xAA000000));
        if (f > 0.001f) {
            float fw = Math.max(h, w * f);
            R.set(x, y, x + fw, y + h);
            c.drawRoundRect(R, h / 2, h / 2, fill(c1));
            R.set(x + 1.5f, y + 1, x + fw - 1.5f, y + h * 0.45f);
            c.drawRoundRect(R, h / 4, h / 4, fill(c2));
        }
        R.set(x, y, x + w, y + h);
        c.drawRoundRect(R, h / 2, h / 2, stroke(0x66FFFFFF, 1f));
    }

    static void glow(Canvas c, float cx, float cy, float r, int color, float a) {
        c.drawCircle(cx, cy, r, shader(new RadialGradient(cx, cy, r, alpha(color, a), alpha(color, 0f), Shader.TileMode.CLAMP)));
    }

    static void star(Canvas c, float cx, float cy, float r, int color) {
        PA.reset();
        for (int i = 0; i < 10; i++) {
            float ang = (float) (-Math.PI / 2 + i * Math.PI / 5), rr = i % 2 == 0 ? r : r * 0.42f;
            float px = cx + cos(ang) * rr, py = cy + sin(ang) * rr;
            if (i == 0) PA.moveTo(px, py); else PA.lineTo(px, py);
        }
        PA.close();
        c.drawPath(PA, fill(color));
    }

    static void sparkle(Canvas c, float cx, float cy, float r, int color) {
        PA.reset();
        PA.moveTo(cx, cy - r); PA.quadTo(cx, cy, cx + r, cy); PA.quadTo(cx, cy, cx, cy + r);
        PA.quadTo(cx, cy, cx - r, cy); PA.quadTo(cx, cy, cx, cy - r); PA.close();
        c.drawPath(PA, fill(color));
    }

    static void lock(Canvas c, float cx, float cy, float s, int col) {
        R.set(cx - s * 0.7f, cy - s * 0.1f, cx + s * 0.7f, cy + s * 0.8f);
        c.drawRoundRect(R, s * 0.18f, s * 0.18f, fill(col));
        R.set(cx - s * 0.45f, cy - s * 0.85f, cx + s * 0.45f, cy + s * 0.25f);
        c.drawArc(R, 180, 180, false, stroke(col, s * 0.2f));
    }

    static void check(Canvas c, float cx, float cy, float s, int col) {
        PA.reset();
        PA.moveTo(cx - s * 0.6f, cy); PA.lineTo(cx - s * 0.15f, cy + s * 0.5f); PA.lineTo(cx + s * 0.65f, cy - s * 0.5f);
        c.drawPath(PA, stroke(col, s * 0.3f));
    }

    static void coin(Canvas c, float cx, float cy, float r) {
        c.drawCircle(cx, cy, r, fill(0xFFB8860B));
        c.drawCircle(cx, cy, r * 0.8f, fill(GOLD));
        textC(c, "$", cx, cy, r * 1.15f, 0xFF7A5200, true);
    }

    static void bolt(Canvas c, float cx, float cy, float r, int col) {
        PA.reset();
        PA.moveTo(cx + r * 0.2f, cy - r); PA.lineTo(cx - r * 0.6f, cy + r * 0.1f); PA.lineTo(cx - r * 0.05f, cy + r * 0.1f);
        PA.lineTo(cx - r * 0.25f, cy + r); PA.lineTo(cx + r * 0.65f, cy - r * 0.2f); PA.lineTo(cx + r * 0.1f, cy - r * 0.2f); PA.close();
        c.drawPath(PA, fill(col));
    }

    static void heart(Canvas c, float cx, float cy, float r, int col) {
        PA.reset();
        PA.moveTo(cx, cy + r * 0.8f); PA.cubicTo(cx - r * 1.3f, cy - r * 0.1f, cx - r * 0.6f, cy - r * 0.95f, cx, cy - r * 0.3f);
        PA.cubicTo(cx + r * 0.6f, cy - r * 0.95f, cx + r * 1.3f, cy - r * 0.1f, cx, cy + r * 0.8f); PA.close();
        c.drawPath(PA, fill(col));
    }

    // ---------- runas e emblemas ----------
    /** 0 chama, 1 onda, 2 folha, 3 lua, 4 sol, 5 estrela */
    static void glyph(Canvas c, int id, float cx, float cy, float r, int col) {
        switch (id) {
            case 0:
                PA.reset();
                PA.moveTo(cx, cy - r); PA.cubicTo(cx + r * 0.2f, cy - r * 0.5f, cx + r * 0.95f, cy - r * 0.1f, cx + r * 0.6f, cy + r * 0.5f);
                PA.cubicTo(cx + r * 0.45f, cy + r * 0.95f, cx - r * 0.45f, cy + r * 0.95f, cx - r * 0.6f, cy + r * 0.5f);
                PA.cubicTo(cx - r * 0.95f, cy - r * 0.1f, cx - r * 0.2f, cy - r * 0.4f, cx, cy - r); PA.close();
                c.drawPath(PA, fill(col));
                PA.reset();
                PA.moveTo(cx, cy - r * 0.1f); PA.cubicTo(cx + r * 0.3f, cy + r * 0.2f, cx + r * 0.35f, cy + r * 0.6f, cx, cy + r * 0.75f);
                PA.cubicTo(cx - r * 0.35f, cy + r * 0.6f, cx - r * 0.3f, cy + r * 0.2f, cx, cy - r * 0.1f); PA.close();
                c.drawPath(PA, fill(0xAAFFF2B3));
                break;
            case 1:
                for (int i = -1; i <= 1; i++) {
                    float yy = cy + i * r * 0.5f;
                    PA.reset();
                    PA.moveTo(cx - r, yy); PA.quadTo(cx - r * 0.5f, yy - r * 0.45f, cx, yy); PA.quadTo(cx + r * 0.5f, yy + r * 0.45f, cx + r, yy);
                    c.drawPath(PA, stroke(col, r * 0.22f));
                }
                break;
            case 2:
                PA.reset();
                PA.moveTo(cx - r * 0.8f, cy + r * 0.8f); PA.quadTo(cx - r * 0.95f, cy - r * 0.75f, cx + r * 0.85f, cy - r * 0.85f);
                PA.quadTo(cx + r * 0.95f, cy + r * 0.7f, cx - r * 0.8f, cy + r * 0.8f); PA.close();
                c.drawPath(PA, fill(col));
                c.drawLine(cx - r * 0.75f, cy + r * 0.75f, cx + r * 0.45f, cy - r * 0.45f, stroke(0x88000000, r * 0.1f));
                break;
            case 3:
                PA.reset(); PA.addCircle(cx, cy, r, Path.Direction.CW);
                PB.reset(); PB.addCircle(cx + r * 0.5f, cy - r * 0.2f, r * 0.82f, Path.Direction.CW);
                PA.op(PB, Path.Op.DIFFERENCE);
                c.drawPath(PA, fill(col));
                break;
            case 4:
                c.drawCircle(cx, cy, r * 0.5f, fill(col));
                for (int i = 0; i < 8; i++) {
                    float a = (float) (i * Math.PI / 4);
                    c.drawLine(cx + cos(a) * r * 0.68f, cy + sin(a) * r * 0.68f, cx + cos(a) * r, cy + sin(a) * r, stroke(col, r * 0.14f));
                }
                break;
            default:
                star(c, cx, cy, r, col);
        }
    }

    static void houseEmblem(Canvas c, int h, float cx, float cy, float r, float t) {
        int col = Data.HCOL[Math.max(0, h)];
        glow(c, cx, cy, r * 1.7f, col, 0.45f + 0.1f * sin(t * 2));
        c.drawCircle(cx, cy, r, fill(0xFF150E30));
        c.drawCircle(cx, cy, r, stroke(col, Math.max(1.5f, r * 0.08f)));
        c.drawCircle(cx, cy, r * 0.86f, stroke(alpha(col, 0.5f), 1f));
        glyph(c, Math.max(0, h), cx, cy, r * 0.58f, col);
    }

    /** ícones simples dos menus */
    static void icon(Canvas c, int id, float cx, float cy, float r, int col) {
        switch (id) {
            case 0: glyph(c, 4, cx, cy, r, col); break;
            case 1: catIcon(c, 5, cx, cy, r, col); break;
            case 2:
                R.set(cx - r * 0.85f, cy - r * 0.6f, cx + r * 0.2f, cy + r * 0.9f); c.drawRoundRect(R, 3, 3, fill(alpha(col, 0.6f)));
                R.set(cx - r * 0.2f, cy - r * 0.9f, cx + r * 0.85f, cy + r * 0.6f); c.drawRoundRect(R, 3, 3, fill(col));
                break;
            case 3:
                for (int i = 2; i >= 0; i--) { R.set(cx - r * 0.7f + i * r * 0.2f, cy - r * 0.8f + i * r * 0.2f, cx + r * 0.5f + i * r * 0.2f, cy + r * 0.4f + i * r * 0.2f); c.drawRoundRect(R, 3, 3, fill(alpha(col, 0.45f + 0.25f * (2 - i)))); }
                break;
            case 4:
                R.set(cx - r * 0.55f, cy - r * 0.8f, cx + r * 0.55f, cy + r * 0.8f); c.drawRoundRect(R, 4, 4, fill(col));
                R.set(cx - r * 0.8f, cy - r * 0.9f, cx + r * 0.8f, cy - r * 0.6f); c.drawRoundRect(R, r * 0.15f, r * 0.15f, fill(alpha(col, 0.7f)));
                for (int i = 0; i < 3; i++) c.drawLine(cx - r * 0.3f, cy - r * 0.25f + i * r * 0.35f, cx + r * 0.3f, cy - r * 0.25f + i * r * 0.35f, stroke(0x88000000, 1.5f));
                break;
            case 5:
                c.drawCircle(cx, cy + r * 0.25f, r * 0.7f, fill(col));
                R.set(cx - r * 0.35f, cy - r * 0.85f, cx + r * 0.35f, cy - r * 0.2f); c.drawRoundRect(R, 3, 3, fill(alpha(col, 0.8f)));
                break;
            case 6: chest(c, 1, cx, cy + r * 0.1f, r / 55f, 0, 0); break;
            case 7:
                PA.reset(); PA.moveTo(cx - r * 0.6f, cy - r * 0.8f); PA.lineTo(cx + r * 0.6f, cy - r * 0.8f); PA.quadTo(cx + r * 0.6f, cy + r * 0.3f, cx, cy + r * 0.35f);
                PA.quadTo(cx - r * 0.6f, cy + r * 0.3f, cx - r * 0.6f, cy - r * 0.8f); PA.close(); c.drawPath(PA, fill(col));
                c.drawLine(cx, cy + r * 0.3f, cx, cy + r * 0.7f, stroke(col, r * 0.2f));
                R.set(cx - r * 0.4f, cy + r * 0.65f, cx + r * 0.4f, cy + r * 0.9f); c.drawRect(R, fill(col));
                break;
            case 8:
                PA.reset(); PA.moveTo(cx - r * 0.85f, cy + r * 0.6f); PA.lineTo(cx - r * 0.85f, cy - r * 0.5f); PA.lineTo(cx - r * 0.4f, cy);
                PA.lineTo(cx, cy - r * 0.7f); PA.lineTo(cx + r * 0.4f, cy); PA.lineTo(cx + r * 0.85f, cy - r * 0.5f); PA.lineTo(cx + r * 0.85f, cy + r * 0.6f); PA.close();
                c.drawPath(PA, fill(col));
                break;
            case 9:
                for (int i = 0; i < 8; i++) { float a = (float) (i * Math.PI / 4); c.drawLine(cx + cos(a) * r * 0.55f, cy + sin(a) * r * 0.55f, cx + cos(a) * r * 0.95f, cy + sin(a) * r * 0.95f, stroke(col, r * 0.3f)); }
                c.drawCircle(cx, cy, r * 0.62f, fill(col));
                c.drawCircle(cx, cy, r * 0.25f, fill(0xFF150E30));
                break;
            default:
                R.set(cx - r * 0.5f, cy - r * 0.85f, cx + r * 0.3f, cy + r * 0.85f); c.drawRoundRect(R, 3, 3, stroke(col, r * 0.2f));
                c.drawLine(cx - r * 0.1f, cy, cx + r * 0.85f, cy, stroke(col, r * 0.2f));
                c.drawLine(cx + r * 0.55f, cy - r * 0.3f, cx + r * 0.85f, cy, stroke(col, r * 0.2f));
                c.drawLine(cx + r * 0.55f, cy + r * 0.3f, cx + r * 0.85f, cy, stroke(col, r * 0.2f));
        }
    }

    static void catIcon(Canvas c, int cat, float cx, float cy, float r, int col) {
        switch (cat) {
            case 0:
                c.drawLine(cx - r * 0.8f, cy + r * 0.8f, cx + r * 0.3f, cy - r * 0.3f, stroke(col, r * 0.2f));
                sparkle(c, cx + r * 0.45f, cy - r * 0.45f, r * 0.6f, col);
                sparkle(c, cx - r * 0.4f, cy - r * 0.5f, r * 0.25f, col);
                break;
            case 1:
                R.set(cx - r * 0.45f, cy - r * 0.05f, cx + r * 0.45f, cy + r * 0.7f); c.drawOval(R, fill(col));
                c.drawCircle(cx - r * 0.62f, cy - r * 0.2f, r * 0.2f, fill(col)); c.drawCircle(cx - r * 0.22f, cy - r * 0.58f, r * 0.2f, fill(col));
                c.drawCircle(cx + r * 0.22f, cy - r * 0.58f, r * 0.2f, fill(col)); c.drawCircle(cx + r * 0.62f, cy - r * 0.2f, r * 0.2f, fill(col));
                break;
            case 2:
                PA.reset();
                PA.moveTo(cx - r * 0.6f, cy - r * 0.3f); PA.lineTo(cx - r * 0.3f, cy - r * 0.8f); PA.lineTo(cx + r * 0.3f, cy - r * 0.8f);
                PA.lineTo(cx + r * 0.6f, cy - r * 0.3f); PA.lineTo(cx, cy + r * 0.85f); PA.close();
                c.drawPath(PA, fill(col));
                c.drawLine(cx - r * 0.6f, cy - r * 0.3f, cx + r * 0.6f, cy - r * 0.3f, stroke(0x55000000, r * 0.08f));
                c.drawLine(cx - r * 0.3f, cy - r * 0.8f, cx - r * 0.15f, cy - r * 0.3f, stroke(0x55000000, r * 0.08f));
                c.drawLine(cx + r * 0.3f, cy - r * 0.8f, cx + r * 0.15f, cy - r * 0.3f, stroke(0x55000000, r * 0.08f));
                break;
            case 3:
                c.drawCircle(cx, cy + r * 0.25f, r * 0.6f, fill(col));
                R.set(cx - r * 0.2f, cy - r * 0.7f, cx + r * 0.2f, cy - r * 0.1f); c.drawRect(R, fill(col));
                R.set(cx - r * 0.3f, cy - r * 0.85f, cx + r * 0.3f, cy - r * 0.65f); c.drawRoundRect(R, 3, 3, fill(0xFFB8860B));
                c.drawCircle(cx - r * 0.2f, cy + r * 0.15f, r * 0.15f, fill(0x66FFFFFF));
                break;
            case 4:
                R.set(cx - r * 0.5f, cy - r * 0.4f, cx + r * 0.5f, cy + r * 0.85f); c.drawRect(R, fill(col));
                for (int i = 0; i < 3; i++) { float bx = cx - r * 0.5f + i * r * 0.4f; R.set(bx, cy - r * 0.8f, bx + r * 0.2f, cy - r * 0.4f); c.drawRect(R, fill(col)); }
                R.set(cx - r * 0.15f, cy + r * 0.25f, cx + r * 0.15f, cy + r * 0.85f); c.drawRoundRect(R, r * 0.15f, r * 0.15f, fill(0x88000000));
                break;
            case 5:
                c.drawLine(cx - r * 0.6f, cy + r * 0.6f, cx + r * 0.75f, cy - r * 0.75f, stroke(col, r * 0.22f));
                c.drawLine(cx - r * 0.6f, cy + r * 0.05f, cx - r * 0.05f, cy + r * 0.6f, stroke(col, r * 0.2f));
                c.drawLine(cx - r * 0.85f, cy + r * 0.85f, cx - r * 0.55f, cy + r * 0.55f, stroke(col, r * 0.25f));
                break;
            case 6:
                PA.reset();
                PA.moveTo(cx - r * 0.7f, cy - r * 0.75f); PA.lineTo(cx + r * 0.7f, cy - r * 0.75f); PA.lineTo(cx + r * 0.7f, cy + r * 0.05f);
                PA.quadTo(cx + r * 0.6f, cy + r * 0.6f, cx, cy + r * 0.95f); PA.quadTo(cx - r * 0.6f, cy + r * 0.6f, cx - r * 0.7f, cy + r * 0.05f); PA.close();
                c.drawPath(PA, fill(col));
                c.drawLine(cx, cy - r * 0.6f, cx, cy + r * 0.6f, stroke(0x55000000, r * 0.1f));
                break;
            default:
                heart(c, cx, cy, r, col);
        }
    }

    // ---------- cartas ----------
    static void badge(Canvas c, float cx, float cy, int col, String v) {
        c.drawCircle(cx, cy, 10, fill(0xFF0B0820));
        c.drawCircle(cx, cy, 10, stroke(col, 1.8f));
        textC(c, v, cx, cy, v.length() > 2 ? 7.5f : 10, 0xFFFFFFFF, true);
    }

    static void shimmer(Canvas c, float t, Card d) {
        c.save();
        R.set(3, 3, 97, 137);
        PA.reset(); PA.addRoundRect(R, 7, 7, Path.Direction.CW);
        c.clipPath(PA);
        float p = ((t * 0.55f + d.id * 0.17f) % 1.8f) * 190f - 60f;
        PB.reset(); PB.moveTo(p, 0); PB.lineTo(p + 22, 0); PB.lineTo(p - 18, 140); PB.lineTo(p - 40, 140); PB.close();
        c.drawPath(PB, fill(d.rar == 5 ? 0x44FFFFFF : 0x33FFFFFF));
        if (d.rar == 5) {
            for (int i = 0; i < 3; i++) {
                float q = ((t * 0.35f + i * 0.33f) % 1f) * 170 - 20;
                PB.reset(); PB.moveTo(q, 0); PB.lineTo(q + 10, 0); PB.lineTo(q - 30, 140); PB.lineTo(q - 40, 140); PB.close();
                c.drawPath(PB, fill(alpha(hue(t * 80 + i * 120), 0.2f)));
            }
        }
        c.restore();
        for (int i = 0; i < (d.rar == 5 ? 6 : 3); i++) {
            float a2 = sin(t * 2.2f + i * 1.7f + d.id);
            if (a2 > 0.2f) sparkle(c, i % 2 == 0 ? 4 : 96, 20 + ((i * 41 + d.id * 11) % 100), 4f * a2, 0xFFFFFFFF);
        }
    }

    static void card(Canvas c, Card d, float x, float y, float w, int stars, boolean locked, float t, float dim) {
        float s = w / 100f;
        c.save();
        c.translate(x, y);
        c.scale(s, s);
        int rc = d.rar == 5 ? hue(t * 70f + d.id * 13f) : Data.RARC[d.rar];
        if (!locked && d.rar >= 4) glow(c, 50, 70, d.rar == 5 ? 88 : 72, rc, 0.28f + 0.12f * sin(t * 3 + d.id));
        R.set(0, 0, 100, 140);
        c.drawRoundRect(R, 9, 9, fill(locked ? alpha(rc, 0.35f) : rc));
        R.set(3, 3, 97, 137);
        c.drawRoundRect(R, 7, 7, fill(locked ? 0xFF0E0A1D : 0xFF181236));
        if (locked) {
            textC(c, "?", 50, 60, 44, 0x44FFFFFF, true);
            lock(c, 50, 102, 10, 0x66FFFFFF);
            text(c, Data.RARN[d.rar], 50, 128, 7f, alpha(rc, 0.6f), 1, true);
        } else {
            int cc = Data.CATC[d.cat];
            R.set(7, 19, 93, 80);
            c.drawRoundRect(R, 5, 5, fill(dark(cc, 0.42f)));
            glow(c, 50, 50, 36, cc, 0.6f);
            catIcon(c, d.cat, 50, 50, 21, 0xFFFFFFFF);
            for (int i = 0; i < 3; i++) {
                float px = 14 + ((d.id * 37 + i * 29) % 70), py = 24 + ((d.id * 53 + i * 17) % 50);
                sparkle(c, px, py, 2.2f, alpha(0xFFFFFFFF, 0.5f + 0.5f * sin(t * 2 + i + d.id)));
            }
            float fs = 9.5f;
            while (fs > 6 && tw(d.name, fs, true) > 88) fs -= 0.5f;
            textC(c, d.name, 50, 11.5f, fs, 0xFFFFFFFF, true);
            R.set(9, 21, 31, 31);
            c.drawRoundRect(R, 4, 4, fill(0xAA000000));
            textC(c, "Nv." + d.lvl, 20, 26.2f, 6.2f, GOLD, true);
            for (int i = 0; i < stars; i++) star(c, 13 + i * 7.5f, 74, 3.2f, GOLD);
            text(c, Data.CAT[d.cat], 50, 90, 6.5f, DIM, 1, true);
            text(c, Data.RARN[d.rar], 50, 99, 7f, rc, 1, true);
            float m = Player.starMult(stars);
            badge(c, 22, 118, 0xFFE5484D, String.valueOf(Math.round(d.atk * m)));
            badge(c, 50, 118, 0xFF3D9BFF, String.valueOf(Math.round(d.def * m)));
            badge(c, 78, 118, GOLD, String.valueOf(d.en));
            if (d.rar >= 4) shimmer(c, t, d);
        }
        if (dim > 0) { R.set(0, 0, 100, 140); c.drawRoundRect(R, 9, 9, fill(alpha(0xFF000000, dim))); }
        c.restore();
    }

    static void cardBack(Canvas c, float x, float y, float w, int rar, float t) {
        float s = w / 100f;
        int rc = rar == 5 ? hue(t * 70f) : Data.RARC[rar];
        c.save();
        c.translate(x, y);
        c.scale(s, s);
        R.set(0, 0, 100, 140); c.drawRoundRect(R, 9, 9, fill(rc));
        R.set(3, 3, 97, 137); c.drawRoundRect(R, 7, 7, fill(0xFF1A1245));
        R.set(10, 10, 90, 130); c.drawRoundRect(R, 5, 5, stroke(alpha(rc, 0.7f), 1.5f));
        glow(c, 50, 70, 40, rc, 0.5f);
        star(c, 50, 70, 24, rc);
        c.drawCircle(50, 70, 32, stroke(alpha(rc, 0.7f), 1.5f));
        c.restore();
    }

    // ---------- monstros ----------
    static void eyes(Canvas c, float x, float y, float s, int n, int col) {
        for (int i = 0; i < n; i++) {
            float ex = x + (i - (n - 1) / 2f) * 24 * s;
            R.set(ex - 9 * s, y - 10 * s, ex + 9 * s, y + 10 * s); c.drawOval(R, fill(col));
            c.drawCircle(ex + 1.5f * s, y + 1 * s, 4.5f * s, fill(0xFF140A28));
        }
    }

    static void enemy(Canvas c, Enemy e, float cx, float cy, float s, float t, float hurt) {
        float bob = sin(t * 2.2f) * 4 * s;
        float x = cx + (hurt > 0 ? sin(t * 60) * 6 * hurt * s : 0), y = cy + bob;
        R.set(cx - 48 * s, cy + 62 * s, cx + 48 * s, cy + 76 * s);
        c.drawOval(R, fill(0x55000000));
        int col = e.color, dk = dark(col, 0.5f), lt = mix(col, 0xFFFFFFFF, 0.4f);
        if (e.boss) glow(c, x, y, 130 * s, 0xFFB00020, 0.35f + 0.1f * sin(t * 3));
        switch (e.kind) {
            case 0:
                PA.reset();
                PA.moveTo(x - 46 * s, y + 50 * s); PA.cubicTo(x - 62 * s, y - 56 * s, x + 62 * s, y - 56 * s, x + 46 * s, y + 50 * s);
                for (int i = 0; i < 4; i++) { float bx = x + 46 * s - (i + 1) * 23 * s; PA.quadTo(bx + 11.5f * s, y + (i % 2 == 0 ? 70 : 40) * s, bx, y + 50 * s); }
                PA.close();
                c.drawPath(PA, fill(col));
                R.set(x - 30 * s, y - 14 * s, x + 30 * s, y + 38 * s); c.drawOval(R, fill(alpha(lt, 0.3f)));
                eyes(c, x, y - 8 * s, s, 2, 0xFFFFFFFF);
                c.drawLine(x - 16 * s, y + 24 * s, x - 6 * s, y + 30 * s, stroke(dk, 3 * s)); c.drawLine(x - 6 * s, y + 30 * s, x + 6 * s, y + 24 * s, stroke(dk, 3 * s)); c.drawLine(x + 6 * s, y + 24 * s, x + 16 * s, y + 30 * s, stroke(dk, 3 * s));
                break;
            case 1:
                R.set(x - 50 * s, y - 30 * s, x + 50 * s, y + 55 * s); c.drawOval(R, fill(col));
                PA.reset(); PA.moveTo(x - 44 * s, y - 14 * s); PA.lineTo(x - 36 * s, y - 62 * s); PA.lineTo(x - 12 * s, y - 28 * s); PA.close(); c.drawPath(PA, fill(dk));
                PA.reset(); PA.moveTo(x + 44 * s, y - 14 * s); PA.lineTo(x + 36 * s, y - 62 * s); PA.lineTo(x + 12 * s, y - 28 * s); PA.close(); c.drawPath(PA, fill(dk));
                R.set(x - 24 * s, y + 8 * s, x + 24 * s, y + 46 * s); c.drawOval(R, fill(lt));
                eyes(c, x, y - 6 * s, s, 2, 0xFFFFE08A);
                c.drawCircle(x, y + 16 * s, 5 * s, fill(0xFF140A28));
                PA.reset(); PA.moveTo(x - 12 * s, y + 30 * s); PA.lineTo(x - 7 * s, y + 42 * s); PA.lineTo(x - 2 * s, y + 30 * s); PA.close(); c.drawPath(PA, fill(0xFFFFFFFF));
                PA.reset(); PA.moveTo(x + 12 * s, y + 30 * s); PA.lineTo(x + 7 * s, y + 42 * s); PA.lineTo(x + 2 * s, y + 30 * s); PA.close(); c.drawPath(PA, fill(0xFFFFFFFF));
                break;
            case 2:
                PA.reset(); PA.moveTo(x, y - 62 * s); PA.quadTo(x + 40 * s, y - 40 * s, x + 56 * s, y + 56 * s); PA.lineTo(x - 56 * s, y + 56 * s); PA.quadTo(x - 40 * s, y - 40 * s, x, y - 62 * s); PA.close();
                c.drawPath(PA, fill(col));
                R.set(x - 24 * s, y - 36 * s, x + 24 * s, y + 8 * s); c.drawOval(R, fill(0xFF0A0614));
                eyes(c, x, y - 14 * s, s * 0.6f, 2, 0xFFFFE08A);
                PA.reset(); PA.moveTo(x - 56 * s, y + 56 * s); PA.quadTo(x, y + 44 * s, x + 56 * s, y + 56 * s); c.drawPath(PA, stroke(lt, 3 * s));
                break;
            case 3:
                R.set(x - 40 * s, y - 6 * s, x + 40 * s, y + 58 * s); c.drawRoundRect(R, 8 * s, 8 * s, fill(col));
                R.set(x - 28 * s, y - 52 * s, x + 28 * s, y - 6 * s); c.drawRoundRect(R, 8 * s, 8 * s, fill(lt));
                R.set(x - 62 * s, y, x - 40 * s, y + 40 * s); c.drawRoundRect(R, 6 * s, 6 * s, fill(dk));
                R.set(x + 40 * s, y, x + 62 * s, y + 40 * s); c.drawRoundRect(R, 6 * s, 6 * s, fill(dk));
                R.set(x - 16 * s, y - 36 * s, x - 4 * s, y - 26 * s); c.drawRect(R, fill(0xFF8EF3FF));
                R.set(x + 4 * s, y - 36 * s, x + 16 * s, y - 26 * s); c.drawRect(R, fill(0xFF8EF3FF));
                c.drawLine(x - 10 * s, y + 8 * s, x + 4 * s, y + 24 * s, stroke(dk, 2 * s)); c.drawLine(x + 4 * s, y + 24 * s, x - 6 * s, y + 40 * s, stroke(dk, 2 * s));
                break;
            default:
                R.set(x - 56 * s, y - 40 * s, x + 56 * s, y + 62 * s); c.drawOval(R, fill(col));
                PA.reset(); PA.moveTo(x - 40 * s, y - 28 * s); PA.quadTo(x - 70 * s, y - 70 * s, x - 36 * s, y - 92 * s); PA.quadTo(x - 44 * s, y - 60 * s, x - 22 * s, y - 40 * s); PA.close(); c.drawPath(PA, fill(lt));
                PA.reset(); PA.moveTo(x + 40 * s, y - 28 * s); PA.quadTo(x + 70 * s, y - 70 * s, x + 36 * s, y - 92 * s); PA.quadTo(x + 44 * s, y - 60 * s, x + 22 * s, y - 40 * s); PA.close(); c.drawPath(PA, fill(lt));
                eyes(c, x, y - 10 * s, s * 0.75f, 3, 0xFFFF4D4D);
                PA.reset(); PA.moveTo(x - 30 * s, y + 24 * s);
                for (int i = 0; i < 7; i++) PA.lineTo(x - 30 * s + (i + 1) * 60 * s / 7, y + (i % 2 == 0 ? 42 : 24) * s);
                c.drawPath(PA, stroke(0xFFFFFFFF, 3 * s));
        }
        if (hurt > 0) c.drawCircle(x, y, 62 * s, fill(alpha(0xFFFFFFFF, 0.45f * hurt)));
    }

    // ---------- baús ----------
    static void chest(Canvas c, int tier, float cx, float cy, float s, float open, float t) {
        int[] body = {0xFF8D5A2B, 0xFF5F7A99, 0xFF7B3FD4, 0xFFD9A441, 0xFFFF4D8D};
        int[] trim = {0xFFD9A441, 0xFFC0D4EA, 0xFFE0C8FF, 0xFFFFF2B3, 0xFFFFFFFF};
        int bc = tier == 4 ? hue(t * 90f) : body[tier], tc = trim[tier];
        if (open > 0.3f) {
            float a = Math.min(1f, (open - 0.3f) * 1.4f);
            for (int i = 0; i < 7; i++) {
                float ang = (float) (-Math.PI / 2 + (i - 3) * 0.28 + Math.sin(t + i) * 0.05);
                PA.reset(); PA.moveTo(cx, cy - 10 * s);
                PA.lineTo(cx + cos(ang - 0.07f) * 240 * s, cy - 10 * s + sin(ang - 0.07f) * 240 * s);
                PA.lineTo(cx + cos(ang + 0.07f) * 240 * s, cy - 10 * s + sin(ang + 0.07f) * 240 * s);
                PA.close();
                c.drawPath(PA, fill(alpha(0xFFFFF2B3, 0.2f * a)));
            }
            glow(c, cx, cy - 10 * s, 100 * s, 0xFFFFE08A, 0.7f * a);
        }
        R.set(cx - 50 * s, cy - 5 * s, cx + 50 * s, cy + 45 * s); c.drawRoundRect(R, 6 * s, 6 * s, fill(bc));
        R.set(cx - 50 * s, cy + 10 * s, cx + 50 * s, cy + 16 * s); c.drawRect(R, fill(alpha(tc, 0.9f)));
        R.set(cx - 50 * s, cy - 5 * s, cx - 42 * s, cy + 45 * s); c.drawRect(R, fill(alpha(tc, 0.9f)));
        R.set(cx + 42 * s, cy - 5 * s, cx + 50 * s, cy + 45 * s); c.drawRect(R, fill(alpha(tc, 0.9f)));
        c.save();
        c.rotate(-open * 75f, cx - 50 * s, cy - 5 * s);
        R.set(cx - 50 * s, cy - 38 * s, cx + 50 * s, cy - 3 * s); c.drawRoundRect(R, 14 * s, 14 * s, fill(mix(bc, 0xFFFFFFFF, 0.12f)));
        R.set(cx - 50 * s, cy - 16 * s, cx + 50 * s, cy - 8 * s); c.drawRect(R, fill(alpha(tc, 0.9f)));
        c.restore();
        R.set(cx - 9 * s, cy - 2 * s, cx + 9 * s, cy + 22 * s); c.drawRoundRect(R, 3 * s, 3 * s, fill(0xFFFFD35A));
        c.drawCircle(cx, cy + 9 * s, 3 * s, fill(0xFF5A3A00));
    }
}
