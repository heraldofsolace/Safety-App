package dev.abhattacharyea.safety

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter
import org.jetbrains.anko.image

class ContactListAdapter(var contacts: ArrayList<Contact>) :
	DragDropSwipeAdapter<Contact, ContactListAdapter.ViewHolder>(contacts) {
	private val colorGenerator: ColorGenerator = ColorGenerator.MATERIAL
	private val builder: TextDrawable.IBuilder =
		TextDrawable.builder().beginConfig().withBorder(4).endConfig().round()
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(context).inflate(R.layout.contact_list_row,parent,false)
//        return ViewHolder(view)
//    }
//
//    override fun getItemCount(): Int {
//        return contacts.size
//    }
	
	override fun getViewHolder(itemView: View): ViewHolder {
		return ViewHolder(itemView)
	}
	
	override fun getViewToTouchToStartDraggingItem(
		item: Contact,
		viewHolder: ViewHolder,
		position: Int
	): View? {
		return viewHolder.dragIcon
	}
	
	override fun onBindViewHolder(item: Contact, viewHolder: ViewHolder, position: Int) {
		val contact = contacts[position]
		
		
		viewHolder.contactImage.image =
			builder.build(item.name[0].toString(), colorGenerator.randomColor)
		viewHolder.name.text = item.name
		viewHolder.number.text = item.number
	}
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val contact = contacts[position]
//        holder.name.text = contact.name
//        holder.number.text = contact.number
//    }
	
	class ViewHolder(view: View) : DragDropSwipeAdapter.ViewHolder(view) {
		val name: TextView = view.findViewById<TextView>(R.id.contact_list_row_name)
		val number: TextView = view.findViewById<TextView>(R.id.contact_list_row_number)
		val dragIcon: ImageView = view.findViewById<ImageView>(R.id.dragIcon)
		val contactImage: ImageView = view.findViewById<ImageView>(R.id.contact_image)
	}
}