package ru.pl.criminalintent.details

import android.app.AlertDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.view.doOnLayout
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import ru.pl.criminalintent.R
import ru.pl.criminalintent.utils.getScaledBitmap

class PhotoDialogFragment : DialogFragment() {

    private val args: PhotoDialogFragmentArgs by navArgs()


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val photoPath = args.photoPath

        val preferWidth = getCurrentScreenDimension().first / 2
        val preferHeight = preferWidth * 2
        Log.d("oncreatedialogtest", preferWidth.toString())
        Log.d("oncreatedialogtest", preferHeight.toString())

        val scaledBitmap = getScaledBitmap(
            photoPath,
            preferWidth,
            preferHeight
        )

        val imageView =
            layoutInflater.inflate(R.layout.fragment_dialog_photo_zoom, null) as ImageView
        imageView.setImageBitmap(scaledBitmap)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP


        return AlertDialog.Builder(requireContext())
            .setView(imageView)
            .setNegativeButton("close", null)
            .show()
    }


    @Suppress("DEPRECATION")
    private fun getCurrentScreenDimension(): Pair<Int, Int> {
        var width: Int
        var height: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = requireActivity().windowManager.currentWindowMetrics
            width = windowMetrics.bounds.width()
            height = windowMetrics.bounds.height()

        } else {
            val displayMetrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
            width = displayMetrics.widthPixels
            height = displayMetrics.heightPixels

        }
        return width to height
    }


}