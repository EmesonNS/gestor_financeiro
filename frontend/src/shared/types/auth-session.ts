export type AuthSessionUser = {
  id: string;
  name: string;
  email: string;
  role: 'USER' | 'ADMIN';
};
