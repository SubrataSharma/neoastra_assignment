package com.example.neoastra

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.yalantis.ucrop.UCrop
import timber.log.Timber
import java.io.File

class IconPickerActivity : AppCompatActivity(){
    private var launcher = 0
    private var isCapturedFromCamera = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_icon_picker)

        launcher = intent.getIntExtra(EXTRA_LAUNCH_DIRECT, 0)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                REQUEST_CODE_PERMISSION
            )
        }
        if (launcher == LAUNCH_CAMERA) {
            launchCameraPicker()
        } else if (launcher == LAUNCH_GALLERY) {
            browseFromGallery()
        }
    }


    fun requestPermission(permission: String, requestCode: Int) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            showRationale(permission)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        }
    }

    private fun showRationale(permission: String) {
        AlertDialog.Builder(this, com.google.android.material.R.style.MaterialAlertDialog_Material3)
            .setTitle(getString(R.string.permission_required)).setMessage(
                getString(
                    R.string.permission_is_required_to_perform_this_action,
                    permission
                )
            )
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(getString(R.string.launch_app_settings)) { dialog, which -> showAppPermissionsScreen() }
            .create().show()
    }

    protected fun showAppPermissionsScreen() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun launchCameraPicker() {
        isCapturedFromCamera = true
        dispatchTakePictureIntent()
    }

    private fun browseFromGallery() {
        val intent = Intent()
        intent.putExtra("crop", true)
        intent.putExtra("aspectX", 1)
        intent.putExtra("aspectY", 1)
        intent.putExtra("outputX", OUTPUT_X)
        intent.putExtra("outputY", OUTPUT_Y)
        intent.putExtra("return-data", false)
        intent.action = Intent.ACTION_PICK
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (launcher != 0 && resultCode == RESULT_CANCELED) {
            setResult(RESULT_CANCELED)
            finish()
        } else if (requestCode == PICK_IMAGE && data != null && data.data != null) {
            val _uri = data.data
            performCrop(_uri!!)
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val _uri = Uri.fromFile(File(externalCacheDir, FILE_CAPTURED_PIC))
            performCrop(_uri)
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            val output: Uri? = data?.let { UCrop.getOutput(it) }
            if (output != null) {
                finish(output.path, false)
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError: Throwable? = data?.let { UCrop.getError(it) }
            Timber.d(cropError)
        } else if (requestCode == PIC_CROP && (data == null || !data.hasExtra("data"))) {
            Thread.dumpStack()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun finish(imageFilePath: String?, isIcon: Boolean) {
        if (imageFilePath != null) {
            val resultData = Intent()
            resultData.putExtra(CHOOSEN_FILE, imageFilePath)
            resultData.putExtra(IS_ICON, isIcon)
            resultData.putExtra(IS_CAPTURED_FROM_CAM, isCapturedFromCamera)
            setResult(RESULT_OK, resultData)
            finish()
        }
    }


    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, setImageUri())
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun setImageUri(): Uri? {

        return FileProvider.getUriForFile(
            this, "$packageName.fileProvider", File(
                externalCacheDir, FILE_CAPTURED_PIC
            )
        )
    }


    private fun performCrop(imageFilePath: Uri) {
        // take care of exceptions
        try {
            val uri = Uri.fromFile(File(externalCacheDir, "cropped_" + System.currentTimeMillis()))
            val options: UCrop.Options = UCrop.Options()
            UCrop.of(imageFilePath, uri)
                .withAspectRatio(1F, 1F)
                .withMaxResultSize(320, 320)
                .withOptions(options)
                .start(this)
            //            new Crop(imageFilePath).output(uri).withAspect(1,1).asSquare().start(this);
        } catch (anfe: ActivityNotFoundException) {
            Timber.e(anfe)
            finish(imageFilePath.path, false)
        }
    }

    companion object {
        const val REQUEST_CODE_PERMISSION = 13
        const val EXTRA_LAUNCH_DIRECT = "launch_immediately"
        const val LAUNCH_GALLERY = 1
        const val LAUNCH_CAMERA = 2
        const val LAUNCH_ICON_ONLY = 3
        private const val PICK_IMAGE = 100
        const val CHOOSEN_FILE = "choosen_file"
        const val IS_CAPTURED_FROM_CAM = "is_captured_from_camera"
        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_CODE_PICK_ICON = 10

        private const val PIC_CROP = 101
        const val OUTPUT_X = 300
        const val OUTPUT_Y = 300
        const val X = "X"
        const val Y = "Y"
        const val IS_ICON = "is_icon"
        const val FILE_CAPTURED_PIC = "captured_pic"
    }
}