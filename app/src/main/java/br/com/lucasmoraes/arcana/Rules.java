package br.com.lucasmoraes.arcana;

import java.util.ArrayList;
import java.util.Random;

final class Rules {
    static final Random RND = new Random();
    static final int[] CHEST_CARDS = {1, 2, 2, 3, 3};
    static final int[][] CHEST_W = {{70, 25, 5, 0, 0, 0}, {30, 40, 25, 5, 0, 0}, {0, 20, 45, 30, 5, 0}, {0, 0, 25, 45, 27, 3}, {0, 0, 0, 30, 55, 15}};
    static final int[][] CHEST_COINS = {{30, 60}, {100, 200}, {300, 500}, {800, 1200}, {2000, 3000}};

    static Reward levelReward(int lvl, int house) {
        int coins = 40 + 15 * lvl;
        int chest = -1;
        if (lvl % 5 == 0) chest = lvl < 10 ? 0 : lvl < 20 ? 1 : lvl < 30 ? 2 : lvl < 40 ? 3 : 4;
        int card = 0;
        if (house >= 0) {
            if (lvl == 5) card = Data.exclusive(house, 0);
            else if (lvl == 15) card = Data.exclusive(house, 1);
            else if (lvl == 30) card = Data.exclusive(house, 2);
        }
        int item = lvl % 2 == 0 ? 1 : 0;
        return new Reward(0, coins, card, item, item, chest);
    }

    static String levelRewardText(int lvl) {
        Reward r = levelReward(lvl, -1);
        StringBuilder b = new StringBuilder(r.coins + " moedas");
        if (r.item > 0) b.append(", Poção de Vida");
        if (r.chest >= 0) b.append(", Baú ").append(Data.CHEST_NAMES[r.chest]);
        if (lvl == 5 || lvl == 15 || lvl == 30) b.append(", Carta exclusiva da Casa");
        return b.toString();
    }

    static String describe(Reward r) {
        ArrayList<String> p = new ArrayList<>();
        if (r.xp > 0) p.add(r.xp + " XP");
        if (r.coins > 0) p.add(r.coins + " moedas");
        if (r.card > 0) { Card c = Data.card(r.card); if (c != null) p.add("Carta " + c.name); }
        if (r.item > 0 && Data.ITEMS[r.item] != null) p.add(Math.max(1, r.itemN) + "x " + Data.ITEMS[r.item].name);
        if (r.chest >= 0) p.add("Baú " + Data.CHEST_NAMES[r.chest]);
        if (p.isEmpty()) return "—";
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < p.size(); i++) { if (i > 0) b.append(", "); b.append(p.get(i)); }
        return b.toString();
    }

    static void giveReward(Player p, Reward r, boolean bonus) {
        if (r.xp > 0) p.addXp(r.xp, bonus);
        if (r.coins > 0) p.addCoins(r.coins, bonus);
        if (r.card > 0) p.giveCard(r.card);
        if (r.item > 0) p.giveItem(r.item, Math.max(1, r.itemN));
        if (r.chest >= 0) p.giveChest(r.chest, 1);
        p.checkAch();
    }

    static int roll(int[] w, Random r) {
        int t = 0;
        for (int v : w) t += v;
        int x = r.nextInt(Math.max(1, t));
        for (int i = 0; i < w.length; i++) { x -= w[i]; if (x < 0) return i; }
        return 0;
    }

    static int pick(int rar, int house, Random r) {
        for (int d = 0; d < 6; d++) {
            for (int sgn = -1; sgn <= 1; sgn += 2) {
                int rr = rar + sgn * d;
                if (rr < 0 || rr > 5) continue;
                ArrayList<Card> pool = new ArrayList<>();
                for (Card c : Data.CARDS) if (c.rar == rr && (c.house < 0 || c.house == house)) pool.add(c);
                if (!pool.isEmpty()) return pool.get(r.nextInt(pool.size())).id;
            }
        }
        return 1;
    }

    static ChestResult openChest(Player p, int tier, Random r) {
        ChestResult res = new ChestResult();
        int n = CHEST_CARDS[tier];
        res.cards = new int[n];
        res.kinds = new int[n];
        for (int i = 0; i < n; i++) {
            int id = pick(roll(CHEST_W[tier], r), p.house, r);
            res.cards[i] = id;
            res.kinds[i] = p.giveCard(id);
        }
        int[] cr = CHEST_COINS[tier];
        res.coins = p.addCoins(cr[0] + r.nextInt(cr[1] - cr[0] + 1), true);
        if (r.nextInt(100) < 40) { res.frag = 1 + r.nextInt(3); p.giveItem(4, res.frag); }
        p.inc("chests", 1);
        p.checkAch();
        return res;
    }

    static Reward completePhase(Player p, Phase ph) {
        boolean first = !p.done.contains(ph.key());
        Reward w = ph.rw;
        Reward out = new Reward(first ? w.xp : w.xp * 30 / 100, first ? w.coins : w.coins * 30 / 100,
                first ? w.card : 0, first ? w.item : 0, first ? w.itemN : 0, first ? w.chest : -1);
        giveReward(p, out, true);
        if (first) {
            p.done.add(ph.key());
            if (ph.ch > 0) p.inc("phases", 1);
            if (ph.ch >= 1 && ph.ch < 3 && ph.idx == Data.phaseCount(ph.ch)) {
                p.chapters.add(ph.ch + 1);
                p.notices.add("Novo capítulo liberado!");
            }
        }
        p.runPhase = "";
        p.runStep = 0;
        p.checkAch();
        return out;
    }

    static Reward battleWin(Player p, Enemy e, boolean practice) {
        if (practice) return new Reward(0, 0, 0, 0, 0, -1);
        int fr = RND.nextInt(100) < 22 ? 1 + RND.nextInt(2) : 0;
        long xp = p.addXp(e.xp, true), coins = p.addCoins(e.coins, true);
        p.inc("battles_won", 1);
        if (e.boss && e.name.equals("Mestre das Sombras")) p.inc("boss1", 1);
        if (fr > 0) p.giveItem(4, fr);
        p.checkAch();
        return new Reward((int) xp, (int) coins, 0, fr > 0 ? 4 : 0, fr, -1);
    }

    static void battleLose(Player p, boolean practice) { if (!practice) p.inc("battles_lost", 1); }

    static Reward quizWin(Player p, int correct, int total) {
        long xp = p.addXp(correct * 6 + (correct == total ? 10 : 0), true);
        long coins = p.addCoins(correct * 4, true);
        p.checkAch();
        return new Reward((int) xp, (int) coins, 0, 0, 0, -1);
    }

    static void quizAnswer(Player p, boolean ok) {
        p.inc("quiz_done", 1);
        if (ok) p.inc("quiz_correct", 1);
        p.checkAch();
    }

    static Reward riddleSolved(Player p) {
        long xp = p.addXp(12, true), coins = p.addCoins(10, true);
        p.inc("riddles", 1);
        p.checkAch();
        return new Reward((int) xp, (int) coins, 0, 0, 0, -1);
    }
}
