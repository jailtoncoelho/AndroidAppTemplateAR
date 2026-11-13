package com.ifpr.androidapptemplatear.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.ifpr.androidapptemplatear.R
import io.github.sceneview.SceneView
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class CameraFragment : Fragment() {

    private enum class Mode {
        CHECKING,
        AR,
        CAMERA,
        STATIC
    }

    companion object {
        private const val AVAILABILITY_TIMEOUT_MS = 4000L
        private const val AR_TIMEOUT_MS = 15000L
        private const val CAMERA_TIMEOUT_MS = 8000L
    }

    private var mode = Mode.CHECKING

    private var container: FrameLayout? = null
    private var placeButton: ExtendedFloatingActionButton? = null
    private var statusText: TextView? = null
    private var overlay: View? = null

    private var sceneView: SceneView? = null
    private var arView: ARSceneView? = null

    private var modelNode: ModelNode? = null
    private var anchorNode: AnchorNode? = null
    private var placementHit: HitResult? = null

    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraPreview: Preview? = null

    private var loadJob: Job? = null
    private var availabilityJob: Job? = null
    private var arWatchdogJob: Job? = null
    private var cameraWatchdogJob: Job? = null

    private var generation = 0
    private var modelGeneration = 0
    private var pendingAr = false

    private var arTracking = false
    private var cameraStreaming = false

    private var currentModelFile = "escudo.glb"

    private lateinit var gameViewModel: GameViewModel
    private var livesText: TextView? = null
    private var targetText: TextView? = null
    private var startButton: Button? = null
    private var gameJob: Job? = null
    private var lastGameTickMs: Long? = null
    private val modelCache = mutableMapOf<String, ModelNode>()
    private var rulesDialog: androidx.appcompat.app.AlertDialog? = null
    private var telaEncerramentoAberta = false

    private lateinit var sharedViewModel: SharedModelViewModel

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (container != null && !gameViewModel.jogoEncerrado) {
                when {
                    !granted -> startPreview(useCamera = false)
                    pendingAr -> startAr()
                    else -> startPreview(useCamera = true)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_camera,
            parent,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        container = view.findViewById(R.id.sceneContainer)
        placeButton = view.findViewById(R.id.place)

        gameViewModel = ViewModelProvider(requireActivity())[GameViewModel::class.java]
        if (gameViewModel.jogoEncerrado) {
            encerrarJogo(motivoDoResultado())
            return
        }
        createControls()

        sharedViewModel =
            ViewModelProvider(requireActivity())[
                SharedModelViewModel::class.java
            ]

        val selectedModel = sharedViewModel.selectedModel.value

        if (gameViewModel.partidaIniciada) {
            currentModelFile = gameViewModel.objetoAtual.arquivo
        } else if (!selectedModel.isNullOrBlank()) {
            currentModelFile = selectedModel
        } else {
            sharedViewModel.selectModel(currentModelFile)
        }

        sharedViewModel.selectedModel.observe(viewLifecycleOwner) { path ->
            if (!gameViewModel.partidaIniciada && !path.isNullOrBlank() && path != currentModelFile) {
                currentModelFile = path
                loadModel()
            }
        }

        placeButton?.setOnClickListener {
            if (gameViewModel.jogoEncerrado) return@setOnClickListener
            when (mode) {
                Mode.AR -> placeModel()

                Mode.CAMERA,
                Mode.STATIC -> {
                    startPreview(useCamera = mode == Mode.CAMERA)
                }

                Mode.CHECKING -> Unit
            }
        }

        checkAvailability()
        startGameLoop()
    }

    private fun mostrarRegras() {
        if (gameViewModel.partidaIniciada || rulesDialog?.isShowing == true) return
        rulesDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.game_rules_title)
            .setMessage(R.string.game_rules)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.start_game) { _, _ ->
                gameViewModel.iniciarPartida()
                currentModelFile = gameViewModel.objetoAtual.arquivo
                loadModel(preserveAnchor = true)
                updateGameControls()
            }
            .show()
    }

    private fun motivoDoResultado(): String = getString(when (gameViewModel.resultado) {
        GameViewModel.Resultado.VITORIA -> R.string.victory_reason
        GameViewModel.Resultado.TEMPO_ESGOTADO -> R.string.timeout_reason
        else -> R.string.game_over_reason
    })

    private fun sceneReady(): Boolean = modelNode != null && when (mode) {
        Mode.AR -> anchorNode != null && arTracking
        Mode.CAMERA -> cameraStreaming
        Mode.STATIC -> true
        Mode.CHECKING -> false
    }

    private fun updateGameControls() {
        livesText?.text = getString(R.string.game_hud, gameViewModel.vidas,
            gameViewModel.escudos, GameViewModel.META,
            (gameViewModel.tempoRestanteMs + 999L) / 1000L)
        startButton?.visibility = if (gameViewModel.partidaIniciada) View.GONE else View.VISIBLE
        targetText?.setText(when {
            !gameViewModel.partidaIniciada -> R.string.game_rules_summary
            !sceneReady() -> R.string.game_waiting
            gameViewModel.objetoConsumido -> R.string.next_object
            gameViewModel.objetoAtual == GameViewModel.Objeto.ESCUDO -> R.string.collect_shield
            else -> R.string.avoid_sword
        })
    }

    private fun startGameLoop() {
        gameJob = viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                lastGameTickMs = null
                try {
                    while (isActive && !gameViewModel.jogoEncerrado) {
                        advanceClock(SystemClock.elapsedRealtime())
                        updateGameControls()
                        delay(100L)
                    }
                } finally {
                    lastGameTickMs = null
                }
            }
        }
    }

    private fun advanceClock(now: Long) {
        val previous = lastGameTickMs
        lastGameTickMs = now
        if (!gameViewModel.partidaIniciada || gameViewModel.jogoEncerrado || !sceneReady()) {
            lastGameTickMs = null
            return
        }
        val roundBefore = gameViewModel.rodada
        if (previous != null) gameViewModel.avancarTempo(now - previous)
        if (gameViewModel.jogoEncerrado) {
            encerrarJogo(motivoDoResultado())
        } else if (roundBefore != gameViewModel.rodada) {
            currentModelFile = gameViewModel.objetoAtual.arquivo
            loadModel(preserveAnchor = true)
        }
    }

    private fun tocarObjeto(round: Int) {
        if (!viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) ||
            !sceneReady()) return
        val now = SystemClock.elapsedRealtime()
        // Atualiza o prazo antes do toque: não permite pontuar depois dos 30 segundos.
        advanceClock(now)
        val effect = gameViewModel.tocarObjeto(round, now)
        if (effect != GameViewModel.Toque.IGNORADO) {
            modelNode?.isVisible = false
            modelNode?.isTouchable = false
            updateGameControls()
        }
        if (gameViewModel.jogoEncerrado) encerrarJogo(motivoDoResultado())
    }

    private fun encerrarJogo(motivo: String) {
        if (telaEncerramentoAberta || !gameViewModel.jogoEncerrado) return
        telaEncerramentoAberta = true
        placeButton?.isEnabled = false
        modelNode?.isEditable = false
        modelNode?.isTouchable = false
        availabilityJob?.cancel()
        gameJob?.cancel()
        // Retira câmera, gestos, carregamentos e watchdogs antes de abrir a tela final.
        clearScene()
        startActivity(Intent(requireContext(), GameOverActivity::class.java).apply {
            putExtra(GameOverActivity.EXTRA_MOTIVO, motivo)
            putExtra(GameOverActivity.EXTRA_VITORIA,
                gameViewModel.resultado == GameViewModel.Resultado.VITORIA)
            putExtra(GameOverActivity.EXTRA_ESCUDOS, gameViewModel.escudos)
        })
        requireActivity().finish()
    }

    private fun createControls() {
        val parent = container?.parent as? ViewGroup ?: return

        val layer = FrameLayout(requireContext())

        val panel = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setBackgroundColor(Color.argb(190, 25, 30, 35))
        }

        statusText = TextView(requireContext()).apply {
            text = "Preparando visualização…"
            textSize = 13f
            setTextColor(Color.WHITE)
        }

        livesText = TextView(requireContext()).apply {
            text = getString(R.string.game_hud, gameViewModel.vidas, gameViewModel.escudos,
                GameViewModel.META, (gameViewModel.tempoRestanteMs + 999L) / 1000L)
            textSize = 16f
            setTextColor(Color.WHITE)
        }
        panel.addView(livesText)
        targetText = TextView(requireContext()).apply {
            textSize = 14f
            setTextColor(Color.rgb(166, 243, 208))
        }
        panel.addView(targetText)
        startButton = Button(requireContext()).apply {
            setText(R.string.start_hunt)
            setOnClickListener { mostrarRegras() }
        }
        panel.addView(startButton)
        panel.addView(statusText)

        layer.addView(
            panel,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
            ).apply {
                leftMargin = dp(12)
                rightMargin = dp(12)
                bottomMargin = dp(85)
            }
        )

        parent.addView(
            layer,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        overlay = layer
        updateGameControls()

        parent.findViewById<View>(R.id.titleImage)?.bringToFront()
        placeButton?.bringToFront()
    }

    /**
     * Identificação por características comuns de emuladores.
     * Por escolha deste aplicativo, eles iniciam sem ARCore.
     */
    private fun isEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("google_sdk", ignoreCase = true) ||
                Build.MODEL.contains("Emulator", ignoreCase = true) ||
                Build.MODEL.contains(
                    "Android SDK built for",
                    ignoreCase = true
                ) ||
                Build.PRODUCT.contains("sdk", ignoreCase = true) ||
                Build.HARDWARE.contains("goldfish", ignoreCase = true) ||
                Build.HARDWARE.contains("ranchu", ignoreCase = true)
    }

    private fun checkAvailability() {
        if (gameViewModel.jogoEncerrado) return
        availabilityJob?.cancel()

        // Não tenta iniciar ARCore no emulador.
        if (isEmulator()) {
            requestMode(useAr = false)
            return
        }

        val expected = generation
        var resolved = false

        availabilityJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(AVAILABILITY_TIMEOUT_MS)

            if (
                !resolved &&
                container != null &&
                expected == generation
            ) {
                resolved = true
                requestMode(useAr = false)
            }
        }

        try {
            ArCoreApk.getInstance()
                .checkAvailabilityAsync(requireContext()) { availability ->
                    activity?.runOnUiThread {
                        if (
                            !resolved &&
                            container != null &&
                            expected == generation
                        ) {
                            resolved = true
                            availabilityJob?.cancel()
                            availabilityJob = null

                            val installed =
                                availability ==
                                        ArCoreApk.Availability.SUPPORTED_INSTALLED

                            requestMode(useAr = installed)
                        }
                    }
                }
        } catch (_: Exception) {
            if (
                !resolved &&
                container != null &&
                expected == generation
            ) {
                resolved = true
                availabilityJob?.cancel()
                availabilityJob = null

                requestMode(useAr = false)
            }
        }
    }

    private fun requestMode(useAr: Boolean) {
        if (gameViewModel.jogoEncerrado) return
        pendingAr = useAr

        val permitted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        when {
            !permitted -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }

            useAr -> startAr()

            else -> startPreview(useCamera = true)
        }
    }

    private fun startAr() {
        if (gameViewModel.jogoEncerrado) return
        val host = container ?: return

        clearScene()
        mode = Mode.AR

        val expected = generation

        statusText?.text = "Aponte para uma mesa ou para o chão."

        placeButton?.apply {
            text = "Posicionar"
            isEnabled = false
        }

        try {
            val ar = ARSceneView(requireContext())

            arView = ar
            sceneView = ar

            ar.configureSession { _, config ->
                config.planeFindingMode =
                    Config.PlaneFindingMode.HORIZONTAL

                config.lightEstimationMode =
                    Config.LightEstimationMode.DISABLED
            }

            ar.onSessionFailed = {
                host.post {
                    if (
                        container != null &&
                        expected == generation &&
                        mode == Mode.AR
                    ) {
                        startPreview(useCamera = true)
                    }
                }
            }

            ar.onSessionUpdated = { _, frame ->
                if (
                    expected == generation &&
                    !gameViewModel.jogoEncerrado &&
                    mode == Mode.AR
                ) {
                    arTracking =
                        frame.camera.trackingState ==
                                TrackingState.TRACKING

                    if (anchorNode == null) {
                        placementHit =
                            if (
                                arTracking &&
                                ar.width > 0 &&
                                ar.height > 0
                            ) {
                                frame.hitTest(
                                    ar.width / 2f,
                                    ar.height / 2f
                                ).firstOrNull { hit ->
                                    val plane = hit.trackable as? Plane

                                    plane != null &&
                                            plane.type ==
                                            Plane.Type.HORIZONTAL_UPWARD_FACING &&
                                            plane.isPoseInPolygon(hit.hitPose)
                                }
                            } else {
                                null
                            }

                        val ready =
                            placementHit != null &&
                                    modelNode != null

                        placeButton?.isEnabled = ready

                        statusText?.text =
                            if (ready) {
                                "Superfície encontrada. Toque em Posicionar."
                            } else {
                                "Mova o aparelho devagar sobre uma superfície."
                            }
                    }
                }
            }

            ar.lifecycle = viewLifecycleOwner.lifecycle

            host.addView(ar, fullSize())

            startArWatchdog(expected)
            loadModel()

        } catch (_: Exception) {
            startPreview(useCamera = true)
        }
    }

    /**
     * Troca automaticamente quando o ARCore não consegue rastrear.
     * Conta somente o tempo em que a tela está em primeiro plano.
     * A ausência de uma superfície, sozinha, não provoca a troca.
     */
    private fun startArWatchdog(expected: Int) {
        arWatchdogJob?.cancel()

        arWatchdogJob = viewLifecycleOwner.lifecycleScope.launch {
            var withoutTrackingMs = 0L
            var lastTime = SystemClock.elapsedRealtime()

            while (isActive) {
                delay(500L)

                if (
                    container == null ||
                    expected != generation ||
                    mode != Mode.AR
                ) {
                    return@launch
                }

                val now = SystemClock.elapsedRealtime()
                val elapsed = now - lastTime
                lastTime = now

                val resumed = viewLifecycleOwner.lifecycle
                    .currentState.isAtLeast(Lifecycle.State.RESUMED)

                if (!resumed || arTracking) {
                    withoutTrackingMs = 0L
                } else {
                    withoutTrackingMs += elapsed
                }

                if (withoutTrackingMs >= AR_TIMEOUT_MS) {
                    startPreview(useCamera = true)
                    return@launch
                }
            }
        }
    }

    private fun startPreview(useCamera: Boolean) {
        if (gameViewModel.jogoEncerrado) return
        val host = container ?: return

        clearScene()

        mode = if (useCamera) Mode.CAMERA else Mode.STATIC

        placeButton?.apply {
            text = "Centralizar"
            isEnabled = false
        }

        statusText?.text = if (useCamera) {
            "Modelo sobre a câmera, sem fixação no ambiente."
        } else {
            "Visualização 3D sem câmera."
        }

        host.setBackgroundColor(Color.rgb(45, 55, 65))

        val cameraView = if (useCamera) {
            PreviewView(requireContext()).apply {
                implementationMode =
                    PreviewView.ImplementationMode.COMPATIBLE

                scaleType = PreviewView.ScaleType.FILL_CENTER

                host.addView(this, fullSize())
            }
        } else {
            null
        }

        try {
            val scene = SceneView(
                context = requireContext(),
                isOpaque = !useCamera
            )

            scene.lifecycle = viewLifecycleOwner.lifecycle
            scene.cameraNode.position = Position(0f, 0f, 3f)
            scene.cameraNode.lookAt(Position(0f, 0f, 0f))

            sceneView = scene

            host.addView(scene, fullSize())

            loadModel()

            if (cameraView != null) {
                openCamera(cameraView)
            }
        } catch (_: Exception) {
            statusText?.text = "Não foi possível iniciar a cena 3D."
        }
    }

    private fun openCamera(previewView: PreviewView) {
        if (gameViewModel.jogoEncerrado) return
        val context = context ?: return

        val expected = generation
        val owner = viewLifecycleOwner

        // O prazo também cobre a obtenção do CameraProvider.
        startCameraWatchdog(expected)

        try {
            val future = ProcessCameraProvider.getInstance(context)

            future.addListener({
                if (
                    container == null ||
                    expected != generation ||
                    mode != Mode.CAMERA
                ) {
                    return@addListener
                }

                try {
                    val provider = future.get()

                    val selector = when {
                        provider.hasCamera(
                            CameraSelector.DEFAULT_BACK_CAMERA
                        ) -> CameraSelector.DEFAULT_BACK_CAMERA

                        provider.hasCamera(
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        ) -> CameraSelector.DEFAULT_FRONT_CAMERA

                        else -> {
                            startPreview(useCamera = false)
                            return@addListener
                        }
                    }

                    val preview = Preview.Builder().build()

                    preview.setSurfaceProvider(
                        previewView.surfaceProvider
                    )

                    cameraProvider = provider
                    cameraPreview = preview

                    previewView.previewStreamState.observe(owner) { state ->
                        if (
                            expected == generation &&
                            mode == Mode.CAMERA
                        ) {
                            cameraStreaming =
                                state == PreviewView.StreamState.STREAMING
                        }
                    }

                    provider.bindToLifecycle(
                        owner,
                        selector,
                        preview
                    )

                } catch (_: Exception) {
                    if (
                        expected == generation &&
                        container != null
                    ) {
                        startPreview(useCamera = false)
                    }
                }
            }, ContextCompat.getMainExecutor(context))

        } catch (_: Exception) {
            if (
                expected == generation &&
                container != null
            ) {
                startPreview(useCamera = false)
            }
        }
    }

    private fun startCameraWatchdog(expected: Int) {
        cameraWatchdogJob?.cancel()

        cameraWatchdogJob = viewLifecycleOwner.lifecycleScope.launch {
            var withoutCameraMs = 0L
            var lastTime = SystemClock.elapsedRealtime()

            while (isActive) {
                delay(500L)

                if (
                    container == null ||
                    expected != generation ||
                    mode != Mode.CAMERA
                ) {
                    return@launch
                }

                val now = SystemClock.elapsedRealtime()
                val elapsed = now - lastTime
                lastTime = now

                val resumed = viewLifecycleOwner.lifecycle
                    .currentState.isAtLeast(Lifecycle.State.RESUMED)

                if (!resumed || cameraStreaming) {
                    withoutCameraMs = 0L
                } else {
                    withoutCameraMs += elapsed
                }

                if (withoutCameraMs >= CAMERA_TIMEOUT_MS) {
                    startPreview(useCamera = false)
                    return@launch
                }
            }
        }
    }

    private fun loadModel(preserveAnchor: Boolean = false) {
        if (gameViewModel.jogoEncerrado) return
        val scene = sceneView ?: return
        val location = modelLocation(currentModelFile)

        if (location == null) {
            statusText?.text =
                "Modelo não encontrado: $currentModelFile"

            placeButton?.isEnabled = false
            return
        }

        loadJob?.cancel()
        lastGameTickMs = null
        removeModel(preserveAnchor)

        val expected = generation
        val expectedModel = ++modelGeneration
        val isAr = mode == Mode.AR
        val fileName = currentModelFile
        val round = gameViewModel.rodada

        placeButton?.isEnabled = false

        loadJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val node = modelCache[fileName] ?: run {
                    val instance = scene.modelLoader.loadModelInstance(location)
                    if (expected != generation || expectedModel != modelGeneration ||
                        sceneView !== scene || gameViewModel.jogoEncerrado) return@launch
                    if (instance == null) {
                        statusText?.setText(R.string.model_load_failed)
                        return@launch
                    }
                    ModelNode(
                        modelInstance = instance,
                        scaleToUnits = if (isAr) 0.5f else 1.5f,
                        centerOrigin = Position(0f, 0f, 0f)
                    ).also { modelCache[fileName] = it }
                }
                if (gameViewModel.partidaIniciada) node.position = Position(0f, 0f, 0f)
                node.isEditable = !gameViewModel.partidaIniciada
                node.isScaleEditable = false
                node.isVisible = !gameViewModel.partidaIniciada || !gameViewModel.objetoConsumido
                node.isTouchable = node.isVisible
                node.onSingleTapConfirmed = {
                    if (expected == generation && expectedModel == modelGeneration &&
                        !gameViewModel.jogoEncerrado) tocarObjeto(round)
                    true
                }
                modelNode = node

                if (!isAr) {
                    scene.addChildNode(node)
                    placeButton?.isEnabled = true
                } else if (anchorNode != null) {
                    anchorNode?.addChildNode(node)
                    placeButton?.isEnabled = true
                }
                updateGameControls()

            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                if (
                    expected == generation &&
                    expectedModel == modelGeneration
                ) {
                    statusText?.text =
                        "Erro ao carregar: $fileName"
                }
            }
        }
    }

    private fun placeModel() {
        if (gameViewModel.jogoEncerrado) return
        val ar = arView ?: return

        if (anchorNode != null) {
            loadModel()
            return
        }

        val hit = placementHit ?: return
        val model = modelNode ?: return

        try {
            val anchor = AnchorNode(
                engine = ar.engine,
                anchor = hit.createAnchor()
            )

            anchor.addChildNode(model)
            ar.addChildNode(anchor)

            anchorNode = anchor
            placementHit = null

            placeButton?.apply {
                text = "Reposicionar"
                isEnabled = true
            }

            statusText?.text = "Objeto posicionado no ambiente."

        } catch (_: Exception) {
            statusText?.text =
                "Não foi possível posicionar. Tente outra superfície."
        }
    }

    private fun modelLocation(fileName: String): String? {
        val context = context ?: return null

        try {
            context.assets.open(fileName).use { }
            return fileName
        } catch (_: Exception) {
            // Procura no armazenamento interno.
        }

        val file = File(context.filesDir, fileName)

        return if (file.isFile) {
            Uri.fromFile(file).toString()
        } else {
            null
        }
    }

    private fun removeModel(preserveAnchor: Boolean = false) {
        val scene = sceneView

        modelNode?.let { model ->
            runCatching {
                anchorNode?.removeChildNode(model)
                scene?.removeChildNode(model)
                // Os dois modelos são reutilizados; liberados em clearScene().
            }
        }

        modelNode = null
        if (preserveAnchor) return

        anchorNode?.let { anchor ->
            runCatching {
                scene?.removeChildNode(anchor)
                anchor.anchor.detach()
                anchor.destroy()
            }
        }

        anchorNode = null
        placementHit = null

        if (mode == Mode.AR) {
            placeButton?.text = "Posicionar"
        }
    }

    private fun clearScene() {
        lastGameTickMs = null
        generation++
        modelGeneration++

        arWatchdogJob?.cancel()
        arWatchdogJob = null

        cameraWatchdogJob?.cancel()
        cameraWatchdogJob = null

        loadJob?.cancel()
        loadJob = null

        cameraPreview?.let { preview ->
            runCatching {
                cameraProvider?.unbind(preview)
            }
        }

        cameraPreview = null
        cameraProvider = null

        arTracking = false
        cameraStreaming = false

        removeModel()
        modelCache.values.forEach { runCatching { it.destroy() } }
        modelCache.clear()

        sceneView?.let { scene ->
            runCatching {
                scene.lifecycle = null
                scene.destroy()
            }
        }

        sceneView = null
        arView = null

        container?.removeAllViews()
    }

    private fun fullSize() = FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        availabilityJob?.cancel()
        availabilityJob = null
        gameJob?.cancel()
        gameJob = null

        clearScene()

        overlay?.let {
            (it.parent as? ViewGroup)?.removeView(it)
        }

        rulesDialog?.dismiss()
        rulesDialog = null
        livesText = null
        targetText = null
        startButton = null
        overlay = null
        statusText = null
        placeButton = null
        container = null

        pendingAr = false
        mode = Mode.CHECKING

        super.onDestroyView()
    }
}