import React, { useState } from 'react';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';
import AutocompleteDistrito from '@/components/shared/AutocompleteDistrito';

export default function Location({ location, onNext, onBack }: {
  location: string;
  onNext: (data: { location: string, zip: string }) => void;
  onBack: () => void;
}) {
  const [currentLocation, setCurrentLocation] = useState(location);
  const [currentZip, setCurrentZip] = useState('');

  const handleNext = () => {
    onNext({ location: currentLocation, zip: currentZip });
  };

  return (
    <div className="space-y-6">
      <div>
        <h3 className="text-lg font-medium">Ubicación</h3>
        <p className="text-sm text-muted-foreground">
          ¿Dónde es necesario realizar esta tarea?
        </p>
      </div>
      <div className="space-y-2">
        <Label htmlFor="location">Location</Label>
        <AutocompleteDistrito
          value={currentLocation}
          onSelect={(item) => {
            // Construimos la cadena completa para la dirección
            const fullAddress = `${item.distrito}, ${item.provincia}, ${item.departamento}`;
            setCurrentLocation(fullAddress); // Actualizamos el estado local
            setCurrentZip(String(item.zip_code)); // Actualizamos el estado local
          }}
        />
      </div>
      <div className="flex justify-between">
        <Button variant="outline" onClick={onBack}>
          Atrás
        </Button>
        <Button onClick={handleNext} disabled={!currentLocation || !currentZip}>
          Siguiente
        </Button>
      </div>
    </div>
  );
}
