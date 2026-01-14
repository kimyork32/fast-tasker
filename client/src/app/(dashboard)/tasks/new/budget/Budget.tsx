import React from 'react';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';

export default function Budget({ budget, setBudget, onBack, onSubmit }: {
  budget: string;
  setBudget: (v: string) => void;
  onBack: () => void;
  onSubmit: () => void;
}) {
  return (
    <div className="space-y-6">
      <div>
        <h3 className="text-lg font-medium">Presupuesto</h3>
        <p className="text-sm text-muted-foreground">
          Establezca su precio para esta tarea.
        </p>
      </div>
      <div className="space-y-2">
        <Label htmlFor="budget">Precio (S/.)</Label>
        <Input id="budget" type="number" placeholder="por ejemplo, 40" value={budget} onChange={e => setBudget(e.target.value)} required />
      </div>
      <div className="flex justify-between">
        <Button variant="outline" onClick={onBack}>
          Atrás
        </Button>
        <Button onClick={onSubmit}>
          Enviar Tarea
        </Button>
      </div>
    </div>
  );
}
