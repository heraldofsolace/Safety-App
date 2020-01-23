package dev.abhattacharyea.safety

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

class ContactsDbOpenHelper private constructor(ctx: Context) :
	ManagedSQLiteOpenHelper(ctx, "MyDatabase", null, 1) {
	init {
		instance = this
	}
	
	companion object {
		private var instance: ContactsDbOpenHelper? = null
		const val contactsTableName = "Contacts"
		@Synchronized
		fun getInstance(ctx: Context) = instance ?: ContactsDbOpenHelper(ctx.applicationContext)
	}
	
	override fun onCreate(db: SQLiteDatabase) {
		// Here you create tables
		db.createTable(
			contactsTableName, true,
			"name" to TEXT,
			"number" to TEXT + PRIMARY_KEY + UNIQUE,
			"priority" to INTEGER
		)
	}
	
	override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
		// Here you can upgrade tables, as usual
		db.dropTable(contactsTableName, true)
		onCreate(db)
	}
	
	
}

// Access property for Context
val Context.database: ContactsDbOpenHelper
	get() = ContactsDbOpenHelper.getInstance(this)
