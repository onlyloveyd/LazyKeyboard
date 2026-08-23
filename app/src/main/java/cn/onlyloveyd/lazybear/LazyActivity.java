package cn.onlyloveyd.lazybear;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import cn.onlyloveyd.lazybear.databinding.ActivityLazyBinding;

public class LazyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLazyBinding binding = ActivityLazyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}
