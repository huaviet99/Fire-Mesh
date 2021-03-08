package com.ceslab.firemesh.ota.ble

import com.ceslab.firemesh.ota.utils.UuidUtils.parseIntFromUuidStart
import java.util.*

/**
 * Enumeration of all available Gatt Services.
 * https://developer.bluetooth.org/gatt/services/Pages/ServicesHome.aspx
 */
enum class GattService {
    GenericAccess(0x00001800, "org.bluetooth.service.generic_access",
        GattCharacteristic.DeviceName,
        GattCharacteristic.Appearance),
    GenericAttribute(0x00001801, "org.bluetooth.service.generic_attribute", GattCharacteristic.ServiceChange),
    ImmediateAlert(0x00001802, "org.bluetooth.service.immediate_alert", GattCharacteristic.AlertLevel),
    LinkLoss(0x00001803, "org.bluetooth.service.link_loss", GattCharacteristic.AlertLevel),
    TxPower(0x00001804, "org.bluetooth.service.tx_power", GattCharacteristic.TxPowerLevel),
    HealthThermometer(0x00001809, "org.bluetooth.service.health_thermometer",
        GattCharacteristic.Temperature,
        GattCharacteristic.TemperatureType,
        GattCharacteristic.IntermediateTemperature),
    DeviceInformation(0x0000180a, "org.bluetooth.service.device_information",
        GattCharacteristic.ManufacturerName,
        GattCharacteristic.ModelNumberString,
        GattCharacteristic.SystemId),
    BatteryService(0x0000180f, "org.bluetooth.service.battery_service", GattCharacteristic.BatteryLevel),
    MeshProvisioningService(0x00001827,"com.silabs.service.mesh_provisioning_service",
        GattCharacteristic.MeshProvisioningDataIn,
        GattCharacteristic.MeshProvisioningDataOut),
    MeshProxyService(0x00001828, "com.silabs.service.mesh_proxy_service",
        GattCharacteristic.MeshProxyDataIn,
        GattCharacteristic.MeshProxyDataOut),
    HudDocking(-0x23fc6ff3, "com.sensedriver.service.hud_docking", GattCharacteristic.DockStatus),
    OtaService(0x1d14d6ee, "com.silabs.service.ota",
        GattCharacteristic.OtaControl,
        GattCharacteristic.OtaData,
        GattCharacteristic.FwVersion,
        GattCharacteristic.OtaVersion),
    ThreadLightService("dd1c077d-d306-4b30-846a-4f55cc35767a", "custom.type",
        GattCharacteristic.Light,
        GattCharacteristic.TriggerSource,
        GattCharacteristic.SourceAddress),
    ConnectLightService("62792313-adf2-4fc9-974d-fab9ddf2622c", "custom.type",
        GattCharacteristic.Light,
        GattCharacteristic.TriggerSource,
        GattCharacteristic.SourceAddress),
    ZigbeeLightService("bae55b96-7d19-458d-970c-50613d801bc9", "custom.type",
        GattCharacteristic.Light,
        GattCharacteristic.TriggerSource,
        GattCharacteristic.SourceAddress),
    ProprietaryLightService("63f596e4-b583-4078-bfc3-b04225378713", "custom.type",
        GattCharacteristic.Light,
        GattCharacteristic.TriggerSource,
        GattCharacteristic.SourceAddress),
    RangeTestService("530aa649-17e6-4d62-9f20-9e393b177e63", "custom.type",
        GattCharacteristic.RangeTestDestinationId,
        GattCharacteristic.RangeTestSourceId,
        GattCharacteristic.RangeTestPacketsReceived,
        GattCharacteristic.RangeTestPacketsSend,
        GattCharacteristic.RangeTestPacketsCount,
        GattCharacteristic.RangeTestPacketsRequired,
        GattCharacteristic.RangeTestPER,
        GattCharacteristic.RangeTestMA,
        GattCharacteristic.RangeTestChannel,
        GattCharacteristic.RangeTestRadioMode,
        GattCharacteristic.RangeTestFrequency,
        GattCharacteristic.RangeTestTxPower,
        GattCharacteristic.RangeTestPayload,
        GattCharacteristic.RangeTestMaSize,
        GattCharacteristic.RangeTestLog,
        GattCharacteristic.RangeTestIsRunning);

    /**
     * The so-called "Assigned Number" of this service.
     */
    val number: UUID

    /**
     * The "Type" of this service (fully qualified name).
     */
    val type: String

    /**
     * Available gatt characteristics for this service.
     */
    private val availableCharacteristics: Array<GattCharacteristic>

    constructor(number: Int, type: String, vararg availableCharacteristics: GattCharacteristic) {
        this.number = UUID.fromString(String.format(Locale.US, FORMAT_STR, number))
        this.type = type
        this.availableCharacteristics = arrayOf(*availableCharacteristics)
        BluetoothLEGatt.GATT_SERVICE_DESCS.put(number, this)
    }

    constructor(uuid: String?, type: String, vararg availableCharacteristics: GattCharacteristic) {
        number = UUID.fromString(uuid)
        this.type = type
        this.availableCharacteristics = arrayOf(*availableCharacteristics)
        val key = parseIntFromUuidStart(uuid!!)
        BluetoothLEGatt.GATT_SERVICE_DESCS.put(key, this)
    }

    companion object {
        private const val FORMAT_STR = "%08x-0000-1000-8000-00805f9b34fb"
        val UUID_MASK = UUID.fromString("0000ffff-0000-0000-0000-000000000000")
        fun fromUuid(uuid: UUID): GattService? {
            for (i in values().indices) {
                val service = values()[i]
                if (service.number == uuid) {
                    return service
                }
            }
            return null
        }
    }
}