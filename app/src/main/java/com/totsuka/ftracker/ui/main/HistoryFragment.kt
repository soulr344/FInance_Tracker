package com.totsuka.ftracker.ui.main

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.italic
import androidx.fragment.app.Fragment
import com.google.android.material.behavior.SwipeDismissBehavior
import com.google.android.material.snackbar.Snackbar
import com.totsuka.ftracker.R
import com.totsuka.ftracker.databinding.CardBinding
import com.totsuka.ftracker.databinding.HistoryFragmentBinding
import com.totsuka.ftracker.db
import com.google.android.material.card.MaterialCardView

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
    }

    fun update(){
        val history: MutableList<User>? = db?.getData()
        if (history != null) {
            for (item in history){
                try{
                    addCard(
                        item.id,
                        item.value,
                        item.balance,
                        item.reason,
                        item.date,
                        item.time
                    )
                } catch (e: NullPointerException) {
                    db?.setCount(0)
                    return
                }
            }
        }
    }

    fun addCard(id: Int, amount: Int, currBalance: Int, reason: String, date: String, time: String) {
        val card: View = LayoutInflater.from(context).inflate(R.layout.card, null, false);
        historyFragmentBinding!!.container.addView(card, 0)

        var amountStr: String = getMoney(amount)
        var amountDesc = "Deposited: "
        if (amount < 0) {
            amountStr = getMoney(-amount)
            amountDesc = "Spent: "
        }
        val binding = CardBinding.bind(card)
        binding.idText.setText(id.toString())
        binding.currBalanceText.setText(generateText("Balance: ", getMoney(currBalance)))
        binding.amountText.setText(generateText(amountDesc, amountStr))
        binding.balanceAfterText.setText(generateText("Balance After: ",
            (currBalance + amount).toString()))
        binding.reasonText.setText(generateText("Reason: ",
            reason.replaceFirstChar { a -> a.uppercaseChar() }))
        binding.dateText.setText(date)
        binding.timeText.setText(time)

        if (amount < 0) {
            binding.card.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.card_red)
        } else {
            binding.card.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.card_green)
        }

        val swipeDismissBehavior = SwipeDismissBehavior<View>()
        swipeDismissBehavior.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_END_TO_START)

        val cardContentLayout: MaterialCardView = binding.card
        val coordinatorParams = cardContentLayout.layoutParams as CoordinatorLayout.LayoutParams

        coordinatorParams.behavior = swipeDismissBehavior

        val that = this
        swipeDismissBehavior.listener = object : SwipeDismissBehavior.OnDismissListener {
            override fun onDismiss(view: View?) {
                Snackbar.make(historyFragmentBinding!!.container, "Deleted Record", Snackbar.LENGTH_INDEFINITE)
                    .setAction("UNDO") { _ ->
                        db?.insertData(amount, reason, currBalance, date, time, id)
                        binding.root.visibility = View.VISIBLE
                        coordinatorParams.setMargins(0, 0, 0, 0);
                        cardContentLayout.alpha = 1.0f;
                        cardContentLayout.requestLayout();
                    }.show()
                binding.root.visibility = View.GONE
                db?.deleteRecord(id, requireContext())
            }

            override fun onDragStateChanged(state: Int) {
                that.onDragStateChanged(state, cardContentLayout)
            }
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

    fun onDragStateChanged(state: Int, cardContentLayout: MaterialCardView) {
        when (state) {
            SwipeDismissBehavior.STATE_DRAGGING, SwipeDismissBehavior.STATE_SETTLING -> cardContentLayout.isDragged =
                true
            SwipeDismissBehavior.STATE_IDLE -> cardContentLayout.isDragged = false
            else -> {
            }
        }
    }
}