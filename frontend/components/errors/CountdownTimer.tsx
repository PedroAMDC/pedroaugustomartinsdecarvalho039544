'use client';

import { useEffect, useState, useCallback, useRef } from 'react';
import { cn } from '@/lib/utils';

interface CountdownTimerProps {
  seconds: number;
  onComplete?: () => void;
  className?: string;
  size?: 'sm' | 'md' | 'lg';
}

const sizeConfig = {
  sm: { dimension: 80, fontSize: 'text-xl', strokeWidth: 3 },
  md: { dimension: 120, fontSize: 'text-3xl', strokeWidth: 4 },
  lg: { dimension: 160, fontSize: 'text-5xl', strokeWidth: 5 },
};

export function CountdownTimer({
  seconds: initialSeconds,
  onComplete,
  className,
  size = 'md',
}: CountdownTimerProps) {
  const [secondsLeft, setSecondsLeft] = useState(initialSeconds);
  const completedRef = useRef(false);
  const onCompleteRef = useRef(onComplete);

  useEffect(() => {
    onCompleteRef.current = onComplete;
  }, [onComplete]);

  const config = sizeConfig[size];
  const radius = (config.dimension - config.strokeWidth * 2) / 2;
  const circumference = 2 * Math.PI * radius;
  const progress = secondsLeft / initialSeconds;
  const strokeDashoffset = circumference * (1 - progress);
  const isComplete = secondsLeft <= 0;

  useEffect(() => {
    if (completedRef.current || secondsLeft <= 0) return;

    const timer = setInterval(() => {
      setSecondsLeft((prev) => {
        const next = prev - 1;
        if (next <= 0) {
          clearInterval(timer);
          completedRef.current = true;
          setTimeout(() => onCompleteRef.current?.(), 0);
          return 0;
        }
        return next;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [secondsLeft]);

  const formatTime = useCallback((secs: number): string => {
    if (secs < 60) return `${secs}s`;
    const mins = Math.floor(secs / 60);
    const remainingSecs = secs % 60;
    return `${mins}:${remainingSecs.toString().padStart(2, '0')}`;
  }, []);

  return (
    <div
      className={cn('relative inline-flex items-center justify-center', className)}
      role="timer"
      aria-live="polite"
      aria-label={`${secondsLeft} segundos restantes`}
    >
      <svg width={config.dimension} height={config.dimension} className="transform -rotate-90">
        <circle
          cx={config.dimension / 2}
          cy={config.dimension / 2}
          r={radius}
          fill="none"
          stroke="var(--muted)"
          strokeWidth={config.strokeWidth}
          className="opacity-30"
        />

        <circle
          cx={config.dimension / 2}
          cy={config.dimension / 2}
          r={radius}
          fill="none"
          stroke={isComplete ? 'var(--primary)' : 'var(--destructive)'}
          strokeWidth={config.strokeWidth}
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={strokeDashoffset}
          className="transition-all duration-1000 ease-linear"
        />
      </svg>

      <div
        className={cn(
          'absolute inset-0 flex items-center justify-center',
          config.fontSize,
          'font-bold tabular-nums',
          isComplete ? 'text-primary' : 'text-foreground',
          !isComplete && secondsLeft <= 5 && 'animate-countdown-pulse text-destructive'
        )}
      >
        {isComplete ? <CheckIcon className="w-8 h-8 text-primary" /> : formatTime(secondsLeft)}
      </div>
    </div>
  );
}

function CheckIcon({ className }: { className?: string }) {
  return (
    <svg
      className={className}
      fill="none"
      viewBox="0 0 24 24"
      stroke="currentColor"
      strokeWidth={3}
    >
      <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
    </svg>
  );
}

export function useCountdown(initialSeconds: number) {
  const [seconds, setSeconds] = useState(initialSeconds);
  const [isRunning, setIsRunning] = useState(false);
  const completedRef = useRef(false);

  const isComplete = isRunning && seconds <= 0;

  const start = useCallback(() => {
    completedRef.current = false;
    setIsRunning(true);
  }, []);

  const reset = useCallback(
    (newSeconds?: number) => {
      completedRef.current = false;
      setSeconds(newSeconds ?? initialSeconds);
      setIsRunning(false);
    },
    [initialSeconds]
  );

  useEffect(() => {
    if (!isRunning || completedRef.current || seconds <= 0) {
      if (isRunning && seconds <= 0) {
        completedRef.current = true;
      }
      return;
    }

    const timer = setInterval(() => {
      setSeconds((prev) => prev - 1);
    }, 1000);

    return () => clearInterval(timer);
  }, [isRunning, seconds]);

  const progress = seconds / initialSeconds;

  return {
    seconds,
    progress,
    isRunning: isRunning && seconds > 0,
    isComplete,
    start,
    reset,
  };
}
