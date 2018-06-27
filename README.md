# LazyKeyboard
A SecurityKeyBoard for Android. Supporting three kinds of keyboard:
Letter, Number, Symbol.
[中文版](http://www.onlyloveyd.cn/article/32)

## Installation
### Gradle
**Step 1.** Add it in your root build.gradle at the end of repositories:
```groovy
allprojects {
    repositories {
	...
	maven { url 'https://jitpack.io' }
    }
}
``` 
**Step 2.** Add the dependency
```groovy
dependencies {
    implementation 'com.github.onlyloveyd:LazyKeyboard:v1.2'
}
```

## Basic Usage
**Step 1.** Add **SecurityEditText** into your layout
```xml
   <LinearLayout
        android:id="@+id/container"
        ...">

        <com.gs.keyboard.SecurityEditText
            android:id="@+id/et_security_keyboard"
            android:layout_width="match_parent"
            ... />

        <EditText
            android:id="@+id/et_security_keyboard_two"
            ... />
    </LinearLayout>
```
**Step 2.** init **SecurityKeyboard** with the layout contains **SecurityEditText**
```java
   SecurityConfigure configure = new SecurityConfigure()
           .setDefaultKeyboardType(KeyboardType.NUMBER)
           .setLetterEnabled(false);
   securityKeyboard = new SecurityKeyboard(binding.loginLayout, configure);
```

## Basic Result
![letter](screenshot/letter.png)
![symbol](screenshot/symbol.png)
![number](screenshot/number.png)
