import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import {
  AppBar,
  Toolbar,
  Typography,
  Button,
  Box,
  Menu,
  MenuItem,
} from '@mui/material';
import { AccountCircle } from '@mui/icons-material';
import { useAuth } from '../../hooks/useAuth';
import ChangePasswordModal from '../modals/ChangePasswordModal';

export default function TopNavbar() {
  const { user, isAdmin, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [passwordModalOpen, setPasswordModalOpen] = useState(false);

  const handleMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = () => {
    handleClose();
    logout();
    navigate('/login');
  };

  const handleChangePassword = () => {
    handleClose();
    setPasswordModalOpen(true);
  };

  const isActive = (path: string) => location.pathname === path;

  const adminLinks = [
    { path: '/employees', label: 'Pracownicy' },
    { path: '/assets', label: 'Zasoby' },
    { path: '/assignments', label: 'Przydziały' },
  ];

  const employeeLinks = [
    { path: '/my-assets', label: 'Moje zasoby' },
    { path: '/my-history', label: 'Historia przydziałów' },
  ];

  const links = isAdmin ? adminLinks : employeeLinks;

  return (
    <>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" component="div" sx={{ mr: 4 }}>
            Asset Management
          </Typography>

          <Box sx={{ flexGrow: 1, display: 'flex', gap: 1 }}>
            {links.map((link) => (
              <Button
                key={link.path}
                color="inherit"
                onClick={() => navigate(link.path)}
                sx={{
                  backgroundColor: isActive(link.path)
                    ? 'rgba(255,255,255,0.1)'
                    : 'transparent',
                }}
              >
                {link.label}
              </Button>
            ))}
          </Box>

          <Box>
            <Button
              color="inherit"
              onClick={handleMenu}
              startIcon={<AccountCircle />}
            >
              {user?.email}
            </Button>
            <Menu
              anchorEl={anchorEl}
              open={Boolean(anchorEl)}
              onClose={handleClose}
            >
              <MenuItem onClick={handleChangePassword}>Zmień hasło</MenuItem>
              <MenuItem onClick={handleLogout}>Wyloguj</MenuItem>
            </Menu>
          </Box>
        </Toolbar>
      </AppBar>

      <ChangePasswordModal
        open={passwordModalOpen}
        onClose={() => setPasswordModalOpen(false)}
      />
    </>
  );
}
