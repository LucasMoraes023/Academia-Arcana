package br.com.lucasmoraes.loja;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LojaDb extends SQLiteOpenHelper {
    public LojaDb(Context c) { super(c, "minha_loja.db", null, 1); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE clientes (id INTEGER PRIMARY KEY AUTOINCREMENT, nome TEXT NOT NULL, cpf TEXT, telefone TEXT, endereco TEXT)");
        db.execSQL("CREATE TABLE aparelhos (id INTEGER PRIMARY KEY AUTOINCREMENT, marca TEXT, modelo TEXT NOT NULL, imei TEXT, preco REAL NOT NULL, status TEXT DEFAULT 'Disponível')");
        db.execSQL("CREATE TABLE vendas (id INTEGER PRIMARY KEY AUTOINCREMENT, cliente_id INTEGER, aparelho_id INTEGER, entrada REAL, parcelas INTEGER, valor_parcela REAL, total REAL, data TEXT, status TEXT DEFAULT 'Ativo')");
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
    public int count(String table) {
        Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM "+table,null);
        int n=c.moveToFirst()?c.getInt(0):0; c.close(); return n;
    }
}