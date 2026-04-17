import { useState, useEffect } from "react";
import {
  Box,
  Container,
  Paper,
  Typography,
  Button,
  CircularProgress,
  Alert,
} from "@mui/material";
import { Edit, AlertTriangle, MapPin } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { lostItemsApi } from "../api/lostItemsApi";
import { useAuth } from "../hooks/useAuth";
import type { LostItem } from "../types/lostItem";

const AllPosts = () => {
  const [lostItems, setLostItems] = useState<LostItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();
  const { user } = useAuth();

  useEffect(() => {
    fetchLostItems();
  }, []);

  // Fetch all lost items from the backend
  const fetchLostItems = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await lostItemsApi.getAll();
      setLostItems(data);
    } catch (err) {
      setError(
        "Failed to load lost items. Please check your backend connection.",
      );
      console.error("Error fetching lost items:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (itemId: number) => {
    navigate(`/edit?id=${itemId}`);
  };

  if (loading) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4, textAlign: "center" }}>
        <CircularProgress />
        <Typography variant="body1" sx={{ mt: 2 }}>
          Loading lost items...
        </Typography>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg">
      <Box sx={{ py: 4 }}>
        <Typography
          variant="h4"
          component={motion.h1}
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          gutterBottom
          sx={{ mb: 3 }}
        >
          Lost Item Reports
        </Typography>

        {error && (
          <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError(null)}>
            {error}
          </Alert>
        )}

        {lostItems.length === 0 ? (
          <Paper
            component={motion.div}
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.5 }}
            elevation={2}
            sx={{ p: 4, textAlign: "center" }}
          >
            <AlertTriangle
              size={48}
              style={{ color: "#999", marginBottom: "16px" }}
            />
            <Typography variant="h6" color="text.secondary">
              No lost items reported yet
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              Click "Report Lost Item" to create a new report
            </Typography>
          </Paper>
        ) : (
          <Box
            sx={{
              display: "grid",
              gridTemplateColumns: { xs: "1fr", md: "repeat(2, 1fr)" },
              gap: 3,
            }}
          >
            {lostItems.map((item, index) => (
              <Paper
                component={motion.div}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.4, delay: index * 0.1 }}
                elevation={3}
                sx={{ p: 3, height: "100%" }}
                key={item.itemId}
              >
                <Box
                  sx={{
                    display: "flex",
                    justifyContent: "space-between",
                    mb: 2,
                  }}
                >
                  <Typography variant="h6" component="h2">
                    {item.itemName}
                  </Typography>
                  <Button
                    size="small"
                    startIcon={<Edit size={16} />}
                    onClick={() => handleEdit(item.itemId)}
                    disabled={user?.userId !== item.createdByUserId}
                  >
                    Edit
                  </Button>
                </Box>

                <Typography
                  variant="body2"
                  color="text.secondary"
                  sx={{ mb: 2 }}
                >
                  {item.itemDesc}
                </Typography>

                <Box
                  sx={{ display: "flex", flexDirection: "column", gap: 0.5 }}
                >
                  <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                    <MapPin size={16} color="#666" />
                    <Typography variant="body2" color="text.secondary">
                      {item.itemLocation}
                    </Typography>
                  </Box>
                  <Typography
                    variant="caption"
                    color="text.secondary"
                    sx={{ mt: 1 }}
                  >
                    Contact: {item.founderNumber}
                  </Typography>
                </Box>
              </Paper>
            ))}
          </Box>
        )}
      </Box>
    </Container>
  );
};

export default AllPosts;
