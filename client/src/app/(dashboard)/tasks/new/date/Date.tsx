import React from 'react';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';

export default function DateStep({ date, setDate, onBack, onNext }: {
  date: string;
  setDate: (v: string) => void;
  onBack: () => void;
  onNext: () => void;
}) {
  return (
    <div className="space-y-6">
      <div>
        <h3 className="text-lg font-medium">Date</h3>
        <p className="text-sm text-muted-foreground">
          ¿Cuando debe realizarse la tarea?
        </p>
      </div>
      <div className="space-y-2">
        <Label htmlFor="date">Fecha de la Tarea</Label>
        <Input id="date" type="date" value={date} onChange={e => setDate(e.target.value)} required />
      </div>
      <div className="flex justify-between">
        <Button variant="outline" onClick={onBack}>
          Atrás
        </Button>
        <Button onClick={onNext}>Siguiente</Button>
      </div>
    </div>
  );
}
