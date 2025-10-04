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
const API_BASE = "http://localhost:8080/api";
let currentToken = ""; // token đã lấy khi login

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

// 👉 Gọi API lấy danh sách phòng
async function loadRoomList() {
  try {
    const res = await fetch(`${API_BASE}/room/list`, {
      headers: {
        Authorization: `Bearer ${currentToken}`,
      },
    });

    if (!res.ok) {
      throw new Error("Lỗi khi tải danh sách phòng");
    }

    const rooms = await res.json();
    renderRoomList(rooms);
  } catch (err) {
    alert(err.message);
  }
}

// 👉 Render UI danh sách phòng
function renderRoomList(rooms) {
  const container = document.getElementById("roomList");
  container.innerHTML = "";

  if (rooms.length === 0) {
    container.innerHTML = "<p>Bạn chưa tham gia phòng nào.</p>";
    return;
  }

  rooms.forEach((room) => {
    const div = document.createElement("div");
    div.className = "room-item";
    div.innerHTML = `
      <p><strong>Code:</strong> ${room.code} | <strong>Host:</strong> ${room.host.username} | <strong>Active:</strong> ${room.active}</p>
      <button onclick="leaveRoom('${room.code}')">Rời phòng</button>
      <button onclick="closeRoom('${room.code}')">Đóng phòng</button>
    `;
    container.appendChild(div);
  });

  // Hiện section
  document.getElementById("roomListSection").style.display = "block";
}

// 👉 API leaveRoom
async function leaveRoom(code) {
  if (!confirm("Bạn có chắc muốn rời phòng " + code + " ?")) return;
  try {
    const res = await fetch(`${API_BASE}/room/leave/${code}`, {
      method: "POST",
      headers: { Authorization: `Bearer ${currentToken}` },
    });
    const text = await res.text();
    alert(text);
    loadRoomList(); // refresh danh sách
  } catch (err) {
    alert("Lỗi: " + err.message);
  }
}

// 👉 API closeRoom
async function closeRoom(code) {
  if (!confirm("Chỉ OWNER mới có thể đóng phòng. Bạn chắc chứ?")) return;
  try {
    const res = await fetch(`${API_BASE}/room/close/${code}`, {
      method: "POST",
      headers: { Authorization: `Bearer ${currentToken}` },
    });
    const text = await res.text();
    alert(text);
    loadRoomList(); // refresh danh sách
  } catch (err) {
    alert("Lỗi: " + err.message);
  }
}
