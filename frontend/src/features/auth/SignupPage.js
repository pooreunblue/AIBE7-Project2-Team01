import { formField } from "../../shared/ui/index.js";

export function SignupPage() {
  return `
    <section class="auth-page">
      <form class="auth-card" data-signup-form>
        <h1>회원가입</h1>
        <p>프로필 정보와 로그인 정보를 입력해 주세요.</p>
        <label class="upload-box profile-upload">
          <span>프로필 사진 선택</span>
          <small data-profile-file-name>선택사항 · JPG, PNG 파일</small>
          <input name="profileImage" type="file" accept="image/png,image/jpeg,image/jpg" />
        </label>
        ${formField("닉네임", `<input name="nickname" type="text" autocomplete="nickname" required minlength="2" maxlength="20" />`)}
        ${formField("아이디", `<input name="email" type="email" autocomplete="email" required placeholder="이메일 형식으로 입력" />`)}
        ${formField("비밀번호", `<input name="password" type="password" autocomplete="new-password" required />`)}
        <button class="button primary" type="submit">Sign Up</button>
        <p class="form-message" data-signup-message aria-live="polite"></p>
        <span class="inline-note">Already have an account? <a href="#/login">Sign in</a></span>
      </form>
    </section>
  `;
}
