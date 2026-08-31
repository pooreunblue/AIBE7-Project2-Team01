import assert from "node:assert/strict";
import test from "node:test";
import { JSDOM } from "jsdom";
import { PortfolioCreatePage } from "../src/features/portfolio/PortfolioCreatePage.js";
import { RequestCreatePage } from "../src/features/request/RequestCreatePage.js";
import { TalentCreatePage } from "../src/features/talent/TalentCreatePage.js";

const dom = new JSDOM("<!doctype html><html><body></body></html>", {
  url: "http://localhost:3000/",
});

globalThis.window = dom.window;
globalThis.document = dom.window.document;
globalThis.Node = dom.window.Node;

const {
  appendSafeHtml,
  escapeHtml,
  safeImageUrl,
  safeUrl,
  sanitizeHtml,
  setSafeHtml,
} = await import("../src/shared/security/xss.js");

test("escapeHtml escapes text and attribute delimiters", () => {
  assert.equal(
    escapeHtml(`<img src=x onerror='alert(1)'>`),
    "&lt;img src=x onerror=&#39;alert(1)&#39;&gt;"
  );
});

test("sanitizeHtml removes executable tags and event handlers", () => {
  const sanitized = sanitizeHtml(`
    <p onclick="alert(1)">안전한 본문</p>
    <script>alert(1)</script>
    <img src="x" onerror="alert(1)">
    <svg><script>alert(1)</script></svg>
  `);

  assert.match(sanitized, /안전한 본문/);
  assert.doesNotMatch(sanitized, /<script/i);
  assert.doesNotMatch(sanitized, /onclick|onerror/i);
});

test("sanitizeHtml removes javascript URLs", () => {
  const sanitized = sanitizeHtml(`
    <a href="javascript:alert(1)">링크</a>
    <img src="javascript:alert(1)">
  `);

  assert.doesNotMatch(sanitized, /javascript:/i);
  assert.doesNotMatch(sanitized, /href=|src=/i);
});

test("URL allowlist blocks executable and data protocols", () => {
  assert.equal(safeUrl("javascript:alert(1)"), "");
  assert.equal(safeUrl("java\nscript:alert(1)"), "");
  assert.equal(safeUrl("data:text/html,<script>alert(1)</script>"), "");
  assert.equal(safeImageUrl("data:image/svg+xml,<svg onload=alert(1) />"), "");
  assert.equal(safeUrl("#/talents"), "#/talents");
  assert.equal(safeUrl("https://example.com/file.pdf"), "https://example.com/file.pdf");
});

test("safe DOM helpers sanitize replacement and appended HTML", () => {
  const target = document.createElement("div");
  setSafeHtml(target, `<img src=x onerror="alert(1)"><p>first</p>`);
  appendSafeHtml(target, "beforeend", `<script>alert(1)</script><p>second</p>`);

  assert.equal(target.querySelectorAll("script").length, 0);
  assert.equal(target.querySelector("img").hasAttribute("onerror"), false);
  assert.equal(target.textContent.trim(), "firstsecond");
});

test("sanitized create forms preserve the non-clobbering title field", () => {
  for (const renderPage of [RequestCreatePage, TalentCreatePage, PortfolioCreatePage]) {
    const target = document.createElement("div");
    setSafeHtml(target, renderPage());

    const titleInput = target.querySelector("[data-portfolio-title-input]");
    assert.equal(titleInput?.name, "postTitle");
    assert.equal(new dom.window.FormData(titleInput.form).has("postTitle"), true);
  }
});
