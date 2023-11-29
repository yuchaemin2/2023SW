package com.example.a2023sw.view.activity

import android.content.Context
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.MediaScannerConnectionClient
import android.net.Uri
import android.util.Log


class MediaScanner private constructor(private val mContext: Context) {
    private var file_Path: String? = null
    private var mMediaScanner: MediaScannerConnection? = null
    private var mMediaScannerClient: MediaScannerConnectionClient? = null
    fun mediaScanning(path: String?) {
        if (mMediaScanner == null) {
            mMediaScannerClient = object : MediaScannerConnectionClient {
                override fun onMediaScannerConnected() {
                    mMediaScanner!!.scanFile(file_Path, null)
                }

                override fun onScanCompleted(path: String, uri: Uri) {
                    Log.d("TastyLog", "::::MediaScan Success::::")
                    mMediaScanner!!.disconnect()
                }
            }
            mMediaScanner = MediaScannerConnection(mContext, mMediaScannerClient)
        }
        file_Path = path
        mMediaScanner!!.connect()
    }

    companion object {
        fun newInstance(context: Context): MediaScanner {
            return MediaScanner(context)
        }
    }
}