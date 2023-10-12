package jp.co.abs.filedownloaderkamada

import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.HandlerCompat
import jp.co.abs.filedownloaderkamada.databinding.ActivityFileDownLoaderBinding
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors


class FileDownLoaderActivity : AppCompatActivity() {

    private lateinit var binding : ActivityFileDownLoaderBinding
    //パス文字列
    private var downloadDir =
        File(Environment.getExternalStorageDirectory().path, "/Kama_Picture/")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileDownLoaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!downloadDir.exists()){
            AlertDialog.Builder(this) // FragmentではActivityを取得して生成
                .setTitle("タイトル")
                .setMessage("メッセージ")
                .setPositiveButton("OK") { _, _ ->}
                .setNegativeButton("No") { _, _ -> finish() }
                .show()
        }

        binding.downloadStart.setOnClickListener {
            val stringUrl: String = binding.urlEditText.text.toString()
            downloadImage(stringUrl)
        }
        //GALLERY
        binding.gallery.setOnClickListener {
            openDocument.launch(arrayOf("image/*"))
        }
        //clearボタン
        binding.clear.setOnClickListener {
            binding.imageView.setImageDrawable(null)
            binding.urlEditText.editableText.clear()
        }
    }

    private fun downloadImage(urlSt:String){
        // Singleの別スレッドを立ち上げる
        Executors.newSingleThreadExecutor().execute {
            try {
                val url = URL(urlSt)
                val urlCon = url.openConnection() as HttpURLConnection

                // タイムアウト設定
                urlCon.readTimeout = 10000
                urlCon.connectTimeout = 20000

                // リクエストメソッド
                urlCon.requestMethod = "GET"

                // リダイレクトを自動で許可しない設定
                urlCon.instanceFollowRedirects = false
                val ism = urlCon.inputStream
                val bmp = BitmapFactory.decodeStream(ism)

                // 別スレッド内での処理を管理し実行する
                HandlerCompat.createAsync(mainLooper).post { // Mainスレッドに渡す
                    binding.imageView.setImageBitmap(bmp)
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        try {
            // 保存先のディレクトリが無ければ作成する
            if (!downloadDir.exists()) {
                downloadDir.mkdir()
            }
        } catch (error : SecurityException) {
            // ファイルに書き込み用のパーミッションが無い場合など
            error.printStackTrace()
        } catch (error: IOException) {
            // 何らかの原因で誤ってディレクトリを2回作成してしまった場合など
            error.printStackTrace()
        } catch (error: Exception) {
            error.printStackTrace()
        }

        val fileName = "sample.jpg"
        val absoluteFilePath = downloadDir.absolutePath + "/" + fileName
        val outputFile = File(absoluteFilePath,"sample.jpg")

        val context:Context = applicationContext

        val file = File()
    }

    private val openDocument = registerForActivityResult(ActivityResultContracts.OpenDocument()){
        binding.imageView.setImageURI(it)
    }
}