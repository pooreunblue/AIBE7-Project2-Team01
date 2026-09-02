import assert from "node:assert/strict";
import test from "node:test";
import { JSDOM } from "jsdom";

const moduleDom = new JSDOM("", { url: "http://localhost:3000/#/ai-search" });
globalThis.window = moduleDom.window;
globalThis.document = moduleDom.window.document;

const { setSafeHtml } = await import("../src/shared/security/xss.js");
const { shell } = await import("../src/shared/ui/index.js");
const { AiSearchPage } = await import("../src/features/search/AiSearchPage.js");
const { HomePage } = await import("../src/features/home/HomePage.js");

test("AI 매칭 화면은 AI 검색창과 접힌 상세 조건을 제공한다", () => {
  const target = document.createElement("div");
  setSafeHtml(target, AiSearchPage());

  assert.ok(target.querySelector("[data-ai-match-form]"));
  assert.equal(target.querySelector(".ai-search-logo")?.textContent.trim(), "✦");
  assert.equal(target.querySelector("[data-ai-match-ai-submit]"), null);
  assert.ok(target.querySelector('button[type="submit"]'));
  assert.ok(target.querySelector("[data-ai-filter-toggle]"));
  assert.ok(target.querySelector("[data-ai-match-options]")?.hasAttribute("hidden"));
  assert.equal(target.querySelectorAll('[name="targetType"]').length, 2);
  assert.ok(target.querySelector('[name="categoryId"]'));
  assert.ok(target.querySelector('[name="maxPrice"]'));
  assert.ok(target.querySelector('[name="minBudget"]'));
});

test("AI 매칭 초기 화면에는 mock 추천 결과가 포함되지 않는다", () => {
  const html = AiSearchPage();

  assert.doesNotMatch(html, /Top Talent Matches/);
  assert.doesNotMatch(html, /Similar Requests/);
  assert.doesNotMatch(html, /fintech app/);
  assert.doesNotMatch(html, /검색을 시작해보세요/);
});

test("메인 검색창은 AI 로고를 표시하되 버튼으로 사용하지 않는다", () => {
  const target = document.createElement("div");
  setSafeHtml(target, HomePage());

  assert.equal(target.querySelector("[data-home-ai-submit]"), null);
  assert.equal(target.querySelector(".ai-search-logo")?.textContent.trim(), "✦");
});

test("공통 헤더는 주요 목록과 AI 매칭 이동 버튼을 제공한다", () => {
  const target = document.createElement("div");
  setSafeHtml(target, shell("<section></section>", "home", { isLoggedIn: true, canGoBack: true }));

  assert.ok(target.querySelector('a[href="#/talents"]'));
  assert.ok(target.querySelector('a[href="#/requests"]'));
  assert.ok(target.querySelector('a[href="#/ai-search"]'));
  assert.equal(target.querySelector('a[href="#/chat"]')?.textContent.trim(), "채팅");
  assert.ok(target.querySelector("[data-header-back]"));
  assert.ok(target.querySelector('a[href="#/mypage"] [data-header-avatar]'));
  assert.equal(target.querySelector("[data-account-menu]"), null);
});

test("헤더 뒤로가기 버튼은 이동 가능한 경우에만 표시한다", () => {
  const target = document.createElement("div");
  setSafeHtml(target, shell("<section></section>", "home", { canGoBack: false }));

  assert.equal(target.querySelector("[data-header-back]"), null);
});
