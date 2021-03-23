package com.example.centicbids.ui

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.centicbids.R
import com.example.centicbids.databinding.ActivityItemDetailsBinding
import com.example.centicbids.listener.EnterAmountDialogListener
import com.example.centicbids.model.Bid
import com.example.centicbids.model.Item
import com.example.centicbids.repository.FirebaseRepository
import com.example.centicbids.util.*
import com.example.centicbids.viewmodel.ItemDetailViewModel
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.ImageListener
import kotlinx.android.synthetic.main.activity_item_details.*

class ItemDetailsActivity : AppCompatActivity(), EnterAmountDialogListener {

    lateinit var layoutBinding: ActivityItemDetailsBinding
    lateinit var detailViewModel: ItemDetailViewModel
    lateinit var firebaseAuth: FirebaseAuth

    var documentId: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDataBinding()
        setUpObservers()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    fun onPlaceBidClicked(view: View) {
        if (firebaseAuth.currentUser != null) {
            // user is logged in . place the bid
            displayEnterAmountDialog(this, detailViewModel.item.value!!, this)
        } else {
            // navigate the user to login screen
            promptLogin()
        }
    }

    private fun setUpObservers() {
        detailViewModel.throwable.observe(this, {
            showToast(
                this,
                getString(R.string.msg_failed_to_resolve_document),
                Toast.LENGTH_SHORT
            )
        })

        detailViewModel.item.observe(this, { item ->
            if (item != null) {
                setUpView(item)
            } else {
                showToast(
                    this,
                    getString(R.string.msg_failed_to_resolve_document),
                    Toast.LENGTH_SHORT
                )
            }
        })
    }

    private fun setUpView(item: Item) {
        if (isValid(item.expiryDate)) {
            setBtnEnabled(true)
            val timeDifference = getTimeDifference(item.expiryDate!!)
            txt_item_detail_expiry_date.text = getTimerText(timeDifference)
            val countDownTimer =
                object : CountDownTimer(timeDifference, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        txt_item_detail_expiry_date.text =
                            getTimerText(millisUntilFinished)
                    }

                    override fun onFinish() {
                        txt_item_detail_expiry_date.text = "00 h : 00 m : 00 s"
                        setBtnEnabled(false)
                    }
                }
            countDownTimer.start()
        } else {
            txt_item_detail_expiry_date.text = "N/A"
            setBtnEnabled(false)
        }

        if (!item.images.isNullOrEmpty()) {
            var imageListener =
                ImageListener { position, imageView ->
                    Picasso.get()
                        .load(item.images[position])
                        .placeholder(R.drawable.vector_loading)
                        .error(R.drawable.vector_loading)
                        .into(imageView)
                }

            carousel_item_detail_images.setImageListener(imageListener)
            carousel_item_detail_images.pageCount = item.images.size
        }

    }

    private fun setBtnEnabled(enabled: Boolean) {
        btn_item_detail_place_bid.isEnabled = enabled
        btn_item_detail_place_bid.isClickable = enabled
        btn_item_detail_place_bid.isFocusable = enabled
    }

    private fun initDataBinding() {
        layoutBinding = DataBindingUtil.setContentView(this, R.layout.activity_item_details)
        detailViewModel = ViewModelProvider(this).get(ItemDetailViewModel::class.java)
        layoutBinding.viewModel = detailViewModel
        layoutBinding.lifecycleOwner = this
        firebaseAuth = FirebaseRepository.getFirebaseAuth()
    }

    private fun loadData() {
        if (intent.hasExtra("documentId")) {
            val documentId = intent.getStringExtra("documentId")
            this.documentId = documentId
            if (documentId != null) {
                detailViewModel.downLoadItem(documentId)
                setUpSnapshotListener(documentId)
            } else {
                showToast(
                    this,
                    getString(R.string.msg_failed_to_resolve_document),
                    Toast.LENGTH_SHORT
                )
            }
        } else {
            showToast(this, getString(R.string.msg_failed_to_resolve_document), Toast.LENGTH_SHORT)
        }
    }

    private fun setUpSnapshotListener(documentId: String) {
        val itemDocumentReference = FirebaseRepository.getItemDocumentReference(documentId)
        itemDocumentReference.addSnapshotListener(
            this
        ) { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)

            }

            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "Current data: ${snapshot.data}")
                detailViewModel.downLoadItem(documentId)
            } else {
                // snapshot
                Log.d(TAG, "Current data: null")
            }
        }
    }


    private fun promptLogin() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.msg_login_first)
        builder.setNegativeButton(R.string.msg_no) { dialog, _ ->
            dialog.dismiss()
        }
        builder.setPositiveButton(
            R.string.msg_yes
        ) { dialog, _ ->
            dialog.dismiss()
            navigateToLogin()
        }
        builder.create().show()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginScreen::class.java)
        startActivity(intent)
    }

    companion object {
        private const val TAG = "CenticBids.Log"
    }

    override fun onCancelClicked() {

    }

    override fun onInvalidAmountEntered() {
        showToast(this, getString(R.string.msg_invalid_amount), Toast.LENGTH_SHORT)
    }

    override fun onValidAmountEntered(amount: Double) {
        // user entered a valid amount . place the bid
        val placeBid =
            detailViewModel.placeBid(Bid(firebaseAuth.currentUser.uid, amount, documentId!!))
        if (placeBid) {
            // bid placed successfully
            showToast(this, getString(R.string.msg_bid_placed_successfully), Toast.LENGTH_SHORT)
            finish()
        } else {
            // failed to place the bid. notify the user.
            showToast(this, getString(R.string.msg_failed_to_place_bid), Toast.LENGTH_SHORT)
        }
    }
}