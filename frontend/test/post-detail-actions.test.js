import assert from "node:assert/strict";
import test from "node:test";
import { JSDOM } from "jsdom";

const moduleDom = new JSDOM("", { url: "http://localhost:3000/#/request/request-1" });
globalThis.window = moduleDom.window;
globalThis.document = moduleDom.window.document;

const { setSafeHtml } = await import("../src/shared/security/xss.js");
const { RequestDetailPage } = await import("../src/features/request/RequestDetailPage.js");
const { TalentDetailPage } = await import("../src/features/talent/TalentDetailPage.js");
const requestApi = await import("../src/features/request/requestApi.js");
const tradeApi = await import("../src/features/trade/tradeApi.js");

test("요청글 상세는 작성자 액션을 꽂을 수 있는 액션 영역을 제공한다", () => {
  const target = document.createElement("div");
  setSafeHtml(target, RequestDetailPage("request-1"));

  assert.ok(target.querySelector("[data-request-actions]"));
  assert.ok(target.querySelector("[data-request-chat]"));
});

test("재능글 상세는 작성자 액션 영역을 제공한다", () => {
  const target = document.createElement("div");
  setSafeHtml(target, TalentDetailPage("talent-1"));

  assert.ok(target.querySelector("[data-talent-actions]"));
  assert.ok(target.querySelector("[data-talent-chat]"));
});

test("요청글 상세 관리 API와 거래 취소 API가 프론트에서 호출 가능하다", () => {
  assert.equal(typeof requestApi.closeRequest, "function");
  assert.equal(typeof requestApi.deleteRequest, "function");
  assert.equal(typeof tradeApi.cancelTrade, "function");
});
