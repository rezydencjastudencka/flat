package pl.rpieja.flat.activity

import android.app.Activity
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import pl.rpieja.flat.R
import pl.rpieja.flat.dialog.DateDialog
import pl.rpieja.flat.dto.Category
import pl.rpieja.flat.dto.User
import pl.rpieja.flat.viewmodels.NewRevenueViewModel

class NewRevenueActivity : AppCompatActivity() {
    object Constants {
        const val SET_DATE_TAG = "pl.rpieja.flat.newRevenue.setDate"

        const val REQUEST_CREATE = 0
        const val RESULT_CREATE = "pl.rpieja.flat.newRevenue.resultCreate"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_revenue)

        val toolbar: Toolbar = findViewById(R.id.toolbar_1)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        val newRevenueViewModel = ViewModelProviders.of(this)
                .get(NewRevenueViewModel::class.java)
        newRevenueViewModel.loadUsers(applicationContext)

        prepareDateSelectionField(newRevenueViewModel)

        bindEditTextWithLiveData(findViewById(R.id.new_revenue_name),
                newRevenueViewModel.name)

        bindEditTextWithLiveData(findViewById(R.id.new_revenue_amount),
                newRevenueViewModel.amount)

        val category: Spinner = findViewById(R.id.new_revenue_category)
        category.adapter = CategoriesAdapter(newRevenueViewModel.categories,this)
        category.onItemSelectedListener =  object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                newRevenueViewModel.selectedCategory.value = null
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val category = parent!!.getItemAtPosition(position) as Category
                newRevenueViewModel.selectedCategory.value = category
            }
        }

        val users: RecyclerView = findViewById(R.id.new_revenue_users_list)
        users.layoutManager = LinearLayoutManager(this)
        users.adapter = UsersListAdapter(newRevenueViewModel.users,
                newRevenueViewModel.selectedUsers, this)

        val accept: FloatingActionButton = findViewById(R.id.accept_button)
        accept.isEnabled = newRevenueViewModel.isValid.value ?: false
        newRevenueViewModel.isValid.observe(this, Observer { isValid ->
            accept.isEnabled = isValid ?: false
            if (isValid == true) {
                accept.backgroundTintList = ColorStateList.valueOf(
                        resources.getColor(R.color.colorAccent, theme))
            } else {
                accept.backgroundTintList = ColorStateList.valueOf(
                        resources.getColor(R.color.iconColorGreyDark, theme))
            }
        })
        accept.setOnClickListener {
            newRevenueViewModel.createRevenue(this) {
                intent.putExtra(Constants.RESULT_CREATE, it)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }

    private fun bindEditTextWithLiveData(field: EditText, liveData: MutableLiveData<String>) {
        val value = liveData.value
        if (value != null) {
            field.setText(value)
        }
        field.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                liveData.value = editable.toString()
            }

            override fun beforeTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        })
    }

    private fun prepareDateSelectionField(newRevenueViewModel: NewRevenueViewModel) {
        val currentSetDate = supportFragmentManager.findFragmentByTag(Constants.SET_DATE_TAG) as DateDialog?
        currentSetDate?.setDateSetListener(newRevenueViewModel.date::setValue)


        val newRevenueDate: TextView = findViewById(R.id.new_revenue_date)
        newRevenueDate.setOnClickListener {
            val dialog = DateDialog()
            dialog.setDateSetListener(newRevenueViewModel.date::setValue)
            val ft = supportFragmentManager.beginTransaction()
            dialog.show(ft, Constants.SET_DATE_TAG)
        }


        newRevenueViewModel.date.observe(this, Observer { calendar ->
            newRevenueDate.text = DateFormat.getLongDateFormat(this).format(calendar!!.time)
        })
    }
}

private class CategoriesAdapter(
        private val categories: MutableLiveData<List<Category>>,
        lifecycleOwner: LifecycleOwner) : BaseAdapter() {

    init {
        categories.observe(lifecycleOwner, Observer { this.notifyDataSetChanged() })
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(parent?.context)
                .inflate(R.layout.category_list_item, parent, false)
        val textView = view as TextView
        textView.text = categories.value?.get(position)!!.name
        return textView
    }

    override fun getItem(position: Int): Any {
        return categories.value?.get(position)!!
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return categories.value?.size ?: 0
    }
}

private class ViewHolder(val mCheckBox: CheckBox) : RecyclerView.ViewHolder(mCheckBox)

private class UsersListAdapter(
        private val users: MutableLiveData<List<User>>,
        private val selectedUsers: MutableLiveData<Set<User>>,
        lifecycleOwner: LifecycleOwner) : RecyclerView.Adapter<ViewHolder>() {

    init {
        users.observe(lifecycleOwner, Observer { this.notifyDataSetChanged() })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val checkbox = LayoutInflater.from(parent.context)
                .inflate(R.layout.user_list_item, parent, false) as CheckBox

        return ViewHolder(checkbox)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users.value!![position]
        holder.mCheckBox.text = user.name
        holder.mCheckBox.isChecked = selectedUsers.value?.contains(user) ?: false
        holder.mCheckBox.setOnClickListener {
            val newUsers = if (selectedUsers.value?.contains(user) == true) {
                selectedUsers.value?.minus(user) ?: emptySet()
            } else {
                (selectedUsers.value ?: emptySet()).plus(user)
            }
            selectedUsers.setValue(newUsers)
        }
    }

    override fun getItemCount(): Int {
        return users.value?.size ?: 0
    }
}

