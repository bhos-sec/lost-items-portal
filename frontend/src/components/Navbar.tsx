import { AppBar, Toolbar, Typography, Button, Box } from "@mui/material";
import { Package, Plus, List } from "lucide-react";
import { Link } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";

const Navbar = () => {
  const { isAuthenticated, user, logout } = useAuth();

  const handleLogout = async () => {
    await logout();
  };

  return (
    <AppBar position="sticky" sx={{ top: 0, zIndex: 1100 }}>
      <Toolbar>
        <Package style={{ marginRight: "12px" }} />
        <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
          Campus Lost Item Reporter
        </Typography>
        <Box sx={{ display: "flex", gap: 1 }}>
          <Button
            color="inherit"
            component={Link}
            to="/"
            startIcon={<List size={20} />}
          >
            All Posts
          </Button>
          <Button
            color="inherit"
            component={Link}
            to="/create"
            startIcon={<Plus size={20} />}
          >
            Report Lost Item
          </Button>
          {!isAuthenticated ? (
            <>
              <Button color="inherit" component={Link} to="/login">
                Login
              </Button>
              <Button color="inherit" component={Link} to="/register">
                Register
              </Button>
            </>
          ) : (
            <>
              <Typography variant="body2" sx={{ alignSelf: "center", px: 1 }}>
                {user?.email}
              </Typography>
              <Button color="inherit" onClick={handleLogout}>
                Logout
              </Button>
            </>
          )}
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default Navbar;
