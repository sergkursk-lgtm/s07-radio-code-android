package com.soueast.s07code

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soueast.s07code.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            S07RadioCodeTheme {
                S07CodeScreen()
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

fun getCurrentVersionName(context: Context): String {
    return try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName ?: "1.0"
    } catch (e: PackageManager.NameNotFoundException) {
        "1.0"
    }
}

private fun isUpdateCheckDisabled(context: Context): Boolean {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    return prefs.getBoolean("disable_update_check", false)
}

private fun setUpdateCheckDisabled(context: Context, disabled: Boolean) {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    prefs.edit().putBoolean("disable_update_check", disabled).apply()
}

@Composable
fun S07CodeScreen() {
    val calendar = remember { Calendar.getInstance() }
    var currentMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH) + 1) }
    var currentDay by remember { mutableIntStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
    var currentHour by remember { mutableIntStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var secondsLeft by remember { mutableIntStateOf(0) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var updateInfo by remember { mutableStateOf<GithubRelease?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Calendar.getInstance()
            currentMonth = now.get(Calendar.MONTH) + 1
            currentDay = now.get(Calendar.DAY_OF_MONTH)
            currentHour = now.get(Calendar.HOUR_OF_DAY)

            val minutes = now.get(Calendar.MINUTE)
            val seconds = now.get(Calendar.SECOND)
            secondsLeft = (3600 - (minutes * 60 + seconds)).coerceAtLeast(0)

            delay(1000L)
        }
    }

    LaunchedEffect(Unit) {
        if (!isUpdateCheckDisabled(context)) {
            val currentVersionName = getCurrentVersionName(context)
            val release = UpdateChecker.checkForUpdate(currentVersionName)
            if (release != null) {
                updateInfo = release
                showUpdateDialog = true
            }
        }
    }

    if (showUpdateDialog && updateInfo != null) {
        UpdateDialog(
            release = updateInfo!!,
            isDownloading = isDownloading,
            onDismiss = { showUpdateDialog = false },
            onConfirm = {
                isDownloading = true
                scope.launch {
                    UpdateChecker.downloadAndInstall(context, updateInfo!!)
                    isDownloading = false
                    showUpdateDialog = false
                }
            },
            onDontAskAgain = {
                setUpdateCheckDisabled(context, true)
                showUpdateDialog = false
            }
        )
    }

    val code = generateCode(currentMonth, currentDay, currentHour)
    val minutesLeft = secondsLeft / 60
    val secsLeft = secondsLeft % 60

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    val titleFontSize = if (isLandscape) 20.sp else 24.sp
    val codeFontSize = if (isLandscape) 40.sp else 52.sp
    val timerFontSize = if (isLandscape) 24.sp else 30.sp
    val horizontalPadding = if (isLandscape) 16.dp else 20.dp
    val cardPadding = if (isLandscape) 16.dp else 28.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontalPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(max = 350.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(cardPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
Text(
                                text = "Код Soueast S07 awd",
                                fontSize = titleFontSize,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A237E)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "КОД ДЛЯ ГУ",
                                fontSize = 14.sp,  // was 11.sp
                                color = Color(0xFF78909C),
                                letterSpacing = 1.sp
                            )
                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFE8EDF2), RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFB))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = code,
                                    fontSize = codeFontSize,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 6.sp,
                                    color = GreenCode,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFE8EDF2), RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFB))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "КОДУ ОСТАЛОСЬ ЖИТЬ",
                                    fontSize = 12.sp,
                                    color = Color(0xFF78909C),
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = String.format("%02d:%02d", minutesLeft, secsLeft),
                                    fontSize = timerFontSize,
                                    color = YellowTimer,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(max = 350.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(cardPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Как попасть в меню ADB:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A237E)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        InstructionStep("1", "Откройте звонилку")
                        InstructionStep("2", "Наберите *#20230730#*")
                        InstructionStep("3", "Выберите предпоследний пункт")
                        InstructionStep("4", "Введите код")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 400.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(cardPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Код Soueast S07 awd",
                            fontSize = titleFontSize,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A237E)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "КОД ДЛЯ ГУ",
                            fontSize = 12.sp,
                            color = Color(0xFF78909C),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFE8EDF2), RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFB))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = code,
                                    fontSize = codeFontSize,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 6.sp,
                                    color = GreenCode,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFE8EDF2), RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFB))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "КОДУ ОСТАЛОСЬ ЖИТЬ",
                                    fontSize = 11.sp,
                                    color = Color(0xFF78909C),
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = String.format("%02d:%02d", minutesLeft, secsLeft),
                                    fontSize = timerFontSize,
                                    color = YellowTimer,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GreenCode.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FAF8))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Как попасть в меню ADB:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1A237E)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                InstructionStep("1", "Откройте звонилку")
                                InstructionStep("2", "Наберите *#20230730#*")
                                InstructionStep("3", "Выберите предпоследний пункт")
                                InstructionStep("4", "Введите код")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InstructionStep(number: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(GreenCode.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = GreenCode
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 15.sp,
            color = Color(0xFF37474F),
            lineHeight = 20.sp
        )
    }
}

@Composable
fun UpdateDialog(
    release: GithubRelease,
    isDownloading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onDontAskAgain: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isDownloading) onDismiss() },
        containerColor = Color.White,
        title = {
            Text(
                text = "Доступно обновление",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF1A237E)
            )
        },
        text = {
            Column {
                Text(
                    text = "Версия ${release.versionName}",
                    fontSize = 20.sp,  // was 16.sp
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF37474F)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = release.releaseNotes,
                    fontSize = 18.sp,  // was 14.sp
                    color = Color(0xFF546E7A),
                    lineHeight = 24.sp
                )
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDontAskAgain,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFECEFF1),
                            contentColor = Color(0xFF546E7A)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 3.dp,
                            pressedElevation = 1.dp
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "Больше не спрашивать",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFECEFF1),
                            contentColor = Color(0xFF546E7A)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 3.dp,
                            pressedElevation = 1.dp
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "Позже",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Button(
                    onClick = onConfirm,
                    enabled = !isDownloading,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenCode,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 2.dp
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        if (isDownloading) "Загрузка..." else "Установить",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        dismissButton = {}
    )
}
