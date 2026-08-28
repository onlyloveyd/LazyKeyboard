package cn.onlyloveyd.lazybear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gs.keyboard.compose.SecurityTextField
import com.gs.keyboard.compose.rememberSecurityInputState

/**
 * Compose 适配演示：SecurityTextField + rememberSecurityInputState。
 */
class ComposeDemoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SecurityKeyboardComposeDemo()
                }
            }
        }
    }
}

@Composable
private fun SecurityKeyboardComposeDemo() {
    val passwordState = rememberSecurityInputState()
    val amountState = rememberSecurityInputState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Compose 适配演示", style = MaterialTheme.typography.titleLarge)
        Text(
            "SecurityTextField 包装核心库的 SecurityEditText，输入内容通过 rememberSecurityInputState 以 Compose 状态实时暴露",
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(Modifier.height(16.dp))

        Text("支付密码", style = MaterialTheme.typography.titleSmall)
        SecurityTextField(state = passwordState, hint = "请输入支付密码")
        Text("已输入 ${passwordState.text.length} 位", style = MaterialTheme.typography.bodySmall)

        Spacer(Modifier.height(16.dp))
        Text("金额（明文输入）", style = MaterialTheme.typography.titleSmall)
        SecurityTextField(state = amountState, hint = "请输入金额", isPassword = false)
        Text("金额 = ${amountState.text}", style = MaterialTheme.typography.bodySmall)
    }
}
