package jp.co.abs.filedownloaderkamada

import android.R.string.cancel
import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLConnection
import kotlin.math.floor


open class AsyncFileDownload : AppCompatActivity() {
    fun AsyncFileDownload(activity: Activity?, url: String?, oFile: File?) {
        owner = activity
        urlString = url
        outputFile = oFile
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    private val aTAG = "AsyncFileDownload"

    var owner: Activity? = null
    private val BUFFER_SIZE = 1024

    private var urlString: String? = null
    private var outputFile: File? = null
    private var fileOutputStream: FileOutputStream? = null
    private var inputStream: InputStream? = null
    private var bufferedInputStream: BufferedInputStream? = null

    private var totalByte = 0
    private var currentByte = 0

    private val buffer = ByteArray(BUFFER_SIZE)

    private var url: URL? = null
    private var urlConnection: URLConnection? = null

    protected fun doInBackground(vararg url: String?): Boolean? {
        try {
            connect()
        } catch (e: IOException) {
            Log.d(aTAG, "ConnectError:$e")
        }
        if (bufferedInputStream != null) {
            try {
                var len: Int
                while (bufferedInputStream!!.read(buffer).also { len = it } != -1) {
                    fileOutputStream!!.write(buffer, 0, len)
                    currentByte += len
                    //publishProgress();
                }
            } catch (e: IOException) {
                Log.d(aTAG, e.toString())
                return false
            }
        } else {
            Log.d(aTAG, "bufferedInputStream == null")
        }
        try {
            close()
        } catch (e: IOException) {
            Log.d(aTAG, "CloseError:$e")
        }
        return true
    }

    protected fun onPreExecute() {}

    protected fun onPostExecute(result: Boolean?) {}

    protected fun onProgressUpdate(vararg progress: Void?) {}

    @Throws(IOException::class)
    private fun connect() {
        url = URL(urlString)
        urlConnection = url!!.openConnection()
        this.urlConnection.readTimeout = 5000
        urlConnection.readTimeout = 30000
        inputStream = urlConnection.getInputStream()
        bufferedInputStream = BufferedInputStream(inputStream, BUFFER_SIZE)
        fileOutputStream = FileOutputStream(outputFile)
        totalByte = urlConnection.getContentLength()
        currentByte = 0
    }

    @Throws(IOException::class)
    private fun close() {
        fileOutputStream!!.flush()
        fileOutputStream!!.close()
        bufferedInputStream!!.close()
    }

    fun getLoadedBytePercent(): Int {
        return if (totalByte <= 0) {
            0
        } else floor((100 * currentByte / totalByte).toDouble()).toInt()
    }
}