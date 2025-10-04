// let ws;

// function connectWebSocket() {
//   if (!joinedRoom) {
//     console.error("Bạn chưa join phòng!");
//     return;
//   }

//   if (!currentToken) {
//     console.error("Chưa có token!");
//     return;
//   }

//   // Tạo kết nối WebSocket kèm JWT
//   ws = new WebSocket(`ws://localhost:8080/signal?token=${currentToken}`);

//   ws.onopen = () => {
//     console.log("✅ WebSocket connected");
//     // Gửi message JOIN ngay khi WS open
//     ws.send(
//       JSON.stringify({ type: "JOIN", room: joinedRoom, user: currentUser })
//     );
//   };

//   ws.onmessage = (event) => {
//     console.log("📩 Nhận:", event.data);
//     let data;
//     try {
//       data = JSON.parse(event.data);
//     } catch {
//       data = { text: event.data };
//     }

//     const msgBox = document.getElementById("messages");

//     if (data.type === "CHAT") {
//       msgBox.innerHTML += `<div class="msg-other"><b>${data.user}:</b> ${data.text}</div>`;
//     } else if (data.type === "JOINED") {
//       msgBox.innerHTML += `<div class="sys-msg">${
//         data.user || "Người dùng"
//       } đã vào phòng ${data.room}</div>`;
//     } else {
//       msgBox.innerHTML += `<div class="sys-msg">${event.data}</div>`;
//     }

//     msgBox.scrollTop = msgBox.scrollHeight;
//   };

//   ws.onclose = () => console.log("❌ WebSocket disconnected");
//   ws.onerror = (err) => console.error("⚠️ WebSocket error:", err);
// }

// function sendMessage() {
//   const text = document.getElementById("msgInput").value.trim();
//   if (!text || !ws || ws.readyState !== WebSocket.OPEN) {
//     alert("Chưa kết nối WebSocket hoặc chưa join room!");
//     return;
//   }

//   const msg = { type: "CHAT", room: joinedRoom, text: text, user: currentUser };
//   ws.send(JSON.stringify(msg));

//   // Hiển thị tin nhắn của chính mình
//   const msgBox = document.getElementById("messages");
//   msgBox.innerHTML += `<div class="msg-self"><b>${currentUser}:</b> ${text}</div>`;
//   msgBox.scrollTop = msgBox.scrollHeight;
//   document.getElementById("msgInput").value = "";

// }

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

  ws = new WebSocket(`ws://localhost:8080/signal?token=${currentToken}`);

  ws.onopen = () => {
    console.log("✅ WebSocket connected");
    ws.send(
      JSON.stringify({ type: "JOIN", room: joinedRoom, user: currentUser })
    );
  };

  ws.onmessage = (event) => {
    let data;
    try {
      data = JSON.parse(event.data);
    } catch {
      data = { text: event.data };
    }

    if (data.type === "CHAT") {
      renderMessage(data.user, data.text, data.user === currentUser);
    } else if (data.type === "JOINED") {
      renderSystemMessage(`${data.user} đã vào phòng ${data.room}`);
    } else if (data.type === "LEFT") {
      renderSystemMessage(`${data.user} đã rời phòng`);
    } else {
      renderSystemMessage(event.data);
    }
  };
}

function sendMessage() {
  const text = document.getElementById("msgInput").value.trim();
  if (!text || !ws || ws.readyState !== WebSocket.OPEN) return;

  const msg = { type: "CHAT", room: joinedRoom, text: text, user: currentUser };
  ws.send(JSON.stringify(msg));
  renderMessage(currentUser, text, true);
  document.getElementById("msgInput").value = "";
}

function renderMessage(sender, text, isSelf = false) {
  const msgBox = document.getElementById("messages");

  const wrapper = document.createElement("div");
  wrapper.className = `message ${isSelf ? "self" : "other"}`;

  if (!isSelf) {
    const name = document.createElement("div");
    name.className = "sender";
    name.textContent = sender;
    wrapper.appendChild(name);
  }

  const bubble = document.createElement("div");
  bubble.className = `bubble ${isSelf ? "self-bubble" : "other-bubble"}`;
  bubble.textContent = text;

  wrapper.appendChild(bubble);
  msgBox.appendChild(wrapper);
  msgBox.scrollTop = msgBox.scrollHeight;
}

function renderSystemMessage(text) {
  const msgBox = document.getElementById("messages");
  const sysMsg = document.createElement("div");
  sysMsg.className = "system-message";
  sysMsg.textContent = text;
  msgBox.appendChild(sysMsg);
  msgBox.scrollTop = msgBox.scrollHeight;
}
