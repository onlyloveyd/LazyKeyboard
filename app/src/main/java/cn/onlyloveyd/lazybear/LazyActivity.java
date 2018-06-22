package cn.onlyloveyd.lazybear;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.gs.keyboard.KeyboardType;
import com.gs.keyboard.SecurityConfigure;
import com.gs.keyboard.SecurityKeyboard;

import cn.onlyloveyd.lazybear.databinding.ActivityLazyBinding;

public class LazyActivity extends AppCompatActivity {

    SecurityKeyboard securityKeyboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLazyBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_lazy);
        SecurityConfigure configure = new SecurityConfigure()
                .setDefaultKeyboardType(KeyboardType.NUMBER);
        securityKeyboard = new SecurityKeyboard(binding.loginLayout, configure);
    }
}
