package ua.vitolex.measuringscreen

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ua.vitolex.measuringscreen.ui.theme.MeasuringScreenTheme

import kotlin.math.round


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this)
        setContent {
            val scale = this.resources.displayMetrics.density

            val dpi = this.resources.displayMetrics.densityDpi


            val ydpi = this.resources.displayMetrics.ydpi


//            px (float) to см: float/ydpi*2.54
//            dp to см: dp*scale/ydpi*2.54


            var unitsOfMeasurement by remember { mutableStateOf("cm") }
            var geometricFigure by remember { mutableStateOf("circle") }
            var koef by remember { mutableStateOf(2.54f) }
            var radius by remember { mutableStateOf(ydpi / koef) }
            var screenWidth =
                this.resources.displayMetrics.widthPixels / ydpi * koef - 40 * scale / ydpi * koef

            var angle by remember { mutableStateOf(90f) }

            val text = radius / ydpi * koef

            suspend fun PointerInputScope.detectPressGestures(
                onLongPress: ((Offset) -> Unit)? = null,
                onPressStart: ((Offset) -> Unit)? = null,
                onPressEnd: (() -> Unit)? = null,
                onLongPressEnd: (() -> Unit)? = null,
            ) = coroutineScope {

                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()

                    val downTime = System.currentTimeMillis()
                    onPressStart?.invoke(down.position)

                    val longPressTimeout = onLongPress?.let {
                        viewConfiguration.longPressTimeoutMillis
                    } ?: (Long.MAX_VALUE / 2)

                    var longPressInvoked = false

                    do {
                        val event: PointerEvent = awaitPointerEvent()
                        val currentTime = System.currentTimeMillis()

                        if (!longPressInvoked && currentTime - downTime >= longPressTimeout) {
                            onLongPress?.invoke(event.changes.first().position)
                            longPressInvoked = true
                        }

                        event.changes
                            .forEach { pointerInputChange: PointerInputChange ->
                                pointerInputChange.consume()
                            }


                    } while (event.changes.any { it.pressed })

                    if (longPressInvoked) {
                        onLongPressEnd?.invoke()
                    } else {
                        onPressEnd?.invoke()
                    }

                }
            }


            MeasuringScreenTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        Box(
                            modifier = Modifier.heightIn(min = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
//                            BannerAdView(id = "ca-app-pub-3940256099942544/9214589741")
                        BannerAdView(id = "ca-app-pub-1869740172940843/7635799124")
                        }
                        Box ( contentAlignment = Alignment.BottomStart){
                            Canvas(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth()
                                    .padding(20.dp),
                            ) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                drawLine(
                                    start = Offset(x = 0f, y = -20f),
                                    end = Offset(x = 0f, y = canvasHeight),
                                    color = Color.Gray,
                                    strokeWidth = 2f,
                                    alpha = 0.2f
                                )
                                for (i in 0..(canvasWidth / ydpi * koef).toInt() * 2 + 1) {
                                    drawLine(
                                        start = Offset(x = i * ydpi / koef / 2, y = -20f),
                                        end = Offset(x = i * ydpi / koef / 2, y = canvasHeight),
                                        color = Color.Gray,
                                        strokeWidth = 2f,
                                        alpha = 0.2f
                                    )
                                }

                                drawLine(
                                    start = Offset(x = -20f, y = 0f),
                                    end = Offset(x = canvasWidth, y = 0f),
                                    color = Color.Gray,
                                    strokeWidth = 2f,
                                    alpha = 0.2f
                                )

                                for (i in 0..(canvasHeight / ydpi * koef).toInt() * 2 + 1) {
                                    drawLine(
                                        start = Offset(
                                            x = -20f,
                                            y = i * ydpi / koef.toFloat() / 2.toFloat()
                                        ),
                                        end = Offset(
                                            x = canvasWidth,
                                            y = i * ydpi / koef.toFloat() / 2.toFloat()
                                        ),
                                        color = Color.Gray,
                                        strokeWidth = 2f,
                                        alpha = 0.2f
                                    )
                                }

                            }
                            Column(Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .weight(1f),
                                    Arrangement.SpaceBetween
                                ) {
                                    Canvas(modifier = Modifier.fillMaxHeight()) {

                                        drawCircle(
                                            color = Color.Yellow,
                                            radius = radius,
                                            center = Offset(x = radius, y = radius),
//                                    style = Stroke(width = 8f)
                                        )
                                        drawArc(
                                            color = Color.LightGray,
                                            startAngle = -180f,
                                            sweepAngle = -(360f - angle),
                                            useCenter = true,
//                                    style = Stroke(35f),
                                            size = Size(radius * 2, radius * 2),
//                                    topLeft = Offset(x = 0, y = radius)
                                        )
                                        drawCircle(
                                            color = Color.Red,
                                            radius = radius,
                                            center = Offset(x = radius, y = radius),
                                            style = Stroke(width = 8f)
                                        )
                                        drawCircle(
                                            color = Color.Black,
                                            radius = radius,
                                            center = Offset(x = radius, y = radius),
                                            style = Stroke(width = 2f)
                                        )
                                        drawLine(
                                            start = Offset(x = radius, y = radius),
                                            end = Offset(x = 0f, y = radius),
                                            color = Color.Red,
                                            strokeWidth = 8f
                                        )
                                        drawLine(
                                            start = Offset(x = radius, y = radius),
                                            end = Offset(x = 0f, y = radius),
                                            color = Color.Black,
                                            strokeWidth = 2f
                                        )
                                        drawCircle(
                                            color = Color.Black,
                                            radius = 5f,
                                            center = Offset(x = radius, y = radius),
                                        )
                                        drawCircle(
                                            color = Color.Black,
                                            radius = 5f,
                                            center = Offset(x = 0f, y = radius),
                                        )
                                    }

                                    Box(modifier = Modifier.fillMaxHeight()) {
                                        val textMeasurer = rememberTextMeasurer()
                                        Canvas(modifier = Modifier.fillMaxHeight()) {
                                            val canvasHeight = size.height
                                            drawRect(
                                                color = Color.LightGray.copy(0.2f),
                                                topLeft = Offset(-210f, 0f),
                                                size = Size(200f, canvasHeight)
                                            )
                                            for (i in 0..canvasHeight.toInt()) {
                                                val y = (i * ydpi / koef / 10.toFloat()).toFloat()
                                                var offsetY = 0
                                                if (y < canvasHeight.toInt()) offsetY =
                                                    (i * ydpi / koef / 10.toFloat()).toFloat()
                                                        .toInt() else offsetY = 0
                                                drawCircle(
                                                    color = Color.Red,
                                                    radius = (ydpi / koef / 50).toFloat(),
                                                    center = Offset(
                                                        x = -8f,
                                                        y = offsetY.toFloat(),
                                                    )
                                                )
                                            }
                                            for (i in 0..canvasHeight.toInt()) {
                                                val y = i * ydpi / koef / 2.toFloat()
                                                var offsetY = 0
                                                if (y < canvasHeight.toInt()) offsetY =
                                                    (i * ydpi / koef / 2.toFloat()).toFloat()
                                                        .toInt() else offsetY = 0
                                                drawCircle(
                                                    color = Color.Black,
                                                    radius = (ydpi / koef / 20).toFloat(),
                                                    center = Offset(
                                                        x = -8f,
                                                        y = offsetY.toFloat()
                                                    ),
                                                )

                                                drawLine(
                                                    start = Offset(
                                                        x = -8f,
                                                        y = offsetY.toFloat()
                                                    ),
                                                    end = Offset(
                                                        x = -38f,
                                                        y = offsetY.toFloat()
                                                    ),
                                                    color = Color.Red,
                                                    strokeWidth = 2f
                                                )
                                            }

                                            for (i in 0..canvasHeight.toInt()) {
                                                val y = i * ydpi / koef
                                                var offsetY = 0
                                                if (y < canvasHeight.toInt()) offsetY =
                                                    (i * ydpi / koef).toFloat()
                                                        .toInt() else offsetY = 0
                                                drawLine(
                                                    start = Offset(
                                                        x = -8f,
                                                        y = offsetY.toFloat()
                                                    ),
                                                    end = Offset(
                                                        x = -48f,
                                                        y = offsetY.toFloat()
                                                    ),
                                                    color = Color.Red,
                                                    strokeWidth = 2f
                                                )
                                            }
                                            for (i in 0..(canvasHeight / ydpi * koef).toInt()) {
                                                rotate(
                                                    degrees = -90f,
                                                    Offset(
                                                        x = -84f,
                                                        y = (i * ydpi / koef.toFloat() + 10f).toFloat()
                                                    )
                                                ) {
                                                    drawText(
                                                        textMeasurer = textMeasurer,
                                                        text = "${i}",
                                                        style = TextStyle(
                                                            fontSize = 15.sp,
                                                            color = Color.Black,
//                                                    background = Color.Red.copy(alpha = 0.2f)
                                                        ),
                                                        topLeft =
                                                        Offset(
                                                            x = -88f,
                                                            y = i * ydpi / koef.toFloat() - 23f
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier
//                                    .height(100.dp)
//                                    .width(100.dp)
                                    .padding(20.dp),
                                contentAlignment = Alignment.BottomStart
                            ) {
                                Column {
                                    if (geometricFigure == "circle") {
                                        Text(
                                            text = "Radius = ${
                                                kotlin.math.round(
                                                    text * 100
                                                ) / 100
                                            }", fontSize = 14.sp
                                        )
                                        Text(
                                            text = "Circumference = ${kotlin.math.round(text * 2 * java.lang.Math.PI * 100) / 100}",
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "Area = ${kotlin.math.round(text * text * java.lang.Math.PI * 100) / 100}",
                                            fontSize = 14.sp
                                        )
                                    } else {
                                        Text(
                                            text = "Angular measure = ${angle.toInt()}°",
                                            fontSize = 14.sp
                                        )
                                    }


                                    Spacer(modifier = Modifier.height(20.dp))
                                    Column {
                                        Text(text = "Units of measurement:", fontSize = 14.sp)

                                        MultiToggleButton(
                                            unitsOfMeasurement,
                                            listOf("cm", "in"),
                                            {
                                                if (unitsOfMeasurement == "cm") {
                                                    unitsOfMeasurement = "in"; koef = 1f;
                                                    radius = ydpi / koef
                                                } else {
                                                    unitsOfMeasurement = "cm"; koef = 2.54f;
                                                    radius = ydpi / koef
                                                }
                                            },
                                            width = 40.dp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Column {
                                        Text(text = "Geometric figure:", fontSize = 14.sp)
                                        MultiToggleButton(
                                            currentSelection = geometricFigure,
                                            toggleStates = listOf("circle", "angle"),
                                            onToggleChange = {
                                                if (geometricFigure == "circle") {
                                                    geometricFigure = "angle"
                                                } else {
                                                    geometricFigure = "circle"
                                                }
                                            },
                                            width = 100.dp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Row {
                                        var isPressed = remember { mutableStateOf(false) }
                                        val scope = rememberCoroutineScope()

                                        CalcButtonComponent(
                                            Modifier
                                                .height(30.dp)
                                                .width(60.dp)
                                                .pointerInput(Unit) {
                                                    detectPressGestures(
                                                        onPressStart = {
                                                            if (radius > 0.1 && geometricFigure == "circle") radius =
                                                                radius - ydpi / koef / 100
                                                            if (geometricFigure == "angle" && angle > 0) angle--
                                                        },
                                                        onLongPress = {
                                                            isPressed.value = true
                                                            scope.launch {
                                                                try {
                                                                    // Цикл збільшення кута під час натискання
                                                                    while (isPressed.value) {
                                                                        if (geometricFigure == "angle" && angle > 0f)
                                                                            angle--  // Збільшуємо кут
                                                                        if (radius > 0.1 && geometricFigure == "circle") radius =
                                                                            radius - ydpi / koef / 100
                                                                        delay(100)  // Затримка між змінами
                                                                    }
                                                                } finally {
                                                                    isPressed.value =
                                                                        false  // Коли натискання припиняється, зупиняємо цикл
                                                                }
                                                            }

                                                        },
                                                        onLongPressEnd = {
                                                            isPressed.value = false
                                                        },
                                                        onPressEnd = {
                                                            isPressed.value = false
                                                        }
                                                    )
                                                },
                                            color = Color.LightGray,
                                            text = "-"
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))

                                        CalcButtonComponent(
                                            Modifier
                                                .height(30.dp)
                                                .width(60.dp)
                                                .pointerInput(Unit) {
                                                    detectPressGestures(
                                                        onPressStart = {
                                                            if (radius / ydpi * koef < round(screenWidth * 10) / 10 && geometricFigure == "circle")
                                                                radius =
                                                                    radius + ydpi / koef / 100.toFloat()
                                                            if (geometricFigure == "angle" && angle < 360f) angle++
                                                        },
                                                        onLongPress = {
                                                            isPressed.value = true
                                                            scope.launch {
                                                                try {
                                                                    // Цикл збільшення кута під час натискання
                                                                    while (isPressed.value) {
                                                                        if (geometricFigure == "angle" && angle < 360f)
                                                                            angle++  // Збільшуємо кут
                                                                        if (radius / ydpi * koef < round(
                                                                                screenWidth * 10
                                                                            ) / 10 && geometricFigure == "circle"
                                                                        ) radius =
                                                                            radius + ydpi / koef / 100
                                                                        delay(100)  // Затримка між змінами
                                                                    }
                                                                } finally {
                                                                    isPressed.value =
                                                                        false  // Коли натискання припиняється, зупиняємо цикл
                                                                }
                                                            }

                                                        },
                                                        onLongPressEnd = {
                                                            isPressed.value = false
                                                        },
                                                        onPressEnd = {
                                                            isPressed.value = false
                                                        }
                                                    )
                                                },
                                            color = Color.LightGray,
                                            text = "+"
                                        )
                                    }
                                }
                            }
                        }



                    }
                }
            }
        }
    }
}


