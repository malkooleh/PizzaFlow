import { useState } from "react";
import { useAuditFeed } from "@/hooks/use-admin";
import { Skeleton } from "@/components/ui/skeleton";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Clock, User, Box, CornerDownRight } from "lucide-react";

const ACTION_COLOR: Record<string, string> = {
  CREATE: "bg-green-500",
  UPDATE: "bg-blue-500",
  DELETE: "bg-red-500",
  CANCEL: "bg-orange-500",
  COMPLETE: "bg-purple-500",
  CONFIRM: "bg-teal-500",
};

function dotColor(action: string): string {
  const key = Object.keys(ACTION_COLOR).find((k) => action.toUpperCase().startsWith(k));
  return key ? ACTION_COLOR[key] : "bg-muted-foreground";
}

function fmt(iso: string): string {
  return new Date(iso).toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  });
}

/**
 * Chronological operations timeline sourced from the admin audit feed.
 * Shows actor, action, resource type/ID, timestamp, and optional correlation ID.
 */
export function OpsTimeline() {
  const [page, setPage] = useState(0);
  const { data = [], isLoading } = useAuditFeed({ page, size: 20 });

  if (isLoading) {
    return (
      <div className="space-y-3">
        {Array.from({ length: 6 }).map((_, i) => (
          <div key={i} className="flex gap-3">
            <Skeleton className="h-3 w-3 rounded-full mt-1.5 shrink-0" />
            <div className="flex-1 space-y-1.5">
              <Skeleton className="h-4 w-48" />
              <Skeleton className="h-3 w-72" />
            </div>
          </div>
        ))}
      </div>
    );
  }

  if (data.length === 0) {
    return (
      <p className="text-sm text-muted-foreground py-8 text-center">
        No recent activity recorded.
      </p>
    );
  }

  return (
    <div className="space-y-4">
      {/* Timeline entries */}
      <div className="relative pl-4 border-l border-border space-y-5">
        {data.map((entry) => (
          <div key={entry.id} className="relative -ml-[1.125rem] flex items-start gap-3">
            {/* Dot */}
            <span
              className={`mt-1 h-3 w-3 rounded-full shrink-0 ring-2 ring-background ${dotColor(entry.action)}`}
            />
            <div className="flex-1 min-w-0">
              <div className="flex flex-wrap items-center gap-x-2 gap-y-0.5">
                <Badge variant="outline" className="text-xs px-1.5 py-0 h-auto">
                  {entry.action}
                </Badge>
                <span className="text-sm font-medium truncate">{entry.resourceType}</span>
                <span className="text-xs text-muted-foreground font-mono truncate">
                  #{entry.resourceId}
                </span>
              </div>
              <div className="flex flex-wrap gap-x-3 mt-0.5 text-xs text-muted-foreground">
                <span className="flex items-center gap-1">
                  <User className="h-3 w-3" />
                  {entry.actorRole} · {entry.actorId}
                </span>
                <span className="flex items-center gap-1">
                  <Clock className="h-3 w-3" />
                  {fmt(entry.timestamp)}
                </span>
                {entry.correlationId && (
                  <span className="flex items-center gap-1">
                    <CornerDownRight className="h-3 w-3" />
                    <span className="font-mono text-[10px]">{entry.correlationId}</span>
                  </span>
                )}
              </div>
              {entry.details && Object.keys(entry.details).length > 0 && (
                <div className="mt-1 flex flex-wrap gap-1">
                  {Object.entries(entry.details).map(([k, v]) => (
                    <span
                      key={k}
                      className="text-[10px] bg-muted rounded px-1.5 py-0.5 text-muted-foreground"
                    >
                      <span className="font-medium">{k}:</span> {v}
                    </span>
                  ))}
                </div>
              )}
            </div>
          </div>
        ))}
      </div>

      {/* Pagination */}
      <div className="flex items-center justify-between text-xs text-muted-foreground">
        <Button
          variant="ghost"
          size="sm"
          disabled={page === 0}
          onClick={() => setPage((p) => Math.max(0, p - 1))}
        >
          <Box className="h-3 w-3 mr-1" />
          Newer
        </Button>
        <span>Page {page + 1}</span>
        <Button
          variant="ghost"
          size="sm"
          disabled={data.length < 20}
          onClick={() => setPage((p) => p + 1)}
        >
          Older
          <Box className="h-3 w-3 ml-1" />
        </Button>
      </div>
    </div>
  );
}
