package cn.onlyloveyd.lazybear;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.gs.keyboard.SecurityKeyboard;

import cn.onlyloveyd.lazybear.databinding.ActivityLazyBinding;

public class LazyActivity extends AppCompatActivity {

    SecurityKeyboard securityKeyboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLazyBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_lazy);

        securityKeyboard = new SecurityKeyboard(binding.container);
    }
}
