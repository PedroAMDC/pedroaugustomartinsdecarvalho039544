import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import LoginPage from '../page';

// Mock useAuth hook
const mockLogin = vi.fn();
vi.mock('@/hooks/useAuth', () => ({
  useAuth: () => ({
    login: mockLogin,
    isAuthenticated: false,
    isLoading: false,
    user: null,
  }),
}));


describe('LoginPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('renderiza formulário', () => {
    it('should render email input', () => {
      render(<LoginPage />);
      expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    });

    it('should render password input', () => {
      render(<LoginPage />);
      expect(screen.getByLabelText(/senha/i)).toBeInTheDocument();
    });

    it('should render submit button', () => {
      render(<LoginPage />);
      expect(screen.getByRole('button', { name: /entrar/i })).toBeInTheDocument();
    });

    it('should render registration link', () => {
      render(<LoginPage />);
      expect(screen.getByRole('link', { name: /registre-se/i })).toHaveAttribute(
        'href',
        '/registro'
      );
    });
  });

  describe('validação de email', () => {
    it('should show error for empty email', async () => {
      const user = userEvent.setup();
      render(<LoginPage />);

      await user.click(screen.getByRole('button', { name: /entrar/i }));

      expect(await screen.findByText(/email é obrigatório/i)).toBeInTheDocument();
    });

    it('should show error for invalid email format', async () => {
      const user = userEvent.setup();
      render(<LoginPage />);

      // Use email that passes HTML5 validation but fails Zod's Gmail-based rules
      // (consecutive dots in local part)
      await user.type(screen.getByLabelText(/email/i), 'test..test@test.com');
      await user.click(screen.getByRole('button', { name: /entrar/i }));

      expect(await screen.findByText(/email inválido/i)).toBeInTheDocument();
    });
  });

  describe('validação de senha', () => {
    it('should show error for password less than 6 characters', async () => {
      const user = userEvent.setup();
      render(<LoginPage />);

      await user.type(screen.getByLabelText(/email/i), 'test@test.com');
      await user.type(screen.getByLabelText(/senha/i), '12345');
      await user.click(screen.getByRole('button', { name: /entrar/i }));

      expect(await screen.findByText(/mínimo 6 caracteres/i)).toBeInTheDocument();
    });
  });

  describe('submit com credenciais válidas', () => {
    it('should call login with correct credentials', async () => {
      mockLogin.mockResolvedValueOnce(undefined);
      const user = userEvent.setup();
      render(<LoginPage />);

      await user.type(screen.getByLabelText(/email/i), 'test@test.com');
      await user.type(screen.getByLabelText(/senha/i), 'password123');
      await user.click(screen.getByRole('button', { name: /entrar/i }));

      await waitFor(() => {
        expect(mockLogin).toHaveBeenCalledWith('test@test.com', 'password123');
      });
    });
  });

  describe('exibe erro com credenciais inválidas', () => {
    it('should show error message on 401 response', async () => {
      mockLogin.mockRejectedValueOnce({
        response: { status: 401, data: { message: 'Unauthorized' } },
      });
      const user = userEvent.setup();
      render(<LoginPage />);

      await user.type(screen.getByLabelText(/email/i), 'wrong@test.com');
      await user.type(screen.getByLabelText(/senha/i), 'wrongpassword');
      await user.click(screen.getByRole('button', { name: /entrar/i }));

      expect(await screen.findByText(/email ou senha incorretos/i)).toBeInTheDocument();
    });

    it('should show API error message', async () => {
      mockLogin.mockRejectedValueOnce({
        response: { status: 400, data: { message: 'Conta desativada' } },
      });
      const user = userEvent.setup();
      render(<LoginPage />);

      await user.type(screen.getByLabelText(/email/i), 'test@test.com');
      await user.type(screen.getByLabelText(/senha/i), 'password123');
      await user.click(screen.getByRole('button', { name: /entrar/i }));

      expect(await screen.findByText(/conta desativada/i)).toBeInTheDocument();
    });

    it('should show generic error on unexpected failure', async () => {
      mockLogin.mockRejectedValueOnce(new Error('Network error'));
      const user = userEvent.setup();
      render(<LoginPage />);

      await user.type(screen.getByLabelText(/email/i), 'test@test.com');
      await user.type(screen.getByLabelText(/senha/i), 'password123');
      await user.click(screen.getByRole('button', { name: /entrar/i }));

      expect(await screen.findByText(/erro ao fazer login/i)).toBeInTheDocument();
    });
  });

  describe('loading state durante submit', () => {
    it('should show loading text on button during submit', async () => {
      mockLogin.mockImplementation(() => new Promise(() => {})); // Never resolves
      const user = userEvent.setup();
      render(<LoginPage />);

      await user.type(screen.getByLabelText(/email/i), 'test@test.com');
      await user.type(screen.getByLabelText(/senha/i), 'password123');
      await user.click(screen.getByRole('button', { name: /entrar/i }));

      expect(await screen.findByText(/entrando/i)).toBeInTheDocument();
    });

    it('should disable submit button during loading', async () => {
      mockLogin.mockImplementation(() => new Promise(() => {}));
      const user = userEvent.setup();
      render(<LoginPage />);

      await user.type(screen.getByLabelText(/email/i), 'test@test.com');
      await user.type(screen.getByLabelText(/senha/i), 'password123');
      await user.click(screen.getByRole('button', { name: /entrar/i }));

      await waitFor(() => {
        expect(screen.getByRole('button')).toBeDisabled();
      });
    });

    it('should disable inputs during loading', async () => {
      mockLogin.mockImplementation(() => new Promise(() => {}));
      const user = userEvent.setup();
      render(<LoginPage />);

      await user.type(screen.getByLabelText(/email/i), 'test@test.com');
      await user.type(screen.getByLabelText(/senha/i), 'password123');
      await user.click(screen.getByRole('button', { name: /entrar/i }));

      await waitFor(() => {
        expect(screen.getByLabelText(/email/i)).toBeDisabled();
        expect(screen.getByLabelText(/senha/i)).toBeDisabled();
      });
    });
  });

  describe('redirect após login', () => {
    it('should redirect to home on successful login', async () => {
      const locationHrefSpy = vi.fn();
      Object.defineProperty(window, 'location', {
        value: { href: '' },
        writable: true,
      });
      Object.defineProperty(window.location, 'href', {
        set: locationHrefSpy,
        get: () => '',
      });

      mockLogin.mockResolvedValueOnce(undefined);
      const user = userEvent.setup();
      render(<LoginPage />);

      await user.type(screen.getByLabelText(/email/i), 'test@test.com');
      await user.type(screen.getByLabelText(/senha/i), 'password123');
      await user.click(screen.getByRole('button', { name: /entrar/i }));

      await waitFor(() => {
        expect(locationHrefSpy).toHaveBeenCalledWith('/');
      });
    });
  });
});
