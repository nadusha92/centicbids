package com.example.centicbids.util

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import androidx.databinding.BindingAdapter
import com.example.centicbids.R
import com.example.centicbids.databinding.LayoutEnterBidAmountBinding
import com.example.centicbids.listener.EnterAmountDialogListener
import com.example.centicbids.model.Item
import java.sql.Time
import java.util.concurrent.TimeUnit


fun showToast(context: Context, message: String, length: Int) {
    Toast.makeText(context, message, length).show()
}

fun getTimeDifference(expiryDate: Long): Long {
    return expiryDate - System.currentTimeMillis()
}

fun isValid(expiryDate: Long?): Boolean {
    return if (expiryDate != null) {
        val currentTime = Time(System.currentTimeMillis())
        val expiryTime = Time(expiryDate)
        currentTime.before(expiryTime)
    }else{
        false
    }
}

fun displayEnterAmountDialog(context: Context, item: Item, listener: EnterAmountDialogListener){

    val metrics = context.resources.displayMetrics
    val width = (metrics.widthPixels * 0.80).toInt()

    val dialog = AppCompatDialog(context)
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val layoutBinding : LayoutEnterBidAmountBinding = LayoutEnterBidAmountBinding.inflate(inflater)

    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(layoutBinding.root)
    dialog.setCancelable(false)

    val lp = WindowManager.LayoutParams()
    lp.copyFrom(dialog.window!!.attributes)
    lp.width = width
    dialog.window!!.attributes = lp

    layoutBinding.btnOk.setOnClickListener {
        if (!layoutBinding.txtAmount.text.isNullOrEmpty()){
            val amountInString = layoutBinding.txtAmount.text.toString()
            val doubleValue = amountInString.toDoubleOrNull()
            if (doubleValue!= null){
                if (doubleValue > item.basePrice!! && doubleValue > item.highestBid!!){
                    dialog.dismiss()
                    listener.onValidAmountEntered(doubleValue)
                }else{
                    listener.onInvalidAmountEntered()
                }
            }else{
                listener.onInvalidAmountEntered()
            }
        }else{
            listener.onInvalidAmountEntered()
        }
    }
    layoutBinding.btnCancel.setOnClickListener {
        dialog.dismiss()
        listener.onCancelClicked()
    }

    dialog.show()

}

@SuppressLint("DefaultLocale")
fun getTimerText(milliseconds: Long): String{
    return java.lang.String.format(
        "%02d h : %02d m : %02d s", TimeUnit.MILLISECONDS.toHours(milliseconds),
        TimeUnit.MILLISECONDS.toMinutes(milliseconds) % TimeUnit.HOURS.toMinutes(1),
        TimeUnit.MILLISECONDS.toSeconds(milliseconds) % TimeUnit.MINUTES.toSeconds(1)
    )
}

@BindingAdapter("doubleToString")
fun setDoubleToString(textView: TextView, value: Double){
    textView.text = value.toString()
}

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = context.getString(R.string.notification_channel_name)
        val descriptionText = context.getString(R.string.notification_channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel =
            NotificationChannel(Constants.notification_channel_id, name, importance).apply {
                description = descriptionText
            }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}