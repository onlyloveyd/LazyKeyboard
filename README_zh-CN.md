# LazyKeyboard

[![Android CI](https://github.com/onlyloveyd/LazyKeyboard/actions/workflows/android.yml/badge.svg)](https://github.com/onlyloveyd/LazyKeyboard/actions/workflows/android.yml)
[![JitPack](https://jitpack.io/v/onlyloveyd/LazyKeyboard.svg)](https://jitpack.io/#onlyloveyd/LazyKeyboard)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![API](https://img.shields.io/badge/API-23%2B-orange.svg)](https://android-arsenal.com/api?level=23)

**LazyKeyboard** 是一个轻量级的 Android 安全键盘。在密码、支付口令、证件号等敏感输入场景下，它用应用内键盘替代系统输入法，输入内容不再经过任何第三方输入法。

[English](README.md)

## 预览

| 字母 | 符号 | 数字（乱序） | 自定义配色 |
|:---:|:---:|:---:|:---:|
| ![letter](screenshot/letter.png) | ![symbol](screenshot/symbol.png) | ![number](screenshot/number.png) | ![custom](screenshot/new_keyboard.png) |

## 为什么不用系统键盘？

用户安装的任何输入法——尤其是拥有完整网络权限的第三方输入法——都能读取普通 `EditText` 上的每一次按键。金融、政务、医疗类应用对敏感输入通常要求内容不离开应用本身。

LazyKeyboard 使用自己的底部键盘直接向输入框写入字符，系统输入法全程不参与；数字键盘每次展示时都会重新乱序，防止肩窥与随手录屏。

## 特性

- **开箱即用** — 布局中把 `EditText` 换成 `SecurityEditText` 即可。聚焦弹窗、失焦收起、返回键关闭，全部自动处理。
- **输入法硬阻断** — 输入框不创建 `InputConnection`，任何系统键盘都无法绑定，长按选择、业务代码主动 `showSoftInput` 也无法唤起。
- **防遮挡自动抬升** — 键盘若会遮住输入框，屏幕内容自动上移，键盘关闭后平滑复原。
- **三种布局** — 字母 / 符号 / 数字，带切换栏，横屏布局内置。
- **数字键盘乱序** — 默认开启。
- **可定制** — 切换栏选中/未选中颜色、切换栏与键盘背景、按键预览开关。
- **纯 Java 实现、体积小** — 不引入 Kotlin 运行时，仅依赖 AndroidX appcompat + constraintlayout。

## 环境要求

- minSdk 23，compileSdk 35+
- v1.7 将键盘渲染层替换为自绘实现，并新增输入回调（`OnSecurityKeyListener`），无破坏性 API 变更。
- v1.6 将 minSdk 从 19 提升到了 23（AndroidX 的硬性要求）。如需支持 Android 5.x，请继续使用 [v1.5](https://github.com/onlyloveyd/LazyKeyboard/tree/28cba58)。

## 引入

**第一步**，在 `settings.gradle(.kts)` 中添加 JitPack 仓库：

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}
```

**第二步**，添加依赖：

```kotlin
dependencies {
    implementation("com.github.onlyloveyd:LazyKeyboard:v1.7")
}
```

## 快速上手

在需要输入敏感信息的地方直接使用 `SecurityEditText`：

```xml
<com.gs.keyboard.SecurityEditText
    android:id="@+id/login_input_password"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="密码"
    android:inputType="textPassword"
    app:chooserBackground="@color/iron"
    app:chooserSelectedColor="#000000"
    app:chooserUnselectedColor="#999999"
    app:keyPreview="true"
    app:keyboardBackground="@drawable/keyboard_bg" />
```

不需要写任何 Java 代码。完整示例（登录页）见 `app` 模块。

## 属性

以下属性均为可选项。

| 属性 | 类型 | 说明 |
|--|--|--|
| `chooserSelectedColor` | color | 选中的键盘类型标签文字颜色 |
| `chooserUnselectedColor` | color | 未选中的标签文字颜色 |
| `chooserBackground` | color / drawable | 键盘类型切换栏背景 |
| `keyboardBackground` | color / drawable | 键盘区域背景 |
| `keyPreview` | boolean | 按键按下时是否显示预览气泡 |

## 输入回调

`OnSecurityKeyListener` 在每次按键作用到输入框**之前**触发，接入方可以借此维护自己的加密序列，或做输入审计：

```java
SecurityEditText editText = findViewById(R.id.login_input_password);
editText.setOnSecurityKeyListener((primaryCode, label) -> {
    if (primaryCode == OnSecurityKeyListener.KEYCODE_DELETE) {
        // 从自己的加密序列中删除一位
    } else if (primaryCode >= 0) {
        // 向自己的加密序列追加该字符
    }
});
```

功能键（大小写 / 完成 / 删除）以负数编码下发，常量定义在监听器接口上。

## 本地化

切换栏默认文案（字母 / 符号 / 数字）是库内的字符串资源。在应用内重写 `title_letter`、`title_symbol`、`title_number` 即可完成本地化。

## 已知局限

- 键盘由库内自绘视图（`SecurityKeyboardView`）渲染，不再依赖框架在 API 29 标记废弃的 `KeyboardView`；每个按键持有独立的背景 drawable 实例，keyBackground 使用带动画的 selector 也不会再导致渲染错乱。
- 由于拒绝一切输入法绑定，输入法组合输入（以及 Android 6–8 上长按粘贴）在 `SecurityEditText` 上不可用——这是安全上的有意取舍。
- 本库只提供 UI 层防护：能防输入法抓取与普通肩窥，不加密输入内容，也无法保护已被入侵的设备。有合规要求的场景请将本库与你们自身的提交值加密方案配合使用。

## 许可

[MIT](LICENSE) © 易冬 (onlyloveyd)
