package com.ifpr.androidapptemplatear

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ifpr.androidapptemplatear.ui.GameOverActivity
import com.ifpr.androidapptemplatear.ui.GameViewModel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Requer Android: não substitui o teste físico do toque e rastreamento AR. */
@RunWith(AndroidJUnit4::class)
class GameFlowTest {
    @Test fun recriarActivityNaoRecuperaVidasNemLiberaDanoAntecipado() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val game = ViewModelProvider(activity)[GameViewModel::class.java]
                // Prepara o estado de dano diretamente para testar a retenção na recriação.
                game.iniciarPartida()
                assertTrue(game.perderVida(1000L))
            }
            scenario.recreate()
            scenario.onActivity { activity ->
                val game = ViewModelProvider(activity)[GameViewModel::class.java]
                assertEquals(2, game.vidas)
                assertFalse(game.perderVida(1500L))
            }
            onView(withText(org.hamcrest.Matchers.startsWith("Vidas: 2/3"))).check(matches(isDisplayed()))
        }
    }

    @Test fun gameOverPreservaMotivoAoRecriarEPermiteReiniciar() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), GameOverActivity::class.java)
            .putExtra(GameOverActivity.EXTRA_MOTIVO, "Suas vidas acabaram.")
        ActivityScenario.launch<GameOverActivity>(intent).use { scenario ->
            onView(withText("GAME OVER\nSuas vidas acabaram.")).check(matches(isDisplayed()))
            scenario.recreate()
            onView(withText("GAME OVER\nSuas vidas acabaram.")).check(matches(isDisplayed()))
            onView(withText(R.string.play_again)).perform(click())
            onView(withText(R.string.start_hunt)).check(matches(isDisplayed()))
        }
    }

    @Test fun vitoriaMostraMetaAtingida() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), GameOverActivity::class.java)
            .putExtra(GameOverActivity.EXTRA_MOTIVO, "Você recuperou os 5 escudos!")
            .putExtra(GameOverActivity.EXTRA_VITORIA, true)
            .putExtra(GameOverActivity.EXTRA_ESCUDOS, 5)
        ActivityScenario.launch<GameOverActivity>(intent).use {
            onView(withText("VITÓRIA!\nVocê recuperou os 5 escudos!"))
                .check(matches(isDisplayed()))
            onView(withText("Escudos recuperados: 5 / 5")).check(matches(isDisplayed()))
            onView(withText(R.string.play_again)).check(matches(isDisplayed()))
        }
    }
}
