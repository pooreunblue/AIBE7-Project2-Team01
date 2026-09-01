import { formField } from "../../shared/ui/index.js";

export function LoginPage() {
  return `
    <section class="auth-page">
      <form class="auth-card" data-login-form>
        <h1>Knotty</h1>
        <p>Welcome back. Please sign in to continue.</p>
        ${formField("Email Address", `<input name="email" type="email" autocomplete="email" required />`)}
        ${formField("Password", `<input name="password" type="password" autocomplete="current-password" required />`)}
        <button class="button primary" type="submit">Sign In</button>
        <p class="form-message" data-form-message aria-live="polite"></p>
        <div class="auth-options">
          <a href="http://localhost:8080/oauth2/authorization/google">구글로 시작하기</a>
        </div>
        <span class="inline-note">Do not have an account? <a href="#/signup">Sign up</a></span>
      </form>
    </section>
  `;
}
