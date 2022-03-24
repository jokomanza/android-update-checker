package com.quick.updatechecker.data


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppVersionDto(
    @SerialName("id")
    val id: Int,
    @SerialName("app_id")
    val appId: Int,
    @SerialName("version_code")
    val versionCode: Int,
    @SerialName("version_name")
    val versionName: String,
    @SerialName("min_sdk_level")
    val minSdkLevel: Int,
    @SerialName("target_sdk_level")
    val targetSdkLevel: Int,
    @SerialName("apk_file_url")
    val apkFileUrl: String,
    @SerialName("apk_file_size")
    val apkFileSize: String,
    @SerialName("icon_url")
    val iconUrl: String,
    @SerialName("description")
    val description: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    val mandatory: Boolean? = false
)