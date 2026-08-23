package com.soueast.s07code

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soueast.s07code.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            S07RadioCodeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    S07CodeScreen()
                }
            }
        }
    }
}

fun generateCode(month: Int, day: Int, hour: Int): String {
    val s = String.format("%02d%02d%02d", month, day, hour)
    val num = s.toLong() * 240830L
    val code = num % 1000000L
    return String.format("%06d", code)
}

@Composable
fun S07CodeScreen() {
    val calendar = remember { Calendar.getInstance() }
    var currentMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH) + 1) }
    var currentDay by remember { mutableIntStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
    var currentHour by remember { mutableIntStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var secondsLeft by remember { mutableIntStateOf(0) }

    // Manual input state
    var inputMonth by remember { mutableStateOf("") }
    var inputDay by remember { mutableStateOf("") }
    var inputHour by remember { mutableStateOf("") }
    var manualCode by remember { mutableStateOf("") }

    // Timer coroutine
    LaunchedEffect(Unit) {
        while (true) {
            val now = Calendar.getInstance()
            currentMonth = now.get(Calendar.MONTH) + 1
            currentDay = now.get(Calendar.DAY_OF_MONTH)
            currentHour = now.get(Calendar.HOUR_OF_DAY)

            val minutes = now.get(Calendar.MINUTE)
            val seconds = now.get(Calendar.SECOND)
            secondsLeft = 3600 - (minutes * 60 + seconds)

            delay(1000L)
        }
    }

    val code = generateCode(currentMonth, currentDay, currentHour)
    val minutesLeft = secondsLeft / 60
    val secsLeft = secondsLeft % 60

    val months = listOf(
        "января", "февраля", "марта", "апреля", "мая", "июня",
        "июля", "августа", "сентября", "октября", "ноября", "декабря"
    )
    val dateText = "${currentDay} ${months[currentMonth - 1]} ${calendar.get(Calendar.YEAR)}"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = CardDefaults.outlinedCardBorder().takeIf { false },
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "Код Soueast S07",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "КОД ДЛЯ МАГНИТОЛЫ",
                    fontSize = 13.sp,
                    color = SubText,
                    letterSpacing = 0.08.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ADB Instruction block
                InstructionBlock()

                Spacer(modifier = Modifier.height(6.dp))

                // Current code
                Text(
                    text = code,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 6.sp,
                    color = GreenCode,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "КОДУ ОСТАЛОСЬ ЖИТЬ",
                    fontSize = 13.sp,
                    color = SubText,
                    letterSpacing = 0.08.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Timer
                Text(
                    text = String.format("%02d:%02d", minutesLeft, secsLeft),
                    fontSize = 24.sp,
                    color = YellowTimer,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(22.dp))

                // Divider
                Divider(
                    thickness = 1.dp,
                    color = CardBorder
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Manual input section
                Text(
                    text = "ВРУЧНУЮ: месяц, день, час (как на магнитоле)",
                    fontSize = 13.sp,
                    color = SubText,
                    letterSpacing = 0.08.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CodeInput(
                        value = inputMonth,
                        onValueChange = { inputMonth = it },
                        placeholder = "ММ",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    CodeInput(
                        value = inputDay,
                        onValueChange = { inputDay = it },
                        placeholder = "ДД",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    CodeInput(
                        value = inputHour,
                        onValueChange = { inputHour = it },
                        placeholder = "ЧЧ",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val m = inputMonth.toIntOrNull()
                        val d = inputDay.toIntOrNull()
                        val h = inputHour.toIntOrNull()
                        if (m != null && d != null && h != null) {
                            manualCode = generateCode(m, d, h)
                        } else {
                            manualCode = "— введи ММ ДД ЧЧ —"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButtonGreen,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Сгенерировать",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (manualCode.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = manualCode,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 5.sp,
                        color = GreenCode,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun InstructionBlock() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = InstructionBg),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Как попасть в меню ADB:",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = YellowTimer
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "1. Откройте звонилку",
                fontSize = 12.sp,
                color = SubText,
                lineHeight = 18.sp
            )
            Text(
                text = "2. Наберите *#20230730#*",
                fontSize = 12.sp,
                color = SubText,
                lineHeight = 18.sp
            )
            Text(
                text = "3. Выберите предпоследний пункт",
                fontSize = 12.sp,
                color = SubText,
                lineHeight = 18.sp
            )
            Text(
                text = "4. Введите код",
                fontSize = 12.sp,
                color = SubText,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun CodeInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = { if (it.length <= 2) onValueChange(it) },
        placeholder = {
            Text(
                text = placeholder,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF555D6B)
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            color = Color.White
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = InputBg,
            unfocusedContainerColor = InputBg,
            focusedIndicatorColor = ButtonGreen,
            unfocusedIndicatorColor = InputBorder,
            cursorColor = ButtonGreen
        ),
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, InputBorder, RoundedCornerShape(8.dp))
    )
}
