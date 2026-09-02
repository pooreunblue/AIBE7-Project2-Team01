import assert from "node:assert/strict";
import test from "node:test";
import { JSDOM } from "jsdom";

test("unsafe API requests bootstrap a CSRF cookie and send its token header", async () => {
  const dom = new JSDOM("<!doctype html><html><body></body></html>", {
    url: "http://localhost:3000/",
  });
  globalThis.window = dom.window;
  globalThis.document = dom.window.document;

  const requests = [];
  globalThis.fetch = async (url, options) => {
    requests.push({ url, options });

    if (url.endsWith("/auth/csrf")) {
      document.cookie = "XSRF-TOKEN=test-csrf-token; Path=/";
    }

    return new Response(JSON.stringify({ data: null }), {
      status: 200,
      headers: { "Content-Type": "application/json" },
    });
  };

  const moduleUrl = new URL("../src/api/api.js", import.meta.url);
  moduleUrl.searchParams.set("test", String(Date.now()));
  const { apiRequest } = await import(moduleUrl.href);

  await apiRequest("/talents", {
    method: "POST",
    body: JSON.stringify({ title: "test" }),
  });

  assert.equal(requests.length, 2);
  assert.equal(requests[0].url, "/api/auth/csrf");
  assert.equal(requests[1].url, "/api/talents");
  assert.equal(requests[1].options.headers.get("X-XSRF-TOKEN"), "test-csrf-token");
});
