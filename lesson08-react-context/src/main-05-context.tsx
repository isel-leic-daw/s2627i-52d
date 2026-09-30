import * as React from "react";
import {
  createContext,
  ReactNode,
  useContext,
  useEffect,
  useState,
} from "react";
import * as ReactDOM from "react-dom/client";

type Theme = "light" | "dark";

const ContextTheme = createContext<Theme>("light");

function Main() {
  const [theme, setTheme] = useState<Theme>("light")
  return (
    <ContextTheme value={theme}>
      <div>
        <button onClick={() => setTheme("light")}>light</button>
        <button onClick={() => setTheme("dark")}>dark</button>
        Main Content
        <hr></hr>
        <Panel>
          <h3>ISEL LEIC</h3>
          Main courses:
          <ul>
            <li>TDS</li>
            <li>LAE</li>
            <li>PC</li>
          </ul>
        </Panel>
      </div>
    </ContextTheme>
  );
}

function Panel({ children }: { children: ReactNode }) {
  return (
    <fieldset>
      <legend>Panel</legend>
      {children}
      <Counter></Counter>
    </fieldset>
  );
}

function Counter() {
  const [count, setCount] = useState(0);
  const theme = useContext(ContextTheme)
  function decHandler(): void {
    setCount(count - 1);
  }
  function incHandler(): void {
    setCount(count + 1);
  }

  return (
    <>
      <button onClick={decHandler}>-</button>
      {count}
      <button onClick={incHandler}>+</button>
      Theme: {theme}
    </>
  );
}

const root = ReactDOM.createRoot(document.getElementById("container")!);
root.render(<Main></Main>);
