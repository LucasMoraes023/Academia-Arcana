package br.com.lucasmoraes.loja;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends Activity {
    private LojaDb helper;
    private final int navy=Color.rgb(12,22,40), blue=Color.rgb(37,99,235), ink=Color.rgb(15,23,42), muted=Color.rgb(100,116,139);
    private LinearLayout root, body;
    private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    private TextView text(String s,int size,int color,boolean bold){
        TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        t.setTypeface(Typeface.DEFAULT,bold?Typeface.BOLD:Typeface.NORMAL); return t;
    }
    private void space(LinearLayout p,int h){View v=new View(this);p.addView(v,new LinearLayout.LayoutParams(1,dp(h)));}
    private TextView action(String label,Runnable run){
        TextView b=text(label,15,Color.WHITE,true); b.setGravity(Gravity.CENTER_VERTICAL); b.setPadding(dp(16),0,dp(16),0);
        b.setBackgroundColor(blue); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(52));lp.setMargins(0,dp(5),0,dp(5));b.setLayoutParams(lp);
        b.setOnClickListener(v->run.run());return b;
    }
    private LinearLayout panel(){
        LinearLayout p=new LinearLayout(this);p.setOrientation(LinearLayout.VERTICAL);p.setPadding(dp(16),dp(14),dp(16),dp(14));p.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(5),0,dp(5));p.setLayoutParams(lp);return p;
    }
    private EditText field(String hint,int type){
        EditText e=new EditText(this);e.setSingleLine(true);e.setHint(hint);e.setTextSize(15);e.setInputType(type);
        e.setPadding(dp(12),0,dp(12),0);e.setBackgroundColor(Color.rgb(241,245,249));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(50));lp.setMargins(0,dp(5),0,dp(5));e.setLayoutParams(lp);return e;
    }
    @Override public void onCreate(Bundle b){super.onCreate(b);helper=new LojaDb(this);showHome();}
    private void base(String title,String subtitle){
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Color.rgb(245,247,251));
        LinearLayout head=new LinearLayout(this);head.setOrientation(LinearLayout.VERTICAL);head.setPadding(dp(20),dp(22),dp(20),dp(20));head.setBackgroundColor(navy);
        head.addView(text("MINHA LOJA  /  GESTÃO",12,Color.rgb(147,197,253),true));space(head,5);
        head.addView(text(title,25,Color.WHITE,true));space(head,4);head.addView(text(subtitle,13,Color.rgb(203,213,225),false));root.addView(head);
        ScrollView scroll=new ScrollView(this);body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(14),dp(12),dp(14),dp(24));scroll.addView(body);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
    }
    private void heading(String s){space(body,10);body.addView(text(s,18,ink,true));space(body,4);}
    private void metric(String name,String value){
        LinearLayout c=panel();c.addView(text(name,12,muted,true));space(c,5);c.addView(text(value,24,ink,true));body.addView(c);
    }
    private void showHome(){
        base("Painel da loja","Resumo da operação e atalhos");
        heading("Visão geral");
        metric("CLIENTES CADASTRADOS",String.valueOf(helper.count("clientes")));
        metric("APARELHOS NO ESTOQUE",String.valueOf(helper.count("aparelhos")));
        metric("VENDAS / FINANCIAMENTOS",String.valueOf(helper.count("vendas")));
        heading("Operação");
        body.addView(action("＋  Cadastrar cliente",()->customerForm()));
        body.addView(action("＋  Cadastrar aparelho",()->deviceForm()));
        body.addView(action("＋  Nova venda financiada",()->saleForm()));
        heading("Consultas");
        body.addView(action("Clientes cadastrados",()->list("clientes")));
        body.addView(action("Estoque de aparelhos",()->list("aparelhos")));
        body.addView(action("Vendas e contratos",()->list("vendas")));
        body.addView(text("Os dados ficam salvos neste aparelho. A sincronização em nuvem e os pagamentos online serão integrados em etapa própria.",12,muted,false));
    }
    private void customerForm(){
        base("Novo cliente","Informe os dados para cadastro"); EditText nome=field("Nome completo",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        EditText cpf=field("CPF",InputType.TYPE_CLASS_NUMBER);EditText tel=field("Telefone",InputType.TYPE_CLASS_PHONE);EditText end=field("Endereço",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        body.addView(nome);body.addView(cpf);body.addView(tel);body.addView(end);
        body.addView(action("Salvar cliente",()->{if(nome.getText().toString().trim().isEmpty()){msg("Informe o nome do cliente");return;}
            ContentValues v=new ContentValues();v.put("nome",nome.getText().toString().trim());v.put("cpf",cpf.getText().toString());v.put("telefone",tel.getText().toString());v.put("endereco",end.getText().toString());
            helper.getWritableDatabase().insert("clientes",null,v);msg("Cliente cadastrado");showHome();}));
        body.addView(action("Voltar",()->showHome()));
    }
    private void deviceForm(){
        base("Novo aparelho","Cadastre o aparelho disponível para venda");
        EditText marca=field("Marca",InputType.TYPE_CLASS_TEXT);EditText modelo=field("Modelo",InputType.TYPE_CLASS_TEXT);
        EditText imei=field("IMEI (opcional)",InputType.TYPE_CLASS_NUMBER);EditText preco=field("Preço à vista (R$)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        body.addView(marca);body.addView(modelo);body.addView(imei);body.addView(preco);
        body.addView(action("Salvar aparelho",()->{if(modelo.getText().toString().trim().isEmpty()||preco.getText().toString().trim().isEmpty()){msg("Informe modelo e preço");return;}
            try{ContentValues v=new ContentValues();v.put("marca",marca.getText().toString());v.put("modelo",modelo.getText().toString());v.put("imei",imei.getText().toString());v.put("preco",Double.parseDouble(preco.getText().toString().replace(",",".")));
            helper.getWritableDatabase().insert("aparelhos",null,v);msg("Aparelho cadastrado");showHome();}catch(Exception e){msg("Preço inválido");}}));
        body.addView(action("Voltar",()->showHome()));
    }
    private void saleForm(){
        Cursor cc=helper.getReadableDatabase().rawQuery("SELECT id,nome FROM clientes ORDER BY nome",null);
        Cursor dc=helper.getReadableDatabase().rawQuery("SELECT id,marca,modelo,preco FROM aparelhos WHERE status='Disponível' ORDER BY modelo",null);
        if(cc.getCount()==0||dc.getCount()==0){cc.close();dc.close();new AlertDialog.Builder(this).setMessage("Cadastre pelo menos um cliente e um aparelho disponível antes de fazer uma venda.").setPositiveButton("OK",(d,w)->showHome()).show();return;}
        final java.util.ArrayList<Integer> cids=new java.util.ArrayList<>(),dids=new java.util.ArrayList<>();
        final java.util.ArrayList<String> cnames=new java.util.ArrayList<>(),dnames=new java.util.ArrayList<>();final java.util.ArrayList<Double> prices=new java.util.ArrayList<>();
        while(cc.moveToNext()){cids.add(cc.getInt(0));cnames.add(cc.getString(1));}while(dc.moveToNext()){dids.add(dc.getInt(0));dnames.add(dc.getString(1)+" "+dc.getString(2));prices.add(dc.getDouble(3));}cc.close();dc.close();
        base("Nova venda","Selecione cliente, aparelho e condições");
        Spinner cs=new Spinner(this);cs.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,cnames));
        Spinner ds=new Spinner(this);ds.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,dnames));
        EditText entrada=field("Entrada (R$)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText qtd=field("Quantidade de parcelas",InputType.TYPE_CLASS_NUMBER);
        TextView resumo=text("Selecione o aparelho para ver o valor.",14,muted,false);
        ds.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){}public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){resumo.setText("Preço do aparelho: "+money(prices.get(pos)));}});
        body.addView(text("Cliente",13,muted,true));body.addView(cs);body.addView(text("Aparelho",13,muted,true));body.addView(ds);body.addView(resumo);body.addView(entrada);body.addView(qtd);
        body.addView(action("Registrar venda e gerar parcelas",()->{try{
            int pos=ds.getSelectedItemPosition();double total=prices.get(pos);double down=entrada.getText().toString().trim().isEmpty()?0:Double.parseDouble(entrada.getText().toString().replace(",","."));int n=Integer.parseInt(qtd.getText().toString());
            if(n<1||down<0||down>=total){msg("Confira a entrada e a quantidade de parcelas");return;}
            double installment=(total-down)/n;SQLiteDatabase db=helper.getWritableDatabase();ContentValues sale=new ContentValues();sale.put("cliente_id",cids.get(cs.getSelectedItemPosition()));sale.put("aparelho_id",dids.get(pos));sale.put("entrada",down);sale.put("parcelas",n);sale.put("valor_parcela",installment);sale.put("total",total);sale.put("data",new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new java.util.Date()));
            long sid=db.insert("vendas",null,sale);ContentValues upd=new ContentValues();upd.put("status","Vendido");db.update("aparelhos",upd,"id=?",new String[]{String.valueOf(dids.get(pos))});
            db.execSQL("CREATE TABLE IF NOT EXISTS parcelas (id INTEGER PRIMARY KEY AUTOINCREMENT, venda_id INTEGER, numero INTEGER, valor REAL, vencimento TEXT, status TEXT DEFAULT 'Pendente')");
            java.util.Calendar cal=java.util.Calendar.getInstance();for(int i=1;i<=n;i++){cal.add(java.util.Calendar.MONTH,1);ContentValues par=new ContentValues();par.put("venda_id",sid);par.put("numero",i);par.put("valor",installment);par.put("vencimento",new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US).format(cal.getTime()));db.insert("parcelas",null,par);}
            msg("Venda registrada. "+n+" parcelas de "+money(installment));showHome();
        }catch(Exception e){msg("Confira os valores informados");}}));
        body.addView(action("Voltar",()->showHome()));
    }
    private String money(double v){return NumberFormat.getCurrencyInstance(new Locale("pt","BR")).format(v);}
    private void list(String table){
        String title=table.equals("clientes")?"Clientes":table.equals("aparelhos")?"Estoque":"Vendas";
        base(title,"Registros salvos neste aparelho");
        String sql=table.equals("clientes")?"SELECT nome,cpf,telefone FROM clientes ORDER BY id DESC":
          table.equals("aparelhos")?"SELECT marca,modelo,preco,status FROM aparelhos ORDER BY id DESC":
          "SELECT c.nome,a.modelo,v.total,v.parcelas,v.valor_parcela,v.status FROM vendas v LEFT JOIN clientes c ON c.id=v.cliente_id LEFT JOIN aparelhos a ON a.id=v.aparelho_id ORDER BY v.id DESC";
        Cursor cur=helper.getReadableDatabase().rawQuery(sql,null);
        if(cur.getCount()==0)body.addView(text("Nenhum registro cadastrado ainda.",14,muted,false));
        while(cur.moveToNext()){
            LinearLayout card=panel();StringBuilder s=new StringBuilder();
            for(int i=0;i<cur.getColumnCount();i++){if(i>0)s.append("\n");String val=cur.getString(i);if(table.equals("aparelhos")&&i==2)val=money(cur.getDouble(i));if(table.equals("vendas")&&i==2)val=money(cur.getDouble(i));if(table.equals("vendas")&&i==4)val=money(cur.getDouble(i));s.append(val==null?"—":val);}
            card.addView(text(s.toString(),14,ink,true));body.addView(card);
        }cur.close();body.addView(action("Voltar ao painel",()->showHome()));
    }
    private void msg(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    @Override protected void onDestroy(){if(helper!=null)helper.close();super.onDestroy();}
}