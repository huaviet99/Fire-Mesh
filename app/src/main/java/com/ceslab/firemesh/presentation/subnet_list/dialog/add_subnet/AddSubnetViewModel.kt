package com.ceslab.firemesh.presentation.subnet_list.dialog.add_subnet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ceslab.firemesh.meshmodule.bluetoothmesh.MeshNetworkManager
import com.ceslab.firemesh.util.AppUtil
import com.siliconlab.bluetoothmesh.adk.data_model.network.SubnetMaxExceededException
import com.siliconlab.bluetoothmesh.adk.data_model.subnet.SubnetCreationException
import timber.log.Timber
import javax.inject.Inject

class AddSubnetViewModel @Inject constructor(
    private val meshNetworkManager: MeshNetworkManager

) : ViewModel() {
    val errorMessage = MutableLiveData<String>()

    fun addSubnet(newSubnetName: String){
        Timber.d("addSubnet: $newSubnetName")
        if (!AppUtil.isNameValid(newSubnetName)) {
            errorMessage.value = "Subnet name is not valid"
            return
        }
        try {
            meshNetworkManager.createSubnet(newSubnetName)
        } catch (e: SubnetCreationException) {
            Timber.e("addSubnet exception: ${e.cause}")
            if(e.cause is SubnetMaxExceededException){
                errorMessage.value = "Max number of subnet reached"
            }
        }

    }
}