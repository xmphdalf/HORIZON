package dev.horizon.filter

import android.app.Activity
import androidx.core.content.edit
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams

/** One application-store connection scoped to the host activity. No account or receipt is stored by Horizon. */
internal class PlayTips(private val activity: Activity, private val protectCheckout: (Boolean) -> Unit) {
    private val ids = listOf("horizon_tip_small", "horizon_tip_medium", "horizon_tip_large")
    private val prefs = activity.getSharedPreferences("play_support", Context.MODE_PRIVATE)
    private val consuming = mutableSetOf<String>()
    private var offers = emptyMap<String, ProductDetails>()
    var prices by mutableStateOf(emptyMap<String, String>())
        private set
    var message by mutableStateOf("")
        private set
    var busy by mutableStateOf(false)
        private set
    var received by mutableStateOf(false)
        private set
    private var connecting = false
    private var closed = false
    private var checkout = false
    private val client = BillingClient.newBuilder(activity)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection()
        .setListener { result, purchases -> onUi {
            checkout = false
            protectCheckout(false)
            when (result.responseCode) {
                BillingResponseCode.OK -> process(purchases.orEmpty())
                BillingResponseCode.ITEM_ALREADY_OWNED -> recover()
                BillingResponseCode.USER_CANCELED -> {
                    markOutstanding(false)
                    busy = false
                    message = ""
                }
                else -> { busy = false; message = "Google Play couldn’t finish checkout. Please try again." }
            }
        } }
        .build()

    private fun onUi(action: () -> Unit) {
        activity.runOnUiThread { if (!closed) action() }
    }

    private fun markOutstanding(value: Boolean) {
        prefs.edit { putBoolean("outstanding", value) }
    }

    fun resume(sheetOpen: Boolean) {
        if (sheetOpen || prefs.getBoolean("outstanding", false)) connect()
    }

    fun open() {
        received = false
        connect()
    }

    private fun connect() {
        if (connecting || checkout) return
        if (client.isReady) { refresh(); return }
        connecting = true
        message = "Checking Google Play…"
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) = onUi {
                connecting = false
                if (result.responseCode == BillingResponseCode.OK) refresh() else unavailable()
            }
            override fun onBillingServiceDisconnected() = onUi {
                connecting = false
                unavailable()
            }
        })
    }

    private fun unavailable() {
        offers = emptyMap()
        prices = emptyMap()
        busy = checkout || consuming.isNotEmpty()
        message = "Tips are unavailable from Google Play right now. You can keep using Horizon."
    }

    private fun refresh() {
        recover()
        val products = ids.map {
            QueryProductDetailsParams.Product.newBuilder().setProductId(it).setProductType(ProductType.INAPP).build()
        }
        client.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(products).build()) { result, response ->
            onUi {
                if (result.responseCode != BillingResponseCode.OK) { unavailable(); return@onUi }
                offers = response.productDetailsList.filter { it.oneTimePurchaseOfferDetailsList?.isNotEmpty() == true }
                    .associateBy { it.productId }
                prices = ids.mapNotNull { id ->
                    offers[id]?.oneTimePurchaseOfferDetailsList?.firstOrNull()?.let { id to it.formattedPrice }
                }.toMap()
                if (prices.isEmpty()) unavailable() else if (!busy) message = ""
            }
        }
    }

    fun purchase(id: String) {
        if (busy || checkout || received) return
        val product = offers[id] ?: return
        val offer = product.oneTimePurchaseOfferDetailsList?.firstOrNull() ?: return
        val params = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(product).apply { offer.offerToken?.let { setOfferToken(it) } }.build()
        busy = true
        checkout = true
        message = ""
        markOutstanding(true)
        protectCheckout(true)
        val result = client.launchBillingFlow(activity, BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(params)).build())
        if (result.responseCode != BillingResponseCode.OK) {
            checkout = false
            protectCheckout(false)
            if (result.responseCode == BillingResponseCode.ITEM_ALREADY_OWNED) recover()
            else {
                markOutstanding(false)
                busy = false
                message = "Checkout couldn’t open. Try again in Google Play."
            }
        }
    }

    private fun recover() {
        client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(ProductType.INAPP).build()) { result, purchases ->
            onUi { if (result.responseCode == BillingResponseCode.OK && !checkout) process(purchases) }
        }
    }

    private fun process(all: List<Purchase>) {
        val tips = all.filter { purchase -> purchase.products.any { it in ids } }
        val pending = tips.any { it.purchaseState == Purchase.PurchaseState.PENDING }
        if (pending) message = "Your tip is pending in Google Play."
        busy = pending || consuming.isNotEmpty()
        markOutstanding(tips.isNotEmpty() || consuming.isNotEmpty())
        for (tip in tips) {
            if (tip.purchaseState != Purchase.PurchaseState.PURCHASED || !consuming.add(tip.purchaseToken)) continue
            busy = true
            client.consumeAsync(ConsumeParams.newBuilder().setPurchaseToken(tip.purchaseToken).build()) { result, token ->
                onUi {
                    consuming.remove(token)
                    val success = result.responseCode == BillingResponseCode.OK
                    busy = pending || consuming.isNotEmpty()
                    markOutstanding(busy || !success)
                    if (success) received = true
                    else message = "Horizon will check your tip with Google Play again when you return."
                }
            }
        }
    }

    fun close() {
        closed = true
        protectCheckout(false)
        client.endConnection()
    }
}
