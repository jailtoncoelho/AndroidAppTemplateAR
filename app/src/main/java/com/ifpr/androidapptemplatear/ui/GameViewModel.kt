package com.ifpr.androidapptemplatear.ui

import androidx.lifecycle.ViewModel

/** Regras independentes da cena. A Activity mantém a partida durante a rotação. */
class GameViewModel : ViewModel() {
    enum class Objeto(val arquivo: String) { ESCUDO("escudo.glb"), ESPADA("espada.glb") }
    enum class Resultado { VITORIA, SEM_VIDAS, TEMPO_ESGOTADO }
    enum class Toque { IGNORADO, COLETA, DANO }

    companion object {
        const val META = 5
        const val DURACAO_MS = 30_000L
        const val TROCA_MS = 2_000L
    }

    var vidas = 3
        private set
    var escudos = 0
        private set
    var tempoRestanteMs = DURACAO_MS
        private set
    var partidaIniciada = false
        private set
    var resultado: Resultado? = null
        private set
    val jogoEncerrado get() = resultado != null
    var objetoAtual = Objeto.ESCUDO
        private set
    var rodada = 0
        private set
    var objetoConsumido = false
        private set
    private var restanteRodadaMs = TROCA_MS
    private var ultimoDano: Long? = null

    fun iniciarPartida() {
        if (!jogoEncerrado) partidaIniciada = true
    }

    /** Só recebe tempo em primeiro plano, com cena pronta e objeto posicionado. */
    fun avancarTempo(deltaMs: Long) {
        if (!partidaIniciada || jogoEncerrado || deltaMs <= 0L) return
        tempoRestanteMs = (tempoRestanteMs - deltaMs).coerceAtLeast(0L)
        if (tempoRestanteMs == 0L) {
            resultado = Resultado.TEMPO_ESGOTADO
            return
        }
        restanteRodadaMs -= deltaMs
        while (restanteRodadaMs <= 0L) {
            restanteRodadaMs += TROCA_MS
            rodada++
            objetoAtual = if (rodada % 2 == 0) Objeto.ESCUDO else Objeto.ESPADA
            objetoConsumido = false
        }
    }

    /** O identificador impede que um toque atrasado acerte o objeto seguinte. */
    fun tocarObjeto(rodadaTocada: Int, agora: Long): Toque {
        if (!partidaIniciada || jogoEncerrado || objetoConsumido || rodadaTocada != rodada) {
            return Toque.IGNORADO
        }
        val efeito = when (objetoAtual) {
            Objeto.ESCUDO -> {
                escudos++
                if (escudos == META) resultado = Resultado.VITORIA
                Toque.COLETA
            }
            Objeto.ESPADA -> {
                if (!perderVida(agora)) return Toque.IGNORADO
                Toque.DANO
            }
        }
        objetoConsumido = true
        return efeito
    }

    /** Mantém também o intervalo mínimo de dano do exemplo do professor. */
    fun perderVida(agora: Long): Boolean {
        if (!partidaIniciada || jogoEncerrado) return false
        val anterior = ultimoDano
        if (anterior != null && agora - anterior < 1000L) return false
        ultimoDano = agora
        vidas = (vidas - 1).coerceAtLeast(0)
        if (vidas == 0) resultado = Resultado.SEM_VIDAS
        return true
    }
}
