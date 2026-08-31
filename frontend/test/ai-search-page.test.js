import assert from "node:assert/strict";
import test from "node:test";
import { JSDOM } from "jsdom";

const dom = new JSDOM("<!doctype html><html><body></body></html>", {
  url: "http://localhost:3000/#/ai-search",
});

globalThis.window = dom.window;
globalThis.document = dom.window.document;

const { setSafeHtml } = await import("../src/shared/security/xss.js");
const { AiSearchPage } = await import("../src/features/search/AiSearchPage.js");

test("AI search page renders target tabs and structured filters", () => {
  const target = document.createElement("div");
  setSafeHtml(target, AiSearchPage());

  assert.ok(target.querySelector("[data-ai-match-form]"));
  assert.equal(target.querySelectorAll("[data-ai-target]").length, 2);
  assert.ok(target.querySelector('input[name="maxPrice"]'));
  assert.ok(target.querySelector('input[name="maxBudget"]'));
});
