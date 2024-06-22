package com.example.my

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.example.my.ui.theme.MyTheme

class PdfActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyTheme {
                val pdfUriString = intent.data
                if (pdfUriString != null) {
                    PdfViewer(this,pdfUriString)
                } else {
                    ErrorScreen("PDF URI not provided")
                }
            }
        }
    }
}

@Composable
fun PdfViewer(context :Context,uri: Uri) {

    val bitmaps = rememberPdfBitmaps(context, uri)
    PdfPage(bitmaps)
}

@Composable
fun rememberPdfBitmaps(context: Context, uri: Uri): List<Bitmap> {
    val bitmaps = remember { mutableListOf<Bitmap>() }
    val displayMetrics = DisplayMetrics()
    (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(displayMetrics)// Load PDF pages and generate bitmaps (can be optimized further)
    val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
    parcelFileDescriptor?.use { descriptor ->
        val renderer = PdfRenderer(descriptor)
        for (i in 0 until renderer.pageCount) {
            val page = renderer.openPage(i)
            val bitmap = createBitmap(displayMetrics.widthPixels - 20, displayMetrics.heightPixels)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmaps.add(bitmap)
            page.close()
        }
        renderer.close()
    }

    return bitmaps
}

@Composable
fun PdfPage(bitmaps: List<Bitmap>) {
    LazyColumn {
        items(bitmaps) { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "PDF Page",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            )
        }
    }
}

@Composable
fun ErrorScreen(message: String) {
    Text(text = message, modifier = Modifier.fillMaxWidth().padding(16.dp))
}

@Composable
@Preview
fun Preview() {
    MyTheme {
        ErrorScreen("Example Error")
    }
}