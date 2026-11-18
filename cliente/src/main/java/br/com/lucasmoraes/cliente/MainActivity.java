package br.com.lucasmoraes.cliente;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private DevicePolicyManager policy;
    private ComponentName admin;
    private LinearLayout content;
    private int dp(float n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        policy = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
        admin = new ComponentName(this, LojaDeviceAdminReceiver.class);
        render();
    }

    private TextView label(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextSize(size); t.setTextColor(color);
        t.setTypeface(null, bold ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        t.setPadding(0, dp(7), 0, dp(7));
        return t;
    }

    private void button(String value, Runnable action) {
        TextView b = label(value, 15, Color.WHITE, true);
        b.setGravity(Gravity.CENTER);
        b.setBackgroundColor(Color.rgb(37, 99, 235));
        b.setPadding(dp(14), dp(14), dp(14), dp(14));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(7), 0, dp(7));
        content.addView(b, lp);
        b.setOnClickListener(v -> action.run());
    }

    private boolean isAdmin() { return policy != null && policy.isAdminActive(admin); }

    private void render() {
        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(22), dp(28), dp(22), dp(28));
        content.setBackgroundColor(Color.rgb(247, 249, 252));
        scroll.addView(content);
        setContentView(scroll);

        content.addView(label("MINHA LOJA", 13, Color.rgb(37, 99, 235), true));
        content.addView(label("Proteção e situação do aparelho", 25, Color.rgb(15, 23, 42), true));
        content.addView(label("Este aplicativo só aplica políticas de gerenciamento depois que o titular autoriza a ativação. Não acessa conversas, fotos, senhas ou histórico.", 15, Color.rgb(71, 85, 105), false));

        content.addView(label("STATUS DO GERENCIAMENTO", 12, Color.GRAY, true));
        content.addView(label(isAdmin() ? "Administrador autorizado neste aparelho" : "Ainda não autorizado", 17,
                isAdmin() ? Color.rgb(22, 101, 52) : Color.rgb(180, 83, 9), true));

        if (!isAdmin()) {
            button("Autorizar gerenciamento do aparelho", () -> new AlertDialog.Builder(this)
                    .setTitle("Autorização do titular")
                    .setMessage("Ao continuar, você autoriza o Minha Loja a usar a política Android de bloqueio imediato da tela. A autorização pode ser revogada nas configurações de administradores do dispositivo.")
                    .setNegativeButton("Cancelar", (d, w) -> {})
                    .setPositiveButton("Continuar", (d, w) -> {
                        Intent i = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                        i.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin);
                        i.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                "Permite somente bloquear a tela quando solicitado de forma autorizada.");
                        startActivity(i);
                    }).show());
        } else {
            button("Bloquear tela agora (teste)", () -> {
                if (isAdmin()) {
                    policy.lockNow();
                    Toast.makeText(this, "Comando de bloqueio enviado ao Android.", Toast.LENGTH_SHORT).show();
                }
            });
            button("Revogar autorização", () -> new AlertDialog.Builder(this)
                    .setTitle("Revogar gerenciamento?")
                    .setMessage("O administrador será desativado neste aparelho.")
                    .setNegativeButton("Cancelar", (d, w) -> {})
                    .setPositiveButton("Revogar", (d, w) -> {
                        policy.removeActiveAdmin(admin);
                        render();
                    }).show());
        }

        content.addView(label("IMPORTANTE", 12, Color.GRAY, true));
        content.addView(label("Esta versão comprova apenas a autorização do administrador e o bloqueio imediato da tela. Ela ainda não recebe comandos da loja nem restringe o uso do aparelho por atraso de parcelas. Para isso é necessário provisionamento Android Enterprise como Device Owner, servidor seguro, contrato/consentimento e testes em aparelhos preparados para gerenciamento.", 14, Color.rgb(71, 85, 105), false));
    }
}
