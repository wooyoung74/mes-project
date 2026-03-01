import { useState } from "react";
import { useNavigate } from "react-router-dom";
import type { Menu } from "../api/menu";

interface HeaderProps {
  menus: Menu[];
}

export default function Header({ menus }: HeaderProps) {
  const navigate = useNavigate();
  const [openMenu, setOpenMenu] = useState<number | null>(null);

  const parentMenus = menus
    .filter((m) => m.menuLevel === 1)
    .sort((a, b) => a.sortOrder - b.sortOrder);

  const childMenus = menus.filter((m) => m.menuLevel === 2);

  const logout = () => {
    localStorage.removeItem("accessToken");
    navigate("/");
  };

  return (
    <div
      style={{
        position: "fixed",
        top: 0,
        left: 0,
        right: 0,
        height: "60px",
        background: "#1f2937",
        color: "white",
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        padding: "0 30px",
        zIndex: 1000,
      }}
    >
      {/* 좌측 영역 */}
      <div style={{ display: "flex", alignItems: "center", gap: "40px" }}>
        <div
          style={{ fontWeight: "bold", cursor: "pointer", fontSize: "18px" }}
          onClick={() => navigate("/main")}
        >
          WOOYOUNG MES
        </div>

        {/* 🔥 상단 대메뉴 영역 */}
        <div style={{ display: "flex", gap: "30px" }}>
          {parentMenus.map((parent) => {
            const children = childMenus
              .filter((child) => child.parent?.id === parent.id)
              .sort((a, b) => a.sortOrder - b.sortOrder);

            return (
              <div
                key={parent.id}
                style={{
                  position: "relative",
                  height: "60px",
                  display: "flex",
                  alignItems: "center",
                }}
                onMouseEnter={() => setOpenMenu(parent.id)}
                onMouseLeave={() => setOpenMenu(null)}
              >
                {/* 대메뉴 */}
                <div style={{ cursor: "pointer" }}>
                  {parent.menuName}
                </div>

                {/* 🔥 드롭다운 */}
                {openMenu === parent.id && children.length > 0 && (
                  <div
                    style={{
                      position: "absolute",
                      top: "60px", // 🔥 헤더 바로 아래 딱 붙임
                      left: 0,
                      background: "#1f2937",
                      color: "white",
                      minWidth: "180px",
                      borderRadius: "0 0 6px 6px",
                      boxShadow: "0 6px 14px rgba(0,0,0,0.4)",
                      padding: "8px 0",
                    }}
                  >
                    {children.map((child) => (
                      <div
                        key={child.id}
                        style={{
                          padding: "10px 16px",
                          cursor: "pointer",
                          whiteSpace: "nowrap",
                        }}
                        onClick={() => navigate(child.menuPath || "/main")}
                        onMouseEnter={(e) =>
                          (e.currentTarget.style.background = "#374151")
                        }
                        onMouseLeave={(e) =>
                          (e.currentTarget.style.background = "transparent")
                        }
                      >
                        {child.menuName}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>

      {/* 우측 사용자 */}
      <div>
        master |{" "}
        <span
          onClick={logout}
          style={{ cursor: "pointer", color: "#f87171" }}
        >
          로그아웃
        </span>
      </div>
    </div>
  );
}