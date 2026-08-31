import assert from "node:assert/strict";
import test from "node:test";
import { JSDOM } from "jsdom";

const moduleDom = new JSDOM("", { url: "http://localhost:3000/#/ai-search" });
globalThis.window = moduleDom.window;
globalThis.document = moduleDom.window.document;

const { setSafeHtml } = await import("../src/shared/security/xss.js");
const { AiSearchPage } = await import("../src/features/search/AiSearchPage.js");
const { HomePage } = await import("../src/features/home/HomePage.js");

test("AI 매칭 화면은 AI 로고 검색과 텍스트 검색 입력을 제공한다", () => {
  const target = document.createElement("div");
  setSafeHtml(target, AiSearchPage());

  assert.ok(target.querySelector("[data-ai-match-form]"));
  assert.equal(target.querySelector("[data-ai-match-ai-submit]")?.textContent.trim(), "✦");
  assert.ok(target.querySelector('button[type="submit"]'));
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
});

test("메인 검색창도 같은 AI 로고 버튼을 사용한다", () => {
  const target = document.createElement("div");
  setSafeHtml(target, HomePage());

  const aiButton = target.querySelector("[data-home-ai-submit]");
  assert.ok(aiButton);
  assert.equal(aiButton.textContent.trim(), "✦");
});
