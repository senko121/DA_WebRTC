const API_BASE = "http://localhost:8080/api";
let currentToken = localStorage.getItem("accessToken") || "";

// Load danh sách phòng
async function loadRoomList() {
  try {
    const res = await fetch(`${API_BASE}/room/list`, {
      headers: { Authorization: `Bearer ${currentToken}` },
    });

    if (!res.ok) throw new Error("❌ Lỗi khi tải danh sách phòng");

    const rooms = await res.json();
    renderRoomList(rooms);
  } catch (err) {
    alert(err.message);
  }
}

// Render danh sách phòng
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
      <p><strong>Code:</strong> ${room.code}</p>
      <p><strong>Host:</strong> ${room.host.username}</p>
      <p><strong>Active:</strong> ${room.active}</p>
      <div class="btn-group">
        <button onclick="leaveRoom('${room.code}')">Rời phòng</button>
        <button onclick="closeRoom('${room.code}')">Đóng phòng</button>
      </div>
    `;
    container.appendChild(div);
  });
}

// Rời phòng
async function leaveRoom(code) {
  if (!confirm("Bạn có chắc muốn rời phòng " + code + " ?")) return;
  try {
    const res = await fetch(`${API_BASE}/room/leave/${code}`, {
      method: "POST",
      headers: { Authorization: `Bearer ${currentToken}` },
    });
    const text = await res.text();
    alert(text);
    loadRoomList();
  } catch (err) {
    alert("Lỗi: " + err.message);
  }
}

// Đóng phòng
async function closeRoom(code) {
  if (!confirm("Chỉ OWNER mới có thể đóng phòng. Bạn chắc chứ?")) return;
  try {
    const res = await fetch(`${API_BASE}/room/close/${code}`, {
      method: "POST",
      headers: { Authorization: `Bearer ${currentToken}` },
    });
    const text = await res.text();
    alert(text);
    loadRoomList();
  } catch (err) {
    alert("Lỗi: " + err.message);
  }
}

// Tự động load khi mở trang
window.onload = loadRoomList;
