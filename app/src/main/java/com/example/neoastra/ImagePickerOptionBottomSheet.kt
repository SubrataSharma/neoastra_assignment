package com.example.neoastra

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ImagePickerOptionBottomSheet(
) : BottomSheetDialogFragment(), View.OnClickListener {
    private lateinit var camera_view: ImageView
    private lateinit var gallery_view: ImageView
    private var launchCamera: (() -> Unit)? = null
    private var launchGallery: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.layout_image_picker_option, container, false)

        camera_view = view.findViewById(R.id.camera_view)
        gallery_view = view.findViewById(R.id.gallery_view)


        camera_view.setOnClickListener(this)
        gallery_view.setOnClickListener(this)

        setView()
        return view
    }

    private fun setView() {


    }




    companion object {
        const val TAG = "ModalBottomSheet"
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.camera_view -> {
                launchCamera?.invoke()
            }
            R.id.gallery_view -> {
                launchGallery?.invoke()
            }

        }
    }

    fun cameraLaunchListener(cameraLaunch: () -> Unit) {
        this.launchCamera = cameraLaunch
    }

    fun galleryLaunchListener(galleryLaunch: () -> Unit) {
        this.launchGallery = galleryLaunch
    }

}