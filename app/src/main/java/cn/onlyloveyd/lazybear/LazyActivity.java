package cn.onlyloveyd.lazybear;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.databinding.DataBindingUtil;

import cn.onlyloveyd.lazybear.databinding.ActivityLazyBinding;

public class LazyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLazyBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_lazy);

        AppCompatButton button = findViewById(R.id.btn_login);
//        button.setOnClickListener(v-> KeyboardDialog.show(this, binding.loginInputUsername));
    }
}
