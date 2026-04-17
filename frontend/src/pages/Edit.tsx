import { useState, useEffect, useCallback } from "react";
import {
  Box,
  Container,
  Paper,
  Typography,
  TextField,
  Button,
  Alert,
  CircularProgress,
  Stack,
} from "@mui/material";
import { Save, Trash2 } from "lucide-react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { motion } from "framer-motion";
import { lostItemsApi } from "../api/lostItemsApi";
import { useAuth } from "../hooks/useAuth";
import type { LostItemForm } from "../types/lostItem";
import { isValidPhoneNumber } from "../utils/phoneValidation";

const Edit = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const itemId = searchParams.get("id");
  const { user } = useAuth();

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const [formData, setFormData] = useState<LostItemForm>({
    itemName: "",
    itemDesc: "",
    itemLocation: "",
    founderNumber: "",
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };
  // Fetch the lost item by ID
  const fetchLostItem = useCallback(async () => {
    if (!itemId) {
      setError("No item ID provided");
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const item = await lostItemsApi.getById(itemId);
      if (user && item.createdByUserId !== user.userId) {
        setError("You do not have permission to edit this item.");
        return;
      }

      setFormData({
        itemName: item.itemName,
        itemDesc: item.itemDesc,
        itemLocation: item.itemLocation,
        founderNumber: item.founderNumber,
      });
    } catch (err) {
      setError(
        "Failed to load lost item. Please check your backend connection.",
      );
      console.error("Error fetching lost item:", err);
    } finally {
      setLoading(false);
    }
  }, [itemId, user]);

  useEffect(() => {
    fetchLostItem();
  }, [fetchLostItem]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!itemId) {
      setError("No item ID provided");
      return;
    }

    // Validation
    if (
      !formData.itemName ||
      !formData.itemDesc ||
      !formData.itemLocation ||
      !formData.founderNumber
    ) {
      setError("Please fill in all required fields");
      return;
    }

    // Validate phone number
    if (!isValidPhoneNumber(formData.founderNumber)) {
      setError("Please enter a valid phone number");
      return;
    }

    try {
      setSaving(true);
      setError(null);

      await lostItemsApi.update(itemId, formData);
      setSuccess(true);

      // Redirect to all posts after 1.5 seconds
      setTimeout(() => {
        navigate("/");
      }, 1500);
    } catch (err) {
      setError(
        "Failed to update lost item. Please check your backend connection.",
      );
      console.error("Error updating lost item:", err);
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!itemId) {
      setError("No item ID provided");
      return;
    }

    if (
      !window.confirm("Are you sure you want to delete this lost item report?")
    ) {
      return;
    }

    try {
      setSaving(true);
      setError(null);

      await lostItemsApi.remove(itemId);
      setSuccess(true);

      // Redirect to all posts after 1 second
      setTimeout(() => {
        navigate("/");
      }, 1000);
    } catch (err) {
      setError(
        "Failed to delete lost item. Please check your backend connection.",
      );
      console.error("Error deleting lost item:", err);
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    navigate("/");
  };

  if (loading) {
    return (
      <Container maxWidth="md" sx={{ mt: 4, textAlign: "center" }}>
        <CircularProgress />
        <Typography variant="body1" sx={{ mt: 2 }}>
          Loading lost item...
        </Typography>
      </Container>
    );
  }

  if (!itemId) {
    return (
      <Container maxWidth="md" sx={{ mt: 4 }}>
        <Alert severity="error">
          No item ID provided. Please select a lost item from the list.
        </Alert>
        <Button onClick={() => navigate("/")} sx={{ mt: 2 }}>
          Back to All Posts
        </Button>
      </Container>
    );
  }

  return (
    <Container maxWidth="md">
      <Box sx={{ py: 4 }}>
        <Paper
          component={motion.div}
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.5 }}
          elevation={3}
          sx={{ p: 4 }}
        >
          <Typography
            variant="h4"
            component={motion.h1}
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            gutterBottom
            sx={{ mb: 3 }}
          >
            Edit Lost Item Report
          </Typography>

          {error && (
            <Alert
              severity="error"
              sx={{ mb: 3 }}
              onClose={() => setError(null)}
            >
              {error}
            </Alert>
          )}

          {success && (
            <Alert severity="success" sx={{ mb: 3 }}>
              Changes saved successfully! Redirecting...
            </Alert>
          )}

          <form onSubmit={handleSubmit}>
            <Stack spacing={3}>
              <TextField
                fullWidth
                label="Item Name"
                name="itemName"
                value={formData.itemName}
                onChange={handleChange}
                required
                disabled={saving}
                placeholder="What did you lose?"
              />
              <TextField
                fullWidth
                label="Description"
                name="itemDesc"
                value={formData.itemDesc}
                onChange={handleChange}
                required
                disabled={saving}
                multiline
                rows={4}
                placeholder="Provide detailed information about the lost item"
              />

              <Box
                sx={{
                  display: "grid",
                  gridTemplateColumns: { xs: "1fr", sm: "1fr 1fr" },
                  gap: 3,
                }}
              >
                <TextField
                  fullWidth
                  label="Location"
                  name="itemLocation"
                  value={formData.itemLocation}
                  onChange={handleChange}
                  required
                  disabled={saving}
                  placeholder="Where did you lose it?"
                />
                <TextField
                  fullWidth
                  label="Contact with the Founder"
                  name="founderNumber"
                  value={formData.founderNumber}
                  onChange={handleChange}
                  required
                  disabled={saving}
                  placeholder="Your phone number"
                  helperText="Enter a valid phone number"
                />
              </Box>

              <Box
                sx={{
                  display: "flex",
                  gap: 2,
                  justifyContent: "space-between",
                }}
              >
                <Button
                  variant="outlined"
                  color="error"
                  startIcon={<Trash2 size={20} />}
                  onClick={handleDelete}
                  disabled={saving}
                >
                  Delete
                </Button>
                <Box sx={{ display: "flex", gap: 2 }}>
                  <Button
                    variant="outlined"
                    onClick={handleCancel}
                    disabled={saving}
                  >
                    Cancel
                  </Button>
                  <Button
                    type="submit"
                    variant="contained"
                    startIcon={<Save size={20} />}
                    disabled={saving}
                  >
                    {saving ? "Saving..." : "Save Changes"}
                  </Button>
                </Box>
              </Box>
            </Stack>
          </form>
        </Paper>
      </Box>
    </Container>
  );
};

export default Edit;
