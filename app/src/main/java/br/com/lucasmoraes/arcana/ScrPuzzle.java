package br.com.lucasmoraes.arcana;

import android.graphics.Canvas;

import java.util.ArrayList;

/** Enigmas: sequência de runas, símbolos, código, lógica, combinação e memória. */
final class PuzzleScreen extends Screen {
    static final int[] RC = {0xFFFF7A3D, 0xFF4DA3FF, 0xFF3DD68C, 0xFFB89CFF, 0xFFFFD35A, 0xFFFF8FD0};
    static final int[] GEM = {0xFFE5484D, 0xFF4DA3FF, 0xFF3DD68C, 0xFFFFD35A};
    final Riddle r;
    final String title;
    boolean solved;
    float shake, textBottom, areaY, showT = 0.9f, litT;
    String msg = "";
    Reward rw;
    final ArrayList<Integer> picked = new ArrayList<>();
    final ArrayList<int[]> hist = new ArrayList<>();
    final ArrayList<Btn> opts = new ArrayList<>();
    final int[] dig = new int[3], slots = new int[3];
    int[] seq;
    int phase, showIdx, inPos, lit = -1, tries;

    PuzzleScreen(Riddle r, String title) {
        this.r = r;
        this.title = title;
        float top = Game.TOP + 52;
        textBottom = top + 20 + Gfx.paraH(r.text, 14, 288, 20, false);
        areaY = textBottom + 14;
        float W = Game.W;
        switch (r.type) {
            case Riddle.SEQ:
                for (int i = 0; i < 4; i++) { final int k = i; opts.add(btn(24 + i * 80, areaY + 96, 72, 72, "", 1, () -> { if (r.b[k] == r.ans) solve(); else wrong("Não é essa runa. Observe o padrão!"); })); }
                break;
            case Riddle.LOGIC:
                for (int i = 0; i < r.s.length; i++) { final int k = i; btn(24, areaY + 6 + i * 62, 312, 52, r.s[i], 1, () -> { if (k == r.ans) solve(); else wrong("Resposta incorreta. Pense mais um pouco!"); }); }
                break;
            case Riddle.SYM:
                for (int i = 0; i < 6; i++) { final int k = i; opts.add(btn(33 + (i % 3) * 102, areaY + 74 + (i / 3) * 92, 90, 80, "", 1, () -> tapRune(k))); }
                break;
            case Riddle.CODE: {
                float ch = 0;
                for (String s : r.s) ch += Gfx.paraH(s, 13, 276, 18, false) + 4;
                float cy = areaY + ch + 6;
                for (int i = 0; i < 3; i++) {
                    final int d = i;
                    float cx = 80 + i * 100;
                    btn(cx - 26, cy, 52, 34, "+", 1, () -> dig[d] = (dig[d] + 1) % 10);
                    btn(cx - 26, cy + 112, 52, 34, "-", 1, () -> dig[d] = (dig[d] + 9) % 10);
                }
                areaY = cy;
                btnC(cy + 160, 240, 48, "ABRIR CADEADO", 0, () -> {
                    if (dig[0] == r.a[0] && dig[1] == r.a[1] && dig[2] == r.a[2]) solve(); else wrong("O cadeado não abriu. Releia as pistas.");
                });
                break;
            }
            case Riddle.COMBO:
                for (int i = 0; i < 3; i++) { final int k = i; opts.add(btn(50 + i * 100, areaY + 10, 60, 60, "", 1, () -> slots[k] = (slots[k] + 1) % 4)); }
                btnC(areaY + 84, 220, 44, "TESTAR COMBINAÇÃO", 0, () -> test());
                break;
            default:
                seq = new int[r.n];
                for (int i = 0; i < r.n; i++) seq[i] = Game.rnd.nextInt(4);
                for (int i = 0; i < 4; i++) { final int k = i; Btn b = btn(54 + (i % 2) * 136, areaY + 24 + (i / 2) * 116, 124, 104, "", 1, () -> tapPad(k)); b.on = false; opts.add(b); }
                msg = "Observe a sequência...";
        }
    }

    void wrong(String m) { msg = m; shake = 0.45f; Audio.sfx(Audio.FAIL); }

    void solve() {
        if (solved) return;
        solved = true;
        rw = Rules.riddleSolved(Game.pl);
        Game.save();
        Audio.sfx(Audio.OK);
        Game.burst(Game.W / 2, 260, Gfx.GOLD, 70);
        for (Btn b : btns) { b.on = false; b.vis = false; }
        btnC(Game.H - 90, 260, 50, "CONTINUAR", 0, () -> Runner.next());
    }

    void tapRune(int k) {
        if (k == r.a[picked.size()]) {
            picked.add(k);
            Audio.sfx(Audio.MAGIC);
            if (picked.size() == r.a.length) solve();
        } else { picked.clear(); wrong("Ordem errada! Releia o pergaminho."); }
    }

    void test() {
        int ok = 0;
        for (int i = 0; i < 3; i++) if (slots[i] == r.a[i]) ok++;
        if (ok == 3) { hist.add(new int[]{slots[0], slots[1], slots[2], ok}); solve(); return; }
        hist.add(new int[]{slots[0], slots[1], slots[2], ok});
        tries++;
        if (tries >= r.n) { hist.clear(); tries = 0; wrong("Sem tentativas! O cofre foi reiniciado."); }
        else { msg = ok + " cristal(is) no lugar certo. Tentativa " + tries + " de " + r.n + "."; Audio.sfx(Audio.CLICK); }
    }

    void setPads(boolean on) { for (Btn b : opts) b.on = on; }

    void tapPad(int p) {
        if (phase != 1) return;
        lit = p;
        litT = 0.2f;
        if (seq[inPos] == p) {
            inPos++;
            Audio.sfx(Audio.MAGIC);
            if (inPos >= seq.length) solve();
        } else {
            wrong("Errou! Observe a sequência de novo.");
            phase = 0; showIdx = 0; lit = -1; showT = 1.1f;
            setPads(false);
        }
    }

    void memUpdate(float dt) {
        if (r.type != Riddle.MEM || solved) return;
        if (phase == 1 && litT > 0) { litT -= dt; if (litT <= 0) lit = -1; }
        if (phase != 0) return;
        showT -= dt;
        if (showT > 0) return;
        if (lit >= 0) { lit = -1; showT = 0.25f; showIdx++; }
        else if (showIdx < seq.length) { lit = seq[showIdx]; showT = 0.55f; Audio.sfx(Audio.CLICK); }
        else { phase = 1; inPos = 0; setPads(true); msg = "Sua vez! Repita a sequência."; }
    }

    @Override void onBack() { Runner.quit(); }

    void gem(Canvas c, int col, float cx, float cy, float rr) {
        c.drawCircle(cx, cy, rr, Gfx.fill(GEM[col]));
        c.drawCircle(cx - rr * 0.3f, cy - rr * 0.3f, rr * 0.28f, Gfx.fill(0x88FFFFFF));
        c.drawCircle(cx, cy, rr, Gfx.stroke(0x88FFFFFF, 1.5f));
    }

    @Override
    void draw(Canvas c, float dt) {
        tick(dt);
        if (shake > 0) shake = Math.max(0, shake - dt);
        memUpdate(dt);
        header(c, r.title.toUpperCase());
        float top = Game.TOP + 52;
        Gfx.panel(c, 16, top, 328, textBottom - top, 14, Gfx.alpha(0xFF120C2E, 0.92f), Gfx.alpha(Gfx.GOLD, 0.5f));
        Gfx.para(c, r.text, 36, top + 12, 288, 14, 0xFFFFFFFF, 20, 1, false);
        c.save();
        if (shake > 0) c.translate(Gfx.sin(t * 70) * 7 * shake, 0);
        if (!solved) {
            switch (r.type) {
                case Riddle.SEQ:
                    for (int i = 0; i < 6; i++) {
                        float x = 33 + i * 50;
                        Gfx.panel(c, x, areaY + 20, 44, 44, 10, 0xFF0B0820, Gfx.alpha(Gfx.GOLD, 0.6f));
                        if (r.a[i] >= 0) Gfx.glyph(c, r.a[i], x + 22, areaY + 42, 14, RC[r.a[i]]);
                        else Gfx.textC(c, "?", x + 22, areaY + 42, 24, Gfx.alpha(Gfx.GOLD, 0.6f + 0.4f * Gfx.sin(t * 4)), true);
                    }
                    break;
                case Riddle.SYM: {
                    int n = r.a.length;
                    float x0 = (Game.W - (n * 46 + (n - 1) * 8)) / 2;
                    for (int i = 0; i < n; i++) {
                        float x = x0 + i * 54;
                        Gfx.panel(c, x, areaY + 8, 46, 46, 10, 0xFF0B0820, Gfx.alpha(Gfx.GOLD, i == picked.size() ? 1f : 0.4f));
                        if (i < picked.size()) Gfx.glyph(c, picked.get(i), x + 23, areaY + 31, 14, RC[picked.get(i)]);
                    }
                    break;
                }
                case Riddle.CODE: {
                    float y = textBottom + 14;
                    for (String s : r.s) {
                        c.drawCircle(30, y + 8, 3, Gfx.fill(Gfx.GOLD));
                        y += Gfx.para(c, s, 40, y, 276, 13, Gfx.TXT, 18, 0, false) + 4;
                    }
                    for (int i = 0; i < 3; i++) {
                        float cx = 80 + i * 100;
                        Gfx.panel(c, cx - 30, areaY + 40, 60, 64, 12, 0xFF0B0820, Gfx.GOLD);
                        Gfx.textC(c, String.valueOf(dig[i]), cx, areaY + 72, 40, 0xFFFFFFFF, true);
                    }
                    break;
                }
                case Riddle.COMBO:
                    Gfx.text(c, "Tentativas: " + (r.n - tries) + " restantes", Game.W / 2, areaY + 148, 12, Gfx.DIM, 1, true);
                    for (int i = Math.max(0, hist.size() - 5); i < hist.size(); i++) {
                        int[] h = hist.get(i);
                        float y = areaY + 166 + (i - Math.max(0, hist.size() - 5)) * 26;
                        for (int k = 0; k < 3; k++) gem(c, h[k], 100 + k * 28, y, 9);
                        Gfx.text(c, h[3] + " no lugar certo", 160, y + 5, 13, h[3] > 0 ? Gfx.GREEN : Gfx.DIM, 0, true);
                    }
                    break;
                default: break;
            }
        }
        drawBtns(c);
        if (!solved) {
            switch (r.type) {
                case Riddle.SEQ:
                    for (int i = 0; i < 4; i++) Gfx.glyph(c, r.b[i], opts.get(i).r.centerX(), opts.get(i).r.centerY(), 24, RC[r.b[i]]);
                    break;
                case Riddle.SYM:
                    for (int i = 0; i < 6; i++) Gfx.glyph(c, i, opts.get(i).r.centerX(), opts.get(i).r.centerY(), 26, RC[i]);
                    break;
                case Riddle.COMBO:
                    for (int i = 0; i < 3; i++) gem(c, slots[i], opts.get(i).r.centerX(), opts.get(i).r.centerY(), 20);
                    break;
                case Riddle.MEM:
                    for (int i = 0; i < 4; i++) {
                        Btn b = opts.get(i);
                        boolean on = lit == i;
                        Gfx.panel(c, b.r.left, b.r.top, b.r.width(), b.r.height(), 14, Gfx.alpha(RC[i], on ? 0.85f : 0.18f), on ? 0xFFFFFFFF : Gfx.alpha(RC[i], 0.7f));
                        if (on) Gfx.glow(c, b.r.centerX(), b.r.centerY(), 80, RC[i], 0.6f);
                        Gfx.glyph(c, i, b.r.centerX(), b.r.centerY(), 28, on ? 0xFFFFFFFF : RC[i]);
                    }
                    break;
                default: break;
            }
        }
        c.restore();
        if (solved) {
            float cx = Game.W / 2;
            Gfx.glow(c, cx, 230, 150, Gfx.GOLD, 0.4f);
            Gfx.textS(c, "ENIGMA RESOLVIDO!", cx, 200, 27, Gfx.GOLD, 1, true);
            Gfx.star(c, cx, 262, 28 + 3 * Gfx.sin(t * 4), Gfx.GOLD);
            if (rw != null) Gfx.para(c, "Recompensas: " + Rules.describe(rw), 40, 316, 280, 15, Gfx.GREEN, 21, 1, true);
        } else if (!msg.isEmpty()) {
            Gfx.para(c, msg, 20, Game.H - 56, 320, 13, r.type == Riddle.MEM && phase == 1 ? Gfx.GREEN : Gfx.alpha(0xFFFFE08A, 1f), 18, 1, true);
        }
    }
}
