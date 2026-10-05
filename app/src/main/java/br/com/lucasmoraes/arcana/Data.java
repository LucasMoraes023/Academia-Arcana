package br.com.lucasmoraes.arcana;

import java.util.ArrayList;

final class Data {
    static final int FEI = 0, CRI = 1, ART = 2, POC = 3, GUA = 4, ATQ = 5, DEF = 6, SUP = 7;
    static final int COM = 0, INC = 1, RAR = 2, EPI = 3, LEN = 4, MIT = 5;
    static final int AB_NONE = 0, AB_HEAL = 1, AB_BURN = 2, AB_STUN = 3, AB_DRAIN = 4, AB_SHIELD = 5,
            AB_ENERGY = 6, AB_DRAW = 7, AB_FURY = 8, AB_POISON = 9;
    static final String[] CAT = {"FEITIÇO", "CRIATURA", "ARTEFATO", "POÇÃO", "GUARDIÃO", "ATAQUE", "DEFESA", "SUPORTE"};
    static final int[] CATC = {0xFF8E5BE0, 0xFF3DAA5C, 0xFFD9A441, 0xFFE0508E, 0xFF6C8EAD, 0xFFE5484D, 0xFF3D9BFF, 0xFF2EC4B6};
    static final String[] RARN = {"COMUM", "INCOMUM", "RARO", "ÉPICO", "LENDÁRIO", "MÍTICO"};
    static final int[] RARC = {0xFFB0B7C3, 0xFF4CD964, 0xFF3D9BFF, 0xFFB35BFF, 0xFFFFB627, 0xFFFF4D8D};
    static final String[] HNAME = {"IGNIS", "MARIS", "SYLVA", "LUNARIS"};
    static final int[] HCOL = {0xFFE8541E, 0xFF2B8CE0, 0xFF3DAA5C, 0xFF8E5BE0};
    static final String[] HSYM = {"A Fênix", "A Maré", "O Carvalho", "A Coruja da Lua"};
    static final String[] HDESC = {
        "Os corajosos de coração em chamas. Atacam primeiro e jamais recuam diante do perigo.",
        "Mestres da calma e da estratégia. Para eles, as marés sempre voltam a seu favor.",
        "Guardiões leais da floresta antiga. Resistem a qualquer tempestade e protegem os seus.",
        "Buscadores de segredos sob a luz da lua. Aprendem mais rápido que todos os outros."};
    static final String[] HBONUS = {"+15% de dano nos seus ataques", "+1 de energia por turno nas batalhas",
        "+20% de defesa e +15 de vida", "+20% de XP em tudo que você ganha"};
    static final String[] RUNE = {"Chama", "Onda", "Folha", "Lua", "Sol", "Estrela"};
    static final String[] AB_NAME = {"—", "Cura", "Queimar", "Atordoar", "Drenar", "Escudo", "Energizar", "Comprar", "Fúria", "Veneno"};
    static final String[] CHEST_NAMES = {"COMUM", "RARO", "ÉPICO", "LENDÁRIO", "MÍTICO"};
    static final int[] CHEST_PRICE = {150, 500, 1500, 5000, 15000};
    static final String[] CHAPTERS = {"NÍVEL 0 — TUTORIAL", "CAPÍTULO 1 — A CHEGADA", "CAPÍTULO 2 — AS CATACUMBAS", "CAPÍTULO 3 — A TORRE ETERNA"};

    static String abDesc(int ab, int pw) {
        switch (ab) {
            case AB_HEAL: return "Restaura " + pw + " de vida.";
            case AB_BURN: return "Queima: " + pw + " de dano por 3 turnos.";
            case AB_STUN: return "Atordoa o inimigo por 1 turno.";
            case AB_DRAIN: return "Causa " + pw + " de dano e recupera metade em vida.";
            case AB_SHIELD: return "Ganha " + pw + " de escudo.";
            case AB_ENERGY: return "Recupera " + pw + " de energia.";
            case AB_DRAW: return "Compra " + pw + " cartas.";
            case AB_FURY: return "Seu próximo ataque causa +" + pw + " de dano.";
            case AB_POISON: return "Envenena: " + pw + " de dano por 5 turnos.";
            default: return "Sem habilidade especial.";
        }
    }

    static String title(int lv) {
        if (lv <= 0) return "Novo Aprendiz";
        if (lv < 5) return "Aprendiz";
        if (lv < 10) return "Estudante";
        if (lv < 15) return "Conjurador";
        if (lv < 20) return "Feiticeiro";
        if (lv < 30) return "Mago";
        if (lv < 40) return "Mestre Mago";
        if (lv < 50) return "Grão-Mago";
        return "Arquimago";
    }

    // ---------------- CARTAS ----------------
    static final ArrayList<Card> CARDS = new ArrayList<>();

    static void c(String n, int cat, int rar, int lvl, int atk, int def, int en, int ab, int pw, int house, String d) {
        CARDS.add(new Card(CARDS.size() + 1, n, cat, rar, lvl, atk, def, en, ab, pw, house, d));
    }

    static {
        c("Centelha Arcana", FEI, COM, 1, 5, 0, 1, AB_NONE, 0, -1, "Uma faísca de magia pura. Todo aprendiz começa por aqui.");
        c("Orbe Cintilante", FEI, COM, 1, 6, 2, 1, AB_NONE, 0, -1, "Esfera de luz que fere e protege ao mesmo tempo.");
        c("Chuva de Estrelas", FEI, INC, 3, 8, 0, 2, AB_BURN, 3, -1, "Estrelas cadentes que deixam o alvo em chamas.");
        c("Corrente Etérea", FEI, RAR, 8, 12, 4, 3, AB_STUN, 0, -1, "Prende o inimigo em correntes de luz fria.");
        c("Tempestade Rúnica", FEI, EPI, 18, 20, 4, 4, AB_BURN, 6, -1, "Runas em fúria devastam tudo à frente.");
        c("Cometa do Arquimago", FEI, LEN, 30, 30, 6, 5, AB_BURN, 9, -1, "O feitiço favorito dos grandes arquimagos.");
        c("Gnomo Faiscante", CRI, COM, 1, 4, 4, 1, AB_NONE, 0, -1, "Pequeno gnomo que adora soltar fagulhas.");
        c("Lobo de Neblina", CRI, COM, 2, 7, 3, 2, AB_NONE, 0, -1, "Caça silencioso entre as brumas da floresta.");
        c("Coruja Sábia", CRI, INC, 4, 6, 6, 2, AB_DRAW, 2, -1, "Seus olhos enxergam respostas escondidas.");
        c("Basilisco de Cristal", CRI, RAR, 10, 14, 8, 3, AB_POISON, 4, -1, "Sua carapaça reluz, e seu veneno é lento e certeiro.");
        c("Grifo Celeste", CRI, EPI, 20, 19, 12, 4, AB_FURY, 8, -1, "Rei dos céus da Academia, orgulhoso e veloz.");
        c("Dragão das Névoas", CRI, MIT, 40, 38, 22, 7, AB_DRAIN, 18, -1, "Ninguém viu seu rosto inteiro e permaneceu o mesmo.");
        c("Amuleto de Bronze", ART, COM, 1, 0, 6, 1, AB_SHIELD, 3, -1, "Um simples amuleto que afasta o azar.");
        c("Anel Cintilante", ART, COM, 2, 3, 3, 1, AB_ENERGY, 2, -1, "Armazena um pouco de energia para depois.");
        c("Cajado de Carvalho", ART, INC, 5, 8, 4, 2, AB_NONE, 0, -1, "Canaliza a magia das raízes antigas.");
        c("Espelho Revelador", ART, RAR, 9, 6, 10, 2, AB_DRAW, 2, -1, "Mostra o que há por trás de cada ilusão.");
        c("Coroa do Mago Antigo", ART, EPI, 22, 14, 14, 4, AB_ENERGY, 4, -1, "Pertenceu a quem fundou a Academia.");
        c("Cálice das Eras", ART, LEN, 32, 10, 20, 5, AB_HEAL, 40, -1, "Quem bebe dele sente séculos de vigor.");
        c("Poção de Vitalidade", POC, COM, 1, 0, 0, 1, AB_HEAL, 12, -1, "Gosto de hortelã e um pouco de coragem.");
        c("Essência Prismática", POC, COM, 3, 0, 0, 1, AB_ENERGY, 2, -1, "Cores que dançam e renovam a energia.");
        c("Elixir de Fogo", POC, INC, 5, 4, 0, 2, AB_BURN, 4, -1, "Arremesse e veja o inimigo arder.");
        c("Tônico das Fadas", POC, RAR, 10, 0, 0, 2, AB_HEAL, 28, -1, "Receita secreta das fadas do bosque.");
        c("Néctar Dourado", POC, EPI, 20, 0, 4, 3, AB_HEAL, 45, -1, "Doce como mel e forte como um dragão.");
        c("Elixir da Eternidade", POC, LEN, 35, 0, 0, 4, AB_HEAL, 80, -1, "Diz a lenda que adia o fim de qualquer jornada.");
        c("Sentinela de Pedra", GUA, COM, 1, 2, 8, 2, AB_SHIELD, 4, -1, "Imóvel, paciente e teimosa.");
        c("Escudeiro Espectral", GUA, COM, 4, 3, 7, 2, AB_NONE, 0, -1, "Um fantasma leal que carrega o escudo por você.");
        c("Golem de Musgo", GUA, INC, 6, 5, 11, 3, AB_SHIELD, 6, -1, "Macio por fora, inabalável por dentro.");
        c("Cavaleiro Arcano", GUA, RAR, 12, 12, 12, 3, AB_FURY, 6, -1, "Armadura rúnica e juramento eterno.");
        c("Titã de Obsidiana", GUA, EPI, 24, 16, 22, 5, AB_STUN, 0, -1, "Cada passo seu faz o chão tremer.");
        c("Guardião do Crepúsculo", GUA, LEN, 38, 24, 28, 6, AB_SHIELD, 20, -1, "Protege a Academia quando o sol se vai.");
        c("Golpe Veloz", ATQ, COM, 1, 6, 0, 1, AB_NONE, 0, -1, "Rápido como um piscar de olhos.");
        c("Lâmina de Luz", ATQ, COM, 3, 8, 0, 2, AB_NONE, 0, -1, "Corta sombras e dúvidas.");
        c("Raio Cortante", ATQ, INC, 5, 11, 0, 2, AB_NONE, 0, -1, "Um relâmpago em formato de lança.");
        c("Fúria do Trovão", ATQ, RAR, 11, 16, 0, 3, AB_STUN, 0, -1, "O estrondo deixa o alvo atordoado.");
        c("Meteoro Sombrio", ATQ, EPI, 25, 24, 0, 4, AB_BURN, 6, -1, "Uma rocha negra que cai do céu noturno.");
        c("Lança do Eclipse", ATQ, MIT, 45, 40, 4, 6, AB_DRAIN, 20, -1, "Forjada no instante em que o sol se apagou.");
        c("Barreira Simples", DEF, COM, 1, 0, 6, 1, AB_NONE, 0, -1, "Um escudo de luz básico, mas confiável.");
        c("Véu Protetor", DEF, COM, 3, 0, 8, 1, AB_NONE, 0, -1, "Tecido de magia que amortece golpes.");
        c("Muralha de Gelo", DEF, INC, 6, 2, 12, 2, AB_NONE, 0, -1, "Fria, lisa e muito difícil de quebrar.");
        c("Égide Luminosa", DEF, RAR, 13, 4, 18, 2, AB_HEAL, 8, -1, "Protege e cura quem estiver atrás dela.");
        c("Cúpula Arcana", DEF, EPI, 26, 5, 26, 3, AB_SHIELD, 12, -1, "Uma redoma que ninguém consegue atravessar.");
        c("Escudo do Primeiro Mago", DEF, LEN, 40, 8, 36, 4, AB_ENERGY, 2, -1, "O escudo que deu início a todas as lendas.");
        c("Sussurro Revigorante", SUP, COM, 2, 0, 3, 1, AB_HEAL, 8, -1, "Palavras gentis também curam.");
        c("Bênção Menor", SUP, INC, 4, 0, 5, 1, AB_ENERGY, 2, -1, "Uma bênção rápida para recarregar as forças.");
        c("Canção de Ninar", SUP, INC, 7, 0, 4, 2, AB_STUN, 0, -1, "Até monstros bocejam ao ouvir.");
        c("Pergaminho de Estudo", SUP, RAR, 14, 0, 6, 1, AB_DRAW, 2, -1, "Resumo perfeito feito por um mestre.");
        c("Hino da Academia", SUP, EPI, 28, 4, 8, 3, AB_ENERGY, 3, -1, "Cantado em coro, acende a magia no ar.");
        c("Coro das Estrelas", SUP, LEN, 42, 6, 10, 3, AB_HEAL, 50, -1, "Vozes do céu que curam feridas profundas.");
        // exclusivas das casas (ids 49-60)
        c("Fagulha da Fênix", FEI, INC, 5, 7, 2, 1, AB_BURN, 3, 0, "Uma centelha viva da fênix de Ignis.");
        c("Fênix Rubra", CRI, EPI, 15, 18, 10, 4, AB_DRAIN, 10, 0, "Renasce das cinzas e arde ainda mais forte.");
        c("Cometa Flamejante", ATQ, LEN, 30, 32, 0, 5, AB_BURN, 10, 0, "O golpe supremo dos campeões de Ignis.");
        c("Maré Calmante", SUP, INC, 5, 0, 4, 1, AB_ENERGY, 2, 1, "A calma de Maris renova a energia.");
        c("Serpente das Marés", CRI, EPI, 15, 17, 10, 4, AB_STUN, 0, 1, "Hipnotiza com o movimento das ondas.");
        c("Tsunami Arcano", FEI, LEN, 30, 30, 8, 5, AB_DRAIN, 15, 1, "Uma onda de magia que leva tudo embora.");
        c("Casca de Carvalho", DEF, INC, 5, 0, 14, 1, AB_HEAL, 5, 2, "Proteção das árvores mais velhas de Sylva.");
        c("Ancião Carvalho", GUA, EPI, 15, 12, 24, 4, AB_SHIELD, 12, 2, "Um guardião com mil anéis de memória.");
        c("Semente da Vida Eterna", SUP, LEN, 30, 0, 12, 3, AB_HEAL, 70, 2, "Dela nasce a cura mais poderosa da floresta.");
        c("Coruja Lunar", CRI, INC, 5, 6, 6, 2, AB_DRAW, 2, 3, "Guia de Lunaris nas noites sem estrelas.");
        c("Amuleto Estelar", ART, EPI, 15, 8, 14, 3, AB_ENERGY, 3, 3, "Guarda o brilho de uma estrela distante.");
        c("Eclipse Total", FEI, LEN, 30, 28, 6, 5, AB_STUN, 0, 3, "O dia vira noite e o inimigo congela.");
    }

    static Card card(int id) { return id >= 1 && id <= CARDS.size() ? CARDS.get(id - 1) : null; }
    static int exclusive(int house, int n) { return 49 + house * 3 + n; }
    static final int[] STARTER = {1, 2, 7, 13, 19, 25, 31, 37};

    // ---------------- ITENS ----------------
    static final Item[] ITEMS = {
        null,
        new Item(1, 0, "Poção de Vida", "Restaura 40 de vida em batalha.", 40, 0),
        new Item(2, 0, "Poção de Energia", "Restaura 3 de energia em batalha.", 0, 3),
        new Item(3, 0, "Elixir Completo", "Restaura 50 de vida e 3 de energia.", 50, 3),
        new Item(4, 1, "Fragmento Arcano", "Junte 10 para forjar um Baú Comum.", 0, 0),
        new Item(5, 1, "Fragmento Lunar", "Fragmento raro. Reserve-o para o futuro.", 0, 0),
        new Item(6, 2, "Chave da Sala Secreta", "Abre a porta escondida atrás da biblioteca.", 0, 0),
        new Item(7, 2, "Chave do Portal", "Ativa o portal antigo da Academia.", 0, 0),
        new Item(8, 3, "Amuleto de Vitalidade", "Passivo: +10 de vida máxima.", 0, 0),
        new Item(9, 3, "Anel da Fúria", "Passivo: +10% de dano.", 0, 0),
        new Item(10, 3, "Cálice Ancestral", "Passivo: +1 de energia inicial nas batalhas.", 0, 0),
        new Item(11, 3, "Bússola Arcana", "Passivo: +10% de moedas.", 0, 0),
        new Item(12, 4, "Pergaminho Selado", "Uma mensagem misteriosa dos mestres.", 0, 0),
        new Item(13, 4, "Fragmento do Portal", "Pedaço de um portal quebrado.", 0, 0),
        new Item(14, 4, "Selo do Mestre", "Prova de que você derrotou o primeiro chefe.", 0, 0)};
    static final String[] ITEM_KIND = {"POÇÕES", "FRAGMENTOS", "CHAVES", "ARTEFATOS", "ITENS DE MISSÃO"};

    // ---------------- INIMIGOS ----------------
    static final ArrayList<Enemy> ENEMIES = new ArrayList<>();

    static void e(String n, int hp, int atk, String pat, String pat2, int kind, int col, int xp, int coins, boolean boss) {
        ENEMIES.add(new Enemy(n, hp, atk, pat, pat2, kind, col, xp, coins, boss));
    }

    static {
        e("Sombra Travessa", 18, 3, "AAD", "", 0, 0xFF7A5CFF, 20, 15, false);
        e("Duende Rabugento", 44, 6, "AAHD", "", 0, 0xFF4CAF50, 30, 25, false);
        e("Livro Mordedor", 58, 8, "AHDA", "", 0, 0xFFB5651D, 35, 30, false);
        e("Aprendiz Rival", 80, 9, "AHDA", "", 2, 0xFFE5484D, 45, 40, false);
        e("Lobo Esmeralda", 92, 11, "AAHA", "", 1, 0xFF2ECC71, 50, 45, false);
        e("Armadura Assombrada", 112, 12, "DAHA", "", 3, 0xFF8795A1, 60, 55, false);
        e("Guardião de Pedra", 145, 14, "AHDH", "", 3, 0xFF9E9E9E, 70, 65, false);
        e("Espectro do Portal", 135, 15, "APAE", "", 2, 0xFF00BCD4, 80, 75, false);
        e("Mestre das Sombras", 280, 16, "AAHP", "HAEH", 4, 0xFF4A148C, 200, 250, true);
    }

    static final String[] TRAIN_NAMES = {"Fantasma de Treino", "Espantalho Arcano", "Golem de Palha", "Sombra Estudiosa", "Lobo Ilusório"};

    static Enemy training(int level, int pick) {
        int l = Math.max(1, level);
        return new Enemy(TRAIN_NAMES[Math.abs(pick) % TRAIN_NAMES.length], 30 + 8 * l, 4 + Math.round(l * 0.9f), "AAHD", "",
                pick % 3 == 0 ? 0 : pick % 3 == 1 ? 1 : 2, 0xFF6A5ACD, 12 + 3 * l, 8 + 2 * l, false);
    }

    // ---------------- QUIZ ----------------
    static final String[] QS = {
        "1|Qual criatura lendária renasce das próprias cinzas?|Fênix;Hidra;Grifo;Unicórnio|0|0",
        "3|Qual elemento está ligado à Casa Maris?|Fogo;Água;Terra;Ar|1|0",
        "0|O que um alquimista normalmente tenta criar?|Poções e elixires;Armaduras;Mapas;Instrumentos|0|0",
        "0|Qual objeto ajuda magos a canalizar a magia?|Cajado;Martelo;Pá;Rede|0|0",
        "0|Qual destes seres costuma guardar tesouros?|Dragão;Coruja;Sapo;Gato|0|0",
        "0|Um grimório é...|Um livro de feitiços;Uma poção;Uma criatura;Uma capa|0|0",
        "0|Qual cor costuma representar a energia arcana?|Roxo;Marrom;Cinza;Bege|0|0",
        "1|Verdadeiro ou falso: poções de cura restauram vida.|Verdadeiro;Falso|0|0",
        "1|Verdadeiro ou falso: na Academia Arcana existem três casas.|Verdadeiro;Falso|1|0",
        "1|Verdadeiro ou falso: cartas Míticas são as mais raras do jogo.|Verdadeiro;Falso|0|0",
        "1|Verdadeiro ou falso: defender aumenta o seu escudo no turno.|Verdadeiro;Falso|0|0",
        "1|Verdadeiro ou falso: dragões fazem parte da fantasia clássica.|Verdadeiro;Falso|0|0",
        "1|Verdadeiro ou falso: a Casa Lunaris ganha mais XP.|Verdadeiro;Falso|0|0",
        "2|Todo mago estuda. Luna é maga. Então Luna...|Estuda;Não estuda;Não é maga;Dorme sempre|0|0",
        "2|Três gnomos dividem 9 tochas igualmente. Quantas cada um leva?|2;3;4;6|1|0",
        "2|Uma poção custa 12 moedas. Quanto custam 3 poções?|24;30;36;48|2|0",
        "2|Qual item não pertence ao grupo: Fênix, Dragão, Martelo, Grifo?|Fênix;Dragão;Martelo;Grifo|2|0",
        "2|Maria chegou antes de João e João antes de Ana. Quem chegou por último?|Maria;João;Ana;Ninguém|2|0",
        "3|Como se chama a escola da sua aventura?|Academia Arcana;Escola dos Ventos;Castelo Sombrio;Torre de Vidro|0|0",
        "3|Qual casa dá +20% de XP?|Ignis;Maris;Sylva;Lunaris|3|0",
        "3|Qual casa dá +1 de energia por turno?|Ignis;Maris;Sylva;Lunaris|1|0",
        "3|Qual casa aumenta o dano em 15%?|Ignis;Maris;Sylva;Lunaris|0|0",
        "3|Qual casa aumenta defesa e vida?|Ignis;Maris;Sylva;Lunaris|2|0",
        "3|Quem alcança o nível 50 se torna...|Arquimago;Aprendiz;Goblin;Nada|0|0",
        "4|Que tipo de carta usa este símbolo?|Poção;Ataque;Artefato;Guardião|0|3",
        "4|Que tipo de carta usa este símbolo?|Ataque;Defesa;Suporte;Poção|0|5",
        "4|Que tipo de carta usa este símbolo?|Ataque;Defesa;Criatura;Artefato|1|6",
        "4|Que tipo de carta usa este símbolo?|Feitiço;Suporte;Guardião;Poção|1|7",
        "4|Que tipo de carta usa este símbolo?|Artefato;Poção;Criatura;Ataque|0|2",
        "4|A qual casa pertence esta runa?|Ignis;Maris;Sylva;Lunaris|0|100",
        "4|A qual casa pertence esta runa?|Ignis;Maris;Sylva;Lunaris|3|103",
        "4|A qual casa pertence esta runa?|Ignis;Maris;Sylva;Lunaris|2|102",
        "5|Complete: 2, 4, 8, 16, ?|24;30;32;64|2|0",
        "5|Complete: 1, 1, 2, 3, 5, ?|6;7;8;9|2|0",
        "5|Complete: 5, 10, 15, 20, ?|22;25;30;35|1|0",
        "5|Complete: 3, 6, 12, 24, ?|30;36;48;60|2|0",
        "5|Complete: 100, 90, 80, 70, ?|50;60;65;75|1|0",
        "5|Complete: A, C, E, G, ?|H;I;J;K|1|0",
        "5|Complete: 1, 4, 9, 16, ?|20;24;25;36|2|0"};
    static final ArrayList<QQ> QUIZ = new ArrayList<>();

    static {
        for (String s : QS) {
            String[] p = s.split("\\|");
            QUIZ.add(new QQ(Integer.parseInt(p[0]), p[1], p[2].split(";"), Integer.parseInt(p[3]), Integer.parseInt(p[4])));
        }
    }

    static final String[] QTYPE = {"MÚLTIPLA ESCOLHA", "VERDADEIRO OU FALSO", "LÓGICA", "CONHECIMENTO MÁGICO", "IDENTIFICAÇÃO", "SEQUÊNCIA"};

    // ---------------- QUIZ DA CASA ----------------
    static final String[] HQS = {
        "2|Uma criatura desconhecida bloqueia seu caminho na floresta. O que você faz?|Encaro de frente e ataco primeiro|3,0,1,0|Observo e espero a maré virar a meu favor|0,3,0,1|Protejo meus amigos e firmo o terreno|1,0,3,0|Estudo seus movimentos em silêncio|0,1,0,3",
        "1|Qual destas qualidades você mais valoriza?|Coragem|3,0,0,1|Serenidade|0,3,1,0|Lealdade|0,1,3,0|Sabedoria|1,0,0,3",
        "2|Na biblioteca da Academia, qual livro chama sua atenção?|Crônicas de Grandes Batalhas|3,1,0,0|Mapa das Correntes Ocultas|0,3,0,1|Guia das Árvores Ancestrais|0,0,3,1|Segredos da Lua e das Estrelas|1,0,0,3",
        "1|Qual é o seu momento favorito do dia?|Meio-dia, com o sol a pino|3,0,1,0|Entardecer à beira da água|0,3,0,1|Manhã cedo entre as árvores|0,0,3,1|Madrugada sob a lua|0,1,0,3",
        "3|Seu melhor amigo está em perigo. Você...|Corre para o combate sem pensar|3,0,1,0|Cria um plano com calma antes de agir|0,2,0,3|Fica ao lado dele e o protege até o fim|1,0,3,0|Usa o que sabe para achar uma saída secreta|0,1,0,3",
        "1|Qual animal combina mais com você?|Fênix|3,0,0,1|Golfinho|0,3,1,0|Urso|1,0,3,0|Coruja|0,1,0,3",
        "2|Como você prefere vencer um desafio?|Com força e velocidade|3,0,1,0|Com paciência e estratégia|0,3,0,2|Com resistência e perseverança|0,0,3,1|Com conhecimento e astúcia|0,1,0,3",
        "1|Que tesouro você procuraria?|Uma espada flamejante|3,0,0,1|Uma pérola do oceano|0,3,1,0|Uma semente milenar|0,1,3,0|Um grimório esquecido|1,0,0,3",
        "2|Em um grupo, você normalmente é quem...|Lidera a investida|3,0,1,0|Mantém a calma e media conflitos|0,3,1,1|Cuida de todos e dá segurança|0,0,3,0|Faz as perguntas certas|0,1,0,3",
        "1|Onde você escolheria viver?|Um vulcão adormecido|3,0,0,1|Uma ilha cercada de recifes|0,3,0,1|Uma floresta antiga|0,0,3,1|Uma torre no alto da montanha|1,0,0,3"};
    static final ArrayList<HQ> HQUIZ = new ArrayList<>();

    static {
        for (String s : HQS) {
            String[] p = s.split("\\|");
            String[] ans = new String[4];
            int[][] v = new int[4][4];
            for (int i = 0; i < 4; i++) {
                ans[i] = p[2 + i * 2];
                String[] n = p[3 + i * 2].split(",");
                for (int j = 0; j < 4; j++) v[i][j] = Integer.parseInt(n[j]);
            }
            HQUIZ.add(new HQ(Integer.parseInt(p[0]), p[1], ans, v));
        }
    }

    /** answers[i] = índice da resposta escolhida na pergunta i. */
    static int houseFrom(int[] answers) {
        int[] sc = new int[4];
        int[] tb = new int[4];
        for (int i = 0; i < HQUIZ.size() && i < answers.length; i++) {
            HQ q = HQUIZ.get(i);
            for (int h = 0; h < 4; h++) {
                sc[h] += q.w * q.v[answers[i]][h];
                if (q.w == 3) tb[h] += q.v[answers[i]][h];
            }
        }
        int best = 0;
        for (int h = 1; h < 4; h++) if (sc[h] > sc[best] || (sc[h] == sc[best] && tb[h] > tb[best])) best = h;
        return best;
    }

    // ---------------- ENIGMAS ----------------
    static final ArrayList<Riddle> RIDDLES = new ArrayList<>();

    static void r(int type, String title, String text, int[] a, int[] b, String[] s, int ans, int n) {
        RIDDLES.add(new Riddle(type, title, text, a, b, s, ans, n));
    }

    static {
        r(Riddle.SEQ, "Runas em Sequência", "Qual runa completa a sequência?", new int[]{0, 3, 0, 3, 0, -1}, new int[]{3, 4, 5, 1}, null, 3, 0); // 0
        r(Riddle.SEQ, "O Padrão das Runas", "Toque na runa que falta.", new int[]{1, 2, 3, 1, 2, -1}, new int[]{1, 2, 3, 0}, null, 3, 0); // 1
        r(Riddle.SYM, "O Pergaminho das Runas", "\"O brilho do dia, a dona da noite e o calor do inverno.\" Toque nas runas nessa ordem.", new int[]{4, 3, 0}, null, null, 0, 0); // 2
        r(Riddle.CODE, "O Cadeado Antigo", "Descubra o código de 3 dígitos.", new int[]{2, 4, 6}, null, new String[]{"O primeiro dígito é par, maior que 0 e menor que 4.", "O segundo é o dobro do primeiro.", "O terceiro é a soma dos dois primeiros."}, 0, 0); // 3
        r(Riddle.LOGIC, "As Três Caixas", "A caixa vermelha está à esquerda da azul. A verde está à direita da azul. Qual caixa está no meio?", null, null, new String[]{"Vermelha", "Azul", "Verde", "Nenhuma"}, 1, 0); // 4
        r(Riddle.COMBO, "O Cofre de Cristais", "Combine 3 cristais. Após cada tentativa você descobre quantos estão no lugar certo.", new int[]{2, 0, 3}, null, null, 0, 8); // 5
        r(Riddle.MEM, "Eco das Runas", "Memorize a sequência de luzes e repita.", null, null, null, 0, 4); // 6
        r(Riddle.SEQ, "Sol e Estrela", "Qual runa completa o padrão?", new int[]{4, 5, 4, 5, -1, 5}, new int[]{4, 5, 0, 3}, null, 4, 0); // 7
        r(Riddle.CODE, "O Cofre do Mestre", "Descubra o código de 3 dígitos.", new int[]{7, 5, 3}, null, new String[]{"Os 3 dígitos são diferentes entre si.", "A soma dos dígitos é 15.", "O primeiro dígito é 7.", "O último é ímpar e menor que o do meio."}, 0, 0); // 8
        r(Riddle.SYM, "As Quatro Forças", "\"A semente que cresce, a onda que quebra, a estrela que guia e a chama que arde.\" Toque nas runas nessa ordem.", new int[]{2, 1, 5, 0}, null, null, 0, 0); // 9
        r(Riddle.LOGIC, "Lógica do Grifo", "Todo grifo voa e nenhum animal que voa vive no fundo do mar. Então um grifo...", null, null, new String[]{"Vive no fundo do mar", "Não vive no fundo do mar", "Não voa", "É um peixe"}, 1, 0); // 10
        r(Riddle.COMBO, "Cristais Duplos", "Combine 3 cristais (cores podem repetir).", new int[]{1, 3, 1}, null, null, 0, 10); // 11
        r(Riddle.MEM, "Eco Profundo", "Memorize a sequência e repita.", null, null, null, 0, 5); // 12
        r(Riddle.LOGIC, "O Enigma do Mapa", "Tenho cidades, mas não tenho casas; montanhas, mas não árvores; água, mas não peixes. O que sou?", null, null, new String[]{"Um mapa", "Um sonho", "Uma nuvem", "Um livro"}, 0, 0); // 13
        r(Riddle.CODE, "O Grande Cadeado", "Descubra o código de 3 dígitos.", new int[]{3, 4, 5}, null, new String[]{"Cada dígito é maior que o anterior.", "Os três somam 12.", "O primeiro é 3."}, 0, 0); // 14
        r(Riddle.SYM, "As Forças da Natureza", "\"O que ilumina, o que floresce, o que molha e o que queima.\" Toque nas runas nessa ordem.", new int[]{4, 2, 1, 0}, null, null, 0, 0); // 15
        r(Riddle.LOGIC, "Poções em Dupla", "Dois magos têm 5 poções juntos. Um tem 1 a mais que o outro. Quantas tem o mago com mais?", null, null, new String[]{"2", "3", "4", "5"}, 1, 0); // 16
        r(Riddle.MEM, "Eco Infinito", "Memorize a sequência e repita.", null, null, null, 0, 6); // 17
    }

    // ---------------- MISSÕES ----------------
    static final ArrayList<Quest> QUESTS = new ArrayList<>();

    static void q(int id, int kind, String n, String d, String key, int t, Reward rw) {
        QUESTS.add(new Quest(id, kind, n, d, key, t, rw));
    }

    static Reward rw(int xp, int coins, int card, int item, int itemN, int chest) {
        return new Reward(xp, coins, card, item, itemN, chest);
    }

    static {
        q(1, 0, "Primeiros Passos", "Vença sua primeira batalha.", "battles_won", 1, rw(40, 50, 0, 0, 0, -1));
        q(2, 0, "Mente Afiada", "Acerte 10 perguntas de quiz.", "quiz_correct", 10, rw(60, 80, 0, 0, 0, -1));
        q(3, 0, "Decifrador", "Resolva 3 enigmas.", "riddles", 3, rw(80, 100, 0, 4, 3, -1));
        q(4, 0, "Caminho do Aprendiz", "Complete 3 fases.", "phases", 3, rw(100, 120, 0, 0, 0, 0));
        q(5, 0, "Evolução", "Alcance o nível 5.", "level", 5, rw(120, 150, 44, 0, 0, -1));
        q(6, 0, "Colecionador Iniciante", "Possua 15 cartas.", "cards", 15, rw(150, 200, 0, 0, 0, 1));
        q(7, 0, "Coração do Capítulo", "Complete 8 fases.", "phases", 8, rw(250, 300, 0, 9, 1, -1));
        q(8, 0, "O Primeiro Chefe", "Derrote o Mestre das Sombras.", "boss1", 1, rw(500, 600, 0, 0, 0, 2));
        q(51, 1, "Duelista", "Vença 10 batalhas.", "battles_won", 10, rw(100, 150, 0, 1, 2, -1));
        q(52, 1, "Estudioso", "Acerte 30 perguntas.", "quiz_correct", 30, rw(120, 150, 0, 0, 0, -1));
        q(53, 1, "Mestre dos Enigmas", "Resolva 10 enigmas.", "riddles", 10, rw(150, 200, 0, 4, 5, -1));
        q(54, 1, "Abridor de Baús", "Abra 5 baús.", "chests", 5, rw(100, 150, 0, 0, 0, 0));
        q(55, 1, "Magnata", "Acumule 1000 moedas.", "coins", 1000, rw(100, 100, 0, 0, 0, -1));
        q(56, 1, "Colecionador", "Possua 30 cartas.", "cards", 30, rw(300, 400, 0, 0, 0, 2));
        q(57, 1, "Veterano", "Alcance o nível 15.", "level", 15, rw(300, 400, 0, 10, 1, -1));
        q(58, 1, "Campeão", "Vença 50 batalhas.", "battles_won", 50, rw(500, 700, 0, 0, 0, 3));
        q(101, 2, "Treino Diário", "Vença 2 batalhas hoje.", "battles_won", 2, rw(40, 60, 0, 0, 0, -1));
        q(102, 2, "Quiz do Dia", "Acerte 5 perguntas hoje.", "quiz_correct", 5, rw(30, 40, 0, 0, 0, -1));
        q(103, 2, "Enigma do Dia", "Resolva 1 enigma hoje.", "riddles", 1, rw(30, 40, 0, 0, 0, -1));
        q(104, 2, "Tesouro do Dia", "Abra 1 baú hoje.", "chests", 1, rw(40, 50, 0, 1, 1, -1));
    }

    static Quest quest(int id) {
        for (Quest x : QUESTS) if (x.id == id) return x;
        return null;
    }

    // ---------------- CONQUISTAS ----------------
    static final ArrayList<Ach> ACHS = new ArrayList<>();

    static void a(int id, String n, String d, String k, int t, int coins) { ACHS.add(new Ach(id, n, d, k, t, coins)); }

    static {
        a(1, "PRIMEIRA BATALHA", "Vença sua primeira batalha.", "battles_won", 1, 50);
        a(2, "PRIMEIRO ENIGMA", "Resolva seu primeiro enigma.", "riddles", 1, 50);
        a(3, "PRIMEIRA CARTA RARA", "Obtenha uma carta Rara ou melhor.", "rare_card", 1, 100);
        a(4, "PRIMEIRA CARTA LENDÁRIA", "Obtenha uma carta Lendária.", "legend_card", 1, 300);
        a(5, "COLECIONADOR", "Possua 25 cartas diferentes.", "cards", 25, 200);
        a(6, "MESTRE DOS QUIZZES", "Acerte 100 perguntas.", "quiz_correct", 100, 300);
        a(7, "CAMPEÃO", "Vença 25 batalhas.", "battles_won", 25, 300);
        a(8, "MESTRE DA MAGIA", "Alcance o nível 50.", "level", 50, 5000);
        a(9, "PRIMEIRA CARTA ÉPICA", "Obtenha uma carta Épica ou melhor.", "epic_card", 1, 150);
        a(10, "PRIMEIRA CARTA MÍTICA", "Obtenha uma carta Mítica.", "myth_card", 1, 600);
        a(11, "ABRIDOR DE BAÚS", "Abra 10 baús.", "chests", 10, 200);
        a(12, "CAÇADOR DE SOMBRAS", "Derrote o Mestre das Sombras.", "boss1", 1, 500);
        a(13, "RICO", "Tenha 5000 moedas ao mesmo tempo.", "coins", 5000, 300);
        a(14, "INCANSÁVEL", "Complete 10 fases.", "phases", 10, 400);
    }

    // ---------------- FASES ----------------
    static final ArrayList<Phase> PHASES = new ArrayList<>();

    static Step story(String s) { return new Step(Step.STORY, 0, s, null); }
    static Step quiz(int... q) { return new Step(Step.QUIZ, 0, null, q); }
    static Step riddle(int id) { return new Step(Step.RIDDLE, id, null, null); }
    static Step battle(int e) { return new Step(Step.BATTLE, e, null, null); }

    static void ph(int ch, int idx, String title, String desc, int rec, Reward rw, Step... st) {
        PHASES.add(new Phase(ch, idx, title, desc, st, rw, rec));
    }

    static {
        ph(0, 1, "NÍVEL 0 — NOVO APRENDIZ", "Conheça o mundo, receba sua primeira carta e vença seu primeiro duelo.", 0, rw(60, 100, 0, 0, 0, -1),
            story("Bem-vindo à Academia Arcana, a maior escola de magia do mundo conhecido. Dizem que seus corredores mudam de lugar à noite e que os livros sussurram segredos a quem sabe ouvir.|Aqui, aprendizes dominam feitiços, convivem com criaturas, forjam artefatos e duelam com cartas encantadas. Cada carta guarda um fragmento do poder do Véu Arcano.|Você chegou como Novo Aprendiz — Nível 0. Sua casa já escolheu você. Chegou a hora de receber a sua primeira carta!"),
            new Step(Step.CARD, 0, null, null),
            quiz(18, 7, 0),
            riddle(0),
            battle(0));
        ph(1, 1, "O NOVO APRENDIZ", "Seu primeiro dia de aulas começa com perguntas e um visitante travesso.", 1, rw(60, 60, 0, 12, 1, -1),
            story("A sineta toca e o grande salão se enche de aprendizes. Um mestre lhe entrega um pergaminho selado e pede que você prove seu valor.|Mas, antes disso, uma sombra travessa decidiu fazer do seu primeiro dia uma aventura."),
            quiz(18, 8, 19), battle(1));
        ph(1, 2, "O PRIMEIRO ENIGMA", "As runas do corredor guardam um enigma antigo.", 2, rw(80, 70, 0, 6, 1, -1),
            story("No corredor das runas, as paredes brilham quando você se aproxima. Uma voz suave diz: 'Apenas quem entende o padrão pode passar.'"),
            riddle(1), riddle(2), quiz(7, 9, 10));
        ph(1, 3, "A SALA SECRETA", "Atrás da biblioteca existe uma porta que só abre para mentes atentas.", 3, rw(100, 90, 0, 0, 0, 0),
            story("A chave que você recebeu gira sozinha na fechadura. Atrás da estante, uma sala empoeirada guarda cadeados e livros que mordem."),
            riddle(3), riddle(4), battle(2));
        ph(1, 4, "O DUELO", "Um rival da Academia desafia você diante de todos.", 4, rw(120, 110, 21, 0, 0, -1),
            story("No pátio central, um aprendiz de capa vermelha cruza os braços: 'Dizem que você é promissor. Prove isso num duelo de cartas!'"),
            battle(3));
        ph(1, 5, "A FLORESTA", "A floresta proibida esconde criaturas, símbolos e ecos.", 5, rw(140, 130, 0, 4, 5, -1),
            story("Além dos muros, a floresta respira. Luzes verdes dançam entre as árvores e algo observa você de dentro da neblina."),
            quiz(26, 27, 28), battle(4), riddle(6));
        ph(1, 6, "O ARTEFATO", "Um artefato adormecido pede um código e um combate.", 6, rw(160, 150, 0, 8, 1, -1),
            story("Em uma clareira, um pedestal de pedra guarda um artefato brilhante. Para pegá-lo, é preciso vencer o guardião assombrado que o protege."),
            riddle(5), battle(5));
        ph(1, 7, "O GUARDIÃO", "O guardião de pedra bloqueia o caminho para o portal.", 8, rw(190, 180, 0, 7, 1, 1),
            story("Uma estátua colossal desperta de um longo sono. Seus olhos de rubi se fixam em você: 'Responda... e lute.'"),
            quiz(14, 16, 17), battle(6));
        ph(1, 8, "O PORTAL", "Um portal antigo se abre. O que está do outro lado?", 9, rw(220, 210, 4, 13, 1, -1),
            story("Com a chave em mãos, o portal acende em tons de azul. Espectros atravessam o brilho, curiosos e famintos."),
            riddle(7), riddle(12), battle(7));
        ph(1, 9, "O GRANDE ENIGMA", "Três enigmas e cinco perguntas guardam a porta do chefe.", 10, rw(260, 250, 0, 0, 0, 1),
            story("Diante da última porta, três mecanismos giram lentamente. Só quem domina lógica, símbolos e números poderá avançar."),
            riddle(10), riddle(14), riddle(9), quiz(32, 33, 35, 13, 24));
        ph(1, 10, "O PRIMEIRO CHEFE", "O Mestre das Sombras espera no coração da Academia.", 12, rw(500, 500, 0, 14, 1, 2),
            story("A porta se abre. O ar fica frio. Do centro do salão, uma figura imensa levanta a cabeça: 'Você chegou longe, aprendiz. Mostre se tem coragem de ir além.'"),
            battle(8));
        // capítulos 2 e 3 gerados
        String[][] titles = {
            {"O Corredor dos Ecos", "A Cripta Esquecida", "O Lago Sussurrante", "A Biblioteca Proibida", "O Salão dos Espelhos", "A Rainha dos Ecos"},
            {"A Escadaria Infinita", "O Observatório", "A Sala das Estrelas", "O Jardim Suspenso", "O Relógio Arcano", "O Arquimago Esquecido"}};
        float[] mult = {1.3f, 1.6f};
        int[] riddlePool = {6, 11, 15, 16, 9, 13};
        int[] quizPool = {20, 21, 22, 32, 34, 36, 15, 29, 30, 31, 33, 37};
        for (int ci = 0; ci < 2; ci++) {
            for (int i = 0; i < 6; i++) {
                int en;
                if (i == 5) {
                    ENEMIES.add(ENEMIES.get(8).scaled(1.05f + ci * 0.1f, i == 5 ? (ci == 0 ? "Rainha dos Ecos" : "Arquimago Esquecido") : null));
                } else {
                    ENEMIES.add(ENEMIES.get(1 + (i + ci * 2) % 7).scaled(mult[ci], null));
                }
                en = ENEMIES.size() - 1;
                int q1 = quizPool[(i * 2 + ci * 5) % quizPool.length], q2 = quizPool[(i * 3 + 1 + ci) % quizPool.length], q3 = quizPool[(i + 4 + ci * 3) % quizPool.length];
                int base = 300 + ci * 400 + i * 80;
                ph(2 + ci, i + 1, titles[ci][i].toUpperCase(), "Desafios mais difíceis aguardam você.", 14 + ci * 10 + i * 2,
                    rw(base, base / 2, 0, i == 2 ? 4 : 0, i == 2 ? 6 : 0, i == 5 ? 2 + ci : (i % 2 == 0 ? ci : -1)),
                    story(titles[ci][i] + " guarda segredos que poucos aprendizes ousaram enfrentar. Prepare seu deck, aprendiz."),
                    quiz(q1, q2, q3), riddle(riddlePool[(i + ci * 2) % riddlePool.length]), battle(en));
            }
        }
    }

    static Phase phase(int ch, int idx) {
        for (Phase p : PHASES) if (p.ch == ch && p.idx == idx) return p;
        return null;
    }

    static int phaseCount(int ch) {
        int n = 0;
        for (Phase p : PHASES) if (p.ch == ch) n++;
        return n;
    }
}
