import * as React from "react";
import { useEffect, useState } from "react";
import * as ReactDOM from "react-dom/client";

function FetchAndShow({ uri }: { uri: string }) {
  const [isComplete, setComplete] = useState(false);
  const [respBody, setRespBody] = useState("");
  useEffect(() => {
    let ignore = false;
    fetch(uri)
      .then((resp) => delay(5000).then(() => resp.text()))
      .then((body) => {
        if (!ignore) {
          setRespBody(body);
          setComplete(true);
        }
      });
    return () => {
      ignore = true;
    };
  }, [uri]);

  return (
    <div>
      {isComplete ? "DONE" : "FETCHING"} {uri}
      <hr></hr>
      <textarea rows={20} cols={40} value={respBody} readOnly></textarea>
    </div>
  );
}

function ReadAndFetch() {
  const [inputUri, setInputUri] = useState("");
  const [fetchUri, setFetchUri] = useState(
    "https://v2.jokeapi.dev/joke/Programming",
  );
  return (
    <>
      <div>
        URI:
        <input
          type="text"
          value={inputUri}
          onChange={(e) => setInputUri(e.target.value)}
        ></input>
        <button onClick={() => setFetchUri(inputUri)}>Submit</button>
      </div>
      <hr></hr>
      <FetchAndShow uri={fetchUri}></FetchAndShow>
    </>
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
root.render(<ReadAndFetch></ReadAndFetch>);
