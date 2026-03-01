import { useState, useEffect } from "react";
import Sidebar from "./Sidebar";
import Header from "./Header";
import Footer from "./Footer";
import { getMyMenus } from "../api/menu";
import type { Menu } from "../api/menu";

export default function Layout({ children }: { children: React.ReactNode }) {
  const [collapsed, setCollapsed] = useState(false);
  const [menus, setMenus] = useState<Menu[]>([]);

  useEffect(() => {
    getMyMenus()
      .then((data) => {
        console.log("메뉴 데이터:", data);
        setMenus(data || []);
      })
      .catch((err) => {
        console.error("메뉴 조회 실패", err);
      });
  }, []);

  return (
    <div style={{ height: "100vh", display: "flex", flexDirection: "column" }}>
      <Header menus={menus} />

      <div style={{ flex: 1, display: "flex", marginTop: "60px" }}>
        <Sidebar
          collapsed={collapsed}
          setCollapsed={setCollapsed}
          menus={menus}
        />

        <div
          style={{
            flex: 1,
            padding: "20px",
            background: "#f3f4f6",
            overflow: "auto",
          }}
        >
          {children}
        </div>
      </div>

      <Footer />
    </div>
  );
}