package pl.rpieja.flat.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.view.View
import pl.rpieja.flat.R
import pl.rpieja.flat.dto.ChargesDTO
import pl.rpieja.flat.dto.Expense
import pl.rpieja.flat.viewmodels.ChargesViewModel

class ExpensesTab: ChargeLikeTab<Expense, ChargeViewHolder>() {
    override fun observe() {
        ViewModelProviders.of(activity!!).get(ChargesViewModel::class.java).charges
                .observe(this, Observer { x: ChargesDTO? -> setData(x!!.incomes) })
    }

    override val layoutId: Int = R.layout.charges_tab
    override val itemLayoutId: Int = R.layout.charges_item
    override val recyclerViewId: Int = R.id.chargesListView

    override fun updateItemView(viewHolder: ChargeViewHolder, item: Expense) {
        viewHolder.chargeName.text = item.name
        viewHolder.chargeAmount.text = currencyFormat.format(item.amount).toString()
        viewHolder.chargeUsers.text = item.from.name
    }

    override fun createViewHolder(view: View): ChargeViewHolder = ChargeViewHolder(view)
}
