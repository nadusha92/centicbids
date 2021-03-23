package com.example.centicbids.listener

interface EnterAmountDialogListener {
    fun onCancelClicked()
    fun onValidAmountEntered(amount : Double)
    fun onInvalidAmountEntered()
}