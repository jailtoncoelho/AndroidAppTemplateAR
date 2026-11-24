package com.ifpr.androidapptemplatear.ui

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ifpr.androidapptemplatear.R

class ChangeObjectFragment : Fragment() {
    private lateinit var sharedViewModel: SharedModelViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_changeobject, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedModelViewModel::class.java]
        carregarModelos()
    }

    /** Lista automaticamente os modelos GLB presentes em app/src/main/assets. */
    private fun carregarModelos() {
        val container = view?.findViewById<LinearLayout>(R.id.categoryContainer)
        container?.removeAllViews()

        val modelos = requireContext().assets.list("")
            ?.filter { it.endsWith(".glb", ignoreCase = true) }
            ?.sorted()
            ?: emptyList()

        if (modelos.isEmpty()) {
            Toast.makeText(requireContext(), "Nenhum personagem 3D encontrado.", Toast.LENGTH_LONG).show()
            return
        }

        val titulo = TextView(requireContext()).apply {
            text = "🎀 Escolha seu personagem"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.rgb(190, 65, 125))
            setPadding(0, 18, 0, 20)
        }
        container?.addView(titulo)

        modelos.forEach { adicionarModelo(container, it) }
    }

    private fun nomeAmigavel(arquivo: String): String {
        val baseName = arquivo.substringBeforeLast(".").replace(Regex(" \\(\\d+\\)$"), "").lowercase()
        return when (baseName) {
            "hello_kitty", "hellokitty" -> "Hello Kitty"
            "my_melody", "mymelody" -> "My Melody"
            "kuromi" -> "Kuromi"
            "cinamon", "cinnamon", "cinnamoroll" -> "Cinnamoroll"
            "pompompurin" -> "Pompompurin"
            else -> arquivo.substringBeforeLast(".").replace("_", " ")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    private fun adicionarModelo(container: LinearLayout?, arquivo: String) {
        val itemLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16, 10, 12, 10)
            setBackgroundColor(Color.rgb(255, 239, 246))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 8 }
        }

        val nome = TextView(requireContext()).apply {
            text = nomeAmigavel(arquivo)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.rgb(105, 55, 82))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val botao = Button(requireContext()).apply {
            text = "Ver na câmera"
            setTextColor(Color.WHITE)
            setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(213, 92, 148)))
            setOnClickListener {
                sharedViewModel.selectModel(arquivo)
                findNavController().navigate(R.id.navigation_camera)
            }
        }

        itemLayout.addView(nome)
        itemLayout.addView(botao)
        container?.addView(itemLayout)
    }
}