package br.com.lucasmoraes.arcana;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

interface Store {
    String get(String k);
    void put(String k, String v);
    void remove(String k);
}

final class MemStore implements Store {
    final HashMap<String, String> m = new HashMap<>();
    public String get(String k) { return m.get(k); }
    public void put(String k, String v) { m.put(k, v); }
    public void remove(String k) { m.remove(k); }
}

final class Accounts {
    final Store st;

    Accounts(Store st) {
        this.st = st;
        if (!exists("ADMIN")) create("ADMIN", "ADMIN123");
    }

    static String key(String nick) { return nick == null ? "" : nick.trim().toLowerCase(Locale.ROOT); }
    boolean exists(String nick) { return st.get("pw." + key(nick)) != null; }

    static String salt() {
        Random r = new java.security.SecureRandom();
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 8; i++) b.append(Integer.toHexString(r.nextInt(16)));
        return b.toString();
    }

    static String hash(String salt, String pw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest((salt + ":" + pw).getBytes("UTF-8"));
            StringBuilder b = new StringBuilder();
            for (byte x : d) b.append(String.format("%02x", x & 0xff));
            return b.toString();
        } catch (Exception e) {
            return String.valueOf((salt + pw).hashCode());
        }
    }

    private void create(String nick, String pw) {
        String s = salt();
        st.put("pw." + key(nick), s + ":" + hash(s, pw));
        st.put("data." + key(nick), new Player(nick).ser());
    }

    /** @return mensagem de erro ou null se deu certo. */
    String register(String nick, String pw, String pw2) {
        nick = nick == null ? "" : nick.trim();
        if (!nick.matches("[A-Za-z0-9_]{3,16}")) return "Nickname: 3 a 16 letras, números ou _.";
        if (key(nick).equals("admin")) return "Este nickname é reservado.";
        if (exists(nick)) return "Este nickname já existe.";
        if (pw == null || pw.length() < 4) return "A senha deve ter ao menos 4 caracteres.";
        if (pw.length() > 32) return "A senha é longa demais (máx. 32).";
        if (!pw.equals(pw2)) return "As senhas não conferem.";
        create(nick, pw);
        return null;
    }

    boolean checkPassword(String nick, String pw) {
        String v = st.get("pw." + key(nick));
        if (v == null || pw == null) return false;
        int i = v.indexOf(':');
        return i > 0 && hash(v.substring(0, i), pw).equals(v.substring(i + 1));
    }

    Player login(String nick, String pw) {
        if (!checkPassword(nick, pw)) return null;
        return load(nick);
    }

    Player load(String nick) {
        String d = st.get("data." + key(nick));
        Player p = Player.parse(nick == null ? "" : nick.trim(), d);
        if (key(nick).equals("admin")) p.nick = "ADMIN";
        return p;
    }

    void save(Player p) { if (p != null && p.nick != null) st.put("data." + key(p.nick), p.ser()); }

    String changePassword(String nick, String pw) {
        if (pw == null || pw.length() < 4) return "A senha deve ter ao menos 4 caracteres.";
        if (pw.length() > 32) return "A senha é longa demais (máx. 32).";
        String s = salt();
        st.put("pw." + key(nick), s + ":" + hash(s, pw));
        return null;
    }

    void setSession(String nick) { if (nick == null) st.remove("session"); else st.put("session", key(nick)); }
    String session() { String s = st.get("session"); return s == null || s.isEmpty() ? null : s; }
}
