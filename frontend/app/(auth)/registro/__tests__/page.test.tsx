import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import RegisterPage from '../page';

// Mock useAuth hook
const mockRegister = vi.fn();
vi.mock('@/hooks/useAuth', () => ({
  useAuth: () => ({
    register: mockRegister,
    isAuthenticated: false,
    isLoading: false,
    user: null,
  }),
}));

// Mock next/navigation
const mockPush = vi.fn();
vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: mockPush,
    replace: vi.fn(),
    prefetch: vi.fn(),
  }),
}));

describe('RegisterPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('renderiza formulário', () => {
    it('should render nome input', () => {
      render(<RegisterPage />);
      expect(screen.getByLabelText(/nome/i)).toBeInTheDocument();
    });

    it('should render email input', () => {
      render(<RegisterPage />);
      expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    });

    it('should render password input', () => {
      render(<RegisterPage />);
      expect(screen.getByLabelText(/^senha$/i)).toBeInTheDocument();
    });

    it('should render confirm password input', () => {
      render(<RegisterPage />);
      expect(screen.getByLabelText(/confirmar senha/i)).toBeInTheDocument();
    });

    it('should render submit button', () => {
      render(<RegisterPage />);
      expect(screen.getByRole('button', { name: /criar conta/i })).toBeInTheDocument();
    });

    it('should render login link', () => {
      render(<RegisterPage />);
      expect(screen.getByRole('link', { name: /entre/i })).toHaveAttribute('href', '/login');
    });
  });

  describe('validação de nome', () => {
    it('should show error for nome less than 2 characters', async () => {
      const user = userEvent.setup();
      render(<RegisterPage />);

      await user.type(screen.getByLabelText(/nome/i), 'A');
      await user.click(screen.getByRole('button', { name: /criar conta/i }));

      expect(await screen.findByText(/nome deve ter pelo menos 2 caracteres/i)).toBeInTheDocument();
    });
  });

  describe('validação de email', () => {
    it('should show error for empty email', async () => {
      const user = userEvent.setup();
      render(<RegisterPage />);

      await user.type(screen.getByLabelText(/nome/i), 'Test User');
      await user.click(screen.getByRole('button', { name: /criar conta/i }));

      expect(await screen.findByText(/email é obrigatório/i)).toBeInTheDocument();
    });

    it('should show error for invalid email format', async () => {
      const user = userEvent.setup();
      render(<RegisterPage />);

      await user.type(screen.getByLabelText(/nome/i), 'Test User');
      await user.type(screen.getByLabelText(/email/i), 'test..test@test.com');
      await user.click(screen.getByRole('button', { name: /criar conta/i }));

      expect(await screen.findByText(/email inválido/i)).toBeInTheDocument();
    });
  });

  describe('validação de senha', () => {
    it('should show error for password less than 8 characters', async () => {
      const user = userEvent.setup();
      render(<RegisterPage />);

      await user.type(screen.getByLabelText(/nome/i), 'Test User');
      await user.type(screen.getByLabelText(/email/i), 'test@test.com');
      await user.type(screen.getByLabelText(/^senha$/i), '1234567');
      await user.type(screen.getByLabelText(/confirmar senha/i), 'password123');
      await user.click(screen.getByRole('button', { name: /criar conta/i }));

      const errors = await screen.findAllByText(/mínimo 8 caracteres/i);
      expect(errors.length).toBeGreaterThanOrEqual(1);
    });

    it('should show error when passwords do not match', async () => {
      const user = userEvent.setup();
      render(<RegisterPage />);

      await user.type(screen.getByLabelText(/nome/i), 'Test User');
      await user.type(screen.getByLabelText(/email/i), 'test@test.com');
      await user.type(screen.getByLabelText(/^senha$/i), 'password123');
      await user.type(screen.getByLabelText(/confirmar senha/i), 'differentpass');
      await user.click(screen.getByRole('button', { name: /criar conta/i }));

      expect(await screen.findByText(/as senhas não conferem/i)).toBeInTheDocument();
    });
  });

  describe('submit com dados válidos', () => {
    it('should call register with correct data', async () => {
      mockRegister.mockResolvedValueOnce(undefined);
      const user = userEvent.setup();
      render(<RegisterPage />);

      await user.type(screen.getByLabelText(/nome/i), 'Test User');
      await user.type(screen.getByLabelText(/email/i), 'test@test.com');
      await user.type(screen.getByLabelText(/^senha$/i), 'password123');
      await user.type(screen.getByLabelText(/confirmar senha/i), 'password123');
      await user.click(screen.getByRole('button', { name: /criar conta/i }));

      await waitFor(() => {
        expect(mockRegister).toHaveBeenCalledWith(
          'Test User',
          'test@test.com',
          'password123',
          'password123'
        );
      });
    });
  });

  describe('exibe erro em caso de falha', () => {
    it('should show error message on 409 response (email already exists)', async () => {
      mockRegister.mockRejectedValueOnce({
        response: { status: 409, data: { message: 'Email already registered' } },
      });
      const user = userEvent.setup();
      render(<RegisterPage />);

      await user.type(screen.getByLabelText(/nome/i), 'Test User');
      await user.type(screen.getByLabelText(/email/i), 'existing@test.com');
      await user.type(screen.getByLabelText(/^senha$/i), 'password123');
      await user.type(screen.getByLabelText(/confirmar senha/i), 'password123');
      await user.click(screen.getByRole('button', { name: /criar conta/i }));

      expect(await screen.findByText(/este email já está cadastrado/i)).toBeInTheDocument();
    });

    it('should show API error message', async () => {
      mockRegister.mockRejectedValueOnce({
        response: { status: 400, data: { message: 'Senha muito fraca' } },
      });
      const user = userEvent.setup();
      render(<RegisterPage />);

      await user.type(screen.getByLabelText(/nome/i), 'Test User');
      await user.type(screen.getByLabelText(/email/i), 'test@test.com');
      await user.type(screen.getByLabelText(/^senha$/i), 'password123');
      await user.type(screen.getByLabelText(/confirmar senha/i), 'password123');
      await user.click(screen.getByRole('button', { name: /criar conta/i }));

      expect(await screen.findByText(/senha muito fraca/i)).toBeInTheDocument();
    });

    it('should show generic error on unexpected failure', async () => {
      mockRegister.mockRejectedValueOnce(new Error('Network error'));
      const user = userEvent.setup();
      render(<RegisterPage />);

      await user.type(screen.getByLabelText(/nome/i), 'Test User');
      await user.type(screen.getByLabelText(/email/i), 'test@test.com');
      await user.type(screen.getByLabelText(/^senha$/i), 'password123');
      await user.type(screen.getByLabelText(/confirmar senha/i), 'password123');
      await user.click(screen.getByRole('button', { name: /criar conta/i }));

      expect(await screen.findByText(/erro ao criar conta/i)).toBeInTheDocument();
    });
  });

  describe('loading state durante submit', () => {
    it('should show loading text on button during submit', async () => {
      mockRegister.mockImplementation(() => new Promise(() => {})); // Never resolves
      const user = userEvent.setup();
      render(<RegisterPage />);

      await user.type(screen.getByLabelText(/nome/i), 'Test User');
      await user.type(screen.getByLabelText(/email/i), 'test@test.com');
      await user.type(screen.getByLabelText(/^senha$/i), 'password123');
      await user.type(screen.getByLabelText(/confirmar senha/i), 'password123');
      await user.click(screen.getByRole('button', { name: /criar conta/i }));

      expect(await screen.findByText(/criando conta/i)).toBeInTheDocument();
    });

    it('should disable submit button during loading', async () => {
      mockRegister.mockImplementation(() => new Promise(() => {}));
      const user = userEvent.setup();
      render(<RegisterPage />);

      await user.type(screen.getByLabelText(/nome/i), 'Test User');
      await user.type(screen.getByLabelText(/email/i), 'test@test.com');
      await user.type(screen.getByLabelText(/^senha$/i), 'password123');
      await user.type(screen.getByLabelText(/confirmar senha/i), 'password123');
      await user.click(screen.getByRole('button', { name: /criar conta/i }));

      await waitFor(() => {
        expect(screen.getByRole('button')).toBeDisabled();
      });
    });

    it('should disable inputs during loading', async () => {
      mockRegister.mockImplementation(() => new Promise(() => {}));
      const user = userEvent.setup();
      render(<RegisterPage />);

      await user.type(screen.getByLabelText(/nome/i), 'Test User');
      await user.type(screen.getByLabelText(/email/i), 'test@test.com');
      await user.type(screen.getByLabelText(/^senha$/i), 'password123');
      await user.type(screen.getByLabelText(/confirmar senha/i), 'password123');
      await user.click(screen.getByRole('button', { name: /criar conta/i }));

      await waitFor(() => {
        expect(screen.getByLabelText(/nome/i)).toBeDisabled();
        expect(screen.getByLabelText(/email/i)).toBeDisabled();
        expect(screen.getByLabelText(/^senha$/i)).toBeDisabled();
        expect(screen.getByLabelText(/confirmar senha/i)).toBeDisabled();
      });
    });
  });

  describe('redirect após registro', () => {
    it('should redirect to home on successful registration', async () => {
      mockRegister.mockResolvedValueOnce(undefined);
      const user = userEvent.setup();
      render(<RegisterPage />);

      await user.type(screen.getByLabelText(/nome/i), 'Test User');
      await user.type(screen.getByLabelText(/email/i), 'test@test.com');
      await user.type(screen.getByLabelText(/^senha$/i), 'password123');
      await user.type(screen.getByLabelText(/confirmar senha/i), 'password123');
      await user.click(screen.getByRole('button', { name: /criar conta/i }));

      await waitFor(() => {
        expect(mockPush).toHaveBeenCalledWith('/');
      });
    });
  });
});
