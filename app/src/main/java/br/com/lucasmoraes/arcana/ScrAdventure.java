package br.com.lucasmoraes.arcana;

import android.graphics.Canvas;

/** Controla a sequência de etapas (história, quiz, enigma, batalha) de uma fase e salva o progresso. */
final class Runner {
    static Phase cur;

    static void start(Phase ph) {
        Player p = Game.pl;
        boolean resume = ph.key().equals(p.runPhase);
        if (!resume) {
            if (ph.ch > 0 && !p.isAdmin() && !p.spendEnergy(Player.PHASE_COST)) {
                Game.toast("Energia insuficiente! Aguarde recuperar (1 a cada 90 s).");
                return;
            }
            p.runPhase = ph.key();
            p.runStep = 0;
        }
        if (p.runStep >= ph.steps.length) p.runStep = 0;
        cur = ph;
        Game.save();
        show();
    }

    static void show() {
        Player p = Game.pl;
        if (cur == null) { Game.go(new HubScreen()); return; }
        Step s = cur.steps[p.runStep];
        switch (s.type) {
            case Step.STORY: Game.go(new StoryScreen(cur.title, s.s.split("\\|"))); break;
            case Step.CARD:
                if (!p.has(1)) { for (int id : Data.STARTER) p.giveCard(id); p.autoDeck(); Game.save(); }
                Game.go(new CardRevealScreen(new int[]{1}, new int[]{0}, "SUA PRIMEIRA CARTA", "Você também recebeu o Kit do Aprendiz: mais 7 cartas e um deck pronto!", () -> next()));
                break;
            case Step.QUIZ: Game.go(new QuizScreen(cur.title, s.qs)); break;
            case Step.RIDDLE: Game.go(new PuzzleScreen(Data.RIDDLES.get(s.a), cur.title)); break;
            default:
                Game.go(new BattleScreen(Data.ENEMIES.get(s.a), false, cur.ch == 0, () -> next(), () -> quit()));
        }
    }

    static void next() {
        Player p = Game.pl;
        p.runStep++;
        Game.save();
        if (cur == null || p.runStep >= cur.steps.length) finish(); else show();
    }

    static void finish() {
        Reward r = Rules.completePhase(Game.pl, cur);
        Game.save();
        Game.go(new PhaseCompleteScreen(cur, r));
    }

    static void quit() {
        Game.save();
        if (Game.pl != null && (Game.pl.tutorialDone() || Game.pl.isAdmin())) Game.go(new AdventureScreen(cur == null || cur.ch == 0 ? 0 : cur.ch));
        else Game.go(new TitleScreen());
    }
}

final class AdventureScreen extends Screen {
    int ch;
    final Scroll sc = new Scroll();
    Btn prev, nextB;
    static final float ROW = 88;

    AdventureScreen(int startCh) {
        Player p = Game.pl;
        ch = startCh;
        if (ch <= 0) {
            ch = 1;
            while (ch < 3 && p.chapterUnlocked(ch + 1) && p.done.contains(ch + "-" + Data.phaseCount(ch))) ch++;
        }
        prev = btn(14, Game.TOP + 42, 44, 38, "<", 1, () -> { ch--; refresh(); });
        nextB = btn(302, Game.TOP + 42, 44, 38, ">", 1, () -> { ch++; refresh(); });
        btnC(Game.H - 56, 200, 42, "VOLTAR", 1, () -> Game.go(new HubScreen()));
        sc.set(Game.TOP + 112, Game.H - 66, 10);
        refresh();
    }

    void refresh() {
        prev.on = ch > 1;
        nextB.on = ch < 3;
        sc.off = 0;
        sc.set(Game.TOP + 112, Game.H - 66, Data.phaseCount(ch) * ROW + 10);
    }

    @Override void onBack() { Game.go(new HubScreen()); }
    @Override void down(float x, float y) { super.down(x, y); if (sc.inside(y)) sc.down(y); }
    @Override void move(float x, float y) { super.move(x, y); sc.move(y); }

    @Override
    void up(float x, float y) {
        boolean wasBtn = pressed != null;
        super.up(x, y);
        boolean tap = sc.up();
        if (wasBtn || !tap || !sc.inside(y) || !Game.pl.chapterUnlocked(ch)) return;
        int i = (int) (sc.toContent(y) / ROW) + 1;
        Phase ph = Data.phase(ch, i);
        if (ph == null) return;
        if (Game.pl.phaseUnlocked(ph)) Game.push(new PhaseInfoScreen(ph));
        else { Game.toast("Fase bloqueada. Conclua a fase anterior."); Audio.sfx(Audio.FAIL); }
    }

    @Override
    void draw(Canvas c, float dt) {
        tick(dt);
        sc.update(dt);
        Player p = Game.pl;
        p.refreshEnergy();
        header(c, "AVENTURA");
        Gfx.textC(c, Data.CHAPTERS[ch], Game.W / 2, Game.TOP + 61, 14, Gfx.GOLD, true);
        Gfx.bolt(c, 120, Game.TOP + 98, 8, 0xFFFFE08A);
        Gfx.text(c, "Energia " + p.energy + "/" + p.maxEnergy() + "  (cada fase custa " + Player.PHASE_COST + ")", 134, Game.TOP + 102, 11.5f, 0xFFFFE08A, 0, true);
        if (!p.chapterUnlocked(ch)) {
            Gfx.lock(c, Game.W / 2, 260, 34, Gfx.DIM);
            Gfx.para(c, "Capítulo bloqueado.\nConclua o capítulo anterior para liberar.", 40, 310, 280, 15, Gfx.TXT, 22, 1, true);
            drawBtns(c);
            return;
        }
        int n = Data.phaseCount(ch);
        sc.begin(c);
        for (int i = 1; i <= n; i++) {
            Phase ph = Data.phase(ch, i);
            float y = (i - 1) * ROW + 4, cy = y + 38;
            boolean done = p.done.contains(ph.key()), open = p.phaseUnlocked(ph), prog = ph.key().equals(p.runPhase);
            if (i < n) c.drawLine(46, cy + 22, 46, cy + ROW - 22, Gfx.stroke(done ? Gfx.alpha(Gfx.GREEN, 0.6f) : 0x553D3170, 3));
            Gfx.panel(c, 14, y, 332, ROW - 8, 14, open ? Gfx.alpha(0xFF1A1240, 0.95f) : Gfx.alpha(0xFF0E0A22, 0.9f), done ? Gfx.alpha(Gfx.GREEN, 0.7f) : open ? Gfx.GOLD : 0x443D3170);
            int nc = done ? Gfx.GREEN : prog ? Gfx.BLUE : open ? Gfx.GOLD : 0xFF4A4668;
            c.drawCircle(46, cy, 20, Gfx.fill(0xFF0B0820));
            c.drawCircle(46, cy, 20, Gfx.stroke(nc, 2.5f));
            if (done) Gfx.check(c, 46, cy, 10, nc);
            else if (!open) Gfx.lock(c, 46, cy - 2, 8, nc);
            else Gfx.textC(c, String.valueOf(i), 46, cy, 18, nc, true);
            Gfx.text(c, "FASE " + i + " — " + ph.title.replaceFirst("^[^—]*— ", ""), 76, y + 24, 14, open ? 0xFFFFFFFF : Gfx.DIM, 0, true);
            Gfx.para(c, ph.desc, 76, y + 30, 262, 11, Gfx.DIM, 14, 0, false);
            Gfx.text(c, (prog ? "EM ANDAMENTO • " : "") + "Nv. recomendado " + ph.rec, 76, y + ROW - 18, 10.5f, prog ? Gfx.BLUE : Gfx.GOLD, 0, true);
        }
        sc.end(c);
        drawBtns(c);
    }
}

final class PhaseInfoScreen extends Screen {
    final Phase ph;

    PhaseInfoScreen(Phase ph) {
        this.ph = ph;
        overlay = true;
        boolean resume = ph.key().equals(Game.pl.runPhase);
        btn(30, Game.H - 128, 300, 48, resume ? "CONTINUAR FASE" : "INICIAR FASE", 0, () -> { Game.pop(); Runner.start(ph); });
        btn(30, Game.H - 72, 300, 42, "FECHAR", 1, () -> Game.pop());
    }

    @Override
    void draw(Canvas c, float dt) {
        tick(dt);
        c.drawRect(0, 0, Game.W, Game.H, Gfx.fill(0xDD000000));
        float y = Game.TOP + 14;
        Gfx.panel(c, 16, y, 328, Game.H - y - 144, 18, 0xFF161036, Gfx.GOLD);
        float h = Gfx.para(c, ph.title, 30, y + 14, 300, 19, Gfx.GOLD, 24, 1, true);
        y += 20 + h;
        y += Gfx.para(c, ph.desc, 32, y, 296, 13, Gfx.TXT, 18, 1, false) + 10;
        Gfx.text(c, "ETAPAS", 32, y + 12, 12, Gfx.DIM, 0, true);
        y += 22;
        for (Step s : ph.steps) {
            String l;
            switch (s.type) {
                case Step.STORY: l = "História"; break;
                case Step.CARD: l = "Receber sua primeira carta"; break;
                case Step.QUIZ: l = "Quiz (" + s.qs.length + " perguntas)"; break;
                case Step.RIDDLE: l = "Enigma: " + Data.RIDDLES.get(s.a).title; break;
                default: l = "Batalha: " + Data.ENEMIES.get(s.a).name;
            }
            c.drawCircle(40, y + 8, 3.5f, Gfx.fill(Gfx.GOLD));
            Gfx.text(c, l, 52, y + 13, 13, 0xFFFFFFFF, 0, false);
            y += 21;
        }
        y += 8;
        Gfx.text(c, "RECOMPENSAS", 32, y + 12, 12, Gfx.DIM, 0, true);
        y += 20;
        y += Gfx.para(c, Rules.describe(ph.rw), 32, y, 296, 13, Gfx.GREEN, 18, 0, true) + 8;
        boolean resume = ph.key().equals(Game.pl.runPhase);
        if (ph.ch > 0 && !resume && !Game.pl.isAdmin()) Gfx.text(c, "Custo: " + Player.PHASE_COST + " de energia", 32, y + 14, 12, 0xFFFFE08A, 0, true);
        drawBtns(c);
    }
}

final class StoryScreen extends Screen {
    final String title;
    final String[] pages;
    int page;
    float shown;

    StoryScreen(String title, String[] pages) { this.title = title; this.pages = pages; }

    @Override void onBack() { Runner.quit(); }

    @Override
    void up(float x, float y) {
        boolean wasBtn = pressed != null;
        super.up(x, y);
        if (wasBtn) return;
        if (shown < pages[page].length()) { shown = pages[page].length(); return; }
        Audio.sfx(Audio.CLICK);
        if (page + 1 < pages.length) { page++; shown = 0; } else Runner.next();
    }

    @Override
    void draw(Canvas c, float dt) {
        tick(dt);
        shown += dt * 48;
        float cx = Game.W / 2;
        Gfx.houseEmblem(c, Game.pl.house, cx, Game.TOP + 62, 34, t);
        Gfx.textS(c, title, cx, Game.TOP + 130, 17, Gfx.GOLD, 1, true);
        float py = Game.TOP + 150, ph = Game.H - py - 130;
        Gfx.panel(c, 18, py, 324, ph, 18, Gfx.alpha(0xFF161036, 0.95f), Gfx.alpha(Gfx.GOLD, 0.6f));
        String s = pages[page], vis = s.substring(0, Math.min(s.length(), (int) shown));
        Gfx.para(c, vis, 38, py + 18, 284, 16, 0xFFFFFFFF, 24, 0, false);
        for (int i = 0; i < pages.length; i++) c.drawCircle(cx + (i - (pages.length - 1) / 2f) * 16, py + ph - 16, 4, Gfx.fill(i == page ? Gfx.GOLD : 0x55FFFFFF));
        if (shown >= s.length() && (int) (t * 2) % 2 == 0) Gfx.text(c, "toque para continuar", cx, Game.H - 90, 13, Gfx.DIM, 1, false);
    }
}

final class CardRevealScreen extends Screen {
    final int[] ids, kinds;
    final String title, sub;
    final Runnable done;
    int i;
    float flip = -1;
    boolean fired;

    CardRevealScreen(int[] ids, int[] kinds, String title, String sub, Runnable done) {
        this.ids = ids; this.kinds = kinds; this.title = title; this.sub = sub; this.done = done;
    }

    @Override void onBack() { }

    @Override
    void up(float x, float y) {
        if (flip < 0) { flip = 0; fired = false; return; }
        if (flip < 1) return;
        Audio.sfx(Audio.CLICK);
        if (i + 1 < ids.length) { i++; flip = -1; } else done.run();
    }

    @Override
    void draw(Canvas c, float dt) {
        tick(dt);
        float cx = Game.W / 2, w = 190, h = 266, y = Math.max(Game.TOP + 66, (Game.H - h) / 2 - 30);
        Gfx.textS(c, title, cx, Game.TOP + 30, 22, Gfx.GOLD, 1, true);
        if (ids.length > 1) Gfx.text(c, (i + 1) + " de " + ids.length, cx, Game.TOP + 50, 13, Gfx.DIM, 1, true);
        Card d = Data.card(ids[i]);
        if (flip >= 0 && flip < 1) flip = Math.min(1, flip + dt * 2.2f);
        if (flip >= 0.5f && !fired) {
            fired = true;
            int rc = Data.RARC[d.rar];
            Game.burst(cx, y + h / 2, rc, 40 + d.rar * 25);
            Audio.sfx(d.rar >= 3 ? Audio.LEVEL : Audio.OK);
            if (d.rar >= 4) Game.view.ps.confetti(30);
        }
        float sx = flip < 0 ? 1 : flip < 0.5f ? 1 - flip * 2 : flip * 2 - 1;
        c.save();
        c.translate(cx, y + h / 2);
        c.scale(Math.max(0.02f, sx), 1);
        c.translate(-cx, -(y + h / 2));
        if (flip < 0.5f) Gfx.cardBack(c, cx - w / 2, y, w, d.rar, t);
        else Gfx.card(c, d, cx - w / 2, y, w, 1, false, t, 0);
        c.restore();
        if (flip >= 1) {
            Gfx.textS(c, d.name, cx, y + h + 30, 19, Gfx.mix(Data.RARC[d.rar], 0xFFFFFFFF, 0.3f), 1, true);
            int k = kinds != null && i < kinds.length ? kinds[i] : 0;
            String msg = k == 0 ? "NOVA CARTA!" : k == 1 ? "DUPLICATA! A carta evoluiu (+ estrela)" : "DUPLICATA MÁXIMA! Convertida em moedas";
            Gfx.text(c, msg, cx, y + h + 52, 13, k == 0 ? Gfx.GREEN : Gfx.GOLD, 1, true);
            if (i + 1 >= ids.length && sub != null) Gfx.para(c, sub, 30, y + h + 60, 300, 13, Gfx.TXT, 18, 1, false);
            if ((int) (t * 2) % 2 == 0) Gfx.text(c, "toque para continuar", cx, Game.H - 24, 13, Gfx.DIM, 1, false);
        } else if (flip < 0 && (int) (t * 2) % 2 == 0) Gfx.text(c, "toque para revelar", cx, Game.H - 40, 15, Gfx.GOLD, 1, true);
    }
}

final class QuizScreen extends Screen {
    final String title;
    final int[] qs;
    int idx, correct, sel = -1, shuffled[];
    float wait;
    boolean finished, pass;
    Reward rw;
    float qBottom;

    QuizScreen(String title, int[] qs) { this.title = title; this.qs = qs; build(); }

    void build() {
        btns.clear();
        sel = -1;
        QQ q = Data.QUIZ.get(qs[idx]);
        int n = q.o.length;
        shuffled = new int[n];
        for (int i = 0; i < n; i++) shuffled[i] = i;
        if (q.type != 1) for (int i = n - 1; i > 0; i--) { int j = Game.rnd.nextInt(i + 1), tmp = shuffled[i]; shuffled[i] = shuffled[j]; shuffled[j] = tmp; }
        boolean icon = q.type == 4;
        float qh = Gfx.paraH(q.q, 16, 280, 22, true);
        qBottom = Game.TOP + 86 + qh + 22 + (icon ? 90 : 0);
        float y = qBottom + 16;
        for (int i = 0; i < n; i++) {
            final int k = i;
            btn(24, y + i * 62, 312, 52, q.o[shuffled[i]], 1, () -> pick(k));
        }
    }

    void pick(int k) {
        if (sel >= 0) return;
        QQ q = Data.QUIZ.get(qs[idx]);
        sel = k;
        boolean ok = shuffled[k] == q.a;
        if (ok) correct++;
        Rules.quizAnswer(Game.pl, ok);
        Audio.sfx(ok ? Audio.OK : Audio.FAIL);
        for (int i = 0; i < btns.size(); i++) {
            Btn b = btns.get(i);
            b.on = false;
            if (shuffled[i] == q.a) { b.style = 3; b.on = true; } else if (i == k) { b.style = 2; b.on = true; }
        }
        wait = 1.1f;
    }

    void advance() {
        idx++;
        if (idx >= qs.length) {
            finished = true;
            btns.clear();
            pass = correct * 100 >= 60 * qs.length;
            if (pass) {
                rw = Rules.quizWin(Game.pl, correct, qs.length);
                Game.save();
                Game.burst(Game.W / 2, 240, Gfx.GOLD, 60);
                btnC(Game.H - 110, 260, 48, "CONTINUAR", 0, () -> Runner.next());
            } else {
                btnC(Game.H - 110, 260, 48, "TENTAR NOVAMENTE", 0, () -> { idx = 0; correct = 0; finished = false; build(); });
                btnC(Game.H - 54, 260, 42, "SAIR", 1, () -> Runner.quit());
            }
        } else build();
    }

    @Override void onBack() { Runner.quit(); }

    @Override
    void draw(Canvas c, float dt) {
        tick(dt);
        float cx = Game.W / 2;
        if (finished) {
            Gfx.textS(c, pass ? "QUIZ CONCLUÍDO!" : "QUASE LÁ!", cx, Game.TOP + 70, 26, pass ? Gfx.GOLD : Gfx.RED, 1, true);
            Gfx.textS(c, "Acertos: " + correct + " de " + qs.length, cx, Game.TOP + 120, 20, 0xFFFFFFFF, 1, true);
            if (pass && rw != null) Gfx.para(c, "Recompensas: " + Rules.describe(rw), 40, Game.TOP + 150, 280, 16, Gfx.GREEN, 22, 1, true);
            else Gfx.para(c, "Você precisa acertar pelo menos 60% para avançar. Tente de novo!", 40, Game.TOP + 150, 280, 15, Gfx.TXT, 21, 1, false);
            drawBtns(c);
            return;
        }
        QQ q = Data.QUIZ.get(qs[idx]);
        Gfx.text(c, title, cx, Game.TOP + 20, 13, Gfx.DIM, 1, true);
        Gfx.text(c, Data.QTYPE[q.type] + "  •  " + (idx + 1) + "/" + qs.length, cx, Game.TOP + 42, 13, Gfx.GOLD, 1, true);
        Gfx.bar(c, 40, Game.TOP + 52, 280, 8, idx / (float) qs.length, 0xFF8E5BE0, 0xFFC9B0FF);
        float top = Game.TOP + 70;
        Gfx.panel(c, 16, top, 328, qBottom - top, 14, Gfx.alpha(0xFF120C2E, 0.92f), Gfx.alpha(Gfx.GOLD, 0.5f));
        float y = top + 14;
        if (q.type == 4) {
            c.drawCircle(cx, y + 38, 38, Gfx.fill(0xFF0B0820));
            c.drawCircle(cx, y + 38, 38, Gfx.stroke(Gfx.GOLD, 2));
            if (q.arg >= 100) Gfx.glyph(c, q.arg - 100, cx, y + 38, 22, Data.HCOL[q.arg - 100 < 4 ? q.arg - 100 : 0]);
            else Gfx.catIcon(c, q.arg, cx, y + 38, 24, Data.CATC[q.arg]);
            y += 86;
        }
        Gfx.para(c, q.q, 40, y, 280, 16, 0xFFFFFFFF, 22, 1, true);
        drawBtns(c);
        if (sel >= 0) {
            wait -= dt;
            if (wait <= 0) advance();
        }
    }
}

final class PhaseCompleteScreen extends Screen {
    final Phase ph;
    final Reward rw;

    PhaseCompleteScreen(Phase ph, Reward rw) {
        this.ph = ph;
        this.rw = rw;
        Audio.sfx(Audio.LEVEL);
        Game.view.ps.confetti(70);
        btnC(Game.H - 100, 260, 50, "CONTINUAR", 0, () -> { if (ph.ch == 0) Game.go(new HubScreen()); else Game.go(new AdventureScreen(ph.ch)); });
    }

    @Override void onBack() { }

    @Override
    void draw(Canvas c, float dt) {
        tick(dt);
        float cx = Game.W / 2, y = Game.TOP + 40;
        Gfx.glow(c, cx, y + 70, 160, Gfx.GOLD, 0.4f + 0.1f * Gfx.sin(t * 3));
        Gfx.textS(c, ph.ch == 0 ? "NÍVEL 0 CONCLUÍDO!" : "FASE CONCLUÍDA!", cx, y + 30, 28, Gfx.GOLD, 1, true);
        Gfx.star(c, cx, y + 90, 30 + 3 * Gfx.sin(t * 4), Gfx.GOLD);
        float h = Gfx.para(c, ph.title, 40, y + 134, 280, 17, 0xFFFFFFFF, 23, 1, true);
        Gfx.panel(c, 30, y + 150 + h, 300, 150, 16, Gfx.alpha(0xFF161036, 0.95f), Gfx.GOLD);
        Gfx.text(c, "RECOMPENSAS", cx, y + 176 + h, 13, Gfx.DIM, 1, true);
        Gfx.para(c, Rules.describe(rw), 46, y + 184 + h, 268, 15, Gfx.GREEN, 21, 1, true);
        String nx = ph.ch == 0 ? "A Fase 1 foi desbloqueada!" : ph.idx == Data.phaseCount(ph.ch) && ph.ch < 3 ? "Novo capítulo liberado!" : "Próxima fase desbloqueada!";
        Gfx.text(c, nx, cx, y + 140 + h + 150, 14, Gfx.BLUE, 1, true);
        drawBtns(c);
    }
}
