package com.mathfacts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathfacts.domain.MathFactGenerator

private val DeepBlue = Color(0xFF071A3D)
private val EdgePurple = Color(0xFF3D1E68)

@Composable
fun App() {
    MaterialTheme {
        val factGenerator = remember { MathFactGenerator() }
        var fact by remember { mutableStateOf(factGenerator.next()) }
        var isAnswerVisible by remember { mutableStateOf(false) }
        val interactionSource = remember { MutableInteractionSource() }
        val faceDownDetector = remember { FaceDownDetector() }

        DisposableEffect(faceDownDetector) {
            faceDownDetector.start {
                isAnswerVisible = true
            }
            onDispose { faceDownDetector.stop() }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colorStops = arrayOf(
                            0.0f to DeepBlue,
                            0.62f to DeepBlue,
                            1.0f to EdgePurple,
                        ),
                        center = Offset.Unspecified,
                        radius = 1_200f,
                    ),
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        if (isAnswerVisible) {
                            fact = factGenerator.next()
                            isAnswerVisible = false
                        } else {
                            isAnswerVisible = true
                        }
                    },
                ),
        ) {
            if (isAnswerVisible) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(horizontal = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    FactText(text = fact.equation, fontSize = 48)
                    FactText(text = fact.answer.toString(), fontSize = 168)
                }
            } else {
                FactText(
                    text = fact.question,
                    fontSize = 168,
                    modifier = Modifier.align(Alignment.Center).padding(horizontal = 48.dp),
                )
            }
        }
    }
}

@Composable
private fun FactText(
    text: String,
    fontSize: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        color = Color.White,
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = (fontSize + 8).sp,
        textAlign = TextAlign.Center,
        style = TextStyle(
            shadow = Shadow(
                color = Color(0xFF02040C).copy(alpha = 0.9f),
                offset = Offset(8f, 12f),
                blurRadius = 18f,
            ),
        ),
    )
}

expect class FaceDownDetector() {
    fun start(onFaceDown: () -> Unit)
    fun stop()
}
