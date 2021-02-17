package com.ceslab.firemesh.presentation.ota_list.dialog.ota_process_dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.ceslab.firemesh.R

class OTAProcessDialog: DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_ota_process,container,false)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Make dialog concern rounded
        return view
    }
}