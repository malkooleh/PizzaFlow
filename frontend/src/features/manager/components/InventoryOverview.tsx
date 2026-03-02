import { AlertTriangle } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useStockLevels } from "@/hooks/use-manager";

interface InventoryOverviewProps {
  restaurantId: string;
}

export function InventoryOverview({ restaurantId }: InventoryOverviewProps) {
  const { data: items, isLoading } = useStockLevels(restaurantId);

  if (isLoading) {
    return (
      <div className="space-y-2">
        {Array.from({ length: 6 }).map((_, i) => (
          <Skeleton key={i} className="h-10 w-full" />
        ))}
      </div>
    );
  }

  if (!items?.length) {
    return (
      <div className="py-10 text-center text-muted-foreground">
        No inventory data available.
      </div>
    );
  }

  const sorted = [...items].sort((a, b) => {
    const aLow = a.currentStock < a.minStockLevel ? 0 : 1;
    const bLow = b.currentStock < b.minStockLevel ? 0 : 1;
    return aLow - bLow || a.ingredientName.localeCompare(b.ingredientName);
  });

  return (
    <div className="rounded-md border">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Ingredient</TableHead>
            <TableHead className="text-right">Current Stock</TableHead>
            <TableHead className="text-right">Min Level</TableHead>
            <TableHead>Unit</TableHead>
            <TableHead>Status</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {sorted.map((item) => {
            const isLow = item.currentStock < item.minStockLevel;
            return (
              <TableRow
                key={item.ingredientId}
                className={isLow ? "bg-yellow-50 dark:bg-yellow-950/20" : undefined}
              >
                <TableCell className="font-medium">
                  <div className="flex items-center gap-2">
                    {isLow && (
                      <AlertTriangle className="h-3.5 w-3.5 text-yellow-500" />
                    )}
                    {item.ingredientName}
                  </div>
                </TableCell>
                <TableCell
                  className={`text-right font-mono ${isLow ? "text-yellow-700 dark:text-yellow-400" : ""}`}
                >
                  {item.currentStock}
                </TableCell>
                <TableCell className="text-right font-mono text-muted-foreground">
                  {item.minStockLevel}
                </TableCell>
                <TableCell className="text-muted-foreground">{item.unit}</TableCell>
                <TableCell>
                  <Badge variant={isLow ? "warning" : "success"}>
                    {isLow ? "Low Stock" : "OK"}
                  </Badge>
                </TableCell>
              </TableRow>
            );
          })}
        </TableBody>
      </Table>
    </div>
  );
}
