package com.ifpr.androidapptemplatear.ui

import org.junit.Assert.*
import org.junit.Test

class GameViewModelTest {
    @Test fun partidaComecaComTresVidasEAguardaAsRegras() {
        val game = GameViewModel()
        assertEquals(3, game.vidas)
        assertFalse(game.partidaIniciada)
        assertFalse(game.jogoEncerrado)
        assertFalse(game.perderVida(0L))
        assertEquals(3, game.vidas)
    }

    @Test fun contatoRespeitaIntervaloInclusiveNoPrimeiroSegundoDoAparelho() {
        val game = GameViewModel()
        game.iniciarPartida()
        assertTrue(game.perderVida(0L))
        assertEquals(2, game.vidas)
        assertFalse(game.perderVida(999L))
        assertEquals(2, game.vidas)
        assertTrue(game.perderVida(1000L))
        assertEquals(1, game.vidas)
    }

    @Test fun terceiroDanoEncerraEBloqueiaDanosEReinicioNaMesmaPartida() {
        val game = GameViewModel()
        game.iniciarPartida()
        for (time in listOf(100L, 1100L, 2100L)) assertTrue(game.perderVida(time))
        assertEquals(0, game.vidas)
        assertTrue(game.jogoEncerrado)
        assertFalse(game.perderVida(3100L))
        game.iniciarPartida()
        assertEquals(0, game.vidas)
        assertTrue(game.jogoEncerrado)
    }

    @Test fun reexibirRegrasNaoRecuperaVidasNemRemoveIntervalo() {
        val game = GameViewModel()
        game.iniciarPartida()
        game.perderVida(10L)
        game.iniciarPartida()
        assertEquals(2, game.vidas)
        assertFalse(game.perderVida(500L))
    }

    @Test fun novaActivityRecebeUmEstadoNovo() {
        val anterior = GameViewModel()
        anterior.iniciarPartida()
        for (time in listOf(0L, 1000L, 2000L)) anterior.perderVida(time)
        val nova = GameViewModel()
        assertEquals(3, nova.vidas)
        assertFalse(nova.jogoEncerrado)
        assertFalse(nova.partidaIniciada)
        nova.iniciarPartida()
        assertTrue(nova.perderVida(2001L))
    }
}
