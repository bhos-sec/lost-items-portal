import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { CssBaseline, Box } from "@mui/material";
import "./index.css";
import Navbar from "./components/Navbar";
import ProtectedRoute from "./components/ProtectedRoute";
import { AuthProvider } from "./context/AuthContext";
import AllPosts from "./pages/AllPosts";
import Create from "./pages/Create";
import Edit from "./pages/Edit";
import Login from "./pages/Login";
import Register from "./pages/Register";
import ItemDetails from "./pages/ItemDetails";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <AuthProvider>
      <BrowserRouter>
        <CssBaseline />
        <Navbar />
        <Box
          sx={{
            width: "100%",
            minHeight: "calc(100vh - 64px)",
          }}
        >
          <Routes>
            <Route path="/" element={<AllPosts />} />
            <Route path="/items/:id" element={<ItemDetails />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route element={<ProtectedRoute />}>
              <Route path="/create" element={<Create />} />
              <Route path="/edit" element={<Edit />} />
            </Route>
          </Routes>
        </Box>
      </BrowserRouter>
    </AuthProvider>
  </StrictMode>,
);
