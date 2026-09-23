import * as React from "react";
import { useState } from "react";
import * as ReactDOM from "react-dom/client";

function FetchAndShow({ uri }: { uri: string }) {
  const [isComplete, setComplete] = useState(false);
  const [respBody, setRespBody] = useState("");
  // DON'T do this => do not generate side-effects inside a Component definition
  fetch(uri)
    .then((resp) => delay(1000).then(() => resp.text()))
    .then((body) => {
      setRespBody(body);
      setComplete(true);
    });

  return (
    <div>
      {isComplete ? "DONE" : "FETCHING"} {uri}
      <hr></hr>
      <textarea rows={20} cols={40} value={respBody} readOnly></textarea>
    </div>
  );
}

/**
 * Returns a Promise that id fulfilled after a delay of ms.
 * @param ms timeout
 */
function delay(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

const root = ReactDOM.createRoot(document.getElementById("container")!);
root.render(
  <FetchAndShow uri="https://v2.jokeapi.dev/joke/Programming"></FetchAndShow>,
);
