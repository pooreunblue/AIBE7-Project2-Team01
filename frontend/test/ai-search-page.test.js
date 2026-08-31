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
const { HomePage } = await import("../src/features/home/HomePage.js");

test("AI search page renders target tabs and structured filters", () => {
  const target = document.createElement("div");
  setSafeHtml(target, AiSearchPage());

  assert.ok(target.querySelector("[data-ai-match-form]"));
  assert.ok(target.querySelector("[data-ai-match-ai-submit]"));
  assert.equal(target.querySelector("[data-ai-match-ai-submit]").textContent.trim(), "✦");
  assert.equal(target.querySelectorAll("[data-ai-target]").length, 2);
  assert.ok(target.querySelector('input[name="maxPrice"]'));
  assert.ok(target.querySelector('input[name="maxBudget"]'));
});

test("home search page uses the same clickable AI logo", () => {
  const target = document.createElement("div");
  setSafeHtml(target, HomePage());

  const aiButton = target.querySelector("[data-home-ai-submit]");
  assert.ok(aiButton);
  assert.equal(aiButton.textContent.trim(), "✦");
});
