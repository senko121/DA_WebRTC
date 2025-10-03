let ws;

function connectWebSocket() {
  if (!joinedRoom) {
    console.error("❌ Bạn chưa join phòng!");
    return;
  }

  if (!currentToken) {
    console.error("❌ Chưa có token!");
    return;
  }

  // Kết nối WebSocket
  ws = new WebSocket(`ws://localhost:8080/signal?token=${currentToken}`);

  ws.onopen = () => {
    console.log("✅ WebSocket connected");
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
      if (data.user === currentUser) {
        // Tin nhắn của chính mình (bên phải)
        msgBox.innerHTML += `
          <div class="d-flex justify-content-between">
            <p class="small mb-1 text-muted">${new Date().toLocaleTimeString()}</p>
            <p class="small mb-1">${data.user}</p>
          </div>
          <div class="d-flex flex-row justify-content-end mb-4 pt-1">
            <div>
              <p class="small p-2 me-3 mb-3 text-white rounded-3 bg-warning">
                ${data.text}
              </p>
            </div>
            <img src="https://mdbcdn.b-cdn.net/img/Photos/new-templates/bootstrap-chat/ava6-bg.webp"
              alt="avatar" style="width: 45px; height: 100%;">
          </div>
        `;
      } else {
        // Tin nhắn của người khác (bên trái)
        msgBox.innerHTML += `
          <div class="d-flex justify-content-between">
            <p class="small mb-1">${data.user}</p>
            <p class="small mb-1 text-muted">${new Date().toLocaleTimeString()}</p>
          </div>
          <div class="d-flex flex-row justify-content-start">
            <img src="https://mdbcdn.b-cdn.net/img/Photos/new-templates/bootstrap-chat/ava5-bg.webp"
              alt="avatar" style="width: 45px; height: 100%;">
            <div>
              <p class="small p-2 ms-3 mb-3 rounded-3 bg-body-tertiary">
                ${data.text}
              </p>
            </div>
          </div>
        `;
      }
    } else if (data.type === "JOINED") {
      msgBox.innerHTML += `<div class="text-center text-muted small mb-2"><em>${
        data.user || "Người dùng"
      } đã vào phòng</em></div>`;
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

  // Tự hiển thị tin nhắn của mình luôn
  const msgBox = document.getElementById("messages");
  msgBox.innerHTML += `
    <div class="d-flex justify-content-between">
      <p class="small mb-1 text-muted">${new Date().toLocaleTimeString()}</p>
      <p class="small mb-1">${currentUser}</p>
    </div>
    <div class="d-flex flex-row justify-content-end mb-4 pt-1">
      <div>
        <p class="small p-2 me-3 mb-3 text-white rounded-3 bg-warning">
          ${text}
        </p>
      </div>
      <img src="https://mdbcdn.b-cdn.net/img/Photos/new-templates/bootstrap-chat/ava6-bg.webp"
        alt="avatar" style="width: 45px; height: 100%;">
    </div>
  `;

  msgBox.scrollTop = msgBox.scrollHeight;
  document.getElementById("msgInput").value = "";
}
