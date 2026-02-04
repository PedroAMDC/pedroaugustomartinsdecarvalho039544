'use client';

interface GlobalErrorProps {
  error: Error & { digest?: string };
  reset: () => void;
}

export default function GlobalError({ error, reset }: GlobalErrorProps) {
  return (
    <html lang="pt-BR">
      <body
        style={{
          margin: 0,
          padding: 0,
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
          backgroundColor: '#f5f3f0',
          color: '#3d3629',
        }}
      >
        <div
          style={{
            textAlign: 'center',
            padding: '2rem',
            maxWidth: '500px',
          }}
        >
          <svg width="120" height="120" viewBox="0 0 100 100" style={{ marginBottom: '1.5rem' }}>
            <circle cx="50" cy="50" r="45" fill="#1a1a1a" />
            <circle cx="50" cy="50" r="15" fill="#b8860b" />
            <circle cx="50" cy="50" r="3" fill="#f5f0e8" />
            <path
              d="M 50 5 L 48 25 L 52 35 L 47 50 L 53 65 L 48 80 L 50 95"
              fill="none"
              stroke="#3a3a3a"
              strokeWidth="2"
              strokeLinecap="round"
            />
          </svg>

          <h1
            style={{
              fontSize: '1.75rem',
              fontWeight: 'bold',
              marginBottom: '0.5rem',
            }}
          >
            Erro Crítico
          </h1>

          <p
            style={{
              color: '#6b6356',
              marginBottom: '1.5rem',
              lineHeight: 1.6,
            }}
          >
            Ocorreu um erro grave na aplicação. Por favor, tente recarregar a página.
          </p>

          <button
            onClick={reset}
            style={{
              backgroundColor: '#5c4d3c',
              color: '#f5f3f0',
              border: 'none',
              padding: '0.75rem 1.5rem',
              borderRadius: '0.5rem',
              fontSize: '1rem',
              fontWeight: 500,
              cursor: 'pointer',
              marginRight: '0.5rem',
            }}
          >
            Tentar Novamente
          </button>

          <button
            onClick={() => (window.location.href = '/')}
            style={{
              backgroundColor: 'transparent',
              color: '#5c4d3c',
              border: '1px solid #d4cfc6',
              padding: '0.75rem 1.5rem',
              borderRadius: '0.5rem',
              fontSize: '1rem',
              fontWeight: 500,
              cursor: 'pointer',
            }}
          >
            Ir para Início
          </button>

          {error.digest && (
            <p
              style={{
                marginTop: '2rem',
                fontSize: '0.75rem',
                color: '#9b9488',
              }}
            >
              Código: {error.digest}
            </p>
          )}
        </div>
      </body>
    </html>
  );
}
