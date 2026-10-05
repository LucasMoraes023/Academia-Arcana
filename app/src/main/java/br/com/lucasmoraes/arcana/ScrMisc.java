package br.com.lucasmoraes.arcana;

import android.graphics.Canvas;

import java.util.ArrayList;

final class QuestsScreen extends Screen {
    static final float ROW = 92;
    int tab;
    final Scroll sc = new Scroll();
    final Btn[] tabs = new Btn[3];
    final ArrayList<Quest> list = new ArrayList<>();

    QuestsScreen() {
        String[] n = {"PRINCIPAIS", "SECUNDÁRIAS", "DIÁRIAS"};
        for (int i = 0; i < 3; i++) { final int k = i; tabs[i] = btn(10 + i * 117, Game.TOP + 44, 112, 34, n[i], i == 0 ? 0 : 1, () -> { tab = k; rebuild(); }); }
        btnC(Game.H - 56, 200, 42, "VOLTAR", 1, () -> Game.go(new HubScreen()));
        rebuild();
    }

    void rebuild() {
        list.clear();
        for (Quest q : Data.QUESTS) if (q.kind == tab) list.add(q);
        for (int i = 0; i < 3; i++) tabs[i].style = i == tab ? 0 : 1;
        sc.off = 0;
        sc.set(Game.TOP + 88, Game.H - 66, list.size() * ROW + 6);
    }

    @Override void onBack() { Game.go(new HubScreen()); }
    @Override void down(float x, float y) { super.down(x, y); if (sc.inside(y)) sc.down(y); }
    @Override void move(float x, float y) { super.move(x, y); sc.move(y); }

    @Override
    void up(float x, float y) {
        boolean wasBtn = pressed != null;
        super.up(x, y);
        boolean tap = sc.up();
        if (wasBtn || !tap || !sc.inside(y)) return;
        float cy = sc.toContent(y);
        int i = (int) (cy / ROW);
        if (i < 0 || i >= list.size()) return;
        float by = i * ROW + 4 + 26;
        if (x >= 246 && x <= 340 && cy >= by && cy <= by + 34) {
            Player p = Game.pl;
            Quest q = list.get(i);
            if (p.claimQuest(q)) { Game.save(); Audio.sfx(Audio.OK); Game.burst(x, y, Gfx.GOLD, 30); }
            else if (!p.qDone(q)) Game.toast("Missão ainda não concluída.");
        }
    }

    @Override
    void draw(Canvas c, float dt) {
        tick(dt);
        sc.update(dt);
        Player p = Game.pl;
        header(c, "MISSÕES");
        float viewH = sc.bottom - sc.top;
        sc.begin(c);
        for (int i = 0; i < list.size(); i++) {
            Quest q = list.get(i);
            float y = i * ROW + 4;
            if (y + ROW < sc.off || y > sc.off + viewH) continue;
            boolean cl = p.qClaimed(q), done = p.qDone(q);
            Gfx.panel(c, 10, y, 340, ROW - 8, 14, Gfx.alpha(0xFF1A1240, 0.95f), cl ? Gfx.alpha(Gfx.GREEN, 0.6f) : done ? Gfx.GOLD : 0x553D3170);
            Gfx.text(c, q.name, 24, y + 22, 15, 0xFFFFFFFF, 0, true);
            Gfx.text(c, q.desc, 24, y + 40, 12, Gfx.DIM, 0, false);
            long pr = cl ? q.target : p.qProg(q);
            Gfx.bar(c, 24, y + 48, 200, 10, pr / (float) q.target, done ? 0xFF2FB070 : 0xFF8E5BE0, done ? 0xFF7CF0B0 : 0xFFC9B0FF);
            Gfx.text(c, pr + "/" + q.target, 232, y + 57, 11, Gfx.TXT, 0, true);
            Gfx.text(c, "Recompensa: " + Rules.describe(q.rw), 24, y + 74, 10.5f, Gfx.GOLD, 0, false);
            float by = y + 26;
            if (cl) { Gfx.check(c, 296, by + 17, 12, Gfx.GREEN); }
            else if (done) {
                Gfx.panel(c, 246, by, 94, 34, 10, 0xFFB8860B, 0xFFFFF2B3);
                Gfx.textC(c, "RESGATAR", 293, by + 17, 12.5f, 0xFF3A2600, true);
            } else Gfx.panel(c, 246, by, 94, 34, 10, 0x663A3650, 0x446B6785);
        }
        sc.end(c);
        if (tab == 2) Gfx.text(c, "As missões diárias reiniciam todos os dias.", Game.W / 2, Game.H - 66, 11, Gfx.DIM, 1, false);
        drawBtns(c);
    }
}

final class InventoryScreen extends Screen {
    final Scroll sc = new Scroll();
    final ArrayList<int[]> rows = new ArrayList<>();

    InventoryScreen() {
        btnC(Game.H - 56, 200, 42, "VOLTAR", 1, () -> Game.go(new HubScreen()));
        rebuild();
    }

    void rebuild() {
        Player p = Game.pl;
        rows.clear();
        float h = 6;
        for (int k = 0; k < 5; k++) {
            boolean any = false;
            for (int id = 1; id < Data.ITEMS.length; id++)
                if (Data.ITEMS[id].kind == k && p.count(id) > 0) {
                    if (!any) { rows.add(new int[]{0, k}); h += 32; any = true; }
                    rows.add(new int[]{1, id});
                    h += 64;
                }
        }
        sc.set(Game.TOP + 52, Game.H - 66, h);
    }

    @Override void onBack() { Game.go(new HubScreen()); }
    @Override void down(float x, float y) { super.down(x, y); if (sc.inside(y)) sc.down(y); }
    @Override void move(float x, float y) { super.move(x, y); sc.move(y); }

    @Override
    void up(float x, float y) {
        boolean wasBtn = pressed != null;
        super.up(x, y);
        boolean tap = sc.up();
        if (wasBtn || !tap || !sc.inside(y)) return;
        float cy = sc.toContent(y), yy = 6;
        for (int[] r : rows) {
            float hh = r[0] == 0 ? 32 : 64;
            if (r[0] == 1 && cy >= yy && cy < yy + hh && r[1] == 4 && x >= 250 && Game.pl.count(4) >= 10) {
                for (int i = 0; i < 10; i++) Game.pl.useItem(4);
                Game.pl.giveChest(0, 1);
                Game.save();
                Audio.sfx(Audio.OK);
                Game.toast("Baú Comum forjado com 10 fragmentos!");
                rebuild();
                return;
            }
            yy += hh;
        }
    }

    @Override
    void draw(Canvas c, float dt) {
        tick(dt);
        sc.update(dt);
        Player p = Game.pl;
        header(c, "INVENTÁRIO");
        if (rows.isEmpty()) Gfx.para(c, "Seu inventário está vazio. Complete fases, missões e abra baús para conseguir itens!", 40, 200, 280, 15, Gfx.TXT, 22, 1, false);
        float viewH = sc.bottom - sc.top;
        sc.begin(c);
        float y = 6;
        for (int[] r : rows) {
            if (r[0] == 0) {
                Gfx.text(c, Data.ITEM_KIND[r[1]], 18, y + 22, 13, Gfx.GOLD, 0, true);
                c.drawLine(18, y + 28, 342, y + 28, Gfx.stroke(Gfx.alpha(Gfx.GOLD, 0.35f), 1));
                y += 32;
                continue;
            }
            Item it = Data.ITEMS[r[1]];
            if (y + 64 >= sc.off && y <= sc.off + viewH) {
                Gfx.panel(c, 10, y, 340, 58, 12, Gfx.alpha(0xFF1A1240, 0.95f), 0x553D3170);
                float cx = 40, cy = y + 29;
                c.drawCircle(cx, cy, 20, Gfx.fill(0xFF0B0820));
                switch (it.kind) {
                    case 0: Gfx.catIcon(c, 3, cx, cy, 13, it.hp > 0 ? 0xFFE0508E : 0xFFFFD35A); break;
                    case 1: Gfx.catIcon(c, 2, cx, cy, 13, 0xFF8EF3FF); break;
                    case 2: c.drawCircle(cx - 4, cy - 4, 6, Gfx.stroke(Gfx.GOLD, 3)); c.drawLine(cx, cy, cx + 10, cy + 10, Gfx.stroke(Gfx.GOLD, 3)); c.drawLine(cx + 6, cy + 6, cx + 10, cy + 2, Gfx.stroke(Gfx.GOLD, 3)); break;
                    case 3: Gfx.glyph(c, 5, cx, cy, 13, Gfx.GOLD); break;
                    default: Gfx.icon(c, 4, cx, cy, 12, 0xFFC9B0FF);
                }
                Gfx.text(c, it.name + "  x" + p.count(it.id), 70, y + 24, 14, 0xFFFFFFFF, 0, true);
                Gfx.para(c, it.desc, 70, y + 28, r[1] == 4 ? 170 : 270, 11, Gfx.DIM, 14, 0, false);
                if (r[1] == 4) {
                    boolean ok = p.count(4) >= 10;
                    Gfx.panel(c, 250, y + 12, 90, 34, 10, ok ? 0xFFB8860B : 0x663A3650, ok ? 0xFFFFF2B3 : 0x446B6785);
                    Gfx.textC(c, "FORJAR BAÚ", 295, y + 29, 11.5f, ok ? 0xFF3A2600 : 0xFF9A96B0, true);
                }
            }
            y += 64;
        }
        sc.end(c);
        drawBtns(c);
    }
}

final class ChestsScreen extends Screen {
    final Btn[] open = new Btn[5], buy = new Btn[5];

    ChestsScreen() {
        for (int i = 0; i < 5; i++) {
            final int k = i;
            float y = rowY(i);
            open[i] = btn(238, y + 8, 106, 32, "ABRIR", 3, () -> open(k));
            buy[i] = btn(238, y + 44, 106, 30, "", 4, () -> buy(k));
        }
        btnC(Game.H - 56, 200, 42, "VOLTAR", 1, () -> Game.go(new HubScreen()));
    }

    float rowY(int i) { return Game.TOP + 48 + i * 88; }
    @Override void onBack() { Game.go(new HubScreen()); }

    void open(int k) {
        Player p = Game.pl;
        if (p.chests[k] <= 0) return;
        p.chests[k]--;
        Game.go(new ChestOpenScreen(k));
    }

    void buy(int k) {
        Player p = Game.pl;
        if (p.coins < Data.CHEST_PRICE[k]) { Game.toast("Moedas insuficientes."); return; }
        p.coins -= Data.CHEST_PRICE[k];
        p.chests[k]++;
        Game.save();
        Audio.sfx(Audio.OK);
        Game.toast("Baú " + Data.CHEST_NAMES[k] + " comprado!");
    }

    @Override
    void draw(Canvas c, float dt) {
        tick(dt);
        Player p = Game.pl;
        header(c, "BAÚS");
        Gfx.coin(c, 262, Game.TOP + 62, 8);
        Gfx.text(c, String.valueOf(p.coins), 276, Game.TOP + 67, 14, Gfx.GOLD, 0, true);
        for (int i = 0; i < 5; i++) {
            float y = rowY(i) + 12 - 12;
            Gfx.panel(c, 10, y, 340, 82, 14, Gfx.alpha(0xFF1A1240, 0.95f), Gfx.alpha(i == 4 ? Gfx.hue(t * 90) : Data.RARC[i + 1 > 5 ? 5 : i + 1], 0.8f));
            Gfx.chest(c, i, 62, y + 46, 0.5f, 0, t);
            Gfx.text(c, "BAÚ " + Data.CHEST_NAMES[i], 116, y + 28, 15, 0xFFFFFFFF, 0, true);
            Gfx.text(c, "No inventário: " + p.chests[i], 116, y + 48, 12, p.chests[i] > 0 ? Gfx.GREEN : Gfx.DIM, 0, true);
            Gfx.text(c, Rules.CHEST_CARDS[i] + " carta(s) + moedas", 116, y + 66, 11, Gfx.DIM, 0, false);
            open[i].on = p.chests[i] > 0;
            buy[i].label = "COMPRAR " + Data.CHEST_PRICE[i];
            buy[i].on = p.coins >= Data.CHEST_PRICE[i];
        }
        drawBtns(c);
    }
}

final class ChestOpenScreen extends Screen {
    final int tier;
    final ChestResult res;
    boolean burst, going;

    ChestOpenScreen(int tier) {
        this.tier = tier;
        res = Rules.openChest(Game.pl, tier, Game.rnd);
        Game.save();
    }

    @Override void onBack() { }

    void next() {
        if (going) return;
        going = true;
        String sub = "+" + res.coins + " moedas" + (res.frag > 0 ? " e " + res.frag + "x Fragmento Arcano" : "");
        Game.go(new CardRevealScreen(res.cards, res.kinds, "BAÚ " + Data.CHEST_NAMES[tier], sub, () -> Game.go(new ChestsScreen())));
    }

    @Override
    void up(float x, float y) { if (t > 1.9f) next(); }

    @Override
    void draw(Canvas c, float dt) {
        tick(dt);
        float cx = Game.W / 2, cy = Game.H / 2;
        float open = t < 1.7f ? 0 : Math.min(1f, (t - 1.7f) / 0.5f);
        if (t > 1.72f && !burst) {
            burst = true;
            Audio.sfx(Audio.CHEST);
            Game.burst(cx, cy - 30, 0xFFFFE08A, 90);
            Game.view.ps.confetti(30);
        }
        Gfx.textS(c, "BAÚ " + Data.CHEST_NAMES[tier], cx, Game.TOP + 40, 26, Gfx.GOLD, 1, true);
        c.save();
        if (t < 1.7f) c.rotate(Gfx.sin(t * 30) * (2 + t * 5), cx, cy + 30);
        if (t < 1.7f) Gfx.glow(c, cx, cy, 90 + t * 40, 0xFFFFE08A, 0.15f + t * 0.2f);
        Gfx.chest(c, tier, cx, cy, 1.4f, open, t);
        c.restore();
        if (t > 1.9f && (int) (t * 2) % 2 == 0) Gfx.text(c, "toque para ver as recompensas", cx, Game.H - 60, 14, Gfx.DIM, 1, true);
        if (t > 3.2f) next();
    }
}

final class AchievementsScreen extends Screen {
    static final float ROW = 70;
    final Scroll sc = new Scroll();

    AchievementsScreen() {
        sc.set(Game.TOP + 70, Game.H - 66, Data.ACHS.size() * ROW + 6);
        btnC(Game.H - 56, 200, 42, "VOLTAR", 1, () -> Game.go(new HubScreen()));
    }

    @Override void onBack() { Game.go(new HubScreen()); }
    @Override void down(float x, float y) { super.down(x, y); if (sc.inside(y)) sc.down(y); }
    @Override void move(float x, float y) { super.move(x, y); sc.move(y); }
    @Override void up(float x, float y) { super.up(x, y); sc.up(); }

    @Override
    void draw(Canvas c, float dt) {
        tick(dt);
        sc.update(dt);
        Player p = Game.pl;
        header(c, "CONQUISTAS");
        Gfx.text(c, p.ach.size() + " / " + Data.ACHS.size() + " conquistadas", Game.W / 2, Game.TOP + 62, 13, Gfx.GOLD, 1, true);
        float viewH = sc.bottom - sc.top;
        sc.begin(c);
        for (int i = 0; i < Data.ACHS.size(); i++) {
            Ach a = Data.ACHS.get(i);
            float y = i * ROW + 4;
            if (y + ROW < sc.off || y > sc.off + viewH) continue;
            boolean ok = p.ach.contains(a.id);
            Gfx.panel(c, 10, y, 340, ROW - 8, 14, Gfx.alpha(0xFF1A1240, 0.95f), ok ? Gfx.GOLD : 0x443D3170);
            c.drawCircle(42, y + 31, 22, Gfx.fill(0xFF0B0820));
            Gfx.icon(c, 7, 42, y + 31, 14, ok ? Gfx.GOLD : 0xFF4A4668);
            Gfx.text(c, a.name, 76, y + 22, 13.5f, ok ? Gfx.GOLD : 0xFFFFFFFF, 0, true);
            Gfx.text(c, a.desc, 76, y + 38, 11, Gfx.DIM, 0, false);
            long v = Math.min(p.stat(a.key), a.target);
            Gfx.bar(c, 76, y + 45, 160, 8, v / (float) a.target, ok ? 0xFFB8860B : 0xFF8E5BE0, ok ? 0xFFF2C14E : 0xFFC9B0FF);
            Gfx.text(c, v + "/" + a.target, 244, y + 53, 10, Gfx.TXT, 0, true);
            Gfx.text(c, "+" + a.coins, 340, y + 24, 12, Gfx.GOLD, 2, true);
            if (ok) Gfx.check(c, 322, y + 42, 7, Gfx.GREEN);
        }
        sc.end(c);
        drawBtns(c);
    }
}

final class AdminScreen extends Screen {
    final ArrayList<String> log = new ArrayList<>();
    Btn god;

    AdminScreen() {
        if (Game.pl == null || !Game.pl.isAdmin()) { Game.toast("Acesso negado."); return; }
        log.add("Painel pronto. Use os botões ou o console.");
        float y = Game.TOP + 124, w = 166, h = 40, g = 8;
        add(12, y, w, h, "+ MOEDAS", 0, () -> out(Admin.addCoins(Game.pl, 1000)));
        add(182, y, w, h, "+ XP", 0, () -> out(Admin.addXp(Game.pl, 1000)));
        y += h + g;
        add(12, y, w, h, "SETAR NÍVEL", 0, () -> Game.ask("Definir nível (0-50)", "ex.: 20", false, "", s -> {
            try { out(Admin.setLevel(Game.pl, Integer.parseInt(s.trim()))); } catch (NumberFormatException ex) { out("Valor inválido."); }
        }));
        add(182, y, w, h, "LIBERAR TODAS AS CARTAS", 0, () -> out(Admin.unlockCards(Game.pl)));
        y += h + g;
        add(12, y, w, h, "LIBERAR TODAS AS FASES", 0, () -> out(Admin.unlockPhases(Game.pl)));
        add(182, y, w, h, "LIBERAR TODOS OS CAPÍTULOS", 0, () -> out(Admin.unlockChapters(Game.pl)));
        y += h + g;
        add(12, y, w, h, "COMPLETAR MISSÕES", 0, () -> out(Admin.completeQuests(Game.pl)));
        add(182, y, w, h, "RESTAURAR ENERGIA", 0, () -> out(Admin.restoreEnergy(Game.pl)));
        y += h + g;
        add(12, y, w, h, "ALTERAR CASA", 0, () -> out(Admin.setHouse(Game.pl, Game.pl.house + 1)));
        add(182, y, w, h, "RESETAR PROGRESSO", 2, () -> Game.push(new ConfirmScreen("Resetar progresso?", "Todo o progresso do ADMIN será apagado.", () -> {
            out(Admin.reset(Game.pl));
            Game.save();
            Game.enter();
        })));
        y += h + g;
        god = add(12, y, w, h, "", 4, () -> { out(Admin.toggleGod(Game.pl)); godLabel(); });
        add(182, y, w, h, "TESTAR BATALHA", 3, () -> Game.go(new BattleScreen(Data.training(Math.max(1, Game.pl.level), Game.rnd.nextInt(30)), true, false,
                () -> Game.go(new AdminScreen()), () -> Game.go(new AdminScreen()))));
        y += h + g;
        add(12, y, 336, h, "CONSOLE DE COMANDOS", 4, () -> Game.ask("Console ADMIN", "/givecoins 999999  (ou /help)", false, "", s -> { out(Admin.exec(Game.pl, s)); Game.save(); }));
        y += h + g;
        add(12, y, w, h, "ALTERAR SENHA ADMIN", 1, () -> Game.ask("Nova senha do ADMIN", "mínimo 4 caracteres", true, "", s -> {
            String e = Game.acc.changePassword("ADMIN", s);
            out(e == null ? "Senha do ADMIN alterada." : e);
        }));
        add(182, y, w, h, "INICIAR TUTORIAL", 1, () -> Runner.start(Data.phase(0, 1)));
        y += h + g;
        add(12, y, 336, 40, "VOLTAR", 1, () -> Game.go(new HubScreen()));
        godLabel();
    }

    Btn add(float x, float y, float w, float h, String l, int st, Runnable r) { return btn(x, y, w, h, l, st, r); }
    void godLabel() { if (god != null) god.label = "MODO DEUS: " + (Game.pl.god ? "LIGADO" : "DESLIGADO"); }

    void out(String s) {
        log.add(s);
        while (log.size() > 4) log.remove(0);
        Game.save();
    }

    @Override void onBack() { Game.go(new HubScreen()); }

    @Override
    void draw(Canvas c, float dt) {
        tick(dt);
        header(c, "PAINEL ADMINISTRADOR");
        Player p = Game.pl;
        Gfx.panel(c, 12, Game.TOP + 42, 336, 74, 12, 0xEE060410, Gfx.alpha(Gfx.GREEN, 0.7f));
        for (int i = 0; i < log.size(); i++) Gfx.text(c, "> " + log.get(i), 20, Game.TOP + 58 + i * 15, 11, i == log.size() - 1 ? Gfx.GREEN : Gfx.alpha(Gfx.GREEN, 0.6f), 0, false);
        Gfx.text(c, "Nv " + p.level + " | " + p.coins + " moedas | cartas " + p.cards.size(), 340, Game.TOP + 112, 10, Gfx.DIM, 2, false);
        drawBtns(c);
    }
}
