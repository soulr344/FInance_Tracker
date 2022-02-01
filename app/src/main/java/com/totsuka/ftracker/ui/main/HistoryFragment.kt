package com.totsuka.ftracker.ui.main

import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.italic
import androidx.fragment.app.Fragment
import com.totsuka.ftracker.R
import com.totsuka.ftracker.databinding.CardBinding
import com.totsuka.ftracker.databinding.HistoryFragmentBinding
import com.totsuka.ftracker.db

lateinit var a: HistoryFragment

open class HistoryFragment: Fragment(R.layout.history_fragment)  {
    private var historyFragmentBinding: HistoryFragmentBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = HistoryFragmentBinding.bind(view)
        historyFragmentBinding = binding

        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                update()
                mainHandler.postDelayed(this, 1000)
            }
        })
//        update()

    }

    fun update(){
        val history: MutableList<User>? = db?.getData()
        if (history != null) {
            for (item in history){
                addCard(
                    item.id,
                    item.value,
                    item.balance,
                    item.reason,
                    item.date,
                    item.time
                )
            }
        }
    }

    fun addCard(id: Int, amount: Int, currBalance: Int, reason: String, date: String, time: String){
        val card: View = LayoutInflater.from(context).inflate(R.layout.card, null, false);
        historyFragmentBinding!!.container.addView(card, 0)

        var amountStr: String = getMoney(amount)
        var amountDesc = "Deposited "
        if (amount < 0){
            amountStr = getMoney(-amount)
            amountDesc = "Spent "
        }
        val binding = CardBinding.bind(card)
        binding.idText.setText(id.toString())
        binding.currBalanceText.setText(generateText("Balance: ", getMoney(currBalance)))
        binding.amountText.setText(generateText(amountDesc,amountStr))
        binding.balanceAfterText.setText(generateText("Balance After: ", (currBalance + amount).toString()))
        binding.reasonText.setText(generateText("Reason: ", reason.replaceFirstChar { a -> a.uppercaseChar() }))
        binding.dateText.setText(date)
        binding.timeText.setText(time)

        if (amount < 0) {
            binding.cardContainer.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.card_red)
        } else {
            binding.cardContainer.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.card_green)
        }

        binding.cardContainer.setOnLongClickListener { v ->
            val alertDialog: AlertDialog? = activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setPositiveButton("OK",
                        DialogInterface.OnClickListener { dialog, _ ->
                            db?.deleteRecord(id, requireContext())
                            binding.root.visibility = View.GONE
                        })
                    setNegativeButton("Cancel",
                        DialogInterface.OnClickListener { dialog, id ->
                            // User cancelled the dialog
                        })
                }
                // Set other dialog properties
                builder?.setMessage("Delete the record?")
                    .setTitle("Alert")

                // Create the AlertDialog
                builder.create()
            }

            alertDialog?.show()
            false
        }
    }

    fun getMoney(money: Int): String {
        return "Rs. $money"
    }

    fun generateText(bold: String, text: String): SpannableStringBuilder {
        val s= SpannableStringBuilder()
            .bold { append(bold) }
            .italic { append(text) }
        return s
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}