package com.ceslab.firemesh.presentation.node

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ceslab.firemesh.R
import com.ceslab.firemesh.meshmodule.model.MeshStatus
import com.ceslab.firemesh.presentation.base.BaseFragment
import com.ceslab.firemesh.presentation.main.activity.MainActivity
import com.ceslab.firemesh.presentation.provision_list.dialog.ProvisionBottomDialog
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_node.*
import org.w3c.dom.Node
import timber.log.Timber

class NodeFragment(private val nodeName: String) : BaseFragment() {
    companion object {
        const val TAG = "NodeFragment"
    }

    private lateinit var nodePagerAdapter: NodePagerAdapter
    private lateinit var nodeViewModel: NodeViewModel
    private var isFirstConfig: Boolean = false

    override fun getResLayoutId(): Int {
        return R.layout.fragment_node
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate")
        arguments?.let {
            if (it.containsKey(ProvisionBottomDialog.IS_FIRST_CONFIG_KEY)) {
                isFirstConfig = it.getBoolean(ProvisionBottomDialog.IS_FIRST_CONFIG_KEY)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy")
        disconnectFromNode()
    }


    override fun onMyViewCreated(view: View) {
        Timber.d("onMyViewCreated")
        setupViewModel()
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as MainActivity).supportActionBar?.title = nodeName
        setupDeviceViewPager()
       connectToNode()
    }






    private fun setupViewModel() {
        Timber.d("setupViewModel")
        AndroidSupportInjection.inject(this)
        nodeViewModel = ViewModelProvider(this, viewModelFactory).get(NodeViewModel::class.java)
        nodeViewModel.isFirstConfig = isFirstConfig
        nodeViewModel.getMeshStatus().observe(this, meshStatusObserver)
    }

    private fun setupDeviceViewPager() {
        Timber.d("setupDeviceViewPager")
        nodePagerAdapter = NodePagerAdapter(childFragmentManager)
        view_pager_node.adapter = nodePagerAdapter
        tab_layout_node.setupWithViewPager(view_pager_node)
    }

    private fun connectToNode() {
        Timber.d("connectToNode")
        nodeViewModel.connectToNode()
    }

    private fun disconnectFromNode() {
        Timber.d("disconnectFromNode")
        nodeViewModel.disconnectFromNode()
    }

    private val meshStatusObserver = Observer<MeshStatus> {
        activity?.runOnUiThread {
            when (it) {
                MeshStatus.MESH_CONNECTING -> showProgressDialog("Connecting to node")
                MeshStatus.MESH_CONNECTED -> {

                }
                MeshStatus.MESH_DISCONNECTED -> {

                }
                MeshStatus.INIT_CONFIGURATION_LOADED -> {

                }
            }
        }
    }
}