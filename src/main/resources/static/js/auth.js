// auth.js
let currentUser = null;
let currentToken = null; // lưu JWT

async function register() {
  const username = document.getElementById("username").value.trim();
  const password = document.getElementById("password").value;

  if (!username || !password) {
    alert("Vui lòng nhập đầy đủ!");
    return;
  }

  try {
    const res = await fetch("/api/auth/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password })
    });

    if (!res.ok) {
      const text = await res.text();
      alert("❌ Lỗi: " + text);
      return;
    }

    const user = await res.json();
    alert("✅ Đăng ký thành công cho user: " + user.username);
    currentUser = user.username;

    // show room section
    document.getElementById("authSection").style.display = "none";
    document.getElementById("roomSection").style.display = "block";

  } catch (err) {
    console.error("Lỗi kết nối:", err);
    alert("⚠️ Không thể kết nối server!");
  }
}

async function login() {
  const username = document.getElementById("username").value.trim();
  const password = document.getElementById("password").value;

  if (!username || !password) {
    alert("Vui lòng nhập đầy đủ!");
    return;
  }

  try {
    const res = await fetch("/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password })
    });

    if (!res.ok) {
      const text = await res.text();
      alert("❌ " + text);
      return;
    }

    const data = await res.json();
    currentUser = username;
    currentToken = data.token; // lưu token

    alert(`✅ Login thành công! Role: ${data.role}`);

    document.getElementById("authSection").style.display = "none";
    document.getElementById("roomSection").style.display = "block";

  } catch (err) {
    console.error("Lỗi kết nối:", err);
    alert("⚠️ Không thể kết nối server!");
  }
}
