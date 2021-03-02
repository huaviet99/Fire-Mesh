package com.ceslab.firemesh.presentation.node.node_config

import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ceslab.firemesh.R
import com.ceslab.firemesh.meshmodule.model.ConfigurationTask
import com.ceslab.firemesh.meshmodule.model.MeshNode
import com.ceslab.firemesh.meshmodule.model.NodeConfig
import com.ceslab.firemesh.meshmodule.model.NodeFunctionality
import com.ceslab.firemesh.presentation.base.BaseFragment
import com.siliconlab.bluetoothmesh.adk.ErrorType
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_node_config.*
import timber.log.Timber

class NodeConfigFragment : BaseFragment() {

    private lateinit var nodeConfigViewModel: NodeConfigViewModel

    override fun getResLayoutId(): Int {
        return R.layout.fragment_node_config
    }

    override fun onMyViewCreated(view: View) {
        Timber.d("onMyViewCreated")
        setupViewModel()
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy")
        nodeConfigViewModel.removeConfigListeners()
    }

    private fun setupViewModel() {
        Timber.d("setupViewModel")
        AndroidSupportInjection.inject(this)
        nodeConfigViewModel =
            ViewModelProvider(this, viewModelFactory).get(NodeConfigViewModel::class.java)
        nodeConfigViewModel.apply {
            setConfigListeners()
            getNodeConfig().observe(this@NodeConfigFragment,nodeConfigObserver)
            getProxyStatus().observe(this@NodeConfigFragment,proxyStatusObserver)
            getRelayStatus().observe(this@NodeConfigFragment,relayStatusObserver)
            getFriendStatus().observe(this@NodeConfigFragment,friendStatusObserver)
            getCurrentConfigTask().observe(this@NodeConfigFragment, configurationStatusObserver)
            getConfigurationError().observe(this@NodeConfigFragment, configurationErrorObserver)
        }

    }

    private fun setupNodeFeatureConfig(nodeConfig: NodeConfig) {
        Timber.d("setupNodeFeatureConfig")
        activity?.runOnUiThread {
            nodeConfig.apply {
                isSupportProxy?.let { isSupportProxy ->
                    sw_proxy.isEnabled = isSupportProxy
                }
                isSupportFriend?.let { isSupportFriend ->
                    sw_friend.isEnabled = isSupportFriend
                }

                isSupportRelay?.let { isSupportRelay ->
                    sw_relay.isEnabled = isSupportRelay
                }

                isSupportLowPower?.let {
                    if (it) tv_low_power_support.text =
                        "Is Supported" else "No Supported"
                }
            }

            //SETUP CHANGE SWITCH STATUS
            sw_proxy.setOnCheckedChangeListener { _, isChecked ->
                showProgressDialog("Proxy Feature Changing")
                nodeConfigViewModel.changeProxy(isChecked)
            }
            sw_relay.setOnCheckedChangeListener { _, isChecked ->
                showProgressDialog("Relay Feature Changing")
                nodeConfigViewModel.changeRelay(isChecked)
            }
            sw_friend.setOnCheckedChangeListener { _, isChecked ->
                showProgressDialog("Friend Feature Changing")
                nodeConfigViewModel.changeFriend(isChecked)
            }
        }

    }

    private fun setFeaturesOnClickListeners() {
        btn_get_proxy.setOnClickListener {
            showProgressDialog("Updating Proxy Status")
            nodeConfigViewModel.updateProxy()
        }
        btn_get_relay.setOnClickListener {
            showProgressDialog("Updating Relay Status")
            nodeConfigViewModel.updateRelay()
        }
        btn_get_friend.setOnClickListener {
            showProgressDialog("Updating Friend Status")
            nodeConfigViewModel.updateFriend()
        }
    }

    private fun setupGroupSpinner(meshNode: MeshNode) {
        Timber.d("setupGroupSpinner")
        activity?.runOnUiThread {
            meshNode.apply {
                val groupListInSubnet = node.subnets.first().groups.sortedBy { it.name }
                val groupNameList = ArrayList<String>()
                groupNameList.add("")
                groupListInSubnet.forEach { groupInfo ->
                    groupNameList.add(groupInfo.name)
                }
                val groupAdapter = ArrayAdapter<String>(
                    context!!,
                    android.R.layout.simple_spinner_item,
                    groupNameList
                )
                groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                spinner_group.onItemSelectedListener = null
                spinner_group.adapter = groupAdapter
                if (node.groups.isNotEmpty()) {
                    val groupInfo = groupListInSubnet.find { group ->
                        group == node.groups.iterator().next()
                    }
                    groupInfo?.apply {
                        spinner_group.setSelection(groupNameList.indexOf(name), false)
                    }
                } else if (groupListInSubnet.isNotEmpty()) {
                    spinner_group.setSelection(Adapter.NO_SELECTION, false)
                }
                spinner_group.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if (position == 0) {
                            nodeConfigViewModel.changeGroup(null)
                        } else {
                            nodeConfigViewModel.changeGroup(groupListInSubnet[position - 1])

                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
        }
    }

    private fun setupFunctionalitySpinner(meshNode: MeshNode) {
        Timber.d("setupFunctionalitySpinner")
        activity?.runOnUiThread {
            meshNode.apply {
                val functionalitiesNamed =
                    NodeFunctionality.getFunctionalitiesNamed(node).toMutableList()
                functionalitiesNamed.sortBy { it.functionalityName }

                val functionalitiesName = functionalitiesNamed.map { it.functionalityName }
                val functionalityAdapter = ArrayAdapter<String>(
                    context!!,
                    android.R.layout.simple_spinner_item,
                    functionalitiesName
                )
                functionalityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner_functionality.apply {
                    onItemSelectedListener = null
                    adapter = functionalityAdapter
                    if (functionality != NodeFunctionality.VENDOR_FUNCTIONALITY.Unknown) {
                        functionalitiesNamed.find { it.functionality == functionality }
                            ?.let { functionalityNamed ->
                                setSelection(
                                    functionalitiesNamed.indexOf(functionalityNamed),
                                    false
                                )
                            }

                    } else {
                        setSelection(Adapter.NO_SELECTION, false)
                    }

                    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            nodeConfigViewModel.changeFunctionality(functionalitiesNamed[position].functionality)
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
                }
            }
        }
    }

    private val configurationStatusObserver = Observer<ConfigurationTask> {
        Timber.d("configurationStatusObserver: $it")
        activity?.runOnUiThread {
            when (it) {
                ConfigurationTask.BIND_NODE_TO_GROUP -> showProgressDialog("Bind node to group")
                ConfigurationTask.UNBIND_NODE_FROM_GROUP -> showProgressDialog("Unbind node from group")
                ConfigurationTask.BIND_MODEL_TO_GROUP -> showProgressDialog("Bind model to group")
                ConfigurationTask.UNBIND_MODEL_FROM_GROUP -> showProgressDialog("Unbind model from group")
                ConfigurationTask.SET_PUBLICATION_SETTING -> showProgressDialog("Set publication settings")
                ConfigurationTask.CLEAR_PUBLICATION_SETTING -> showProgressDialog("Clear publication settings")
                ConfigurationTask.ADD_SUBSCRIPTION_SETTING -> showProgressDialog("Add subscription settings")
                ConfigurationTask.REMOVE_SUBSCRIPTION_SETTING -> showProgressDialog("Remove subscription settings")
            }
        }
    }

    private val configurationErrorObserver = Observer<ErrorType> {
        activity?.runOnUiThread {
            showFailedDialog(it.type.toString())
        }
    }

    private val nodeConfigObserver = Observer<NodeConfig> {
        Timber.d("nodeConfigObserver")
        activity?.runOnUiThread {
            hideDialog()
            setFeaturesOnClickListeners()
            setupNodeFeatureConfig(it)
            setupGroupSpinner(it.meshNode)
            setupFunctionalitySpinner(it.meshNode)
        }
    }

    private val proxyStatusObserver = Observer<Boolean> { isEnabled ->
        activity?.runOnUiThread {
            sw_proxy.isChecked = isEnabled
        }
    }

    private val relayStatusObserver = Observer<Boolean> { isEnabled ->
        activity?.runOnUiThread {
            sw_relay.isChecked = isEnabled
        }
    }

    private val friendStatusObserver = Observer<Boolean> { isEnabled ->
        activity?.runOnUiThread {
            sw_friend.isChecked = isEnabled
        }
    }

}