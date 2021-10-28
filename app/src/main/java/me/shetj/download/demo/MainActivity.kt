package me.shetj.download.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.work.Data
import kotlinx.coroutines.launch
import me.shetj.download.http.KCHttpV2
import me.shetj.download.worker.DownloadWorker

class MainActivity : AppCompatActivity() {


    private val downloadUrl: String =
        "https://dldir1.qq.com/wework/work_weixin/wxwork_android_3.0.31.13637_100001.apk"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tv_msg = findViewById<TextView>(R.id.tv_msg)

        findViewById<View>(R.id.test_DownloadWorker).setOnClickListener {
            DownloadWorker.startDownload(
                this@MainActivity,
                downloadUrl,
                this@MainActivity.cacheDir.path,
                "wxwork_android_3.apk"
            )
        }

        findViewById<View>(R.id.test_Download).setOnClickListener {
            tv_msg.text = ""
            lifecycleScope.launch{
                KCHttpV2.download(downloadUrl, "${this@MainActivity.cacheDir.path}/${"wxwork_android_3.apk"}", onProcess = { _, _, process ->
                    Log.i("download","process =${(process * 100).toInt()}% \n")
                    tv_msg.append("process =${(process * 100).toInt()}% \n")
                }, onSuccess = {
                   Log.i("download",filesDir.path)
                },onError = {
                    it.printStackTrace()
                })
            }
        }
    }
}