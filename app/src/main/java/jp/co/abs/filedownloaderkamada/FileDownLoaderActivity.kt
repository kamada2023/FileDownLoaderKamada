package jp.co.abs.filedownloaderkamada

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.HandlerCompat
import jp.co.abs.filedownloaderkamada.databinding.ActivityFileDownLoaderBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.Executors

open class FileDownLoaderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFileDownLoaderBinding

    //パス文字列
    private val downloadPath:String = Environment.getExternalStorageDirectory().path+"/Kamada_Picture"
    private var fileName = ""
    private var success = "ダウンロードが完了しました"
    private var failure = "画像取得に失敗しました"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileDownLoaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val downloadPathFile = File(downloadPath)
        if (!downloadPathFile.exists()) {
            AlertDialog.Builder(this) // FragmentではActivityを取得して生成
                .setTitle("ストレージへの")
                .setMessage("アクセス許可")
                .setPositiveButton("許可する") { _, _ ->

                    try {
                        // 保存先のディレクトリが無ければ作成する
                        downloadPathFile.mkdir()
                        Log.d("Log","new_path:$downloadPathFile")
                    } catch (error: SecurityException) {
                        // ファイルに書き込み用のパーミッションが無い場合など
                        error.printStackTrace()
                    } catch (error: IOException) {
                        // 何らかの原因で誤ってディレクトリを2回作成してしまった場合など
                        error.printStackTrace()
                    } catch (error: Exception) {
                        error.printStackTrace()
                    }
                }
                .setNegativeButton("しない") { _, _ -> finish() }
                .show()
        }

        //ダウンロード開始ボタン
        binding.downloadStart.setOnClickListener {
            val stringUrl: String = binding.urlEditText.text.toString()
            if (stringUrl.isNotEmpty()){
                downloadImage(stringUrl)
            }else{
                Toast.makeText(applicationContext, failure, Toast.LENGTH_SHORT).show()
            }
        }
        //GALLERY
        binding.gallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            //ギャラリーへ遷移
            receivePicture.launch(intent)
        }
        //clearボタン
        binding.clear.setOnClickListener {
            binding.imageView.setImageDrawable(null)
            binding.urlEditText.editableText.clear()
        }
        //ダウンロードした画像ボタン
        binding.downloadStill.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            receivePicture.launch(intent)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun downloadImage(urlSt: String) {
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

                //画像をダウンロード
                val ism = urlCon.inputStream
                val bmp = BitmapFactory.decodeStream(ism)

                val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
                val current = sdf.format(Date())

                // ファイルを作成
                fileName = "$current.png"
                val file = File(downloadPath, fileName)

                // ファイルに書き込み
                FileOutputStream(file).use { stream ->
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }

                // 別スレッド内での処理を管理し実行する
                HandlerCompat.createAsync(mainLooper).post { // Mainスレッドに渡す
                    binding.imageView.setImageBitmap(bmp)
                    Toast.makeText(applicationContext, success, Toast.LENGTH_SHORT).show()
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }catch (e: MalformedURLException){
                e.printStackTrace()
            }

        }
    }
    //ギャラリーから画像受け取り
    private val receivePicture =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                // 画像を表示（ギャラリーintentから取得）
                binding.imageView.setImageURI(it.data?.data)
                Toast.makeText(applicationContext, "画像を取得しました", Toast.LENGTH_SHORT).show()
            }
        }
}