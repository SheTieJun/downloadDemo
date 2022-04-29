package me.shetj.download.demo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkInfo.State.FAILED
import androidx.work.WorkInfo.State.RUNNING
import androidx.work.WorkInfo.State.SUCCEEDED
import androidx.work.WorkManager
import java.util.*
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
            val startDownload = DownloadWorker.startDownload(
                this@MainActivity,
                downloadUrl,
                this@MainActivity.cacheDir.path,
                "wxwork_android_3.apk"
            )
            onWorkDownProcess(tv_msg, startDownload)
        }

        findViewById<View>(R.id.test_Download).setOnClickListener {
            tv_msg.text = ""
            lifecycleScope.launch {
                KCHttpV2.download(
                    downloadUrl,
                    "${this@MainActivity.cacheDir.path}/${"wxwork_android_3.apk"}",
                    onProcess = { _, _, process ->
                        Log.i("download", "process =${(process * 100).toInt()}% \n")
                        tv_msg.append("下载进度：progress =${(process * 100).toInt()}% \n")
                    },
                    onSuccess = {
                        tv_msg.append("下载成功\n")
                        Log.i("download", filesDir.path)
                    },
                    onError = {
                        it.printStackTrace()
                        tv_msg.append("下载失败\n")
                    })
            }
        }
    }

    private fun onWorkDownProcess(tv_msg: TextView, startDownload: UUID) {
        tv_msg.text = ""
        WorkManager.getInstance(applicationContext)
            .getWorkInfoByIdLiveData(startDownload)
            .observe(this) { t -> // 任务执行完毕之后，会在这里获取到返回的结果
                when (t?.state) {
                    RUNNING -> {
                        tv_msg.append("下载进度：progress =${t.progress.getInt("progress", 0)}% \n")
                    }
                    SUCCEEDED -> {
                        tv_msg.append("下载成功\n")
                    }
                    FAILED -> {
                        tv_msg.append("下载失败\n")
                    }
                }
            }
    }
}