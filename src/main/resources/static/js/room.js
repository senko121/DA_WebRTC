let joinedRoom = null;

async function createRoom() {
  if (!currentUser || !currentToken) {
    alert("Bạn cần đăng nhập trước!");
    return;
  }

  try {
    const res = await fetch(`/api/room/create`, {
      method: "POST",
      headers: { "Authorization": "Bearer " + currentToken }
    });

    if (!res.ok) {
      const text = await res.text();
      alert("❌ Không thể tạo phòng: " + text);
      return;
    }

    const room = await res.json();
    joinedRoom = room.code;
    alert("✅ Phòng được tạo với mã: " + room.code);

    // 🔥 tự động connect WS
    connectWebSocket();

    document.getElementById("roomSection").style.display = "none";
    document.getElementById("chatSection").style.display = "block";

  } catch (err) {
    console.error(err);
    alert("⚠️ Lỗi kết nối server!");
  }
}

async function joinRoom() {
  const code = document.getElementById("roomCode").value.trim();
  if (!code) {
    alert("Vui lòng nhập mã phòng!");
    return;
  }

  try {
    const res = await fetch(`/api/room/join/${code}`, {
      method: "POST",
      headers: { "Authorization": "Bearer " + currentToken }
    });

    const text = await res.text();

    if (res.ok && text.startsWith("Join success")) {
      joinedRoom = code;
      alert("✅ " + text);

      // 🔥 tự động connect WS
      connectWebSocket();

      document.getElementById("roomSection").style.display = "none";
      document.getElementById("chatSection").style.display = "block";
    } else {
      alert("❌ " + text);
    }
  } catch (err) {
    console.error(err);
    alert("⚠️ Lỗi kết nối server!");
  }
}
