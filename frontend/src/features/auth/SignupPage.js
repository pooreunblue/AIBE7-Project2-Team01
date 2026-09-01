import { formField } from "../../shared/ui/index.js";

export function SignupPage() {
  return `
    <section class="auth-page">
      <form class="auth-card" data-signup-form>
        <h1>회원가입</h1>
        <p>프로필 정보와 로그인 정보를 입력해 주세요.</p>
        <div class="profile-upload-field">
          <label class="profile-image-picker" aria-label="프로필 사진 선택">
            <span class="signup-avatar" data-profile-preview aria-hidden="true">○</span>
            <span class="profile-edit-button" aria-hidden="true">+</span>
            <input name="profileImage" type="file" accept="image/png,image/jpeg,image/jpg" />
          </label>
          <small data-profile-file-name>선택사항 · JPG, PNG 파일</small>
        </div>
        ${formField("닉네임", `<input name="nickname" type="text" autocomplete="nickname" required minlength="2" maxlength="20" />`)}
        ${formField("아이디", `<input name="email" type="email" autocomplete="email" required placeholder="이메일 형식으로 입력" />`)}
        ${formField("비밀번호", `<input name="password" type="password" autocomplete="new-password" required minlength="8" placeholder="8자 이상 입력" />`)}
        <button class="button primary" type="submit">Sign Up</button>
        <p class="form-message" data-signup-message aria-live="polite"></p>
        <div class="auth-options">
          <a href="http://localhost:8080/oauth2/authorization/google">구글로 시작하기</a>
        </div>
        <span class="inline-note">Already have an account? <a href="#/login">Sign in</a></span>
      </form>
    </section>
  `;
}
