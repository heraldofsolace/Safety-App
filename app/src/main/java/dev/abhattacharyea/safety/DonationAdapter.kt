package dev.abhattacharyea.safety

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.SkuDetails

class DonationAdapter(
	val activity: Activity,
	val list: List<SkuDetails>,
	val billingClient: BillingClient
) : RecyclerView.Adapter<DonationAdapter.ViewHolder>() {
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonationAdapter.ViewHolder {
		val v = LayoutInflater.from(activity).inflate(R.layout.donation_list_row, parent, false)
		
		return ViewHolder(v)
	}
	
	override fun getItemCount(): Int = list.size
	
	override fun onBindViewHolder(holder: DonationAdapter.ViewHolder, position: Int) {
		val skuDetails = list[position]
		holder.title.text = list[position].title
		holder.description.text = list[position].description
		holder.price.text = list[position].price
		
		holder.itemView.setOnClickListener {
			val billingFlowParams = BillingFlowParams
				.newBuilder()
				.setSkuDetails(skuDetails)
				.build()
			billingClient.launchBillingFlow(
				activity,
				billingFlowParams
			)
		}
	}
	
	class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		var title = view.findViewById<TextView>(R.id.donation_sku_title)
		var description = view.findViewById<TextView>(R.id.donation_sku_description)
		var price = view.findViewById<TextView>(R.id.donation_sku_price)
	}
}