package dev.abhattacharyea.safety.ui.home

import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
import org.jetbrains.anko.support.v4.toast

class HomeFragment : Fragment() {
    private val contactsList = ArrayList<Contact>()
    private lateinit var adapter: ContactListAdapter

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
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 100) {
            if(resultCode == RESULT_OK) {
                val results = MultiContactPicker.obtainResult(data)

                context?.database?.use {
                    execSQL("INSERT INTO Contacts(name, number, priority) VALUES(\"${results[0].displayName}\", \"${results[0].phoneNumbers[0].number}\", (SELECT IFNULL(max(priority), 0) + 1 FROM Contacts))")

                    //val id = insert("Contacts", "name" to results[0].displayName, "number" to results[0].phoneNumbers[0].number)
                    toast("Done")
                    refreshList()

                }

                Log.d("CONTACT", results[0].displayName)

            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_home, container, false)


        val fab = root.findViewById<FloatingActionButton>(R.id.floatingActionButton)
        val rec = root.findViewById<DragDropSwipeRecyclerView>(R.id.contacts_recyclerview)
        adapter = ContactListAdapter(contactsList)
        refreshList()
        rec.adapter = adapter
        rec.layoutManager = LinearLayoutManager(context!!)
        rec.orientation = DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_VERTICAL_DRAGGING

        rec.swipeListener = onItemSwipeListener
        rec.dragListener = onItemDragListener
        rec.scrollListener = onListScrollListener

        rec.behindSwipedItemIconDrawableId = R.drawable.ic_delete
        rec.behindSwipedItemBackgroundColor = R.color.colorAccent
        rec.reduceItemAlphaOnSwiping = true
        fab.setOnClickListener {
            MultiContactPicker.Builder(this)
                .setChoiceMode(MultiContactPicker.CHOICE_MODE_SINGLE)
                .showPickerForResult(100)
        }
        return root
    }

    private val onItemSwipeListener = object : OnItemSwipeListener<Contact> {
        override fun onItemSwiped(position: Int, direction: OnItemSwipeListener.SwipeDirection, item: Contact): Boolean {
            context!!.database.use {
                delete(ContactsDbOpenHelper.contactsTableName, "number = ?", arrayOf(item.number))
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
                Log.d("CON", contact.toString())
                Log.d("CON", contactsList.size.toString())
                context!!.database.use {
                    val c = ContentValues()
                    c.put("priority", index + 1)
                    update(ContactsDbOpenHelper.contactsTableName, c, "number = ?", arrayOf(contact.number))
                }

            }


        }
    }

    private val onListScrollListener = object : OnListScrollListener {
        override fun onListScrollStateChanged(scrollState: OnListScrollListener.ScrollState) {
            // Handle change on list scroll state
        }

        override fun onListScrolled(scrollDirection: OnListScrollListener.ScrollDirection, distance: Int) {
            // Handle scrolling
        }
    }

}