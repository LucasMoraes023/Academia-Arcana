package br.com.lucasmoraes.arcana;

import java.text.SimpleDateFormat;
import java.util.*;

final class Player {
    static final int MAX_LEVEL = 50, DECK_MAX = 10, PHASE_COST = 3;
    String nick;
    int house = -1, level = 0, energy, runStep = 0;
    long xp = 0, coins = 0, energyTs = System.currentTimeMillis();
    boolean god, allPh, allCh;
    String runPhase = "", dailyDate = "";
    final HashMap<Integer, Integer> cards = new HashMap<>(), items = new HashMap<>();
    final ArrayList<Integer> deck = new ArrayList<>();
    final int[] chests = new int[5];
    final HashMap<String, Long> stats = new HashMap<>(), daily = new HashMap<>();
    final HashSet<Integer> claimed = new HashSet<>(), dclaimed = new HashSet<>(), ach = new HashSet<>(), chapters = new HashSet<>();
    final HashSet<String> done = new HashSet<>();
    final ArrayList<String> notices = new ArrayList<>();
    final ArrayList<Integer> levelUps = new ArrayList<>();

    Player(String nick) {
        this.nick = nick;
        chapters.add(1);
        energy = maxEnergy();
    }

    boolean isAdmin() { return nick != null && nick.equalsIgnoreCase("ADMIN"); }
    static long xpNeed(int lvl) { return lvl >= MAX_LEVEL ? 0 : 30 + 10L * lvl + 2L * lvl * lvl; }
    int count(int item) { Integer v = items.get(item); return v == null ? 0 : v; }
    int stars(int id) { Integer v = cards.get(id); return v == null ? 0 : v; }
    boolean has(int id) { return stars(id) > 0; }
    int maxHp() { return 60 + level * 4 + (house == 2 ? 15 : 0) + (count(8) > 0 ? 10 : 0); }
    float dmgMult() { return 1f + (house == 0 ? 0.15f : 0f) + (count(9) > 0 ? 0.10f : 0f); }
    float defMult() { return 1f + (house == 2 ? 0.20f : 0f); }
    int energyRegen() { return 3 + (house == 1 ? 1 : 0); }
    int startEnergy() { return 4 + (count(10) > 0 ? 1 : 0); }
    float xpMult() { return 1f + (house == 3 ? 0.20f : 0f); }
    float coinMult() { return 1f + (count(11) > 0 ? 0.10f : 0f); }
    int maxEnergy() { return 30 + level; }
    boolean tutorialDone() { return done.contains("0-1"); }
    static float starMult(int stars) { return 1f + 0.12f * (Math.max(1, stars) - 1); }

    // ---------- energia (stamina das fases) ----------
    void refreshEnergy() {
        long now = System.currentTimeMillis();
        int max = maxEnergy();
        if (energy >= max) { energy = max; energyTs = now; return; }
        if (now < energyTs) energyTs = now;
        long gained = (now - energyTs) / 90000L;
        if (gained > 0) {
            energy = (int) Math.min(max, energy + gained);
            energyTs += gained * 90000L;
            if (energy >= max) energyTs = now;
        }
    }

    boolean spendEnergy(int n) {
        refreshEnergy();
        if (energy < n) return false;
        energy -= n;
        return true;
    }

    void restoreEnergy() { energy = maxEnergy(); energyTs = System.currentTimeMillis(); }

    int secondsToNextEnergy() {
        refreshEnergy();
        if (energy >= maxEnergy()) return 0;
        return (int) Math.max(0, 90 - (System.currentTimeMillis() - energyTs) / 1000L);
    }

    // ---------- progressão ----------
    long addXp(long n, boolean bonus) {
        if (n <= 0) return 0;
        if (bonus) n = Math.round(n * xpMult());
        if (level >= MAX_LEVEL) return n;
        xp += n;
        while (level < MAX_LEVEL && xp >= xpNeed(level)) {
            xp -= xpNeed(level);
            level++;
            levelUps.add(level);
            notices.add("NÍVEL " + level + " alcançado!");
            Rules.giveReward(this, Rules.levelReward(level, house), false);
        }
        if (level >= MAX_LEVEL) xp = 0;
        checkAch();
        return n;
    }

    long addCoins(long n, boolean bonus) {
        if (n <= 0) return 0;
        if (bonus) n = Math.round(n * coinMult());
        coins += n;
        return n;
    }

    int giveCard(int id) {
        Card c = Data.card(id);
        if (c == null) return -1;
        int st = stars(id), kind;
        if (st == 0) { cards.put(id, 1); kind = 0; }
        else if (st < 5) { cards.put(id, st + 1); kind = 1; }
        else { addCoins(10L * (c.rar + 1) * (c.rar + 1), false); kind = 2; }
        if (c.rar >= 2) inc("rare_card", 1);
        if (c.rar >= 3) inc("epic_card", 1);
        if (c.rar >= 4) inc("legend_card", 1);
        if (c.rar == 5) inc("myth_card", 1);
        checkAch();
        return kind;
    }

    void giveItem(int id, int n) { if (id > 0 && n > 0) items.put(id, count(id) + n); }
    boolean useItem(int id) { int c = count(id); if (c <= 0) return false; if (c == 1) items.remove(id); else items.put(id, c - 1); return true; }
    void giveChest(int tier, int n) { if (tier >= 0 && tier < 5) chests[tier] += n; }

    // ---------- estatísticas e diárias ----------
    static String today() { return new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date()); }

    void checkDaily() {
        String t = today();
        if (!t.equals(dailyDate)) { dailyDate = t; daily.clear(); dclaimed.clear(); }
    }

    long stat(String k) {
        if (k.equals("level")) return level;
        if (k.equals("cards")) return cards.size();
        if (k.equals("coins")) return coins;
        Long v = stats.get(k);
        return v == null ? 0 : v;
    }

    long dstat(String k) {
        checkDaily();
        if (k.equals("level")) return level;
        if (k.equals("cards")) return cards.size();
        if (k.equals("coins")) return coins;
        Long v = daily.get(k);
        return v == null ? 0 : v;
    }

    void inc(String k, long n) {
        checkDaily();
        stats.put(k, stat(k) + n);
        daily.put(k, dstat(k) + n);
        checkAch();
    }

    long qProg(Quest q) { return Math.min(q.kind == 2 ? dstat(q.key) : stat(q.key), q.target); }
    boolean qClaimed(Quest q) { checkDaily(); return q.kind == 2 ? dclaimed.contains(q.id) : claimed.contains(q.id); }
    boolean qDone(Quest q) { return qClaimed(q) || qProg(q) >= q.target; }

    boolean claimQuest(Quest q) {
        if (!qDone(q) || qClaimed(q)) return false;
        if (q.kind == 2) dclaimed.add(q.id); else claimed.add(q.id);
        Rules.giveReward(this, q.rw, true);
        notices.add("Missão concluída: " + q.name);
        return true;
    }

    void checkAch() {
        for (Ach a : Data.ACHS) {
            if (!ach.contains(a.id) && stat(a.key) >= a.target) {
                ach.add(a.id);
                addCoins(a.coins, false);
                notices.add("Conquista: " + a.name + " (+" + a.coins + " moedas)");
            }
        }
    }

    // ---------- fases ----------
    boolean chapterUnlocked(int ch) { return isAdmin() || allCh || ch <= 0 || chapters.contains(ch); }

    boolean phaseUnlocked(Phase ph) {
        if (isAdmin() || allPh) return true;
        if (ph.ch == 0) return true;
        if (!tutorialDone() || !chapters.contains(ph.ch)) return false;
        if (ph.idx == 1) return true;
        Phase prev = Data.phase(ph.ch, ph.idx - 1);
        return prev == null || done.contains(prev.key());
    }

    // ---------- deck ----------
    boolean canEquip(Card c) { return c.lvl <= Math.max(1, level) || isAdmin(); }

    static int score(Card c) { return c.rar * 100 + c.atk + c.def + c.pw; }

    void autoDeck() {
        deck.clear();
        ArrayList<Card> list = new ArrayList<>();
        for (int id : cards.keySet()) { Card c = Data.card(id); if (c != null && canEquip(c)) list.add(c); }
        Collections.sort(list, new Comparator<Card>() {
            public int compare(Card a, Card b) { return score(b) - score(a); }
        });
        for (Card c : list) { if (deck.size() >= DECK_MAX) break; deck.add(c.id); }
    }

    void cleanDeck() {
        for (int i = deck.size() - 1; i >= 0; i--) {
            Card c = Data.card(deck.get(i));
            if (c == null || !has(c.id) || !canEquip(c)) deck.remove(i);
        }
        while (deck.size() > DECK_MAX) deck.remove(deck.size() - 1);
        if (deck.isEmpty()) autoDeck();
    }

    void resetProgress() {
        house = -1; level = 0; xp = 0; coins = 0; god = false; allPh = false; allCh = false;
        runPhase = ""; runStep = 0; dailyDate = "";
        cards.clear(); items.clear(); deck.clear(); stats.clear(); daily.clear();
        claimed.clear(); dclaimed.clear(); ach.clear(); chapters.clear(); done.clear(); notices.clear(); levelUps.clear();
        for (int i = 0; i < 5; i++) chests[i] = 0;
        chapters.add(1);
        restoreEnergy();
    }

    // ---------- serialização ----------
    static String mi(Map<Integer, Integer> m) {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<Integer, Integer> e : m.entrySet()) { if (b.length() > 0) b.append(','); b.append(e.getKey()).append(':').append(e.getValue()); }
        return b.toString();
    }

    static String ml(Map<String, Long> m) {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String, Long> e : m.entrySet()) { if (b.length() > 0) b.append(','); b.append(e.getKey()).append(':').append(e.getValue()); }
        return b.toString();
    }

    static String joinI(Collection<Integer> s) {
        StringBuilder b = new StringBuilder();
        for (int v : s) { if (b.length() > 0) b.append(','); b.append(v); }
        return b.toString();
    }

    static String joinS(Collection<String> s) {
        StringBuilder b = new StringBuilder();
        for (String v : s) { if (b.length() > 0) b.append(','); b.append(v); }
        return b.toString();
    }

    static void pmi(String s, Map<Integer, Integer> m) {
        m.clear();
        if (s == null || s.isEmpty()) return;
        for (String t : s.split(",")) {
            String[] kv = t.split(":");
            if (kv.length == 2) try { m.put(Integer.parseInt(kv[0]), Integer.parseInt(kv[1])); } catch (NumberFormatException ex) { }
        }
    }

    static void pml(String s, Map<String, Long> m) {
        m.clear();
        if (s == null || s.isEmpty()) return;
        for (String t : s.split(",")) {
            String[] kv = t.split(":");
            if (kv.length == 2) try { m.put(kv[0], Long.parseLong(kv[1])); } catch (NumberFormatException ex) { }
        }
    }

    static void pli(String s, Collection<Integer> c) {
        c.clear();
        if (s == null || s.isEmpty()) return;
        for (String t : s.split(",")) try { c.add(Integer.parseInt(t.trim())); } catch (NumberFormatException ex) { }
    }

    static void pls(String s, Collection<String> c) {
        c.clear();
        if (s == null || s.isEmpty()) return;
        for (String t : s.split(",")) if (!t.isEmpty()) c.add(t);
    }

    String ser() {
        StringBuilder b = new StringBuilder();
        b.append("nick=").append(nick).append('\n');
        b.append("house=").append(house).append('\n');
        b.append("level=").append(level).append('\n');
        b.append("xp=").append(xp).append('\n');
        b.append("coins=").append(coins).append('\n');
        b.append("energy=").append(energy).append('\n');
        b.append("ets=").append(energyTs).append('\n');
        b.append("god=").append(god ? 1 : 0).append('\n');
        b.append("allph=").append(allPh ? 1 : 0).append('\n');
        b.append("allch=").append(allCh ? 1 : 0).append('\n');
        b.append("rp=").append(runPhase).append('\n');
        b.append("rs=").append(runStep).append('\n');
        b.append("cards=").append(mi(cards)).append('\n');
        b.append("deck=").append(joinI(deck)).append('\n');
        b.append("items=").append(mi(items)).append('\n');
        b.append("chests=").append(chests[0]).append(',').append(chests[1]).append(',').append(chests[2]).append(',').append(chests[3]).append(',').append(chests[4]).append('\n');
        b.append("stats=").append(ml(stats)).append('\n');
        b.append("daily=").append(ml(daily)).append('\n');
        b.append("dd=").append(dailyDate).append('\n');
        b.append("claimed=").append(joinI(claimed)).append('\n');
        b.append("dclaimed=").append(joinI(dclaimed)).append('\n');
        b.append("ach=").append(joinI(ach)).append('\n');
        b.append("done=").append(joinS(done)).append('\n');
        b.append("chap=").append(joinI(chapters)).append('\n');
        return b.toString();
    }

    static Player parse(String fallbackNick, String s) {
        Player p = new Player(fallbackNick);
        if (s == null) return p;
        for (String line : s.split("\n")) {
            int i = line.indexOf('=');
            if (i < 0) continue;
            String k = line.substring(0, i), v = line.substring(i + 1);
            try {
                switch (k) {
                    case "nick": if (!v.isEmpty()) p.nick = v; break;
                    case "house": p.house = Integer.parseInt(v); break;
                    case "level": p.level = Integer.parseInt(v); break;
                    case "xp": p.xp = Long.parseLong(v); break;
                    case "coins": p.coins = Long.parseLong(v); break;
                    case "energy": p.energy = Integer.parseInt(v); break;
                    case "ets": p.energyTs = Long.parseLong(v); break;
                    case "god": p.god = v.equals("1"); break;
                    case "allph": p.allPh = v.equals("1"); break;
                    case "allch": p.allCh = v.equals("1"); break;
                    case "rp": p.runPhase = v; break;
                    case "rs": p.runStep = Integer.parseInt(v); break;
                    case "cards": pmi(v, p.cards); break;
                    case "deck": pli(v, p.deck); break;
                    case "items": pmi(v, p.items); break;
                    case "chests": {
                        String[] c = v.split(",");
                        for (int j = 0; j < 5 && j < c.length; j++) p.chests[j] = Integer.parseInt(c[j].trim());
                        break;
                    }
                    case "stats": pml(v, p.stats); break;
                    case "daily": pml(v, p.daily); break;
                    case "dd": p.dailyDate = v; break;
                    case "claimed": pli(v, p.claimed); break;
                    case "dclaimed": pli(v, p.dclaimed); break;
                    case "ach": pli(v, p.ach); break;
                    case "done": pls(v, p.done); break;
                    case "chap": pli(v, p.chapters); break;
                    default: break;
                }
            } catch (NumberFormatException ex) { /* ignora linha corrompida */ }
        }
        p.chapters.add(1);
        p.level = Math.max(0, Math.min(MAX_LEVEL, p.level));
        p.house = Math.max(-1, Math.min(3, p.house));
        p.refreshEnergy();
        return p;
    }
}
