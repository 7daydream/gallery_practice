package com.example.galprac

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.provider.MediaStore
import android.util.Half.toFloat
import android.util.Size
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity() {

    val GET_GALLERY_IMAGE : Int = 200
    var REQ_GALLERY : Int = 1000
    var REQ_STORAGE_PERMISSION : Int = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val message = intent.getStringExtra(EXTRA_MESSAGE)

        val imageview : ImageView = findViewById(R.id.imageView)
        val textView : TextView = findViewById(R.id.textView)
        val uploadbutton : Button = findViewById(R.id.btn_upload)
        textView.apply {
            text = message
        }

        imageview.setOnClickListener {
            selectGallery()
        }
        uploadbutton.setOnClickListener {
            val mountainsRef = FirebaseStorage.getInstance().getReference().child("aaa")

            // Get the data from an ImageView as bytes
            imageview.isDrawingCacheEnabled = true
            imageview.buildDrawingCache()
            val bitmap = (imageview.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            var uploadTask = mountainsRef.putBytes(data)
            uploadTask.addOnFailureListener {
                // Handle unsuccessful uploads
                Toast.makeText(this, "실패", Toast.LENGTH_LONG).show()
            }.addOnSuccessListener { taskSnapshot ->
                // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                // ...
                Toast.makeText(this, "성공", Toast.LENGTH_LONG).show()
            }
        }
    }

    var startX = -1
    var startY = -1
    var stopX = -1
    var stopY = -1

    @SuppressLint("WrongCall")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> { //touch 시작, 화면에 손가락 올림
                startX = event.x.toInt()
                startY = event.y.toInt()
                // Toast.makeText(this, )
            }
            MotionEvent.ACTION_MOVE -> { //화면에서 손가락 띄었을 때
                stopX = event.x.toInt()
                stopY = event.y.toInt()
            }
            MotionEvent.ACTION_UP -> {

            }
        }
        return super.onTouchEvent(event)
    }

    private fun selectGallery() {
        val writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        if(writePermission == PackageManager.PERMISSION_DENIED || readPermission == PackageManager.PERMISSION_DENIED) {
            //권한 없어서 요청
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), REQ_STORAGE_PERMISSION)
        } else {
            //권한 있음
            var intent = Intent(Intent.ACTION_PICK)
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            intent.type = "image/*"
            startActivityForResult(intent, REQ_GALLERY)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val imageview : ImageView = findViewById(R.id.imageView)
        if(requestCode == REQ_GALLERY && resultCode == RESULT_OK && data != null)
        {
            var selectedImageUri: Uri = data.data!!
            imageview.setImageURI(selectedImageUri)
        }
    }
}