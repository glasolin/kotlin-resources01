package otus.gpb.homework.viewandresources

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import android.widget.Switch
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

enum class Themes {
    SYSTEM, LIGHT, DARK
}

enum class Screens {
    NONE, CONTACTS, CART, DIALOG
}

@Serializable
data class UserPreferencesStorage (var theme:Themes = Themes.SYSTEM, var screen:Screens=Screens.NONE)

object UserPreferences {

    private val tag = "UserPreferences"
    private val preferencesTag= stringPreferencesKey("JSON_user_prefs")
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings.dat")
    private var preferences = UserPreferencesStorage()
    /*init {
        load()
    }*/
    fun setTheme(newTheme: Themes) {
        preferences.theme=newTheme
    }
    fun getTheme() = preferences.theme

    fun setCurrentScreen(currentScreen: Screens) {
        preferences.screen=currentScreen
    }

    fun getCurrentScreen() = preferences.screen

    fun store(context: Context) {
        val json=Json.encodeToString(preferences)
        runBlocking {
            launch {
                context.dataStore.edit { settings ->
                    settings[preferencesTag] = json
                }
            }
        }
    }

    fun load(context: Context) {
        //val context: Context = MainApplicationInstance().getApplicationInstance()
        runBlocking {
            val json: Flow<String> = context.dataStore.data
                .map { settings ->
                    // No type safety.
                    settings[preferencesTag] ?: ""
                }
            json.firstOrNull().also {
                try {
                    if (it!=null) {
                        preferences = Json.decodeFromString<UserPreferencesStorage>(it)
                    }
                } catch (e: Exception) {
                    Log.d(tag, e.message.toString())
                }
            }
        }
    }
}

open class ActivityHelper(contentLayoutId:Int=0): AppCompatActivity(contentLayoutId) {

    private val preferences=UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        preferences.load(this)
        loadTheme()
        super.onCreate(savedInstanceState)
    }

    private fun loadTheme() {
        when (preferences.getTheme()) {
            Themes.DARK -> setTheme(R.style.Theme_ThemeSwitcher_Dark)
            Themes.LIGHT -> setTheme(R.style.Theme_ThemeSwitcher_Light)
            else -> setTheme(R.style.Theme_ViewResources)
        }
    }

   open fun switchTheme(newTheme:Themes) {
        if (preferences.getTheme() == newTheme ) {
            return
        }
        preferences.setTheme(newTheme)
        preferences.store(this)
        recreate()
    }

    open fun currentTheme()=preferences.getTheme()

    fun currentScreen() = preferences.getCurrentScreen()
    fun switchCurrentScreen(newScreen:Screens) {
        preferences.setCurrentScreen(newScreen)
    }
}

class MainActivity: ActivityHelper(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findViewById<Button>(R.id.xml_view_button).setOnClickListener {
            startActivity(Intent(this, MainXMLActivity::class.java))
        }

        findViewById<Spinner>(R.id.theme_selector).apply {
            ArrayAdapter.createFromResource(
                context,
                R.array.themes,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears.
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner.
                this.adapter = adapter
                this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                        // An item is selected. You can retrieve the selected item using
                        when (pos) {
                            1 -> switchTheme(Themes.LIGHT)
                            2 -> switchTheme(Themes.DARK)
                            else -> switchTheme(Themes.SYSTEM)
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        switchTheme(Themes.SYSTEM)
                    }
                }
                when (currentTheme()) {
                    Themes.LIGHT -> setSelection(1)
                    Themes.DARK -> setSelection(2)
                    else -> setSelection(0)
                }
            }
        }
    }
    override fun switchTheme(newTheme: Themes)=super.switchTheme(newTheme)
    override fun currentTheme()=super.currentTheme()

}

class MainXMLActivity : ActivityHelper() {

    private fun showContacts() {
        startActivity(Intent(this, ContactsActivity::class.java))
    }

    private fun showCart() {
        startActivity(Intent(this, CartActivity::class.java))
    }

    private fun showDialog() {
        MaterialAlertDialogBuilder(this)
            .setView(R.layout.dialog_signin)
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (currentScreen()) {
            Screens.CONTACTS -> startActivity(Intent(this, ContactsActivity::class.java))
            Screens.CART -> startActivity(Intent(this, CartActivity::class.java))
            Screens.DIALOG -> showDialog()
            else ->
        }
        setContentView(R.layout.activity_main_xml)
        findViewById<Button>(R.id.contacts_button).setOnClickListener {
            showContacts()
        }

        findViewById<Button>(R.id.cart_button).setOnClickListener {
            showCart()
        }

        findViewById<Button>(R.id.signin_button).setOnClickListener {
            showDialog()
        }
    }
}