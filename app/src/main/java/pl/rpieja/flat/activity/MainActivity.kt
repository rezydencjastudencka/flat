package pl.rpieja.flat.activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import pl.rpieja.flat.R
import pl.rpieja.flat.authentication.AccountService
import pl.rpieja.flat.fragment.ChargesFragment
import pl.rpieja.flat.fragment.TransfersFragment

class MainActivityNavigation(val activity: MainActivity):
        NavigationView.OnNavigationItemSelectedListener {

    private val drawer: DrawerLayout = activity.findViewById(R.id.drawer_layout)
    private var toggle: ActionBarDrawerToggle? = null

    init {
        activity.findViewById<NavigationView>(R.id.navigation)
                .setNavigationItemSelectedListener(this)
        setupHamburger()
    }

    private fun setupHamburger() {
        toggle = ActionBarDrawerToggle(activity, drawer, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle!!)
        toggle!!.syncState() // FIXME call syncState() from onPostCreate()

        activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        activity.supportActionBar!!.setHomeButtonEnabled(true)
    }

    fun toggleDrawer(): Boolean {
        return if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
            true
        } else {
            false
        }
    }

    fun openDrawer(item: MenuItem?): Boolean {
        return toggle!!.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.charges_nav -> {
                val fragment: Fragment = activity.supportFragmentManager
                        .findFragmentByTag(MainActivity.chargesTab) ?: ChargesFragment()
                activity.supportFragmentManager.beginTransaction().replace(R.id.content_frame,
                        fragment, MainActivity.chargesTab).commit()
            }
            R.id.transfers_nav -> {
                val fragment: Fragment = activity.supportFragmentManager
                        .findFragmentByTag(MainActivity.transfersTag) ?: TransfersFragment()
                activity.supportFragmentManager.beginTransaction().replace(R.id.content_frame,
                        fragment, MainActivity.transfersTag).commit()
            }
            R.id.logout_nav -> {
                AccountService.removeCurrentAccount(activity)
                activity.startActivity(Intent(activity, LoginActivity::class.java))
                activity.finish()
            }
        }

        activity.findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawer(GravityCompat.START)

        return true
    }
}

class MainActivity: AppCompatActivity() {
    companion object {
        const val chargesTab = "ChargesFragment"
        const val transfersTag = "TransfersFragment"
    }

    private var navigation: MainActivityNavigation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        navigation = MainActivityNavigation(this)

        // FIXME does not scale well with more perspectives
        if (supportFragmentManager.findFragmentByTag(chargesTab) == null &&
                supportFragmentManager.findFragmentByTag(transfersTag) == null) {
            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.content_frame, ChargesFragment(), chargesTab)
                    .commit()
        }
    }

    override fun onBackPressed() {
        if (!navigation!!.toggleDrawer()) super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (navigation!!.openDrawer(item))
            true
        else
            super.onOptionsItemSelected(item)
    }
}
