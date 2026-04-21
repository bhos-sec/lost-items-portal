import { useState } from "react";
import {
  Box,
  Container,
  Paper,
  Typography,
  TextField,
  Button,
  Alert,
  Stack,
  MenuItem,
} from "@mui/material";
import { Save, Package } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { lostItemsApi } from "../api/lostItemsApi";
import type { LostItemForm } from "../types/lostItem";
import {
  LOST_ITEM_STATUS_LABELS,
  LOST_ITEM_STATUS_OPTIONS,
} from "../types/lostItemStatus";
import { isValidPhoneNumber } from "../utils/phoneValidation";

const Create = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const [formData, setFormData] = useState<LostItemForm>({
    itemName: "",
    itemDesc: "",
    itemLocation: "",
    founderNumber: "",
    status: "STILL_LOOKING",
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

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
      setLoading(true);
      setError(null);

      await lostItemsApi.create(formData);
      setSuccess(true);

      // Redirect to all posts after 1.5 seconds
      setTimeout(() => {
        navigate("/");
      }, 1500);
    } catch (err) {
      setError(
        "Failed to create lost item report. Please check your backend connection.",
      );
      console.error("Error creating lost item:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    navigate("/");
  };

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
          <Box sx={{ display: "flex", alignItems: "center", mb: 3 }}>
            <Package style={{ marginRight: "12px" }} />
            <Typography
              variant="h4"
              component={motion.h1}
              initial={{ opacity: 0, y: 30 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5 }}
            >
              Report Lost Item
            </Typography>
          </Box>

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
              Lost item reported successfully! Redirecting...
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
                disabled={loading}
                placeholder="What did you lose?"
              />
              <TextField
                fullWidth
                label="Description"
                name="itemDesc"
                value={formData.itemDesc}
                onChange={handleChange}
                required
                disabled={loading}
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
                  disabled={loading}
                  placeholder="Where did you lose it?"
                />
                <TextField
                  fullWidth
                  label="Contact with the Founder"
                  name="founderNumber"
                  value={formData.founderNumber}
                  onChange={handleChange}
                  required
                  disabled={loading}
                  placeholder="Your phone number"
                  helperText="Enter a valid phone number"
                />
              </Box>
              <TextField
                select
                fullWidth
                label="Status"
                name="status"
                value={formData.status}
                onChange={handleChange}
                required
                disabled={loading}
              >
                {LOST_ITEM_STATUS_OPTIONS.map((status) => (
                  <MenuItem key={status} value={status}>
                    {LOST_ITEM_STATUS_LABELS[status]}
                  </MenuItem>
                ))}
              </TextField>

              <Box sx={{ display: "flex", gap: 2, justifyContent: "flex-end" }}>
                <Button
                  variant="outlined"
                  onClick={handleCancel}
                  disabled={loading}
                >
                  Cancel
                </Button>
                <Button
                  type="submit"
                  variant="contained"
                  startIcon={<Save size={20} />}
                  disabled={loading}
                >
                  {loading ? "Submitting..." : "Submit Report"}
                </Button>
              </Box>
            </Stack>
          </form>
        </Paper>
      </Box>
    </Container>
  );
};

export default Create;
