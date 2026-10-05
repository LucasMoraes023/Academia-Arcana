package br.com.lucasmoraes.arcana;

import android.graphics.Canvas;
import android.graphics.RectF;

final class TitleScreen extends Screen {
    TitleScreen() {
        float by = Math.max(318, Game.H - 5 * 54 - 22), w = 250, h = 46;
        btnC(by, w, h, "JOGAR", 0, () -> {
            if (Game.pl == null) { Game.toast("Entre ou crie uma conta para jogar!"); Game.push(new AuthScreen(false)); }
            else Game.enter();
        });
        btnC(by + 54, w, h, "ENTRAR", 1, () -> Game.push(new AuthScreen(false)));
        btnC(by + 108, w, h, "CRIAR CONTA", 1, () -> Game.push(new AuthScreen(true)));
        btnC(by + 162, w, h, "SOBRE", 1, () -> Game.push(new AboutScreen()));
        btnC(by + 216, w, h, "CONFIGURAÇÕES", 1, () -> Game.push(new SettingsScreen()));
    }

    @Override
    void draw(Canvas c, float dt) {
        tick(dt);
        float W = Game.W, by = btns.get(0).r.top, ey = by * 0.26f + 22;
        Gfx.glow(c, W / 2, ey, 96, 0xFFB89CFF, 0.5f);
        c.save(); c.rotate(t * 12, W / 2, ey); Gfx.star(c, W / 2, ey, 56, Gfx.alpha(Gfx.GOLD, 0.9f)); c.restore();
        c.save(); c.rotate(-t * 8, W / 2, ey); Gfx.star(c, W / 2, ey, 36, 0xFF5B34B8); c.restore();
        Gfx.glyph(c, 3, W / 2 - 3, ey, 17, 0xFFFFF2B3);
        float ty = ey + 92;
        Gfx.textS(c, "ACADEMIA", W / 2, ty, 26, Gfx.GOLD, 1, true);
        Gfx.text(c, "ARCANA", W / 2, ty + 52, 58, Gfx.alpha(0xFFB89CFF, 0.55f + 0.2f * Gfx.sin(t * 2)), 1, true);
        Gfx.textS(c, "ARCANA", W / 2, ty + 50, 58, 0xFFFFE9A8, 1, true);
        Gfx.text(c, "Cartas & Magia", W / 2, ty + 76, 14, Gfx.DIM, 1, false);
        if (Game.pl != null) Gfx.text(c, "Aprendiz conectado: " + Game.pl.nick, W / 2, ty + 98, 13, Gfx.GREEN, 1, true);
        drawBtns(c);
    }

    @Override void onBack() { Game.act.finish(); }
}

final class AuthScreen extends Screen {
    final boolean reg;
    String nick = "", pw = "", pw2 = "", err = "";
    final RectF f1 = new RectF(), f2 = new RectF(), f3 = new RectF();

    AuthScreen(boolean reg) {
        this.reg = reg;
        f1.set(44, 136, 316, 180);
        f2.set(44, 210, 316, 254);
        f3.set(44, 284, 316, 328);
        float by = reg ? 372 : 304;
        btn(44, by, 272, 46, reg ? "CRIAR CONTA" : "ENTRAR", 0, () -> submit());
        btn(44, by + 56, 272, 42, "VOLTAR", 1, () -> Game.pop());
    }

    static String mask(int n) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < n; i++) b.append('*');
        return b.toString();
    }

    void submit() {
        if (reg) {
            String e = Game.acc.register(nick, pw, pw2);
            if (e != null) { err = e; Audio.sfx(Audio.FAIL); return; }
            Player p = Game.acc.login(nick, pw);
            if (p != null) { Game.toast("Conta criada! Bem-vindo, " + p.nick + "!"); Game.login(p); }
        } else {
            Player p = Game.acc.login(nick, pw);
            if (p == null) { err = "Nickname ou senha incorretos."; Audio.sfx(Audio.FAIL); return; }
            Game.login(p);
        }
    }

    @Override
    void up(float x, float y) {
        boolean wasBtn = pressed != null;
        super.up(x, y);
        if (wasBtn) return;
        if (f1.contains(x, y)) Game.ask("Nickname", "3 a 16 letras, números ou _", false, nick, s -> nick = s.trim());
        else if (f2.contains(x, y)) Game.ask("Senha", "mínimo 4 caracteres", true, "", s -> pw = s);
        else if (reg && f3.contains(x, y)) Game.ask("Confirmar senha", "repita a senha", true, "", s -> pw2 = s);
    }

    void field(Canvas c, RectF f, String label, String value, String hint) {
        Gfx.text(c, label, f.left + 4, f.top - 8, 13, Gfx.GOLD, 0, true);
        Gfx.panel(c, f.left, f.top, f.width(), f.height(), 10, 0xFF0B0820, 0xFF7C6BC4);
        boolean empty = value.isEmpty();
        Gfx.text(c, empty ? hint : value, f.left + 14, f.centerY() + 5, 15, empty ? Gfx.alpha(Gfx.DIM, 0.7f) : 0xFFFFFFFF, 0, false);
    }

    @Override
    void draw(Canvas c, float dt) {
        tick(dt);
        header(c, reg ? "CRIAR CONTA" : "ENTRAR");
        Gfx.panel(c, 24, 100, 312, reg ? 350 : 282, 16, Gfx.alpha(0xFF120C2E, 0.9f), Gfx.alpha(Gfx.GOLD, 0.5f));
        field(c, f1, "Nickname", nick, "toque para digitar");
        field(c, f2, "Senha", mask(pw.length()), "toque para digitar");
        if (reg) field(c, f3, "Confirmar senha", mask(pw2.length()), "repita a senha");
        if (!err.isEmpty()) Gfx.para(c, err, 40, reg ? 336 : 262, 280, 13, Gfx.RED, 17, 1, true);
        drawBtns(c);
    }
}

final class HouseQuizScreen extends Screen {
    int idx;
    final int[] ans = new int[10];
    float qBottom;

    HouseQuizScreen() { build(); }

    void build() {
        btns.clear();
        HQ q = Data.HQUIZ.get(idx);
        float qh = Gfx.paraH(q.q, 17, 280, 23, true);
        qBottom = 142 + qh + 24;
        float y = qBottom + 18;
        for (int i = 0; i < 4; i++) {
            final int k = i;
            btn(24, y + i * 68, 312, 58, q.ans[i], 1, () -> pick(k));
        }
    }

    void pick(int k) {
        ans[idx] = k;
        idx++;
        if (idx >= Data.HQUIZ.size()) {
            int h = Data.houseFrom(ans);
            Game.pl.house = h;
            Game.save();
            Game.go(new DestinyScreen(h));
        } else build();
    }

    @Override void onBack() { if (idx > 0) { idx--; build(); } else Game.go(new TitleScreen()); }

    @Override
    void draw(Canvas c, float dt) {
        tick(dt);
        header(c, "QUIZ DA CASA");
        float n = Data.HQUIZ.size();
        Gfx.text(c, "Pergunta " + (idx + 1) + " de " + (int) n, 24, TOPY(), 13, Gfx.DIM, 0, true);
        Gfx.bar(c, 24, TOPY() + 8, 312, 10, idx / n, 0xFF8E5BE0, 0xFFB89CFF);
        Gfx.panel(c, 16, 118 + 20, 328, qBottom - 138 + 0, 14, Gfx.alpha(0xFF120C2E, 0.92f), Gfx.alpha(Gfx.GOLD, 0.5f));
        Gfx.para(c, Data.HQUIZ.get(idx).q, 40, 142, 280, 17, 0xFFFFFFFF, 23, 1, true);
        drawBtns(c);
    }

    float TOPY() { return Game.TOP + 58; }
}

final class DestinyScreen extends Screen {
    final int h;
    boolean b1, b2;

    DestinyScreen(int h) {
        this.h = h;
        Btn b = btnC(Game.H - 64, 260, 46, "CONTINUAR", 0, () -> Game.enter());
        b.vis = false;
        b.on = false;
    }

    static float cl(float v) { return Math.max(0, Math.min(1, v)); }

    @Override void onBack() { }

    @Override
    void draw(Canvas c, float dt) {
        tick(dt);
        float W = Game.W, cx = W / 2, ey = 215;
        int col = Data.HCOL[h];
        if (t > 1.6f) Gfx.glow(c, cx, ey, 170 + 20 * Gfx.sin(t * 2), col, 0.55f);
        Gfx.textS(c, "SEU DESTINO", cx, 82, 26, Gfx.alpha(0xFFFFFFFF, cl((t - 0.2f) / 0.8f)), 1, true);
        Gfx.textS(c, "FOI REVELADO", cx, 118, 31, Gfx.alpha(Gfx.GOLD, cl((t - 0.9f) / 0.8f)), 1, true);
        if (t > 1.8f) {
            float k = cl((t - 1.8f) / 1.2f), sc = 1 + 2.2f * (k - 1) * (k - 1) * (k - 1) + 1.2f * (k - 1) * (k - 1);
            Gfx.houseEmblem(c, h, cx, ey, 66 * sc, t);
        }
        if (!b1 && t > 2f) { b1 = true; Game.burst(cx, ey, col, 70); Audio.sfx(Audio.MAGIC); }
        if (!b2 && t > 2.9f) { b2 = true; Game.burst(cx, ey, 0xFFFFE08A, 60); Audio.sfx(Audio.OK); }
        float a3 = cl((t - 3f) / 0.6f), a4 = cl((t - 3.6f) / 0.6f);
        Gfx.textS(c, "CASA " + Data.HNAME[h], cx, ey + 112, 31, Gfx.alpha(Gfx.mix(col, 0xFFFFFFFF, 0.35f), a3), 1, true);
        Gfx.text(c, "Símbolo: " + Data.HSYM[h], cx, ey + 134, 14, Gfx.alpha(Gfx.DIM, a3), 1, false);
        Gfx.para(c, Data.HDESC[h], 30, ey + 142, 300, 14, Gfx.alpha(Gfx.TXT, a3), 19, 1, false);
        Gfx.panel(c, 30, ey + 204, 300, 30, 12, Gfx.alpha(0xFF120C2E, 0.9f * a4), Gfx.alpha(col, a4));
        Gfx.textC(c, "BÔNUS: " + Data.HBONUS[h], cx, ey + 219, 12.5f, Gfx.alpha(0xFFFFFFFF, a4), true);
        Gfx.text(c, "Cartas exclusivas da casa", cx, ey + 256, 12, Gfx.alpha(Gfx.GOLD, a4), 1, true);
        if (a4 > 0) for (int i = 0; i < 3; i++) Gfx.card(c, Data.card(Data.exclusive(h, i)), cx - 98 + i * 68, ey + 266, 60, 1, true, t, 1f - a4);
        Btn b = btns.get(0);
        if (t > 4.2f && !b.vis) { b.vis = true; b.on = true; }
        drawBtns(c);
    }
}
