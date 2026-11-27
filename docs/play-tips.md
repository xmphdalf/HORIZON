# Google Play tips

For the Play listing with package ID `dev.horizon.filter`, create three consumable one-time products with one active Buy purchase option each. The India prices below match the design. Configure the other markets in Play Console. Horizon displays Play’s formatted price.

| Product ID | India base price |
|---|---|
| `horizon_tip_small` | ₹100 |
| `horizon_tip_medium` | ₹300 |
| `horizon_tip_large` | ₹700 |

The Home mission link leads to **Keep the lamp on**. The user picks an amount and taps **Continue** to open Google Play. Back or **I’m good, thanks** leaves without a purchase. Support is optional and changes no filter features. Custom amounts are omitted because checkout requires a configured product.

For checkout testing, activate the catalog and make the app available to an internal tester. Add their Google account under license testing. Use the Play test payment methods and check success, cancellation, pending approval, pending decline, return after app restart, and a second purchase of the same tier. A sideloaded APK can use test billing with a correctly configured license tester. Devices without a working Play Store can use the complete filter.

[Google Play testing instructions](https://developer.android.com/google/play/billing/test)
