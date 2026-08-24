import { formField } from "../../shared/ui/index.js";

export function LoginPage() {
  return `
    <section class="auth-page">
      <form class="auth-card">
        <h1>TalentPulse</h1>
        <p>Welcome back. Please sign in to continue.</p>
        ${formField("Email Address", `<input type="email" value="alex@example.com" />`)}
        ${formField("Password", `<input type="password" value="password" />`)}
        <button class="button primary" type="submit">Sign In</button>
        <div class="auth-options">
          <button type="button">Google</button>
          <button type="button">Apple</button>
        </div>
        <span class="inline-note">Do not have an account? <a href="#/login">Sign up</a></span>
      </form>
    </section>
  `;
}
