package org.example.link.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.example.link.common.response.ApiResponse;
import org.example.link.domain.chat.dto.ChatMessageResponse;
import org.example.link.domain.chat.dto.ChatRoomCreateRequest;
import org.example.link.domain.chat.dto.ChatRoomResponse;
import org.example.link.domain.chat.service.ChatService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chatrooms")
public class ChatRoomController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ApiResponse<ChatRoomResponse>> create(
            @RequestBody ChatRoomCreateRequest request,
            Authentication authentication
    ) {
        ChatRoomResponse response = chatService.createRoom(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    public ApiResponse<List<ChatRoomResponse>> myRooms(Authentication authentication) {
        return ApiResponse.ok(chatService.getMyRooms(authentication.getName()));
    }

    @GetMapping("/{id}")
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
}
