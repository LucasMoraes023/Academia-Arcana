package br.com.lucasmoraes.arcana;

import android.graphics.Canvas;

import java.util.ArrayList;

final class PotionScreen extends Screen {
    final int n;

    PotionScreen(final BattleScreen bs) {
        overlay = true;
        float y = Game.H / 2 - 110;
        int k = 0;
        for (int id = 1; id <= 3; id++) {
            final int fid = id;
            int cnt = Game.pl.count(id);
            if (cnt > 0) { btn(40, y + 50 + k * 58, 280, 48, Data.ITEMS[id].name + "  x" + cnt, 3, () -> { Game.pop(); bs.after(bs.b.usePotion(fid)); }); k++; }
        }
        n = k;
        btn(40, y + 56 + Math.max(1, k) * 58, 280, 42, "FECHAR", 1, () -> Game.pop());
    }

    @Override
    void draw(Canvas c, float dt) {
        tick(dt);
        c.drawRect(0, 0, Game.W, Game.H, Gfx.fill(0xCC000000));
        float y = Game.H / 2 - 110;
        Gfx.panel(c, 24, y, 312, 120 + Math.max(1, n) * 58, 18, 0xFF161036, Gfx.GOLD);
        Gfx.textS(c, "USAR POÇÃO", Game.W / 2, y + 34, 20, Gfx.GOLD, 1, true);
        if (n == 0) Gfx.para(c, "Você não tem poções. Consiga mais em baús, missões e fases!", 44, y + 56, 272, 14, Gfx.TXT, 19, 1, false);
        drawBtns(c);
    }
}

final class BattleScreen extends Screen {
    static final class Floater { float x, y, delay, life = 1.2f; String text = ""; int color; Fx fx; boolean active; }

    final Enemy e;
    final boolean practice, tutorial;
    final Runnable onWin, onExit;
    Battle b;
    int sel = -1;
    float busy, endT, hurtE, hurtP, lunge, flashRed, ecy, panelY, handY;
    boolean resultShown;
    Reward rw;
    final ArrayList<Floater> fl = new ArrayList<>();

    BattleScreen(Enemy e, boolean practice, boolean tutorial, Runnable onWin, Runnable onExit) {
        this.e = e; this.practice = practice; this.tutorial = tutorial; this.onWin = onWin; this.onExit = onExit;
        reset();
    }

    void reset() {
        btns.clear();
        fl.clear();
        b = new Battle(Game.pl, e, practice);
        sel = -1; busy = 0.4f; endT = 0; resultShown = false; rw = null;
        float rowH = 40, w = 108, y2 = Game.H - Game.BOT - rowH, y1 = y2 - 46;
        handY = y1 - 8 - 112;
        panelY = handY - 52;
        ecy = (Game.TOP + 64) + (panelY - 14 - (Game.TOP + 64)) * 0.42f;
        btn(10, y1, w, rowH, "ATACAR", 2, () -> act(0));
        btn(126, y1, w, rowH, "DEFENDER", 0, () -> act(1));
        btn(242, y1, w, rowH, "HABILIDADE", 0, () -> act(2));
        btn(10, y2, w, rowH, "POÇÃO", 3, () -> { if (canAct()) Game.push(new PotionScreen(this)); });
        btn(126, y2, w, rowH, "PASSAR TURNO", 4, () -> endTurn());
        btn(242, y2, w, rowH, "SAIR", 1, () -> confirmExit());
    }

    boolean canAct() { return busy <= 0 && b.state == 0 && !resultShown; }
    @Override boolean allowPopups() { return false; }
    @Override void onBack() { confirmExit(); }

    void confirmExit() {
        if (resultShown) return;
        Game.push(new ConfirmScreen("Abandonar batalha?", "O progresso desta batalha será perdido.", () -> onExit.run()));
    }

    void act(int use) {
        if (!canAct()) return;
        String err;
        if (use == 0) err = sel >= 0 ? b.play(sel, Battle.USE_ATK) : b.basicAttack();
        else if (use == 1) err = sel >= 0 ? b.play(sel, Battle.USE_DEF) : b.basicDefend();
        else err = sel >= 0 ? b.play(sel, Battle.USE_AB) : "Selecione uma carta com habilidade.";
        after(err);
    }

    void after(String err) {
        if (err != null) { Game.toast(err); Audio.sfx(Audio.FAIL); return; }
        sel = -1;
        consume(0f);
    }

    void endTurn() {
        if (!canAct()) return;
        sel = -1;
        b.endTurn();
        consume(0.35f);
    }

    void consume(float start) {
        float d = start;
        for (Fx f : b.fx) { Floater x = new Floater(); x.fx = f; x.delay = d; fl.add(x); d += 0.4f; }
        busy = Math.max(busy, d + 0.15f);
        b.fx.clear();
    }

    void activate(Floater x) {
        x.active = true;
        Fx f = x.fx;
        float W = Game.W;
        x.x = W / 2 + (Game.rnd.nextFloat() - 0.5f) * (f.side == 1 ? 50 : 90);
        x.y = f.side == 1 ? ecy - 30 : panelY - 8;
        switch (f.kind) {
            case Fx.DMG:
                x.text = f.amt > 0 ? "-" + f.amt : f.text.contains("bloq") ? "BLOQUEADO" : f.text.isEmpty() ? "0" : f.text;
                x.color = f.side == 1 ? 0xFFFFE08A : Gfx.RED;
                if (f.side == 1) { hurtE = 0.4f; Game.burst(W / 2, ecy, 0xFFFFD35A, 24); }
                else { hurtP = 0.4f; lunge = 0.35f; flashRed = 0.5f; Game.burst(W / 2, panelY, 0xFFE5484D, 20); }
                if (f.amt > 0) Audio.sfx(Audio.HIT);
                break;
            case Fx.HEAL: x.text = "+" + f.amt; x.color = Gfx.GREEN; Game.burst(W / 2, panelY, 0xFF3DD68C, 20); Audio.sfx(Audio.MAGIC); break;
            case Fx.BLOCK: x.text = "+" + f.amt + " escudo"; x.color = Gfx.BLUE; Audio.sfx(Audio.MAGIC); break;
            case Fx.ENERGY: x.text = f.text; x.color = 0xFFFFE08A; break;
            default: x.text = f.text; x.color = 0xFFFFFFFF; Audio.sfx(Audio.MAGIC);
        }
    }

    void showResult() {
        resultShown = true;
        btns.clear();
        if (b.state == 2) {
            rw = Rules.battleWin(Game.pl, e, practice);
            Game.save();
            Audio.sfx(Audio.LEVEL);
            Game.view.ps.confetti(80);
            btnC(Game.H / 2 + 80, 240, 48, "CONTINUAR", 0, () -> onWin.run());
        } else {
            Rules.battleLose(Game.pl, practice);
            Game.save();
            Audio.sfx(Audio.FAIL);
            Game.view.ps.embers(60);
            btnC(Game.H / 2 + 44, 240, 48, "TENTAR NOVAMENTE", 0, () -> reset());
            btnC(Game.H / 2 + 102, 240, 42, "SAIR", 1, () -> onExit.run());
        }
    }

    float cardW() { int n = Math.max(1, b.hand.size()); return Math.min(80, (Game.W - 16 - (n - 1) * 6) / n); }
    float cardX(int i) { float w = cardW(), total = b.hand.size() * w + (b.hand.size() - 1) * 6; return (Game.W - total) / 2 + i * (w + 6); }

    @Override
    void up(float x, float y) {
        boolean wasBtn = pressed != null;
        super.up(x, y);
        if (wasBtn || !canAct()) return;
        float w = cardW();
        if (y >= handY - 16 && y <= handY + w * 1.4f)
            for (int i = 0; i < b.hand.size(); i++) {
                float cx = cardX(i);
                if (x >= cx && x <= cx + w) { sel = sel == i ? -1 : i; Audio.sfx(Audio.CLICK); return; }
            }
    }

    @Override
    void draw(Canvas c, float dt) {
        tick(dt);
        float W = Game.W, cx = W / 2;
        if (busy > 0) busy -= dt;
        hurtE = Math.max(0, hurtE - dt); hurtP = Math.max(0, hurtP - dt); lunge = Math.max(0, lunge - dt); flashRed = Math.max(0, flashRed - dt);
        for (int i = fl.size() - 1; i >= 0; i--) {
            Floater x = fl.get(i);
            if (!x.active) { x.delay -= dt; if (x.delay <= 0) activate(x); }
            else { x.life -= dt; x.y -= 34 * dt; if (x.life <= 0) fl.remove(i); }
        }
        if (b.state != 0 && !resultShown && busy <= 0) { endT += dt; if (endT > 0.6f) showResult(); }
        // ---- inimigo
        Gfx.textS(c, e.name + (e.boss ? "  (CHEFE)" : ""), cx, Game.TOP + 14, 16, e.boss ? Gfx.RED : 0xFFFFFFFF, 1, true);
        Gfx.bar(c, 40, Game.TOP + 22, 280, 15, b.ehp / (float) b.emax, 0xFFB3262E, 0xFFFF8A8E);
        Gfx.textC(c, b.ehp + " / " + b.emax, cx, Game.TOP + 29.5f, 10.5f, 0xFFFFFFFF, true);
        StringBuilder st = new StringBuilder();
        if (b.burnT > 0) st.append("Queimando(").append(b.burnT).append(")  ");
        if (b.poisT > 0) st.append("Envenenado(").append(b.poisT).append(")  ");
        if (b.estun > 0) st.append("Atordoado  ");
        if (b.eblock > 0) st.append("Escudo ").append(b.eblock);
        if (st.length() > 0) Gfx.text(c, st.toString(), cx, Game.TOP + 54, 11.5f, 0xFFFFA45C, 1, true);
        int ic = b.next == 'D' ? Gfx.BLUE : b.next == 'P' ? Gfx.GREEN : b.next == 'E' ? 0xFFB89CFF : Gfx.RED;
        if (b.estun > 0) ic = 0xFFFFE08A;
        float iw = Math.max(150, Gfx.tw("Próxima ação: " + b.intent, 12.5f, true) + 28);
        Gfx.panel(c, cx - iw / 2, Game.TOP + 62, iw, 24, 12, Gfx.alpha(0xFF120C2E, 0.92f), Gfx.alpha(ic, 0.9f));
        Gfx.textC(c, "Próxima ação: " + b.intent, cx, Game.TOP + 74, 12.5f, ic, true);
        float s = e.boss ? 1.2f : 1f;
        c.save();
        if (flashRed > 0) c.translate(Gfx.sin(t * 80) * 4 * flashRed, 0);
        Gfx.enemy(c, e, cx, ecy + (lunge > 0 ? 26 * lunge / 0.35f : 0), s, t, hurtE / 0.4f);
        c.restore();
        // ---- informações / dicas
        String info;
        if (sel >= 0 && sel < b.hand.size()) {
            Card d = Data.card(b.hand.get(sel));
            info = d.name + " • custo " + d.en + " • " + (d.ab != 0 ? Data.AB_NAME[d.ab] + ": " + Data.abDesc(d.ab, d.pw) : "sem habilidade");
        } else info = "Toque numa carta e escolha uma ação.";
        Gfx.para(c, info, 20, ecy + 76, 320, 12, 0xFFE9E1FF, 16, 1, false);
        if (tutorial) {
            String h = b.turn <= 1 ? "TUTORIAL: toque em uma carta da sua mão e depois em ATACAR. Cartas gastam energia (bolinhas amarelas)."
                    : "TUTORIAL: use DEFENDER para criar escudo e PASSAR TURNO quando acabar a energia. O inimigo mostra o que vai fazer!";
            Gfx.panel(c, 12, panelY - 54, 336, 46, 12, Gfx.alpha(0xFF241A5C, 0.95f), Gfx.GOLD);
            Gfx.para(c, h, 22, panelY - 51, 316, 11.5f, 0xFFFFFFFF, 14.5f, 1, false);
        }
        if (Game.pl.god) Gfx.text(c, "MODO DEUS", 10, Game.TOP + 14, 10, Gfx.RED, 0, true);
        if (practice) Gfx.text(c, "BATALHA DE TESTE", 350, Game.TOP + 14, 10, Gfx.GOLD, 2, true);
        // ---- jogador
        Gfx.panel(c, 8, panelY, 344, 46, 12, Gfx.alpha(0xFF120C2E, 0.93f), Gfx.alpha(hurtP > 0 ? Gfx.RED : Gfx.GOLD, 0.6f));
        Gfx.heart(c, 24, panelY + 14, 8, Gfx.RED);
        Gfx.bar(c, 38, panelY + 8, 150, 13, b.hp / (float) b.maxHp, 0xFFE5484D, 0xFFFF8A8E);
        Gfx.textC(c, b.hp + " / " + b.maxHp, 113, panelY + 14.5f, 10, 0xFFFFFFFF, true);
        if (b.block > 0) { Gfx.catIcon(c, 6, 206, panelY + 14, 8, Gfx.BLUE); Gfx.text(c, String.valueOf(b.block), 218, panelY + 19, 14, Gfx.BLUE, 0, true); }
        if (b.fury > 0) Gfx.text(c, "Fúria +" + b.fury, 262, panelY + 19, 12, 0xFFFFA45C, 0, true);
        for (int i = 0; i < b.maxEnergy; i++) {
            float ox = 22 + i * 19, oy = panelY + 35;
            c.drawCircle(ox, oy, 6, Gfx.fill(i < b.energy ? 0xFFFFD35A : 0xFF2A2150));
            c.drawCircle(ox, oy, 6, Gfx.stroke(0xFFB8860B, 1.2f));
        }
        Gfx.text(c, "Turno " + b.turn, 344, panelY + 39, 11, Gfx.DIM, 2, true);
        // ---- mão
        float w = cardW();
        for (int i = 0; i < b.hand.size(); i++) {
            Card d = Data.card(b.hand.get(i));
            boolean on = i == sel;
            float x = cardX(i), y = handY - (on ? 14 : 0);
            if (on) Gfx.glow(c, x + w / 2, y + w * 0.7f, w * 0.95f, 0xFFFFE08A, 0.5f);
            Gfx.card(c, d, x, y, w, Game.pl.stars(d.id), false, t, b.energy < d.en ? 0.55f : 0f);
            if (on) { Gfx.R.set(x - 1, y - 1, x + w + 1, y + w * 1.4f + 1); c.drawRoundRect(Gfx.R, 8, 8, Gfx.stroke(0xFFFFE08A, 2.5f)); }
        }
        if (b.hand.isEmpty()) Gfx.text(c, "Sem cartas na mão — use as ações básicas.", cx, handY + 50, 12, Gfx.DIM, 1, false);
        drawBtns(c);
        // ---- números flutuantes
        for (Floater x : fl) {
            if (!x.active) continue;
            float a = Math.min(1f, x.life * 2.2f);
            Gfx.textS(c, x.text, x.x, x.y, x.text.length() > 6 ? 17 : 26, Gfx.alpha(x.color, a), 1, true);
        }
        if (flashRed > 0) c.drawRect(0, 0, W, Game.H, Gfx.fill(Gfx.alpha(0xFFE5484D, 0.25f * flashRed)));
        if (resultShown) {
            c.drawRect(0, 0, W, Game.H, Gfx.fill(0xCC000000));
            float y = Game.H / 2 - 150;
            boolean win = b.state == 2;
            Gfx.panel(c, 24, y, 312, win ? 300 : 276, 20, 0xFF161036, win ? Gfx.GOLD : Gfx.RED);
            Gfx.textS(c, win ? "VITÓRIA!" : "DERROTA", cx, y + 56, 40, win ? Gfx.GOLD : Gfx.RED, 1, true);
            if (win) {
                Gfx.text(c, e.name + " foi derrotado!", cx, y + 84, 14, 0xFFFFFFFF, 1, true);
                Gfx.para(c, practice ? "Batalha de teste: sem recompensas." : "Recompensas: " + Rules.describe(rw), 44, y + 98, 272, 15, Gfx.GREEN, 21, 1, true);
            } else Gfx.para(c, "Seus feitiços não foram suficientes desta vez. Ajuste seu deck e tente de novo!", 44, y + 84, 272, 14, Gfx.TXT, 20, 1, false);
            drawBtns(c);
        }
    }
}
