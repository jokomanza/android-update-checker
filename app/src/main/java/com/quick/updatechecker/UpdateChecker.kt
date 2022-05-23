package com.quick.updatechecker

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quick.updatechecker.data.AppVersionDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.*
import okio.IOException


class UpdateChecker(val ctx: Context, val baseUrl: String?) : IUpdateChecker {

    private constructor(builder: Builder) : this(builder.ctx, builder.baseUrl)

    companion object {
        private var isShown = false

        inline fun build(ctx: Context, block: Builder.() -> Unit = { }) =
            Builder(ctx).apply(block).build()
    }

    init {

    }

    data class Builder(var ctx: Context, var baseUrl: String? = null) {

        fun setContext(ctx: Context) = this.apply {
            this.ctx = ctx
        }

        fun setBaseUrl(url: String) = this.apply {
            baseUrl = url
        }

        fun build() = UpdateChecker(this)

    }

    var TAG = "UpdateChecker"
    var BASE_URL = baseUrl ?: "http://produksi.quick.com/quick-appstore-web/public/index.php/api/"

    private var httpClient = OkHttpClient()

    override fun checkUpdate() {
        CoroutineScope(Dispatchers.IO).launch {
            val versionCode = ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionCode
            val request = Request.Builder()
                .url("${BASE_URL}app/${ctx.packageName}/version/${versionCode}/library/update")
                .build()

            httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!it.isSuccessful) {
                            throw IOException("Unexpected code $response")
                        }

                        val body = it.body?.string() ?: ""

                        Log.d(TAG, "onResponse: ${body}")

                        val json = Json {
                            ignoreUnknownKeys = true
                        }

                        var result: AppVersionDto? = null

                        try {
                            result =
                                json.decodeFromString<AppVersionDto>(body)
                        } catch (e:Exception) {
                            CoroutineScope(Dispatchers.Main).launch {
                                MaterialAlertDialogBuilder(ctx).setTitle("Error")
                                    .setMessage("Error : ${e.message}\n Body :  $body")
                                    .show()
                            }
                        }

                        CoroutineScope(Dispatchers.Main).launch {
                            if (result == null) return@launch

                            if (!isShown) {
                                isShown = true
                                val dialog = MaterialAlertDialogBuilder(ctx)
                                    .setTitle("New Update")
                                    .setCancelable(false)

                                if (result.mandatory == false) {
                                    dialog.setMessage("This app has new update, please update it now")
                                    dialog.setNegativeButton("I'll do it later") { _, _ ->
                                        isShown =  false
                                    }
                                } else {
                                    dialog.setMessage("This application gets a new update, you must update this application first to use it")
                                }

                                dialog.setPositiveButton("Update now") { _, _ ->
                                    isShown = false
                                    val intent =
                                        ctx.packageManager?.getLaunchIntentForPackage("com.quick.quickappstore")
                                            ?.apply {
                                                putExtra("go_to_manage_apps", true)
                                            }
                                    if (intent == null) {
                                        MaterialAlertDialogBuilder(ctx)
                                            .setTitle("Quick App Store not found")
                                            .setMessage("Looks like you haven't installed the quick app store application, please open a web browser to download and install it")
                                            .setPositiveButton("Open Browser") { _, _ ->
                                                ctx.startActivity(
                                                    Intent(
                                                        Intent.ACTION_VIEW,
                                                        Uri.parse("${BASE_URL}client/download")
                                                    )
                                                )
                                            }
                                            .show()
                                    } else ctx.startActivity(intent)
                                }

                                dialog.show()

                            }

                        }


                        Log.d(TAG, "onResponse: $body")
                    }
                }
            })
        }
    }
}