package com.example.mycanvas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.mycanvas.ui.theme.MycanvasTheme

object State {
    var currentColor : Color = Color.Black
    var currentWidth : Float = 7.5f
    var isEraser = false
    var strokes = mutableStateListOf<BrushStroke>()
    var redoStrokes = mutableStateListOf<BrushStroke>() // where are the stacks
}
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MycanvasTheme {
                App()
            }
        }
    }
}

@Composable
fun App() {

    Scaffold { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) { // what do you mean edgetoedge is forced now?
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth()
                    .padding(8.dp)
                    .zIndex(1f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Palette {
                        selectedColor -> State.currentColor = selectedColor
                        State.isEraser = false
                    }
                    Eraser()
                    SizeSlider{
                        selectedSize -> State.currentWidth = selectedSize
                    }

                }
                Row(modifier = Modifier.fillMaxWidth()
                    .padding(8.dp)
                    .zIndex(1f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Undo()
                    Redo()
                }
                MyCanvas()
            }
        }
    }
}

@Composable
fun Palette(onColorSelected: (Color) -> Unit) {

    val palette = mapOf(
        Color.Black to "Black",
        Color.Blue to "Blue",
        Color.Green to "Green",
        Color.Yellow to "Yellow",
        Color.Red to "Red",
        Color.Gray to "Gray"
    )
    Row {
        palette.forEach { (color, name) ->
            Box(modifier = Modifier
                .size(25.dp)
                .background(color, CircleShape)
                .padding(10.dp)
                .clickable{ onColorSelected(color) }
            )
        }
    }
}
@Composable
fun Eraser() {
    Button(onClick = {State.isEraser = true} ) {
        Text("Eraser")
    }
}

@Composable
fun SizeSlider(onSelected: (Float) -> Unit) {

    var sliderPosition by remember { mutableFloatStateOf(7.5f) }
    Slider(
        value = sliderPosition,
        onValueChange = { sliderPosition = it
                        onSelected(it)},
        valueRange = 1f..32.5f
    )
}

@Composable
fun Undo() {
    Button(onClick = {
        if (State.strokes.isNotEmpty()) {
            // get last ID on list
            var id = State.strokes.last().strokeID
            do {
                State.redoStrokes.add(State.strokes.removeLast())
            } while (State.strokes.isNotEmpty() && State.strokes.last().strokeID == id)
        }
    } ) {
        Text("Undo")
    }
}
@Composable
fun Redo() {
    Button(onClick = {
        if (State.redoStrokes.isNotEmpty()) {
            var id = State.redoStrokes.first().strokeID
            do {
                State.strokes.add(State.redoStrokes.removeLast())
            } while (State.redoStrokes.isNotEmpty() && State.redoStrokes.last().strokeID == id)
        }
    } ) {
        Text("Redo")
    }
}
@Composable
fun MyCanvas() {

    var strokeNumber by remember { mutableIntStateOf(0) }
    Canvas(modifier = Modifier
        .background(Color.White)
        .fillMaxSize()
        .zIndex(0f)
        .pointerInput(true) {
            detectDragGestures (
                onDragStart = { State.redoStrokes.clear()},
                onDragEnd = { strokeNumber++ },
                onDrag = { change, dragAmount ->
                    val stroke = BrushStroke(
                        start = change.position - dragAmount,
                        end = change.position,
                        color = if (State.isEraser) Color.White else State.currentColor,
                        strokeWidth = State.currentWidth,
                        strokeID = strokeNumber
                    )
                    change.consume()
                    State.strokes.add(stroke)
                }
            )
        }
    ) {
        State.strokes.forEach { stroke ->
            drawLine(
                start = stroke.start,
                end = stroke.end,
                color = stroke.color,
                strokeWidth = stroke.strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

data class BrushStroke(
    val start: Offset,
    val end: Offset,
    val color: Color = Color.Black,
    val strokeWidth: Float = 15f,
    val strokeID: Int
)