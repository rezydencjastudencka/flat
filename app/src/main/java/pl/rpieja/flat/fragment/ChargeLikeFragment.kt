package pl.rpieja.flat.fragment

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import pl.rpieja.flat.R
import pl.rpieja.flat.dto.ChargeLike
import pl.rpieja.flat.dto.User
import pl.rpieja.flat.viewmodels.Loadable
import pl.rpieja.flat.views.EmptyRecyclerView
import java.text.NumberFormat
import java.util.*

abstract class ChargeLikeFragment<T: ChargeLike, VH: RecyclerView.ViewHolder, VM , DTO>:
        Fragment() where VM: Loadable<DTO>, VM: ViewModel {
    open val listBottomPaddingDp = 0f
    open val layoutId: Int = R.layout.charges_tab
    open val itemLayoutId: Int = R.layout.charges_item
    open val recyclerViewId: Int = R.id.chargesListView
    open val emptyListViewId: Int = R.id.emptyChargesList
    abstract val modelClass: Class<VM>

    val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance()

    init {
        currencyFormat.currency = Currency.getInstance("USD") // TODO fetch
    }

    abstract fun getUsers(item: T): List<User>
    abstract fun extractLiveData(vm: VM): LiveData<DTO>
    abstract fun extractEntityFromDTO(dto: DTO): List<T>
    abstract fun updateItemView(viewHolder: VH, item: T)
    abstract fun createViewHolder(view: View): VH
    abstract fun formatAmount(amountTextView: TextView, amount: Double)

    private var recyclerView: EmptyRecyclerView? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var elements: List<T>? = null

    private fun setData(data: List<T>) {
        recyclerView?.adapter = ItemAdapter(data)
        elements = data
    }

    inner class ItemAdapter(val list: List<T>): RecyclerView.Adapter<VH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(context)
                    .inflate(this@ChargeLikeFragment.itemLayoutId, parent, false)
            return this@ChargeLikeFragment.createViewHolder(view)
        }

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: VH, position: Int) =
                this@ChargeLikeFragment.updateItemView(holder, list[position])
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observe()
    }

    private fun observe() {
        val viewModel: VM = ViewModelProviders.of(activity!!).get(modelClass)
        extractLiveData(viewModel).observe(this, Observer<DTO> { dto ->
            setData(extractEntityFromDTO(dto!!))
            swipeRefreshLayout?.isRefreshing = false
        })
    }

    private fun reload() {
        val viewModel: VM = ViewModelProviders.of(activity!!).get(modelClass)
        viewModel.load(context!!, true)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(layoutId, container, false)
        swipeRefreshLayout = rootView as SwipeRefreshLayout
        swipeRefreshLayout!!.setOnRefreshListener { reload() }
        recyclerView = rootView.findViewById(recyclerViewId)

        recyclerView?.layoutManager = LinearLayoutManager(activity)
        recyclerView?.adapter = ItemAdapter(elements ?: emptyList())
        val paddingPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, listBottomPaddingDp,
                resources.displayMetrics).toInt()
        recyclerView?.setPadding(0, 0, 0, paddingPx)
        recyclerView?.emptyView = rootView.findViewById(emptyListViewId)

        return rootView
    }
}

class ChargeViewHolder(view: View): RecyclerView.ViewHolder(view) {
    val chargeName: TextView = itemView.findViewById(R.id.chargeName)
    val chargeAmount: TextView = itemView.findViewById(R.id.chargeAmount)
    val chargeUsers: TextView = itemView.findViewById(R.id.chargeUsers)
    val chargeCategory: TextView = itemView.findViewById(R.id.chargeCategory)
}

abstract class ChargeLayoutFragment<T: ChargeLike, VM, DTO>:
        ChargeLikeFragment<T, ChargeViewHolder, VM, DTO>() where VM: Loadable<DTO>, VM: ViewModel {
    override fun createViewHolder(view: View): ChargeViewHolder = ChargeViewHolder(view)

    override fun updateItemView(viewHolder: ChargeViewHolder, item: T) {
        viewHolder.chargeName.text = item.chargeName
        formatAmount(viewHolder.chargeAmount, item.chargeAmount)
        viewHolder.chargeUsers.text = android.text.TextUtils.join(", ",
                getUsers(item).map { user -> user.name })
        viewHolder.chargeCategory.text = item.chargeCategory
    }

    override fun formatAmount(amountTextView: TextView, amount: Double) {
        amountTextView.text = currencyFormat.format(amount).toString()
    }
}