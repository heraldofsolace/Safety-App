package dev.abhattacharyea.safety.ui.donation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.*
import dev.abhattacharyea.safety.DonationAdapter
import dev.abhattacharyea.safety.R
import org.jetbrains.anko.toast

class DonationFragment : Fragment(), PurchasesUpdatedListener {
	private lateinit var billingClient: BillingClient
	private val skuList = listOf("donate_10", "donate_50", "donate_100")
	private lateinit var skuRecyclerView: RecyclerView
	private lateinit var skuAdapter: DonationAdapter
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val v = inflater.inflate(R.layout.fragment_donation, container, false)
		skuRecyclerView = v.findViewById(R.id.donation_list)
		skuRecyclerView.layoutManager = LinearLayoutManager(context)
		setUpBillingClient()
		
		return v
	}
	
	private fun setUpBillingClient() {
		activity?.let {
			billingClient = BillingClient.newBuilder(it)
				.enablePendingPurchases()
				.setListener(this)
				.build()
			
			billingClient.startConnection(object : BillingClientStateListener {
				override fun onBillingServiceDisconnected() {
					Log.i(TAG, "BillingClient disconnected")
					
				}
				
				override fun onBillingSetupFinished(billingResult: BillingResult) {
					Log.i(TAG, "BillingClient set up")
					if(billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
						if(billingClient.isReady) {
							val params = SkuDetailsParams.newBuilder()
								.setSkusList(skuList)
								.setType(BillingClient.SkuType.INAPP)
								.build()
							
							billingClient.querySkuDetailsAsync(params) { billingResult_, skuDetailsList ->
								if(billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList.isNotEmpty()) {
									skuAdapter = DonationAdapter(it, skuDetailsList, billingClient)
									skuRecyclerView.adapter = skuAdapter
									
								}
							}
						} else {
							Log.i(TAG, "BillingClient not ready")
						}
					} else {
						Log.i(TAG, billingResult.responseCode.toString())
					}
				}
			})
			
		}
	}
	
	override fun onPurchasesUpdated(
		billingResult: BillingResult?,
		purchases: MutableList<Purchase>?
	) {
		if(billingResult?.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
			context?.toast("Thank you for donating")
			for(purchase in purchases) {
				val consumeParams =
					ConsumeParams.newBuilder()
						.setPurchaseToken(purchase.purchaseToken)
						.setDeveloperPayload(purchase.developerPayload)
						.build()
				
				billingClient.consumeAsync(consumeParams) { billingResult, outToken ->
					if(billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
						acknowledgePurchase(purchase.purchaseToken)
					}
				}
				
			}
		} else if(billingResult?.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
		
			
		} else {
			// Handle any other error codes.
		}
	}
	
	private fun acknowledgePurchase(purchaseToken: String) {
		val params = AcknowledgePurchaseParams.newBuilder()
			.setPurchaseToken(purchaseToken)
			.build()
		billingClient.acknowledgePurchase(params) { billingResult ->
			val responseCode = billingResult.responseCode
			val debugMessage = billingResult.debugMessage
			
		}
	}
	
	companion object {
		val TAG = DonationFragment::class.java.simpleName
	}
}