const WS_BASE_URL = window.__WS_BASE_URL__ || "http://localhost:8080";

/**
 * 채팅방 하나에 STOMP로 연결하고 구독까지 해줌.
 * SockJS/StompJS는 index.html에서 CDN 스크립트로 전역 로드되어 있어야 함(window.SockJS, window.StompJs).
 *
 * @returns 연결에 사용한 StompJs.Client 인스턴스. 페이지를 떠날 때 client.deactivate() 호출해서 정리할 것.
 */
export function connectChatRoom(chatRoomId, { onMessage, onConnect, onError } = {}) {
  const client = new StompJs.Client({
    webSocketFactory: () => new SockJS(`${WS_BASE_URL}/ws`),
    onConnect: () => {
      client.subscribe(`/topic/chat-rooms/${chatRoomId}`, (message) => {
        onMessage?.(JSON.parse(message.body));
      });
      onConnect?.();
    },
    onStompError: (frame) => onError?.(frame),
    onWebSocketError: (event) => onError?.(event),
  });
  client.activate();
  return client;
}

export function sendChatMessage(client, chatRoomId, content, messageType = "TEXT") {
  if (!client?.connected) {
    throw new Error("채팅 서버에 연결되어 있지 않습니다.");
  }
  client.publish({
    destination: "/app/chat.send",
    body: JSON.stringify({ chatRoomId, content, messageType }),
  });
}
