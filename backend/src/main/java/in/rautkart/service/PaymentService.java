package in.rautkart.service;

import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import in.rautkart.exception.ApiException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Razorpay integration, test mode only.
 *
 * When no key id / secret is configured the service runs in "mock" mode: no
 * network call is made and checkout completes immediately. That keeps the demo
 * runnable on a fresh clone without any Razorpay account.
 */
@Service
public class PaymentService {

    private final String keyId;
    private final String keySecret;
    private final RazorpayClient client;

    public PaymentService(@Value("${razorpay.key-id}") String keyId,
                          @Value("${razorpay.key-secret}") String keySecret) {
        this.keyId = keyId == null ? "" : keyId.trim();
        this.keySecret = keySecret == null ? "" : keySecret.trim();

        RazorpayClient c = null;
        if (isConfigured()) {
            try {
                c = new RazorpayClient(this.keyId, this.keySecret);
            } catch (Exception e) {
                throw new IllegalStateException("Could not initialise Razorpay client", e);
            }
        }
        this.client = c;
    }

    public boolean isConfigured() {
        return !keyId.isEmpty() && !keySecret.isEmpty();
    }

    public String getKeyId() {
        return keyId;
    }

    public static int toPaise(BigDecimal rupees) {
        return rupees.multiply(BigDecimal.valueOf(100)).intValueExact();
    }

    /** Returns the Razorpay order id the frontend checkout widget needs. */
    public String createOrder(BigDecimal amount, String receipt) {
        if (!isConfigured()) {
            return null;
        }
        try {
            JSONObject request = new JSONObject();
            request.put("amount", toPaise(amount));
            request.put("currency", "INR");
            request.put("receipt", receipt);
            request.put("payment_capture", 1);
            com.razorpay.Order order = client.orders.create(request);
            return order.get("id");
        } catch (Exception e) {
            throw new ApiException(org.springframework.http.HttpStatus.BAD_GATEWAY,
                    "Could not reach Razorpay: " + e.getMessage());
        }
    }

    /** HMAC check on the handler payload the Razorpay widget hands back. */
    public boolean verifySignature(String razorpayOrderId, String razorpayPaymentId, String signature) {
        if (!isConfigured()) {
            return true;
        }
        try {
            JSONObject payload = new JSONObject();
            payload.put("razorpay_order_id", razorpayOrderId);
            payload.put("razorpay_payment_id", razorpayPaymentId);
            payload.put("razorpay_signature", signature);
            return Utils.verifyPaymentSignature(payload, keySecret);
        } catch (Exception e) {
            return false;
        }
    }
}
