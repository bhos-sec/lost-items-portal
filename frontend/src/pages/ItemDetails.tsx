import { useCallback, useEffect, useState } from "react";
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Container,
  Divider,
  MenuItem,
  Paper,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import { ArrowLeft, Save } from "lucide-react";
import { useNavigate, useParams } from "react-router-dom";
import { lostItemsApi } from "../api/lostItemsApi";
import { useAuth } from "../hooks/useAuth";
import type { LostItemDetails, LostItemStatus } from "../types/lostItem";
import {
  LOST_ITEM_STATUS_LABELS,
  LOST_ITEM_STATUS_OPTIONS,
} from "../types/lostItemStatus";

const ItemDetails = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const { user } = useAuth();

  const [details, setDetails] = useState<LostItemDetails | null>(null);
  const [selectedStatus, setSelectedStatus] =
    useState<LostItemStatus>("STILL_LOOKING");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const fetchDetails = useCallback(async () => {
    if (!id) {
      setError("No item ID provided");
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const response = await lostItemsApi.getDetails(id);
      setDetails(response);
      setSelectedStatus(response.item.status);
    } catch (err) {
      console.error("Error fetching item details:", err);
      setError("Failed to load item details.");
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchDetails();
  }, [fetchDetails]);

  const canUpdateStatus =
    !!user &&
    !!details &&
    (user.userId === details.item.createdByUserId || user.role === "ADMIN");

  const handleStatusSave = async () => {
    if (!id || !details) {
      return;
    }
    try {
      setSaving(true);
      setError(null);
      setSuccess(null);
      await lostItemsApi.updateStatus(id, selectedStatus);
      await fetchDetails();
      setSuccess("Status updated successfully.");
    } catch (err) {
      console.error("Error updating status:", err);
      setError("Failed to update status.");
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <Container maxWidth="md" sx={{ mt: 4, textAlign: "center" }}>
        <CircularProgress />
        <Typography variant="body1" sx={{ mt: 2 }}>
          Loading item details...
        </Typography>
      </Container>
    );
  }

  if (!details) {
    return (
      <Container maxWidth="md" sx={{ mt: 4 }}>
        <Alert severity="error">{error || "Item details not found."}</Alert>
        <Button startIcon={<ArrowLeft size={16} />} sx={{ mt: 2 }} onClick={() => navigate("/")}>
          Back to all posts
        </Button>
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Paper elevation={3} sx={{ p: 4 }}>
        <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
          <Typography variant="h4">{details.item.itemName}</Typography>
          <Chip label={LOST_ITEM_STATUS_LABELS[details.item.status]} color={details.item.status === "CLAIMED" ? "success" : "default"} />
        </Stack>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
            {error}
          </Alert>
        )}
        {success && (
          <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess(null)}>
            {success}
          </Alert>
        )}

        <Typography variant="body1" sx={{ mb: 1 }}>
          {details.item.itemDesc}
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Location: {details.item.itemLocation}
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          Contact: {details.item.founderNumber}
        </Typography>

        <Divider sx={{ mb: 3 }} />

        <Typography variant="h6" sx={{ mb: 2 }}>
          Update status
        </Typography>
        <Stack direction={{ xs: "column", sm: "row" }} spacing={2} sx={{ mb: 3 }}>
          <TextField
            select
            label="Status"
            value={selectedStatus}
            onChange={(e) => setSelectedStatus(e.target.value as LostItemStatus)}
            disabled={!canUpdateStatus || saving}
            sx={{ minWidth: 240 }}
          >
            {LOST_ITEM_STATUS_OPTIONS.map((status) => (
              <MenuItem key={status} value={status}>
                {LOST_ITEM_STATUS_LABELS[status]}
              </MenuItem>
            ))}
          </TextField>
          <Button
            variant="contained"
            startIcon={<Save size={18} />}
            onClick={handleStatusSave}
            disabled={!canUpdateStatus || saving || selectedStatus === details.item.status}
          >
            {saving ? "Saving..." : "Save status"}
          </Button>
        </Stack>
        {!canUpdateStatus && (
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            Only the item owner or an admin can update status.
          </Typography>
        )}

        <Typography variant="h6" sx={{ mb: 2 }}>
          Status activity log
        </Typography>
        {details.statusHistory.length === 0 ? (
          <Typography variant="body2" color="text.secondary">
            No status updates yet.
          </Typography>
        ) : (
          <Stack spacing={1}>
            {details.statusHistory.map((entry) => (
              <Box key={entry.statusChangeId} sx={{ p: 1.5, border: "1px solid #eee", borderRadius: 1 }}>
                <Typography variant="body2">
                  {entry.fromStatus ? LOST_ITEM_STATUS_LABELS[entry.fromStatus] : "Created"}{" "}
                  {"->"} {LOST_ITEM_STATUS_LABELS[entry.toStatus]}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  by user #{entry.changedByUserId} on {new Date(entry.changedAt).toLocaleString()}
                </Typography>
              </Box>
            ))}
          </Stack>
        )}

        <Button
          sx={{ mt: 3 }}
          startIcon={<ArrowLeft size={16} />}
          onClick={() => navigate("/")}
        >
          Back to all posts
        </Button>
      </Paper>
    </Container>
  );
};

export default ItemDetails;
