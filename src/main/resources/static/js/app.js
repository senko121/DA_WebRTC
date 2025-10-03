let ws;

function connectWebSocket() {
  if (!joinedRoom) {
    console.error("Bạn chưa join phòng!");
    return;
  }

  if (!currentToken) {
    console.error("Chưa có token!");
    return;
  }

  // Tạo kết nối WebSocket kèm JWT
  ws = new WebSocket(`ws://localhost:8080/signal?token=${currentToken}`);

  ws.onopen = () => {
    console.log("✅ WebSocket connected");
    // Gửi message JOIN ngay khi WS open
    ws.send(
      JSON.stringify({ type: "JOIN", room: joinedRoom, user: currentUser })
    );
  };

  ws.onmessage = (event) => {
    console.log("📩 Nhận:", event.data);
    let data;
    try {
      data = JSON.parse(event.data);
    } catch {
      data = { text: event.data };
    }

    const msgBox = document.getElementById("messages");

    if (data.type === "CHAT") {
      msgBox.innerHTML += `<div class="msg-other"><b>${data.user}:</b> ${data.text}</div>`;
    } else if (data.type === "JOINED") {
      msgBox.innerHTML += `<div class="sys-msg">${
        data.user || "Người dùng"
      } đã vào phòng ${data.room}</div>`;
    } else {
      msgBox.innerHTML += `<div class="sys-msg">${event.data}</div>`;
    }

    msgBox.scrollTop = msgBox.scrollHeight;
  };

  ws.onclose = () => console.log("❌ WebSocket disconnected");
  ws.onerror = (err) => console.error("⚠️ WebSocket error:", err);
}

function sendMessage() {
  const text = document.getElementById("msgInput").value.trim();
  if (!text || !ws || ws.readyState !== WebSocket.OPEN) {
    alert("Chưa kết nối WebSocket hoặc chưa join room!");
    return;
  }

  const msg = { type: "CHAT", room: joinedRoom, text: text, user: currentUser };
  ws.send(JSON.stringify(msg));

  // Hiển thị tin nhắn của chính mình
  const msgBox = document.getElementById("messages");
  msgBox.innerHTML += `<div class="msg-self"><b>${currentUser}:</b> ${text}</div>`;
  msgBox.scrollTop = msgBox.scrollHeight;
  document.getElementById("msgInput").value = "";
}
