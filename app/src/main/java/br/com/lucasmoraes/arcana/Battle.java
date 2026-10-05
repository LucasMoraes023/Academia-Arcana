package br.com.lucasmoraes.arcana;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

final class Fx {
    static final int DMG = 0, HEAL = 1, BLOCK = 2, INFO = 3, ENERGY = 4;
    final int side, kind, amt;   // side: 0 = jogador, 1 = inimigo
    final String text;

    Fx(int side, int kind, int amt, String text) { this.side = side; this.kind = kind; this.amt = amt; this.text = text; }
}

final class Battle {
    static final int HAND = 4, USE_ATK = 0, USE_DEF = 1, USE_AB = 2;
    final Player p;
    final Enemy e;
    final boolean practice;
    final Random r = new Random();
    int hp, maxHp, energy, maxEnergy = 8, block, turn = 1, state;   // state: 0 jogando, 2 vitória, 3 derrota
    int ehp, emax, eblock, estun, burnD, burnT, poisD, poisT, pPoisD, pPoisT, fury, eIdx;
    boolean enraged, freeUsed;
    char next = 'A';
    String intent = "";
    final ArrayList<Integer> draw = new ArrayList<>(), discard = new ArrayList<>(), hand = new ArrayList<>();
    final ArrayList<Fx> fx = new ArrayList<>();

    Battle(Player p, Enemy e, boolean practice) {
        this.p = p; this.e = e; this.practice = practice;
        maxHp = p.maxHp();
        hp = maxHp;
        energy = Math.min(maxEnergy, p.startEnergy());
        ehp = emax = e.hp;
        p.cleanDeck();
        draw.addAll(p.deck);
        Collections.shuffle(draw, r);
        drawCards(HAND);
        planIntent();
    }

    void drawCards(int n) {
        for (int i = 0; i < n && hand.size() < 7; i++) {
            if (draw.isEmpty()) {
                if (discard.isEmpty()) break;
                draw.addAll(discard);
                discard.clear();
                Collections.shuffle(draw, r);
            }
            hand.add(draw.remove(draw.size() - 1));
        }
    }

    int enemyAtk() { return Math.round(e.atk * (enraged ? 1.25f : 1f)); }

    void planIntent() {
        String pat = enraged && e.pat2.length() > 0 ? e.pat2 : e.pat;
        next = pat.charAt(eIdx % pat.length());
        eIdx++;
        int a = enemyAtk();
        switch (next) {
            case 'A': intent = "Ataque " + a; break;
            case 'H': intent = "Golpe forte " + (int) (a * 1.7f); break;
            case 'D': intent = "Defende " + (int) (a * 1.2f); break;
            case 'P': intent = "Veneno " + (a / 2) + " + 3/turno"; break;
            default: intent = "Drena energia " + (a / 2);
        }
        if (estun > 0) intent = "Atordoado";
    }

    private void spend(int hi, Card c) {
        energy -= c.en;
        hand.remove(hi);
        discard.add(c.id);
    }

    private void checkEnemy() {
        if (ehp <= 0) { ehp = 0; state = 2; return; }
        if (e.boss && !enraged && ehp * 2 <= emax) {
            enraged = true;
            eIdx = 0;
            fx.add(new Fx(1, Fx.INFO, 0, "O chefe se enfurece!"));
            planIntent();
        }
    }

    void hitEnemy(int d) {
        if (p.god) d = 9999;
        int abs = Math.min(eblock, d);
        eblock -= abs;
        ehp -= d - abs;
        fx.add(new Fx(1, Fx.DMG, d - abs, abs > 0 ? "(" + abs + " bloqueado)" : ""));
        checkEnemy();
    }

    private void dot(int d, String label) {
        ehp -= d;
        fx.add(new Fx(1, Fx.DMG, d, label));
        checkEnemy();
    }

    private void hitPlayer(int d) {
        if (p.god) d = 0;
        int abs = Math.min(block, d);
        block -= abs;
        hp -= d - abs;
        fx.add(new Fx(0, Fx.DMG, d - abs, p.god ? "Imune" : abs > 0 ? "(" + abs + " bloqueado)" : ""));
        if (hp <= 0) { hp = 0; state = 3; }
    }

    private void heal(int h) {
        int real = Math.max(0, Math.min(maxHp - hp, h));
        hp += real;
        fx.add(new Fx(0, Fx.HEAL, real, ""));
    }

    private void ability(int ab, int pw) {
        switch (ab) {
            case Data.AB_HEAL: heal(pw); break;
            case Data.AB_BURN: burnD = Math.max(burnD, pw); burnT = 3; fx.add(new Fx(1, Fx.INFO, 0, "Queimando!")); break;
            case Data.AB_STUN: estun = 1; intent = "Atordoado"; fx.add(new Fx(1, Fx.INFO, 0, "Atordoado!")); break;
            case Data.AB_DRAIN: hitEnemy(pw); if (state == 0) heal(pw / 2); break;
            case Data.AB_SHIELD: block += pw; fx.add(new Fx(0, Fx.BLOCK, pw, "Escudo")); break;
            case Data.AB_ENERGY: energy = Math.min(maxEnergy, energy + pw); fx.add(new Fx(0, Fx.ENERGY, pw, "+" + pw + " energia")); break;
            case Data.AB_DRAW: drawCards(pw); fx.add(new Fx(0, Fx.INFO, 0, "Comprou cartas")); break;
            case Data.AB_FURY: fury += pw; fx.add(new Fx(0, Fx.INFO, 0, "Fúria +" + pw)); break;
            case Data.AB_POISON: poisD = Math.max(poisD, pw); poisT = 5; fx.add(new Fx(1, Fx.INFO, 0, "Envenenado!")); break;
            default: break;
        }
    }

    /** Usa a carta da mão. Retorna mensagem de erro ou null. */
    String play(int hi, int use) {
        if (state != 0) return "A batalha terminou.";
        if (hi < 0 || hi >= hand.size()) return "Selecione uma carta.";
        Card c = Data.card(hand.get(hi));
        float sm = Player.starMult(p.stars(c.id));
        if (energy < c.en) return "Energia insuficiente.";
        if (use == USE_ATK) {
            if (c.atk <= 0) return "Esta carta não ataca.";
            int d = Math.round(c.atk * sm * p.dmgMult()) + fury;
            fury = 0;
            spend(hi, c);
            hitEnemy(d);
        } else if (use == USE_DEF) {
            if (c.def <= 0) return "Esta carta não defende.";
            int b = Math.round(c.def * sm * p.defMult());
            spend(hi, c);
            block += b;
            fx.add(new Fx(0, Fx.BLOCK, b, "Escudo"));
        } else {
            if (c.ab == Data.AB_NONE) return "Esta carta não tem habilidade.";
            spend(hi, c);
            ability(c.ab, Math.round(c.pw * sm));
        }
        return null;
    }

    String basicAttack() {
        if (state != 0) return "A batalha terminou.";
        if (freeUsed) return "Ação básica já usada neste turno.";
        freeUsed = true;
        int d = Math.round((4 + p.level / 2f) * p.dmgMult()) + fury;
        fury = 0;
        hitEnemy(d);
        return null;
    }

    String basicDefend() {
        if (state != 0) return "A batalha terminou.";
        if (freeUsed) return "Ação básica já usada neste turno.";
        freeUsed = true;
        int b = Math.round((3 + p.level / 2f) * p.defMult());
        block += b;
        fx.add(new Fx(0, Fx.BLOCK, b, "Escudo"));
        return null;
    }

    String usePotion(int id) {
        if (state != 0) return "A batalha terminou.";
        Item it = id > 0 && id < Data.ITEMS.length ? Data.ITEMS[id] : null;
        if (it == null || it.kind != 0) return "Item inválido.";
        if (p.count(id) <= 0) return "Você não tem esta poção.";
        p.useItem(id);
        if (it.hp > 0) heal(it.hp);
        if (it.en > 0) { energy = Math.min(maxEnergy, energy + it.en); fx.add(new Fx(0, Fx.ENERGY, it.en, "+" + it.en + " energia")); }
        return null;
    }

    boolean canDoAnything() {
        if (!freeUsed) return true;
        for (int id : hand) if (Data.card(id).en <= energy) return true;
        return false;
    }

    /** Encerra o turno do jogador e executa a fase do inimigo. Os efeitos ficam em fx. */
    void endTurn() {
        if (state != 0) return;
        eblock = 0;
        if (burnT > 0) { dot(burnD, "Queimadura"); burnT--; }
        if (state == 0 && poisT > 0) { dot(poisD, "Veneno"); poisT--; }
        if (state != 0) return;
        if (estun > 0) {
            estun--;
            fx.add(new Fx(1, Fx.INFO, 0, "Atordoado!"));
        } else {
            int a = enemyAtk();
            switch (next) {
                case 'A': hitPlayer(a); break;
                case 'H': hitPlayer((int) (a * 1.7f)); break;
                case 'D': eblock += (int) (a * 1.2f); fx.add(new Fx(1, Fx.BLOCK, (int) (a * 1.2f), "Escudo")); break;
                case 'P':
                    hitPlayer(a / 2);
                    pPoisD = Math.max(pPoisD, 3 + e.atk / 6);
                    pPoisT = 3;
                    fx.add(new Fx(0, Fx.INFO, 0, "Envenenado!"));
                    break;
                default:
                    hitPlayer(a / 2);
                    energy = Math.max(0, energy - 2);
                    fx.add(new Fx(0, Fx.ENERGY, -2, "-2 energia"));
            }
        }
        if (state != 0) return;
        turn++;
        block = 0;
        freeUsed = false;
        if (pPoisT > 0) {
            int d = p.god ? 0 : pPoisD;
            hp -= d;
            pPoisT--;
            fx.add(new Fx(0, Fx.DMG, d, "Veneno"));
            if (hp <= 0) { hp = 0; state = 3; return; }
        }
        energy = Math.min(maxEnergy, energy + p.energyRegen());
        drawCards(HAND - hand.size());
        planIntent();
    }
}
