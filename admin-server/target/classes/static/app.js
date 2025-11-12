const REFRESH_INTERVAL_MS = 10_000;
const STREAM_RETRY_MS = 3000;

const metrics = {
    total: document.getElementById("metric-total"),
    online: document.getElementById("metric-online"),
    calls: document.getElementById("metric-calls"),
    messages: document.getElementById("metric-messages")
};
const $users = document.getElementById("users-body");
const $calls = document.getElementById("calls-body");
const $lastUpdated = document.getElementById("last-updated");
const $refreshBtn = document.getElementById("refresh-btn");
const supportsEventSource = typeof window.EventSource !== "undefined";
let stream;

async function fetchJson(url) {
    const response = await fetch(url);
    if (!response.ok) {
        throw new Error(`Request failed: ${response.status} ${response.statusText}`);
    }
    return response.json();
}

function renderUsers(users) {
    if (!users.length) {
        $users.innerHTML = `<tr><td colspan="3" class="placeholder">No users yet.</td></tr>`;
        return;
    }
    $users.innerHTML = users.map(user => `
        <tr>
            <td>${user.username}</td>
            <td>${user.displayName}</td>
            <td>
                <span class="status-dot ${user.online ? "status-online" : "status-offline"}">
                    ${user.online ? "Online" : "Offline"}
                </span>
            </td>
        </tr>
    `).join("");
}

function renderCalls(calls) {
    if (!calls.length) {
        $calls.innerHTML = `<tr><td colspan="5" class="placeholder">No call history.</td></tr>`;
        return;
    }
    $calls.innerHTML = calls.map(call => `
        <tr>
            <td>${call.id}</td>
            <td>${call.caller}</td>
            <td>${call.callee}</td>
            <td>${call.status}</td>
            <td>${formatTime(call.startedAt)}</td>
        </tr>
    `).join("");
}

function renderStats(stats) {
    metrics.total.textContent = stats?.totalUsers ?? 0;
    metrics.online.textContent = stats?.onlineUsers ?? 0;
    metrics.calls.textContent = stats?.activeCalls ?? 0;
    metrics.messages.textContent = stats?.messagesToday ?? 0;
}

function formatTime(iso) {
    const date = new Date(iso);
    if (Number.isNaN(date.getTime())) {
        return "N/A";
    }
    return date.toLocaleString();
}

function showError(targetBody, message) {
    targetBody.innerHTML = `<tr><td colspan="${targetBody.dataset.columns || 3}" class="placeholder">${message}</td></tr>`;
}

function applySnapshot(snapshot) {
    if (!snapshot) {
        return;
    }
    renderStats(snapshot.stats);
    renderUsers(snapshot.users ?? []);
    renderCalls(snapshot.calls ?? []);
    const message = snapshot.generatedAt
        ? `Live at ${formatTime(snapshot.generatedAt)}`
        : `Updated ${new Date().toLocaleTimeString()}`;
    $lastUpdated.textContent = message;
}

async function refreshAll() {
    setLoading(true);
    try {
        const snapshot = await fetchJson("/api/dashboard");
        applySnapshot(snapshot);
    } catch (error) {
        console.error(error);
        renderStats({totalUsers: 0, onlineUsers: 0, activeCalls: 0, messagesToday: 0});
        $lastUpdated.textContent = "Failed to refresh";
        showError($users, "Unable to load users.");
        showError($calls, "Unable to load calls.");
    } finally {
        setLoading(false);
    }
}

function setLoading(isLoading) {
    $refreshBtn.disabled = isLoading;
    $refreshBtn.textContent = isLoading ? "Refreshing..." : "Refresh Data";
}

function beginStream() {
    if (!supportsEventSource) {
        return;
    }
    if (stream) {
        stream.close();
    }
    stream = new EventSource("/api/stream");
    stream.addEventListener("dashboard", event => {
        const snapshot = JSON.parse(event.data);
        applySnapshot(snapshot);
    });
    stream.onerror = () => {
        $lastUpdated.textContent = "Live stream disconnected. Reconnecting...";
        stream.close();
        setTimeout(beginStream, STREAM_RETRY_MS);
    };
}

$refreshBtn.addEventListener("click", refreshAll);
if (supportsEventSource) {
    beginStream();
    refreshAll();
} else {
    refreshAll();
    setInterval(refreshAll, REFRESH_INTERVAL_MS);
}
