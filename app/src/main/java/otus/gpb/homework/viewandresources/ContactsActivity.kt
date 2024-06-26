package otus.gpb.homework.viewandresources

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast

class ContactsActivity : ActivityHelper() {
    private val tag = "ContactsActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        // showing the back button in action bar
        setSupportActionBar(findViewById(R.id.toolbar))

        val actionBar = supportActionBar
        requireNotNull(actionBar==null)
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayUseLogoEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.contacts, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // methods to control the operations that will
    // happen when user clicks on the action buttons
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> Toast.makeText(this, "Search Clicked", Toast.LENGTH_SHORT).show()
            android.R.id.home -> finish()
            else -> Toast.makeText(this, "Else Clicked", Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
    }
}