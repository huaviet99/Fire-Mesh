package com.ceslab.firemesh.presentation.ota_list

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ceslab.firemesh.R
import com.ceslab.firemesh.ota.ble.Discovery
import com.ceslab.firemesh.ota.ble.GattService
import com.ceslab.firemesh.ota.callbacks.TimeoutGattCallback
import com.ceslab.firemesh.ota.model.BluetoothDeviceInfo
import com.ceslab.firemesh.ota.service.OTAService
import com.ceslab.firemesh.presentation.base.BaseFragment
import com.ceslab.firemesh.presentation.base.BaseRecyclerViewAdapter
import com.ceslab.firemesh.presentation.main.activity.MainActivity
import com.ceslab.firemesh.presentation.node_list.OTAListRecyclerViewAdapter
import com.ceslab.firemesh.presentation.ota_config.OTAConfigFragment
import com.ceslab.firemesh.presentation.provision_list.dialog.ProvisionBottomDialog
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_ota_list.*
import kotlinx.android.synthetic.main.fragment_ota_list.bg_ripple
import kotlinx.android.synthetic.main.fragment_ota_list.btn_scanning
import kotlinx.android.synthetic.main.fragment_ota_list.tv_scanning_message
import timber.log.Timber

/**
 * Created by Viet Hua on 04/22/2021.
 */

class OTAListFragment : BaseFragment(), Discovery.BluetoothDiscoveryHost,
    Discovery.DeviceContainer<BluetoothDeviceInfo> {

    companion object {
        const val TAG = "OTAListFragment"
    }

    private lateinit var otaListViewModel: OTAListViewModel
    private lateinit var otaListRecyclerViewAdapter: OTAListRecyclerViewAdapter

    private var service: OTAService? = null
    private var binding: OTAService.Binding? = null
    private val discovery = Discovery(this, this)
    private lateinit var handler: Handler
    private var scanning = false

    override fun getResLayoutId(): Int {
        return R.layout.fragment_ota_list
    }

    override fun onMyViewCreated(view: View) {
        Timber.d("onMyViewCreated")
        setHasOptionsMenu(true)
        setupViewModel()
        setupViews()
        handler = Handler()

        bindBluetoothService()

        discovery.connect(context!!)
    }

    override fun onResume() {
        super.onResume()
        Timber.d("onResume")
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupToolbarTitle("OTA List")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    override fun isReady(): Boolean {
        return !activity?.isFinishing!!
    }

    override fun reDiscover() {
        Timber.d("reDiscover")
        reDiscover(false)
    }

    override fun flushContainer() {
        Timber.d("flushContainer")
    }

    override fun updateWithDevices(devices: List<BluetoothDeviceInfo>) {
        for (device in devices) {
            Timber.d("updateWithDevices: ${device.name} ---- ${device.address}")
        }
        bg_ripple.visibility = View.GONE
        otaListRecyclerViewAdapter.setDataList(devices)
    }

    private fun setupViewModel() {
        Timber.d("setupViewModel")
        AndroidSupportInjection.inject(this)
        otaListViewModel =
            ViewModelProvider(this, viewModelFactory).get(OTAListViewModel::class.java)
        otaListViewModel.apply {

        }
    }

    private fun setupViews() {
        btn_scanning.setOnClickListener(onScanButtonClickListener)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        Timber.d("setupRecyclerView")
        val linearLayoutManager = LinearLayoutManager(context)
        otaListRecyclerViewAdapter = OTAListRecyclerViewAdapter(context!!)
        otaListRecyclerViewAdapter.itemClickListener = onOTAItemClickedListener
        rv_ota_list.apply {
            layoutManager = linearLayoutManager
            setHasFixedSize(true)
            adapter = otaListRecyclerViewAdapter
        }
    }

    private fun bindBluetoothService() {
        Timber.d("bindBluetoothService")
        binding = object : OTAService.Binding(context!!) {
            override fun onBound(service: OTAService?) {
                Timber.d("onBound")
                this@OTAListFragment.service = service
            }
        }
        binding?.bind()
    }

    private fun startScanning() {
        Timber.d("startScanning")
        scanning = true
        // Connected devices are not deleted from list
        reDiscover(false)
    }

    private fun reDiscover(clearCachedDiscoveries: Boolean) {
        Timber.d("reDiscover: $clearCachedDiscoveries")
        discovery.addFilter(GattService.MeshProxyService)
        discovery.addFilter(GattService.MeshProvisioningService)
        discovery.addFilter(GattService.OtaService)
        discovery.startDiscovery(clearCachedDiscoveries)
    }

    private fun connectToDevice(device: BluetoothDeviceInfo?) {
        Timber.d("connectToDevice: ${device!!.address}")
        showProgressDialog("Connecting to device")
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled) {
            hideDialog()
            return
        }

        if (scanning) {
            discovery.stopDiscovery(false)
            scanning = false
            btn_scanning.text = getString(R.string.fragment_provision_list_start_scanning)
            btn_scanning.setBackgroundColor(Color.parseColor("#0288D1"))
        }

        if (device == null) {
            Timber.e("null")
            return
        }

        val bluetoothDeviceInfo: BluetoothDeviceInfo = device

        service?.connectGatt(bluetoothDeviceInfo.device, false, object : TimeoutGattCallback() {
            override fun onTimeout() {
                Timber.d("onTimeout")
                hideDialog()
                showToastMessage("Timeout")
            }

            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                Timber.d("onConnectionStateChange: $status")
                if (newState == BluetoothGatt.STATE_DISCONNECTED && status != BluetoothGatt.GATT_SUCCESS) {
                    hideDialog()
                    if (status == 133) {
                        Timber.e("onConnectionStateChange: Reconnect due to 0x85 (133) error")
                        handler.postDelayed({
                            gatt.close()
                            connectToDevice(device)
                        }, 1000)
                        return

                    }
                } else if (newState == BluetoothGatt.STATE_CONNECTED && status == BluetoothGatt.GATT_SUCCESS) {
                    Timber.d("onConnectionStateChange: GAT_SUCCESS")
                    hideDialog()

                    service?.let {
                        if (it.isGattConnected) {
                            val mainActivity = activity as MainActivity
                            mainActivity.addFragment(
                                OTAConfigFragment(),
                                OTAConfigFragment.TAG,
                                R.id.container_main
                            )
                        }
                    }
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    Timber.d("onConnectionStateChange: STATE_DISCONNECTED")
                    hideDialog()

                    gatt.close()
                    service?.clearGatt()
                }
            }
        })

    }


    private val onScanButtonClickListener = View.OnClickListener {
        Timber.d("onScanButtonClickListener: clicked")
        if (scanning || btn_scanning.text == "Stop Scanning") {
            discovery.stopDiscovery(false)
            scanning = false
            btn_scanning.text = getString(R.string.fragment_ota_list_start_scanning)
            tv_scanning_message.visibility = View.VISIBLE
            tv_scanning_message.text =
                getString(R.string.fragment_ota_list_press_start_message)
            btn_scanning.setBackgroundColor(Color.parseColor("#007bff"))
            bg_ripple.stopRippleAnimation()

        } else {
            btn_scanning.text = getString(R.string.fragment_ota_list_stop_scanning)
            btn_scanning.setBackgroundColor(Color.parseColor("#ff5050"))
            tv_scanning_message.visibility = View.GONE
            bg_ripple.startRippleAnimation()
            startScanning()
        }
    }

    private val onOTAItemClickedListener =
        object : BaseRecyclerViewAdapter.ItemClickListener<BluetoothDeviceInfo> {
            override fun onClick(position: Int, item: BluetoothDeviceInfo) {
                ViewCompat.postOnAnimationDelayed(view!!, // Delay to show ripple effect
                    Runnable {
                        Timber.d("onOTAButtonClickedListener: clicked")
                        connectToDevice(item)
                    }
                    , 50)

            }

            override fun onLongClick(position: Int, item: BluetoothDeviceInfo) {}
        }

}