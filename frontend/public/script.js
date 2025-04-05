const chatForm = document.getElementById("chatForm");
const messageInput = document.getElementById("messageInput");
const chatBox = document.getElementById("chatBox");
const toggleBtn = document.getElementById("modeToggle");
const exportBtn = document.getElementById("exportChat");
const clearBtn = document.getElementById("clearChat");
const voiceBtn = document.getElementById("voiceInput");
const toggleChat = document.getElementById("toggleChat");
const chatContainer = document.getElementById("chatContainer");
const sendSound = document.getElementById("sendSound");

let recognition;
if ("webkitSpeechRecognition" in window) {
  recognition = new webkitSpeechRecognition();
  recognition.lang = "en-US";
  recognition.continuous = false;
  recognition.interimResults = false;

  voiceBtn.onclick = () => {
    recognition.start();
  };

  recognition.onresult = (event) => {
    messageInput.value = event.results[0][0].transcript;
  };
}

toggleChat.addEventListener("click", () => {
  chatContainer.classList.toggle("hidden");
});

chatForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  const message = messageInput.value.trim();
  if (!message) return;

  const timestamp = new Date().toLocaleTimeString();
  appendMessage("You", message, "right", timestamp);
  appendMessage("TetraCare+", "...", "left", timestamp, true); // typing...

  sendSound.play();

  try {
    const response = await fetch("http://localhost:9090/api/chat", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ message })
    });

    if (!response.ok) throw new Error("❌ Server error");
    const data = await response.json();
    updateLastMessage(data.botReply || "⚠️ No response received.");
  } catch (error) {
    updateLastMessage("❌ Error: " + error.message);
  }

  messageInput.value = "";
});

function appendMessage(sender, text, align = "left", timestamp = "", isLoading = false) {
  const wrapper = document.createElement("div");
  wrapper.className = `flex ${align === "right" ? "justify-end" : "justify-start"}`;

  const messageEl = document.createElement("div");
  messageEl.className = `max-w-xs p-3 rounded-lg ${align === "right"
    ? "bg-blue-600 text-white rounded-br-none"
    : "bg-gray-200 dark:bg-gray-700 text-black dark:text-white rounded-bl-none"} shadow-md`;

  messageEl.innerHTML = `
    <strong>${sender}:</strong>
    <span class="msg-text">${text}</span>
    <div class="text-xs text-right opacity-60 mt-1">${timestamp}</div>
  `;

  wrapper.appendChild(messageEl);
  chatBox.appendChild(wrapper);
  chatBox.scrollTop = chatBox.scrollHeight;
}

function updateLastMessage(newText) {
  const lastMsg = chatBox.querySelectorAll(".msg-text");
  if (lastMsg.length) lastMsg[lastMsg.length - 1].textContent = newText;
}

// Theme toggle + memory
toggleBtn.addEventListener("click", () => {
  const html = document.documentElement;
  html.classList.toggle("dark");
  localStorage.setItem("theme", html.classList.contains("dark") ? "dark" : "light");
});
if (localStorage.getItem("theme") === "dark") {
  document.documentElement.classList.add("dark");
}

// Clear Chat
clearBtn.addEventListener("click", () => {
  chatBox.innerHTML = "";
});

// Export Chat
exportBtn.addEventListener("click", () => {
  const messages = Array.from(chatBox.querySelectorAll(".msg-text")).map(el => el.textContent).join("\n");
  const blob = new Blob([messages], { type: "text/plain" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = "TetraCare_Chat.txt";
  a.click();
  URL.revokeObjectURL(url);
});
