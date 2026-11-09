import { useEffect, useState } from "react";
import { createRoot } from "react-dom/client";

const SSE_URL = "https://httpbun.com/sse" // Using the httpbun.com test server
// const SSE_URL = "http://localhost:4000/sse/listen"; // Using the local demo

const READY_STATE_MAP = ["CONNECTING", "OPEN", "CLOSED"];

// "error" and "open" events don't carry any relevant information
// "message" carries the event type, data, and last id
type HandledEvent =
  | { kind: "error"; readyState: string }
  | { kind: "open" }
  | { kind: "message"; type: string; data: string; lastEventId: string };

const root = createRoot(document.getElementById("container")!);
root.render(<App></App>)

export function App() {
  const [events, setEvents] = useState<Array<HandledEvent>>([]);
  useEffect(() => {
    const eventSource = new EventSource(SSE_URL);

    eventSource.addEventListener("error", () => {
      setEvents((it) => [
        ...it,
        {
          kind: "error",
          readyState: READY_STATE_MAP[eventSource.readyState] || "unknown",
        },
      ]);
    });

    eventSource.addEventListener("open", () => {
      setEvents((it) => [...it, { kind: "open" }]);
    });

    function handleMessageEvent(ev: MessageEvent) {
      setEvents((it) => [
        ...it,
        {
          kind: "message",
          type: ev.type,
          data: ev.data,
          lastEventId: ev.lastEventId,
        },
      ]);
    }
    eventSource.addEventListener("foo", handleMessageEvent);
    eventSource.addEventListener("ping", handleMessageEvent);
    
    return () => {
      eventSource.close();
    };
  }, []);

  return (
    <div>
      {events.map((it, ix) => (
        <p key={ix}>{JSON.stringify(it)}</p>
      ))}
    </div>
  );
}
