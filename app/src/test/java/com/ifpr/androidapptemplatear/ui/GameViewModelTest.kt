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

    @Test fun escudoPontuaUmaVezEObjetoSeguinteEPerigoso() {
        val game = GameViewModel()
        game.iniciarPartida()
        assertEquals(GameViewModel.Toque.COLETA, game.tocarObjeto(0, 0L))
        assertEquals(1, game.escudos)
        assertEquals(GameViewModel.Toque.IGNORADO, game.tocarObjeto(0, 100L))
        game.avancarTempo(2000L)
        assertEquals(GameViewModel.Objeto.ESPADA, game.objetoAtual)
        assertFalse(game.objetoConsumido)
        assertEquals(GameViewModel.Toque.DANO, game.tocarObjeto(1, 2000L))
        assertEquals(2, game.vidas)
        assertEquals(GameViewModel.Toque.IGNORADO, game.tocarObjeto(1, 3500L))
        assertEquals(2, game.vidas)
    }

    @Test fun toqueAtrasadoNaoAfetaONovoObjeto() {
        val game = GameViewModel()
        game.iniciarPartida()
        game.avancarTempo(2000L)
        assertEquals(GameViewModel.Toque.IGNORADO, game.tocarObjeto(0, 2500L))
        assertEquals(3, game.vidas)
        assertEquals(0, game.escudos)
    }

    @Test fun cincoEscudosVencemEImpedemQualquerAcaoPosterior() {
        val game = GameViewModel()
        game.iniciarPartida()
        repeat(5) { index ->
            if (index > 0) game.avancarTempo(4000L)
            assertEquals(GameViewModel.Toque.COLETA,
                game.tocarObjeto(game.rodada, index * 4000L))
        }
        assertEquals(5, game.escudos)
        assertEquals(GameViewModel.Resultado.VITORIA, game.resultado)
        assertEquals(14000L, game.tempoRestanteMs)
        game.avancarTempo(20000L)
        assertEquals(14000L, game.tempoRestanteMs)
        assertEquals(GameViewModel.Toque.IGNORADO, game.tocarObjeto(game.rodada, 18000L))
        assertFalse(game.perderVida(18000L))
        assertEquals(3, game.vidas)
    }

    @Test fun tempoEsgotadoEncerraMesmoSemPerderVidas() {
        val game = GameViewModel()
        game.iniciarPartida()
        game.avancarTempo(29999L)
        assertFalse(game.jogoEncerrado)
        game.avancarTempo(1L)
        assertEquals(GameViewModel.Resultado.TEMPO_ESGOTADO, game.resultado)
        assertEquals(0L, game.tempoRestanteMs)
        assertEquals(3, game.vidas)
        assertEquals(GameViewModel.Toque.IGNORADO, game.tocarObjeto(game.rodada, 30000L))
    }

    @Test fun tresEspadasDiferentesEncerramPorVidas() {
        val game = GameViewModel()
        game.iniciarPartida()
        game.avancarTempo(2000L)
        repeat(3) { index ->
            if (index > 0) game.avancarTempo(4000L)
            assertEquals(GameViewModel.Toque.DANO,
                game.tocarObjeto(game.rodada, 2000L + index * 4000L))
        }
        assertEquals(GameViewModel.Resultado.SEM_VIDAS, game.resultado)
        assertEquals(0, game.vidas)
        assertEquals(0, game.escudos)
    }

    @Test fun tempoNaoCorreAntesDoInicioEDeltaNegativoEIgnorado() {
        val game = GameViewModel()
        game.avancarTempo(10000L)
        assertEquals(30000L, game.tempoRestanteMs)
        game.iniciarPartida()
        game.avancarTempo(-500L)
        assertEquals(30000L, game.tempoRestanteMs)
        game.avancarTempo(6001L)
        assertEquals(23999L, game.tempoRestanteMs)
        assertEquals(3, game.rodada)
        assertEquals(GameViewModel.Objeto.ESPADA, game.objetoAtual)
    }

    @Test fun iniciarDeNovoNaoReiniciaRelogioColetaOuRodada() {
        val game = GameViewModel()
        game.iniciarPartida()
        game.tocarObjeto(0, 0L)
        game.avancarTempo(2000L)
        game.iniciarPartida()
        assertEquals(28000L, game.tempoRestanteMs)
        assertEquals(1, game.escudos)
        assertEquals(1, game.rodada)
    }
}
