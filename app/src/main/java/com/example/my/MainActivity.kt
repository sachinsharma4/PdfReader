package com.example.my

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.my.ui.theme.MyTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyTheme {
                ChapterListScreen()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChapterListScreen() {val context = LocalContext.current
    Column(Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(10.dp))
        for (chapter in 101..113) {
            ChapterCard(context, chapter, "Introduction to Numbers")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChapterCard(context: Context, chapterNumber: Int, chapterTitle: String) {
    var downloadId by remember { mutableStateOf<Long?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctxt: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (downloadId == id) {
                    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val uri: Uri? = downloadManager.getUriForDownloadedFile(id)
                    uri?.let {
                        val pdfIntent = Intent(context, PdfActivity::class.java)
                        pdfIntent.setData(it)
                        context.startActivity(pdfIntent)
                    }
                }
            }
        }

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)

        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                context.unregisterReceiver(receiver)
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            context.unregisterReceiver(receiver)
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }

    Row(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(8.dp))
            .fillMaxWidth()
            .background(Color.White)
            .clickable {
                downloadId = downloadChapter(context, chapterNumber)
            }
            .padding(8.dp)
    ) {
        Image(
            painter = painterResource(id = R.mipmap.images),
            contentDescription = "PDF Icon",modifier = Modifier
                .clip(CircleShape)
                .size(30.dp)
                .align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.padding(8.dp))
        Text(
            text = "Chapter $chapterNumber: $chapterTitle",
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
    Spacer(modifier = Modifier.height(10.dp))
}

fun downloadChapter(context: Context, chapterNumber: Int): Long {
    val fileName = "jemh${chapterNumber}.pdf"
    val downloadUrl = "https://ncert.nic.in/textbook/pdf/$fileName"

    Toast.makeText(context, "Downloading Chapter $chapterNumber", Toast.LENGTH_SHORT).show()

    val request = DownloadManager.Request(Uri.parse(downloadUrl))
        .setTitle(fileName)
        .setDescription("Downloading")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
        .setAllowedOverMetered(true)
        .setAllowedOverRoaming(false)
        .setMimeType("application/pdf")

    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    return downloadManager.enqueue(request)
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyTheme {
        ChapterListScreen()
    }
}