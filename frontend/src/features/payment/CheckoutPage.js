import { button, pageTitle } from "../../shared/ui/index.js";
import { talents } from "../../shared/data/mock.js";

export function CheckoutPage() {
  return `
    <section class="checkout-layout">
      ${pageTitle("Checkout", "Review your request and securely complete your payment.")}
      <div class="checkout-grid">
        <div>
          <article class="summary-card">
            <h2>Request Summary</h2>
            <div class="seller-box">
              <div class="visual design thumb"></div>
              <div><strong>${talents[0].title}</strong><span>by ${talents[0].expert}</span></div>
            </div>
            <dl>
              <div><dt>Delivery</dt><dd>Oct 25, 2026</dd></div>
              <div><dt>Revisions</dt><dd>Up to 3</dd></div>
            </dl>
          </article>
          <article class="summary-card">
            <h2>Payment Method</h2>
            <label class="payment-option active"><input type="radio" checked /> Credit Card <span>Visa ending 4242</span></label>
            <label class="payment-option"><input type="radio" /> Apple Pay</label>
          </article>
        </div>
        <aside class="checkout-card">
          <span>Payment Details</span>
          <dl>
            <div><dt>Service Fee</dt><dd>$850.00</dd></div>
            <div><dt>Platform Fee</dt><dd>$42.50</dd></div>
          </dl>
          <strong>$892.50</strong>
          <p class="secure-note">Secure escrow payment. Funds are released after delivery approval.</p>
          ${button("Pay $892.50 Now", "#/mypage", "primary")}
        </aside>
      </div>
    </section>
  `;
}
