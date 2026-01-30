export const TEST_USERS = {
  admin: {
    email: 'admin@example.com',
    password: 'admin123',
    role: 'ADMIN' as const,
  },
  employee: {
    email: 'jan.kowalski@example.com',
    password: 'password123',
    role: 'EMPLOYEE' as const,
  },
};

export const TEST_ASSETS = {
  laptop: { id: 1, seriesNumber: 'SN-LAPTOP-001', vendor: 'Dell', model: 'XPS 15' },
  smartphone: { id: 2, seriesNumber: 'SN-PHONE-001', vendor: 'Apple', model: 'iPhone 14' },
  tablet: { id: 3, seriesNumber: 'SN-TABLET-001', vendor: 'Samsung', model: 'Galaxy Tab S9' },
};
