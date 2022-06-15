package com.example.neoastra

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.neoastra.IconPickerActivity.Companion.REQUEST_CODE_PERMISSION
import com.example.neoastra.IconPickerActivity.Companion.X
import com.example.neoastra.IconPickerActivity.Companion.Y
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {
    private lateinit var editImageImageView: ImageView
    private lateinit var picImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        editImageImageView = findViewById(R.id.editImageImageView)
        picImageView = findViewById(R.id.picImageView)
        editImageImageView.setOnClickListener { launchEditHeaderPopup() }
    }

    private fun launchEditHeaderPopup() {
        val modalBottomSheet = ImagePickerOptionBottomSheet()
        if (supportFragmentManager != null) {
            modalBottomSheet.show(supportFragmentManager, ImagePickerOptionBottomSheet.TAG)
        }
        modalBottomSheet.cameraLaunchListener {
            launchIconPicker(IconPickerActivity.LAUNCH_CAMERA)
            modalBottomSheet.dismiss()
        }
        modalBottomSheet.galleryLaunchListener {
            launchIconPicker(IconPickerActivity.LAUNCH_GALLERY)
            modalBottomSheet.dismiss()
        }


    }
    private fun launchIconPicker(launcher: Int = 0) {

        val intent = Intent(this, IconPickerActivity::class.java)
        intent.putExtra(IconPickerActivity.EXTRA_LAUNCH_DIRECT, launcher)
        intent.putExtra(X, editImageImageView.x.toInt())
        intent.putExtra(Y, editImageImageView.y.toInt())
        startActivityForResult(intent, REQUEST_CODE_PERMISSION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PERMISSION && resultCode == Activity.RESULT_OK) {
            handlePicChanges(data)
        }

    }

    private fun handlePicChanges(data: Intent?) {
        val filepath = data?.getStringExtra(IconPickerActivity.CHOOSEN_FILE)
        val file = Uri.fromFile(filepath?.let { File(it) })

        AlertDialog.Builder(this)
            .setTitle("New picture")
            .setMessage("Do you want to,save your picture ?")
            .setPositiveButton( "OK") { _: DialogInterface, i: Int ->
                saveFile(filepath)
            }
            .setNegativeButton( "Cancel") { _: DialogInterface,i: Int ->}
            .create()
            .show()

        Glide.with(this).load(file).into(picImageView)
    }

    private fun saveFile(filepath: String?) {
        val myBitmap = BitmapFactory.decodeFile(filepath)

        val outStream: FileOutputStream?
        val path: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val dir = File(path!!.absolutePath )
        dir.mkdirs()
        val fileName = String.format("%d.jpg", System.currentTimeMillis())
        val outFile = File(dir, fileName)
        outStream = FileOutputStream(outFile)
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
        outStream.flush()
        outStream.close()
        Timber.d("saveFile $fileName")
    }
}