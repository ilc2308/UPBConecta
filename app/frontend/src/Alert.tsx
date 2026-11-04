import type { ReactNode } from 'react'
import './Alert.css'

interface AlertProps {
  children: ReactNode
  actionLabel?: string
  onAction?: () => void
}

export function Alert({ children, actionLabel, onAction }: AlertProps) {
  return (
    <div className="alert" role="alert" aria-live="assertive">
      <p className="alert__message">{children}</p>
      {actionLabel && onAction && (
        <button className="alert__action" type="button" onClick={onAction}>
          {actionLabel}
        </button>
      )}
    </div>
  )
}
