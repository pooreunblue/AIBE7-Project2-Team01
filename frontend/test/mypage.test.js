import assert from "node:assert/strict";
import test from "node:test";
import { JSDOM } from "jsdom";

const moduleDom = new JSDOM("", { url: "http://localhost:3000/#/mypage" });
globalThis.window = moduleDom.window;
globalThis.document = moduleDom.window.document;

const { setSafeHtml } = await import("../src/shared/security/xss.js");
const { MyTradesPage } = await import("../src/features/trade/MyTradesPage.js");
const { MyPage } = await import("../src/features/user/MyPage.js");

test("마이페이지는 사이드바 없이 포트폴리오, 지갑, 거래 목록 영역을 제공한다", () => {
  const target = document.createElement("div");
  setSafeHtml(target, MyPage());

  assert.equal(target.querySelector(".dashboard-menu"), null);
  assert.ok(target.querySelector("[data-portfolio-preview]"));
  assert.ok(target.querySelector("[data-my-page-wallet]"));
  assert.ok(target.querySelector("[data-my-trade-preview]"));
  assert.ok(target.querySelector("[data-my-authored-posts]"));
  assert.ok(target.querySelector("[data-my-authored-posts-all]"));
  assert.ok(target.querySelector('input[name="profileImage"]'));
  assert.ok(target.querySelector("[data-profile-edit-preview]"));
  assert.ok(target.querySelector("[data-profile-edit-open]"));
  assert.ok(target.querySelector("[data-my-page-logout]"));
});

test("거래 목록 페이지는 완료와 미완료 거래 영역을 나누어 제공한다", () => {
  const target = document.createElement("div");
  setSafeHtml(target, MyTradesPage());

  assert.ok(target.querySelector('[data-trade-list="active"]'));
  assert.ok(target.querySelector('[data-trade-list="completed"]'));
});
