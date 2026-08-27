package org.example.link.domain.chat.controller;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.example.link.auth.security.CustomUserDetails;
import org.example.link.common.response.ApiResponse;
import org.example.link.domain.chat.dto.ChatMessageResponse;
import org.example.link.domain.chat.dto.ChatRoomCreateRequest;
import org.example.link.domain.chat.dto.ChatRoomResponse;
import org.example.link.domain.chat.service.ChatService;
import org.example.link.domain.trade.dto.TradeCreateRequest;
import org.example.link.domain.trade.dto.TradeResponse;
import org.example.link.domain.trade.service.TradeService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chatrooms")
public class ChatRoomController {

    private final ChatService chatService;
    private final TradeService tradeService;

    @PostMapping
    @Operation(summary = "채팅방 생성")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> create(
            @RequestBody ChatRoomCreateRequest request,
            Authentication authentication
    ) {
        ChatRoomResponse response = chatService.createRoom(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    @Operation(summary = "내 채팅방 목록 조회")
    public ApiResponse<List<ChatRoomResponse>> myRooms(Authentication authentication) {
        return ApiResponse.ok(chatService.getMyRooms(authentication.getName()));
    }

    @PostMapping(value = "/{chatRoomId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "채팅 이미지 전송")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendImage(
            @PathVariable Long chatRoomId,
            @RequestPart("file") MultipartFile file,
            Authentication authentication
    ) {
        ChatMessageResponse response = chatService.sendImage(authentication.getName(), chatRoomId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "채팅 메시지 목록 조회")
    public ApiResponse<List<ChatMessageResponse>> messages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication authentication
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.ok(chatService.getMessages(authentication.getName(), id, pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> leave(
            @PathVariable Long id,
            Authentication authentication
    ) {
        chatService.leaveRoom(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{chatRoomId}/trades")
    public ResponseEntity<ApiResponse<TradeResponse>> createTrade(
            @PathVariable Long chatRoomId,
            @Valid @RequestBody TradeCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        TradeResponse response = tradeService.createTrade(user.getUserId(), chatRoomId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }
}
