package org.example.link.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    //common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "이미 존재하는 데이터입니다."),

    //storage
    INVALID_FILE(HttpStatus.BAD_REQUEST, "유효하지 않은 파일입니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제에 실패했습니다."),
    UNSUPPORTED_FILE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다."),

    //auth
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED,"이메일 또는 비밀번호가 일치하지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 Refresh Token입니다."),

    //wallet
    WALLET_NOT_FOUND(HttpStatus.NOT_FOUND, "지갑을 찾을 수 없습니다."),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "잔액이 부족합니다."),
    INVALID_CHARGE_AMOUNT(HttpStatus.BAD_REQUEST, "충전 금액이 올바르지 않습니다."),
    INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "결제 금액이 올바르지 않습니다."),

    //portfolio
    PORTFOLIO_NOT_FOUND(HttpStatus.NOT_FOUND, "포트폴리오를 찾을 수 없습니다."),
    PORTFOLIO_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 포트폴리오에 접근할 권한이 없습니다."),

    //portfolio file
    PORTFOLIO_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "포트폴리오 파일을 찾을 수 없습니다."),
    INVALID_PORTFOLIO_FILE(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다."),
    PORTFOLIO_FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "파일 크기가 허용 범위를 초과했습니다."),

    //chat
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),
    CHAT_ROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "채팅방에 접근할 권한이 없습니다."),

    //category
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다."),

    //post
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시물입니다.");

    private final HttpStatus status;
    private final String message;
}
