import { useState } from "react";
import { useNavigate } from "react-router-dom";
import type { Menu } from "../api/menu";

interface SidebarProps {
  collapsed: boolean;
  setCollapsed: React.Dispatch<React.SetStateAction<boolean>>;
  menus: Menu[];
}

export default function Sidebar({
  collapsed,
  setCollapsed,
  menus,
}: SidebarProps) {
  const navigate = useNavigate();
  const [openMenu, setOpenMenu] = useState<number | null>(null);

  const parentMenus = menus
    .filter((m) => m.menuLevel === 1)
    .sort((a, b) => a.sortOrder - b.sortOrder);

  const childMenus = menus.filter((m) => m.menuLevel === 2);

  return (
    <div
      style={{
        width: collapsed ? "60px" : "220px",
        background: "#0f172a",
        color: "white",
        transition: "0.3s",
      }}
    >
      {parentMenus.map((parent, index) => (
        <div key={parent.id}>
          <div
            style={{
              padding: "12px 15px",
              cursor: "pointer",
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
            }}
            onClick={() =>
              setOpenMenu(openMenu === parent.id ? null : parent.id)
            }
          >
            {!collapsed && parent.menuName}

            {index === 0 && (
              <span
                onClick={(e) => {
                  e.stopPropagation();
                  setCollapsed(!collapsed);
                }}
                style={{ cursor: "pointer", fontWeight: "bold" }}
              >
                {collapsed ? ">>" : "<<"}
              </span>
            )}
          </div>

          {!collapsed &&
            openMenu === parent.id &&
            childMenus
              .filter((child) => child.parent?.id === parent.id)
              .sort((a, b) => a.sortOrder - b.sortOrder)
              .map((child) => (
                <div
                  key={child.id}
                  style={{
                    paddingLeft: "30px",
                    paddingBottom: "8px",
                    cursor: "pointer",
                  }}
                  onClick={() => navigate(child.menuPath || "/main")}
                >
                  {child.menuName}
                </div>
              ))}
        </div>
      ))}
    </div>
  );
}