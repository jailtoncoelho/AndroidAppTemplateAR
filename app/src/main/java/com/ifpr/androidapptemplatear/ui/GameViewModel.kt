package com.ifpr.androidapptemplatear.ui

import androidx.lifecycle.ViewModel

/** Estado da partida, compartilhado pelas telas e preservado durante rotação. */
class GameViewModel : ViewModel() {
    var vidas = 3
        private set
    var partidaIniciada = false
        private set
    val jogoEncerrado: Boolean get() = vidas == 0
    private var ultimoDano: Long? = null

    fun iniciarPartida() {
        if (!jogoEncerrado) partidaIniciada = true
    }

    /** Recebe o relógio monotônico do Android; não usa horário de calendário. */
    fun perderVida(agora: Long): Boolean {
        if (!partidaIniciada || jogoEncerrado) return false
        val anterior = ultimoDano
        if (anterior != null && agora - anterior < 1000L) return false
        ultimoDano = agora
        vidas = (vidas - 1).coerceAtLeast(0)
        return true
    }
}
