'use client';

import { useId } from 'react';
import { cn } from '@/lib/utils';

type ErrorVariant = 'broken' | 'scratched' | 'paused';

interface ErrorVinylProps {
  variant: ErrorVariant;
  size?: number;
  className?: string;
  progress?: number;
}

export function ErrorVinyl({ variant, size = 200, className, progress = 0 }: ErrorVinylProps) {
  const uniqueId = useId();
  const discCenter = 50;

  const isSpinning = variant === 'scratched';
  const isPaused = variant === 'paused';
  const isBroken = variant === 'broken';

  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 120 120"
      className={cn('drop-shadow-xl', className)}
      aria-hidden="true"
    >
      <defs>
        <radialGradient id={`${uniqueId}-disc`} cx="50%" cy="50%" r="50%">
          <stop offset="0%" stopColor="#1a1a1a" />
          <stop offset="35%" stopColor="#0d0d0d" />
          <stop offset="100%" stopColor="#1a1a1a" />
        </radialGradient>

        <radialGradient id={`${uniqueId}-label`} cx="30%" cy="30%" r="70%">
          <stop offset="0%" stopColor="#d4a574" />
          <stop offset="50%" stopColor="#b8860b" />
          <stop offset="100%" stopColor="#8b6914" />
        </radialGradient>

        <linearGradient id={`${uniqueId}-shine`} x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="white" stopOpacity="0.15" />
          <stop offset="50%" stopColor="white" stopOpacity="0" />
          <stop offset="100%" stopColor="white" stopOpacity="0.08" />
        </linearGradient>

        <linearGradient id={`${uniqueId}-crack`} x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="#3a3a3a" />
          <stop offset="50%" stopColor="#1a1a1a" />
          <stop offset="100%" stopColor="#2a2a2a" />
        </linearGradient>

        {isPaused && (
          <clipPath id={`${uniqueId}-progress-clip`}>
            <path d={describeArc(discCenter, discCenter, 48, 0, progress * 360)} fill="none" />
          </clipPath>
        )}
      </defs>

      <g
        className={cn(
          'origin-center',
          isSpinning && 'animate-vinyl-stutter',
          isPaused && 'animate-vinyl-slowdown'
        )}
        style={{ transformOrigin: `${discCenter}px ${discCenter}px` }}
      >
        <circle cx={discCenter} cy={discCenter} r="46" fill="#2a2a2a" />

        <circle cx={discCenter} cy={discCenter} r="45" fill={`url(#${uniqueId}-disc)`} />

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

        {isBroken && (
          <g className="animate-crack-appear">
            <path
              d="M 50 5 L 48 25 L 52 35 L 47 50 L 53 65 L 48 80 L 50 95"
              fill="none"
              stroke={`url(#${uniqueId}-crack)`}
              strokeWidth="2"
              strokeLinecap="round"
            />
            <path
              d="M 50 5 L 52 20 L 48 30 L 54 45 L 49 60 L 53 75 L 50 95"
              fill="none"
              stroke="#0a0a0a"
              strokeWidth="1"
              strokeLinecap="round"
              opacity="0.7"
            />
            <path
              d="M 30 30 L 35 35 L 32 42"
              fill="none"
              stroke={`url(#${uniqueId}-crack)`}
              strokeWidth="1.5"
              strokeLinecap="round"
            />
            <path
              d="M 70 60 L 65 65 L 68 72"
              fill="none"
              stroke={`url(#${uniqueId}-crack)`}
              strokeWidth="1.5"
              strokeLinecap="round"
            />
          </g>
        )}

        {isSpinning && (
          <g className="animate-scratch-flash">
            <line x1="25" y1="35" x2="75" y2="65" stroke="rgba(255,255,255,0.3)" strokeWidth="1" />
            <line
              x1="30"
              y1="60"
              x2="70"
              y2="40"
              stroke="rgba(255,255,255,0.2)"
              strokeWidth="0.5"
            />
          </g>
        )}

        <circle cx={discCenter} cy={discCenter} r="15" fill={`url(#${uniqueId}-label)`} />

        <circle
          cx={discCenter}
          cy={discCenter}
          r="12"
          fill="none"
          stroke="#a0722a"
          strokeWidth="0.3"
        />

        <circle cx={discCenter} cy={discCenter - 8} r="1.5" fill="#e8d5b7" />

        <circle cx={discCenter} cy={discCenter} r="3" fill="#f5f0e8" />

        <circle
          cx={discCenter}
          cy={discCenter}
          r="4"
          fill="none"
          stroke="#c9a961"
          strokeWidth="0.5"
        />

        <ellipse
          cx={discCenter - 12}
          cy={discCenter - 12}
          rx="22"
          ry="16"
          fill={`url(#${uniqueId}-shine)`}
          transform={`rotate(-45 ${discCenter} ${discCenter})`}
        />
      </g>

      {isPaused && progress > 0 && (
        <circle
          cx={discCenter}
          cy={discCenter}
          r="48"
          fill="none"
          stroke="var(--primary)"
          strokeWidth="3"
          strokeLinecap="round"
          strokeDasharray={`${progress * 301.6} 301.6`}
          transform={`rotate(-90 ${discCenter} ${discCenter})`}
          className="transition-all duration-1000 ease-linear"
        />
      )}

      <g>
        <circle cx="100" cy="15" r="8" fill="#d4c4a8" />
        <circle cx="100" cy="15" r="5" fill="#3d3d3d" />
        <circle cx="100" cy="15" r="2" fill="#5a5a5a" />

        <rect
          x="58"
          y="13"
          width="44"
          height="4"
          rx="2"
          fill="#8a8a8a"
          transform={`rotate(${isBroken ? '15' : '-15'} 100 15)`}
          className={cn(isBroken && 'animate-arm-fall')}
        />

        <rect
          x="48"
          y={isBroken ? '35' : '28'}
          width="12"
          height="6"
          rx="1"
          fill="#6a6a6a"
          transform={`rotate(${isBroken ? '45' : '-15'} 54 31)`}
        />

        <rect
          x="45"
          y={isBroken ? '40' : '32'}
          width="6"
          height="4"
          rx="0.5"
          fill="#2a2a2a"
          transform={`rotate(${isBroken ? '45' : '-15'} 48 34)`}
        />
      </g>
    </svg>
  );
}

function describeArc(
  x: number,
  y: number,
  radius: number,
  startAngle: number,
  endAngle: number
): string {
  const start = polarToCartesian(x, y, radius, endAngle);
  const end = polarToCartesian(x, y, radius, startAngle);
  const largeArcFlag = endAngle - startAngle <= 180 ? '0' : '1';

  return ['M', start.x, start.y, 'A', radius, radius, 0, largeArcFlag, 0, end.x, end.y].join(' ');
}

function polarToCartesian(
  centerX: number,
  centerY: number,
  radius: number,
  angleInDegrees: number
) {
  const angleInRadians = ((angleInDegrees - 90) * Math.PI) / 180.0;
  return {
    x: centerX + radius * Math.cos(angleInRadians),
    y: centerY + radius * Math.sin(angleInRadians),
  };
}
