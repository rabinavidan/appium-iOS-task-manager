const http = require("http");
const { randomUUID } = require("crypto");

const port = Number(process.env.MOCK_API_PORT || 8081);
const tasks = new Map();

function sendJson(response, statusCode, body) {
  const payload = JSON.stringify(body);
  response.writeHead(statusCode, {
    "Content-Type": "application/json",
    "Content-Length": Buffer.byteLength(payload)
  });
  response.end(payload);
}

function readJson(request) {
  return new Promise((resolve, reject) => {
    let body = "";
    request.on("data", chunk => {
      body += chunk;
    });
    request.on("end", () => {
      if (!body) {
        resolve({});
        return;
      }

      try {
        resolve(JSON.parse(body));
      } catch (error) {
        reject(error);
      }
    });
    request.on("error", reject);
  });
}

function taskResponse(task) {
  return {
    id: task.id,
    title: task.title,
    notes: task.notes,
    priority: task.priority,
    dueDate: task.dueDate,
    isCompleted: task.isCompleted,
    createdAt: task.createdAt
  };
}

function today() {
  return new Date().toISOString().slice(0, 10);
}

function createTask(payload) {
  const title = String(payload.title || "").trim();
  if (!title) {
    return { error: "Task title is required" };
  }

  const task = {
    id: randomUUID(),
    title,
    notes: String(payload.notes || "").trim(),
    priority: payload.priority || "Medium",
    dueDate: payload.dueDate || today(),
    isCompleted: Boolean(payload.isCompleted),
    createdAt: new Date().toISOString()
  };

  tasks.set(task.id, task);
  return task;
}

async function handleRequest(request, response) {
  const url = new URL(request.url, `http://${request.headers.host}`);
  const pathParts = url.pathname.split("/").filter(Boolean);

  try {
    if (request.method === "GET" && url.pathname === "/health") {
      sendJson(response, 200, { status: "ok", service: "task-manager-mock-api" });
      return;
    }

    if (url.pathname === "/tasks" && request.method === "GET") {
      sendJson(response, 200, Array.from(tasks.values()).map(taskResponse));
      return;
    }

    if (url.pathname === "/tasks" && request.method === "POST") {
      const result = createTask(await readJson(request));
      if (result.error) {
        sendJson(response, 400, result);
        return;
      }

      sendJson(response, 201, taskResponse(result));
      return;
    }

    if (url.pathname === "/tasks" && request.method === "DELETE") {
      tasks.clear();
      sendJson(response, 200, { deleted: "all" });
      return;
    }

    if (pathParts[0] === "tasks" && pathParts.length === 2) {
      const task = tasks.get(pathParts[1]);
      if (!task) {
        sendJson(response, 404, { error: "Task not found" });
        return;
      }

      if (request.method === "GET") {
        sendJson(response, 200, taskResponse(task));
        return;
      }

      if (request.method === "PATCH") {
        const payload = await readJson(request);
        if (Object.prototype.hasOwnProperty.call(payload, "isCompleted")) {
          task.isCompleted = Boolean(payload.isCompleted);
        }
        if (payload.priority) {
          task.priority = payload.priority;
        }
        if (payload.notes !== undefined) {
          task.notes = String(payload.notes).trim();
        }

        sendJson(response, 200, taskResponse(task));
        return;
      }

      if (request.method === "DELETE") {
        tasks.delete(task.id);
        sendJson(response, 200, { deleted: task.id });
        return;
      }
    }

    sendJson(response, 404, { error: "Route not found" });
  } catch (error) {
    sendJson(response, 400, { error: "Invalid request body" });
  }
}

const server = http.createServer(handleRequest);

server.listen(port, "127.0.0.1", () => {
  console.log(`Mock API listening on http://127.0.0.1:${port}`);
});

process.on("SIGTERM", () => {
  server.close(() => process.exit(0));
});

