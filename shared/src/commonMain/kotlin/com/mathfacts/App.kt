package com.mathfacts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mathfacts.domain.DEFAULT_MAX_BOUND
import com.mathfacts.domain.MathFactGenerator
import com.mathfacts.domain.Operation
import com.mathfacts.motion.MotionSampleProvider
import com.mathfacts.motion.PracticeMotionController
import com.mathfacts.motion.PracticeMotionEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val DeepBlue = Color(0xFF071A3D)
private val EdgePurple = Color(0xFF3D1E68)
private val AccentBlue = Color(0xFF3B82F6)
private val AccentYellow = Color(0xFFFFC857)
private val MutedBlue = Color(0xFF243A60)
private val White = Color(0xFFF8FAFF)
private val IncorrectRed = Color(0xFFFF5252)
private val CorrectGreen = Color(0xFF42D77D)
private val IncorrectGradientRed = Color(0xFF7A202A)
private val CorrectGradientGreen = Color(0xFF006B3C)

private enum class Screen {
    Menu,
    Practice,
    Results,
}

private enum class PracticePhase {
    Question,
    Revealing,
    AnswerLocked,
    Answer,
    Scoring,
}

private object FullMotionDurationScale : MotionDurationScale {
    override val scaleFactor: Float = 1f
}

@Composable
fun App() {
    var screen by remember { mutableStateOf(Screen.Menu) }
    var selectedOperations by remember {
        mutableStateOf(setOf(Operation.Addition, Operation.Subtraction))
    }
    var upperBound by remember {
        mutableStateOf(TextFieldValue(DEFAULT_MAX_BOUND.toString()))
    }
    var allowNegatives by remember { mutableStateOf(false) }
    var correctAnswers by remember { mutableStateOf(0) }
    var incorrectAnswers by remember { mutableStateOf(0) }
    val navigationAlpha = remember { Animatable(1f) }
    val navigationScope = rememberCoroutineScope()

    fun navigateTo(destination: Screen) {
        if (destination == screen) return
        navigationScope.launch(FullMotionDurationScale) {
            navigationAlpha.animateTo(0f, tween(durationMillis = 250))
            screen = destination
            navigationAlpha.animateTo(1f, tween(durationMillis = 250))
        }
    }

    MaterialTheme {
        GradientBackground {
            Box(Modifier.fillMaxSize().graphicsLayer { alpha = navigationAlpha.value }) {
                when (screen) {
                Screen.Menu -> MainMenu(
                    selectedOperations = selectedOperations,
                    onToggleOperation = { operation ->
                        selectedOperations = if (operation in selectedOperations) {
                            selectedOperations - operation
                        } else {
                            selectedOperations + operation
                        }
                    },
                    upperBound = upperBound,
                    onUpperBoundChange = { upperBound = it },
                    allowNegatives = allowNegatives,
                    onAllowNegativesChange = { allowNegatives = it },
                    onGo = {
                        correctAnswers = 0
                        incorrectAnswers = 0
                        navigateTo(Screen.Practice)
                    },
                )

                Screen.Practice -> PracticeScreen(
                    upperBound = upperBound.text.toInt(),
                    allowNegatives = allowNegatives,
                    operations = selectedOperations,
                    onBack = { navigateTo(Screen.Menu) },
                    onResults = { correct, incorrect ->
                        correctAnswers = correct
                        incorrectAnswers = incorrect
                        navigateTo(Screen.Results)
                    },
                )

                Screen.Results -> ResultsScreen(
                    correctAnswers = correctAnswers,
                    incorrectAnswers = incorrectAnswers,
                    onTryAgain = {
                        correctAnswers = 0
                        incorrectAnswers = 0
                        navigateTo(Screen.Practice)
                    },
                    onHome = { navigateTo(Screen.Menu) },
                )
                }
            }
        }
    }
}

@Composable
private fun GradientBackground(content: @Composable () -> Unit) {
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
            ),
    ) {
        content()
    }
}

@Composable
private fun MainMenu(
    selectedOperations: Set<Operation>,
    onToggleOperation: (Operation) -> Unit,
    upperBound: TextFieldValue,
    onUpperBoundChange: (TextFieldValue) -> Unit,
    allowNegatives: Boolean,
    onAllowNegativesChange: (Boolean) -> Unit,
    onGo: () -> Unit,
) {
    val validUpperBound = upperBound.text.toIntOrNull()?.takeIf { it in 1..999 }
    val focusManager = LocalFocusManager.current
    val dismissInteractionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = dismissInteractionSource,
                indication = null,
                onClick = { focusManager.clearFocus() },
            )
            .padding(horizontal = 56.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Welcome to Math Flip!",
            color = White,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            style = TextStyle(
                shadow = Shadow(Color.Black.copy(alpha = 0.65f), Offset(4f, 6f), 10f),
            ),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(36.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OperationSelector(
                selectedOperations = selectedOperations,
                onToggleOperation = onToggleOperation,
                modifier = Modifier.weight(1f),
            )
            UpperBoundInput(
                value = upperBound,
                onValueChange = onUpperBoundChange,
                isError = upperBound.text.isNotEmpty() && validUpperBound == null,
                modifier = Modifier.weight(1f),
            )
            NegativeSelector(
                checked = allowNegatives,
                onCheckedChange = onAllowNegativesChange,
                modifier = Modifier.weight(1f),
            )
        }

        Button(
            onClick = {
                focusManager.clearFocus()
                onGo()
            },
            enabled = validUpperBound != null && selectedOperations.isNotEmpty(),
            modifier = Modifier.width(if (selectedOperations.isEmpty()) 300.dp else 180.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentYellow,
                contentColor = AccentBlue,
                disabledContainerColor = MutedBlue,
                disabledContentColor = White.copy(alpha = 0.4f),
            ),
        ) {
            Text(
                text = if (selectedOperations.isEmpty()) "Select an Operator." else "GO!",
                fontSize = if (selectedOperations.isEmpty()) 21.sp else 28.sp,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

@Composable
private fun OperationSelector(
    selectedOperations: Set<Operation>,
    onToggleOperation: (Operation) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        MenuLabel("Operations")
        Column(
            modifier = Modifier.padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OperationButton("+", Operation.Addition in selectedOperations) {
                    onToggleOperation(Operation.Addition)
                }
                OperationButton("−", Operation.Subtraction in selectedOperations) {
                    onToggleOperation(Operation.Subtraction)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OperationButton("×", selected = false, enabled = false) {}
                OperationButton("÷", selected = false, enabled = false) {}
            }
        }
    }
}

@Composable
private fun OperationButton(
    symbol: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val background = when {
        !enabled -> MutedBlue.copy(alpha = 0.42f)
        selected -> AccentYellow
        else -> MutedBlue
    }
    val foreground = when {
        !enabled -> White.copy(alpha = 0.25f)
        selected -> AccentBlue
        else -> White.copy(alpha = 0.55f)
    }

    Box(
        modifier = Modifier
            .size(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .toggleable(
                value = selected,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = { onClick() },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, color = foreground, fontSize = 32.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun UpperBoundInput(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    PlatformUpperBoundInput(value, onValueChange, isError, modifier)
}

@Composable
private fun NegativeSelector(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (checked) AccentYellow else MutedBlue)
            .toggleable(
                value = checked,
                role = Role.Checkbox,
                onValueChange = onCheckedChange,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (checked) "Allow Negatives!" else "Allow Negatives?",
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            color = if (checked) AccentBlue else White.copy(alpha = 0.7f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MenuLabel(text: String) {
    Text(text, color = White.copy(alpha = 0.86f), fontSize = 18.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun PracticeScreen(
    upperBound: Int,
    allowNegatives: Boolean,
    operations: Set<Operation>,
    onBack: () -> Unit,
    onResults: (correct: Int, incorrect: Int) -> Unit,
) {
    val range = if (allowNegatives) -upperBound..upperBound else 0..upperBound
    val factGenerator = remember(range, operations) {
        MathFactGenerator(range = range, operations = operations)
    }
    var fact by remember(factGenerator) { mutableStateOf(factGenerator.next()) }
    var phase by remember(factGenerator) { mutableStateOf(PracticePhase.Question) }
    val results = remember(factGenerator) { mutableStateListOf<Boolean>() }
    val motionController = remember(factGenerator) { PracticeMotionController() }
    val motionSampleProvider = remember { MotionSampleProvider() }
    val scope = rememberCoroutineScope()
    val questionAlpha = remember(factGenerator) { Animatable(0f) }
    val feedbackProgress = remember(factGenerator) { Animatable(0f) }
    val feedbackAlpha = remember(factGenerator) { Animatable(0f) }
    val contentAlpha = remember(factGenerator) { Animatable(1f) }
    val contentScale = remember(factGenerator) { Animatable(1f) }
    var feedbackIsCorrect by remember(factGenerator) { mutableStateOf<Boolean?>(null) }

    fun showAnswer() {
        if (phase != PracticePhase.Question) return
        phase = PracticePhase.Revealing
        motionController.onAnswerShown()
        scope.launch(FullMotionDurationScale) {
            contentAlpha.animateTo(0f, tween(durationMillis = 125))
            phase = PracticePhase.AnswerLocked
            contentScale.snapTo(0.82f)
            launch {
                delay(500)
                if (phase == PracticePhase.AnswerLocked) {
                    phase = PracticePhase.Answer
                    motionController.unlockAnswer()
                }
            }
            launch {
                contentScale.animateTo(
                    1f,
                    keyframes {
                        durationMillis = 350
                        0.82f at 0
                        1.12f at 220
                        1f at 350
                    },
                )
            }
            contentAlpha.animateTo(1f, tween(durationMillis = 125))
        }
    }

    fun recordAnswer(isCorrect: Boolean) {
        if (phase != PracticePhase.Answer) return
        phase = PracticePhase.Scoring
        motionController.disable()
        scope.launch(FullMotionDurationScale) {
            results += isCorrect
            feedbackIsCorrect = isCorrect
            feedbackProgress.snapTo(0f)
            feedbackAlpha.snapTo(1f)
            feedbackProgress.animateTo(1f, tween(durationMillis = 500))
            contentAlpha.animateTo(0f, tween(durationMillis = 125))
            fact = factGenerator.next()
            phase = PracticePhase.Question
            motionController.onQuestionShown()
            contentScale.snapTo(1f)
            launch { contentAlpha.animateTo(1f, tween(durationMillis = 250)) }
            feedbackAlpha.animateTo(0f, tween(durationMillis = 250))
            feedbackIsCorrect = null
        }
    }

    LaunchedEffect(fact) {
        questionAlpha.snapTo(0f)
        launch(FullMotionDurationScale) {
            questionAlpha.animateTo(1f, tween(durationMillis = 250))
        }
    }
    DisposableEffect(motionSampleProvider, motionController) {
        motionController.onQuestionShown()
        motionSampleProvider.start { sample ->
            when (motionController.process(sample)) {
                PracticeMotionEvent.RevealAnswer -> showAnswer()
                PracticeMotionEvent.Correct -> recordAnswer(isCorrect = true)
                PracticeMotionEvent.Incorrect -> recordAnswer(isCorrect = false)
                null -> Unit
            }
        }
        onDispose { motionSampleProvider.stop() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (phase == PracticePhase.Answer || phase == PracticePhase.AnswerLocked) {
                    Modifier.background(
                        Brush.horizontalGradient(
                            0.0f to IncorrectGradientRed,
                            0.25f to DeepBlue,
                            0.75f to DeepBlue,
                            1.0f to CorrectGradientGreen,
                        ),
                    )
                } else {
                    Modifier
                },
            )
            .pointerInput(phase) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val up = waitForUpOrCancellation() ?: return@awaitEachGesture
                    val horizontalMovement = up.position.x - down.position.x
                    val swipeThreshold = 48.dp.toPx()
                    val tappedTopAction = up.position.y <= 80.dp.toPx() &&
                        (up.position.x <= 80.dp.toPx() || up.position.x >= size.width - 160.dp.toPx())

                    if (tappedTopAction) return@awaitEachGesture

                    when (phase) {
                        PracticePhase.Question -> showAnswer()
                        PracticePhase.Answer -> when {
                            horizontalMovement <= -swipeThreshold -> recordAnswer(isCorrect = false)
                            horizontalMovement >= swipeThreshold -> recordAnswer(isCorrect = true)
                            up.position.x <= size.width * 0.2f -> recordAnswer(isCorrect = false)
                            up.position.x >= size.width * 0.8f -> recordAnswer(isCorrect = true)
                        }
                        PracticePhase.Revealing,
                        PracticePhase.AnswerLocked,
                        PracticePhase.Scoring,
                        -> Unit
                    }
                }
            },
    ) {
        BackButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart).padding(start = 18.dp, top = 14.dp),
        )

        Button(
            onClick = {
                onResults(
                    results.count { it },
                    results.count { !it },
                )
            },
            modifier = Modifier.align(Alignment.TopEnd).padding(end = 18.dp, top = 14.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MutedBlue,
                contentColor = White,
            ),
        ) {
            Text("Results", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp, start = 80.dp, end = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            results.forEachIndexed { index, isCorrect ->
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (isCorrect) CorrectGreen else IncorrectRed)
                        .semantics {
                            contentDescription =
                                "Answer ${index + 1}: ${if (isCorrect) "correct" else "incorrect"}"
                        },
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .zIndex(1f)
                .graphicsLayer {
                    alpha = contentAlpha.value
                    scaleX = contentScale.value
                    scaleY = contentScale.value
                },
        ) {
            if (phase == PracticePhase.AnswerLocked ||
                phase == PracticePhase.Answer ||
                phase == PracticePhase.Scoring
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 48.dp),
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
                    modifier = Modifier
                        .padding(horizontal = 48.dp)
                        .graphicsLayer { alpha = questionAlpha.value },
                )
            }
        }

        feedbackIsCorrect?.let { isCorrect ->
            val progress = feedbackProgress.value
            val feedbackBrush = if (isCorrect) {
                if (progress >= 0.999f) {
                    Brush.horizontalGradient(listOf(CorrectGradientGreen, CorrectGradientGreen))
                } else {
                    val boundary = 0.75f * (1f - progress)
                    Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            boundary to Color.Transparent,
                            (boundary + 0.08f).coerceAtMost(1f) to CorrectGradientGreen,
                            1f to CorrectGradientGreen,
                        ),
                    )
                }
            } else {
                if (progress >= 0.999f) {
                    Brush.horizontalGradient(listOf(IncorrectGradientRed, IncorrectGradientRed))
                } else {
                    val boundary = 0.25f + (0.75f * progress)
                    Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0f to IncorrectGradientRed,
                            (boundary - 0.08f).coerceAtLeast(0f) to IncorrectGradientRed,
                            boundary to Color.Transparent,
                            1f to Color.Transparent,
                        ),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = feedbackAlpha.value }
                    .background(feedbackBrush),
            )
        }
    }
}

@Composable
private fun ResultsScreen(
    correctAnswers: Int,
    incorrectAnswers: Int,
    onTryAgain: () -> Unit,
    onHome: () -> Unit,
) {
    val totalAnswers = correctAnswers + incorrectAnswers
    val correctFraction = if (totalAnswers == 0) 0.5f else correctAnswers.toFloat() / totalAnswers
    val backgroundBrush = when (correctFraction) {
        0f -> Brush.verticalGradient(listOf(IncorrectGradientRed, IncorrectGradientRed))
        1f -> Brush.verticalGradient(listOf(CorrectGradientGreen, CorrectGradientGreen))
        else -> {
            val boundary = 1f - correctFraction
            val transitionStart = (boundary - 0.06f).coerceAtLeast(0f)
            val transitionEnd = (boundary + 0.06f).coerceAtMost(1f)
            Brush.verticalGradient(
                colorStops = arrayOf(
                    0f to IncorrectGradientRed,
                    transitionStart to IncorrectGradientRed,
                    transitionEnd to CorrectGradientGreen,
                    1f to CorrectGradientGreen,
                ),
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(backgroundBrush),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "$correctAnswers Correct",
                    color = White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    text = "$incorrectAnswers Incorrect",
                    color = White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                ResultsActionButton("Try Again", onTryAgain)
                ResultsActionButton("Home", onHome)
            }
        }
    }
}

@Composable
private fun ResultsActionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AccentYellow,
            contentColor = AccentBlue,
        ),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
private fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(48.dp)
            .semantics { contentDescription = "Back to main menu" }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(16.dp, 28.dp)) {
            val chevron = Path().apply {
                moveTo(size.width * 0.82f, size.height * 0.08f)
                lineTo(size.width * 0.2f, size.height * 0.5f)
                lineTo(size.width * 0.82f, size.height * 0.92f)
            }
            drawPath(
                path = chevron,
                color = White.copy(alpha = 0.68f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx()),
            )
        }
    }
}

@Composable
private fun FactText(text: String, fontSize: Int, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        color = White,
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

@Composable
expect fun PlatformUpperBoundInput(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier,
)
