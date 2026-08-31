import test from "node:test";
import assert from "node:assert/strict";

import { JSDOM } from "jsdom";

const moduleDom = new JSDOM("", { url: "http://localhost" });
globalThis.window = moduleDom.window;
globalThis.document = moduleDom.window.document;

const { AiSearchPage } = await import("../src/features/search/AiSearchPage.js");

test("AI 매칭 화면은 실제 검색 계약에 필요한 입력을 제공한다", () => {
  const dom = new JSDOM(`<main>${AiSearchPage()}</main>`);
  const document = dom.window.document;

  assert.ok(document.querySelector('[name="query"]'));
  assert.equal(document.querySelectorAll('[name="targetType"]').length, 2);
  assert.ok(document.querySelector('[name="categoryId"]'));
  assert.ok(document.querySelector('[name="maxPrice"]'));
  assert.ok(document.querySelector('[name="minBudget"]'));
  assert.equal(document.querySelector("[data-ai-match-results]").children.length, 0);
});

test("AI 매칭 초기 화면에는 mock 추천 결과가 포함되지 않는다", () => {
  const html = AiSearchPage();

  assert.doesNotMatch(html, /Top Talent Matches/);
  assert.doesNotMatch(html, /Similar Requests/);
  assert.doesNotMatch(html, /fintech app/);
});
