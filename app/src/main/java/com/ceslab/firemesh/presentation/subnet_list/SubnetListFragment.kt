package com.ceslab.firemesh.presentation.subnet_list

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ceslab.firemesh.R
import com.ceslab.firemesh.presentation.base.BaseFragment
import com.ceslab.firemesh.presentation.base.BaseRecyclerViewAdapter
import com.ceslab.firemesh.presentation.main.activity.MainActivity
import com.ceslab.firemesh.presentation.ota_list.OTAListFragment
import com.ceslab.firemesh.presentation.subnet.SubnetFragment
import com.ceslab.firemesh.presentation.subnet_list.dialog.add_subnet.AddSubnetClickListener
import com.ceslab.firemesh.presentation.subnet_list.dialog.add_subnet.AddSubnetDialog
import com.ceslab.firemesh.presentation.subnet_list.dialog.edit_subnet.EditSubnetCallback
import com.ceslab.firemesh.presentation.subnet_list.dialog.edit_subnet.EditSubnetDialog
import com.ceslab.firemesh.service.FireMeshService
import com.siliconlab.bluetoothmesh.adk.data_model.subnet.Subnet
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_node_config.*
import kotlinx.android.synthetic.main.fragment_subnet_list.*
import timber.log.Timber

class SubnetListFragment : BaseFragment() {
    companion object {
        const val TAG = "SubnetListFragment"
        private const val WRITE_EXTERNAL_STORAGE_REQUEST_PERMISSION = 300

    }

    private lateinit var subnetListRecyclerViewAdapter: SubnetListRecyclerViewAdapter
    private lateinit var subnetListViewModel: SubnetListViewModel

    override fun getResLayoutId(): Int {
        return R.layout.fragment_subnet_list
    }


    override fun onMyViewCreated(view: View) {
        Timber.d("onMyViewCreated")
        setupViewModel()
        setupViews()
    }

    override fun onResume() {
        super.onResume()
        Timber.d("onResume")
    }

    private fun setupViews() {
        setupRecyclerView()
        setupAddGroupFab()
        setupOTAButton()
        setupBackgroundScanSwitch()
    }


    private fun setupRecyclerView() {
        Timber.d("setupRecyclerView")
        val linearLayoutManager = LinearLayoutManager(view!!.context)
        subnetListRecyclerViewAdapter = SubnetListRecyclerViewAdapter(view!!.context)
        subnetListRecyclerViewAdapter.itemClickListener = onSubnetItemClickedListener
        rv_subnet_list.apply {
            layoutManager = linearLayoutManager
            setHasFixedSize(true)
            adapter = subnetListRecyclerViewAdapter
        }

    }

    private fun setupViewModel() {
        Timber.d("setupViewModel")
        AndroidSupportInjection.inject(this)
        subnetListViewModel =
            ViewModelProvider(this, viewModelFactory).get(SubnetListViewModel::class.java)
        subnetListViewModel.apply {
            getSubnetList().observe(this@SubnetListFragment, subnetListObserver)
        }

    }

    private fun setupAddGroupFab() {
        Timber.d("setupAddGroupFab")
        fab_add_subnet.setOnClickListener {
            Timber.d("onAddGroupClick")
            val addSubnetDialog = AddSubnetDialog()
            addSubnetDialog.show(fragmentManager!!, "AddSubnetDialog")
            addSubnetDialog.setAddSubnetClickListener(onAddSubnetClickListener)
        }
    }

    private fun setupOTAButton() {
        btn_ota_start.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    context!!,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    WRITE_EXTERNAL_STORAGE_REQUEST_PERMISSION
                )
            } else {
                val mainActivity = activity as MainActivity
                mainActivity.replaceFragment(
                    OTAListFragment(),
                    OTAListFragment.TAG,
                    R.id.container_main
                )
            }
        }
    }

    private fun setupBackgroundScanSwitch() {
        val mainActivity = activity as MainActivity
        sw_background_scan.isChecked = mainActivity.isServiceRunning(FireMeshService::class.java)
        sw_background_scan.setOnClickListener {
            if (isLocationEnabled() && BluetoothAdapter.getDefaultAdapter().isEnabled) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    showToastMessage("Only Android 8.0 (Oreo) or higher can use this feature")
                    sw_background_scan.isChecked = false
                }
            } else {
                showToastMessage("Check bluetooth and location")
                sw_background_scan.isChecked = false
            }

        }

        sw_background_scan.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                mainActivity.startFireMeshService()
            } else {
                mainActivity.stopFireMeshService()
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager =
            activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        return locationManager?.let {
            it.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || it.isProviderEnabled(
                LocationManager.GPS_PROVIDER
            )
        } ?: false
    }

    private val onSubnetItemClickedListener =
        object : BaseRecyclerViewAdapter.ItemClickListener<Subnet> {
            override fun onClick(position: Int, item: Subnet) {
                Timber.d("onSubnetItemClickedListener: clicked")
                ViewCompat.postOnAnimationDelayed(view!!, // Delay to show ripple effect
                    Runnable {
                        if (!BluetoothAdapter.getDefaultAdapter().isEnabled || !isLocationEnabled()) {
                            showToastMessage("Please enable bluetooth and location")
                        } else {
                            subnetListViewModel.setCurrentSubnet(item)
                            val mainActivity = activity as MainActivity
                            mainActivity.replaceFragment(
                                SubnetFragment(item.name),
                                SubnetFragment.TAG,
                                R.id.container_main
                            )
                        }

                    }
                    , 50)
            }

            override fun onLongClick(position: Int, item: Subnet) {
                Timber.d("onSubnetItemClickedListener: longClicked")
                val editSubnetDialog = EditSubnetDialog(item)
                editSubnetDialog.show(fragmentManager!!, "EditSubnetDialog")
                editSubnetDialog.setEditSubnetCallback(onEditSubnetCallback)

            }
        }

    private val subnetListObserver = Observer<Set<Subnet>> {
        activity?.runOnUiThread {
            if (it.isNotEmpty()) {
                no_subnet_background.visibility = View.GONE
                subnetListRecyclerViewAdapter.setDataList(it.toMutableList())
            } else {
                subnetListRecyclerViewAdapter.clear()
                no_subnet_background.visibility = View.VISIBLE
            }
        }
    }

    private val onAddSubnetClickListener = object :
        AddSubnetClickListener {
        override fun onClicked() {
            subnetListViewModel.getSubnetList()
        }
    }

    private val onEditSubnetCallback = object : EditSubnetCallback {
        override fun onChanged() {
            subnetListViewModel.getSubnetList()

        }
    }

}