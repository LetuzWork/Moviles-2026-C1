package com.menusemana.core.common

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

object PhotoStorage {

    fun createPhotoUri(context: Context): Uri {
        val dir = File(context.filesDir, "meals")
        dir.mkdirs()
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun deletePhoto(context: Context, uriString: String) {
        runCatching {
            val uri = Uri.parse(uriString)
            context.contentResolver.delete(uri, null, null)
        }
    }
}
