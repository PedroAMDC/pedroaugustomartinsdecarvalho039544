import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Pagination } from '../Pagination';

describe('Pagination', () => {
  const defaultProps = {
    page: 1,
    totalPages: 10,
    totalItems: 100,
    pageSize: 10,
    onPageChange: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('rendering', () => {
    it('renders correctly with all elements', () => {
      render(<Pagination {...defaultProps} />);

      expect(screen.getByText('Mostrando 1-10 de 100 itens')).toBeInTheDocument();
      expect(screen.getByLabelText('Ir para página anterior')).toBeInTheDocument();
      expect(screen.getByLabelText('Ir para próxima página')).toBeInTheDocument();
    });

    it('returns null when totalPages is 1', () => {
      const { container } = render(
        <Pagination {...defaultProps} totalPages={1} totalItems={10} />
      );
      expect(container).toBeEmptyDOMElement();
    });

    it('returns null when totalPages is 0', () => {
      const { container } = render(
        <Pagination {...defaultProps} totalPages={0} totalItems={0} />
      );
      expect(container).toBeEmptyDOMElement();
    });
  });

  describe('item count display', () => {
    it('shows "Mostrando X-Y de Z itens" correctly', () => {
      render(<Pagination {...defaultProps} page={1} />);
      expect(screen.getByText('Mostrando 1-10 de 100 itens')).toBeInTheDocument();
    });

    it('shows correct range for middle pages', () => {
      render(<Pagination {...defaultProps} page={5} />);
      expect(screen.getByText('Mostrando 41-50 de 100 itens')).toBeInTheDocument();
    });

    it('shows correct range for last page with partial items', () => {
      render(<Pagination {...defaultProps} page={10} totalItems={95} />);
      expect(screen.getByText('Mostrando 91-95 de 95 itens')).toBeInTheDocument();
    });
  });

  describe('page numbers', () => {
    it('displays page numbers for navigation', () => {
      render(<Pagination {...defaultProps} page={5} />);

      // Page numbers are rendered (hidden on mobile, visible on sm+)
      expect(screen.getByLabelText('Ir para página 1')).toBeInTheDocument();
      expect(screen.getByLabelText('Ir para página 10')).toBeInTheDocument();
    });

    it('highlights current page', () => {
      render(<Pagination {...defaultProps} page={5} />);

      const currentPage = screen.getByLabelText('Ir para página 5');
      expect(currentPage).toHaveAttribute('aria-current', 'page');
    });
  });

  describe('previous button', () => {
    it('is disabled on first page', () => {
      render(<Pagination {...defaultProps} page={1} />);

      const prevButton = screen.getByLabelText('Ir para página anterior');
      expect(prevButton).toHaveAttribute('aria-disabled', 'true');
      expect(prevButton).toHaveClass('pointer-events-none', 'opacity-50');
    });

    it('does not call onPageChange when disabled', async () => {
      const onPageChange = vi.fn();
      render(<Pagination {...defaultProps} page={1} onPageChange={onPageChange} />);

      const prevButton = screen.getByLabelText('Ir para página anterior');
      await userEvent.click(prevButton);

      expect(onPageChange).not.toHaveBeenCalled();
    });

    it('is enabled when not on first page', () => {
      render(<Pagination {...defaultProps} page={5} />);

      const prevButton = screen.getByLabelText('Ir para página anterior');
      expect(prevButton).toHaveAttribute('aria-disabled', 'false');
      expect(prevButton).not.toHaveClass('pointer-events-none');
    });

    it('calls onPageChange with page - 1 when clicked', async () => {
      const onPageChange = vi.fn();
      render(<Pagination {...defaultProps} page={5} onPageChange={onPageChange} />);

      const prevButton = screen.getByLabelText('Ir para página anterior');
      await userEvent.click(prevButton);

      expect(onPageChange).toHaveBeenCalledWith(4);
    });
  });

  describe('next button', () => {
    it('is disabled on last page', () => {
      render(<Pagination {...defaultProps} page={10} />);

      const nextButton = screen.getByLabelText('Ir para próxima página');
      expect(nextButton).toHaveAttribute('aria-disabled', 'true');
      expect(nextButton).toHaveClass('pointer-events-none', 'opacity-50');
    });

    it('does not call onPageChange when disabled', async () => {
      const onPageChange = vi.fn();
      render(<Pagination {...defaultProps} page={10} onPageChange={onPageChange} />);

      const nextButton = screen.getByLabelText('Ir para próxima página');
      await userEvent.click(nextButton);

      expect(onPageChange).not.toHaveBeenCalled();
    });

    it('is enabled when not on last page', () => {
      render(<Pagination {...defaultProps} page={5} />);

      const nextButton = screen.getByLabelText('Ir para próxima página');
      expect(nextButton).toHaveAttribute('aria-disabled', 'false');
      expect(nextButton).not.toHaveClass('pointer-events-none');
    });

    it('calls onPageChange with page + 1 when clicked', async () => {
      const onPageChange = vi.fn();
      render(<Pagination {...defaultProps} page={5} onPageChange={onPageChange} />);

      const nextButton = screen.getByLabelText('Ir para próxima página');
      await userEvent.click(nextButton);

      expect(onPageChange).toHaveBeenCalledWith(6);
    });
  });

  describe('page link clicks', () => {
    it('calls onPageChange with clicked page number', async () => {
      const onPageChange = vi.fn();
      render(<Pagination {...defaultProps} page={1} onPageChange={onPageChange} />);

      const page3Link = screen.getByLabelText('Ir para página 3');
      await userEvent.click(page3Link);

      expect(onPageChange).toHaveBeenCalledWith(3);
    });
  });
});
