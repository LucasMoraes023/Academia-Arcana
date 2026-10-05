package br.com.lucasmoraes.arcana;

import java.util.Random;

interface TextCb { void on(String s); }

/** Estado global do jogo (single Activity, single View). */
final class Game {
    static MainActivity act;
    static GameView view;
    static Store store;
    static Accounts acc;
    static Player pl;
    static float W = 360, H = 640;
    static final float TOP = 28, BOT = 14;
    static final Random rnd = new Random();
    static float vol = 0.7f;
    static boolean fx = true, music = true;
    static int quality = 2;

    static void init(MainActivity a, Store s) {
        act = a;
        store = s;
        acc = new Accounts(s);
        loadSettings();
        pl = null;
        String ses = acc.session();
        if (ses != null && acc.exists(ses)) { pl = acc.load(ses); pl.refreshEnergy(); }
    }

    static void loadSettings() {
        String s = store.get("cfg");
        if (s == null) return;
        try {
            String[] p = s.split(",");
            vol = Float.parseFloat(p[0]);
            fx = p[1].equals("1");
            music = p[2].equals("1");
            quality = Integer.parseInt(p[3]);
        } catch (Exception e) { /* mantém padrões */ }
    }

    static void saveSettings() { store.put("cfg", vol + "," + (fx ? 1 : 0) + "," + (music ? 1 : 0) + "," + quality); }
    static void save() { if (pl != null && acc != null) acc.save(pl); }
    static void toast(String s) { if (view != null) view.toast(s); }
    static void go(Screen s) { view.go(s); }
    static void push(Screen s) { view.push(s); }
    static void pop() { view.pop(); }
    static void burst(float x, float y, int col, int n) { view.ps.burst(x, y, n, col, 170); }
    static void ask(String title, String hint, boolean pass, String init, TextCb cb) { act.ask(title, hint, pass, init, cb); }

    static void login(Player p) {
        pl = p;
        acc.setSession(p.nick);
        p.refreshEnergy();
        enter();
    }

    static void logout() {
        save();
        acc.setSession(null);
        pl = null;
        go(new TitleScreen());
    }

    /** Decide para onde o jogador vai ao entrar: quiz da casa, tutorial ou menu principal. */
    static void enter() {
        if (pl == null) { go(new TitleScreen()); return; }
        if (pl.house < 0) { go(new HouseQuizScreen()); return; }
        if (!pl.tutorialDone() && !pl.isAdmin()) { Runner.start(Data.phase(0, 1)); return; }
        go(new HubScreen());
    }
}
