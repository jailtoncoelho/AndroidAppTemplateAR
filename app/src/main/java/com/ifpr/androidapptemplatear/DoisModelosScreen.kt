package com.ifpr.androidapptemplatear

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader

@Composable
fun DoisModelosScreen() {
    var mostrarModelos by remember { mutableStateOf(false) }

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val cameraNode = rememberCameraNode(engine) {
        position = Position(z = 4f)
    }


    val processador = rememberModelInstance(modelLoader, "processador.glb")
    val ram = rememberModelInstance(modelLoader, "ramfinalfile.glb")

    Box(Modifier.fillMaxSize()) {
        Scene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            cameraNode = cameraNode
        ) {

            if (mostrarModelos) {
                processador?.let {
                    ModelNode(
                        modelInstance = it,
                        scaleToUnits = 1f,
                        apply = { position = Position(x = -0.8f) }
                    )
                }
                ram?.let {
                    ModelNode(
                        modelInstance = it,
                        scaleToUnits = 1f,
                        apply = { position = Position(x = 0.8f) }
                    )
                }
            }
        }

        Button(
            onClick = { mostrarModelos = !mostrarModelos },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
        ) {
            Text(if (mostrarModelos) "Esconder modelos" else "Mostrar processador e RAM")
        }
    }
}