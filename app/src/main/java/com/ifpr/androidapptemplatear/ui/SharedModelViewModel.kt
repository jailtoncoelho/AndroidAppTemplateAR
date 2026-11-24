package com.ifpr.androidapptemplatear.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Estado compartilhado da tela AR.
 * O ViewModel preserva vidas e intervalo entre danos durante rotação da tela.
 * Uma nova MainActivity (botão Jogar novamente) cria uma partida limpa.
 */
class SharedModelViewModel : ViewModel() {
    val selectedModel = MutableLiveData<String>()

    var vidas: Int = 3
        private set
    var jogoEncerrado: Boolean = false
    private var ultimoDano: Long? = null

    fun selectModel(modelPath: String) {
        selectedModel.value = modelPath
    }

    /** Retorna true somente quando um dano válido foi aplicado. */
    fun registrarDano(agora: Long): Boolean {
        if (jogoEncerrado || vidas <= 0) return false
        val anterior = ultimoDano
        if (anterior != null && agora - anterior < 1000L) return false

        ultimoDano = agora
        vidas = (vidas - 1).coerceAtLeast(0)
        return true
    }
}
