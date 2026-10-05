package br.com.lucasmoraes.arcana;

import java.util.Locale;

final class Admin {
    static final String HELP = "/givecoins N  /givexp N  /setlevel N  /unlockall  /givecard ID  /maxlevel  /godmode  /completequest ID  /resetprogress";

    static String addCoins(Player p, long n) {
        if (!p.isAdmin()) return "Acesso negado.";
        p.coins = Math.max(0, p.coins + n);
        p.checkAch();
        return (n >= 0 ? "+" : "") + n + " moedas. Total: " + p.coins;
    }

    static String addXp(Player p, long n) {
        if (!p.isAdmin()) return "Acesso negado.";
        p.addXp(n, false);
        return "+" + n + " XP. Nível " + p.level + ", XP " + p.xp;
    }

    static String setLevel(Player p, int n) {
        if (!p.isAdmin()) return "Acesso negado.";
        n = Math.max(0, Math.min(Player.MAX_LEVEL, n));
        p.level = n;
        p.xp = 0;
        p.checkAch();
        return "Nível definido para " + n + ".";
    }

    static String unlockCards(Player p) {
        if (!p.isAdmin()) return "Acesso negado.";
        for (Card c : Data.CARDS) if (!p.has(c.id)) p.cards.put(c.id, 1);
        p.checkAch();
        return "Todas as " + Data.CARDS.size() + " cartas liberadas.";
    }

    static String unlockPhases(Player p) {
        if (!p.isAdmin()) return "Acesso negado.";
        p.allPh = true;
        return "Todas as fases liberadas.";
    }

    static String unlockChapters(Player p) {
        if (!p.isAdmin()) return "Acesso negado.";
        p.allCh = true;
        for (int i = 1; i <= 3; i++) p.chapters.add(i);
        return "Todos os capítulos liberados.";
    }

    static String unlockAll(Player p) {
        if (!p.isAdmin()) return "Acesso negado.";
        unlockCards(p);
        unlockPhases(p);
        unlockChapters(p);
        return "Cartas, fases e capítulos liberados.";
    }

    static String completeQuests(Player p) {
        if (!p.isAdmin()) return "Acesso negado.";
        int n = 0;
        for (Quest q : Data.QUESTS) if (forceQuest(p, q)) n++;
        return n + " missões completadas.";
    }

    static boolean forceQuest(Player p, Quest q) {
        if (!p.isAdmin() || p.qClaimed(q)) return false;
        if (q.kind == 2) p.dclaimed.add(q.id); else p.claimed.add(q.id);
        Rules.giveReward(p, q.rw, false);
        return true;
    }

    static String restoreEnergy(Player p) {
        if (!p.isAdmin()) return "Acesso negado.";
        p.restoreEnergy();
        return "Energia restaurada: " + p.energy + "/" + p.maxEnergy();
    }

    static String setHouse(Player p, int h) {
        if (!p.isAdmin()) return "Acesso negado.";
        p.house = ((h % 4) + 4) % 4;
        return "Casa alterada para " + Data.HNAME[p.house] + ".";
    }

    static String toggleGod(Player p) {
        if (!p.isAdmin()) return "Acesso negado.";
        p.god = !p.god;
        return "Modo Deus: " + (p.god ? "ATIVADO" : "DESATIVADO");
    }

    static String reset(Player p) {
        if (!p.isAdmin()) return "Acesso negado.";
        p.resetProgress();
        return "Progresso resetado.";
    }

    /** Executa um comando do console. Somente ADMIN consegue executar. */
    static String exec(Player p, String line) {
        if (p == null || !p.isAdmin()) return "Acesso negado: somente ADMIN pode usar comandos.";
        String s = line == null ? "" : line.trim();
        if (!s.startsWith("/")) return "Comandos começam com '/'. Digite /help.";
        String[] t = s.substring(1).split("\\s+");
        String cmd = t[0].toLowerCase(Locale.ROOT);
        try {
            switch (cmd) {
                case "help": return HELP;
                case "givecoins": return addCoins(p, Long.parseLong(arg(t, 1)));
                case "givexp": return addXp(p, Long.parseLong(arg(t, 1)));
                case "setlevel": return setLevel(p, Integer.parseInt(arg(t, 1)));
                case "unlockall": return unlockAll(p);
                case "maxlevel": return setLevel(p, Player.MAX_LEVEL);
                case "godmode": return toggleGod(p);
                case "resetprogress": return reset(p);
                case "givecard": {
                    Card c = Data.card(Integer.parseInt(arg(t, 1)));
                    if (c == null) return "Carta inexistente (use 001 a " + Data.CARDS.size() + ").";
                    p.giveCard(c.id);
                    p.checkAch();
                    return "Carta " + c.code() + " — " + c.name + " entregue.";
                }
                case "completequest": {
                    Quest q = Data.quest(Integer.parseInt(arg(t, 1)));
                    if (q == null) return "Missão inexistente.";
                    return forceQuest(p, q) ? "Missão " + q.id + " (" + q.name + ") completada." : "Missão já concluída.";
                }
                default: return "Comando desconhecido. Digite /help.";
            }
        } catch (NumberFormatException ex) {
            return "Valor inválido.";
        }
    }

    static String arg(String[] t, int i) {
        if (i >= t.length) throw new NumberFormatException();
        return t[i];
    }
}
