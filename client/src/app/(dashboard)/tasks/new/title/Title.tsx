import React from 'react';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';

export default function Title({ title, setTitle, onNext }: {
  title: string;
  setTitle: (v: string) => void;
  onNext: () => void;
}) {
  return (
    <div className="space-y-6">
      <div>
        <h3 className="text-lg font-medium">Título de la Tarea</h3>
        <p className="text-sm text-muted-foreground">
          Empecemos por lo básico. ¿Qué necesitas hacer?
        </p>
      </div>
      <div className="space-y-2">
        <Label htmlFor="title">Título de la Tarea</Label>
        <Input id="title" placeholder="Por ejemplo, necesito alguien para pintar las paredes de mi sala." value={title} onChange={e => setTitle(e.target.value)} required />
      </div>
      <div className="flex justify-end">
        <Button onClick={onNext}>Siguiente</Button>
      </div>
    </div>
  );
}