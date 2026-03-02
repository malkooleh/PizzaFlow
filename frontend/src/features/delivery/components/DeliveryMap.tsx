import Map, { Marker, Source, Layer } from "react-map-gl";
import type { LineLayerSpecification } from "mapbox-gl";
import { Navigation, MapPin } from "lucide-react";
import { Card } from "@/components/ui/card";
import type { DeliveryLocation, DeliveryResponse } from "@/types/models";

interface DeliveryMapProps {
  delivery: DeliveryResponse;
  courierLocation?: DeliveryLocation;
}

const ROUTE_LAYER: LineLayerSpecification = {
  id: "route",
  type: "line",
  source: "route",
  layout: { "line-join": "round", "line-cap": "round" },
  paint: { "line-color": "#3b82f6", "line-width": 4, "line-opacity": 0.8 },
};

/** Parse "lat,lng" strings or fall back to null. */
function parseCoords(
  address: string,
): { latitude: number; longitude: number } | null {
  const parts = address.split(",").map(Number);
  if (parts.length === 2 && !parts.some(isNaN)) {
    return { latitude: parts[0], longitude: parts[1] };
  }
  return null;
}

export function DeliveryMap({ delivery, courierLocation }: DeliveryMapProps) {
  const token = import.meta.env.VITE_MAPBOX_TOKEN as string | undefined;

  if (!token) {
    return (
      <Card className="flex h-48 items-center justify-center text-sm text-muted-foreground">
        Set <code className="mx-1 font-mono text-xs">VITE_MAPBOX_TOKEN</code> to
        enable the map.
      </Card>
    );
  }

  const deliveryCoords =
    delivery.currentLocation ??
    parseCoords(delivery.deliveryAddress);

  const pickupCoords = parseCoords(delivery.pickupAddress);

  const center = courierLocation ??
    deliveryCoords ??
    pickupCoords ?? { latitude: 50.45, longitude: 30.52 };

  const routeCoordinates: [number, number][] = [
    pickupCoords
      ? [pickupCoords.longitude, pickupCoords.latitude]
      : undefined,
    courierLocation
      ? [courierLocation.longitude, courierLocation.latitude]
      : undefined,
    deliveryCoords
      ? [deliveryCoords.longitude, deliveryCoords.latitude]
      : undefined,
  ].filter(Boolean) as [number, number][];

  return (
    <div className="h-64 overflow-hidden rounded-lg">
      <Map
        mapboxAccessToken={token}
        initialViewState={{
          longitude: center.longitude,
          latitude: center.latitude,
          zoom: 13,
        }}
        style={{ width: "100%", height: "100%" }}
        mapStyle="mapbox://styles/mapbox/streets-v12"
      >
        {/* Route line */}
        {routeCoordinates.length >= 2 && (
          <Source
            id="route"
            type="geojson"
            data={{
              type: "Feature",
              geometry: { type: "LineString", coordinates: routeCoordinates },
              properties: {},
            }}
          >
            <Layer {...ROUTE_LAYER} />
          </Source>
        )}

        {/* Pickup marker */}
        {pickupCoords && (
          <Marker
            longitude={pickupCoords.longitude}
            latitude={pickupCoords.latitude}
            anchor="bottom"
          >
            <div className="rounded-full bg-blue-500 p-1 text-white shadow-md">
              <Navigation className="h-4 w-4" />
            </div>
          </Marker>
        )}

        {/* Courier (live location) */}
        {courierLocation && (
          <Marker
            longitude={courierLocation.longitude}
            latitude={courierLocation.latitude}
            anchor="center"
          >
            <div className="h-4 w-4 rounded-full border-2 border-white bg-blue-600 shadow-md" />
          </Marker>
        )}

        {/* Delivery destination */}
        {deliveryCoords && (
          <Marker
            longitude={deliveryCoords.longitude}
            latitude={deliveryCoords.latitude}
            anchor="bottom"
          >
            <div className="rounded-full bg-red-500 p-1 text-white shadow-md">
              <MapPin className="h-4 w-4" />
            </div>
          </Marker>
        )}
      </Map>
    </div>
  );
}
