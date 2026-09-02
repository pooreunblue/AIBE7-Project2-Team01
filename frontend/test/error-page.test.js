import test from "node:test";
import assert from "node:assert/strict";
import { ErrorPage, errorState } from "../src/features/error/ErrorPage.js";

test("상태코드별 문구", () => {
    assert.match(ErrorPage(404), /페이지를 찾을 수 없어요/);
    assert.match(ErrorPage(403), /접근 권한이 없어요/);
    assert.match(ErrorPage(500), /서버에 문제가/);
    assert.match(ErrorPage(418), /잘못된 요청이에요/);
});
test("errorState", () => {
    assert.match(errorState({ status: 404 }), /404/);
    assert.match(errorState(new Error("x")), /500/);
});