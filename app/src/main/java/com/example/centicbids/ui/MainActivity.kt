package com.example.centicbids.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.centicbids.R
import com.example.centicbids.adapter.ItemAdapter
import com.example.centicbids.databinding.ActivityMainBinding
import com.example.centicbids.listener.OnItemClickListener
import com.example.centicbids.repository.FirebaseRepository
import com.example.centicbids.util.showToast
import com.example.centicbids.viewmodel.MainActivityViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var layoutBinding: ActivityMainBinding
    private lateinit var mainViewModel: MainActivityViewModel

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mQuery: Query
    private lateinit var mAdapter: ItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDataBinding()
        initRecyclerView()
    }

    override fun onStart() {
        super.onStart()
        setAdapterQuery()
        mAdapter.startListening()
    }

    private fun setAdapterQuery() {
        mAdapter.setQuery(mQuery)
    }

    override fun onStop() {
        super.onStop()
        mAdapter.stopListening()
    }

    private fun initDataBinding() {
        layoutBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mainViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        layoutBinding.viewModel = mainViewModel
        layoutBinding.lifecycleOwner = this
        firebaseAuth = FirebaseRepository.getFirebaseAuth()
        mQuery = FirebaseRepository.getAllItemsQuery()
    }

    private fun initRecyclerView() {
        mAdapter = object : ItemAdapter(mQuery, this@MainActivity) {
            override fun onDataChanged() {
                // Show/hide content if the query returns empty.
            }

            override fun onError(e: FirebaseFirestoreException?) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Error: check logs for info.", Snackbar.LENGTH_LONG
                ).show()
            }
        }
        recyclerview_items.layoutManager = LinearLayoutManager(this)
        recyclerview_items.adapter = mAdapter
    }

    fun onLogoutClicked(view: View) {
        if (firebaseAuth.currentUser != null) {
            // user is logged in . place the bid
            promptLogOut()
        } else {
            showToast(this, getString(R.string.msg_user_not_logged_in), Toast.LENGTH_SHORT)
        }
    }

    override fun onItemClicked(item: DocumentSnapshot) {
        navigateToDetails(item.id)
    }

    private fun navigateToDetails(documentId: String) {
        val intent = Intent(this, ItemDetailsActivity::class.java)
        intent.putExtra("documentId", documentId)
        startActivity(intent)
    }

    private fun promptLogOut() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.msg_logout_confirmation)
        builder.setNegativeButton(R.string.msg_no) { dialog, _ ->
            dialog.dismiss()
        }
        builder.setPositiveButton(
            R.string.msg_yes
        ) { dialog, _ ->
            firebaseAuth.signOut()
            showToast(this, getString(R.string.msg_user_logged_out), Toast.LENGTH_SHORT)
            dialog.dismiss()
        }
        builder.create().show()
    }

}