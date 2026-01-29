import { FormControl, InputLabel, Select, MenuItem, IconButton, Box } from '@mui/material';
import { Clear } from '@mui/icons-material';

interface Option {
  value: number;
  label: string;
}

interface FilterDropdownProps {
  label: string;
  options: Option[];
  value: number | '';
  onChange: (value: number | '') => void;
}

export default function FilterDropdown({
  label,
  options,
  value,
  onChange,
}: FilterDropdownProps) {
  return (
    <Box sx={{ display: 'flex', alignItems: 'center', minWidth: 200 }}>
      <FormControl fullWidth size="small">
        <InputLabel>{label}</InputLabel>
        <Select
          value={value}
          label={label}
          onChange={(e) => onChange(e.target.value as number | '')}
        >
          <MenuItem value="">
            <em>Wszystkie</em>
          </MenuItem>
          {options.map((option) => (
            <MenuItem key={option.value} value={option.value}>
              {option.label}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
      {value !== '' && (
        <IconButton size="small" onClick={() => onChange('')} sx={{ ml: 1 }}>
          <Clear />
        </IconButton>
      )}
    </Box>
  );
}
