package cn.onlyloveyd.lazybear;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.gs.keyboard.OnSecurityKeyListener;

import cn.onlyloveyd.lazybear.databinding.ActivityLazyBinding;

public class LazyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLazyBinding binding = ActivityLazyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.loginInputPassword.setOnSecurityKeyListener(new OnSecurityKeyListener() {
            private final StringBuilder trace = new StringBuilder();

            @Override
            public void onKey(int primaryCode, CharSequence label) {
                String token;
                if (primaryCode == OnSecurityKeyListener.KEYCODE_DELETE) {
                    token = "⌫";
                } else if (primaryCode == OnSecurityKeyListener.KEYCODE_SHIFT) {
                    token = "⇧";
                } else if (primaryCode == OnSecurityKeyListener.KEYCODE_CANCEL) {
                    token = "完成";
                } else {
                    token = String.valueOf((char) primaryCode);
                }
                trace.append(token).append(' ');
                if (trace.length() > 48) {
                    trace.delete(0, trace.length() - 48);
                }
                binding.tvKeyTrace.setText(trace.toString().trim());
            }
        });
    }
}
