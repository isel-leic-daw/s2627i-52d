import * as React from "react";
import {
  createContext,
  ReactNode,
  useContext,
  useEffect,
  useState,
} from "react";
import * as ReactDOM from "react-dom/client";

type ThemeStatus = "light" | "dark";

type Theme = {
  status: ThemeStatus;
  setTheme: (theme: ThemeStatus) => void;
};

const ContextTheme = createContext<Theme>({
  status: "light",
  setTheme: () => {
    throw new Error("Unsupported operation!");
  },
});

function Main() {
  const [theme, setTheme] = useState<ThemeStatus>("light");
  return (
    <ContextTheme value={{ status: theme, setTheme }}>
      <div>
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
  const theme = useContext(ContextTheme);
  return (
    <fieldset>
      <button onClick={() => theme.setTheme("light")}>light</button>
      <button onClick={() => theme.setTheme("dark")}>dark</button>
      <hr></hr>
      <legend>Panel</legend>
      {children}
      <Counter></Counter>
    </fieldset>
  );
}

function Counter() {
  const [count, setCount] = useState(0);
  const theme = useContext(ContextTheme);
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
      Theme: {theme.status}
    </>
  );
}

const root = ReactDOM.createRoot(document.getElementById("container")!);
root.render(<Main></Main>);
