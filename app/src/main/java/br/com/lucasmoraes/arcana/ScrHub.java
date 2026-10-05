package br.com.lucasmoraes.arcana;

import android.graphics.Canvas;

final class HubScreen extends Screen {
    HubScreen() {
        Player p = Game.pl;
        float y = Game.TOP + 120, w = 164, h = 56;
        Btn b = btn(16, y, 328, 64, "AVENTURA", 4, () -> Game.go(new AdventureScreen(0)));
        b.icon = 0;
        y += 74;
        add(16, y, w, h, "TREINO", 1, 1, () -> training());
        add(180, y, w, h, "COLEÇÃO", 1, 2, () -> Game.go(new CollectionScreen()));
        y += 64;
        add(16, y, w, h, "DECK", 1, 3, () -> Game.go(new DeckScreen()));
        add(180, y, w, h, "MISSÕES", 1, 4, () -> Game.go(new QuestsScreen()));
        y += 64;
        add(16, y, w, h, "INVENTÁRIO", 1, 5, () -> Game.go(new InventoryScreen()));
        add(180, y, w, h, "BAÚS", 1, 6, () -> Game.go(new ChestsScreen()));
        y += 64;
        add(16, y, w, h, "CONQUISTAS", 1, 7, () -> Game.go(new AchievementsScreen()));
        add(180, y, w, h, "CONFIGURAÇÕES", 1, 9, () -> Game.push(new SettingsScreen()));
        y += 64;
        if (p.isAdmin()) {
            Btn a = btn(16, y, 328, 52, "PAINEL ADMIN", 2, () -> Game.go(new AdminScreen()));
            a.icon = 8;
            y += 60;
        }
        Btn s = btn(16, y, 328, 46, "SAIR DA CONTA", 1, () -> Game.logout());
        s.icon = 10;
    }

    void add(float x, float y, float w, float h, String l, int st, int icon, Runnable r) { btn(x, y, w, h, l, st, r).icon = icon; }

    void training() {
        Player p = Game.pl;
        if (!p.isAdmin() && !p.spendEnergy(1)) { Game.toast("Energia insuficiente (1 por treino)."); return; }
        Game.save();
        startTraining();
    }

    static void startTraining() {
        Player p = Game.pl;
        Game.go(new BattleScreen(Data.training(p.level, Game.rnd.nextInt(30)), false, false, () -> Game.go(new HubScreen()), () -> Game.go(new HubScreen())));
    }

    @Override void onBack() { Game.go(new TitleScreen()); }

    @Override
    void up(float x, float y) {
        boolean wasBtn = pressed != null;
        super.up(x, y);
        if (!wasBtn && y < Game.TOP + 76 && y > Game.TOP && x > 70) Game.push(new LevelsScreen());
    }

    @Override
    void draw(Canvas c, float dt) {
        tick(dt);
        Player p = Game.pl;
        p.refreshEnergy();
        float top = Game.TOP;
        int hc = p.house >= 0 ? Data.HCOL[p.house] : Gfx.GOLD;
        Gfx.panel(c, 8, top - 2, 344, 108, 16, Gfx.alpha(0xFF120C2E, 0.9f), Gfx.alpha(hc, 0.7f));
        Gfx.houseEmblem(c, p.house, 46, top + 40, 24, t);
        Gfx.textS(c, p.nick, 82, top + 28, 19, 0xFFFFFFFF, 0, true);
        String house = p.house >= 0 ? "Casa " + Data.HNAME[p.house] : "";
        Gfx.text(c, "NÍVEL " + p.level + " • " + Data.title(p.level) + "  " + house, 82, top + 46, 11.5f, Gfx.DIM, 0, true);
        long need = Player.xpNeed(p.level);
        Gfx.bar(c, 82, top + 54, 252, 12, need == 0 ? 1f : p.xp / (float) need, 0xFF8E5BE0, 0xFFC9B0FF);
        Gfx.textC(c, need == 0 ? "NÍVEL MÁXIMO" : "XP " + p.xp + " / " + need, 208, top + 60, 9.5f, 0xFFFFFFFF, true);
        Gfx.coin(c, 96, top + 84, 9);
        Gfx.text(c, String.valueOf(p.coins), 110, top + 89, 14, Gfx.GOLD, 0, true);
        Gfx.bolt(c, 212, top + 84, 9, 0xFFFFE08A);
        String en = p.energy + "/" + p.maxEnergy();
        Gfx.text(c, en, 226, top + 89, 14, 0xFFFFE08A, 0, true);
        if (p.energy < p.maxEnergy()) Gfx.text(c, "+1 em " + p.secondsToNextEnergy() + "s", 330, top + 89, 10, Gfx.DIM, 2, false);
        if (p.god) Gfx.text(c, "MODO DEUS", 340, top + 20, 10, Gfx.RED, 2, true);
        drawBtns(c);
    }
}

final class LevelsScreen extends Screen {
    final Scroll sc = new Scroll();
    static final float ROW = 54;

    LevelsScreen() {
        sc.set(Game.TOP + 96, Game.H - 66, (Player.MAX_LEVEL + 1) * ROW + 8);
        sc.off = Math.max(0, Game.pl.level * ROW - 80);
        sc.clamp();
        btnC(Game.H - 56, 200, 42, "VOLTAR", 1, () -> Game.pop());
    }

    @Override void down(float x, float y) { super.down(x, y); if (sc.inside(y)) sc.down(y); }
    @Override void move(float x, float y) { super.move(x, y); sc.move(y); }
    @Override void up(float x, float y) { super.up(x, y); sc.up(); }

    @Override
    void draw(Canvas c, float dt) {
        tick(dt);
        sc.update(dt);
        Player p = Game.pl;
        header(c, "NÍVEIS E RECOMPENSAS");
        long need = Player.xpNeed(p.level);
        Gfx.text(c, "NÍVEL " + p.level + (need == 0 ? " (máximo)" : "   XP " + p.xp + " / " + need), 180, Game.TOP + 62, 13, 0xFFFFFFFF, 1, true);
        Gfx.bar(c, 30, Game.TOP + 70, 300, 12, need == 0 ? 1f : p.xp / (float) need, 0xFF8E5BE0, 0xFFC9B0FF);
        sc.begin(c);
        for (int l = 0; l <= Player.MAX_LEVEL; l++) {
            float y = l * ROW + 4;
            if (y + ROW < sc.off || y > sc.off + (sc.bottom - sc.top)) continue;
            boolean cur = l == p.level, past = l < p.level;
            Gfx.panel(c, 14, y, 332, ROW - 6, 10, cur ? 0xFF2A1F5E : Gfx.alpha(0xFF120C2E, 0.9f), cur ? Gfx.GOLD : past ? Gfx.alpha(Gfx.GREEN, 0.6f) : 0x553D3170);
            Gfx.textC(c, String.valueOf(l), 40, y + (ROW - 6) / 2, 20, cur ? Gfx.GOLD : past ? Gfx.GREEN : Gfx.DIM, true);
            Gfx.text(c, Data.title(l) + (l == 0 ? "" : "  •  precisa de " + Player.xpNeed(l - 1) + " XP"), 70, y + 18, 12, 0xFFFFFFFF, 0, true);
            Gfx.text(c, l == 0 ? "Início da jornada" : Rules.levelRewardText(l), 70, y + 36, 10.5f, Gfx.GOLD, 0, false);
            if (past) Gfx.check(c, 326, y + (ROW - 6) / 2, 8, Gfx.GREEN);
        }
        sc.end(c);
        drawBtns(c);
    }
}

final class SettingsScreen extends Screen {
    boolean drag;
    Btn bFx, bMus, bQ;
    static final float SX = 40, SW = 280, SY = 128;

    SettingsScreen() {
        float y = 170;
        bFx = btn(30, y, 300, 46, "", 1, () -> { Game.fx = !Game.fx; Game.saveSettings(); lab(); });
        bMus = btn(30, y + 56, 300, 46, "", 1, () -> {
            Game.music = !Game.music;
            Game.saveSettings();
            if (Game.music) Audio.startMusic(); else Audio.stopMusic();
            lab();
        });
        bQ = btn(30, y + 112, 300, 46, "", 1, () -> { Game.quality = (Game.quality + 1) % 3; Game.saveSettings(); lab(); });
        Btn r = btn(30, y + 182, 300, 46, "RESETAR PROGRESSO", 2, () -> Game.push(new ConfirmScreen("Resetar progresso?",
                "Todo o progresso desta conta será apagado (a conta e a senha continuam). Você refará o quiz da casa.", () -> {
                    Game.pl.resetProgress();
                    Game.save();
                    Game.toast("Progresso resetado.");
                    Game.enter();
                })));
        Btn s = btn(30, y + 238, 300, 46, "SAIR DA CONTA", 1, () -> Game.logout());
        r.on = s.on = Game.pl != null;
        btn(30, y + 304, 300, 44, "VOLTAR", 1, () -> Game.pop());
        lab();
    }

    void lab() {
        bFx.label = "EFEITOS SONOROS: " + (Game.fx ? "LIGADO" : "DESLIGADO");
        bMus.label = "MÚSICA: " + (Game.music ? "LIGADA" : "DESLIGADA");
        bQ.label = "QUALIDADE GRÁFICA: " + (Game.quality == 0 ? "BAIXA" : Game.quality == 1 ? "MÉDIA" : "ALTA");
    }

    void setVol(float x) { Game.vol = Math.max(0f, Math.min(1f, (x - SX) / SW)); }

    @Override
    void down(float x, float y) {
        super.down(x, y);
        if (pressed == null && y > SY - 22 && y < SY + 22) { drag = true; setVol(x); }
    }

    @Override void move(float x, float y) { super.move(x, y); if (drag) setVol(x); }

    @Override
    void up(float x, float y) {
        super.up(x, y);
        if (drag) { drag = false; Game.saveSettings(); Audio.sfx(Audio.CLICK); }
    }

    @Override
    void draw(Canvas c, float dt) {
        tick(dt);
        header(c, "CONFIGURAÇÕES");
        Gfx.text(c, "VOLUME: " + Math.round(Game.vol * 100) + "%", SX, SY - 20, 14, Gfx.GOLD, 0, true);
        Gfx.bar(c, SX, SY - 4, SW, 12, Game.vol, 0xFF8E5BE0, 0xFFC9B0FF);
        c.drawCircle(SX + SW * Game.vol, SY + 2, 13, Gfx.fill(0xFFFFE9A8));
        c.drawCircle(SX + SW * Game.vol, SY + 2, 13, Gfx.stroke(0xFFB8860B, 2));
        drawBtns(c);
    }
}

final class AboutScreen extends Screen {
    final Scroll sc = new Scroll();
    static final String TXT = "ACADEMIA ARCANA é um jogo de cartas e aventura mágica ambientado em uma grande escola de fantasia. "
            + "Você chega como Novo Aprendiz — Nível 0 —, descobre a sua Casa, monta o seu deck e enfrenta enigmas, quizzes e batalhas por turnos.\n\n"
            + "O UNIVERSO\nNo alto de uma montanha coberta de névoa fica a Academia Arcana. Dizem que seus corredores mudam de lugar à noite, que os livros sussurram e que cada carta guarda um fragmento do Véu Arcano, a energia que sustenta toda a magia. "
            + "Quatro Casas formam a escola: Ignis, dos corajosos; Maris, dos estrategistas; Sylva, dos protetores; e Lunaris, dos sábios.\n\n"
            + "A PROPOSTA\nColecione mais de 60 cartas de 8 categorias e 6 raridades, evolua do nível 0 ao 50, complete missões, abra baús, "
            + "resolva enigmas e derrote chefes até conquistar o título de Arquimago. Tudo funciona offline, direto no seu aparelho.\n\n"
            + "COMO JOGAR\nNas batalhas, toque em uma carta e escolha ATACAR, DEFENDER ou HABILIDADE. Cada carta custa energia; gaste com sabedoria e passe o turno quando precisar. "
            + "Poções do inventário ajudam nos momentos difíceis.\n\n"
            + "Toda a arte, os sons e a música deste jogo foram criados de forma original, em código.";
    float h;

    AboutScreen() {
        h = Gfx.paraH(TXT, 14, 300, 20, false) + 20;
        sc.set(Game.TOP + 48, Game.H - 128, h);
        btnC(Game.H - 54, 200, 42, "VOLTAR", 1, () -> Game.pop());
    }

    @Override void down(float x, float y) { super.down(x, y); if (sc.inside(y)) sc.down(y); }
    @Override void move(float x, float y) { super.move(x, y); sc.move(y); }
    @Override void up(float x, float y) { super.up(x, y); sc.up(); }

    @Override
    void draw(Canvas c, float dt) {
        tick(dt);
        sc.update(dt);
        header(c, "SOBRE O JOGO");
        sc.begin(c);
        Gfx.para(c, TXT, 30, 6, 300, 14, Gfx.TXT, 20, 0, false);
        sc.end(c);
        Gfx.panel(c, 40, Game.H - 118, 280, 54, 14, Gfx.alpha(0xFF120C2E, 0.95f), Gfx.GOLD);
        Gfx.textC(c, "Criado por Lucas Moraes", Game.W / 2, Game.H - 91, 17, Gfx.GOLD, true);
        drawBtns(c);
    }
}

final class ConfirmScreen extends Screen {
    final String title, msg;

    ConfirmScreen(String title, String msg, Runnable yes) {
        this.title = title;
        this.msg = msg;
        overlay = true;
        float y = Game.H / 2 + 20;
        btn(36, y + 40, 140, 44, "SIM", 3, () -> { Game.pop(); yes.run(); });
        btn(184, y + 40, 140, 44, "NÃO", 1, () -> Game.pop());
    }

    @Override
    void draw(Canvas c, float dt) {
        tick(dt);
        c.drawRect(0, 0, Game.W, Game.H, Gfx.fill(0xCC000000));
        float y = Game.H / 2 - 100;
        Gfx.panel(c, 24, y, 312, 214, 18, 0xFF161036, Gfx.GOLD);
        Gfx.textS(c, title, Game.W / 2, y + 34, 20, Gfx.GOLD, 1, true);
        Gfx.para(c, msg, 44, y + 50, 272, 14, Gfx.TXT, 19, 1, false);
        drawBtns(c);
    }
}

final class LevelUpScreen extends Screen {
    final int lv;

    LevelUpScreen(int lv) {
        this.lv = lv;
        overlay = true;
        btnC(Game.H / 2 + 96, 200, 46, "CONTINUAR", 0, () -> Game.pop());
        Audio.sfx(Audio.LEVEL);
        Game.burst(Game.W / 2, Game.H / 2 - 60, Gfx.GOLD, 90);
        Game.view.ps.confetti(40);
    }

    @Override boolean allowPopups() { return false; }

    @Override
    void draw(Canvas c, float dt) {
        tick(dt);
        c.drawRect(0, 0, Game.W, Game.H, Gfx.fill(0xCC000000));
        float cx = Game.W / 2, y = Game.H / 2 - 150;
        Gfx.glow(c, cx, y + 70, 150, Gfx.GOLD, 0.45f + 0.1f * Gfx.sin(t * 4));
        Gfx.panel(c, 30, y, 300, 290, 20, 0xFF161036, Gfx.GOLD);
        Gfx.textS(c, "NÍVEL " + lv + "!", cx, y + 58, 40, Gfx.GOLD, 1, true);
        Gfx.text(c, Data.title(lv), cx, y + 84, 17, 0xFFFFFFFF, 1, true);
        Gfx.star(c, cx, y + 126, 24 + 2 * Gfx.sin(t * 5), Gfx.GOLD);
        Gfx.text(c, "RECOMPENSAS", cx, y + 176, 13, Gfx.DIM, 1, true);
        Gfx.para(c, Rules.levelRewardText(lv), 48, y + 184, 264, 14, Gfx.GREEN, 19, 1, true);
        drawBtns(c);
    }
}
