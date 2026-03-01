import { Routes, Route } from "react-router-dom";
import LoginPage from "../pages/LoginPage";
import MainPage from "../pages/MainPage";
import ProtectedRoute from "./ProtectedRoute";
import Layout from "../components/Layout";

export default function Router() {
  return (
    <Routes>
      <Route path="/" element={<LoginPage />} />

      <Route
        path="/main"
        element={
          <ProtectedRoute>
            <Layout>
              <MainPage />
            </Layout>
          </ProtectedRoute>
        }
      />
    </Routes>
  );
}