package br.com.lucasmoraes.arcana;

final class Card {
    final int id, cat, rar, lvl, atk, def, en, ab, pw, house;
    final String name, desc;

    Card(int id, String name, int cat, int rar, int lvl, int atk, int def, int en, int ab, int pw, int house, String desc) {
        this.id = id; this.name = name; this.cat = cat; this.rar = rar; this.lvl = lvl;
        this.atk = atk; this.def = def; this.en = en; this.ab = ab; this.pw = pw; this.house = house; this.desc = desc;
    }

    String code() { return id < 10 ? "00" + id : id < 100 ? "0" + id : String.valueOf(id); }
}

final class Enemy {
    final String name, pat, pat2;
    final int hp, atk, kind, color, xp, coins;
    final boolean boss;

    Enemy(String name, int hp, int atk, String pat, String pat2, int kind, int color, int xp, int coins, boolean boss) {
        this.name = name; this.hp = hp; this.atk = atk; this.pat = pat; this.pat2 = pat2; this.kind = kind;
        this.color = color; this.xp = xp; this.coins = coins; this.boss = boss;
    }

    Enemy scaled(float m, String newName) {
        return new Enemy(newName == null ? name : newName, Math.round(hp * m), Math.round(atk * m), pat, pat2, kind,
                color, Math.round(xp * m), Math.round(coins * m), boss);
    }
}

final class Item {
    final int id, kind, hp, en;
    final String name, desc;

    Item(int id, int kind, String name, String desc, int hp, int en) {
        this.id = id; this.kind = kind; this.name = name; this.desc = desc; this.hp = hp; this.en = en;
    }
}

final class Reward {
    final int xp, coins, card, item, itemN, chest;

    Reward(int xp, int coins, int card, int item, int itemN, int chest) {
        this.xp = xp; this.coins = coins; this.card = card; this.item = item; this.itemN = itemN; this.chest = chest;
    }
}

final class Quest {
    final int id, kind, target;
    final String name, desc, key;
    final Reward rw;

    Quest(int id, int kind, String name, String desc, String key, int target, Reward rw) {
        this.id = id; this.kind = kind; this.name = name; this.desc = desc; this.key = key; this.target = target; this.rw = rw;
    }
}

final class Ach {
    final int id, target, coins;
    final String name, desc, key;

    Ach(int id, String name, String desc, String key, int target, int coins) {
        this.id = id; this.name = name; this.desc = desc; this.key = key; this.target = target; this.coins = coins;
    }
}

final class Step {
    static final int STORY = 0, CARD = 1, QUIZ = 2, RIDDLE = 3, BATTLE = 4;
    final int type, a;
    final String s;
    final int[] qs;

    Step(int type, int a, String s, int[] qs) { this.type = type; this.a = a; this.s = s; this.qs = qs; }
}

final class Phase {
    final int ch, idx, rec;
    final String title, desc;
    final Step[] steps;
    final Reward rw;

    Phase(int ch, int idx, String title, String desc, Step[] steps, Reward rw, int rec) {
        this.ch = ch; this.idx = idx; this.title = title; this.desc = desc; this.steps = steps; this.rw = rw; this.rec = rec;
    }

    String key() { return ch + "-" + idx; }
}

final class QQ {
    final int type, a, arg;
    final String q;
    final String[] o;

    QQ(int type, String q, String[] o, int a, int arg) { this.type = type; this.q = q; this.o = o; this.a = a; this.arg = arg; }
}

final class Riddle {
    static final int SEQ = 0, SYM = 1, CODE = 2, COMBO = 3, MEM = 4, LOGIC = 5;
    final int type, ans, n;
    final String title, text;
    final int[] a, b;
    final String[] s;

    Riddle(int type, String title, String text, int[] a, int[] b, String[] s, int ans, int n) {
        this.type = type; this.title = title; this.text = text; this.a = a; this.b = b; this.s = s; this.ans = ans; this.n = n;
    }
}

final class HQ {
    final int w;
    final String q;
    final String[] ans;
    final int[][] v;

    HQ(int w, String q, String[] ans, int[][] v) { this.w = w; this.q = q; this.ans = ans; this.v = v; }
}

final class ChestResult {
    int[] cards = new int[0];
    int[] kinds = new int[0];
    long coins;
    int frag;
}
