import * as React from "react";
import { useState } from "react";
import * as ReactDOM from "react-dom/client";

function Wacky() {
  const [exchange, setExchange] = useState(false);
  let count: [number, (arg: any) => void];
  let msg: [string, (arg: any) => void];
  /**
   * WRONG
   * DON'T use a CONDITIONAL order between several uses of useSate()!!!!
   */
  if (exchange) {
    count = useState(0);
    msg = useState("isel");
  } else {
    msg = useState("isel");
    count = useState(0);
  }

  function submitHandler(): void {
    setExchange(!exchange)
    count[1](count[0] + 1)
    msg[1](msg[0].split("").reverse().join(""))
  }

  return (
    <>
      Count: {count[0]}
      <br></br>
      Message: {msg[0]}
      <br></br>
      <button onClick={submitHandler}>Submit</button>
    </>
  );
}

const root = ReactDOM.createRoot(document.getElementById("container")!);
root.render(
  <>
    <Wacky></Wacky>
  </>,
);
