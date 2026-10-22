package com.nilemnod.mangareader.app.crash

import android.content.Intent
import android.os.Bundle
import androidx.core.view.WindowCompat
import com.nilemnod.mangareader.app.presentation.crash.CrashScreen
import com.nilemnod.mangareader.app.ui.base.activity.BaseActivity
import com.nilemnod.mangareader.app.ui.main.MainActivity
import com.nilemnod.mangareader.app.util.view.setComposeContent

class CrashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val exception = GlobalExceptionHandler.getThrowableFromIntent(intent)
        setComposeContent {
            CrashScreen(
                exception = exception,
                onRestartClick = {
                    finishAffinity()
                    startActivity(Intent(this@CrashActivity, MainActivity::class.java))
                },
            )
        }
    }
}
