package com.ifpr.androidapptemplatear.ui

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ifpr.androidapptemplatear.MainActivity
import com.ifpr.androidapptemplatear.R

class GameOverActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_MOTIVO = "motivo"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val motivo = intent.getStringExtra(EXTRA_MOTIVO)
            ?: getString(R.string.game_over_default_reason)
        val padding = (24 * resources.displayMetrics.density).toInt()
        val painel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(padding, padding, padding, padding)
        }
        painel.addView(TextView(this).apply {
            text = getString(R.string.game_over_message, motivo)
            textSize = 28f
            gravity = Gravity.CENTER
        })
        painel.addView(Button(this).apply {
            setText(R.string.play_again)
            setOnClickListener {
                isEnabled = false
                // Uma Activity nova cria ViewModels novos e repõe a cena inicial.
                startActivity(Intent(this@GameOverActivity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
        })
        setContentView(painel)
        ViewCompat.setOnApplyWindowInsetsListener(painel) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(padding + bars.left, padding + bars.top,
                padding + bars.right, padding + bars.bottom)
            insets
        }
    }
}
