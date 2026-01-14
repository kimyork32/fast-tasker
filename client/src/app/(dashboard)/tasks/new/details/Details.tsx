import React from 'react';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';

export default function Details({ details, setDetails, onNext, onBack }: {
  details: string;
  setDetails: (v: string) => void;
  onNext: () => void;
  onBack: () => void;
}) {
  return (
    <div className="space-y-6">
      <div>
        <h3 className="text-lg font-medium">Detalles</h3>
        <p className="text-sm text-muted-foreground">
          Proporcione más detalles sobre su tarea.
        </p>
      </div>
      <div className="space-y-2">
        <Label htmlFor="details">Detalles de la Tarea</Label>
        <textarea
          id="details"
          placeholder="Describe lo que necesitas hacer, cualquier requisito, etc."
          value={details}
          onChange={e => setDetails(e.target.value)}
          className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
          required
        />
      </div>
      <div className="flex justify-between">
        <Button variant="outline" onClick={onBack}>
          Atrás
        </Button>
        <Button onClick={onNext}>
          Siguiente
        </Button>
      </div>
    </div>
  );
}
