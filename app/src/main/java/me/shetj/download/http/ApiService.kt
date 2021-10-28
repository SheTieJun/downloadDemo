package me.shetj.download.http

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface ApiService {
 	@Streaming
 	@GET
 	suspend fun downloadFile(@Url fileUrl: String?): ResponseBody
 }