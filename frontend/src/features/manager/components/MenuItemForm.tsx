import { useState } from "react";
import { Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import type { MenuItemUpdateRequest } from "@/api/catalog.api";
import { MenuCategory } from "@/types/enums";
import type { MenuItem } from "@/types/models";

interface MenuItemFormProps {
  defaultValues?: Partial<MenuItem>;
  onSubmit: (data: MenuItemUpdateRequest) => void;
  isSubmitting: boolean;
}

const CATEGORY_LABELS: Record<MenuCategory, string> = {
  [MenuCategory.PIZZA]: "Pizza",
  [MenuCategory.PASTA]: "Pasta",
  [MenuCategory.SALAD]: "Salad",
  [MenuCategory.APPETIZER]: "Appetizer",
  [MenuCategory.DESSERT]: "Dessert",
  [MenuCategory.BEVERAGE]: "Beverage",
  [MenuCategory.SIDES]: "Sides",
  [MenuCategory.SPECIAL]: "Special",
};

export function MenuItemForm({
  defaultValues,
  onSubmit,
  isSubmitting,
}: MenuItemFormProps) {
  const [name, setName] = useState(defaultValues?.name ?? "");
  const [description, setDescription] = useState(
    defaultValues?.description ?? "",
  );
  const [price, setPrice] = useState(String(defaultValues?.price ?? ""));
  const [category, setCategory] = useState<MenuCategory>(
    defaultValues?.category ?? MenuCategory.PIZZA,
  );
  const [imageUrl, setImageUrl] = useState(defaultValues?.imageUrl ?? "");
  const [allergens, setAllergens] = useState(
    (defaultValues?.allergens ?? []).join(", "),
  );

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    onSubmit({
      name: name.trim(),
      description: description.trim() || undefined,
      price: parseFloat(price),
      category,
      imageUrl: imageUrl.trim() || undefined,
      allergens: allergens
        ? allergens.split(",").map((a) => a.trim()).filter(Boolean)
        : undefined,
    });
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <Label htmlFor="name">Name *</Label>
        <Input
          id="name"
          required
          value={name}
          onChange={(e) => setName(e.target.value)}
          className="mt-1"
        />
      </div>

      <div>
        <Label htmlFor="description">Description</Label>
        <Textarea
          id="description"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          className="mt-1"
          rows={2}
        />
      </div>

      <div className="grid grid-cols-2 gap-3">
        <div>
          <Label htmlFor="price">Price *</Label>
          <Input
            id="price"
            type="number"
            step="0.01"
            min="0"
            required
            value={price}
            onChange={(e) => setPrice(e.target.value)}
            className="mt-1"
          />
        </div>
        <div>
          <Label>Category *</Label>
          <Select
            value={category}
            onValueChange={(v) => setCategory(v as MenuCategory)}
          >
            <SelectTrigger className="mt-1">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {Object.values(MenuCategory).map((c) => (
                <SelectItem key={c} value={c}>
                  {CATEGORY_LABELS[c]}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      <div>
        <Label htmlFor="imageUrl">Image URL</Label>
        <Input
          id="imageUrl"
          type="url"
          value={imageUrl}
          onChange={(e) => setImageUrl(e.target.value)}
          className="mt-1"
          placeholder="https://…"
        />
      </div>

      <div>
        <Label htmlFor="allergens">Allergens (comma-separated)</Label>
        <Input
          id="allergens"
          value={allergens}
          onChange={(e) => setAllergens(e.target.value)}
          className="mt-1"
          placeholder="gluten, dairy, nuts"
        />
      </div>

      <Button type="submit" className="w-full" disabled={isSubmitting}>
        {isSubmitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
        Save Item
      </Button>
    </form>
  );
}
