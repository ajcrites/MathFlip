package com.mathfacts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathfacts.domain.DEFAULT_MAX_BOUND
import com.mathfacts.domain.MathFactGenerator
import com.mathfacts.domain.Operation

private val DeepBlue = Color(0xFF071A3D)
private val EdgePurple = Color(0xFF3D1E68)
private val AccentBlue = Color(0xFF3B82F6)
private val AccentYellow = Color(0xFFFFC857)
private val MutedBlue = Color(0xFF243A60)
private val White = Color(0xFFF8FAFF)

private enum class Screen {
    Menu,
    Practice,
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

    MaterialTheme {
        GradientBackground {
            when (screen) {
                Screen.Menu -> MainMenu(
                    selectedOperations = selectedOperations,
                    onToggleOperation = { operation ->
                        selectedOperations = if (operation in selectedOperations) {
                            if (selectedOperations.size > 1) selectedOperations - operation
                            else selectedOperations
                        } else {
                            selectedOperations + operation
                        }
                    },
                    upperBound = upperBound,
                    onUpperBoundChange = { upperBound = it },
                    allowNegatives = allowNegatives,
                    onAllowNegativesChange = { allowNegatives = it },
                    onGo = { screen = Screen.Practice },
                )

                Screen.Practice -> PracticeScreen(
                    upperBound = upperBound.text.toInt(),
                    allowNegatives = allowNegatives,
                    operations = selectedOperations,
                    onBack = { screen = Screen.Menu },
                )
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

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 56.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Welcome to Math Facts!",
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
                modifier = Modifier.weight(1.5f),
            )
            UpperBoundInput(
                value = upperBound,
                onValueChange = onUpperBoundChange,
                isError = upperBound.text.isNotEmpty() && validUpperBound == null,
                modifier = Modifier.weight(0.65f),
            )
            NegativeSelector(
                checked = allowNegatives,
                onCheckedChange = onAllowNegativesChange,
                modifier = Modifier.weight(0.85f),
            )
        }

        Button(
            onClick = onGo,
            enabled = validUpperBound != null && selectedOperations.isNotEmpty(),
            modifier = Modifier.width(180.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentYellow,
                contentColor = DeepBlue,
                disabledContainerColor = MutedBlue,
                disabledContentColor = White.copy(alpha = 0.4f),
            ),
        ) {
            Text("GO!", fontSize = 28.sp, fontWeight = FontWeight.Black)
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
        Row(
            modifier = Modifier.padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OperationButton("+", Operation.Addition in selectedOperations) {
                onToggleOperation(Operation.Addition)
            }
            OperationButton("−", Operation.Subtraction in selectedOperations) {
                onToggleOperation(Operation.Subtraction)
            }
            OperationButton("×", selected = false, enabled = false) {}
            OperationButton("÷", selected = false, enabled = false) {}
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
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        MenuLabel("Up to")
        OutlinedTextField(
            value = value,
            onValueChange = { proposed ->
                val digits = proposed.text.filter(Char::isDigit).take(3)
                onValueChange(
                    proposed.copy(
                        text = digits,
                        selection = TextRange(
                            start = proposed.selection.start.coerceIn(0, digits.length),
                            end = proposed.selection.end.coerceIn(0, digits.length),
                        ),
                    ),
                )
            },
            modifier = Modifier
                .padding(top = 10.dp)
                .width(130.dp)
                .onFocusChanged { state ->
                    if (state.isFocused) {
                        onValueChange(value.copy(selection = TextRange(0, value.text.length)))
                    }
                },
            textStyle = TextStyle(
                color = White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            ),
            singleLine = true,
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentYellow,
                unfocusedBorderColor = AccentBlue.copy(alpha = 0.75f),
                cursorColor = AccentYellow,
                errorBorderColor = Color(0xFFFF7A7A),
            ),
        )
    }
}

@Composable
private fun NegativeSelector(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.clickable(role = Role.Checkbox) { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = AccentYellow,
                checkmarkColor = DeepBlue,
                uncheckedColor = White.copy(alpha = 0.7f),
            ),
        )
        Spacer(Modifier.width(4.dp))
        MenuLabel("Allow Negatives?")
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
) {
    val range = if (allowNegatives) -upperBound..upperBound else 0..upperBound
    val factGenerator = remember(range, operations) {
        MathFactGenerator(range = range, operations = operations)
    }
    var fact by remember(factGenerator) { mutableStateOf(factGenerator.next()) }
    var isAnswerVisible by remember(factGenerator) { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val faceDownDetector = remember { FaceDownDetector() }

    DisposableEffect(faceDownDetector) {
        faceDownDetector.start { isAnswerVisible = true }
        onDispose { faceDownDetector.stop() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
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
        BackButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart).padding(start = 18.dp, top = 14.dp),
        )

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

expect class FaceDownDetector() {
    fun start(onFaceDown: () -> Unit)
    fun stop()
}
