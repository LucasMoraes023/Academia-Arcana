package br.com.lucasmoraes.loja;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private int dp(float v) {
        return (int)(v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView txt(String s, float size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s); t.setTextSize(size); t.setTextColor(color);
        t.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        return t;
    }

    private View button(String label) {
        TextView b = txt(label, 15, Color.WHITE, true);
        b.setGravity(Gravity.CENTER_VERTICAL);
        b.setPadding(dp(18), 0, dp(18), 0);
        b.setBackgroundColor(Color.rgb(21,101,192));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(52));
        p.setMargins(0, dp(5), 0, dp(5));
        b.setLayoutParams(p);
        return b;
    }

    private View card(String title, String value) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(16),dp(13),dp(16),dp(13));
        c.setBackgroundColor(Color.WHITE);
        c.addView(txt(title,12,Color.rgb(100,116,139),false));
        c.addView(txt(value,22,Color.rgb(15,23,42),true));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,dp(92),1);
        p.setMargins(dp(4),dp(4),dp(4),dp(4));
        c.setLayoutParams(p);
        return c;
    }

    private void row(LinearLayout p, String a, String av, String b, String bv) {
        LinearLayout r = new LinearLayout(this);
        r.addView(card(a,av)); r.addView(card(b,bv));
        p.addView(r);
    }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(245,247,250));

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.VERTICAL);
        head.setPadding(dp(20),dp(22),dp(20),dp(20));
        head.setBackgroundColor(Color.rgb(11,18,32));
        head.addView(txt("MINHA LOJA",14,Color.rgb(147,197,253),true));
        head.addView(txt("Painel da loja",25,Color.WHITE,true));
        head.addView(txt("Vendas • Clientes • Aparelhos • Financiamentos",13,Color.rgb(203,213,225),false));
        root.addView(head);

        ScrollView scroll = new ScrollView(this);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(12),dp(12),dp(12),dp(20));

        body.addView(txt("Visão geral",19,Color.rgb(15,23,42),true));
        row(body,"Clientes","0","Aparelhos","0");
        row(body,"Vendas","0","Parcelas abertas","0");

        body.addView(txt("Ações rápidas",19,Color.rgb(15,23,42),true));
        body.addView(button("＋  Nova venda"));
        body.addView(button("＋  Novo cliente"));
        body.addView(button("＋  Novo aparelho"));
        body.addView(button("▣  Contratos / parcelas"));
        body.addView(button("◉  Clientes"));
        body.addView(button("▤  Estoque"));
        body.addView(button("⚙  Configurações"));

        TextView status = txt("Versão inicial da base da loja. Cadastro e financiamento serão ligados na próxima etapa.",13,Color.rgb(71,85,105),false);
        status.setPadding(dp(8),dp(18),dp(8),dp(8));
        body.addView(status);

        scroll.addView(body);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
    }
}
