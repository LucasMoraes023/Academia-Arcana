package br.com.lucasmoraes.arcana;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
    GameView view;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        final SharedPreferences sp = getSharedPreferences("arcana", MODE_PRIVATE);
        Store st = new Store() {
            public String get(String k) { return sp.getString(k, null); }
            public void put(String k, String v) { sp.edit().putString(k, v).apply(); }
            public void remove(String k) { sp.edit().remove(k).apply(); }
        };
        view = new GameView(this);
        Game.init(this, st);
        setContentView(view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        immersive();
        if (Game.music) Audio.startMusic();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Game.save();
        Audio.stopMusic();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) immersive();
    }

    void immersive() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    @Override
    public void onBackPressed() {
        if (!view.back()) super.onBackPressed();
    }

    /** Caixa de diálogo nativa para digitar texto (nickname, senha, comandos). */
    void ask(final String title, final String hint, final boolean pass, final String init, final TextCb cb) {
        runOnUiThread(new Runnable() {
            public void run() {
                final EditText et = new EditText(MainActivity.this);
                et.setHint(hint);
                et.setSingleLine(true);
                et.setInputType(pass ? (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                        : (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS));
                if (init != null) et.setText(init);
                et.setTextColor(0xFFFFFFFF);
                et.setHintTextColor(0xFF9C93C4);
                LinearLayout box = new LinearLayout(MainActivity.this);
                box.setOrientation(LinearLayout.VERTICAL);
                box.setPadding(48, 24, 48, 0);
                box.addView(et);
                new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                        .setTitle(title).setView(box)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int w) { cb.on(et.getText().toString()); }
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });
    }
}
