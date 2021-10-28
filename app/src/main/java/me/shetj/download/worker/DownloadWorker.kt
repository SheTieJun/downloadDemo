package me.shetj.download.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import me.shetj.download.demo.R
import me.shetj.download.http.KCHttpV2
import java.util.*


/**
 * 测试下载
 */
class DownloadWorker(context: Context, parameters: WorkerParameters) :
        CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        val inputUrl = inputData.getString(KEY_INPUT_URL)
                ?: return Result.failure()
        val outputUrl = inputData.getString(KEY_OUT_PUT_URL)
                ?: return Result.failure()
        val filename = inputData.getString(KEY_OUTPUT_FILE_NAME)
                ?: return Result.failure()
        val progress = "Starting Download"
        setForeground(createForegroundInfo(progress))
        download(inputUrl, outputUrl, filename)
        return Result.success()
    }

    private suspend fun download(downloadUrl: String, outputFile: String, fileName: String) {
        KCHttpV2.download(downloadUrl, "$outputFile/$fileName", onProcess = { _, _, process ->
            setForeground(createForegroundInfo("${(process * 100).toInt()}%"))
            setProgress(Data.Builder().let {
                it.putInt("progress", (process * 100).toInt())
                it.build()
            })
        }, onSuccess = {
            setForeground(createForegroundInfo("download ok"))
        },onError = {
            it.printStackTrace()
        })
    }
    private fun createForegroundInfo(progress: String): ForegroundInfo {
        val intent = WorkManager.getInstance(applicationContext)
                .createCancelPendingIntent(id)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        val notification = NotificationCompat.Builder(applicationContext, getChannelID())
                .setContentTitle(getTitle())
                .setTicker(getTitle())
                .setContentText(progress)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true) //防止滑动删除
                .addAction(R.mipmap.picture_icon_delete_photo , "取消", intent)
                .build()

        return ForegroundInfo("下载文件".hashCode(), notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        if (NotificationManagerCompat.from(applicationContext).getNotificationChannel(
                        getChannelID()
                ) == null) {
            val name = "文件下载"
            val description = "文件下载"
            val importance = NotificationManager.IMPORTANCE_LOW
            val mChannel = NotificationChannel(getChannelID(), name, importance)
            mChannel.description = description
            mChannel.enableLights(true)
            mChannel.lightColor = Color.RED
            mChannel.enableVibration(true)
            return NotificationManagerCompat.from(applicationContext).createNotificationChannel(mChannel)
        }
    }

    private fun getTitle(): String {
        return "文件下载"
    }

    private fun getChannelID(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            "文件下载"
        } else {
            ""
        }
    }

    companion object {
        const val KEY_INPUT_URL = "KEY_INPUT_URL"
        const val KEY_OUT_PUT_URL = "KEY_OUT_URL"
        const val KEY_OUTPUT_FILE_NAME = "KEY_OUTPUT_FILE_NAME"

        /*** WorkManager.getInstance(applicationContext)
         *       .getWorkInfoByIdLiveData(checkDisk.id)
         *       .observe(this, object : Observer<WorkInfo> {
         *            override fun onChanged(t: WorkInfo?) {
         *              // 任务执行完毕之后，会在这里获取到返回的结果
         *              if(t?.state == WorkInfo.State.RUNNING) {
         *                  Log.d("TEST", "Work progress --- ${t.progress.getInt("progress", 0)}")
         *              } else if(t?.state == WorkInfo.State.SUCCEEDED){
         *                  Toast.makeText(this@MainActivity, "Check disk success", Toast.LENGTH_LONG).show()
         *              } else if(t?.state == WorkInfo.State.FAILED){
         *                  Toast.makeText(this@MainActivity, "Check disk failed", Toast.LENGTH_LONG).show()
         *              }
         *          }
         *})
         */
        fun startDownload(context: Context, downloadUrl: String, outputFile: String, fileName: String): UUID {
            val inputData: Data = Data.Builder().apply {
                putString(KEY_INPUT_URL, downloadUrl)
                putString(KEY_OUTPUT_FILE_NAME, fileName)
                putString(KEY_OUT_PUT_URL, outputFile)
            }.build()
            val request = OneTimeWorkRequestBuilder<DownloadWorker>().setInputData(inputData).build()
            WorkManager.getInstance(context).enqueue(request)
            return request.id
        }
    }
}