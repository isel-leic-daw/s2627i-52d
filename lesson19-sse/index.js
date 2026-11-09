import http from "http";

const PORT = 4000;
const server = http.createServer(serverHandler);

server.on("error", (error) => {
  console.error("HTTP server error:", error);
});

server.listen(PORT, () => {
  console.log("Listening on port 4000");
});

let connectionCounter = 1;

function serverHandler(req, resp) {
  const requestUrl = new URL(req.url, `http://${req.headers.host ?? "localhost"}`);

  if (requestUrl.pathname === "/" && req.method === "GET") {
    resp.writeHead(200, { "Content-Type": "text/html; charset=utf-8" });
    resp.end(`<html>
              Navigate to <a href="/sse/listen">/sse/listen</a>
              to start receiving events.
              </html>`);
    return;
  }

  if (requestUrl.pathname === "/sse/listen") {
    if (req.method !== "GET") {
      resp.writeHead(405, {
        Allow: "GET",
        "Content-Type": "text/plain; charset=utf-8",
      });
      resp.end("Method Not Allowed");
      return;
    }

    const lastEventId = parseLastEventId(req.headers["last-event-id"]);
    console.log(`Client connected to event stream:
      - connection #${connectionCounter}
      - Last-Event-ID: ${lastEventId ?? "not provided"}`);
    emitSse(connectionCounter++, req, resp);
    return;
  }

  resp.writeHead(404, { "Content-Type": "text/plain; charset=utf-8" });
  resp.end("Resource not found");
}

function parseLastEventId(headerValue) {
  if (typeof headerValue !== "string" || !/^\d+$/.test(headerValue)) {
    return null;
  }

  const eventId = Number(headerValue);
  return Number.isSafeInteger(eventId) ? eventId : null;
}

function emitSse(connId, req, resp) {
  resp.writeHead(200, {
    "Content-Type": "text/event-stream",
    "Cache-Control": "no-cache", // let intermediaries know to not cache anything
    "Access-Control-Allow-Origin": "*",
  });

  let eventCounter = 1;
  const emitter = setInterval(() => {
    if (resp.destroyed || resp.writableEnded) return;

    // We ignore possible ID overlaps between connections after 999 events.
    const eventId = connId * 1000 + eventCounter;
    resp.write(
      `event: my-custom-event\n` +
      `id: ${eventId}\n` +
      `data: Server says hi! (event #${eventCounter} of connection #${connId})\n\n`,
    );
    eventCounter++;
  }, 1000);

  const cleanup = () => {
    clearInterval(emitter);
    console.log(`Client disconnected (connection #${connId})`);
  };

  req.on("aborted", cleanup);
  resp.on("close", cleanup);
  resp.on("error", cleanup);
}
