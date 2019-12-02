package dev.abhattacharyea.safety.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.abhattacharyea.safety.database
import dev.abhattacharyea.safety.Contact
import dev.abhattacharyea.safety.ContactsDbOpenHelper
import org.jetbrains.anko.alert
import org.jetbrains.anko.appcompat.v7.Appcompat
import org.jetbrains.anko.db.classParser
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.select
import org.jetbrains.anko.makeCall
import org.jetbrains.anko.sendSMS

class CallingDialog : AppCompatActivity() {

    private val contactsList = ArrayList<Contact>()
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val call = intent.getBooleanExtra("call", true)
        database.use {
            select(ContactsDbOpenHelper.contactsTableName).exec {
                val parser = classParser<Contact>()
                contactsList.clear()
                contactsList.addAll(parseList(parser))
                val names = ArrayList<String>()
                contactsList.forEach {
                    names.add(it.name)
                }

                alert(Appcompat) {
                    title = "Choose a contact to ${if (call) "call" else "message"}"
                    items(names) { dialogInterface, pos ->
                        if (call) makeCall(contactsList[pos].number)
                        else sendSMS(contactsList[pos].number, "Test")
                        finish()
                    }
                }.build().apply {
                    setCancelable(true)
                    setOnCancelListener {
                        finish()
                    }
                    setOnDismissListener {
                        finish()
                    }
                }.show()

//
            }
        }

    }
}