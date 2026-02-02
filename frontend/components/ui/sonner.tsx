"use client"

import {
  CircleCheckIcon,
  InfoIcon,
  Loader2Icon,
  OctagonXIcon,
  TriangleAlertIcon,
} from "lucide-react"
import { useTheme } from "next-themes"
import { Toaster as Sonner, type ToasterProps } from "sonner"

const Toaster = ({ ...props }: ToasterProps) => {
  const { theme = "light" } = useTheme()

  return (
    <Sonner
      theme={theme as ToasterProps["theme"]}
      className="toaster group"
      icons={{
        success: <CircleCheckIcon className="size-4" />,
        info: <InfoIcon className="size-4" />,
        warning: <TriangleAlertIcon className="size-4" />,
        error: <OctagonXIcon className="size-4" />,
        loading: <Loader2Icon className="size-4 animate-spin" />,
      }}
      toastOptions={{
        classNames: {
          toast: "bg-[var(--popover)] text-[var(--popover-foreground)] border-[var(--border)] shadow-lg",
          title: "text-[var(--foreground)] font-semibold",
          description: "text-[var(--muted-foreground)]",
          success: "bg-[var(--popover)] text-[var(--popover-foreground)] border-[var(--border)]",
          error: "bg-[var(--destructive)] text-[var(--destructive-foreground)] border-[var(--destructive)]",
          warning: "bg-[var(--popover)] text-[var(--popover-foreground)] border-[var(--border)]",
          info: "bg-[var(--popover)] text-[var(--popover-foreground)] border-[var(--border)]",
        },
      }}
      {...props}
    />
  )
}

export { Toaster }
