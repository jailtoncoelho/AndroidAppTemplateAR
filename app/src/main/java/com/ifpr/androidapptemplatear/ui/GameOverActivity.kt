package com.ifpr.androidapptemplatear.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ifpr.androidapptemplatear.MainActivity

class GameOverActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MOTIVO = "motivo"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val motivo = intent.getStringExtra(EXTRA_MOTIVO) ?: "A partida terminou."
        val painel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(40, 40, 40, 40)
            setBackgroundColor(Color.rgb(255, 239, 246))
        }

        painel.addView(TextView(this).apply {
            text = "🎀 GAME OVER 🎀"
            textSize = 34f
            setTextColor(Color.rgb(190, 65, 125))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        })

        painel.addView(TextView(this).apply {
            text = motivo
            textSize = 20f
            setTextColor(Color.rgb(105, 55, 82))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 32)
        })

        painel.addView(Button(this).apply {
            text = "Jogar novamente"
            setTextColor(Color.WHITE)
            setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(Color.rgb(190, 65, 125))
            )
            setOnClickListener {
                val reinicio = Intent(this@GameOverActivity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(reinicio)
                finish()
            }
        })

        setContentView(painel)
    }
}
