package dev.abhattacharyea.safety.ui.contact

import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemDragListener
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemSwipeListener
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnListScrollListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.wafflecopter.multicontactpicker.MultiContactPicker
import dev.abhattacharyea.safety.*
import org.jetbrains.anko.db.classParser
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.select
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast

class ContactFragment : Fragment() {
	private val contactsList = ArrayList<Contact>()
	private lateinit var adapter: ContactListAdapter
	private lateinit var noContactsText: TextView
	lateinit var pref: SharedPreferences
	
	val ADD_CONTACT_REQUEST = 200
	
	private fun refreshList() {
		context?.database?.use {
			select(ContactsDbOpenHelper.contactsTableName).exec {
				val parser = classParser<Contact>()
				contactsList.clear()
				contactsList.addAll(parseList(parser))
				contactsList.sortBy {
					it.priority
				}
				adapter.dataSet = contactsList
				noContactsText.visibility = if(contactsList.size == 0) View.VISIBLE else View.GONE
			}
		}
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if(requestCode == ADD_CONTACT_REQUEST) {
			if(resultCode == RESULT_OK) {
				val results = MultiContactPicker.obtainResult(data)
				Log.d(TAG, results.size.toString())
				if(results.isEmpty()) {
					toast("No contacts selected")
				} else {
					if(results[0].phoneNumbers.isEmpty()) {
						toast("The selected contact does not have a number")
					} else {
						context?.database?.use {
							execSQL("INSERT OR IGNORE INTO Contacts(name, number, priority) VALUES(\"${results[0].displayName}\", \"${results[0].phoneNumbers[0].number}\", (SELECT IFNULL(max(priority), 0) + 1 FROM Contacts))")
							//val id = insert("Contacts", "name" to results[0].displayName, "number" to results[0].phoneNumbers[0].number)
							toast("Done")
							refreshList()
							
						}
						
						Log.d(TAG, results[0].displayName)
					}
					
				}
				
				
			}
		}
		super.onActivityResult(requestCode, resultCode, data)
		
	}
	
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		pref = PreferenceManager.getDefaultSharedPreferences(context)
		val root = inflater.inflate(R.layout.fragment_home, container, false)
		
		
		val fab = root.findViewById<FloatingActionButton>(R.id.floatingActionButton)
		val rec = root.findViewById<DragDropSwipeRecyclerView>(R.id.contacts_recyclerview)
		noContactsText = root.findViewById(R.id.no_contacts_added_textview)
		adapter = ContactListAdapter(contactsList)
		
		refreshList()
		rec.adapter = adapter
		rec.layoutManager = LinearLayoutManager(context!!)
		rec.orientation =
			DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_VERTICAL_DRAGGING
		
		rec.swipeListener = onItemSwipeListener
		rec.dragListener = onItemDragListener
		rec.scrollListener = onListScrollListener
		
		rec.behindSwipedItemIconDrawableId = R.drawable.ic_delete
		rec.behindSwipedItemBackgroundColor = R.color.colorAccent
		rec.reduceItemAlphaOnSwiping = true
		fab.setOnClickListener {
			if(!pref.getBoolean("data_collection_accepted", false)) {
				alert(
					"In order to provide the users with helpful service, the app requires" +
							" the users to log in with email or Google account." +
							" This data is stored securely and never disclosed to a 3rd party. " +
							" This app does not collect or upload your contacts, or location",
					"How we handle user data"
				) {
					positiveButton("I understand and accept") {
						pref.edit {
							putBoolean("data_collection_accepted", true)
							MultiContactPicker.Builder(this@ContactFragment)
								.setChoiceMode(MultiContactPicker.CHOICE_MODE_SINGLE)
								.showPickerForResult(100)
						}
					}
					
					negativeButton("I refuse") {
						pref.edit {
							putBoolean("data_collection_accepted", false)
						}
						toast("We won't access your contact unless you give consent")
					}
					
					show()
				}
			} else
				MultiContactPicker.Builder(this)
					.setChoiceMode(MultiContactPicker.CHOICE_MODE_SINGLE)
					.showPickerForResult(ADD_CONTACT_REQUEST)
		}
		
		if(!pref.getBoolean("tutorial_showed", false)) {
			alert(
				"Select a few of your trusted contacts and call them or send SOS with one click. " +
						"The very first contact is a \"Super contact\". When the screen is turned off, " +
						"the call or SMS will be sent only to this contact directly. The SOS will always be " +
						"sent to every trusted contact", "Tutorial"
			) {
				positiveButton("Ok") {
					pref.edit {
						putBoolean("tutorial_showed", true)
					}
				}
				isCancelable = false
				show()
			}
		}
		
		return root
	}
	
	override fun onResume() {
		super.onResume()
		context?.let {
		
		}
		
	}
	
	private val onItemSwipeListener = object : OnItemSwipeListener<Contact> {
		override fun onItemSwiped(
			position: Int,
			direction: OnItemSwipeListener.SwipeDirection,
			item: Contact
		): Boolean {
			context!!.database.use {
				delete(ContactsDbOpenHelper.contactsTableName, "number = ?", arrayOf(item.number))
				noContactsText.visibility = if(adapter.itemCount == 1) View.VISIBLE else View.GONE
				
			}
			return false
		}
	}
	
	private val onItemDragListener = object : OnItemDragListener<Contact> {
		override fun onItemDragged(previousPosition: Int, newPosition: Int, item: Contact) {
			// Handle action of item being dragged from one position to another
		}
		
		override fun onItemDropped(initialPosition: Int, finalPosition: Int, item: Contact) {
			// Handle action of item dropped
			contactsList.clear()
			contactsList.addAll(adapter.dataSet)
//            contactsList[initialPosition] = contactsList[finalPosition].also { contactsList[finalPosition] = contactsList[initialPosition] }
			val prev = contactsList[initialPosition]
			contactsList.forEachIndexed { index, contact ->
				contactsList[index].priority = index + 1
				Log.d(TAG, contact.toString())
				Log.d(TAG, contactsList.size.toString())
				context!!.database.use {
					val c = ContentValues()
					c.put("priority", index + 1)
					update(
						ContactsDbOpenHelper.contactsTableName,
						c,
						"number = ?",
						arrayOf(contact.number)
					)
				}
				
			}
			
			
		}
	}
	
	private val onListScrollListener = object : OnListScrollListener {
		override fun onListScrollStateChanged(scrollState: OnListScrollListener.ScrollState) {
			// Handle change on list scroll state
		}
		
		override fun onListScrolled(
			scrollDirection: OnListScrollListener.ScrollDirection,
			distance: Int
		) {
			// Handle scrolling
		}
	}
	
	companion object {
		val TAG = ContactFragment::class.java.simpleName
	}
}