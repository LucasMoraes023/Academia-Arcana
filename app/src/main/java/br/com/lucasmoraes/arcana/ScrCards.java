package br.com.lucasmoraes.arcana;

import android.graphics.Canvas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

final class CollectionScreen extends Screen {
    static final float CW = 100, CH = 140, GX = 15, ROWH = 170;
    final Scroll sc = new Scroll();
    final ArrayList<Card> list = new ArrayList<>();
    int filter;
    Btn fb;

    CollectionScreen() {
        fb = btn(236, Game.TOP + 44, 112, 32, "", 1, () -> { filter = (filter + 1) % 7; rebuild(); });
        btnC(Game.H - 56, 200, 42, "VOLTAR", 1, () -> Game.go(new HubScreen()));
        rebuild();
    }

    void rebuild() {
        list.clear();
        for (Card d : Data.CARDS) if (filter == 0 || d.rar == filter - 1) list.add(d);
        fb.label = filter == 0 ? "TODAS" : Data.RARN[filter - 1];
        sc.off = 0;
        sc.set(Game.TOP + 86, Game.H - 66, ((list.size() + 2) / 3) * ROWH + 8);
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
        int row = (int) (cy / ROWH), col = (int) ((x - GX / 2) / (CW + GX));
        if (col < 0 || col > 2) return;
        float cx0 = GX + col * (CW + GX);
        int i = row * 3 + col;
        if (x >= cx0 && x <= cx0 + CW && cy - row * ROWH - 4 <= CH && i < list.size()) Game.push(new CardDetailScreen(list.get(i)));
    }

    @Override
    void draw(Canvas c, float dt) {
        tick(dt);
        sc.update(dt);
        Player p = Game.pl;
        header(c, "MINHAS CARTAS");
        Gfx.text(c, p.cards.size() + " / " + Data.CARDS.size() + " desbloqueadas", 16, Game.TOP + 66, 13, Gfx.GOLD, 0, true);
        float viewH = sc.bottom - sc.top;
        sc.begin(c);
        for (int i = 0; i < list.size(); i++) {
            float x = GX + (i % 3) * (CW + GX), y = (i / 3) * ROWH + 4;
            if (y + ROWH < sc.off || y > sc.off + viewH) continue;
            Card d = list.get(i);
            Gfx.card(c, d, x, y, CW, p.stars(d.id), !p.has(d.id), t, 0);
            Gfx.text(c, "Nº " + d.code() + (p.has(d.id) ? "" : "  (bloqueada)"), x + CW / 2, y + CH + 16, 10.5f, p.has(d.id) ? Gfx.DIM : Gfx.alpha(Gfx.DIM, 0.6f), 1, false);
        }
        sc.end(c);
        drawBtns(c);
    }
}

final class CardDetailScreen extends Screen {
    final Card d;
    Btn deckBtn;

    CardDetailScreen(Card d) {
        this.d = d;
        Player p = Game.pl;
        deckBtn = btn(24, Game.H - 112, 312, 46, "", 0, () -> toggle());
        deckBtn.vis = p.has(d.id);
        btn(24, Game.H - 60, 312, 42, "FECHAR", 1, () -> Game.pop());
        label();
    }

    void label() { deckBtn.label = Game.pl.deck.contains(d.id) ? "REMOVER DO DECK" : "ADICIONAR AO DECK"; }

    void toggle() {
        Player p = Game.pl;
        if (p.deck.contains(d.id)) p.deck.remove((Integer) d.id);
        else if (!p.canEquip(d)) { Game.toast("Requer nível " + d.lvl + " para usar esta carta."); return; }
        else if (p.deck.size() >= Player.DECK_MAX) { Game.toast("Deck cheio (" + Player.DECK_MAX + " cartas). Remova uma carta."); return; }
        else p.deck.add(d.id);
        Game.save();
        label();
    }

    @Override
    void draw(Canvas c, float dt) {
        tick(dt);
        Player p = Game.pl;
        float cx = Game.W / 2, w = 176, y = Game.TOP + 12;
        boolean owned = p.has(d.id);
        Gfx.card(c, d, cx - w / 2, y, w, p.stars(d.id), !owned, t, 0);
        y += w * 1.4f + 22;
        Gfx.textS(c, d.name, cx, y, 20, Gfx.mix(Data.RARC[d.rar], 0xFFFFFFFF, 0.3f), 1, true);
        y += 20;
        Gfx.text(c, Data.RARN[d.rar] + "  •  " + Data.CAT[d.cat] + "  •  Nível " + d.lvl + "  •  Nº " + d.code(), cx, y, 12, Gfx.DIM, 1, true);
        y += 22;
        float m = Player.starMult(p.stars(d.id));
        Gfx.text(c, "ATAQUE " + Math.round(d.atk * m), cx - 100, y, 13, Gfx.RED, 1, true);
        Gfx.text(c, "DEFESA " + Math.round(d.def * m), cx, y, 13, Gfx.BLUE, 1, true);
        Gfx.text(c, "ENERGIA " + d.en, cx + 100, y, 13, 0xFFFFD35A, 1, true);
        y += 12;
        String ab = d.ab == 0 ? "Sem habilidade especial." : "Habilidade " + Data.AB_NAME[d.ab] + ": " + Data.abDesc(d.ab, Math.round(d.pw * m));
        y += Gfx.para(c, ab, 30, y, 300, 13, 0xFFE9E1FF, 18, 1, true) + 2;
        y += Gfx.para(c, d.desc, 30, y, 300, 12.5f, Gfx.DIM, 17, 1, false) + 4;
        if (!owned) {
            String how = d.house >= 0 ? "Carta exclusiva da Casa " + Data.HNAME[d.house] + ": desbloqueie subindo de nível." : "Carta bloqueada. Consiga em baús, missões e fases.";
            Gfx.para(c, how, 30, y, 300, 12.5f, Gfx.GOLD, 17, 1, true);
        } else {
            for (int i = 0; i < 5; i++) Gfx.star(c, cx + (i - 2) * 22, y + 10, 8, i < p.stars(d.id) ? Gfx.GOLD : 0x55FFFFFF);
            Gfx.text(c, "Duplicatas evoluem a carta (até 5 estrelas)", cx, y + 34, 11, Gfx.DIM, 1, false);
        }
        drawBtns(c);
    }
}

final class DeckScreen extends Screen {
    static final float CW = 78, CH = 109, ROWH = 122;
    final Scroll sc = new Scroll();
    final ArrayList<Card> own = new ArrayList<>();
    float gap, slotsY;

    DeckScreen() {
        Player p = Game.pl;
        for (int id : p.cards.keySet()) own.add(Data.card(id));
        Collections.sort(own, new Comparator<Card>() {
            public int compare(Card a, Card b) { return b.rar != a.rar ? b.rar - a.rar : a.id - b.id; }
        });
        gap = (Game.W - 4 * CW) / 5;
        slotsY = Game.TOP + 72;
        sc.set(slotsY + 204, Game.H - 70, ((own.size() + 3) / 4) * ROWH + 8);
        btn(12, Game.H - 60, 108, 44, "AUTO", 3, () -> { Game.pl.autoDeck(); Game.save(); });
        btn(126, Game.H - 60, 108, 44, "LIMPAR", 2, () -> { Game.pl.deck.clear(); Game.save(); });
        btn(240, Game.H - 60, 108, 44, "VOLTAR", 1, () -> Game.go(new HubScreen()));
    }

    @Override void onBack() { Game.go(new HubScreen()); }
    @Override void down(float x, float y) { super.down(x, y); if (sc.inside(y)) sc.down(y); }
    @Override void move(float x, float y) { super.move(x, y); sc.move(y); }

    @Override
    void up(float x, float y) {
        boolean wasBtn = pressed != null;
        super.up(x, y);
        boolean tap = sc.up();
        if (wasBtn) return;
        Player p = Game.pl;
        if (y >= slotsY && y < slotsY + 188) {
            int row = y < slotsY + 93 ? 0 : 1, col = (int) ((x - 13) / 68);
            int i = row * 5 + col;
            if (col >= 0 && col < 5 && i < p.deck.size()) { p.deck.remove(i); Game.save(); Audio.sfx(Audio.CLICK); }
            return;
        }
        if (!tap || !sc.inside(y)) return;
        float cy = sc.toContent(y);
        int row = (int) (cy / ROWH), col = (int) ((x - gap / 2) / (CW + gap));
        int i = row * 4 + col;
        if (col < 0 || col > 3 || i >= own.size()) return;
        Card d = own.get(i);
        if (p.deck.contains(d.id)) p.deck.remove((Integer) d.id);
        else if (!p.canEquip(d)) { Game.toast("Requer nível " + d.lvl + "."); return; }
        else if (p.deck.size() >= Player.DECK_MAX) { Game.toast("Deck cheio! Toque numa carta do deck para remover."); return; }
        else p.deck.add(d.id);
        Audio.sfx(Audio.CLICK);
        Game.save();
    }

    @Override
    void draw(Canvas c, float dt) {
        tick(dt);
        sc.update(dt);
        Player p = Game.pl;
        header(c, "SEU DECK");
        Gfx.text(c, p.deck.size() + " / " + Player.DECK_MAX + " cartas  •  toque para trocar", Game.W / 2, Game.TOP + 60, 12.5f, Gfx.GOLD, 1, true);
        for (int i = 0; i < Player.DECK_MAX; i++) {
            float x = 13 + (i % 5) * 68, y = slotsY + (i / 5) * 96;
            if (i < p.deck.size()) Gfx.card(c, Data.card(p.deck.get(i)), x, y, 62, p.stars(p.deck.get(i)), false, t, 0);
            else {
                Gfx.panel(c, x, y, 62, 87, 8, Gfx.alpha(0xFF120C2E, 0.8f), 0x553D3170);
                Gfx.textC(c, "+", x + 31, y + 43, 26, 0x553D3170, true);
            }
        }
        Gfx.text(c, "COLEÇÃO — toque para adicionar", 16, slotsY + 200, 12, Gfx.DIM, 0, true);
        float viewH = sc.bottom - sc.top;
        sc.begin(c);
        for (int i = 0; i < own.size(); i++) {
            float x = gap + (i % 4) * (CW + gap), y = (i / 4) * ROWH + 4;
            if (y + ROWH < sc.off || y > sc.off + viewH) continue;
            Card d = own.get(i);
            boolean in = p.deck.contains(d.id), lock = !p.canEquip(d);
            Gfx.card(c, d, x, y, CW, p.stars(d.id), false, t, in ? 0.55f : lock ? 0.6f : 0f);
            if (in) Gfx.check(c, x + CW / 2, y + CH / 2, 16, Gfx.GREEN);
            if (lock) Gfx.lock(c, x + CW / 2, y + CH / 2 - 4, 12, 0xFFFFFFFF);
        }
        sc.end(c);
        drawBtns(c);
    }
}
