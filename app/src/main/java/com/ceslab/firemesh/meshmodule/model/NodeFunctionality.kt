package com.ceslab.firemesh.meshmodule.model

import com.siliconlab.bluetoothmesh.adk.data_model.model.VendorModel
import com.siliconlab.bluetoothmesh.adk.data_model.node.Node
import timber.log.Timber

/**
 * Created by Viet Hua on 11/23/2020.
 */

class NodeFunctionality {

    enum class VENDOR_FUNCTIONALITY(vararg val model: VendorModelIdentifier) {
        Unknown,
        MyModelClient(VendorModelIdentifier.MyModelClient),
        MyModelServer(VendorModelIdentifier.MyModelServer);


        fun getAllModels(): Set<VendorModelIdentifier> {
            val models = mutableSetOf<VendorModelIdentifier>()
            models.addAll(model)
            return models
        }

        companion object {
            fun fromId(
                companyIdentifierId: Int,
                assignedModelIdentifier: Int
            ): VENDOR_FUNCTIONALITY? {
                return VendorModelIdentifier.values()
                    .find { it.companyIdentifier == companyIdentifierId && it.assignedModelIdentifier == assignedModelIdentifier }
                    ?.let { modelIdentifier ->
                        values().find { it.model.contains(modelIdentifier) }
                    }
            }
        }
    }

    data class FunctionalityNamed(
        val functionality: VENDOR_FUNCTIONALITY,
        val functionalityName: String
    )

    companion object {

        fun getFunctionalitiesNamed(node: Node): Set<FunctionalityNamed> {
            return mutableSetOf(
                FunctionalityNamed(VENDOR_FUNCTIONALITY.Unknown, "")
            ).apply {
                addAll(node.elements?.flatMap { it.vendorModels }
                    ?.mapNotNull { vendorModel ->
                        VENDOR_FUNCTIONALITY.fromId(
                            vendorModel.vendorCompanyIdentifier(),
                            vendorModel.vendorAssignedModelIdentifier()
                        )?.let {
                            FunctionalityNamed(it, vendorModel.name)
                        }
                    } ?: emptySet())
            }
        }

        fun getVendorModels(node: Node, functionality: VENDOR_FUNCTIONALITY): Set<VendorModel> {
            Timber.d("getVendorModels")
            val supportedModelIds = functionality.getAllModels()

            return node.elements?.flatMap { it.vendorModels }
                ?.filter { vendorModel ->
                    supportedModelIds.any {
                        it.companyIdentifier == vendorModel.vendorCompanyIdentifier() && it.assignedModelIdentifier == vendorModel.vendorAssignedModelIdentifier()
                    }
                }
                ?.toSet() ?: emptySet()
        }
    }
}