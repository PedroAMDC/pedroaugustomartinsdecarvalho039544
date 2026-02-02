'use client';

import { cn } from '@/lib/utils';

interface LoadingSpinnerProps {
  size?: 'sm' | 'md' | 'lg';
  overlay?: boolean;
  className?: string;
}

const sizeConfig = {
  sm: { dimension: 24, showArm: false },
  md: { dimension: 48, showArm: false },
  lg: { dimension: 80, showArm: true },
};

interface VinylDiscProps {
  size: number;
  showArm: boolean;
  className?: string;
}

function VinylDisc({ size, showArm, className }: VinylDiscProps) {
  const viewBoxSize = showArm ? 120 : 100;
  const discCenter = showArm ? 50 : 50;

  return (
    <svg
      width={size}
      height={size}
      viewBox={`0 0 ${viewBoxSize} ${viewBoxSize}`}
      className={className}
      aria-hidden="true"
    >
      {/* Definitions for gradients */}
      <defs>
        {/* Vinyl gradient - creates the grooves effect */}
        <radialGradient id="vinylGradient" cx="50%" cy="50%" r="50%">
          <stop offset="0%" stopColor="currentColor" stopOpacity="0.3" />
          <stop offset="30%" stopColor="currentColor" stopOpacity="0.9" />
          <stop offset="100%" stopColor="currentColor" stopOpacity="1" />
        </radialGradient>

        {/* Light reflection gradient */}
        <linearGradient id="shineGradient" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="white" stopOpacity="0.15" />
          <stop offset="50%" stopColor="white" stopOpacity="0" />
          <stop offset="100%" stopColor="white" stopOpacity="0.1" />
        </linearGradient>

        {/* Label gradient */}
        <radialGradient id="labelGradient" cx="50%" cy="50%" r="50%">
          <stop offset="0%" className="[stop-color:hsl(var(--primary))]" stopOpacity="1" />
          <stop offset="100%" className="[stop-color:hsl(var(--primary))]" stopOpacity="0.8" />
        </radialGradient>
      </defs>

      {/* Main vinyl disc group - this rotates */}
      <g className="origin-center animate-spin-slow" style={{ transformOrigin: `${discCenter}px ${discCenter}px` }}>
        {/* Outer disc */}
        <circle
          cx={discCenter}
          cy={discCenter}
          r="45"
          className="fill-foreground"
        />

        {/* Groove rings - concentric circles */}
        <circle
          cx={discCenter}
          cy={discCenter}
          r="40"
          className="fill-none stroke-background/10"
          strokeWidth="0.5"
        />
        <circle
          cx={discCenter}
          cy={discCenter}
          r="36"
          className="fill-none stroke-background/15"
          strokeWidth="0.5"
        />
        <circle
          cx={discCenter}
          cy={discCenter}
          r="32"
          className="fill-none stroke-background/10"
          strokeWidth="0.5"
        />
        <circle
          cx={discCenter}
          cy={discCenter}
          r="28"
          className="fill-none stroke-background/15"
          strokeWidth="0.5"
        />
        <circle
          cx={discCenter}
          cy={discCenter}
          r="24"
          className="fill-none stroke-background/10"
          strokeWidth="0.5"
        />
        <circle
          cx={discCenter}
          cy={discCenter}
          r="20"
          className="fill-none stroke-background/15"
          strokeWidth="0.5"
        />

        {/* Center label */}
        <circle
          cx={discCenter}
          cy={discCenter}
          r="15"
          className="fill-primary"
        />

        {/* Label decoration - small circle */}
        <circle
          cx={discCenter}
          cy={discCenter - 8}
          r="2"
          className="fill-primary-foreground/60"
        />

        {/* Center hole */}
        <circle
          cx={discCenter}
          cy={discCenter}
          r="3"
          className="fill-background"
        />

        {/* Light reflection overlay */}
        <ellipse
          cx={discCenter - 10}
          cy={discCenter - 10}
          rx="20"
          ry="15"
          className="fill-white/5"
          transform={`rotate(-45 ${discCenter} ${discCenter})`}
        />
      </g>

      {/* Tonearm - static, doesn't rotate */}
      {showArm && (
        <g className="fill-muted-foreground">
          {/* Arm base/pivot */}
          <circle cx="95" cy="15" r="8" className="fill-muted" />
          <circle cx="95" cy="15" r="5" className="fill-foreground" />

          {/* Arm */}
          <rect
            x="55"
            y="13"
            width="42"
            height="4"
            rx="2"
            className="fill-muted-foreground"
            transform="rotate(-15 95 15)"
          />

          {/* Headshell */}
          <rect
            x="48"
            y="28"
            width="12"
            height="6"
            rx="1"
            className="fill-muted-foreground"
            transform="rotate(-15 54 31)"
          />

          {/* Cartridge */}
          <rect
            x="45"
            y="32"
            width="6"
            height="4"
            rx="0.5"
            className="fill-foreground"
            transform="rotate(-15 48 34)"
          />
        </g>
      )}
    </svg>
  );
}

export function LoadingSpinner({
  size = 'md',
  overlay = false,
  className,
}: LoadingSpinnerProps) {
  const config = sizeConfig[size];

  const spinnerContent = (
    <div
      role="status"
      aria-busy="true"
      aria-label="Carregando..."
      className={cn('inline-flex items-center justify-center', className)}
    >
      <VinylDisc
        size={config.dimension}
        showArm={config.showArm}
      />
      <span className="sr-only">Carregando...</span>
    </div>
  );

  if (overlay) {
    return (
      <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 backdrop-blur-sm">
        {spinnerContent}
      </div>
    );
  }

  return spinnerContent;
}
