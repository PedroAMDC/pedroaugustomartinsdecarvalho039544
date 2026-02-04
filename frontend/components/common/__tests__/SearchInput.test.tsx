import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { SearchInput } from '../SearchInput';

vi.mock('@/hooks/useDebounce', () => ({
  useDebounce: (value: string) => value,
}));

describe('SearchInput', () => {
  it('should render with placeholder', () => {
    render(<SearchInput value="" onChange={vi.fn()} placeholder="Buscar artistas..." />);
    expect(screen.getByPlaceholderText('Buscar artistas...')).toBeInTheDocument();
  });

  it('should render with default placeholder', () => {
    render(<SearchInput value="" onChange={vi.fn()} />);
    expect(screen.getByPlaceholderText('Buscar...')).toBeInTheDocument();
  });

  it('should display current value', () => {
    render(<SearchInput value="test" onChange={vi.fn()} />);
    expect(screen.getByDisplayValue('test')).toBeInTheDocument();
  });

  it('should call onChange when typing', async () => {
    const onChange = vi.fn();
    const user = userEvent.setup();

    render(<SearchInput value="" onChange={onChange} />);

    const input = screen.getByPlaceholderText('Buscar...');
    await user.type(input, 'a');

    expect(onChange).toHaveBeenCalledWith('a');
  });

  it('should show clear button when value is present', () => {
    render(<SearchInput value="test" onChange={vi.fn()} />);
    expect(screen.getByLabelText('Limpar busca')).toBeInTheDocument();
  });

  it('should not show clear button when value is empty', () => {
    render(<SearchInput value="" onChange={vi.fn()} />);
    expect(screen.queryByLabelText('Limpar busca')).not.toBeInTheDocument();
  });

  it('should call onChange with empty string when clear is clicked', async () => {
    const onChange = vi.fn();
    const user = userEvent.setup();

    render(<SearchInput value="test" onChange={onChange} />);

    await user.click(screen.getByLabelText('Limpar busca'));
    expect(onChange).toHaveBeenCalledWith('');
  });

  it('should show loading spinner when isLoading is true', () => {
    render(<SearchInput value="test" onChange={vi.fn()} isLoading />);
    expect(screen.getByLabelText('Carregando...')).toBeInTheDocument();
    expect(screen.queryByLabelText('Limpar busca')).not.toBeInTheDocument();
  });
});
