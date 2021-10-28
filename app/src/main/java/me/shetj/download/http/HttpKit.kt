package me.shetj.download.http

import retrofit2.Retrofit
import java.io.File


typealias download_error = suspend (Throwable) -> Unit
typealias download_process = suspend (downloadedSize: Long, length: Long, progress: Float) -> Unit
typealias download_success = suspend (uri: File) -> Unit


/**
 * @author stj
 * @Date 2021/10/28-16:41
 * @Email 375105540@qq.com
 */

object HttpKit {
    val retrofit = Retrofit.Builder()
        .baseUrl("http://www.xxx.com")
        .validateEagerly(true) //在开始的时候直接开始检测所有的方法
        .build()

    val apiService = retrofit.create(ApiService::class.java)

}