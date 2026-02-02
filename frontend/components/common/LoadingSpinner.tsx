'use client';

import { useId } from 'react';
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
  showArm?: boolean;
  spinning?: boolean;
  className?: string;
}

export function VinylDisc({
  size,
  showArm = false,
  spinning = true,
  className,
}: VinylDiscProps) {
  const viewBoxSize = showArm ? 120 : 100;
  const discCenter = 50;
  const uniqueId = useId();

  return (
    <svg
      width={size}
      height={size}
      viewBox={`0 0 ${viewBoxSize} ${viewBoxSize}`}
      className={cn('drop-shadow-lg', className)}
      aria-hidden="true"
    >
      <defs>
        {/* Gradient for vinyl disc - creates realistic grooves look */}
        <radialGradient id={`${uniqueId}-disc`} cx="50%" cy="50%" r="50%">
          <stop offset="0%" stopColor="#1a1a1a" />
          <stop offset="35%" stopColor="#0d0d0d" />
          <stop offset="100%" stopColor="#1a1a1a" />
        </radialGradient>

        {/* Gradient for center label - warm brown/amber */}
        <radialGradient id={`${uniqueId}-label`} cx="30%" cy="30%" r="70%">
          <stop offset="0%" stopColor="#d4a574" />
          <stop offset="50%" stopColor="#b8860b" />
          <stop offset="100%" stopColor="#8b6914" />
        </radialGradient>

        {/* Shine effect */}
        <linearGradient id={`${uniqueId}-shine`} x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="white" stopOpacity="0.15" />
          <stop offset="50%" stopColor="white" stopOpacity="0" />
          <stop offset="100%" stopColor="white" stopOpacity="0.08" />
        </linearGradient>
      </defs>

      {/* Main vinyl disc group */}
      <g
        className={spinning ? 'origin-center animate-spin-slow' : undefined}
        style={{ transformOrigin: `${discCenter}px ${discCenter}px` }}
      >
        {/* Outer edge - slightly lighter */}
        <circle
          cx={discCenter}
          cy={discCenter}
          r="46"
          fill="#2a2a2a"
        />

        {/* Main disc body */}
        <circle
          cx={discCenter}
          cy={discCenter}
          r="45"
          fill={`url(#${uniqueId}-disc)`}
        />

        {/* Groove rings - realistic vinyl grooves */}
        {[42, 40, 38, 36, 34, 32, 30, 28, 26, 24, 22, 20, 18].map((r, i) => (
          <circle
            key={r}
            cx={discCenter}
            cy={discCenter}
            r={r}
            fill="none"
            stroke={i % 2 === 0 ? '#252525' : '#1f1f1f'}
            strokeWidth="0.4"
          />
        ))}

        {/* Center label */}
        <circle
          cx={discCenter}
          cy={discCenter}
          r="15"
          fill={`url(#${uniqueId}-label)`}
        />

        {/* Label text area - decorative lines */}
        <circle
          cx={discCenter}
          cy={discCenter}
          r="12"
          fill="none"
          stroke="#a0722a"
          strokeWidth="0.3"
        />

        {/* Label decoration - small dot */}
        <circle
          cx={discCenter}
          cy={discCenter - 8}
          r="1.5"
          fill="#e8d5b7"
        />

        {/* Center hole */}
        <circle
          cx={discCenter}
          cy={discCenter}
          r="3"
          fill="#f5f0e8"
        />

        {/* Inner ring around hole */}
        <circle
          cx={discCenter}
          cy={discCenter}
          r="4"
          fill="none"
          stroke="#c9a961"
          strokeWidth="0.5"
        />

        {/* Light reflection overlay */}
        <ellipse
          cx={discCenter - 12}
          cy={discCenter - 12}
          rx="22"
          ry="16"
          fill={`url(#${uniqueId}-shine)`}
          transform={`rotate(-45 ${discCenter} ${discCenter})`}
        />
      </g>

      {/* Tonearm - static, doesn't rotate */}
      {showArm && (
        <g>
          {/* Arm base/pivot */}
          <circle cx="95" cy="15" r="8" fill="#d4c4a8" />
          <circle cx="95" cy="15" r="5" fill="#3d3d3d" />
          <circle cx="95" cy="15" r="2" fill="#5a5a5a" />

          {/* Arm */}
          <rect
            x="55"
            y="13"
            width="42"
            height="4"
            rx="2"
            fill="#8a8a8a"
            transform="rotate(-15 95 15)"
          />

          {/* Headshell */}
          <rect
            x="48"
            y="28"
            width="12"
            height="6"
            rx="1"
            fill="#6a6a6a"
            transform="rotate(-15 54 31)"
          />

          {/* Cartridge */}
          <rect
            x="45"
            y="32"
            width="6"
            height="4"
            rx="0.5"
            fill="#2a2a2a"
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
      <VinylDisc size={config.dimension} showArm={config.showArm} spinning />
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
