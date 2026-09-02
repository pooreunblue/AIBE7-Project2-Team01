import assert from "node:assert/strict";
import test from "node:test";
import { JSDOM } from "jsdom";

const moduleDom = new JSDOM("", { url: "http://localhost:3000/#/talents" });
globalThis.window = moduleDom.window;
globalThis.document = moduleDom.window.document;

const { setSafeHtml } = await import("../src/shared/security/xss.js");
const { RequestListPage } = await import("../src/features/request/RequestListPage.js");
const { TalentListPage } = await import("../src/features/talent/TalentListPage.js");

test("재능글 목록은 더보기 대신 좌우 페이지 이동 버튼을 제공한다", () => {
  const target = document.createElement("div");
  setSafeHtml(target, TalentListPage());

  assert.ok(target.querySelector("[data-talent-page-prev]"));
  assert.ok(target.querySelector("[data-talent-page-next]"));
  assert.ok(target.querySelector("[data-talent-page-label]"));
  assert.equal(target.querySelector("[data-talent-load-more]"), null);
});

test("요청글 목록은 더보기 대신 좌우 페이지 이동 버튼을 제공한다", () => {
  const target = document.createElement("div");
  setSafeHtml(target, RequestListPage());

  assert.ok(target.querySelector("[data-request-page-prev]"));
  assert.ok(target.querySelector("[data-request-page-next]"));
  assert.ok(target.querySelector("[data-request-page-label]"));
  assert.equal(target.querySelector("[data-request-load-more]"), null);
});
