package com.ceslab.firemesh.presentation.scan

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.ceslab.firemesh.R
import com.ceslab.firemesh.presentation.base.BaseFragment
import com.ceslab.firemesh.presentation.base.BaseRecyclerViewAdapter
import com.ceslab.firemesh.presentation.dialogs.ProvisionBottomDialog
import kotlinx.android.synthetic.main.fragment_scan.*
import timber.log.Timber

class ScanFragment : BaseFragment(){
    companion object {
        const val TAG = "ScanFragment"
    }

    private lateinit var scannerRecyclerViewAdapter: ScanRecyclerViewAdapter

    override fun getResLayoutId(): Int {
        return R.layout.fragment_scan
    }

    override fun onMyViewCreated(view: View) {
        Timber.d("onMyViewCreated")
        setupRecyclerView()
    }

    private fun setupRecyclerView(){
        Timber.d("setupRecyclerView")
        val linearLayoutManager = LinearLayoutManager(view!!.context)
        scannerRecyclerViewAdapter = ScanRecyclerViewAdapter(view!!.context)
        scannerRecyclerViewAdapter.itemClickListener = onProvisionButtonClickedListener
        rv_scan.layoutManager = linearLayoutManager
        rv_scan.setHasFixedSize(true)
        rv_scan.adapter = scannerRecyclerViewAdapter

        scannerRecyclerViewAdapter.setDataList(mutableListOf("A","B","C","D"))
    }

    private val onProvisionButtonClickedListener = object : BaseRecyclerViewAdapter.ItemClickListener<String> {
        override fun onClick(position: Int, item: String) {
            Timber.d("onProvisionButtonClickedListener: clicked")
            val provisionBottomDialog = ProvisionBottomDialog()
            provisionBottomDialog.show(fragmentManager!!,"ProvisionBottomDialog")
        }
    }
}