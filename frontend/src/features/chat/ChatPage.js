import { button } from "../../shared/ui/index.js";
import { messages } from "../../shared/data/mock.js";

export function ChatPage() {
  return `
    <section class="chat-layout">
      <aside class="conversation-list">
        <h1>Messages</h1>
        <input type="search" placeholder="Search conversations..." aria-label="Search conversations" />
        <a class="conversation active" href="#/chat"><strong>Elena Rodriguez</strong><span>That sounds great.</span></a>
        <a class="conversation" href="#/chat"><strong>Marcus Chen</strong><span>Here is the link.</span></a>
      </aside>
      <article class="chat-panel">
        <header>
          <div class="seller-box">
            <div class="avatar">E</div>
            <div><strong>Elena Rodriguez</strong><span>UX/UI Designer · Response time 1 hr</span></div>
          </div>
          ${button("Request Transaction", "#/checkout", "primary")}
        </header>
        <div class="message-stream">
          ${messages.map(([type, text]) => `<p class="bubble ${type}">${text}</p>`).join("")}
          <div class="shared-card">
            <div class="visual design small"></div>
            <strong>Fintech App Redesign</strong>
            <span>Portfolio preview</span>
          </div>
        </div>
        <form class="message-compose">
          <input type="text" placeholder="Type a message..." aria-label="Message" />
          <button type="submit">Send</button>
        </form>
      </article>
    </section>
  `;
}
