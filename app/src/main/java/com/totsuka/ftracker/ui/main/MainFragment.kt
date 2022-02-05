package com.totsuka.ftracker.ui.main

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.totsuka.ftracker.MainActivity
import com.totsuka.ftracker.R
import com.totsuka.ftracker.databinding.MainFragmentBinding
import com.totsuka.ftracker.db
import java.util.*

class MainFragment: Fragment(R.layout.main_fragment) {
    private var mainFragmentBinding: MainFragmentBinding? = null
    private var isDepositing: Boolean = false
    private var currentBalance: Int = 0
    private var initialMoney: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = MainFragmentBinding.bind(view)
        mainFragmentBinding = binding

        val sharedPref = activity?.getSharedPreferences("user", Context.MODE_PRIVATE) ?: return
        initialMoney = sharedPref.getInt("initial_money", 0)
        currentBalance = sharedPref.getInt("current_balance", 0)

        if (initialMoney <= 0){
            EnableInitial(view, sharedPref)
            return
        } else {
            DisableInitial(initialMoney, currentBalance)
        }
        initializeMain(sharedPref, view)
    }

    fun initializeMain(sharedPref: SharedPreferences, view: View){
        mainFragmentBinding!!.currBalancePreview.text = getMoney(currentBalance)

        mainFragmentBinding!!.mainAmountEditText.editText?.doOnTextChanged { inputText, _, _, _ ->
            onKeyUpHandler(inputText.toString())
        }

        mainFragmentBinding!!.mainAmountEditText.editText?.filters = arrayOf<InputFilter>(MinMaxFilter(1, 2147483647))
        mainFragmentBinding!!.initialMoneyText.editText?.filters = arrayOf<InputFilter>(MinMaxFilter(1, 2147483647))

        mainFragmentBinding!!.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val radio: RadioButton = mainFragmentBinding!!.depositRadio
            isDepositing = radio.isChecked
            onKeyUpHandler(mainFragmentBinding!!.mainAmountEditText.editText?.text.toString())
        }
        onKeyUpHandler()

        mainFragmentBinding!!.submitButton.setOnClickListener {addRecord(view, sharedPref, MainActivity())}
    }

    private fun addRecord(view: View, sharedPref: SharedPreferences, context: Context){
        hideKeyboard()

        var amount = mainFragmentBinding!!.mainAmountEditText.editText?.text.toString().toInt()

        if (!isDepositing){
            amount = -amount
        }

        val date = Calendar.getInstance().time.toString()
        val arr = date.split(" ")
        val dbDate = arr[1] + " " + arr[2] + ", " + arr[5]
        val dbTime = "${arr[3]}".split(":").dropLast(1).joinToString(separator = ":")
        val reason = mainFragmentBinding!!.reasonEdittext.editText?.text.toString()
        val id = db?.insertData(amount, reason, currentBalance, dbDate, dbTime)
        db?.setCount(id!! - 1)

        with (sharedPref.edit()) {
            putInt("current_balance", currentBalance + amount)
            apply()
        }

        currentBalance += amount

        mainFragmentBinding!!.currBalanceSummaryText.text = getMoney(currentBalance)
        mainFragmentBinding!!.currBalancePreview.text = getMoney(currentBalance)

        mainFragmentBinding!!.mainAmountEditText.editText?.setText("")
        mainFragmentBinding!!.reasonEdittext.editText?.setText("")

        onKeyUpHandler()
    }

    private fun setInitial(view: View, sharedPref: SharedPreferences){
        hideKeyboard()
        val editText = mainFragmentBinding!!.initialMoneyText
        val initialEditTextRaw = editText.editText?.editableText.toString()
        var initialEditText: Int
        if ("" == initialEditTextRaw){
            initialEditText = 0
            createSnackBar(view, "Enter the amount of money you have now.", Snackbar.LENGTH_SHORT, "#FF0000")
            return
        } else {
            initialEditText = initialEditTextRaw.toInt()
        }
        if (initialEditText <= 0){
            createSnackBar(view, "Get some money first and try again kek.", Snackbar.LENGTH_SHORT, "#FF0000")
            return
        }
        if (initialEditText <= 100) {
            createSnackBar(view, "Jesus Christ man, you're broke af.", Snackbar.LENGTH_SHORT, "#FFBF00")
        }
        with (sharedPref.edit()) {
            putInt("initial_money", initialEditText)
            putInt("current_balance", initialEditText)
            apply()
        }
        createSnackBar(view, "Updated Initial Balance.", Snackbar.LENGTH_SHORT, "#7FFFD4")
        DisableInitial(initialEditText, initialEditText)

        currentBalance = initialEditText
        initializeMain(sharedPref, view)
    }

    fun createSnackBar(view: View, text: String, length: Int, color: String){
        val snackbar: Snackbar = Snackbar.make(view, text,
            length).setAction("Action", null)
        val snackbarView = snackbar.view
        snackbarView.setBackgroundColor(Color.parseColor(color))
        snackbar.show()
    }

    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun MainActivity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun DisableInitial(initialMoney: Int, currentBalance: Int){
        mainFragmentBinding!!.initialMoneyText.editText?.setText(initialMoney.toString())
        mainFragmentBinding!!.initialLayout.visibility = View.GONE
        mainFragmentBinding!!.mainLayout.visibility = View.VISIBLE
        mainFragmentBinding!!.initialBalanceSummaryText.text = getMoney(initialMoney)
        mainFragmentBinding!!.currBalanceSummaryText.text = getMoney(currentBalance)
    }

    fun EnableInitial(view: View, sharedPref: SharedPreferences){
        mainFragmentBinding!!.setInitial.setOnClickListener {setInitial(view, sharedPref)}
        mainFragmentBinding!!.mainLayout.visibility = View.GONE
    }

    private fun getMoney(money: Int): String {
        return "Rs. $money"
    }

    fun onKeyUpHandler(text: String = ""){
        var change = 0
        if ("" != text) {
            change = text.toInt()
        }
        mainFragmentBinding!!.submitButton.isEnabled = change != 0

        if (isDepositing) {
            mainFragmentBinding!!.changePreview.text = "+ ${getMoney(change)}"
            mainFragmentBinding!!.previewResult.text = getMoney(currentBalance + change)
            mainFragmentBinding!!.mainAmountEditText.error = ""
        } else {
            mainFragmentBinding!!.changePreview.text = "- ${getMoney(change)}"
            mainFragmentBinding!!.previewResult.text = getMoney(currentBalance - change)
            if ((currentBalance - change) < 0) {
                mainFragmentBinding!!.mainAmountEditText.error = "Not enough balance."
                mainFragmentBinding!!.submitButton.isEnabled = false
            } else {
                mainFragmentBinding!!.mainAmountEditText.error = ""
            }
        }
    }

    inner class MinMaxFilter() : InputFilter {
        private var intMin: Int = 0
        private var intMax: Int = 0

        constructor(minValue: Int, maxValue: Int) : this() {
            this.intMin = minValue
            this.intMax = maxValue
        }

        override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dStart: Int, dEnd: Int): CharSequence? {
            try {
                val input = Integer.parseInt(dest.toString() + source.toString())
                if (isInRange(intMin, intMax, input)) {
                    return null
                }
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
            return ""
        }

        private fun isInRange(a: Int, b: Int, c: Int): Boolean {
            return if (b > a) c in a..b else c in b..a
        }
    }
}
