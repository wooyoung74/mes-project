import api from "./axios";

export interface Menu {
  id: number;
  menuName: string;
  menuCode: string;
  menuLevel: number;
  parent: { id: number } | null;
  menuPath: string | null;
  sortOrder: number;
}

export const getMyMenus = async (): Promise<Menu[]> => {
  const response = await api.get<Menu[]>("/api/menus/my");
  return response.data;
};