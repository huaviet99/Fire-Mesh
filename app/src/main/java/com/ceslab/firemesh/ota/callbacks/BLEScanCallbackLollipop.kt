package com.ceslab.firemesh.ota.callbacks

import android.annotation.TargetApi
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.ceslab.firemesh.ota.ble.ScanResultCompat.Companion.from
import com.ceslab.firemesh.ota.service.OTAService

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class BLEScanCallbackLollipop(private val service: OTAService) : ScanCallback() {
    private val handler: Handler = Handler(Looper.getMainLooper())

    override fun onScanResult(callbackType: Int, result: ScanResult) {
        Log.d("onScanResult", "Discovered bluetoothLE +$result");
        if (service.addDiscoveredDevice(from(result)!!)) {
            service.bluetoothAdapter?.bluetoothLeScanner?.stopScan(this)
        }
    }

    override fun onBatchScanResults(results: List<ScanResult>) {
        for (result in results) {
            val device = result.device
            Log.d("onBatchScanResults", "Discovered bluetoothLE +" + device.address + " with name " + device.name);
            if (service.addDiscoveredDevice(from(result)!!)) {
                service.bluetoothAdapter?.bluetoothLeScanner?.stopScan(this)
                break
            }
        }
    }

    override fun onScanFailed(errorCode: Int) {
        if (errorCode != SCAN_FAILED_ALREADY_STARTED) {
            handler.post { service.onDiscoveryCanceled() }
        }
    }

}