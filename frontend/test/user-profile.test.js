import assert from "node:assert/strict";
import test from "node:test";
import { JSDOM } from "jsdom";

const moduleDom = new JSDOM("", { url: "http://localhost:3000/#/users/user-1" });
globalThis.window = moduleDom.window;
globalThis.document = moduleDom.window.document;

const { setSafeHtml } = await import("../src/shared/security/xss.js");
const { RequestDetailPage } = await import("../src/features/request/RequestDetailPage.js");
const { TalentDetailPage } = await import("../src/features/talent/TalentDetailPage.js");
const { UserProfilePage } = await import("../src/features/user/UserProfilePage.js");

test("공개 프로필 페이지는 유저 정보, 포트폴리오, 작성글 영역만 제공한다", () => {
  const target = document.createElement("div");
  setSafeHtml(target, UserProfilePage("user-1"));

  assert.ok(target.querySelector("[data-user-profile-avatar]"));
  assert.ok(target.querySelector("[data-user-profile-portfolios]"));
  assert.ok(target.querySelector("[data-user-profile-posts]"));
  assert.ok(target.querySelector("[data-user-profile-posts-all]"));
  assert.equal(target.querySelector(".public-profile-grid"), null);
  assert.equal(target.querySelector("[data-my-page-wallet]"), null);
  assert.equal(target.querySelector("[data-my-trade-preview]"), null);
});

test("게시글 상세에는 작성자 프로필 이동 영역이 있다", () => {
  const target = document.createElement("div");
  setSafeHtml(target, TalentDetailPage("talent-1") + RequestDetailPage("request-1"));

  assert.ok(target.querySelector("[data-talent-author-box]"));
  assert.ok(target.querySelector("[data-request-author-box]"));
});
